package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.ChatMessageEvent;
import adris.altoclef.eventbus.events.GameOverlayEvent;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.ArrayUtils;

public class PlaceBedAndSetSpawnTask extends Task {
  private final TimerGame regionScanTimer = new TimerGame(9.0D);
  
  private final Vec3i BED_CLEAR_SIZE = new Vec3i(3, 2, 3);
  
  private final Vec3i[] BED_BOTTOM_PLATFORM = new Vec3i[] { new Vec3i(0, -1, 0), new Vec3i(1, -1, 0), new Vec3i(2, -1, 0), new Vec3i(0, -1, -1), new Vec3i(1, -1, -1), new Vec3i(2, -1, -1), new Vec3i(0, -1, 1), new Vec3i(1, -1, 1), new Vec3i(2, -1, 1) };
  
  private final Vec3i BED_PLACE_STAND_POS = new Vec3i(0, 0, 1);
  
  private final Vec3i BED_PLACE_POS = new Vec3i(1, 0, 1);
  
  private final Vec3i[] BED_PLACE_POS_OFFSET = new Vec3i[] { 
      this.BED_PLACE_POS, this.BED_PLACE_POS
      
      .north(), this.BED_PLACE_POS
      .south(), this.BED_PLACE_POS
      .east(), this.BED_PLACE_POS
      .west(), this.BED_PLACE_POS
      .add(-1, 0, 1), this.BED_PLACE_POS
      .add(1, 0, 1), this.BED_PLACE_POS
      .add(-1, 0, -1), this.BED_PLACE_POS
      .add(1, 0, -1), this.BED_PLACE_POS
      .north(2), 
      this.BED_PLACE_POS
      .south(2), this.BED_PLACE_POS
      .east(2), this.BED_PLACE_POS
      .west(2), this.BED_PLACE_POS
      .add(-2, 0, 1), this.BED_PLACE_POS
      .add(-2, 0, 2), this.BED_PLACE_POS
      .add(2, 0, 1), this.BED_PLACE_POS
      .add(2, 0, 2), this.BED_PLACE_POS
      .add(-2, 0, -1), this.BED_PLACE_POS
      .add(-2, 0, -2), this.BED_PLACE_POS
      .add(2, 0, -1), 
      this.BED_PLACE_POS
      .add(2, 0, -2) };
  
  private final Direction BED_PLACE_DIRECTION = Direction.UP;
  
  private final TimerGame bedInteractTimeout = new TimerGame(5.0D);
  
  private final TimerGame inBedTimer = new TimerGame(1.0D);
  
  private final MovementProgressChecker progressChecker = new MovementProgressChecker();
  
  private boolean stayInBed;
  
  private BlockPos currentBedRegion;
  
  private BlockPos currentStructure;
  
  private BlockPos currentBreak;
  
  private boolean spawnSet;
  
  private Subscription<ChatMessageEvent> respawnPointSetMessageCheck;
  
  private Subscription<GameOverlayEvent> respawnFailureMessageCheck;
  
  private boolean sleepAttemptMade;
  
  private boolean wasSleeping;
  
  private BlockPos bedForSpawnPoint;
  
  public adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask stayInBed() {
    Debug.logInternal("Stay in bed method called");
    this.stayInBed = true;
    Debug.logInternal("Setting _stayInBed to true");
    return this;
  }
  
  protected void onStart() {
    AltoClefController mod = controller;
    mod.getBehaviour().push();
    this.progressChecker.reset();
    this.currentBedRegion = null;
    mod.getBehaviour().avoidBlockPlacing(pos -> {
          if (this.currentBedRegion != null) {
            BlockPos start = this.currentBedRegion;
            BlockPos end = this.currentBedRegion.add(this.BED_CLEAR_SIZE);
            return (start.getX() <= pos.getX() && pos.getX() < end.getX() && start.getZ() <= pos.getZ() && pos.getZ() < end.getZ() && start.getY() <= pos.getY() && pos.getY() < end.getY());
          } 
          return false;
        });
    mod.getBehaviour().avoidBlockBreaking(pos -> {
          if (this.currentBedRegion != null)
            for (Vec3i baseOffs : this.BED_BOTTOM_PLATFORM) {
              BlockPos base = this.currentBedRegion.add(baseOffs);
              if (base.equals(pos))
                return true; 
            }  
          return (mod.getWorld() != null) ? (mod.getWorld().getBlockState(pos).getBlock() instanceof BedBlock) : false;
        });
    this.spawnSet = false;
    this.sleepAttemptMade = false;
    this.wasSleeping = false;
    this.respawnPointSetMessageCheck = EventBus.subscribe(ChatMessageEvent.class, evt -> {
          String msg = evt.toString();
          if (msg.contains("Respawn point set")) {
            this.spawnSet = true;
            this.inBedTimer.reset();
          } 
        });
    this.respawnFailureMessageCheck = EventBus.subscribe(GameOverlayEvent.class, evt -> {
          String[] NEUTRAL_MESSAGES = { "You can sleep only at night", "You can only sleep at night", "You may not rest now; there are monsters nearby" };
          for (String checkMessage : NEUTRAL_MESSAGES) {
            if (evt.message.contains(checkMessage)) {
              if (!this.sleepAttemptMade)
                this.bedInteractTimeout.reset(); 
              this.sleepAttemptMade = true;
            } 
          } 
        });
    Debug.logInternal("Started onStart() method");
    Debug.logInternal("Current bed region: " + String.valueOf(this.currentBedRegion));
    Debug.logInternal("Spawn set: " + this.spawnSet);
  }
  
  public void resetSleep() {
    this.spawnSet = false;
    this.sleepAttemptMade = false;
    this.wasSleeping = false;
  }
  
  protected Task onTick() {
    AltoClefController mod = controller;
    if (!this.progressChecker.check(mod) && this.currentBedRegion != null) {
      this.progressChecker.reset();
      Debug.logMessage("Searching new bed region.");
      this.currentBedRegion = null;
    } 
    if (WorldHelper.isInNetherPortal(controller)) {
      setDebugState("We are in nether portal. Wandering");
      this.currentBedRegion = null;
      return (Task)new TimeoutWanderTask();
    } 
    if (WorldHelper.getCurrentDimension(controller) != Dimension.OVERWORLD) {
      setDebugState("Going to the overworld first.");
      return (Task)new DefaultGoToDimensionTask(Dimension.OVERWORLD);
    } 
    Screen screen = (MinecraftClient.getInstance()).currentScreen;
    if (screen instanceof net.minecraft.client.gui.screen.SleepingChatScreen) {
      this.progressChecker.reset();
      setDebugState("Sleeping...");
      this.wasSleeping = true;
      this.spawnSet = true;
      return null;
    } 
    if (this.sleepAttemptMade && 
      this.bedInteractTimeout.elapsed()) {
      Debug.logMessage("Failed to get \"Respawn point set\" message or sleeping, assuming that this bed already contains our spawn.");
      this.spawnSet = true;
      return null;
    } 
    if (mod.getBlockScanner().anyFound(blockPos -> ((WorldHelper.canReach(controller, blockPos) && blockPos.isCenterWithinDistance((Position)mod.getPlayer().getPos(), 40.0D) && mod.getItemStorage().hasItem(ItemHelper.BED)) || (WorldHelper.canReach(controller, blockPos) && !mod.getItemStorage().hasItem(ItemHelper.BED))),
        
        ItemHelper.itemsToBlocks(ItemHelper.BED))) {
      setDebugState("Going to bed to sleep...");
      return new DoToClosestBlockTask(toSleepIn -> {
        boolean closeEnough = toSleepIn.isWithinDistance(new Vec3i((int) mod.getPlayer().getPos().x, (int) mod.getPlayer().getPos().y, (int) mod.getPlayer().getPos().z), 3);
        if (closeEnough) {
          // why 0.2? I'm tired.
          Vec3d centerBed = new Vec3d(toSleepIn.getX() + 0.5, toSleepIn.getY() + 0.2, toSleepIn.getZ() + 0.5);
          BlockHitResult hit = LookHelper.raycast(mod.getPlayer(), centerBed, 6);
          // TODO: Kinda ugly, but I'm tired and fixing for the 2nd attempt speedrun so I will fix this block later
          closeEnough = false;
          if (hit.getType() != HitResult.Type.MISS) {
            // At this poinAt, if we miss, we probably are close enough.
            BlockPos p = hit.getBlockPos();
            if (ArrayUtils.contains(ItemHelper.itemsToBlocks(ItemHelper.BED), mod.getWorld().getBlockState(p).getBlock())) {
              // We have a bed!
              closeEnough = true;
            }
          }
        }
        bedForSpawnPoint = WorldHelper.getBedHead(controller, toSleepIn);
        if (bedForSpawnPoint == null) {
          bedForSpawnPoint = toSleepIn;
        }
        if (!closeEnough) {
          try {
            Direction face = mod.getWorld().getBlockState(toSleepIn).get(BedBlock.FACING);
            Direction side = face.rotateYClockwise();
                        /*
                        BlockPos targetMove = toSleepIn.offset(side).offset(side); // Twice, juust to make sure...
                         */
            return new GetToBlockTask(bedForSpawnPoint.add(side.getVector()));
          } catch (IllegalArgumentException e) {
            // If bed is not loaded, this will happen. In that case just get to the bed first.
          }
        } else {
          inBedTimer.reset();
        }
        if (closeEnough) {
          inBedTimer.reset();
        }
        // Keep track of where our spawn point is
        progressChecker.reset();
        return new InteractWithBlockTask(bedForSpawnPoint);
      }, ItemHelper.itemsToBlocks(ItemHelper.BED));
    } 
    if (mod.getPlayer().isTouchingWater() && mod.getItemStorage().hasItem(ItemHelper.BED)) {
      setDebugState("We are in water. Wandering");
      this.currentBedRegion = null;
      return (Task)new TimeoutWanderTask();
    } 
    if (this.currentBedRegion != null)
      for (Vec3i BedPlacePos : this.BED_PLACE_POS_OFFSET) {
        Block getBlock = mod.getWorld().getBlockState(this.currentBedRegion.add(BedPlacePos)).getBlock();
        if (getBlock instanceof BedBlock) {
          mod.getBlockScanner().addBlock(getBlock, this.currentBedRegion.add(BedPlacePos));
          break;
        } 
      }  
    if (!mod.getItemStorage().hasItem(ItemHelper.BED)) {
      setDebugState("Getting a bed first");
      return (Task)TaskCatalogue.getItemTask("bed", 1);
    } 
    if (this.currentBedRegion == null && 
      this.regionScanTimer.elapsed()) {
      Debug.logMessage("Rescanning for nearby bed place position...");
      this.regionScanTimer.reset();
      this.currentBedRegion = locateBedRegion(mod, mod.getPlayer().getBlockPos());
    } 
    if (this.currentBedRegion == null) {
      setDebugState("Searching for spot to place bed, wandering...");
      return (Task)new TimeoutWanderTask();
    } 
    for (Vec3i baseOffs : this.BED_BOTTOM_PLATFORM) {
      BlockPos blockPos = this.currentBedRegion.add(baseOffs);
      if (!WorldHelper.isSolidBlock(controller, blockPos)) {
        this.currentStructure = blockPos;
        break;
      } 
    } 
    int dx;
    label89: for (dx = 0; dx < this.BED_CLEAR_SIZE.getX(); dx++) {
      for (int dz = 0; dz < this.BED_CLEAR_SIZE.getZ(); dz++) {
        for (int dy = 0; dy < this.BED_CLEAR_SIZE.getY(); dy++) {
          BlockPos toClear = this.currentBedRegion.add(dx, dy, dz);
          if (WorldHelper.isSolidBlock(controller, toClear)) {
            this.currentBreak = toClear;
            break label89;
          } 
        } 
      } 
    } 
    if (this.currentStructure != null)
      if (WorldHelper.isSolidBlock(controller, this.currentStructure)) {
        this.currentStructure = null;
      } else {
        setDebugState("Placing structure for bed");
        return (Task)new PlaceStructureBlockTask(this.currentStructure);
      }  
    if (this.currentBreak != null)
      if (!WorldHelper.isSolidBlock(controller, this.currentBreak)) {
        this.currentBreak = null;
      } else {
        setDebugState("Clearing region for bed");
        return (Task)new DestroyBlockTask(this.currentBreak);
      }  
    BlockPos toStand = this.currentBedRegion.add(this.BED_PLACE_STAND_POS);
    if (!mod.getPlayer().getBlockPos().equals(toStand))
      return (Task)new GetToBlockTask(toStand); 
    BlockPos toPlace = this.currentBedRegion.add(this.BED_PLACE_POS);
    if (mod.getWorld().getBlockState(toPlace.offset(this.BED_PLACE_DIRECTION)).getBlock() instanceof BedBlock) {
      setDebugState("Waiting to rescan + find bed that we just placed. Should be almost instant.");
      this.progressChecker.reset();
      return null;
    } 
    setDebugState("Placing bed...");
    setDebugState("Filling in Portal");
    if (!this.progressChecker.check(mod)) {
      mod.getBaritone().getPathingBehavior().cancelEverything();
      mod.getBaritone().getPathingBehavior().forceCancel();
      mod.getBaritone().getExploreProcess().onLostControl();
      mod.getBaritone().getCustomGoalProcess().onLostControl();
      this.progressChecker.reset();
    } 
    if (thisOrChildSatisfies(task -> {
          if (task instanceof InteractWithBlockTask) {
            InteractWithBlockTask intr = (InteractWithBlockTask)task;
            return (intr.getClickStatus() == InteractWithBlockTask.ClickResponse.CLICK_ATTEMPTED);
          } 
          return false;
        }))
      mod.getInputControls().tryPress(Input.MOVE_BACK); 
    return (Task)new InteractWithBlockTask(new ItemTarget("bed", 1), this.BED_PLACE_DIRECTION, toPlace.offset(this.BED_PLACE_DIRECTION.getOpposite()), false);
  }
  
  protected void onStop(Task interruptTask) {
    controller.getBehaviour().pop();
    EventBus.unsubscribe(this.respawnPointSetMessageCheck);
    EventBus.unsubscribe(this.respawnFailureMessageCheck);
    Debug.logInternal("Tracking stopped for beds");
    Debug.logInternal("Behaviour popped");
    Debug.logInternal("Unsubscribed from respawn point set message");
    Debug.logInternal("Unsubscribed from respawn failure message");
  }
  
  protected boolean isEqual(Task other) {
    boolean isSameTask = other instanceof adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
    if (!isSameTask)
      Debug.logInternal("Tasks are not of the same type"); 
    return isSameTask;
  }
  
  protected String toDebugString() {
    return "Placing a bed nearby + resetting spawn point";
  }
  
  public boolean isFinished() {
    if (WorldHelper.getCurrentDimension(controller) != Dimension.OVERWORLD) {
      Debug.logInternal("Can't place spawnpoint/sleep in a bed unless we're in the overworld!");
      return true;
    } 
    boolean isSleeping = controller.getPlayer().isSleeping();
    boolean timerElapsed = this.inBedTimer.elapsed();
    boolean isFinished = (this.spawnSet && !isSleeping && timerElapsed);
    Debug.logInternal("isSleeping: " + isSleeping);
    Debug.logInternal("timerElapsed: " + timerElapsed);
    Debug.logInternal("isFinished: " + isFinished);
    return isFinished;
  }
  
  public BlockPos getBedSleptPos() {
    Debug.logInternal("Fetching bed slept position");
    return this.bedForSpawnPoint;
  }
  
  public boolean isSpawnSet() {
    Debug.logInternal("Checking if spawn is set");
    return this.spawnSet;
  }
  
  private BlockPos locateBedRegion(AltoClefController mod, BlockPos origin) {
    int SCAN_RANGE = 10;
    BlockPos closestGood = null;
    double closestDist = Double.POSITIVE_INFINITY;
    for (int x = origin.getX() - 10; x < origin.getX() + 10; x++) {
      for (int z = origin.getZ() - 10; z < origin.getZ() + 10; z++) {
        for (int y = origin.getY() - 10; y < origin.getY() + 10; y++) {
          BlockPos attemptPos = new BlockPos(x, y, z);
          double distance = BlockPosVer.getSquaredDistance(attemptPos, (Position)mod.getPlayer().getPos());
          Debug.logInternal("Checking position: " + String.valueOf(attemptPos));
          if (distance > closestDist) {
            Debug.logInternal("Skipping position: " + String.valueOf(attemptPos));
          } else if (isGoodPosition(mod, attemptPos)) {
            Debug.logInternal("Found good position: " + String.valueOf(attemptPos));
            closestGood = attemptPos;
            closestDist = distance;
          } 
        } 
      } 
    } 
    return closestGood;
  }
  
  private boolean isGoodPosition(AltoClefController mod, BlockPos pos) {
    BlockPos BED_CLEAR_SIZE = new BlockPos(2, 1, 2);
    for (int x = 0; x < BED_CLEAR_SIZE.getX(); x++) {
      for (int y = 0; y < BED_CLEAR_SIZE.getY(); y++) {
        for (int z = 0; z < BED_CLEAR_SIZE.getZ(); z++) {
          BlockPos checkPos = pos.add(x, y, z);
          if (!isGoodToPlaceInsideOrClear(mod, checkPos)) {
            Debug.logInternal("Not a good position: " + String.valueOf(checkPos));
            return false;
          } 
        } 
      } 
    } 
    Debug.logInternal("Good position");
    return true;
  }
  
  private boolean isGoodToPlaceInsideOrClear(AltoClefController mod, BlockPos pos) {
    Vec3i[] CHECK = { new Vec3i(0, 0, 0), new Vec3i(-1, 0, 0), new Vec3i(1, 0, 0), new Vec3i(0, 1, 0), new Vec3i(0, -1, 0), new Vec3i(0, 0, 1), new Vec3i(0, 0, -1) };
    for (Vec3i offset : CHECK) {
      BlockPos newPos = pos.add(offset);
      if (!isGoodAsBorder(mod, newPos)) {
        Debug.logInternal("Not good as border: " + String.valueOf(newPos));
        return false;
      } 
    } 
    Debug.logInternal("Good to place inside or clear");
    return true;
  }
  
  private boolean isGoodAsBorder(AltoClefController mod, BlockPos pos) {
    boolean isSolid = WorldHelper.isSolidBlock(controller, pos);
    Debug.logInternal("isSolid: " + isSolid);
    if (isSolid) {
      boolean canBreak = WorldHelper.canBreak(controller, pos);
      Debug.logInternal("canBreak: " + canBreak);
      return canBreak;
    } 
    boolean isAir = WorldHelper.isAir(controller, pos);
    Debug.logInternal("isAir: " + isAir);
    return isAir;
  }
}

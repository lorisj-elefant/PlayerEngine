package adris.altoclef.util.helpers;

import adris.altoclef.AltoClefController;
import adris.altoclef.mixins.EntityAccessor;
import adris.altoclef.multiversion.MethodWrapper;
import adris.altoclef.multiversion.world.WorldVer;
import adris.altoclef.util.Dimension;
import baritone.api.IBaritone;
import baritone.pathing.movement.CalculationContext;
import baritone.pathing.movement.MovementHelper;
import baritone.process.MineProcess;
import baritone.utils.BlockStateInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Holder;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public interface WorldHelper {
  
  static Vec3d toVec3d(BlockPos pos) {
    if (pos == null)
      return null; 
    return new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
  }
  
  static Vec3d toVec3d(Vec3i pos) {
    return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
  }
  
  static Vec3i toVec3i(Vec3d pos) {
    return new Vec3i((int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
  }
  
  static BlockPos toBlockPos(Vec3d pos) {
    return new BlockPos((int)pos.getX(), (int)pos.getY(), (int)pos.getZ());
  }
  
  static boolean isSourceBlock(AltoClefController controller, BlockPos pos, boolean onlyAcceptStill) {
    World world = controller.getWorld();
    BlockState s = world.getBlockState(pos);
    if (s.getBlock() instanceof net.minecraft.block.FluidBlock) {
      if (!s.getFluidState().isSource() && onlyAcceptStill)
        return false; 
      int level = s.getFluidState().getLevel();
      BlockState above = world.getBlockState(pos.up());
      if (above.getBlock() instanceof net.minecraft.block.FluidBlock)
        return false; 
      return (level == 8);
    } 
    return false;
  }
  
  static double distanceXZSquared(Vec3d from, Vec3d to) {
    Vec3d delta = to.subtract(from);
    return delta.x * delta.x + delta.z * delta.z;
  }
  
  static double distanceXZ(Vec3d from, Vec3d to) {
    return Math.sqrt(distanceXZSquared(from, to));
  }
  
  static boolean inRangeXZ(Vec3d from, Vec3d to, double range) {
    return (distanceXZSquared(from, to) < range * range);
  }
  
  static boolean inRangeXZ(BlockPos from, BlockPos to, double range) {
    return inRangeXZ(toVec3d(from), toVec3d(to), range);
  }
  
  static boolean inRangeXZ(Entity entity, Vec3d to, double range) {
    return inRangeXZ(entity.getPos(), to, range);
  }
  
  static boolean inRangeXZ(Entity entity, BlockPos to, double range) {
    return inRangeXZ(entity, toVec3d(to), range);
  }
  
  static boolean inRangeXZ(Entity entity, Entity to, double range) {
    return inRangeXZ(entity, to.getPos(), range);
  }
  
  static Dimension getCurrentDimension(AltoClefController controller) {
    World world = controller.getWorld();
    if (world == null)
      return Dimension.OVERWORLD; 
    if (world.getDimension().ultraWarm())
      return Dimension.NETHER; 
    if (world.getDimension().natural())
      return Dimension.OVERWORLD; 
    return Dimension.END;
  }
  
  static boolean isSolidBlock(AltoClefController controller, BlockPos pos) {
    World world = controller.getWorld();
    return world.getBlockState(pos).isSolidBlock((BlockView)world, pos);
  }
  
  static BlockPos getBedHead(AltoClefController controller, BlockPos posWithBed) {
    World world = controller.getWorld();
    BlockState state = world.getBlockState(posWithBed);
    if (state.getBlock() instanceof BedBlock) {
      Direction facing = (Direction)state.get((Property)BedBlock.FACING);
      if (((BedPart)world.getBlockState(posWithBed).get((Property)BedBlock.PART)).equals(BedPart.HEAD))
        return posWithBed; 
      return posWithBed.offset(facing);
    } 
    return null;
  }
  
  static BlockPos getBedFoot(AltoClefController controller, BlockPos posWithBed) {
    World world = controller.getWorld();
    BlockState state = world.getBlockState(posWithBed);
    if (state.getBlock() instanceof BedBlock) {
      Direction facing = (Direction)state.get((Property)BedBlock.FACING);
      if (((BedPart)world.getBlockState(posWithBed).get((Property)BedBlock.PART)).equals(BedPart.FOOT))
        return posWithBed; 
      return posWithBed.offset(facing.getOpposite());
    } 
    return null;
  }
  
  static int getGroundHeight(AltoClefController controller, int x, int z) {
    World world = controller.getWorld();
    for (int y = world.getTopY(); y >= world.getBottomY(); y--) {
      BlockPos check = new BlockPos(x, y, z);
      if (isSolidBlock(controller, check))
        return y; 
    } 
    return -1;
  }
  
  static BlockPos getADesertTemple(AltoClefController controller) {
    World world = controller.getWorld();
    List<BlockPos> stonePressurePlates = controller.getBlockScanner().getKnownLocations(new Block[] { Blocks.STONE_PRESSURE_PLATE });
    if (!stonePressurePlates.isEmpty())
      for (BlockPos pos : stonePressurePlates) {
        if (world.getBlockState(pos).getBlock() == Blocks.STONE_PRESSURE_PLATE && world
          .getBlockState(pos.down()).getBlock() == Blocks.CUT_SANDSTONE && world
          .getBlockState(pos.down(2)).getBlock() == Blocks.TNT)
          return pos; 
      }  
    return null;
  }
  
  static boolean isUnopenedChest(AltoClefController controller, BlockPos pos) {
    return controller.getItemStorage().getContainerAtPosition(pos).isEmpty();
  }
  
  static int getGroundHeight(AltoClefController controller, int x, int z, Block... groundBlocks) {
    World world = controller.getWorld();
    Set<Block> possibleBlocks = new HashSet<>(Arrays.asList(groundBlocks));
    for (int y = world.getTopY(); y >= world.getBottomY(); y--) {
      BlockPos check = new BlockPos(x, y, z);
      if (possibleBlocks.contains(world.getBlockState(check).getBlock()))
        return y; 
    } 
    return -1;
  }
  
  static boolean canBreak(AltoClefController controller, BlockPos pos) {
    AltoClefController altoClefController = controller;
    boolean prevInteractionPaused = altoClefController.getExtraBaritoneSettings().isInteractionPaused();
    altoClefController.getExtraBaritoneSettings().setInteractionPaused(false);
    boolean canBreak = (altoClefController.getWorld().getBlockState(pos).getHardness((BlockView) altoClefController.getWorld(), pos) >= 0.0F && !altoClefController.getExtraBaritoneSettings().shouldAvoidBreaking(pos) && MineProcess.plausibleToBreak(new CalculationContext((IBaritone) altoClefController.getBaritone()), pos) && canReach(controller, pos));
    altoClefController.getExtraBaritoneSettings().setInteractionPaused(prevInteractionPaused);
    return canBreak;
  }
  
  static boolean isInNetherPortal(AltoClefController controller) {
    LivingEntity player = controller.getPlayer();
    if (player == null)
      return false; 
    return ((EntityAccessor)player).isInNetherPortal();
  }
  
  static boolean canPlace(AltoClefController controller, BlockPos pos) {
    return (!controller.getExtraBaritoneSettings().shouldAvoidPlacingAt(pos) &&
      canReach(controller, pos));
  }
  
  static boolean canReach(AltoClefController controller, BlockPos pos) {
    AltoClefController altoClefController = controller;
    if (altoClefController.getModSettings().shouldAvoidOcean())
      if (altoClefController.getPlayer().getY() > 47.0D && altoClefController.getChunkTracker().isChunkLoaded(pos) && isOcean(altoClefController.getWorld().getBiome(pos)))
        if (pos.getY() < 64 && getGroundHeight(controller, pos.getX(), pos.getZ(), new Block[] { Blocks.WATER }) > pos.getY())
          return false;   
    return !altoClefController.getBlockScanner().isUnreachable(pos);
  }
  
  static boolean isOcean(Holder<Biome> b) {
    return (WorldVer.isBiome(b, Biomes.OCEAN) || 
      WorldVer.isBiome(b, Biomes.COLD_OCEAN) || 
      WorldVer.isBiome(b, Biomes.DEEP_COLD_OCEAN) || 
      WorldVer.isBiome(b, Biomes.DEEP_OCEAN) || 
      WorldVer.isBiome(b, Biomes.DEEP_FROZEN_OCEAN) || 
      WorldVer.isBiome(b, Biomes.DEEP_LUKEWARM_OCEAN) || 
      WorldVer.isBiome(b, Biomes.LUKEWARM_OCEAN) || 
      WorldVer.isBiome(b, Biomes.WARM_OCEAN) || 
      WorldVer.isBiome(b, Biomes.FROZEN_OCEAN));
  }
  
  static boolean isAir(AltoClefController controller, BlockPos pos) {
    return controller.getBlockScanner().isBlockAtPosition(pos, new Block[] { Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR });
  }
  
  static boolean isAir(Block block) {
    return (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR);
  }
  
  static boolean isInteractableBlock(AltoClefController controller, BlockPos pos) {
    Block block = controller.getWorld().getBlockState(pos).getBlock();
    return (block instanceof ChestBlock || block instanceof net.minecraft.block.EnderChestBlock || block instanceof net.minecraft.block.CraftingTableBlock || block instanceof net.minecraft.block.AbstractFurnaceBlock || block instanceof net.minecraft.block.LoomBlock || block instanceof net.minecraft.block.CartographyTableBlock || block instanceof net.minecraft.block.EnchantingTableBlock || block instanceof net.minecraft.block.RedstoneOreBlock || block instanceof net.minecraft.block.BarrelBlock);
  }
  
  static boolean isInsidePlayer(AltoClefController controller, BlockPos pos) {
    return pos.isCenterWithinDistance((Position) controller.getPlayer().getPos(), 2.0D);
  }
  
  static Iterable<BlockPos> getBlocksTouchingPlayer(LivingEntity player) {
    return getBlocksTouchingBox(player.getBoundingBox());
  }
  
  static Iterable<BlockPos> getBlocksTouchingBox(Box box) {
    BlockPos min = new BlockPos((int)box.minX, (int)box.minY, (int)box.minZ);
    BlockPos max = new BlockPos((int)box.maxX, (int)box.maxY, (int)box.maxZ);
    return scanRegion(min, max);
  }


  static Iterable<BlockPos> scanRegion(BlockPos start, BlockPos end) {
    return () -> new Iterator<>() {
      int x = start.getX(), y = start.getY(), z = start.getZ();

      @Override
      public boolean hasNext() {
        return y <= end.getY() && z <= end.getZ() && x <= end.getX();
      }

      @Override
      public BlockPos next() {
        BlockPos result = new BlockPos(x, y, z);
        ++x;
        if (x > end.getX()) {
          x = start.getX();
          ++z;
          if (z > end.getZ()) {
            z = start.getZ();
            ++y;
          }
        }
        return result;
      }
    };
  }

  static boolean fallingBlockSafeToBreak(AltoClefController controller, BlockPos pos) {
    BlockStateInterface bsi = new BlockStateInterface(controller.getBaritone().getEntityContext());
    World clientWorld = controller.getWorld();
    if (clientWorld == null)
      throw new AssertionError(); 
    while (isFallingBlock(controller, pos)) {
      if (MovementHelper.avoidBreaking(bsi, pos.getX(), pos.getY(), pos.getZ(), clientWorld.getBlockState(pos), controller.getBaritoneSettings()))
        return false; 
      pos = pos.up();
    } 
    return true;
  }
  
  static boolean isFallingBlock(AltoClefController controller, BlockPos pos) {
    World clientWorld = controller.getWorld();
    if (clientWorld == null)
      throw new AssertionError(); 
    return clientWorld.getBlockState(pos).getBlock() instanceof net.minecraft.block.FallingBlock;
  }
  
  static Entity getSpawnerEntity(AltoClefController controller, BlockPos pos) {
    World world = controller.getWorld();
    BlockState state = world.getBlockState(pos);
    if (state.getBlock() instanceof net.minecraft.block.SpawnerBlock) {
      BlockEntity be = world.getBlockEntity(pos);
      if (be instanceof MobSpawnerBlockEntity) {
        MobSpawnerBlockEntity blockEntity = (MobSpawnerBlockEntity)be;
        return MethodWrapper.getRenderedEntity(blockEntity.getLogic(), (World)world, pos);
      } 
    } 
    return null;
  }
  
  static boolean isChest(AltoClefController controller, BlockPos block) {
    Block b = controller.getWorld().getBlockState(block).getBlock();
    return isChest(b);
  }
  
  static boolean isChest(Block b) {
    return (b instanceof ChestBlock || b instanceof net.minecraft.block.EnderChestBlock);
  }
  
  static boolean isBlock(AltoClefController controller, BlockPos pos, Block block) {
    return controller.getWorld().getBlockState(pos).getBlock() == block;
  }
  
  static boolean canSleep(AltoClefController controller) {
    World world = controller.getWorld();
    if (world != null) {
      if (world.isThundering() && world.isRaining())
        return true; 
      int time = getTimeOfDay(controller);
      return (12542 <= time && time <= 23992);
    } 
    return false;
  }
  
  static int getTimeOfDay(AltoClefController controller) {
    World world = controller.getWorld();
    if (world != null)
      return (int)(world.getTimeOfDay() % 24000L); 
    return 0;
  }
  
  static boolean isVulnerable(LivingEntity player) {
    int armor = player.getArmor();
    float health = player.getHealth();
    if (armor <= 15 && health < 3.0F)
      return true; 
    if (armor < 10 && health < 10.0F)
      return true; 
    return (armor < 5 && health < 18.0F);
  }
  
  static boolean isSurroundedByHostiles(AltoClefController controller) {
    List<LivingEntity> hostiles = controller.getEntityTracker().getHostiles();
    return isSurrounded(controller, hostiles);
  }
  
  static boolean isSurrounded(AltoClefController controller, List<LivingEntity> entities) {
    LivingEntity player = controller.getPlayer();
    BlockPos playerPos = player.getBlockPos();
    int MIN_SIDES_TO_SURROUND = 2;
    List<Direction> uniqueSides = new ArrayList<>();
    for (Entity entity : entities) {
      if (!entity.isInRange((Entity)player, 8.0D))
        continue; 
      BlockPos entityPos = entity.getBlockPos();
      double angle = calculateAngle(playerPos, entityPos);
      boolean isUnique = !uniqueSides.contains(getHorizontalDirectionFromYaw(angle));
      if (isUnique)
        uniqueSides.add(getHorizontalDirectionFromYaw(angle)); 
    } 
    return (uniqueSides.size() >= 2);
  }
  
  private static double calculateAngle(BlockPos origin, BlockPos target) {
    double translatedX = (target.getX() - origin.getX());
    double translatedZ = (target.getZ() - origin.getZ());
    double angleRad = Math.atan2(translatedZ, translatedX);
    double angleDeg = Math.toDegrees(angleRad);
    angleDeg -= 90.0D;
    if (angleDeg < 0.0D)
      angleDeg += 360.0D; 
    return angleDeg;
  }
  
  private static Direction getHorizontalDirectionFromYaw(double yaw) {
    yaw %= 360.0D;
    if (yaw < 0.0D)
      yaw += 360.0D; 
    if ((yaw >= 45.0D && yaw < 135.0D) || (yaw >= -315.0D && yaw < -225.0D))
      return Direction.WEST; 
    if ((yaw >= 135.0D && yaw < 225.0D) || (yaw >= -225.0D && yaw < -135.0D))
      return Direction.NORTH; 
    if ((yaw >= 225.0D && yaw < 315.0D) || (yaw >= -135.0D && yaw < -45.0D))
      return Direction.EAST; 
    return Direction.SOUTH;
  }
}

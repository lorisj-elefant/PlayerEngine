package adris.altoclef.chains;

import adris.altoclef.AltoClefController;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.EntitySwungEvent;
import adris.altoclef.eventbus.events.PlayerDamageEvent;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class PlayerDefenseChain extends SingleTaskChain {
  private Map<String, DamageTarget> _damageTargets = new HashMap<>();
  
  private Map<Integer, TimerGame> _recentlySwung = new HashMap<>();
  
  private TimerGame _recentlyDamagedUnknown = new TimerGame(0.3D);
  
  private String _currentlyAttackingPlayer = null;
  
  private static int HITS_BEFORE_RETALIATION = 2;
  
  private static int HITS_BEFORE_RETALIATION_LOW_HEALTH = 1;
  
  private static int LOW_HEALTH_THRESHOLD = 14;
  
  private static double SWING_TIMEOUT = 0.4D;

  private AltoClefController mod;
  
  public PlayerDefenseChain(TaskRunner runner) {
    super(runner);
    this.mod = runner.getMod();
    EventBus.subscribe(PlayerDamageEvent.class, evt -> onPlayerDamage(evt.source.getAttacker()));
    EventBus.subscribe(EntitySwungEvent.class, evt -> onEntitySwung(evt.entity));
  }
  
  private void processMaybeDamaged() {
    if (this._recentlyDamagedUnknown == null || this._recentlyDamagedUnknown.elapsed()) {
      this._recentlyDamagedUnknown = null;
      return;
    } 
    this._recentlyDamagedUnknown = null;
    LivingEntity player = mod.getPlayer();
    for (Entity entity : mod.getWorld().iterateEntities()) {
      if (entity == null || (this._recentlySwung.containsKey(Integer.valueOf(entity.getId())) && ((TimerGame)this._recentlySwung.get(Integer.valueOf(entity.getId()))).elapsed())) {
        this._recentlySwung.remove(Integer.valueOf(entity.getId()));
        continue;
      } 
      if (entity == null)
        continue; 
      if (entity.distanceTo((Entity)player) > 5.0F)
        continue; 
      Vec3d playerCenter = player.getPos().add(new Vec3d(0.0D, player.getStandingEyeHeight(), 0.0D));
      if (entity.isAlive() && LookHelper.isLookingAt(entity, playerCenter, 60.0D)) {
        this._recentlySwung.remove(Integer.valueOf(entity.getId()));
        onPlayerDamage(entity);
        return;
      } 
    } 
  }
  
  private void onEntitySwung(Entity entity) {
    int id = entity.getId();
    TimerGame timeout = new TimerGame(SWING_TIMEOUT);
    timeout.reset();
    this._recentlySwung.put(Integer.valueOf(id), timeout);
    processMaybeDamaged();
  }
  
  private void onPlayerDamage(Entity damagedBy) {
    if (damagedBy == null) {
      if (this._recentlyDamagedUnknown == null || this._recentlyDamagedUnknown.elapsed()) {
        this._recentlyDamagedUnknown = new TimerGame(0.3D);
        this._recentlyDamagedUnknown.reset();
      } 
      processMaybeDamaged();
      return;
    } 
    LivingEntity clientPlayer = mod.getPlayer();
    this._recentlyDamagedUnknown = null;
    if (damagedBy instanceof PlayerEntity) {
      PlayerEntity player = (PlayerEntity)damagedBy;
      String offendingName = player.getName().getString();
      if (!this._damageTargets.containsKey(offendingName))
        this._damageTargets.put(offendingName, new DamageTarget()); 
      DamageTarget target = this._damageTargets.get(offendingName);
      if (target.forgetInstigationTimer.elapsed())
        target.timesHit = 0; 
      if (target.forgetAttackTimer.elapsed())
        target.attacking = false; 
      target.forgetInstigationTimer.reset();
      if (!target.attacking) {
        target.timesHit++;
        int hitsBeforeRetaliation = (clientPlayer.getHealth() < LOW_HEALTH_THRESHOLD) ? HITS_BEFORE_RETALIATION_LOW_HEALTH : HITS_BEFORE_RETALIATION;
        System.out.println("Another player hit us " + target.timesHit + "times: " + offendingName + ", attacking if they hit us " + (hitsBeforeRetaliation - target.timesHit) + " more time(s).");
        if (target.timesHit >= hitsBeforeRetaliation) {
          System.out.println("Too many attacks from another player! Retaliating attacks against offending player: " + offendingName);
          target.attacking = true;
          target.forgetAttackTimer.reset();
          target.timesHit = 0;
          this._currentlyAttackingPlayer = offendingName;
        } 
      } else {
        target.forgetAttackTimer.reset();
      } 
    } 
  }
  
  public float getPriority() {
    if (this._currentlyAttackingPlayer != null) {
      Optional<PlayerEntity> currentPlayerEntity = controller.getEntityTracker().getPlayerEntity(this._currentlyAttackingPlayer);
      if (!currentPlayerEntity.isPresent() || !((PlayerEntity)currentPlayerEntity.get()).isAlive())
        this._currentlyAttackingPlayer = null; 
    } 
    String[] playerNames = (String[])this._damageTargets.keySet().toArray(x$0 -> new String[x$0]);
    for (String potentialAttacker : playerNames) {
      if (potentialAttacker == null) {
        this._damageTargets.remove(potentialAttacker);
      } else {
        PlayerEntity potentialPlayer = controller.getEntityTracker().getPlayerEntity(potentialAttacker).orElse(null);
        if (potentialPlayer == null || !potentialPlayer.isAlive() || ((DamageTarget)this._damageTargets.get(potentialAttacker)).forgetAttackTimer.elapsed()) {
          System.out.println("Either forgot or killed player: " + potentialAttacker + " (no longer attacking)");
          this._damageTargets.remove(potentialAttacker);
          if (potentialAttacker.equals(this._currentlyAttackingPlayer))
            this._currentlyAttackingPlayer = null; 
        } 
      } 
    } 
    if (this._currentlyAttackingPlayer != null) {
      setTask((Task)new KillPlayerTask(this._currentlyAttackingPlayer));
      return 55.0F;
    } 
    return 0.0F;
  }
  
  public boolean isActive() {
    return true;
  }
  
  protected void onTaskFinish(AltoClefController mod) {}
  
  public String getName() {
    return "Player Defense";
  }

  static class DamageTarget {
    public TimerGame forgetInstigationTimer = new TimerGame(6);
    public TimerGame forgetAttackTimer = new TimerGame(30);
    public int timesHit = 0;
    public boolean attacking = false;

    public DamageTarget() {
      // init timers
      forgetInstigationTimer.reset();
      forgetAttackTimer.reset();
    }
  }
}

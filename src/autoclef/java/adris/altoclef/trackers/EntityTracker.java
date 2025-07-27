package adris.altoclef.trackers;

import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.PlayerCollidedWithEntityEvent;
import adris.altoclef.mixins.PersistentProjectileEntityAccessor;
import adris.altoclef.trackers.blacklisting.EntityLocateBlacklist;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.baritone.CachedProjectile;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.ProjectileHelper;
import adris.altoclef.util.helpers.WorldHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

public class EntityTracker extends Tracker {
  private final HashMap<Item, List<ItemEntity>> itemDropLocations = new HashMap<>();
  
  private final HashMap<Class, List<Entity>> entityMap = new HashMap<>();
  
  private final List<Entity> closeEntities = new ArrayList<>();
  
  private final List<LivingEntity> hostiles = new ArrayList<>();
  
  private final List<CachedProjectile> projectiles = new ArrayList<>();
  
  private final HashMap<String, PlayerEntity> playerMap = new HashMap<>();
  
  private final HashMap<String, Vec3d> playerLastCoordinates = new HashMap<>();
  
  private final EntityLocateBlacklist entityBlacklist = new EntityLocateBlacklist();
  
  private final HashMap<PlayerEntity, List<Entity>> entitiesCollidingWithPlayerAccumulator = new HashMap<>();
  
  private final HashMap<PlayerEntity, HashSet<Entity>> entitiesCollidingWithPlayer = new HashMap<>();
  
  public EntityTracker(TrackerManager manager) {
    super(manager);
    EventBus.subscribe(PlayerCollidedWithEntityEvent.class, evt -> registerPlayerCollision(evt.player, evt.other));
  }
  
  private static Class squashType(Class<?> type) {
    if (PlayerEntity.class.isAssignableFrom(type))
      return PlayerEntity.class; 
    return type;
  }
  
  private void registerPlayerCollision(PlayerEntity player, Entity entity) {
    if (!this.entitiesCollidingWithPlayerAccumulator.containsKey(player))
      this.entitiesCollidingWithPlayerAccumulator.put(player, new ArrayList<>()); 
    this.entitiesCollidingWithPlayerAccumulator.get(player).add(entity);
  }
  
  public boolean isCollidingWithPlayer(PlayerEntity player, Entity entity) {
    return (this.entitiesCollidingWithPlayer.containsKey(player) && ((HashSet)this.entitiesCollidingWithPlayer.get(player)).contains(entity));
  }
  
  public boolean isCollidingWithPlayer(Entity entity) {
    return isCollidingWithPlayer((PlayerEntity)this.mod.getPlayer(), entity);
  }
  
  public Optional<ItemEntity> getClosestItemDrop(Item... items) {
    return getClosestItemDrop(this.mod.getPlayer().getPos(), items);
  }
  
  public Optional<ItemEntity> getClosestItemDrop(Vec3d position, Item... items) {
    return getClosestItemDrop(position, entity -> true, items);
  }
  
  public Optional<ItemEntity> getClosestItemDrop(Vec3d position, ItemTarget... items) {
    return getClosestItemDrop(position, entity -> true, items);
  }
  
  public Optional<ItemEntity> getClosestItemDrop(Predicate<ItemEntity> acceptPredicate, Item... items) {
    return getClosestItemDrop(this.mod.getPlayer().getPos(), acceptPredicate, items);
  }
  
  public Optional<ItemEntity> getClosestItemDrop(Vec3d position, Predicate<ItemEntity> acceptPredicate, Item... items) {
    ensureUpdated();
    ItemTarget[] tempTargetList = new ItemTarget[items.length];
    for (int i = 0; i < items.length; i++)
      tempTargetList[i] = new ItemTarget(items[i], 9999999); 
    return getClosestItemDrop(position, acceptPredicate, tempTargetList);
  }
  
  public Optional<ItemEntity> getClosestItemDrop(Vec3d position, Predicate<ItemEntity> acceptPredicate, ItemTarget... targets) {
    ensureUpdated();
    if (targets.length == 0) {
      Debug.logError("You asked for the drop position of zero items... Most likely a typo.");
      return Optional.empty();
    } 
    if (!itemDropped(targets))
      return Optional.empty(); 
    ItemEntity closestEntity = null;
    float minCost = Float.POSITIVE_INFINITY;
    for (ItemTarget target : targets) {
      for (Item item : target.getMatches()) {
        if (itemDropped(new Item[] { item }))
          for (ItemEntity entity : this.itemDropLocations.get(item)) {
            if (this.entityBlacklist.unreachable(entity) || 
              !entity.getStack().getItem().equals(item) || 
              !acceptPredicate.test(entity))
              continue; 
            float cost = (float)BaritoneHelper.calculateGenericHeuristic(position, entity.getPos());
            if (cost < minCost) {
              minCost = cost;
              closestEntity = entity;
            } 
          }  
      } 
    } 
    return Optional.ofNullable(closestEntity);
  }
  
  private Class[] parsePossiblyNullEntityTypes(Class... entityTypes) {
    if (entityTypes == null)
      return (Class[])this.entityMap.keySet().toArray(x$0 -> new Class[x$0]); 
    return entityTypes;
  }
  
  public Optional<Entity> getClosestEntity(Class... entityTypes) {
    return getClosestEntity(this.mod.getPlayer().getPos(), entityTypes);
  }
  
  public Optional<Entity> getClosestEntity(Vec3d position, Class... entityTypes) {
    return getClosestEntity(position, entity -> true, entityTypes);
  }
  
  public Optional<Entity> getClosestEntity(Predicate<Entity> acceptPredicate, Class... entityTypes) {
    return getClosestEntity(this.mod.getPlayer().getPos(), acceptPredicate, entityTypes);
  }
  
  public Optional<Entity> getClosestEntity(Vec3d position, Predicate<Entity> acceptPredicate, Class... entityTypes) {
    entityTypes = parsePossiblyNullEntityTypes(entityTypes);
    Entity closestEntity = null;
    double minCost = Double.POSITIVE_INFINITY;
    for (Class toFind : entityTypes) {
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
        if (this.entityMap.containsKey(toFind))
          for (Entity entity : this.entityMap.get(toFind)) {
            if (this.entityBlacklist.unreachable(entity) || 
              !entity.isAlive() || 
              !acceptPredicate.test(entity))
              continue; 
            double cost = entity.squaredDistanceTo(position);
            if (cost < minCost) {
              minCost = cost;
              closestEntity = entity;
            } 
          }  
      } 
    } 
    return Optional.ofNullable(closestEntity);
  }
  
  public boolean itemDropped(Item... items) {
    ensureUpdated();
    for (Item item : items) {
      if (this.itemDropLocations.containsKey(item))
        for (ItemEntity entity : this.itemDropLocations.get(item)) {
          if (!this.entityBlacklist.unreachable(entity))
            return true; 
        }  
    } 
    return false;
  }
  
  public boolean itemDropped(ItemTarget... targets) {
    ensureUpdated();
    for (ItemTarget target : targets) {
      if (itemDropped(target.getMatches()))
        return true; 
    } 
    return false;
  }
  
  public List<ItemEntity> getDroppedItems() {
    ensureUpdated();
    return this.itemDropLocations.values().stream().reduce(new ArrayList<>(), (result, drops) -> {
          result.addAll(drops);
          return result;
        });
  }
  
  public boolean entityFound(Predicate<Entity> shouldAccept, Class... types) {
    ensureUpdated();
    types = parsePossiblyNullEntityTypes(types);
    for (Class type : types) {
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
        for (Entity entity : this.entityMap.getOrDefault(type, Collections.emptyList())) {
          if (shouldAccept.test(entity))
            return true; 
        } 
      } 
    } 
    return false;
  }
  
  public boolean entityFound(Class... types) {
    return entityFound(check -> true, types);
  }
  
  public <T extends Entity> List<T> getTrackedEntities(Class<T> type) {
    ensureUpdated();
    if (!entityFound(new Class[] { type }))
      return Collections.emptyList(); 
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return (List<T>)this.entityMap.get(type);
    } 
  }
  
  public List<Entity> getCloseEntities() {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return this.closeEntities;
    } 
  }
  
  public List<CachedProjectile> getProjectiles() {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return this.projectiles;
    } 
  }
  
  public List<LivingEntity> getHostiles() {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return this.hostiles;
    } 
  }
  
  public boolean isPlayerLoaded(String name) {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return this.playerMap.containsKey(name);
    } 
  }
  
  public List<String> getAllLoadedPlayerUsernames() {
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return new ArrayList<>(this.playerMap.keySet());
    } 
  }
  
  public Optional<Vec3d> getPlayerMostRecentPosition(String name) {
    ensureUpdated();
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      return Optional.ofNullable(this.playerLastCoordinates.getOrDefault(name, null));
    } 
  }
  
  public Optional<PlayerEntity> getPlayerEntity(String name) {
    if (isPlayerLoaded(name))
      synchronized (BaritoneHelper.MINECRAFT_LOCK) {
        return Optional.of(this.playerMap.get(name));
      }  
    return Optional.empty();
  }
  
  public void requestEntityUnreachable(Entity entity) {
    this.entityBlacklist.blackListItem(this.mod, entity, 3);
  }
  
  public boolean isEntityReachable(Entity entity) {
    return !this.entityBlacklist.unreachable(entity);
  }
  
  protected synchronized void updateState() {
    synchronized (BaritoneHelper.MINECRAFT_LOCK) {
      this.itemDropLocations.clear();
      this.entityMap.clear();
      this.closeEntities.clear();
      this.projectiles.clear();
      this.hostiles.clear();
      this.playerMap.clear();
      if (mod.getWorld() == null)
        return; 
      this.entitiesCollidingWithPlayer.clear();
      for (Map.Entry<PlayerEntity, List<Entity>> collisions : this.entitiesCollidingWithPlayerAccumulator.entrySet()) {
        this.entitiesCollidingWithPlayer.put(collisions.getKey(), new HashSet<>());
        ((HashSet)this.entitiesCollidingWithPlayer.get(collisions.getKey())).addAll(collisions.getValue());
      } 
      this.entitiesCollidingWithPlayerAccumulator.clear();
      for (Entity entity : mod.getWorld().iterateEntities()) {
        Class<?> type = entity.getClass();
        type = squashType(type);
        if (entity == null || !entity.isAlive())
          continue; 
        if (type == PlayerEntity.class && entity.equals(this.mod.getPlayer()))
          continue; 
        if (!this.entityMap.containsKey(type))
          this.entityMap.put(type, new ArrayList<>()); 
        ((List<Entity>)this.entityMap.get(type)).add(entity);
        if (this.mod.getControllerExtras().inRange(entity))
          this.closeEntities.add(entity); 
        if (entity instanceof ItemEntity) {
          ItemEntity ientity = (ItemEntity)entity;
          Item droppedItem = ientity.getStack().getItem();
          if (ientity.isOnGround() || ientity.isTouchingWater() || WorldHelper.isSolidBlock(mod, ientity.getBlockPos().down(2)) || WorldHelper.isSolidBlock(mod, ientity.getBlockPos().down(3))) {
            if (!this.itemDropLocations.containsKey(droppedItem))
              this.itemDropLocations.put(droppedItem, new ArrayList<>()); 
            ((List<ItemEntity>)this.itemDropLocations.get(droppedItem)).add(ientity);
          } 
        } 
        if (entity instanceof net.minecraft.entity.mob.MobEntity) {
          if (EntityHelper.isAngryAtPlayer(this.mod, entity)) {
            boolean closeEnough = entity.isInRange((Entity)this.mod.getPlayer(), 26.0D);
            if (closeEnough)
              this.hostiles.add((LivingEntity)entity); 
          } 
          continue;
        } 
        if (entity instanceof ProjectileEntity) {
          ProjectileEntity projEntity = (ProjectileEntity)entity;
          if (!this.mod.getBehaviour().shouldAvoidDodgingProjectile(entity)) {
            CachedProjectile proj = new CachedProjectile();
            boolean inGround = false;
            if (entity instanceof net.minecraft.entity.projectile.PersistentProjectileEntity)
              inGround = ((PersistentProjectileEntityAccessor)entity).isInGround(); 
            if (projEntity instanceof net.minecraft.entity.projectile.FishingBobberEntity || projEntity instanceof net.minecraft.entity.projectile.thrown.EnderPearlEntity || projEntity instanceof net.minecraft.entity.projectile.thrown.ExperienceBottleEntity)
              continue; 
            if (!inGround) {
              proj.position = projEntity.getPos();
              proj.velocity = projEntity.getVelocity();
              proj.gravity = ProjectileHelper.hasGravity(projEntity) ? 0.05000000074505806D : 0.0D;
              proj.projectileType = projEntity.getClass();
              this.projectiles.add(proj);
            } 
          } 
          continue;
        } 
        if (entity instanceof PlayerEntity) {
          PlayerEntity player = (PlayerEntity)entity;
          String name = player.getName().getString();
          this.playerMap.put(name, player);
          this.playerLastCoordinates.put(name, player.getPos());
        } 
      } 
    } 
  }
  
  protected void reset() {
    this.entityBlacklist.clear();
  }
}

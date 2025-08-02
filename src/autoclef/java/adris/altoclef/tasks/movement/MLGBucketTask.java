package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClefController;
import adris.altoclef.Debug;
import adris.altoclef.control.InputControls;
import adris.altoclef.multiversion.DamageSourceVer;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ConfigHelper;
import adris.altoclef.util.helpers.EntityHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.MathsHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.serialization.ItemDeserializer;
import adris.altoclef.util.serialization.ItemSerializer;
import baritone.api.IBaritone;
import baritone.api.utils.IEntityContext;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MLGBucketTask extends Task {
    private static MLGClutchConfig config;

    private BlockPos placedPos;

    private BlockPos movingTorwards;

    static {
        ConfigHelper.loadConfig("configs/mlg_clutch_settings.json", adris.altoclef.tasks.movement.MLGBucketTask.MLGClutchConfig::new, MLGClutchConfig.class, newConfig -> config = newConfig);
    }

    private static boolean isLava(AltoClefController controller, BlockPos pos) {
        assert controller.getWorld() != null;
        return (controller.getWorld().getBlockState(pos).getBlock() == Blocks.LAVA);
    }

    private static boolean lavaWillProtect(AltoClefController controller, BlockPos pos) {
        assert controller.getWorld() != null;
        BlockState state = controller.getWorld().getBlockState(pos);
        if (state.getBlock() == Blocks.LAVA) {
            int level = state.getFluidState().getLevel();
            return (level == 0 || level >= config.lavaLevelOrGreaterWillCancelFallDamage);
        }
        return false;
    }

    private static boolean isWater(AltoClefController controller, BlockPos pos) {
        assert controller.getWorld() != null;
        return (controller.getWorld().getBlockState(pos).getBlock() == Blocks.WATER);
    }

    private static boolean canTravelToInAir(AltoClefController controller, BlockPos pos) {
        LivingEntity clientPlayerEntity = controller.getPlayer();
        assert clientPlayerEntity != null;
        double verticalDist = clientPlayerEntity.getPos().getY() - pos.getY() - 1.0D;
        double verticalVelocity = -1.0D * (clientPlayerEntity.getVelocity()).y;
        double grav = 0.08D;
        double movementSpeedPerTick = config.averageHorizontalMovementSpeedPerTick;
        double ticksToTravelSq = (-verticalVelocity + Math.sqrt(verticalVelocity * verticalVelocity + 2.0D * grav * verticalDist)) / grav;
        double maxMoveDistanceSq = movementSpeedPerTick * movementSpeedPerTick * ticksToTravelSq * ticksToTravelSq;
        double horizontalDistance = WorldHelper.distanceXZ(clientPlayerEntity.getPos(), WorldHelper.toVec3d(pos)) - 0.8D;
        if (horizontalDistance < 0.0D)
            horizontalDistance = 0.0D;
        return (maxMoveDistanceSq > horizontalDistance * horizontalDistance);
    }

    private static boolean isFallDeadly(AltoClefController controller, BlockPos pos) {
        LivingEntity clientPlayerEntity = controller.getPlayer();
        double damage = calculateFallDamageToLandOn(controller, pos);
        assert controller.getWorld() != null;
        Block b = controller.getWorld().getBlockState(pos).getBlock();
        if (b == Blocks.HAY_BLOCK)
            damage *= 0.20000000298023224D;
        assert clientPlayerEntity != null;
        double resultingHealth = (clientPlayerEntity.getHealth() - (float) damage);
        return (resultingHealth < config.preferLavaWhenFallDropsHealthBelowThreshold);
    }

    private static double calculateFallDamageToLandOn(AltoClefController controller, BlockPos pos) {
        World world = controller.getWorld();
        LivingEntity clientPlayerEntity = controller.getPlayer();
        assert clientPlayerEntity != null;
        double totalFallDistance = (clientPlayerEntity).fallDistance + clientPlayerEntity.getY() - pos.getY() - 1.0D;
        double baseFallDamage = MathHelper.ceil(totalFallDistance - 3.0D);
        assert world != null;
        return EntityHelper.calculateResultingPlayerDamage(clientPlayerEntity, DamageSourceVer.getFallDamageSource((World) world), baseFallDamage);
    }

    private static void moveLeftRight(AltoClefController controller, int delta) {
        InputControls controls = controller.getInputControls();
        if (delta == 0) {
            controls.release(Input.MOVE_LEFT);
            controls.release(Input.MOVE_RIGHT);
        } else if (delta > 0) {
            controls.release(Input.MOVE_LEFT);
            controls.hold(Input.MOVE_RIGHT);
        } else {
            controls.hold(Input.MOVE_LEFT);
            controls.release(Input.MOVE_RIGHT);
        }
    }

    private static void moveForwardBack(AltoClefController controller, int delta) {
        InputControls controls = controller.getInputControls();
        if (delta == 0) {
            controls.release(Input.MOVE_FORWARD);
            controls.release(Input.MOVE_BACK);
        } else if (delta > 0) {
            controls.hold(Input.MOVE_FORWARD);
            controls.release(Input.MOVE_BACK);
        } else {
            controls.release(Input.MOVE_FORWARD);
            controls.hold(Input.MOVE_BACK);
        }
    }

    private Task onTickInternal(AltoClefController mod, BlockPos oldMovingTorwards) {
        Optional<BlockPos> willLandOn = getBlockWeWillLandOn(mod);
        Optional<BlockPos> bestClutchPos = getBestConeClutchBlock(mod, oldMovingTorwards);
        if (bestClutchPos.isPresent()) {
            this.movingTorwards = (BlockPos) ((BlockPos) bestClutchPos.get()).mutableCopy();
            if (!this.movingTorwards.equals(oldMovingTorwards))
                if (oldMovingTorwards == null) {
                    Debug.logMessage("(NEW clutch target: " + String.valueOf(this.movingTorwards) + ")");
                } else {
                    Debug.logMessage("(changed clutch target: " + String.valueOf(this.movingTorwards) + ")");
                }
        } else if (oldMovingTorwards != null) {
            Debug.logMessage("(LOST clutch position!)");
        }
        if (willLandOn.isPresent()) {
            handleJumpForLand(mod, willLandOn.get());
            return placeMLGBucketTask(mod, willLandOn.get());
        }
        setDebugState("Wait for it...");
        mod.getInputControls().release(Input.JUMP);
        return null;
    }

    private Task placeMLGBucketTask(AltoClefController mod, BlockPos toPlaceOn) {
        if (!hasClutchItem(mod)) {
            setDebugState("No clutch item");
            return null;
        }
        if (!WorldHelper.isSolidBlock(controller, toPlaceOn))
            toPlaceOn = toPlaceOn.down();
        BlockPos willLandIn = toPlaceOn.up();
        BlockState willLandInState = mod.getWorld().getBlockState(willLandIn);
        if (willLandInState.getBlock() == Blocks.WATER) {
            setDebugState("Waiting to fall into water");
            mod.getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
            return null;
        }
        IEntityContext ctx = mod.getBaritone().getEntityContext();
        Optional<Rotation> reachable = RotationUtils.reachableCenter((Entity) ctx.entity(), toPlaceOn, ctx.playerController().getBlockReachDistance(), false);
        if (reachable.isPresent()) {
            setDebugState("Performing MLG");
            LookHelper.lookAt(controller, reachable.get());
            boolean hasClutch = (!mod.getWorld().getDimension().ultraWarm() && mod.getSlotHandler().forceEquipItem(Items.WATER_BUCKET));
            if (!hasClutch)
                if (!config.clutchItems.isEmpty())
                    for (Item tryEquip : config.clutchItems) {
                        if (mod.getSlotHandler().forceEquipItem(tryEquip)) {
                            hasClutch = true;
                            break;
                        }
                    }
            BlockPos[] toCheckLook = {toPlaceOn, toPlaceOn.up(), toPlaceOn.up(2)};
            if (hasClutch && Arrays.<BlockPos>stream(toCheckLook).anyMatch(check -> mod.getBaritone().getEntityContext().isLookingAt(check))) {
                Debug.logMessage("HIT: " + String.valueOf(willLandIn));
                this.placedPos = willLandIn;
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
            } else {
                setDebugState("NOT LOOKING CORRECTLY!");
            }
        } else {
            setDebugState("Waiting to reach target block...");
        }
        return null;
    }

    protected Task onTick() {
        AltoClefController mod = controller;
        mod.getInputControls().hold(Input.SPRINT);
        BlockPos.Mutable mutable = (this.movingTorwards != null) ? this.movingTorwards.mutableCopy() : null;
        this.movingTorwards = null;
        Task result = onTickInternal(mod, (BlockPos) mutable);
        handleForwardVelocity(mod, !Objects.equals(mutable, this.movingTorwards));
        handleCancellingSidewaysVelocity(mod);
        return result;
    }

    private void handleForwardVelocity(AltoClefController mod, boolean newForwardTarget) {
        if (mod.getPlayer().isOnGround() || this.movingTorwards == null || WorldHelper.inRangeXZ((Entity) mod.getPlayer(), this.movingTorwards, 0.05000000074505806D)) {
            moveForwardBack(mod, 0);
            return;
        }
        Rotation look = LookHelper.getLookRotation(controller);
        look = new Rotation(look.getYaw(), 0.0F);
        Vec3d forwardFacing = LookHelper.toVec3d(look).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3d delta = WorldHelper.toVec3d(this.movingTorwards).subtract(mod.getPlayer().getPos()).multiply(1.0D, 0.0D, 1.0D);
        Vec3d velocity = mod.getPlayer().getVelocity().multiply(1.0D, 0.0D, 1.0D);
        Vec3d pd = delta.subtract(velocity.multiply(3.0D));
        double forwardStrength = pd.dotProduct(forwardFacing);
        if (newForwardTarget)
            LookHelper.lookAt(mod, this.movingTorwards);
        Debug.logInternal("F:" + forwardStrength);
        moveForwardBack(mod, (int) Math.signum(forwardStrength));
    }

    protected void onStart() {
        controller.getBaritone().getPathingBehavior().forceCancel();
        this.placedPos = null;
        controller.getPlayer().setPitch(90.0F);
    }

    private void handleJumpForLand(AltoClefController mod, BlockPos willLandOn) {
        Box blockBounds;
        BlockPos willLandIn = WorldHelper.isSolidBlock(controller, willLandOn) ? willLandOn.up() : willLandOn;
        BlockState s = mod.getWorld().getBlockState(willLandIn);
        if (s.getBlock() == Blocks.LAVA) {
            mod.getInputControls().hold(Input.JUMP);
            return;
        }
        try {
            blockBounds = s.getCollisionShape((BlockView) mod.getWorld(), willLandIn).getBoundingBox();
        } catch (UnsupportedOperationException ex) {
            blockBounds = Box.of(WorldHelper.toVec3d(willLandIn), 1.0D, 1.0D, 1.0D);
        }
        boolean inside = mod.getPlayer().getBoundingBox().intersects(blockBounds);
        if (inside) {
            mod.getInputControls().hold(Input.JUMP);
        } else {
            mod.getInputControls().release(Input.JUMP);
        }
    }

    private Optional<BlockPos> getBlockWeWillLandOn(AltoClefController mod) {
        Vec3d velCheck = mod.getPlayer().getVelocity();
        velCheck.multiply(10.0D, 0.0D, 10.0D);
        Box b = mod.getPlayer().getBoundingBox().offset(velCheck);
        Vec3d c = b.getCenter();
        Vec3d[] coords = {c, new Vec3d(b.minX, c.y, b.minZ), new Vec3d(b.maxX, c.y, b.minZ), new Vec3d(b.minX, c.y, b.maxZ), new Vec3d(b.maxX, c.y, b.maxZ)};
        BlockHitResult result = null;
        double bestSqDist = Double.POSITIVE_INFINITY;
        for (Vec3d rayOrigin : coords) {
            RaycastContext rctx = castDown(rayOrigin);
            BlockHitResult hit = mod.getWorld().raycast(rctx);
            if (hit.getType() == HitResult.Type.BLOCK) {
                double curDis = hit.getPos().squaredDistanceTo(rayOrigin);
                if (curDis < bestSqDist) {
                    result = hit;
                    bestSqDist = curDis;
                }
            }
        }
        if (result == null || result.getType() != HitResult.Type.BLOCK)
            return Optional.empty();
        return Optional.ofNullable(result.getBlockPos());
    }

    private void handleCancellingSidewaysVelocity(AltoClefController mod) {
        if (this.movingTorwards == null) {
            moveLeftRight(mod, 0);
            return;
        }
        Vec3d velocity = mod.getPlayer().getVelocity();
        Vec3d deltaTarget = WorldHelper.toVec3d(this.movingTorwards).subtract(mod.getPlayer().getPos());
        Rotation look = LookHelper.getLookRotation(controller);
        Vec3d forwardFacing = LookHelper.toVec3d(look).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3d rightVelocity = MathsHelper.projectOntoPlane(velocity, forwardFacing).multiply(1.0D, 0.0D, 1.0D);
        Vec3d rightDelta = MathsHelper.projectOntoPlane(deltaTarget, forwardFacing).multiply(1.0D, 0.0D, 1.0D);
        Vec3d pd = rightDelta.subtract(rightVelocity.multiply(2.0D));
        Vec3d faceRight = forwardFacing.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
        boolean moveRight = (pd.dotProduct(faceRight) > 0.0D);
        if (moveRight) {
            moveLeftRight(mod, 1);
        } else {
            moveLeftRight(mod, -1);
        }
    }

    private Optional<BlockPos> getBestConeClutchBlock(AltoClefController mod, BlockPos oldClutchTarget) {
        double pitchHalfWidth = config.epicClutchConePitchAngle;
        double dpitchStart = pitchHalfWidth / config.epicClutchConePitchResolution;
        ConeClutchContext cctx = new ConeClutchContext(mod);
        if (oldClutchTarget != null)
            cctx.checkBlock(mod, oldClutchTarget);
        double pitch;
        for (pitch = dpitchStart; pitch <= pitchHalfWidth; pitch += pitchHalfWidth / config.epicClutchConePitchResolution) {
            double pitchProgress = (pitch - dpitchStart) / (pitchHalfWidth - dpitchStart);
            double yawResolution = config.epicClutchConeYawDivisionStart + pitchProgress * (config.epicClutchConeYawDivisionEnd - config.epicClutchConeYawDivisionStart);
            double yaw;
            for (yaw = 0.0D; yaw < 360.0D; yaw += 360.0D / yawResolution) {
                RaycastContext rctx = castCone(yaw, pitch);
                cctx.checkRay(mod, rctx);
            }
        }
        Vec3d center = mod.getPlayer().getPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                RaycastContext ctx = castDown(center.add(dx, 0.0D, dz));
                cctx.checkRay(mod, ctx);
            }
        }
        return Optional.ofNullable(cctx.bestBlock);
    }

    private RaycastContext castDown(Vec3d origin) {
        LivingEntity clientPlayerEntity = controller.getPlayer();
        assert clientPlayerEntity != null;
        return new RaycastContext(origin, origin.add(0.0D, -1.0D * config.castDownDistance, 0.0D), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity) clientPlayerEntity);
    }

    private RaycastContext castCone(double yaw, double pitch) {
        LivingEntity clientPlayerEntity = controller.getPlayer();
        assert clientPlayerEntity != null;
        Vec3d origin = clientPlayerEntity.getPos();
        double dy = config.epicClutchConeCastHeight;
        double dH = dy * Math.sin(Math.toRadians(pitch));
        double yawRad = Math.toRadians(yaw);
        double dx = dH * Math.cos(yawRad);
        double dz = dH * Math.sin(yawRad);
        Vec3d end = origin.add(dx, -1.0D * dy, dz);
        return new RaycastContext(origin, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, (Entity) clientPlayerEntity);
    }

    protected void onStop(Task interruptTask) {
        IBaritone baritone = controller.getBaritone();
        InputControls controls = controller.getInputControls();
        baritone.getPathingBehavior().forceCancel();
        this.movingTorwards = null;
        baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
        moveLeftRight(controller, 0);
        moveForwardBack(controller, 0);
        controls.release(Input.SPRINT);
        controls.release(Input.JUMP);
    }

    private boolean hasClutchItem(AltoClefController mod) {
        if (!mod.getWorld().getDimension().ultraWarm() && mod.getItemStorage().hasItem(new Item[]{Items.WATER_BUCKET}))
            return true;
        return config.clutchItems.stream().anyMatch(item -> mod.getItemStorage().hasItem(new Item[]{item}));
    }

    public boolean isFinished() {
        LivingEntity player = controller.getPlayer();
        return (player.isSwimming() || player.isTouchingWater() || player.isOnGround() || player.isClimbing());
    }

    protected boolean isEqual(Task other) {
        return other instanceof adris.altoclef.tasks.movement.MLGBucketTask;
    }

    protected String toDebugString() {
        String result = "Epic gaemer moment";
        if (this.movingTorwards != null)
            result = result + " (CLUTCH AT: " + result + ")";
        return result;
    }

    public BlockPos getWaterPlacedPos() {
        return this.placedPos;
    }

    private static class MLGClutchConfig {
        public double castDownDistance = 40;
        public double averageHorizontalMovementSpeedPerTick = 0.25; // How "far" the player moves horizontally per tick. Set too low and the bot will ignore viable clutches. Set too high and the bot will go for clutches it can't reach.
        public double epicClutchConeCastHeight = 40; // How high the "epic clutch" ray cone is
        public double epicClutchConePitchAngle = 25; // How wide (degrees) the "epic clutch" ray cone is
        public int epicClutchConePitchResolution = 8; // How many divisions in each direction the cone's pitch has
        public int epicClutchConeYawDivisionStart = 6; // How many divisions to start the cone clutch at in the center
        public int epicClutchConeYawDivisionEnd = 20; // How many divisions to move the cone clutch at torwars the end
        public int preferLavaWhenFallDropsHealthBelowThreshold = 3; // If a fall results in our player's health going below this value, consider it deadly.
        public int lavaLevelOrGreaterWillCancelFallDamage = 5; // Lava at this level will cancel our fall damage if we hold space.
        @JsonSerialize(using = ItemSerializer.class)
        @JsonDeserialize(using = ItemDeserializer.class)
        public List<Item> clutchItems = List.of(Items.HAY_BLOCK, Items.TWISTING_VINES);
    }

    class ConeClutchContext {
        private final boolean hasClutchItem;
        public BlockPos bestBlock = null;
        private double highestY = Double.NEGATIVE_INFINITY;
        private double closestXZ = Double.POSITIVE_INFINITY;
        private boolean bestBlockIsSafe = false;
        private boolean bestBlockIsDeadlyFall = false;
        private boolean bestBlockIsLava = false;

        public ConeClutchContext(AltoClefController mod) {
            hasClutchItem = hasClutchItem(mod);
        }

        public void checkBlock(AltoClefController mod, BlockPos check) {
            // Already checked
            if (Objects.equals(bestBlock, check))
                return;
            if (WorldHelper.isAir(mod.getWorld().getBlockState(check).getBlock())) {
                Debug.logMessage("(MLG Air block checked for landing, the block broke. We'll try another): " + check);
                return;
            }
            boolean lava = isLava(controller, check);
            boolean lavaWillProtect = lava && lavaWillProtect(controller, check);
            boolean water = isWater(controller, check);
            boolean isDeadlyFall = !hasClutchItem && isFallDeadly(controller, check);
            // Prioritize safe blocks ALWAYS
            if (bestBlockIsSafe && !water)
                return;
            double height = check.getY();
            double distSqXZ = WorldHelper.distanceXZSquared(WorldHelper.toVec3d(check), mod.getPlayer().getPos());
            boolean highestSoFar = height > highestY;
            boolean closestSoFar = distSqXZ < closestXZ;
            // We found a new contender
            if (
                    bestBlock == null || // No target was found.
                            (water && !bestBlockIsSafe) || // We ALWAYS land in water if we can
                            (lava && lavaWillProtect && bestBlockIsDeadlyFall && !hasClutchItem) || // Land in lava if our best alternative is death by fall damage
                            (!lava && !isDeadlyFall && ((closestSoFar && hasClutchItem) && highestSoFar || bestBlockIsLava)) // If it's not lava and is not deadly, land on it if it's higher than before OR if our best alternative is lava
            ) {
                if (canTravelToInAir(controller, (lava || water) ? check.down() : check)) {
                    if (highestSoFar) {
                        highestY = height;
                    }
                    if (closestSoFar) {
                        closestXZ = distSqXZ;
                    }
                    bestBlockIsSafe = water;
                    bestBlockIsDeadlyFall = isDeadlyFall;
                    bestBlockIsLava = lava;
                    bestBlock = check;
                }
            }
        }

        public void checkRay(AltoClefController mod, RaycastContext rctx) {
            BlockHitResult hit = mod.getWorld().raycast(rctx);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos check = hit.getBlockPos();
                // For now, REQUIRE we land on this
                if (hit.getSide().getOffsetY() <= 0)
                    return;
                checkBlock(mod, check);
            }
        }
    }
}

package baritone;

import baritone.command.defaults.DefaultCommands;
import baritone.entity.CustomFishingBobberEntity;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Automatone.MOD_ID)
@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD,modid=Automatone.MOD_ID)
public final class Automatone {
   public static final Logger LOGGER = LogManager.getLogger("Automatone");
   public static final String MOD_ID = "automatone";
   public static final TagKey<Item> EMPTY_BUCKETS = TagKey.create(Registries.ITEM, id("empty_buckets"));
   public static final TagKey<Item> WATER_BUCKETS = TagKey.create(Registries.ITEM, id("water_buckets"));
   private static final ThreadPoolExecutor threadPool;
   private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

   public static final RegistryObject<EntityType<CustomFishingBobberEntity>> FISHING_BOBBER =  ENTITY_TYPES.register("fishing_bobber", ()->EntityType.Builder.<CustomFishingBobberEntity>of(CustomFishingBobberEntity::new,MobCategory.MISC)
      .sized(EntityType.FISHING_BOBBER.getWidth(), EntityType.FISHING_BOBBER.getHeight())
      .setTrackingRange(64)
      .setUpdateInterval(1)
      .setShouldReceiveVelocityUpdates(true)
      .build("fishing_bobber"));

   public static ResourceLocation id(String path) {
      return new ResourceLocation("automatone", path);
   }

   public static ThreadPoolExecutor getExecutor() {
      return threadPool;
   }

   public Automatone() {
      FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

      ENTITY_TYPES.register(modEventBus);
   }

   private void setup(final FMLCommonSetupEvent event) {
      MinecraftForge.EVENT_BUS.register(this);
   }

   @SubscribeEvent
   public void registerCommand(RegisterCommandsEvent e) {
      DefaultCommands.registerAll();
      DefaultCommands.register(e.getDispatcher());
   }

   static {
      AtomicInteger threadCounter = new AtomicInteger(0);
      threadPool = new ThreadPoolExecutor(
         4, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), r -> new Thread(r, "Automatone Worker " + threadCounter.incrementAndGet())
      );
   }
}

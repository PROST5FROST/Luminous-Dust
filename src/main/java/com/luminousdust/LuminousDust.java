package com.luminousdust;

import com.luminousdust.client.particle.DustParticle;
import com.luminousdust.config.LumDustConf;
import com.luminousdust.config.LumDustConfCache;
import com.luminousdust.config.LumDustYACL;
import com.luminousdust.init.ParticleInit;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;

@Mod("luminousdust")
public class LuminousDust {
   public static final String MODID = "luminousdust";
   public static final Logger LOGGER = LogUtils.getLogger();

   public LuminousDust(IEventBus modBus, ModContainer modContainer) {
      ParticleInit.register(modBus);
      modContainer.registerConfig(Type.CLIENT, LumDustConf.SPEC);
      LOGGER.debug("[Luminous Dust] Yay, at least something is working!");

      if (net.neoforged.fml.ModList.get().isLoaded("yet_another_config_lib_v3")) {
         ModLoadingContext.get().registerExtensionPoint(
                 net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                 () -> (mc, parent) -> LumDustYACL.createConfigScreen(parent)
         );
      }
   }

   @EventBusSubscriber(modid = "luminousdust", value = Dist.CLIENT)
   public static class ClientModEvents {
      @SubscribeEvent
      public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
         event.registerSpriteSet(ParticleInit.DUST_PARTICLE.get(), DustParticle.Provider::new);
      }

      @SubscribeEvent
      public static void onConfigReload(ModConfigEvent.Reloading event) {
         LumDustConfCache.refresh();
      }
      @SubscribeEvent
      public static void onConfigLoad(ModConfigEvent.Loading event) {
         LumDustConfCache.refresh();
      }
   }
}

package com.lightdust;

import com.lightdust.client.particle.DustParticle;
import com.lightdust.config.LightDustConfig;
import com.lightdust.config.LightDustYACL;
import com.lightdust.init.ParticleInit;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;

@Mod("lightdust")
public class LightDust {
   public static final String MODID = "lightdust";
   public static final Logger LOGGER = LogUtils.getLogger();

   public LightDust(IEventBus modBus, ModContainer modContainer) {
      ParticleInit.register(modBus);
      modContainer.registerConfig(Type.CLIENT, LightDustConfig.SPEC);
      LOGGER.debug("[Light Dust Neoforge] Yay, at least something working!");

      if (net.neoforged.fml.ModList.get().isLoaded("yet_another_config_lib_v3")) {
         ModLoadingContext.get().registerExtensionPoint(
                 net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                 () -> (mc, parent) -> LightDustYACL.createConfigScreen(parent)
         );
      }
   }

   @EventBusSubscriber(modid = "lightdust", value = Dist.CLIENT)
   public static class ClientModEvents {
      @SubscribeEvent
      public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
         event.registerSpriteSet(ParticleInit.DUST_PARTICLE.get(), DustParticle.Provider::new);
      }
   }
}

package com.lightdust.event;

import com.lightdust.client.particle.DustParticle;
import com.lightdust.config.LightDustConfig;
import com.lightdust.logic.DustSpawner;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Pre;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;

@EventBusSubscriber(modid = "lightdust", value = Dist.CLIENT)
public class AmbientDustHandler {
   @SubscribeEvent
   public static void onClientTick(Pre event) {
      if (LightDustConfig.SPEC == null || !LightDustConfig.SPEC.isLoaded()) {
         return;
      }

      Minecraft mc = Minecraft.getInstance();

      if (mc.isPaused() || mc.player == null || mc.level == null) {
         return;
      }
      DustSpawner.tick(mc.level, mc.player, mc);
   }

   @SubscribeEvent
   public static void onClientLogout(LoggingOut event) {
      clearMaps();
   }

   @SubscribeEvent
   public static void onClientLogin(LoggingIn event) {
      clearMaps();
   }

   @SubscribeEvent
   public static void onDimensionChange(PlayerChangedDimensionEvent event) {
      if (event.getEntity().level().isClientSide) {
         clearMaps();
      }
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      if (event.getEntity().level().isClientSide) {
         clearMaps();
      }
   }

   @SubscribeEvent
   public static void onWorldUnload(Unload event) {
      if (event.getLevel().isClientSide()) {
         clearMaps();
      }
   }

   private static void clearMaps() {
      DustParticle.AMBIENT_COUNTS.clear();
      DustParticle.PENDING_POS = null;
   }
}

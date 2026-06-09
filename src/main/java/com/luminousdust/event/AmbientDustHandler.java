package com.luminousdust.event;

import com.luminousdust.client.particle.helpers.DustLifeManager;
import com.luminousdust.config.LumDustConf;
import com.luminousdust.logic.DustSpawner;
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

import static com.luminousdust.client.particle.helpers.DustLifeManager.AMBIENT_COUNTS;

@EventBusSubscriber(modid = "luminousdust", value = Dist.CLIENT)
public class AmbientDustHandler {
   @SubscribeEvent
   public static void onClientTick(Pre event) {
      if (LumDustConf.SPEC == null || !LumDustConf.SPEC.isLoaded()) {
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
      AMBIENT_COUNTS.clear();
      DustLifeManager.PENDING_POS = null;
   }
}

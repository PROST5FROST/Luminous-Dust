package com.luminousdust.client.particle;

import com.luminousdust.client.particle.helpers.DustParticleColor;
import com.luminousdust.client.particle.helpers.DustPhysicsHelper;
import com.luminousdust.config.LumDustConf;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DustParticle extends TextureSheetParticle {

   public static final Long2IntOpenHashMap AMBIENT_COUNTS = new Long2IntOpenHashMap();
   public static int TOTAL_AMBIENT_COUNT = 0;
   public static BlockPos PENDING_POS = null;


   private final BlockPos ownerPos;
   private final float rotSpeed;
   private final int tickOffset;
   private float baseAlpha;
   private final float variation;

   protected DustParticle(ClientLevel level, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprites) {
      super(level, x, y, z);
      this.ownerPos = PENDING_POS;
      this.tickOffset = level.random.nextInt(20);
      float ambientOpacity = LumDustConf.AMBIENT_DUST_OPACITY.get().floatValue();

      this.variation = level.random.nextFloat() * 0.05F;
      if (this.ownerPos != null) {
         AMBIENT_COUNTS.addTo(this.ownerPos.asLong(), 1);
         TOTAL_AMBIENT_COUNT++;
         int light = level.getBrightness(LightLayer.BLOCK, this.ownerPos);
         float intensity = Math.max(0f, (light - 6) / 9.0f);
         float baseBrightness = 0.15F + (0.85F * intensity);

         // grab the tint
         float[] tint = DustParticleColor.getNearbyTint(level, this.ownerPos);
         float strength = LumDustConf.TINT_STRENGTH.get().floatValue();

         if (tint != null && strength > 0) {
            this.rCol = (baseBrightness * (1 - strength) + tint[0] * strength) + this.variation;
            this.gCol = (baseBrightness * (1 - strength) + tint[1] * strength) + this.variation;
            this.bCol = (baseBrightness * (1 - strength) + tint[2] * strength) + this.variation;
         } else {
            this.rCol = baseBrightness + this.variation;
            this.gCol = baseBrightness + this.variation;
            this.bCol = baseBrightness + this.variation;
         }

         this.baseAlpha = ambientOpacity + (0.28F * intensity);
         this.alpha = 0.0F;
         this.lifetime = LumDustConf.PARTICLE_LIFETIME.get() + level.random.nextInt(100);
      } else {
         this.lifetime = LumDustConf.PARTICLE_LIFETIME.get() / 2;
         this.rCol = 0.8F;
         this.gCol = 0.8F;
         this.bCol = 0.8F;
         this.baseAlpha = ambientOpacity;
         this.alpha = baseAlpha;
      }

      this.quadSize = (LumDustConf.PARTICLE_SIZE.get().floatValue() / 10);
      this.gravity = 0.000F;

      if (dx != 0 || dy != 0 || dz != 0) {
         this.xd = dx;
         this.yd = dy;
         this.zd = dz;
      } else {
         this.xd = (level.random.nextFloat() - 0.5F) * 0.005F;
         this.yd = (level.random.nextFloat() - 0.5F) * 0.005F;
         this.zd = (level.random.nextFloat() - 0.5F) * 0.005F;
      }

      this.hasPhysics = false;
      this.roll = level.random.nextFloat() * Mth.TWO_PI;
      this.oRoll = this.roll;
      this.rotSpeed = (level.random.nextFloat() - 0.5F) * 0.1F;

      this.pickSprite(sprites);
      this.setSize(0.01F, 0.01F);
   }

   @Override
   public void remove() {
      if (!this.removed && ownerPos != null) {
         long key = ownerPos.asLong();
         int oldVal = AMBIENT_COUNTS.addTo(key, -1);
         if (oldVal <= 1) AMBIENT_COUNTS.remove(key);
         if (TOTAL_AMBIENT_COUNT > 0) TOTAL_AMBIENT_COUNT--;
      }
      super.remove();
   }

   @Override
   public @NotNull ParticleRenderType getRenderType() {
      return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   @Override
   public int getLightColor(float partialTick) {
      if ((this.age + tickOffset) % 10 == 0) {
         BlockPos blockpos = BlockPos.containing(this.x, this.y, this.z);
         return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightColor(this.level, blockpos) : 0;
      }
      return super.getLightColor(partialTick);
   }

   @Override
   public void tick() {
      super.tick();
      // Fade Logic
      if (this.age < 5) {
         this.alpha = this.baseAlpha * (this.age / 5.0F);
      } else if (this.age > this.lifetime - 20) {
         this.alpha = this.baseAlpha * ((this.lifetime - this.age) / 20.0F);
      } else {
         this.alpha = this.baseAlpha;
      }

      if ((this.age + tickOffset) % 20 == 0) {
         BlockPos currentPos = BlockPos.containing(this.x, this.y, this.z);
         if (level.getFluidState(currentPos).is(FluidTags.WATER)) {
            this.remove(); return;
         }

         Player player = Minecraft.getInstance().player;
         int blockLight = level.getBrightness(LightLayer.BLOCK, currentPos);
         // I also need to add  && DustUtils.isHoldingLight(player) here. But with sync from other side
         if (blockLight < 4) {
            this.remove();
            return;
         }

         if (this.ownerPos != null) {
            float intensity = Math.max(0f, (blockLight - 6) / 9.0f);
            float baseBrightness = 0.15F + (0.85F * intensity);

            float[] tint = DustParticleColor.getNearbyTint(level, this.ownerPos);
            float strength = LumDustConf.TINT_STRENGTH.get().floatValue();

            if (tint != null && strength > 0) {
               this.rCol = (baseBrightness * (1 - strength) + tint[0] * strength) + this.variation;
               this.gCol = (baseBrightness * (1 - strength) + tint[1] * strength) + this.variation;
               this.bCol = (baseBrightness * (1 - strength) + tint[2] * strength) + this.variation;
            } else {
               this.rCol = baseBrightness + this.variation;
               this.gCol = baseBrightness + this.variation;
               this.bCol = baseBrightness + this.variation;
            }

            float ambientOpacity = LumDustConf.AMBIENT_DUST_OPACITY.get().floatValue();
            this.baseAlpha = ambientOpacity + (0.28F * intensity);
         }

         long time = level.getDayTime() % 24000;
         boolean isDay = time < 13000 || time > 23000;
         if (isDay) {
            int skyLight = level.getBrightness(LightLayer.SKY, currentPos);
            int diffThreshold = LumDustConf.DAYTIME_LIGHT_DIFF.get();

            if ((blockLight - skyLight) <= diffThreshold) {
               this.remove();
               return;
            }
         }

         if (player != null) {
            double maxDist = LumDustConf.AMBIENT_HARD_CAP.get();
            if (player.distanceToSqr(this.x, this.y, this.z) > maxDist * maxDist) {
               this.remove();
               return;
            }
         }
      }

      this.oRoll = this.roll;
      this.roll += this.rotSpeed;

      // Physics

      DustPhysicsHelper.applyAmbientDrift(this, this.level);
      DustPhysicsHelper.applyThermalUpdraft(this, this.level);

      Player player = Minecraft.getInstance().player;
      if (player != null) {
         DustPhysicsHelper.applyPlayerInteraction(this, this.level, player);

         if (level.isClientSide) {
            DustPhysicsHelper.applyBlockBreakInteraction(this, this.level, player);
         }
      }

      this.xd *= 0.94;
      this.yd *= 0.94;
      this.zd *= 0.94;

      this.move(this.xd, this.yd, this.zd);
   }

   @OnlyIn(Dist.CLIENT)
   public static class Provider implements ParticleProvider<SimpleParticleType> {
      private final SpriteSet sprites;
      public Provider(SpriteSet sprites) { this.sprites = sprites; }

      @Override
      public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
         return new DustParticle(level, x, y, z, dx, dy, dz, sprites);
      }
   }

   public double getX() { return this.x; }
   public double getY() { return this.y; }
   public double getZ() { return this.z; }

   public int getAge() { return this.age; }

   public double getXd() { return this.xd; }
   public double getZd() { return this.zd; }
   public double getYd() { return this.yd; }

   public void setXd(double xd) { this.xd = xd; }
   public void setYd(double yd) { this.yd = yd; }
   public void setZd(double zd) { this.zd = zd; }

}
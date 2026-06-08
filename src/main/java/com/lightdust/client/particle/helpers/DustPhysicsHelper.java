package com.lightdust.client.particle.helpers;

import com.lightdust.client.particle.DustParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DustPhysicsHelper {
    public static void applyAmbientDrift(DustParticle particle, ClientLevel level) {
        float seed = (float) (particle.getX() + particle.getY() + particle.getZ());
        float time = (float) ((particle.getAge() + seed) * 0.15);

        double sinX = Mth.sin(time * 0.8f + seed);
        double cosZ = Mth.cos(time * 1.1f + seed);

        double driftDown = 0.00002 + (level.random.nextDouble() * 0.00005);
        double microTurbulence = (level.random.nextDouble() - 0.5) * 0.00012;

        particle.setXd(particle.getXd() + sinX * 0.0001 + microTurbulence);
        particle.setZd(particle.getZd() + cosZ * 0.0001 + (microTurbulence * 0.5));

        particle.setYd(particle.getYd() - (driftDown + (sinX * 0.00005)));
    }

    public static void applyThermalUpdraft(DustParticle particle, ClientLevel level) {
        BlockPos basePos = BlockPos.containing(particle.getX(), particle.getY(), particle.getZ());

        for (int i = 1; i <= 5; i++) {
            BlockPos checkPos = basePos.below(i);
            BlockState state = level.getBlockState(checkPos);

            double updraftForce = 0.0;

            if (state.is(Blocks.LAVA) || level.getFluidState(checkPos).is(FluidTags.LAVA)) {
                updraftForce = 0.0025 / (i + 1);
            } else if (state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                updraftForce = 0.001 / (i + 1);
            } else if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE) || state.is(Blocks.MAGMA_BLOCK)) {
                updraftForce = 0.001 / (i + 1);
            } else if (state.is(Blocks.TORCH) || state.is(Blocks.SOUL_TORCH)) {
                updraftForce = 0.0008 / (i + 1);
            }

            if (updraftForce > 0.0) {
                particle.setYd(particle.getYd() + updraftForce);
                particle.setXd(particle.getXd() + (level.random.nextDouble() - 0.5) * 0.0005);
                particle.setZd(particle.getZd() + (level.random.nextDouble() - 0.5) * 0.0005);
                break; // Found a heat source and pushing the particle up
            }
        }
    }

    public static void applyPlayerInteraction(DustParticle particle, ClientLevel level, Player player) {
        if (player.distanceToSqr(particle.getX(), particle.getY(), particle.getZ()) >= 4.0) return;

        // A jitter of particle in the air
        double range = 2.0;
        double dx = particle.getX() - player.getX();
        double dy = particle.getY() - (player.getY() + 1.0);
        double dz = particle.getZ() - player.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 0.01) dist = 0.01;

        double nx = dx / dist;
        double ny = dy / dist;
        double nz = dz / dist;

        double jitterX = (level.random.nextDouble() - 0.5) * 0.02;
        double jitterY = (level.random.nextDouble() - 0.5) * 0.02;
        double jitterZ = (level.random.nextDouble() - 0.5) * 0.02;

        // A slash with a sword (Or something else)
        if (player.swingTime > 0) {
            Vec3 look = player.getLookAngle();
            if ((nx * look.x) + (ny * look.y) + (nz * look.z) > 0.5) {
                double slashForce = 0.002;
                particle.setXd(particle.getXd() + look.x * slashForce + (nx * 0.005) + jitterX);
                particle.setYd(particle.getYd() + look.y * slashForce + (ny * 0.005) + jitterY);
                particle.setZd(particle.getZd() + look.z * slashForce + (nz * 0.005) + jitterZ);
            }
        }

        // Well, a shield blocking.
        if (player.isUsingItem() && player.getUseItem().getItem() instanceof ShieldItem) {
            Vec3 look = player.getLookAngle();
            if ((nx * look.x) + (ny * look.y) + (nz * look.z) > 0.3) {
                double shieldPush = 0.04 / dist;
                particle.setXd(particle.getXd() + (nx * shieldPush) + jitterX);
                particle.setYd(particle.getYd() + (ny * shieldPush) + jitterY);
                particle.setZd(particle.getZd() + (nz * shieldPush) + jitterZ);
            }
        }

        // Walking through a particle
        Vec3 pVel = player.getDeltaMovement();
        double horizontalSpeed = Math.sqrt(pVel.x * pVel.x + pVel.z * pVel.z);
        if (horizontalSpeed > 0.01) {
            double proximityFactor = (range - dist) / range;
            double pushStrength = horizontalSpeed * proximityFactor * 0.05;
            particle.setXd(particle.getXd() + (nx * pushStrength) + jitterX);
            particle.setYd(particle.getYd() + (ny * pushStrength) + jitterY);
            particle.setZd(particle.getZd() + (nz * pushStrength) + jitterZ);
        }
    }

    public static void applyBlockBreakInteraction (DustParticle particle, ClientLevel level, Player player) {
        if (level.isClientSide) {
            HitResult hit = Minecraft.getInstance().hitResult;
            if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                BlockPos breakPos = ((BlockHitResult)hit).getBlockPos();
                if (player != null && player.swingTime > 0 && breakPos.distToCenterSqr(particle.getX(), particle.getY(), particle.getZ()) < 4.0) {
                    double dX = particle.getX() - (breakPos.getX() + 0.5);
                    double dY = particle.getY() - (breakPos.getY() + 0.5);
                    double dZ = particle.getZ() - (breakPos.getZ() + 0.5);
                    double distSqrBreak = dX * dX + dY * dY + dZ * dZ;
                    if (level.getBlockState(breakPos).isAir() && distSqrBreak < 3) {

                        double distBreak = Math.sqrt(distSqrBreak);
                        if (distBreak < 0.1) distBreak = 0.1;
                        double force = 0.01;

                        double jitterX = (level.random.nextDouble() - 0.5) * 0.2;
                        double jitterY = (level.random.nextDouble() - 0.5) * 0.2;
                        double jitterZ = (level.random.nextDouble() - 0.5) * 0.2;

                        particle.setXd(particle.getXd() + (dX / distBreak) * force + jitterX);
                        particle.setYd(particle.getYd() + (dY / distBreak) * force + jitterY);
                        particle.setZd(particle.getZd() + (dZ / distBreak) * force + jitterZ);
                    }
                }
            }
        }
    }
}

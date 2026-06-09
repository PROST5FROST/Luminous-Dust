package com.luminousdust.client.particle.helpers;

import com.luminousdust.config.LumDustConf;
import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

public class DustParticleColor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final java.util.Map<net.minecraft.world.level.block.Block, float[]> LIGHT_COLORS = new java.util.HashMap<>();
    public static boolean colorsLoaded = false;

    public static void reloadColors() {
        LIGHT_COLORS.clear();
        for (String entry : LumDustConf.CUSTOM_TINTS.get()) {
            try {
                String[] parts = entry.split("=");
                if (parts.length == 2 && parts[1].contains("#")) {
                    net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.parse(parts[0].trim());
                    net.minecraft.world.level.block.Block block = BuiltInRegistries.BLOCK.get(rl);

                    if (block != net.minecraft.world.level.block.Blocks.AIR) {
                        String hex = parts[1].substring(parts[1].indexOf("#") + 1).trim();
                        if (hex.length() == 6) {
                            int r = Integer.parseInt(hex.substring(0, 2), 16);
                            int g = Integer.parseInt(hex.substring(2, 4), 16);
                            int b = Integer.parseInt(hex.substring(4, 6), 16);
                            LIGHT_COLORS.put(block, new float[]{r / 255f, g / 255f, b / 255f});
                        } else {
                            LOGGER.error("[Luminous Dust] Invalid hex code length in config for entry '{}'. Must be 6 characters after '#'.", entry);
                        }
                    } else {
                        LOGGER.warn("[Luminous Dust] Block not found in registry for config entry '{}'. It may be from an uninstalled mod.", entry);
                    }
                } else {
                    LOGGER.error("[Luminous Dust] Malformed custom tint entry: '{}'. Format must be 'modid:block_name=#RRGGBB'.", entry);
                }
            } catch (NumberFormatException e) {
                LOGGER.error("[Luminous Dust] Invalid hex characters in config for entry '{}'.", entry);
            } catch (Exception e) {
                LOGGER.error("[Luminous Dust] Failed to parse custom tint config entry '{}': {}", entry, e.getMessage());
            }
        }
        colorsLoaded = true;
    }

    public static float[] getNearbyTint(ClientLevel level, BlockPos pos) {
        if (!colorsLoaded) {
            reloadColors();
        }

        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            net.minecraft.world.level.block.state.BlockState state = level.getBlockState(p);

            if (state.getLightEmission(level, p) > 0) {
                float[] color = LIGHT_COLORS.get(state.getBlock());
                if (color != null) {
                    return color;
                }
            }
        }
        return null;
    }
}

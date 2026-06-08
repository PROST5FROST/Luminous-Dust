package com.lightdust.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightDustConfigCache {
    public static int AMBIENT_HARD_CAP;
    public static int DAYTIME_LIGHT_DIFF;
    public static float AMBIENT_DUST_OPACITY;
    public static float PARTICLE_SIZE;
    public static int PARTICLE_LIFETIME;
    public static float TINT_STRENGTH;

    public static final Map<ResourceLocation, float[]> TINT_MAP = new HashMap<>();

    public static void refresh() {
        AMBIENT_HARD_CAP = LightDustConfig.AMBIENT_HARD_CAP.get();
        DAYTIME_LIGHT_DIFF = LightDustConfig.DAYTIME_LIGHT_DIFF.get();
        AMBIENT_DUST_OPACITY = LightDustConfig.AMBIENT_DUST_OPACITY.get().floatValue();
        PARTICLE_SIZE = LightDustConfig.PARTICLE_SIZE.get().floatValue();
        PARTICLE_LIFETIME = LightDustConfig.PARTICLE_LIFETIME.get();
        TINT_STRENGTH = LightDustConfig.TINT_STRENGTH.get().floatValue();

        TINT_MAP.clear();
        List<? extends String> rawTints = LightDustConfig.CUSTOM_TINTS.get();
        for (String entry : rawTints) {
            try {
                String[] parts = entry.split("=");
                ResourceLocation blockKey = ResourceLocation.parse(parts[0].trim());
                String hex = parts[1].trim().replace("#", "");

                int r = Integer.valueOf(hex.substring(0, 2), 16);
                int g = Integer.valueOf(hex.substring(2, 4), 16);
                int b = Integer.valueOf(hex.substring(4, 6), 16);

                TINT_MAP.put(blockKey, new float[]{r / 255f, g / 255f, b / 255f});
            } catch (Exception ignored) {}
        }
    }
}

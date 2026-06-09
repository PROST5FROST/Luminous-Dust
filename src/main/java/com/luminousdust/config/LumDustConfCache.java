package com.luminousdust.config;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LumDustConfCache {
    public static int AMBIENT_HARD_CAP;
    public static int PARTICLE_LIFETIME;
    public static int AMBIENT_RADIUS;
    public static int MIN_BLOCK_LIGHT;
    public static int DAYTIME_LIGHT_DIFF;


    public static float TINT_STRENGTH;
    public static float AMBIENT_DUST_OPACITY;
    public static float PARTICLE_SIZE;

    public static final Map<ResourceLocation, float[]> TINT_MAP = new HashMap<>();

    public static void refresh() {
        // Well. Everything
        AMBIENT_HARD_CAP = LumDustConf.AMBIENT_HARD_CAP.get();
        AMBIENT_RADIUS = LumDustConf.AMBIENT_RADIUS.get();

        AMBIENT_DUST_OPACITY = LumDustConf.AMBIENT_DUST_OPACITY.get().floatValue();
        // Light conf
        DAYTIME_LIGHT_DIFF = LumDustConf.DAYTIME_LIGHT_DIFF.get();
        MIN_BLOCK_LIGHT = LumDustConf.MIN_BLOCK_LIGHT.get();
        // Particle conf
        PARTICLE_SIZE = LumDustConf.PARTICLE_SIZE.get().floatValue();
        PARTICLE_LIFETIME = LumDustConf.PARTICLE_LIFETIME.get();

        TINT_STRENGTH = LumDustConf.TINT_STRENGTH.get().floatValue();

        TINT_MAP.clear();
        List<? extends String> rawTints = LumDustConf.CUSTOM_TINTS.get();
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

package com.lightdust.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class LightDustYACL {
    public static Screen createConfigScreen(Screen parentScreen) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Light Dust Settings"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Spawning"))
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Ambient Radius"))
                                .description(OptionDescription.of(Component.literal("ЕБУЧИЙ СЛУЧАЙ ШЛЮХИ ЕБАНЫЕ")))
                                .binding(10,
                                        LightDustConfig.AMBIENT_RADIUS,
                                        val -> { LightDustConfig.AMBIENT_RADIUS.set(val); LightDustConfigCache.refresh(); })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 32).step(1))
                                .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Hard Cap Radius"))
                                .description(OptionDescription.of(Component.literal("ЕБУЧИЙ СЛУЧАЙ ШЛЮХИ ЕБАНЫЕ")))
                                .binding(12,
                                        LightDustConfig.AMBIENT_HARD_CAP,
                                        val -> { LightDustConfig.AMBIENT_HARD_CAP.set(val); LightDustConfigCache.refresh(); })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 48).step(1))
                                .build())

                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Visuals"))

                        .option(Option.<Float>createBuilder()
                                .name(Component.literal("Particle Size"))
                                .description(OptionDescription.of(Component.literal("ЕБУЧИЙ СЛУЧАЙ ШЛЮХИ ЕБАНЫЕ")))
                                .binding(0.022f,
                                        () -> LightDustConfig.PARTICLE_SIZE.get().floatValue(),
                                        val -> { LightDustConfig.PARTICLE_SIZE.set((double) val); LightDustConfigCache.refresh(); })
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.1f, 1.0f).step(0.01f))
                                .build())

                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Particle Lifetime"))
                                .description(OptionDescription.of(Component.literal("ЕБУЧИЙ СЛУЧАЙ ШЛЮХИ ЕБАНЫЕ")))
                                .binding(200,
                                        LightDustConfig.PARTICLE_LIFETIME,
                                        val -> { LightDustConfig.PARTICLE_LIFETIME.set(val); LightDustConfigCache.refresh(); })
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(20, 1000).step(10))
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Component.literal("Tint Strength"))
                                .description(OptionDescription.of(Component.literal("ЕБУЧИЙ СЛУЧАЙ ШЛЮХИ ЕБАНЫЕ")))
                                .binding(0.22f,
                                        () -> LightDustConfig.TINT_STRENGTH.get().floatValue(),
                                        val -> { LightDustConfig.TINT_STRENGTH.set((double) val); LightDustConfigCache.refresh(); })
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.05f))
                                .build())

                        .build())

                .build()
                .generateScreen(parentScreen);
        }
}

package com.lightdust.logic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class DustUtils {
    public static boolean isHoldingLight(Player player) {
        return isLightItem(player.getMainHandItem().getItem()) || isLightItem(player.getOffhandItem().getItem());
    }

    private static boolean isLightItem(Item item) {
        return item == Items.TORCH || item == Items.SOUL_TORCH || item == Items.LANTERN || item == Items.GLOWSTONE || item == Items.SOUL_LANTERN;
    }
}

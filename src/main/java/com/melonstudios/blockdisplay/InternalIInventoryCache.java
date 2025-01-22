package com.melonstudios.blockdisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

class InternalIInventoryCache {
    public static BlockPos pos = BlockPos.ORIGIN;
    public static List<ItemStack> stacks = new ArrayList<>();
}

package com.melonstudios.blockdisplay;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

class InternalIInventoryCache {
    public static int requestCooldown = 20;
    @Nonnull
    public static BlockPos pos = BlockPos.ORIGIN;
    @Nonnull
    public static BlockPos posRequest = BlockPos.ORIGIN;
    @Nonnull
    public static List<ItemStack> stacks = new ArrayList<>();

    public static boolean lastFrameNoRender = false;

    public static void requestInventoryUpdate(World world, BlockPos pos) {
        if ((pos.toLong() != posRequest.toLong()) || requestCooldown <= 0 || lastFrameNoRender) {
            requestCooldown = 20;
            posRequest = pos;
            BlockDisplay.network.sendToServer(new PacketRequestInventory(pos, world.provider.getDimension()));
        }
    }

    public static void clear() {
        pos = BlockPos.ORIGIN;
        posRequest = BlockPos.ORIGIN;
        stacks.clear();
        requestCooldown = 20;
    }

    static int serverTestCooldown = 20;
    public static void testServer() {
        if (!allowTesting) return;
        if (serverTestCooldown > 0) serverTestCooldown--;
        else if (serverTestCooldown == 0) {
            BlockDisplay.logger.info("Testing server for Block Display mod");
            BlockDisplay.network.sendToServer(new PacketTestServer());
            serverTestCooldown = -1;
        }
    }

    private static boolean isLoadedOnServer = false;
    public static boolean isIsLoadedOnServer() {
        return isLoadedOnServer;
    }
    static void confirmServerPresence() {
        isLoadedOnServer = true;
    }
     static void revokeServerPresence() {

    }

    public static boolean allowTesting = false;
}

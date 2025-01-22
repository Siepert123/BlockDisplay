package com.melonstudios.blockdisplay;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketInventory implements IMessage {
    private boolean pleaseClear;
    private BlockPos pos;
    private int stackCount;
    private List<ItemStack> inventory;

    @Override
    public void fromBytes(ByteBuf buf) {
        pleaseClear = buf.readBoolean();
        pos = BlockPos.fromLong(buf.readLong());
        if (!pleaseClear) {
            stackCount = buf.readByte();
            inventory = new ArrayList<>();
            for (int i = 0; i < stackCount; i++) {
                inventory.add(ByteBufUtils.readItemStack(buf));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(pleaseClear);
        buf.writeLong(pos.toLong());
        if (!pleaseClear) {
            buf.writeByte(stackCount);
            for (ItemStack stack : inventory) {
                ByteBufUtils.writeItemStack(buf, stack);
            }
        }
    }

    public PacketInventory() {

    }

    public PacketInventory(BlockPos pos, List<ItemStack> inventory) {
        pleaseClear = inventory.isEmpty();
        this.pos = pos;
        this.stackCount = inventory.size();
        this.inventory = inventory;
    }

    public static class Handler implements IMessageHandler<PacketInventory, IMessage> {
        @Override
        public IMessage onMessage(PacketInventory message, MessageContext ctx) {
            if (message.pleaseClear) {
                InternalIInventoryCache.pos = message.pos;
                InternalIInventoryCache.stacks.clear();
            } else {
                List<ItemStack> rawStacks = message.inventory;
                List<ItemStack> stacks = new ArrayList<>();
                for (ItemStack raw : rawStacks) {
                    boolean flag = stacks.size() >= 12;
                    for (ItemStack prev : stacks) {
                        if (prev.isItemEqual(raw)) {
                            flag = true;
                            prev.grow(raw.getCount());
                            break;
                        }
                    }
                    if (!flag) stacks.add(raw);
                }
                InternalIInventoryCache.pos = message.pos;
                InternalIInventoryCache.stacks = stacks;
            }
            return null;
        }
    }
}

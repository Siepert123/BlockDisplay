package com.melonstudios.blockdisplay;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketRequestInventory implements IMessage {
    private BlockPos pos;
    private int dimension;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong());
        buf.writeInt(dimension);
    }

    public PacketRequestInventory() {

    }

    public PacketRequestInventory(BlockPos pos, int dimension) {
        this.pos = pos;
        this.dimension = dimension;
    }

    public static class Handler implements IMessageHandler<PacketRequestInventory, PacketInventory> {
        @Override
        public PacketInventory onMessage(PacketRequestInventory message, MessageContext ctx) {
            World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension);
            TileEntity te = world.getTileEntity(message.pos);
            if (te instanceof IInventory) {
                IInventory inventory = (IInventory) te;
                int size = inventory.getSizeInventory();
                List<ItemStack> stacks = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (stack.isEmpty()) continue;
                    stacks.add(stack);
                }
                return new PacketInventory(message.pos, stacks);
            }
            return null;
        }
    }
}

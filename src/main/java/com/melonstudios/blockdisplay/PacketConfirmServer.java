package com.melonstudios.blockdisplay;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketConfirmServer implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler implements IMessageHandler<PacketConfirmServer, IMessage> {

        @Override
        public IMessage onMessage(PacketConfirmServer message, MessageContext ctx) {
            InternalIInventoryCache.confirmServerPresence();
            BlockDisplay.logger.info("Confirmed Block Display presence at server!");
            return null;
        }
    }
}

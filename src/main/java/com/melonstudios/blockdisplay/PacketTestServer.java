package com.melonstudios.blockdisplay;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTestServer implements IMessage {
    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

    public static class Handler implements IMessageHandler<PacketTestServer, PacketConfirmServer> {
        @Override
        public PacketConfirmServer onMessage(PacketTestServer message, MessageContext ctx) {
            BlockDisplay.logger.info("Received packet to test if Block Display is available");
            return new PacketConfirmServer();
        }
    }
}

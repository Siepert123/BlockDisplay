package com.melonstudios.blockdisplay;

import com.melonstudios.blockdisplay.proxy.CommonProxy;
import com.melonstudios.melonlib.api.blockdisplay.APIBlockDisplay;
import com.melonstudios.melonlib.misc.MetaBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;

@Mod(modid = BlockDisplay.MODID, name = BlockDisplay.NAME, version = BlockDisplay.VERSION, dependencies = "required-after:melonlib@[1.2,)")
public class BlockDisplay
{
    public static final String MODID = "block_display";
    public static final String NAME = "Block Display";
    public static final String VERSION = "1.1";

    static Logger logger;

    static boolean active = true;

    static SimpleNetworkWrapper network;

    @SidedProxy(
            serverSide = "com.melonstudios.blockdisplay.proxy.CommonProxy",
            clientSide = "com.melonstudios.blockdisplay.proxy.ClientProxy"
    )
    static CommonProxy proxy;

    static boolean isClient() {
        return proxy.isClient();
    }
    static boolean isServer() {
        return proxy.isServer();
    }

    static String getToolClass(IBlockState state) {
        return state.getBlock().getHarvestTool(state);
    }
    static Set<String> getToolClasses(ItemStack stack) {
        return stack.getItem().getToolClasses(stack);
    }
    static int getToolLevel(IBlockState state) {
        return state.getBlock().getHarvestLevel(state);
    }
    static int getToolLevel(ItemStack stack, String tool, @Nullable EntityPlayer player, @Nullable IBlockState state) {
        return stack.getItem().getHarvestLevel(stack, tool, player, state);
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        logger.info("Initializing packets");
        network = NetworkRegistry.INSTANCE.newSimpleChannel("block_display");

        network.registerMessage(new PacketRequestInventory.Handler(),
                PacketRequestInventory.class, 0, Side.SERVER);
        network.registerMessage(new PacketInventory.Handler(),
                PacketInventory.class, 1, Side.CLIENT);

        network.registerMessage(new PacketTestServer.Handler(),
                PacketTestServer.class, 2, Side.SERVER);
        network.registerMessage(new PacketConfirmServer.Handler(),
                PacketConfirmServer.class, 3, Side.CLIENT);

        logger.info("Registering default TE text handlers");
        APIBlockDisplay.addCustomBlockInfoHandler(Blocks.STANDING_SIGN, InfoHandlerSign.instance);
        APIBlockDisplay.addCustomBlockInfoHandler(Blocks.WALL_SIGN, InfoHandlerSign.instance);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }
}

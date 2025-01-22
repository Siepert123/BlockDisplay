package com.melonstudios.blockdisplay;

import com.melonstudios.melonlib.api.blockdisplay.APIBlockDisplay;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

@Mod(modid = BlockDisplay.MODID, name = BlockDisplay.NAME, version = BlockDisplay.VERSION)
public class BlockDisplay
{
    public static final String MODID = "block_display";
    public static final String NAME = "Block Display";
    public static final String VERSION = "1.0";

    static Logger logger;

    static boolean active = true;

    static SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        logger.info("Initializing packets");
        network = NetworkRegistry.INSTANCE.newSimpleChannel("block_display");
        network.registerMessage(new PacketRequestInventory.Handler(),
                PacketRequestInventory.class, 0, Side.SERVER);
        network.registerMessage(new PacketInventory.Handler(),
                PacketInventory.class, 1, Side.CLIENT);

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

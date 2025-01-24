package com.melonstudios.blockdisplay;

import com.melonstudios.melonlib.api.blockdisplay.APIBlockDisplay;
import com.melonstudios.melonlib.api.blockdisplay.IAdditionalBlockInfo;
import com.melonstudios.melonlib.blockdict.BlockDictionary;
import com.melonstudios.melonlib.misc.MetaBlock;
import com.melonstudios.melonlib.render.RenderMelon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "block_display")
public class BlockDisplayEventBus {
    private static EnumInfoDisplayLocation getDisplayLocation() {
        return EnumInfoDisplayLocation.LEFT;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderBlockDisplay(RenderGameOverlayEvent.Post event) {
        if (Minecraft.getMinecraft().currentScreen != null) {
            InternalIInventoryCache.lastFrameNoRender = true;
            return;
        }
        if (!BlockDisplay.active || RenderMelon.isF3ScreenEnabled() || Minecraft.getMinecraft().currentScreen != null) return;
        GlStateManager.pushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != null && mc.player != null) {
            RayTraceResult result = mc.objectMouseOver;
            if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
                if (result.typeOfHit == RayTraceResult.Type.BLOCK) {
                    renderBlockInfo(mc, mc.world, result.getBlockPos(), result);
                } else if (result.typeOfHit == RayTraceResult.Type.ENTITY) {
                    renderEntityInfo(mc, mc.world, result.entityHit, result);
                }
            }
        }
        GlStateManager.popMatrix();
    }

    @SideOnly(Side.CLIENT)
    private static void renderBlockInfo(Minecraft minecraft, World world, BlockPos pos, RayTraceResult result) {
        IBlockState state = world.getBlockState(pos);
        ItemStack display = state.getBlock().getPickBlock(state, result, world, pos, minecraft.player);
        if (!display.isEmpty()) {
            boolean advanced = BlockDisplayConfig.enforceAdvancedInfo || minecraft.gameSettings.advancedItemTooltips;
            MetaBlock block = MetaBlock.of(state);
            String name = APIBlockDisplay.hasCustomName(block) ? APIBlockDisplay.getMetaBlockName(block) : display.getDisplayName()
                    + (advanced ? (" (" + block.getBlock().getRegistryName() + "/" + block.getMetadata() + ")") : "");
            ScaledResolution resolution = new ScaledResolution(minecraft);
            boolean hasAdditional = APIBlockDisplay.hasCustomBlockInfoHandler(block.getBlock());
            boolean hasTE = world.getTileEntity(pos) != null;
            FontRenderer font = minecraft.fontRenderer;
            switch (getDisplayLocation()) {
                case LEFT: {
                    RenderHelper.enableGUIStandardItemLighting();
                    minecraft.getRenderItem().renderItemIntoGUI(display, 1, 1);
                    RenderHelper.disableStandardItemLighting();
                    int x = 18;
                    int y = 1;
                    font.drawStringWithShadow(name, x, 1, 0xffffffff);
                    y += 9;
                    if (hasTE && advanced) {
                        font.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.has_te"), x, y, 0xffaaaaaa);
                        y += 9;
                    }
                    if (!BlockDisplayConfig.disableMineableInfo) {
                        String toolClass = BlockDisplay.getToolClass(world.getBlockState(pos));
                        int toolLevel = BlockDisplay.getToolLevel(world.getBlockState(pos));
                        boolean noTool;
                        if ("null".equals(toolClass) || toolClass == null) {
                            noTool = true;
                            font.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.no_tool"), x, y, 0xffcccccc);
                        } else {
                            noTool = !"pickaxe".equals(toolClass);
                            font.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.mining_level") + ": "
                                    + toolClass + " lvl " + toolLevel, x, y, 0xffcccccc);
                            ItemStack tool = getToolStack(toolClass, toolLevel);
                            if (!tool.isEmpty()) {
                                int w = (font.getStringWidth(InternalLangKeyCache.translate("info.block_display.mining_level") + ": "
                                        + toolClass + " lvl " + toolLevel) + 20) * 2;
                                GlStateManager.scale(0.5, 0.5, 0.5);
                                RenderHelper.enableGUIStandardItemLighting();
                                minecraft.getRenderItem().renderItemIntoGUI(tool, w, y * 2);
                                RenderHelper.disableStandardItemLighting();
                                GlStateManager.scale(2, 2, 2);
                            }
                        }
                        y += 8;
                        boolean mineable = (BlockDisplay.getToolClasses(minecraft.player.getHeldItem(EnumHand.MAIN_HAND)).contains(toolClass) &&
                                BlockDisplay.getToolLevel(minecraft.player.getHeldItem(EnumHand.MAIN_HAND), toolClass, minecraft.player, world.getBlockState(pos)) >= toolLevel)
                                || noTool;
                        font.drawStringWithShadow(mineable ?
                                        InternalLangKeyCache.translate("info.block_display.mineable") :
                                        InternalLangKeyCache.translate("info.block_display.non_mineable"),
                                x, y, mineable ? 0xffcccccc : 0xffffcccc);
                        y += 9;
                    }
                    if (hasAdditional) {
                        IAdditionalBlockInfo handler = APIBlockDisplay.getCustomBlockInfoHandler(block.getBlock());
                        List<String> keys = handler.getAdditionalBlockInfo(world, pos, state);
                        for (String key : keys) {
                            String translation = InternalLangKeyCache.translate(key);
                            font.drawStringWithShadow(translation, x, y, 0xffcccccc);
                            y += 8;
                        }
                    }
                    try {
                        TileEntity te = world.getTileEntity(pos);
                        if (te instanceof IInventory) {
                            if (InternalIInventoryCache.isIsLoadedOnServer()) {
                                if (pos.toLong() != InternalIInventoryCache.pos.toLong()) {
                                    font.drawStringWithShadow("[!] " + InternalLangKeyCache.translate("info.block_display.inventory_warn"), x, y, 0xffff0000);
                                    y += 9;
                                    InternalIInventoryCache.requestInventoryUpdate(world, pos);
                                } else if (InternalIInventoryCache.lastFrameNoRender) {
                                    InternalIInventoryCache.lastFrameNoRender = false;
                                    InternalIInventoryCache.requestInventoryUpdate(world, pos);
                                } else if (InternalIInventoryCache.requestCooldown <= 0)
                                    InternalIInventoryCache.requestInventoryUpdate(world, pos);
                                List<ItemStack> stacks = InternalIInventoryCache.stacks;
                                FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames();
                                if (!stacks.isEmpty()) {
                                    y++;
                                    font.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.inventory") + ": ", x, y, 0xffeeeeee);
                                    y += 9;
                                    RenderHelper.enableGUIStandardItemLighting();
                                    RenderItem render = minecraft.getRenderItem();
                                    int sx = 0;
                                    for (ItemStack stack : stacks) {
                                        if (sx > 5) {
                                            sx = 0;
                                            y += 17;
                                        }
                                        render.renderItemIntoGUI(stack, x + sx * 17, y);
                                        boolean rescale = stack.getCount() > 99;
                                        if (rescale) {
                                            GlStateManager.scale(0.5, 0.5, 0.5);
                                            render.renderItemOverlays(font, stack, (x + sx * 17) * 2 + 16, y * 2 + 16);
                                            GlStateManager.scale(2, 2, 2);
                                        } else {
                                            render.renderItemOverlays(font, stack, x + sx * 17, y);
                                        }
                                        sx++;
                                    }
                                    y += 17;
                                    RenderHelper.disableStandardItemLighting();
                                }
                            } else {
                                font.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.no_server"), x, y, 0xffff0000);
                                y += 9;
                            }
                        } else {
                            InternalIInventoryCache.clear();
                        }
                    } catch (IndexOutOfBoundsException ignored) {
                    } catch (Throwable error) {
                        BlockDisplay.logger.fatal("{}: {}", error.getClass().getCanonicalName(), error.getLocalizedMessage());
                    }
                    if (advanced) {
                        int[] ids = BlockDictionary.getOreIDs(block);
                        if (ids.length > 0) {
                            font.drawStringWithShadow(InternalLangKeyCache.translate("tooltip.melonlib.blockdict_entries") + ':', x, y, 0xffbbbbbb);
                            y += 9;
                            for (int id : ids) {
                                String ore = BlockDictionary.getOreName(id);
                                font.drawStringWithShadow(" *" + ore, x, y, 0xffaaaaaa);
                                y += 8;
                            }
                        }
                    }
                } break;
                case RIGHT: {
                    minecraft.getRenderItem().renderItemIntoGUI(display, resolution.getScaledWidth() - 17, 1);
                } break;
                case MIDDLE: {

                } break;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private static ItemStack getToolStack(String toolClass, int lvl) {
        if ("pickaxe".equals(toolClass)) {
            switch (lvl) {
                case -1:
                case 0: return new ItemStack(Items.WOODEN_PICKAXE);
                case 1: return new ItemStack(Items.STONE_PICKAXE);
                case 3: return new ItemStack(Items.DIAMOND_PICKAXE);
                default: return new ItemStack(Items.IRON_PICKAXE);
            }
        }
        if ("axe".equals(toolClass)) {
            switch (lvl) {
                case -1:
                case 0: return new ItemStack(Items.WOODEN_AXE);
                case 1: return new ItemStack(Items.STONE_AXE);
                case 3: return new ItemStack(Items.DIAMOND_AXE);
                default: return new ItemStack(Items.IRON_PICKAXE);
            }
        }
        if ("shovel".equals(toolClass)) {
            switch (lvl) {
                case -1:
                case 0: return new ItemStack(Items.WOODEN_SHOVEL);
                case 1: return new ItemStack(Items.STONE_SHOVEL);
                case 3: return new ItemStack(Items.DIAMOND_SHOVEL);
                default: return new ItemStack(Items.IRON_SHOVEL);
            }
        }
        if ("hoe".equals(toolClass)) {
            switch (lvl) {
                case -1:
                case 0: return new ItemStack(Items.WOODEN_HOE);
                case 1: return new ItemStack(Items.STONE_HOE);
                case 3: return new ItemStack(Items.DIAMOND_HOE);
                default: return new ItemStack(Items.IRON_HOE);
            }
        }
        if ("sword".equals(toolClass)) {
            switch (lvl) {
                case -1:
                case 0: return new ItemStack(Items.WOODEN_SWORD);
                case 1: return new ItemStack(Items.STONE_SWORD);
                case 3: return new ItemStack(Items.DIAMOND_SWORD);
                default: return new ItemStack(Items.IRON_SWORD);
            }
        }
        if ("shears".equals(toolClass)) return new ItemStack(Items.SHEARS);
        return ItemStack.EMPTY;
    }

    @SideOnly(Side.CLIENT)
    private static void renderEntityInfo(Minecraft minecraft, World world, Entity entity, RayTraceResult result) {
        ItemStack display = entity.getPickedResult(result);
        String name = entity.getDisplayName().getFormattedText();
        boolean advanced = BlockDisplayConfig.enforceAdvancedInfo || minecraft.gameSettings.advancedItemTooltips;
        int x = display.isEmpty() ? 1 : 18;
        int y = 1;
        if (!display.isEmpty()) {
            RenderHelper.enableGUIStandardItemLighting();
            minecraft.getRenderItem().renderItemIntoGUI(display, 1, 1);
            RenderHelper.disableStandardItemLighting();
        }
        minecraft.fontRenderer.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.entity") + ": " + name, x, y, -1);
        y += 9;
        if (advanced) {
            minecraft.fontRenderer.drawStringWithShadow(entity.getUniqueID().toString(), x, y, 0xffaaaaaa);
            y += 9;
        }
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            minecraft.fontRenderer.drawStringWithShadow((int) living.getHealth() + " / " + (int) living.getMaxHealth() + " HP", x, y, 0xffdddddd);
        }

    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void buttonPressed(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.getGui() instanceof GuiLanguage) {
            InternalLangKeyCache.clear();
            BlockDisplay.logger.info("Cleared InternalLangKeyCache!");
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        InternalIInventoryCache.testServer();
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        InventoryPacketLimiter.receivedRequestThisTick = false;
    }

    @SubscribeEvent
    public static void joinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        BlockDisplay.logger.info("Joined server from client: {}", BlockDisplay.isClient());
        InternalIInventoryCache.allowTesting = true;
        InternalIInventoryCache.serverTestCooldown = 20;
    }

    @SubscribeEvent
    public static void leaveServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        BlockDisplay.logger.info("Left server from client: {}", BlockDisplay.isClient());
        InternalIInventoryCache.allowTesting = false;
        InternalIInventoryCache.serverTestCooldown = 20;
        InternalIInventoryCache.revokeServerPresence();
    }
}

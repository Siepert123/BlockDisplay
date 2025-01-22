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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
            boolean advanced = minecraft.gameSettings.advancedItemTooltips;
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
                    if (hasTE) {
                        font.drawStringWithShadow(InternalLangKeyCache.translate("info.block_display.has_te"), x, y, 0xffaaaaaa);
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
                            BlockDisplay.network.sendToServer(new PacketRequestInventory(pos, world.provider.getDimension()));
                            if (pos.toLong() != InternalIInventoryCache.pos.toLong()) {
                                font.drawStringWithShadow("[!] " + InternalLangKeyCache.translate("info.block_display.inventory_warn"), x, y, 0xffff0000);
                                y += 9;
                            }
                            List<ItemStack> stacks = InternalIInventoryCache.stacks;
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
                                    render.renderItemOverlays(font, stack, x + sx * 17, y);
                                    sx++;
                                }
                                RenderHelper.disableStandardItemLighting();
                            }
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
    private static void renderEntityInfo(Minecraft minecraft, World world, Entity entity, RayTraceResult result) {
        ItemStack display = entity.getPickedResult(result);
        String name = entity.getDisplayName().getFormattedText();
        boolean advanced = minecraft.gameSettings.advancedItemTooltips;
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
}

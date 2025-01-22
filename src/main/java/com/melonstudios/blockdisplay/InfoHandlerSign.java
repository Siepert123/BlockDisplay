package com.melonstudios.blockdisplay;

import com.melonstudios.melonlib.api.blockdisplay.IAdditionalBlockInfo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InfoHandlerSign implements IAdditionalBlockInfo {
    public static final IAdditionalBlockInfo instance = new InfoHandlerSign();

    @Override
    public List<String> getAdditionalBlockInfo(World world, BlockPos blockPos, IBlockState iBlockState) {
        TileEntity te = world.getTileEntity(blockPos);
        if (te instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) te;
            ITextComponent[] textComponents = sign.signText;
            List<String> texts = new ArrayList<>();
            texts.add(InternalLangKeyCache.translate("info.block_display.sign_text") + ':');
            for (ITextComponent component : textComponents) {
                texts.add("|" + component.getFormattedText());
            }
            return texts;
        }
        return Collections.emptyList();
    }
}

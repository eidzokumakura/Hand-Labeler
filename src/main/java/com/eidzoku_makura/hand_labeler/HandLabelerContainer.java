package com.eidzoku_makura.hand_labeler;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class HandLabelerContainer extends SimpleContainer {
    public HandLabelerContainer() {
        super(2); // 2 слота: для бирок и для предмета
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}

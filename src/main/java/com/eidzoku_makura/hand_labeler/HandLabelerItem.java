package com.eidzoku_makura.hand_labeler;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HandLabeler.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HandLabelerItem extends Item {
    public HandLabelerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, playerEntity) ->
                            new HandLabelerMenu(containerId, playerInventory),
                    itemStack.getHoverName()
            ));
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    // Добавляем предмет в креативную вкладку Tools and Utilities
    @SubscribeEvent
    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(HandLabeler.HAND_LABELER.get());
        }
    }
}
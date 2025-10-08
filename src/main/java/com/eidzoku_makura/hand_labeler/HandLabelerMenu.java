package com.eidzoku_makura.hand_labeler;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HandLabelerMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Player player;

    public HandLabelerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public HandLabelerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public HandLabelerMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(HandLabeler.HAND_LABELER_MENU.get(), containerId);
        this.access = access;
        this.player = playerInventory.player;

        // Слот для бирок (0) - только 1 бирка
        this.addSlot(new Slot(new HandLabelerContainer(), 0, 44, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() == Items.NAME_TAG;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Слот для предмета для переименования (1) - теперь разрешаем бирки
        this.addSlot(new Slot(new HandLabelerContainer(), 1, 116, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                // Разрешаем любые предметы, кроме самого Hand Labeler
                return stack.getItem() != HandLabeler.HAND_LABELER.get();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Слоты инвентаря игрока
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Слоты хотбара
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // Метод для переименования предмета - теперь с поддержкой переименования бирок
    public void renameItem(String newName) {
        // Выполняем только на серверной стороне
        if (!player.level().isClientSide) {
            Slot nameTagSlot = this.slots.get(0);
            Slot itemSlot = this.slots.get(1);

            // Проверяем, что во втором слоте есть предмет
            if (itemSlot.hasItem()) {
                ItemStack itemToRename = itemSlot.getItem();

                // Если переименовываем бирку - не требуется бирка в первом слоте
                if (itemToRename.getItem() == Items.NAME_TAG) {
                    // Создаем копию бирки с новым именем
                    ItemStack renamedItem = itemToRename.copy();

                    // Переименовываем бирку
                    if (!newName.isEmpty()) {
                        renamedItem.setHoverName(net.minecraft.network.chat.Component.literal(newName));
                    }

                    // Очищаем слот с биркой
                    itemSlot.set(ItemStack.EMPTY);

                    // Пытаемся добавить переименованную бирку в инвентарь игрока
                    if (!this.player.getInventory().add(renamedItem)) {
                        // Если не помещается - выкидываем в мир
                        this.player.drop(renamedItem, false);
                    }

                    // Обновляем слоты
                    this.broadcastChanges();
                }
                // Если переименовываем обычный предмет - требуется бирка в первом слоте
                else if (nameTagSlot.hasItem()) {
                    ItemStack nameTags = nameTagSlot.getItem();

                    // Проверяем, что есть бирка
                    if (!nameTags.isEmpty()) {
                        // Создаем копию предмета с новым именем
                        ItemStack renamedItem = itemToRename.copy();

                        // Переименовываем предмет
                        if (!newName.isEmpty()) {
                            renamedItem.setHoverName(net.minecraft.network.chat.Component.literal(newName));
                        }

                        // Очищаем слоты
                        nameTagSlot.set(ItemStack.EMPTY); // Удаляем бирку
                        itemSlot.set(ItemStack.EMPTY); // Удаляем оригинальный предмет

                        // Пытаемся добавить переименованный предмет в инвентарь игрока
                        if (!this.player.getInventory().add(renamedItem)) {
                            // Если не помещается - выкидываем в мир
                            this.player.drop(renamedItem, false);
                        }

                        // Обновляем слоты
                        this.broadcastChanges();
                    }
                }
            }
        }
    }

    // Метод для возврата предметов при закрытии GUI
    @Override
    public void removed(Player player) {
        super.removed(player);

        // Возвращаем предметы только на серверной стороне
        if (!player.level().isClientSide) {
            returnItemToPlayer(player, 0); // Бирки
            returnItemToPlayer(player, 1); // Предмет
        }
    }

    private void returnItemToPlayer(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack = slot.getItem();

            if (!player.getInventory().add(itemstack)) {
                player.drop(itemstack, false);
            }

            slot.set(ItemStack.EMPTY);
        }
    }
}
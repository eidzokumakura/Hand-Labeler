package com.eidzoku_makura.hand_labeler;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class HandLabelerScreen extends AbstractContainerScreen<HandLabelerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("hand_labeler", "textures/gui/hand_labeler.png");

    private EditBox nameField;
    private Button renameButton;

    public HandLabelerScreen(HandLabelerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();

        // Поле для ввода названия
        this.nameField = new EditBox(this.font, this.leftPos + 25, this.topPos + 5, 126, 12,
                Component.translatable("hand_labeler.name_field"));
        this.nameField.setMaxLength(50);
        this.nameField.setBordered(false);
        this.nameField.setTextColor(0xFFFFFF);
        this.nameField.setCanLoseFocus(false);
        this.addRenderableWidget(this.nameField);

        // Кнопка переименования - теперь отправляет пакет на сервер
        this.renameButton = Button.builder(Component.translatable("hand_labeler.rename"), button -> {
            // Отправляем пакет на сервер вместо прямого вызова метода
            HandLabeler.CHANNEL.sendToServer(new RenameItemPacket(this.nameField.getValue()));
        }).bounds(this.leftPos + 45, this.topPos + 40, 86, 20).build();

        this.addRenderableWidget(this.renameButton);

        this.setInitialFocus(this.nameField);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }

        if (this.nameField.keyPressed(keyCode, scanCode, modifiers) ||
                this.nameField.isFocused()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.nameField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.nameField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.nameField);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }
}
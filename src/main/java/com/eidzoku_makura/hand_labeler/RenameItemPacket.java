package com.eidzoku_makura.hand_labeler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RenameItemPacket {
    private final String newName;

    public RenameItemPacket(String newName) {
        this.newName = newName;
    }

    public static void encode(RenameItemPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.newName);
    }

    public static RenameItemPacket decode(FriendlyByteBuf buffer) {
        return new RenameItemPacket(buffer.readUtf(32767));
    }

    public static void handle(RenameItemPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player != null && player.containerMenu instanceof HandLabelerMenu) {
                HandLabelerMenu menu = (HandLabelerMenu) player.containerMenu;
                menu.renameItem(packet.newName);
            }
        });
        context.get().setPacketHandled(true);
    }
}
package com.example.chronocontrol.network;

import com.example.chronocontrol.ChronoControl;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ChronoNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ChronoControl.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    private static int id = 0;

    private ChronoNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(SnapshotPacket.class, id++, net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
                .encoder(SnapshotPacket::encode).decoder(SnapshotPacket::decode)
                .consumerMainThread(SnapshotPacket::handle).add();
        CHANNEL.messageBuilder(RewindPacket.class, id++, net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
                .encoder(RewindPacket::encode).decoder(RewindPacket::decode)
                .consumerMainThread(RewindPacket::handle).add();
        CHANNEL.messageBuilder(TimeModePacket.class, id++, net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER)
                .encoder(TimeModePacket::encode).decoder(TimeModePacket::decode)
                .consumerMainThread(TimeModePacket::handle).add();
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(Object packet, net.minecraft.server.level.ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}

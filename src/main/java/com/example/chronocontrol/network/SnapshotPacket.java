package com.example.chronocontrol.network;

import com.example.chronocontrol.server.ChronoState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SnapshotPacket {
    public SnapshotPacket() {}
    public static void encode(SnapshotPacket msg, FriendlyByteBuf buf) {}
    public static SnapshotPacket decode(FriendlyByteBuf buf) { return new SnapshotPacket(); }
    public static void handle(SnapshotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) ChronoState.saveSnapshot(ctx.get().getSender());
        });
        ctx.get().setPacketHandled(true);
    }
}

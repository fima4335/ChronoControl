package com.example.chronocontrol.network;

import com.example.chronocontrol.server.ChronoState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class RewindPacket {
    private final boolean pressed;
    public RewindPacket(boolean pressed) { this.pressed = pressed; }
    public static void encode(RewindPacket msg, FriendlyByteBuf buf) { buf.writeBoolean(msg.pressed); }
    public static RewindPacket decode(FriendlyByteBuf buf) { return new RewindPacket(buf.readBoolean()); }
    public static void handle(RewindPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                if (msg.pressed) ChronoState.startRewind(ctx.get().getSender());
                else ChronoState.stopRewind(ctx.get().getSender());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

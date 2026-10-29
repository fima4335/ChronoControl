package com.example.chronocontrol.network;

import com.example.chronocontrol.server.ChronoState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class TimeModePacket {
    private final int mode;
    public TimeModePacket(int mode) { this.mode = mode; }
    public static void encode(TimeModePacket msg, FriendlyByteBuf buf) { buf.writeVarInt(msg.mode); }
    public static TimeModePacket decode(FriendlyByteBuf buf) { return new TimeModePacket(buf.readVarInt()); }
    public static void handle(TimeModePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) ChronoState.setTimeMode(ctx.get().getSender(), msg.mode);
        });
        ctx.get().setPacketHandled(true);
    }
}

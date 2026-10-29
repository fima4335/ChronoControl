package com.example.chronocontrol.client;

import com.example.chronocontrol.ChronoControl;
import com.example.chronocontrol.network.ChronoNetwork;
import com.example.chronocontrol.network.RewindPacket;
import com.example.chronocontrol.network.SnapshotPacket;
import com.example.chronocontrol.network.TimeModePacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ChronoControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChronoClient {
    public static final KeyMapping SNAPSHOT = new KeyMapping("key.chronocontrol.snapshot", GLFW.GLFW_KEY_R, "key.categories.chronocontrol");
    public static final KeyMapping REWIND = new KeyMapping("key.chronocontrol.rewind", GLFW.GLFW_KEY_Z, "key.categories.chronocontrol");
    public static final KeyMapping SPEED = new KeyMapping("key.chronocontrol.speed", GLFW.GLFW_KEY_X, "key.categories.chronocontrol");
    public static final KeyMapping PAUSE = new KeyMapping("key.chronocontrol.pause", GLFW.GLFW_KEY_C, "key.categories.chronocontrol");
    private static boolean rewindWasDown = false;
    private static int speedMode = 0;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(SNAPSHOT);
        event.register(REWIND);
        event.register(SPEED);
        event.register(PAUSE);
    }

    @Mod.EventBusSubscriber(modid = ChronoControl.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            while (SNAPSHOT.consumeClick()) {
                ChronoNetwork.sendToServer(new SnapshotPacket());
                mc.player.displayClientMessage(Component.literal("Chrono Control: снимок сохранён"), true);
            }
            while (SPEED.consumeClick()) {
                speedMode = (speedMode + 1) % 4;
                ChronoNetwork.sendToServer(new TimeModePacket(speedMode));
            }
            while (PAUSE.consumeClick()) {
                speedMode = speedMode == 0 ? 1 : 0;
                ChronoNetwork.sendToServer(new TimeModePacket(speedMode));
            }
            boolean down = REWIND.isDown();
            if (down != rewindWasDown) {
                ChronoNetwork.sendToServer(new RewindPacket(down));
                rewindWasDown = down;
            }
        }

        @SubscribeEvent
        public static void renderHud(RenderGuiOverlayEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            String[] labels = {"Обычное время", "Пауза", "×2", "×5", "×10"};
            int index = speedMode == 0 ? 0 : speedMode == 1 ? 1 : speedMode == 2 ? 2 : speedMode == 3 ? 3 : 4;
            event.getGuiGraphics().drawString(mc.font, "Chrono: " + labels[index], 8, 8, 0xAADDFF, true);
        }
    }
}

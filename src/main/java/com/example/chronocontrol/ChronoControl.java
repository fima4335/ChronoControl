package com.example.chronocontrol;

import com.example.chronocontrol.config.ChronoConfig;
import com.example.chronocontrol.network.ChronoNetwork;
import com.example.chronocontrol.server.ChronoServerEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ChronoControl.MOD_ID)
public class ChronoControl {
    public static final String MOD_ID = "chronocontrol";

    public ChronoControl() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ChronoConfig.SPEC);
        ChronoNetwork.register();
        MinecraftForge.EVENT_BUS.register(new ChronoServerEvents());
    }
}

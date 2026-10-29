package com.example.chronocontrol.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ChronoConfig {
    private ChronoConfig() {}

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SAVE_BLOCKS;
    public static final ForgeConfigSpec.IntValue BLOCK_RADIUS;
    public static final ForgeConfigSpec.IntValue TRAJECTORY_SECONDS;
    public static final ForgeConfigSpec.IntValue COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_EXPERIENCE;
    public static final ForgeConfigSpec.IntValue EXPERIENCE_COST;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CLIENT_EFFECTS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("snapshot");
        SAVE_BLOCKS = b.comment("Capture nearby blocks in the snapshot. Use cautiously on servers.")
                .define("saveBlocks", false);
        BLOCK_RADIUS = b.comment("Block snapshot radius, 0-8.")
                .defineInRange("blockRadius", 3, 0, 8);
        b.pop();

        b.push("rewind");
        TRAJECTORY_SECONDS = b.comment("How many seconds of player trajectory to retain.")
                .defineInRange("trajectorySeconds", 30, 5, 120);
        COOLDOWN_SECONDS = b.comment("Cooldown between snapshot recalls.")
                .defineInRange("cooldownSeconds", 10, 0, 3600);
        REQUIRE_EXPERIENCE = b.comment("Charge experience levels for a recall.")
                .define("requireExperience", false);
        EXPERIENCE_COST = b.comment("Experience levels charged per recall.")
                .defineInRange("experienceCost", 1, 0, 100);
        b.pop();

        b.push("visuals");
        ENABLE_CLIENT_EFFECTS = b.comment("Enable client-side visual effects.")
                .define("enableClientEffects", true);
        b.pop();
        SPEC = b.build();
    }
}

package com.example.chronocontrol.server;

import com.example.chronocontrol.config.ChronoConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ChronoState {
    private static final Map<UUID, Snapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final Map<UUID, Deque<Frame>> TRAJECTORIES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> REWINDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> TIME_MODES = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> REWIND_INDEX = new ConcurrentHashMap<>();

    private ChronoState() {}

    public static void saveSnapshot(ServerPlayer p) {
        Snapshot s = new Snapshot();
        s.dimension = p.level().dimension();
        s.pos = p.position();
        s.yaw = p.getYRot(); s.pitch = p.getXRot();
        s.health = p.getHealth();
        s.food = p.getFoodData().getFoodLevel();
        s.saturation = p.getFoodData().getSaturationLevel();
        s.inventory = new ArrayList<>();
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) s.inventory.add(p.getInventory().getItem(i).copy());
        s.effects = new ArrayList<>();
        for (MobEffectInstance e : p.getActiveEffects()) s.effects.add(e);
        s.dayTime = p.level().getDayTime();
        SNAPSHOTS.put(p.getUUID(), s);
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Снимок Chrono Control сохранён"), true);
    }

    public static void startRewind(ServerPlayer p) {
        REWINDING.put(p.getUUID(), true);
        REWIND_INDEX.put(p.getUUID(), -1);
        recallSnapshot(p);
    }

    public static void stopRewind(ServerPlayer p) {
        REWINDING.remove(p.getUUID());
        REWIND_INDEX.remove(p.getUUID());
    }

    private static void recallSnapshot(ServerPlayer p) {
        Snapshot s = SNAPSHOTS.get(p.getUUID());
        if (s == null) {
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Сначала сохраните снимок клавишей R"), true);
            return;
        }
        long now = p.level().getGameTime();
        long last = COOLDOWNS.getOrDefault(p.getUUID(), Long.MIN_VALUE / 2);
        if (now - last < ChronoConfig.COOLDOWN_SECONDS.get() * 20L) {
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Откат ещё на перезарядке"), true);
            return;
        }
        if (ChronoConfig.REQUIRE_EXPERIENCE.get()) {
            int cost = ChronoConfig.EXPERIENCE_COST.get();
            if (p.experienceLevel < cost) {
                p.displayClientMessage(net.minecraft.network.chat.Component.literal("Недостаточно опыта"), true);
                return;
            }
            p.giveExperienceLevels(-cost);
        }
        ServerLevel target = p.server.getLevel(s.dimension);
        if (target == null) return;
        if (p.level() != target) p.teleportTo(target, s.pos.x, s.pos.y, s.pos.z, s.yaw, s.pitch);
        else p.teleportTo(s.pos.x, s.pos.y, s.pos.z);
        p.setYRot(s.yaw); p.setXRot(s.pitch);
        p.setHealth(Math.min(s.health, p.getMaxHealth()));
        p.getFoodData().setFoodLevel(s.food);
        p.getFoodData().setSaturation(s.saturation);
        for (int i = 0; i < Math.min(s.inventory.size(), p.getInventory().getContainerSize()); i++)
            p.getInventory().setItem(i, s.inventory.get(i).copy());
        p.removeAllEffects();
        for (MobEffectInstance e : s.effects) p.addEffect(new MobEffectInstance(e));
        target.setDayTime(s.dayTime);
        COOLDOWNS.put(p.getUUID(), now);
        p.fallDistance = 0;
    }

    public static void setTimeMode(ServerPlayer p, int mode) {
        TIME_MODES.put(p.getUUID(), Math.max(0, Math.min(4, mode)));
    }

    public static void tickPlayer(ServerPlayer p) {
        UUID id = p.getUUID();
        Deque<Frame> frames = TRAJECTORIES.computeIfAbsent(id, k -> new ArrayDeque<>());
        frames.addFirst(new Frame(p.position(), p.getYRot(), p.getXRot(), p.level().dimension()));
        int max = ChronoConfig.TRAJECTORY_SECONDS.get() * 20;
        while (frames.size() > max) frames.removeLast();
        if (Boolean.TRUE.equals(REWINDING.get(id))) {
            // Snapshot recall on press; trajectory playback can be extended here.
        }
    }

    public static void tickLevel(ServerLevel level) {
        for (ServerPlayer p : level.players()) {
            int mode = TIME_MODES.getOrDefault(p.getUUID(), 0);
            int extra = switch (mode) { case 2 -> 1; case 3 -> 4; case 4 -> 9; default -> 0; };
            if (extra > 0) level.setDayTime(level.getDayTime() + extra);
            if (mode == 1) level.setDayTime(level.getDayTime() - 1);
        }
    }

    private static class Snapshot {
        net.minecraft.resources.ResourceKey<Level> dimension;
        Vec3 pos; float yaw, pitch, health; int food; float saturation; long dayTime;
        List<ItemStack> inventory;
        List<MobEffectInstance> effects;
    }
    private record Frame(Vec3 pos, float yaw, float pitch, net.minecraft.resources.ResourceKey<Level> dimension) {}
}

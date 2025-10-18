package io.github.hyisnoob.railgunsounds.listener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAreaListener {
    private static final double HALF_SIZE = 250.0;
    private static final double MIN_X = -HALF_SIZE;
    private static final double MAX_X = HALF_SIZE;
    private static final double MIN_Z = -HALF_SIZE;
    private static final double MAX_Z = HALF_SIZE;

    private static final Map<UUID, Boolean> previousInside = new ConcurrentHashMap<>();
    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> {
            Set<UUID> currentlyOnline = ConcurrentHashMap.newKeySet();

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                UUID id = player.getUuid();
                currentlyOnline.add(id);

                double x = player.getX();
                double z = player.getZ();
                boolean inside = x >= MIN_X && x <= MAX_X && z >= MIN_Z && z <= MAX_Z;

                boolean wasInside = previousInside.getOrDefault(id, false);

                if (!wasInside && inside) {
                    OrbitalRailgunSounds.LOGGER.info("Player entered 500x500 area: " + player.getName().getString() + " (x=" + x + ", z=" + z + ")");
                } else if (wasInside && !inside) {
                    OrbitalRailgunSounds.LOGGER.info("Player left 500x500 area: " + player.getName().getString() + " (x=" + x + ", z=" + z + ")");
                }

                previousInside.put(id, inside);
            }
            previousInside.keySet().removeIf(uuid -> !currentlyOnline.contains(uuid));
        });
    }
}


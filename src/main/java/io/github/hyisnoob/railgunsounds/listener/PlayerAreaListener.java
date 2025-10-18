package io.github.hyisnoob.railgunsounds.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAreaListener {
    private static final Map<UUID, Boolean> previousInside = new ConcurrentHashMap<>();

    public static void handlePlayerAreaCheck(ServerPlayerEntity player) {
        double soundRange = ServerConfig.INSTANCE.getSoundRange();
        double halfSize = soundRange / 2.0;
        double minX = -halfSize;
        double maxX = halfSize;
        double minZ = -halfSize;
        double maxZ = halfSize;

        UUID id = player.getUuid();
        double x = player.getX();
        double z = player.getZ();
        boolean inside = x >= minX && x <= maxX && z >= minZ && z <= maxZ;

        boolean wasInside = previousInside.getOrDefault(id, false);

        if (!wasInside && inside) {
            OrbitalRailgunSounds.LOGGER.info("Player entered area: " + player.getName().getString() + " (x=" + x + ", z=" + z + ")");
        } else if (wasInside && !inside) {
            OrbitalRailgunSounds.LOGGER.info("Player left area: " + player.getName().getString() + " (x=" + x + ", z=" + z + ")");
        }

        previousInside.put(id, inside);
    }
}

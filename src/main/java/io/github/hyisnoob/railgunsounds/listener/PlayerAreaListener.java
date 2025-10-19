package io.github.hyisnoob.railgunsounds.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAreaListener {
    private static final Map<UUID, Boolean> previousInside = new ConcurrentHashMap<>();

    public static boolean handlePlayerAreaCheck(ServerPlayerEntity player, double laserX, double laserZ) {
        double soundRange = ServerConfig.INSTANCE.getSoundRange();
        double halfSize = soundRange / 2.0;
        double minX = laserX - halfSize;
        double maxX = laserX + halfSize;
        double minZ = laserZ - halfSize;
        double maxZ = laserZ + halfSize;

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        double minY = player.getWorld().getBottomY();
        double maxY = player.getWorld().getTopY();

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ && y >= minY && y <= maxY;
    }
}

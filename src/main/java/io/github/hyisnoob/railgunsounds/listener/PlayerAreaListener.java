package io.github.hyisnoob.railgunsounds.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import io.github.hyisnoob.railgunsounds.registry.SoundsRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class PlayerAreaListener {
    private static final Map<UUID, Boolean> previousInside = new ConcurrentHashMap<>();
    public static final Identifier SHOOT_PACKET_ID = new Identifier("orbital_railgun", "shoot_packet");

    public static void registerPacketListener() {
        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            var blockPos = buf.readBlockPos();

            server.execute(() -> {
                double laserX = blockPos.getX() + 0.5;
                double laserZ = blockPos.getZ() + 0.5;

                PlayerAreaListener.handlePlayerAreaCheck(player, laserX, laserZ);
            });
        });
    }

    public static void handlePlayerAreaCheck(ServerPlayerEntity player, double laserX, double laserZ) {
        double soundRange = ServerConfig.INSTANCE.getSoundRange();
        double halfSize = soundRange / 2.0;
        double minX = laserX - halfSize;
        double maxX = laserX + halfSize;
        double minZ = laserZ - halfSize;
        double maxZ = laserZ + halfSize;

        UUID id = player.getUuid();
        double x = player.getX();
        double z = player.getZ();
        boolean inside = x >= minX && x <= maxX && z >= minZ && z <= maxZ;

        boolean wasInside = previousInside.getOrDefault(id, false);

        if (!wasInside && inside) {
            OrbitalRailgunSounds.LOGGER.info("Player entered laser area: " + player.getName().getString() + " (x=" + x + ", z=" + z + ")");
        } else if (wasInside && !inside) {
            OrbitalRailgunSounds.LOGGER.info("Player left laser area: " + player.getName().getString() + " (x=" + x + ", z=" + z + ")");
        }

        previousInside.put(id, inside);
    }

}

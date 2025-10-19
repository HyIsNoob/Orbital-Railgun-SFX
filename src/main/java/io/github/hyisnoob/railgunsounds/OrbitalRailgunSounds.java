package io.github.hyisnoob.railgunsounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hyisnoob.railgunsounds.registry.SoundsRegistry;
import io.github.hyisnoob.railgunsounds.registry.CommandRegistry;
import io.github.hyisnoob.railgunsounds.logger.SoundLogger;
import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import io.github.hyisnoob.railgunsounds.listener.PlayerAreaListener;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class OrbitalRailgunSounds implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun_sounds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");
    public static final Identifier AREA_CHECK_PACKET_ID = new Identifier(MOD_ID, "area_check");
    public static final Identifier SHOOT_PACKET_ID = new Identifier("orbital_railgun", "shoot_packet");

    @Override
    public void onInitialize() {
        ServerConfig.INSTANCE.loadConfig();
        SoundsRegistry.initialize();
        CommandRegistry.registerCommands();
        LOGGER.info("Orbital Railgun Sounds Addon initialized. Sound events registered");

        // Register player disconnect listener to clean up state
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlayerAreaListener.clearPlayerState(handler.getPlayer().getUuid());
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Cleared area state for disconnected player: {}", 
                    handler.getPlayer().getName().getString());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(PLAY_SOUND_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    Identifier soundId = buf.readIdentifier();
                    SoundEvent sound = Registries.SOUND_EVENT.get(soundId);
                    BlockPos blockPos = buf.readBlockPos();
                    float volumeShoot = buf.readFloat();
                    float pitchShoot = buf.readFloat();

                    server.execute(() -> {
                        if (sound == null) {
                            OrbitalRailgunSounds.LOGGER.warn("Received unknown sound id: " + soundId.toString());
                            return;
                        }

                        double range = ServerConfig.INSTANCE.getSoundRange();
                        double rangeSquared = range * range;
                        double laserX = blockPos.getX() + 0.5;
                        double laserZ = blockPos.getZ() + 0.5;

                        if (ServerConfig.INSTANCE.isDebugMode()) {
                            LOGGER.info("Playing sound {} at BlockPos: {} with range: {}", 
                                soundId, blockPos, range);
                        }

                        // Check all players on the server to see if they're in range
                        server.getPlayerManager().getPlayerList().forEach(nearbyPlayer -> {
                            double distanceSquared = nearbyPlayer.squaredDistanceTo(
                                    blockPos.getX() + 0.5,
                                    blockPos.getY() + 0.5,
                                    blockPos.getZ() + 0.5
                            );
                            
                            if (distanceSquared <= rangeSquared) {
                                // Check if player is in the area using PlayerAreaListener
                                boolean isInRange = PlayerAreaListener.isPlayerInRange(nearbyPlayer, laserX, laserZ);
                                
                                if (isInRange) {
                                    nearbyPlayer.playSound(
                                            sound,
                                            SoundCategory.PLAYERS,
                                            volumeShoot,
                                            pitchShoot
                                    );
                                    SoundLogger.logSoundEvent(soundId.toString(), blockPos, range);
                                    
                                    if (ServerConfig.INSTANCE.isDebugMode()) {
                                        LOGGER.info("Playing sound to player {} (distance: {})", 
                                            nearbyPlayer.getName().getString(), 
                                            Math.sqrt(distanceSquared));
                                    }
                                }
                            }
                        });
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(AREA_CHECK_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    double laserX = buf.readDouble();
                    double laserZ = buf.readDouble();
                    
                    server.execute(() -> {
                        PlayerAreaListener.AreaCheckResult result = 
                            PlayerAreaListener.handlePlayerAreaCheck(player, laserX, laserZ);
                        
                        // Handle area state changes
                        handleAreaStateChange(player, result, laserX, laserZ);
                    });
                });

        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();

            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Received shoot packet from player: {} at BlockPos: {}", 
                    player.getName().getString(), blockPos);
            }

            server.execute(() -> {
                double laserX = blockPos.getX() + 0.5;
                double laserZ = blockPos.getZ() + 0.5;

                // Check all players on the server to see if they're in range
                server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                    PlayerAreaListener.AreaCheckResult result = 
                        PlayerAreaListener.handlePlayerAreaCheck(serverPlayer, laserX, laserZ);
                    
                    // Handle area state changes for each player
                    handleAreaStateChange(serverPlayer, result, laserX, laserZ);
                });
            });
        });
    }

    /**
     * Handles area state changes for a player (entering/leaving the sound range).
     * This is where sound starting/stopping logic should be implemented.
     */
    private static void handleAreaStateChange(ServerPlayerEntity player, 
                                             PlayerAreaListener.AreaCheckResult result, 
                                             double laserX, double laserZ) {
        if (result.hasEntered()) {
            // Player just entered the sound range
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Player {} entered sound range - sounds should start", 
                    player.getName().getString());
            }
            // TODO: Implement sound starting logic here
            // This would typically involve sending a packet to the client to start playing ambient sounds
            
        } else if (result.hasLeft()) {
            // Player just left the sound range
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Player {} left sound range - sounds should stop", 
                    player.getName().getString());
            }
            // TODO: Implement sound stopping logic here
            // This would typically involve sending a packet to the client to stop playing ambient sounds
            
        } else if (result.isInside) {
            // Player is still inside the range (no state change)
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.debug("Player {} remains in sound range", player.getName().getString());
            }
        }
    }
}

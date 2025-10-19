package io.github.hyisnoob.railgunsounds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hyisnoob.railgunsounds.registry.SoundsRegistry;
import io.github.hyisnoob.railgunsounds.registry.CommandRegistry;
import io.github.hyisnoob.railgunsounds.logger.SoundLogger;
import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import io.github.hyisnoob.railgunsounds.listener.PlayerAreaListener;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
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
    public static final Identifier STOP_AREA_SOUND_PACKET_ID = new Identifier(MOD_ID, "stop_area_sound");

    @Override
    public void onInitialize() {
        ServerConfig.INSTANCE.loadConfig();
        SoundsRegistry.initialize();
        CommandRegistry.registerCommands();
        
        LOGGER.info("=================================================");
        LOGGER.info("Orbital Railgun Sounds Addon initialized");
        LOGGER.info("Debug mode: {}", ServerConfig.INSTANCE.isDebugMode());
        LOGGER.info("Sound range: {}", ServerConfig.INSTANCE.getSoundRange());
        LOGGER.info("=================================================");

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

                        server.getPlayerManager().getPlayerList().forEach(nearbyPlayer -> {
                            double distanceSquared = nearbyPlayer.squaredDistanceTo(
                                    blockPos.getX() + 0.5,
                                    blockPos.getY() + 0.5,
                                    blockPos.getZ() + 0.5
                            );
                            
                            if (distanceSquared <= rangeSquared) {
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

        // AREA_CHECK_PACKET_ID is not needed - area checking happens server-side when railgun fires
        // Removed broken packet handler that expected coordinates the client doesn't have

        ServerPlayNetworking.registerGlobalReceiver(SHOOT_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            BlockPos blockPos = buf.readBlockPos();

            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("========================================");
                LOGGER.info("SHOOT_PACKET received from player: {}", player.getName().getString());
                LOGGER.info("Impact location: {}", blockPos);
            }

            server.execute(() -> {
                double laserX = blockPos.getX() + 0.5;
                double laserZ = blockPos.getZ() + 0.5;
                
                int totalPlayers = server.getPlayerManager().getPlayerList().size();
                if (ServerConfig.INSTANCE.isDebugMode()) {
                    LOGGER.info("Checking {} players on server for range", totalPlayers);
                }

                server.getPlayerManager().getPlayerList().forEach(serverPlayer -> {
                    PlayerAreaListener.AreaCheckResult result = 
                        PlayerAreaListener.handlePlayerAreaCheck(serverPlayer, laserX, laserZ);

                    handleAreaStateChange(serverPlayer, result, laserX, laserZ);
                });
                
                if (ServerConfig.INSTANCE.isDebugMode()) {
                    LOGGER.info("========================================");
                }
            });
        });
    }

    /**
     * Handles area state changes for a player (entering/leaving the sound range).
     * Plays railgun sounds to players who are in range when the railgun fires.
     */
    private static void handleAreaStateChange(ServerPlayerEntity player, 
                                             PlayerAreaListener.AreaCheckResult result, 
                                             double laserX, double laserZ) {
        if (result.hasEntered()) {
            // Player just entered the sound range
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Player {} entered sound range at ({}, {})", 
                    player.getName().getString(), laserX, laserZ);
            }
            
            // Play the railgun shoot sound to the player who just entered range
            playRailgunSoundToPlayer(player, laserX, laserZ);
            
        } else if (result.hasLeft()) {
            // Player just left the sound range - stop any playing area sounds
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Player {} left sound range at ({}, {}) - stopping sounds", 
                    player.getName().getString(), laserX, laserZ);
            }
            
            // Send packet to client to stop area-based sounds
            stopAreaSoundsForPlayer(player);
            
        } else if (result.isInside) {
            // Player is still inside the range (already heard the sound)
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.debug("Player {} remains in sound range at ({}, {})", 
                    player.getName().getString(), laserX, laserZ);
            }
        }
    }
    
    /**
     * Plays the railgun shoot sound to a specific player at the laser impact location.
     */
    private static void playRailgunSoundToPlayer(ServerPlayerEntity player, double laserX, double laserZ) {
        // Use the railgun shoot sound from the registry
        SoundEvent shootSound = SoundsRegistry.RAILGUN_SHOOT;
        
        if (shootSound != null) {
            // Play the sound at the laser impact location
            player.playSound(
                shootSound,
                SoundCategory.PLAYERS,
                1.0f,  // volume
                1.0f   // pitch
            );
            
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Playing railgun shoot sound to player {} at ({}, {})", 
                    player.getName().getString(), laserX, laserZ);
            }
        } else {
            LOGGER.warn("Railgun shoot sound not found in registry");
        }
    }
    
    /**
     * Sends a packet to the client to stop area-based sounds.
     */
    private static void stopAreaSoundsForPlayer(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        // Send the sound ID that should be stopped
        buf.writeIdentifier(SoundsRegistry.RAILGUN_SHOOT_ID);
        
        ServerPlayNetworking.send(player, STOP_AREA_SOUND_PACKET_ID, buf);
        
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("Sent stop sound packet to player {}", player.getName().getString());
        }
    }
}

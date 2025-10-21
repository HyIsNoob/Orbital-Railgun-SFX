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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Main mod class for Orbital Railgun Sounds addon.
 * 
 * Features:
 * - Custom sound effects for the Orbital Railgun mod (equip, scope, fire)
 * - Client-side sound position synchronization for multiplayer
 * 
 * Sound Synchronization System:
 * When a railgun fires, the server tracks the timestamp. If a player enters the sound range
 * after the sound has started playing, the client will receive a packet with the elapsed time
 * and attempt to seek the audio to the correct position using OpenAL, ensuring all players
 * hear the sound synchronized to the actual time it has been playing.
 * 
 * Flow:
 * 1. Server: Railgun fires, timestamp recorded (PlayerAreaListener.AreaState)
 * 2. Server: Player enters range, elapsed time calculated
 * 3. Server: PLAY_SOUND_WITH_OFFSET_PACKET_ID sent to client with offset
 * 4. Client: Packet received, SynchronizedSoundManager plays sound
 * 5. Client: OpenAL seeks audio to correct position (via reflection)
 * 6. Client: Sound plays synchronized with other players
 */
public class OrbitalRailgunSounds implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun_sounds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");
    public static final Identifier SHOOT_PACKET_ID = new Identifier("orbital_railgun", "shoot_packet");
    public static final Identifier STOP_AREA_SOUND_PACKET_ID = new Identifier(MOD_ID, "stop_area_sound");
    public static final Identifier PLAY_SOUND_WITH_OFFSET_PACKET_ID = new Identifier(MOD_ID, "play_sound_offset");
    
    // Duration of the railgun shoot sound effect in milliseconds (from railgun-shoot.ogg)
    public static final long RAILGUN_SOUND_DURATION_MS = 52992L; // ~53 seconds

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

        PlayerAreaListener.setAreaChangeCallback(event -> {
            handleAreaStateChange(event.player, event.result, event.laserX, event.laserZ);
        });

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
                    
                    long fireTimestamp = System.currentTimeMillis();

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
                            LOGGER.info("Playing sound {} at BlockPos: {} with range: {} at time {}", 
                                soundId, blockPos, range, fireTimestamp);
                        }

                        // Check all players and track state changes
                        server.getPlayerManager().getPlayerList().forEach(nearbyPlayer -> {
                            double distanceSquared = nearbyPlayer.squaredDistanceTo(
                                    blockPos.getX() + 0.5,
                                    blockPos.getY() + 0.5,
                                    blockPos.getZ() + 0.5
                            );
                            
                            if (distanceSquared <= rangeSquared) {
                                // Use PlayerAreaListener to track state changes with timestamp
                                PlayerAreaListener.AreaCheckResult result = 
                                    PlayerAreaListener.handlePlayerAreaCheck(nearbyPlayer, laserX, laserZ, fireTimestamp);
                                
                                if (result.isInside) {
                                    // Only play sound if player is in range
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
                                
                                // Handle state changes (enter/leave detection)
                                handleAreaStateChange(nearbyPlayer, result, laserX, laserZ);
                            } else {
                                // Player is outside range - check if they left the zone
                                PlayerAreaListener.AreaCheckResult result = 
                                    PlayerAreaListener.handlePlayerAreaCheck(nearbyPlayer, laserX, laserZ, fireTimestamp);
                                
                                if (result.hasLeft()) {
                                    // Player just left the range
                                    handleAreaStateChange(nearbyPlayer, result, laserX, laserZ);
                                }
                            }
                        });
                    });
                });

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

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 == 0) {
                server.getPlayerManager().getPlayerList().forEach(player -> {
                    PlayerAreaListener.checkPlayerPosition(player);
                });
            }
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
            long currentTime = System.currentTimeMillis();
            long elapsedMs = currentTime - result.fireTimestamp;
            
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Player {} entered sound range at ({}, {}) - elapsed: {}ms, duration: {}ms", 
                    player.getName().getString(), laserX, laserZ, elapsedMs, RAILGUN_SOUND_DURATION_MS);
            }
            
            // Only play sound if it hasn't finished yet
            if (elapsedMs < RAILGUN_SOUND_DURATION_MS) {
                // Play the railgun shoot sound to the player who just entered range
                playRailgunSoundToPlayer(player, laserX, laserZ, elapsedMs);
            } else {
                if (ServerConfig.INSTANCE.isDebugMode()) {
                    LOGGER.info("Sound already ended ({}ms > {}ms) - not playing for player {}", 
                        elapsedMs, RAILGUN_SOUND_DURATION_MS, player.getName().getString());
                }
            }
            
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
     * @param elapsedMs How many milliseconds have elapsed since the sound started (for syncing)
     */
    private static void playRailgunSoundToPlayer(ServerPlayerEntity player, double laserX, double laserZ, long elapsedMs) {
        // Use the railgun shoot sound from the registry
        SoundEvent shootSound = SoundsRegistry.RAILGUN_SHOOT;
        
        if (shootSound != null) {
            // Send packet to client with offset information for synchronized playback
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeIdentifier(SoundsRegistry.RAILGUN_SHOOT_ID);
            buf.writeDouble(laserX);
            buf.writeDouble(laserZ);
            buf.writeLong(elapsedMs);
            buf.writeFloat(1.0f); // volume
            buf.writeFloat(1.0f); // pitch
            
            ServerPlayNetworking.send(player, PLAY_SOUND_WITH_OFFSET_PACKET_ID, buf);
            
            if (ServerConfig.INSTANCE.isDebugMode()) {
                LOGGER.info("Sending synchronized sound packet to player {} at ({}, {}) with {}ms offset", 
                    player.getName().getString(), laserX, laserZ, elapsedMs);
            }
        } else {
            LOGGER.warn("Railgun shoot sound not found in registry");
        }
    }
    
    /**
     * Plays the railgun shoot sound to a specific player (legacy method without offset).
     */
    private static void playRailgunSoundToPlayer(ServerPlayerEntity player, double laserX, double laserZ) {
        playRailgunSoundToPlayer(player, laserX, laserZ, 0L);
    }
    
    /**
     * Sends a packet to the client to stop area-based sounds.
     */
    private static void stopAreaSoundsForPlayer(ServerPlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(SoundsRegistry.RAILGUN_SHOOT_ID);
        
        ServerPlayNetworking.send(player, STOP_AREA_SOUND_PACKET_ID, buf);
        
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("Sent stop sound packet to player {}", player.getName().getString());
        }
    }
}

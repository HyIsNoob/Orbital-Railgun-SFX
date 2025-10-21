package io.github.hyisnoob.railgunsounds.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.lwjgl.openal.AL10;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for synchronized sound playback with offset support.
 * Handles playing sounds at specific positions in their timeline for synchronization.
 * 
 * This implementation uses reflection and OpenAL to achieve audio seeking,
 * allowing sounds to start from a specific time offset rather than from the beginning.
 */
public class SynchronizedSoundManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("orbital_railgun_sounds");
    private static final Map<UUID, PositionedRailgunSoundInstance> activeSounds = new ConcurrentHashMap<>();
    
    /**
     * Plays a railgun sound at a specific position with an offset.
     * 
     * @param sound The sound event to play
     * @param category The sound category
     * @param x The X coordinate
     * @param y The Y coordinate (using player's Y if available)
     * @param z The Z coordinate
     * @param volume The volume
     * @param pitch The pitch
     * @param offsetMs The offset in milliseconds from the start of the sound
     */
    public static void playSoundWithOffset(SoundEvent sound, SoundCategory category,
                                          double x, double y, double z,
                                          float volume, float pitch, long offsetMs) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        
        // Use player's Y coordinate if y is 0
        if (y == 0) {
            y = client.player.getY();
        }
        
        // Create a custom sound instance with offset support
        PositionedRailgunSoundInstance soundInstance = new PositionedRailgunSoundInstance(
            sound, category, volume, pitch, x, y, z, offsetMs
        );
        
        // Store the sound instance for tracking
        UUID soundId = UUID.randomUUID();
        activeSounds.put(soundId, soundInstance);
        
        // Play the sound
        client.getSoundManager().play(soundInstance);
        soundInstance.markAsStarted();
        
        // Try to seek the audio if offset is provided
        if (offsetMs > 0) {
            // Schedule the seek operation slightly after the sound starts playing
            // This gives the sound system time to initialize the audio source
            new Thread(() -> {
                try {
                    Thread.sleep(50); // Wait 50ms for the sound to initialize
                    seekSound(soundInstance, offsetMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
            LOGGER.info("Playing synchronized sound at ({}, {}, {}) with {}ms offset", 
                x, y, z, offsetMs);
        } else {
            LOGGER.debug("Playing sound at ({}, {}, {}) from start", x, y, z);
        }
        
        // Schedule cleanup after sound duration
        scheduleCleanup(soundId, 53000 - offsetMs); // 53 seconds - offset
    }
    
    /**
     * Attempts to seek the audio to a specific offset using OpenAL.
     * This uses reflection to access Minecraft's internal sound system.
     * 
     * @param soundInstance The sound instance to seek
     * @param offsetMs The offset in milliseconds
     */
    private static void seekSound(PositionedRailgunSoundInstance soundInstance, long offsetMs) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }
            
            SoundManager soundManager = client.getSoundManager();
            
            // Try to access the sound system's internal source mapping
            // Note: This is implementation-specific and may not work across all Minecraft versions
            // The field names may vary between versions
            
            // Attempt to get the source field from SoundManager
            Field sourcesField = null;
            for (Field field : soundManager.getClass().getDeclaredFields()) {
                // Look for a Map field that might contain sound sources
                if (Map.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(soundManager);
                    if (fieldValue instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) fieldValue;
                        // Check if this map might contain our sound instance
                        if (map.containsKey(soundInstance)) {
                            sourcesField = field;
                            break;
                        }
                    }
                }
            }
            
            if (sourcesField != null) {
                Map<?, ?> sources = (Map<?, ?>) sourcesField.get(soundManager);
                Object source = sources.get(soundInstance);
                
                if (source != null) {
                    // Try to get the OpenAL source ID
                    Integer sourceId = null;
                    for (Method method : source.getClass().getMethods()) {
                        if (method.getName().contains("getSource") || method.getName().contains("getId")) {
                            method.setAccessible(true);
                            Object result = method.invoke(source);
                            if (result instanceof Integer) {
                                sourceId = (Integer) result;
                                break;
                            }
                        }
                    }
                    
                    if (sourceId != null && sourceId > 0) {
                        // Convert milliseconds to seconds for OpenAL
                        float offsetSeconds = offsetMs / 1000.0f;
                        
                        // Use OpenAL to set the playback position
                        AL10.alSourcef(sourceId, AL10.AL_SEC_OFFSET, offsetSeconds);
                        
                        LOGGER.info("Successfully seeked audio to {}s offset", offsetSeconds);
                    } else {
                        LOGGER.debug("Could not find OpenAL source ID for sound seeking");
                    }
                } else {
                    LOGGER.debug("Sound source not found in sound manager");
                }
            } else {
                LOGGER.debug("Could not access sound manager internals for seeking");
            }
        } catch (Exception e) {
            // If reflection fails, we gracefully degrade to playing from the beginning
            LOGGER.debug("Audio seeking not available (reflection failed): {}", e.getMessage());
            LOGGER.info("Sound will play from beginning instead of offset position");
        }
    }
    
    /**
     * Stops all active railgun sounds.
     */
    public static void stopAllSounds(SoundEvent sound, SoundCategory category) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }
        
        client.getSoundManager().stopSounds(sound.getId(), category);
        activeSounds.clear();
    }
    
    /**
     * Schedules cleanup of a sound instance after a delay.
     */
    private static void scheduleCleanup(UUID soundId, long delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(Math.max(0, delayMs));
                activeSounds.remove(soundId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}

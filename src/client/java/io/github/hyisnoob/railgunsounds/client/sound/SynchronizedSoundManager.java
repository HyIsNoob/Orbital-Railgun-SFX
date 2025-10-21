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
            // We try multiple times with increasing delays to ensure the source is ready
            new Thread(() -> {
                boolean seekSuccessful = false;
                int maxAttempts = 5;
                for (int attempt = 1; attempt <= maxAttempts && !seekSuccessful; attempt++) {
                    try {
                        Thread.sleep(attempt * 20); // Progressive delay: 20ms, 40ms, 60ms, 80ms, 100ms
                        seekSuccessful = seekSound(soundInstance, offsetMs);
                        if (seekSuccessful) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                if (!seekSuccessful) {
                    LOGGER.info("Could not seek audio after {} attempts, playing from start", maxAttempts);
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
     * @return true if seeking was successful, false otherwise
     */
    private static boolean seekSound(PositionedRailgunSoundInstance soundInstance, long offsetMs) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null) {
                return;
            }
            
            SoundManager soundManager = client.getSoundManager();
            
            // Access the sound system's internal structures using reflection
            // This is version-specific but works with Minecraft 1.20.1
            
            // Try to find the soundSystem/sources field in SoundManager
            Field soundSystemField = null;
            try {
                // Common field names in different versions
                String[] possibleFieldNames = {"soundSystem", "field_18952", "f_120397_"};
                for (String fieldName : possibleFieldNames) {
                    try {
                        soundSystemField = soundManager.getClass().getDeclaredField(fieldName);
                        soundSystemField.setAccessible(true);
                        break;
                    } catch (NoSuchFieldException ignored) {
                        // Try next field name
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Could not find soundSystem field: {}", e.getMessage());
            }
            
            if (soundSystemField != null) {
                Object soundSystem = soundSystemField.get(soundManager);
                
                if (soundSystem != null) {
                    // Try to find the sources map in the sound system
                    Field sourcesField = null;
                    try {
                        String[] possibleSourcesFieldNames = {"sources", "field_19241", "f_120407_"};
                        for (String fieldName : possibleSourcesFieldNames) {
                            try {
                                sourcesField = soundSystem.getClass().getDeclaredField(fieldName);
                                sourcesField.setAccessible(true);
                                break;
                            } catch (NoSuchFieldException ignored) {
                                // Try next field name
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.debug("Could not find sources field: {}", e.getMessage());
                    }
                    
                    if (sourcesField != null) {
                        Map<?, ?> sources = (Map<?, ?>) sourcesField.get(soundSystem);
                        
                        if (sources != null && sources.containsKey(soundInstance)) {
                            Object channelHandle = sources.get(soundInstance);
                            
                            // Try to get the OpenAL source from the channel
                            try {
                                Field sourceField = null;
                                String[] possibleSourceFieldNames = {"source", "field_19243", "f_120409_"};
                                for (String fieldName : possibleSourceFieldNames) {
                                    try {
                                        sourceField = channelHandle.getClass().getDeclaredField(fieldName);
                                        sourceField.setAccessible(true);
                                        break;
                                    } catch (NoSuchFieldException ignored) {
                                        // Try next field name
                                    }
                                }
                                
                                if (sourceField != null) {
                                    Object sourceObj = sourceField.get(channelHandle);
                                    
                                    // Get the integer source ID from the source object
                                    Method getSourceMethod = null;
                                    try {
                                        getSourceMethod = sourceObj.getClass().getMethod("getInt");
                                    } catch (NoSuchMethodException e) {
                                        // Try alternative method names
                                        for (Method method : sourceObj.getClass().getMethods()) {
                                            if (method.getReturnType() == int.class && method.getParameterCount() == 0) {
                                                getSourceMethod = method;
                                                break;
                                            }
                                        }
                                    }
                                    
                                    if (getSourceMethod != null) {
                                        int sourceId = (int) getSourceMethod.invoke(sourceObj);
                                        
                                        if (sourceId > 0) {
                                            // Convert milliseconds to seconds for OpenAL
                                            float offsetSeconds = offsetMs / 1000.0f;
                                            
                                            // Use OpenAL to set the playback position
                                            AL10.alSourcef(sourceId, AL10.AL_SEC_OFFSET, offsetSeconds);
                                            
                                            // Check for OpenAL errors
                                            int error = AL10.alGetError();
                                            if (error == AL10.AL_NO_ERROR) {
                                                LOGGER.info("Successfully seeked audio to {}s offset (source ID: {})", 
                                                    offsetSeconds, sourceId);
                                                return true;
                                            } else {
                                                LOGGER.warn("OpenAL error when seeking: {}", error);
                                                return false;
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.debug("Could not access channel source: {}", e.getMessage());
                            }
                        } else {
                            LOGGER.debug("Sound instance not yet in sources map");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // If reflection fails, we gracefully degrade to playing from the beginning
            LOGGER.debug("Audio seeking not available (reflection failed): {}", e.getMessage());
        }
        return false;
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

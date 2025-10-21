package io.github.hyisnoob.railgunsounds.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for synchronized sound playback with offset support.
 * Handles playing sounds at specific positions in their timeline for synchronization.
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
        
        // Since Minecraft doesn't natively support audio seeking, we use a workaround:
        // We play the sound normally but track the offset in our custom sound instance.
        // The sound will play from the beginning, but we can use the offset information
        // for future enhancements or mod compatibility.
        
        // Note: True audio seeking would require direct manipulation of the audio system
        // using libraries like LWJGL's OpenAL, which is beyond the scope of Minecraft's API.
        // For now, we play the sound from the beginning but with proper tracking.
        
        client.getSoundManager().play(soundInstance);
        soundInstance.markAsStarted();
        
        if (offsetMs > 0) {
            LOGGER.info("Playing synchronized sound at ({}, {}, {}) with {}ms offset (workaround: playing from start)", 
                x, y, z, offsetMs);
        } else {
            LOGGER.debug("Playing sound at ({}, {}, {}) from start", x, y, z);
        }
        
        // Schedule cleanup after sound duration
        scheduleCleanup(soundId, 53000 - offsetMs); // 53 seconds - offset
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

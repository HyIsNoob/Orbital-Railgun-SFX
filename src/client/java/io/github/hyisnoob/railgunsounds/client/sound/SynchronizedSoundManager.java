package io.github.hyisnoob.railgunsounds.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for synchronized sound playback with offset support.
 * 
 * Since Minecraft doesn't support true audio seeking, this implementation uses a simpler approach:
 * - Sounds play from the beginning but are configured to stop after the remaining duration
 * - The PositionedRailgunSoundInstance tracks the offset and auto-stops appropriately
 * - This provides synchronized behavior without complex reflection or OpenAL manipulation
 */
public class SynchronizedSoundManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("orbital_railgun_sounds");
    private static final Map<UUID, PositionedRailgunSoundInstance> activeSounds = new ConcurrentHashMap<>();
    
    /**
     * Plays a railgun sound at a specific position with an offset.
     * Note: Due to Minecraft limitations, the sound plays from the beginning but stops
     * after the appropriate remaining duration to simulate synchronization.
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
        // The instance will auto-stop after the remaining duration
        PositionedRailgunSoundInstance soundInstance = new PositionedRailgunSoundInstance(
            sound, category, volume, pitch, x, y, z, offsetMs
        );
        
        // Store the sound instance for tracking
        UUID soundId = UUID.randomUUID();
        activeSounds.put(soundId, soundInstance);
        
        // Play the sound - it will play from the beginning but stop after remaining duration
        client.getSoundManager().play(soundInstance);
        soundInstance.markAsStarted();
        
        LOGGER.info("Playing sound at ({}, {}, {}) - offset: {}ms, remaining: {}ms", 
            x, y, z, offsetMs, 52992 - offsetMs);
        
        // Schedule cleanup after sound duration
        scheduleCleanup(soundId, 52992 - offsetMs); // Total duration - offset
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

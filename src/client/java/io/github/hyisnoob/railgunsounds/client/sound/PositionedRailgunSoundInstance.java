package io.github.hyisnoob.railgunsounds.client.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

/**
 * A custom sound instance for the railgun shoot sound that supports starting playback
 * at a specific offset (elapsed time) for synchronization purposes.
 */
public class PositionedRailgunSoundInstance extends MovingSoundInstance {
    private final long startOffsetMs;
    private final long startTimeMs;
    private boolean hasStarted = false;
    
    /**
     * Creates a positioned railgun sound instance with an offset.
     * 
     * @param sound The sound event to play
     * @param category The sound category
     * @param volume The volume (0.0 to 1.0)
     * @param pitch The pitch multiplier
     * @param x The X coordinate of the sound source
     * @param y The Y coordinate of the sound source
     * @param z The Z coordinate of the sound source
     * @param startOffsetMs How many milliseconds into the sound to start playback
     */
    public PositionedRailgunSoundInstance(SoundEvent sound, SoundCategory category, 
                                         float volume, float pitch,
                                         double x, double y, double z,
                                         long startOffsetMs) {
        super(sound, category, Random.create());
        this.volume = volume;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.repeat = false;
        this.repeatDelay = 0;
        this.attenuationType = AttenuationType.LINEAR;
        this.startOffsetMs = startOffsetMs;
        this.startTimeMs = System.currentTimeMillis();
    }
    
    /**
     * Gets the offset in milliseconds from where the sound should start playing.
     * This is used for synchronization when players enter the sound range mid-playback.
     * 
     * @return The offset in milliseconds
     */
    public long getStartOffsetMs() {
        return startOffsetMs;
    }
    
    /**
     * Checks if this sound has started playing yet.
     * 
     * @return true if the sound has started, false otherwise
     */
    public boolean hasStarted() {
        return hasStarted;
    }
    
    /**
     * Marks this sound as started.
     */
    public void markAsStarted() {
        this.hasStarted = true;
    }
    
    @Override
    public void tick() {
        // The sound is stationary, so we don't need to update position
        // Just check if the sound should stop based on elapsed time
        long currentTimeMs = System.currentTimeMillis();
        long totalElapsedMs = startOffsetMs + (currentTimeMs - startTimeMs);
        
        // The railgun sound is approximately 53 seconds long
        if (totalElapsedMs >= 52992) {
            this.setDone();
        }
    }
    
    @Override
    public boolean shouldAlwaysPlay() {
        return true;
    }
}

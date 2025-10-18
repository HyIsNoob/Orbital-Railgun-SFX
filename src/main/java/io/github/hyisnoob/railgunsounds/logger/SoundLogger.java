package io.github.hyisnoob.railgunsounds.logger;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import net.minecraft.util.math.BlockPos;

public class SoundLogger {
    public static void logSoundEvent(String soundName, BlockPos location, double range) {
        if (isDebugModeEnabled()) {
            OrbitalRailgunSounds.LOGGER.info("Sound Event: " + soundName +
                    " | Location: " + location +
                    " | Range: " + range);
        }
    }

    private static boolean isDebugModeEnabled() {
        // Placeholder for actual debug mode check
        return true;
    }
}


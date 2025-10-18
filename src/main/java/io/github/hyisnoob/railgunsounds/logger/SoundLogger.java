package io.github.hyisnoob.railgunsounds.logger;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import io.github.hyisnoob.railgunsounds.registry.CommandRegistry;
import net.minecraft.util.math.BlockPos;

public class SoundLogger {
    public static void logSoundEvent(String soundName, BlockPos location, double range) {
        if (isDebugModeEnabled()) {
            OrbitalRailgunSounds.LOGGER.info("Sound Event: " + soundName +
                    " | Location: " + location +
                    " | Range: " + range);
        }
    }

    public static void logSoundEvent(String soundName, BlockPos location, double range, double distance, float volume) {
        if (isDebugModeEnabled()) {
            OrbitalRailgunSounds.LOGGER.info("Sound Event: " + soundName +
                    " | Location: " + location +
                    " | Range: " + range +
                    " | Distance: " + String.format("%.2f", distance) +
                    " | Attenuated Volume: " + String.format("%.2f", volume));
        }
    }

    private static boolean isDebugModeEnabled() {
        return CommandRegistry.isDebugMode();
    }
}


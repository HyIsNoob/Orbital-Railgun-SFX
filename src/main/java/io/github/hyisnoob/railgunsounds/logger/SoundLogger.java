package io.github.hyisnoob.railgunsounds.logger;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.minecraft.util.math.BlockPos;

public class SoundLogger {
    public static void logSoundEvent(String soundName, BlockPos location, double range) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            OrbitalRailgunSounds.LOGGER.info("Sound Event: " + soundName +
                    " | Location: " + location +
                    " | Range: " + range);
        }
    }
}



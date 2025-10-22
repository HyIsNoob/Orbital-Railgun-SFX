package io.github.hyisnoob.railgunsounds.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.minecraft.util.math.BlockPos;

public class SoundLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("orbital_railgun_sounds");

    public static void logSoundEvent(String soundName, BlockPos location, double range) {
        if (ServerConfig.INSTANCE.isDebugMode()) {
            LOGGER.info("Sound Event: {} | Location: {} | Range: {}", soundName, location, range);
        }
    }
}



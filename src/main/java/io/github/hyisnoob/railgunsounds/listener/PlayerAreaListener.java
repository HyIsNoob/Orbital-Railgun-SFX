package io.github.hyisnoob.railgunsounds.listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.hyisnoob.railgunsounds.config.ServerConfig;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAreaListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("orbital_railgun_sounds");
    private static final Map<UUID, AreaState> playerStates = new ConcurrentHashMap<>();

    /**
     * Tracks state for a player in relation to a laser impact area
     */
    private static class AreaState {
        boolean isInside;
        double lastLaserX;
        double lastLaserZ;

        AreaState(boolean isInside, double laserX, double laserZ) {
            this.isInside = isInside;
            this.lastLaserX = laserX;
            this.lastLaserZ = laserZ;
        }
    }

    /**
     * Checks if a player is within the sound range of a laser impact location.
     * Uses circular distance check (more accurate than square bounds).
     * 
     * @param player The player to check
     * @param laserX The X coordinate of the laser impact
     * @param laserZ The Z coordinate of the laser impact
     * @return true if the player is inside the range, false otherwise
     */
    public static boolean isPlayerInRange(ServerPlayerEntity player, double laserX, double laserZ) {
        double soundRange = ServerConfig.INSTANCE.getSoundRange();
        
        double playerX = player.getX();
        double playerZ = player.getZ();
        
        // Calculate squared distance (more efficient than using sqrt)
        double dx = playerX - laserX;
        double dz = playerZ - laserZ;
        double distanceSquared = dx * dx + dz * dz;
        double rangeSquared = soundRange * soundRange;
        
        return distanceSquared <= rangeSquared;
    }

    /**
     * Handles player area check and tracks state changes (entering/leaving area).
     * Returns information about whether the player's state changed.
     * 
     * @param player The player to check
     * @param laserX The X coordinate of the laser impact
     * @param laserZ The Z coordinate of the laser impact
     * @return An AreaCheckResult containing state information
     */
    public static AreaCheckResult handlePlayerAreaCheck(ServerPlayerEntity player, double laserX, double laserZ) {
        UUID playerId = player.getUuid();
        boolean currentlyInside = isPlayerInRange(player, laserX, laserZ);
        
        AreaState previousState = playerStates.get(playerId);
        boolean wasInside = previousState != null && previousState.isInside;
        
        // Check if this is a new laser location or first check for this player
        boolean isNewLocation = previousState == null || 
                                previousState.lastLaserX != laserX || 
                                previousState.lastLaserZ != laserZ;
        
        // Update the state
        playerStates.put(playerId, new AreaState(currentlyInside, laserX, laserZ));
        
        // Determine what action should be taken
        AreaCheckResult result = new AreaCheckResult();
        result.isInside = currentlyInside;
        result.wasInside = wasInside;
        result.isNewLocation = isNewLocation;
        
        // Log state changes if debug mode is enabled
        if (ServerConfig.INSTANCE.isDebugMode()) {
            if (isNewLocation) {
                LOGGER.info("New laser location: ({}, {}) for player {}", laserX, laserZ, player.getName().getString());
            }
            if (!wasInside && currentlyInside) {
                LOGGER.info("Player {} entered sound range at ({}, {})", 
                    player.getName().getString(), laserX, laserZ);
            } else if (wasInside && !currentlyInside) {
                LOGGER.info("Player {} left sound range at ({}, {})", 
                    player.getName().getString(), laserX, laserZ);
            }
        }
        
        return result;
    }

    /**
     * Clears the state for a specific player (useful when player disconnects)
     */
    public static void clearPlayerState(UUID playerId) {
        playerStates.remove(playerId);
    }

    /**
     * Clears all player states (useful for cleanup)
     */
    public static void clearAllStates() {
        playerStates.clear();
    }

    /**
     * Result of an area check, containing state information
     */
    public static class AreaCheckResult {
        public boolean isInside;
        public boolean wasInside;
        public boolean isNewLocation;
        
        /**
         * @return true if the player just entered the area
         */
        public boolean hasEntered() {
            return isInside && !wasInside;
        }
        
        /**
         * @return true if the player just left the area
         */
        public boolean hasLeft() {
            return !isInside && wasInside;
        }
        
        /**
         * @return true if the player's state changed
         */
        public boolean hasStateChanged() {
            return hasEntered() || hasLeft();
        }
    }
}

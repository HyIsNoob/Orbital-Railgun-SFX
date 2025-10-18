package io.github.hyisnoob.railgunsounds;

import java.util.logging.Logger;

import io.github.hyisnoob.railgunsounds.registry.OrbitalRailgunSoundsRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class OrbitalRailgunSounds implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun_sounds";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");

    @Override
    public void onInitialize() {
        OrbitalRailgunSoundsRegistry.initialize();
        LOGGER.info("Orbital Railgun Sounds Addon initialized. Sound events registered");

        ServerPlayNetworking.registerGlobalReceiver(PLAY_SOUND_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    SoundEvent sound = Registries.SOUND_EVENT.get(buf.readIdentifier());
                    BlockPos blockPos = buf.readBlockPos();
                    float volumeShoot = buf.readFloat();
                    float pitchShoot = buf.readFloat();

                    server.execute(() -> {
                        double range = 500.0;
                        double rangeSquared = range * range;

                        player.getWorld().getPlayers().forEach(nearbyPlayer -> {
                            double distanceSquared = nearbyPlayer.squaredDistanceTo(
                                    blockPos.getX() + 0.5,
                                    blockPos.getY() + 0.5,
                                    blockPos.getZ() + 0.5
                            );
                            if (distanceSquared <= rangeSquared) {
                                nearbyPlayer.playSound(
                                        sound,
                                        SoundCategory.PLAYERS,
                                        volumeShoot,
                                        pitchShoot
                                );
                            }
                        });
                    });
                });
    }
}

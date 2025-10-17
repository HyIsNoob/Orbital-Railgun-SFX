package io.github.hyisnoob.railgunsounds;

import java.util.logging.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.sound.SoundCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class OrbitalRailgunSounds implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun_sounds";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");

    public static final Identifier RAILGUN_SHOOT_ID = new Identifier(MOD_ID, "railgun_shoot");
    public static final Identifier SCOPE_ON_ID = new Identifier(MOD_ID, "scope_on");
    public static final Identifier EQUIP_ID = new Identifier(MOD_ID, "equip");

    public static final SoundEvent RAILGUN_SHOOT = registerSoundEvent(RAILGUN_SHOOT_ID);
    public static final SoundEvent SCOPE_ON = registerSoundEvent(SCOPE_ON_ID);
    public static final SoundEvent EQUIP = registerSoundEvent(EQUIP_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Orbital Railgun Sounds Addon initialized. Sound events registered");

        ServerPlayNetworking.registerGlobalReceiver(PLAY_SOUND_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    SoundEvent sound = Registries.SOUND_EVENT.get(buf.readIdentifier());
                    BlockPos blockPos = buf.readBlockPos();
                    float volume = buf.readFloat();
                    float pitch = buf.readFloat();

                    server.execute(() -> {
                        player.getWorld().playSound(
                                null,
                                blockPos,
                                sound,
                                SoundCategory.PLAYERS,
                                volume,
                                pitch);
                    });
                });
    }

    /**
     * Helper method to register a sound event.
     *
     * @param id The Identifier of the sound event.
     * @return The registered SoundEvent.
     */
    private static SoundEvent registerSoundEvent(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
}

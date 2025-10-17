package io.github.hyisnoob.railgunsounds.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class OrbitalRailgunSoundsRegistry {
    public static final String MOD_ID = "orbital_railgun_sounds";
    public static final Identifier PLAY_SOUND_PACKET_ID = new Identifier(MOD_ID, "play_sound");

    public static final Identifier RAILGUN_SHOOT_ID = new Identifier(MOD_ID, "railgun_shoot");
    public static final Identifier SCOPE_ON_ID = new Identifier(MOD_ID, "scope_on");
    public static final Identifier EQUIP_ID = new Identifier(MOD_ID, "equip");

    public static final SoundEvent RAILGUN_SHOOT = registerSoundEvent(RAILGUN_SHOOT_ID);
    public static final SoundEvent SCOPE_ON = registerSoundEvent(SCOPE_ON_ID);
    public static final SoundEvent EQUIP = registerSoundEvent(EQUIP_ID);

    public static void initialize() {
        // This method is intentionally left blank.
        // Its purpose is to ensure the class is loaded and static initializers are run.
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
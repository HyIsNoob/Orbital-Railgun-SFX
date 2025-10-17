package io.github.hyisnoob.railgunsounds;

import java.util.logging.Logger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class OrbitalRailgunSounds implements ModInitializer {
    public static final String MOD_ID = "orbital_railgun_sounds";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static final Identifier RAILGUN_SHOOT_ID = new Identifier(MOD_ID, "railgun_shoot");
    public static final Identifier SCOPE_ON_ID = new Identifier(MOD_ID, "scope_on");
    public static final Identifier EQUIP_ID = new Identifier(MOD_ID, "equip");

    public static SoundEvent RAILGUN_SHOOT;
    public static SoundEvent SCOPE_ON;
    public static SoundEvent EQUIP;

    @Override
    public void onInitialize() {
    RAILGUN_SHOOT = Registry.register(Registries.SOUND_EVENT, RAILGUN_SHOOT_ID, SoundEvent.of(RAILGUN_SHOOT_ID));
    SCOPE_ON = Registry.register(Registries.SOUND_EVENT, SCOPE_ON_ID, SoundEvent.of(SCOPE_ON_ID));
    EQUIP = Registry.register(Registries.SOUND_EVENT, EQUIP_ID, SoundEvent.of(EQUIP_ID));

        LOGGER.info("Registered sound events for Orbital Railgun addon");
    }
}

package io.github.hyisnoob.railgunsounds.client;

import io.github.hyisnoob.railgunsounds.client.sounds.OrbitalRailgunSoundsSounds;
import net.fabricmc.api.ClientModInitializer;

public class OrbitalRailgunSoundsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OrbitalRailgunSoundsSounds sounds = new OrbitalRailgunSoundsSounds();
        sounds.initializeClient();
    }
}

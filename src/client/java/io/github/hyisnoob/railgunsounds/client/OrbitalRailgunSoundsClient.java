package io.github.hyisnoob.railgunsounds.client;

import io.github.hyisnoob.railgunsounds.client.config.OrbitalRailgunSoundsConfigWrapper;
import io.github.hyisnoob.railgunsounds.client.sounds.OrbitalRailgunSoundsSounds;
import net.fabricmc.api.ClientModInitializer;

public class OrbitalRailgunSoundsClient implements ClientModInitializer {
    public static OrbitalRailgunSoundsConfigWrapper CONFIG;
    
    @Override
    public void onInitializeClient() {
        CONFIG = OrbitalRailgunSoundsConfigWrapper.createAndLoad();
        
        OrbitalRailgunSoundsSounds sounds = new OrbitalRailgunSoundsSounds();
        sounds.initializeClient();
    }
}

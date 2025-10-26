package io.github.hyisnoob.railgunsounds.client;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import io.github.hyisnoob.railgunsounds.client.config.SoundsConfigWrapper;
import io.github.hyisnoob.railgunsounds.client.handler.SoundsHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public class OrbitalRailgunSoundsClient implements ClientModInitializer {
    public static SoundsConfigWrapper CONFIG;
    
    @Override
    public void onInitializeClient() {
        CONFIG = SoundsConfigWrapper.createAndLoad();
        
        SoundsHandler sounds = new SoundsHandler();
        sounds.initializeClient();
        
        // Register packet handler to stop area sounds when player leaves the zone
        ClientPlayNetworking.registerGlobalReceiver(OrbitalRailgunSounds.STOP_AREA_SOUND_PACKET_ID,
            (client, handler, buf, responseSender) -> {
                Identifier soundId = buf.readIdentifier();
                
                client.execute(() -> {
                    // Stop all instances of this sound for the player
                    MinecraftClient.getInstance().getSoundManager().stopSounds(soundId, SoundCategory.PLAYERS);
                });
            });
    }
}

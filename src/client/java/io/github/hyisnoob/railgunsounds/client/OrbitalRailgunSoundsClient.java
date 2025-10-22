package io.github.hyisnoob.railgunsounds.client;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import io.github.hyisnoob.railgunsounds.client.config.OrbitalRailgunSoundsConfigWrapper;
import io.github.hyisnoob.railgunsounds.client.sound.SynchronizedSoundManager;
import io.github.hyisnoob.railgunsounds.client.sounds.OrbitalRailgunSoundsSounds;
import io.github.hyisnoob.railgunsounds.registry.SoundsRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class OrbitalRailgunSoundsClient implements ClientModInitializer {
    public static OrbitalRailgunSoundsConfigWrapper CONFIG;
    
    @Override
    public void onInitializeClient() {
        CONFIG = OrbitalRailgunSoundsConfigWrapper.createAndLoad();
        
        OrbitalRailgunSoundsSounds sounds = new OrbitalRailgunSoundsSounds();
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
        
        // Register packet handler for synchronized sound playback with offset
        ClientPlayNetworking.registerGlobalReceiver(OrbitalRailgunSounds.PLAY_SOUND_WITH_OFFSET_PACKET_ID,
            (client, handler, buf, responseSender) -> {
                Identifier soundId = buf.readIdentifier();
                double x = buf.readDouble();
                double z = buf.readDouble();
                long offsetMs = buf.readLong();
                float volume = buf.readFloat();
                float pitch = buf.readFloat();
                
                // Debug logging - use System.out to ensure it appears
                System.out.println("[CLIENT] Received PLAY_SOUND_WITH_OFFSET packet: sound=" + soundId + ", offset=" + offsetMs + "ms, pos=(" + x + ", " + z + ")");
                
                client.execute(() -> {
                    System.out.println("[CLIENT] Executing sound playback on client thread");
                    SoundEvent sound = Registries.SOUND_EVENT.get(soundId);
                    if (sound != null) {
                        System.out.println("[CLIENT] Sound event found: " + sound.getId());
                        // Use player's Y coordinate for sound positioning
                        double y = client.player != null ? client.player.getY() : 64.0;
                        
                        // Play the sound with offset support
                        SynchronizedSoundManager.playSoundWithOffset(
                            sound, SoundCategory.PLAYERS,
                            x, y, z,
                            volume, pitch, offsetMs
                        );
                    } else {
                        System.out.println("[CLIENT] ERROR: Sound event not found for ID: " + soundId);
                    }
                });
            });
    }
}

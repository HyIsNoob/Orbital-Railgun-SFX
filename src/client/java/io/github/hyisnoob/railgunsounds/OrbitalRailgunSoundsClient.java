package io.github.hyisnoob.railgunsounds;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class OrbitalRailgunSoundsClient implements ClientModInitializer {
    private static final Identifier ORBITAL_RAILGUN_ITEM_ID = new Identifier("orbital_railgun", "orbital_railgun");
    private static final float DEFAULT_VOLUME = 1.0f;
    private static final float DEFAULT_PITCH = 1.0f;

    private boolean wasUsing = false;
    private int lastSelectedSlot = -1;
    private boolean lastCooldownActive = false;
    private Item railgunItem;

    private PositionedSoundInstance scopeSoundInstance;

    @Override
    public void onInitializeClient() {
        railgunItem = Registries.ITEM.get(ORBITAL_RAILGUN_ITEM_ID);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null)
            return;

        boolean focused = client.isWindowFocused();
        float volume = focused ? DEFAULT_VOLUME : 0.0f;

        handleRailgunUsage(client, player, volume);
        handleRailgunCooldown(player, volume);
        handleHotbarSwitch(player, volume);
    }

    private void handleRailgunUsage(MinecraftClient client, ClientPlayerEntity player, float volume) {
        Item currentItem = player.getActiveItem().getItem();
        boolean isUsingRailgun = !player.getActiveItem().isEmpty() && currentItem == railgunItem;

        if (isUsingRailgun) {
            if (!wasUsing) {
                scopeSoundInstance = PositionedSoundInstance.master(OrbitalRailgunSounds.SCOPE_ON, volume,
                        DEFAULT_PITCH);
                client.getSoundManager().play(scopeSoundInstance);
            }
        } else {
            if (wasUsing && scopeSoundInstance != null) {
                client.getSoundManager().stop(scopeSoundInstance);
                scopeSoundInstance = null;
            }
        }

        wasUsing = isUsingRailgun;
    }

    private void handleRailgunCooldown(ClientPlayerEntity player, float volume) {
        if (railgunItem != null) {
            boolean cooldownNow = player.getItemCooldownManager().isCoolingDown(railgunItem);

            if (!lastCooldownActive && cooldownNow) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeIdentifier(Registries.SOUND_EVENT.getId(OrbitalRailgunSounds.RAILGUN_SHOOT));
                buf.writeBlockPos(player.getBlockPos());
                buf.writeFloat(volume);
                buf.writeFloat(DEFAULT_PITCH);

                ClientPlayNetworking.send(OrbitalRailgunSounds.PLAY_SOUND_PACKET_ID, buf);

                System.out.println("Sent railgun shoot sound packet to server at " + player.getBlockPos());
            }

            lastCooldownActive = cooldownNow;
        }
    }

    private void handleHotbarSwitch(ClientPlayerEntity player, float volume) {
        int selected = player.getInventory().selectedSlot;
        if (lastSelectedSlot != selected) {
            Item heldItem = player.getMainHandStack().getItem();

            if (heldItem == railgunItem) {
                player.playSound(OrbitalRailgunSounds.EQUIP, volume, DEFAULT_PITCH);
            }

            lastSelectedSlot = selected;
        }
    }
}

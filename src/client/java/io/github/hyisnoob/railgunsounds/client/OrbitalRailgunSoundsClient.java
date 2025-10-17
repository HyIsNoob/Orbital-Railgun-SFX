package io.github.hyisnoob.railgunsounds.client;

import io.github.hyisnoob.railgunsounds.OrbitalRailgunSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
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

    @Override
    public void onInitializeClient() {
        // Cache the railgun item instance
        railgunItem = Registries.ITEM.get(ORBITAL_RAILGUN_ITEM_ID);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;

        // Check if the game window is focused and adjust volume
        boolean focused = client.isWindowFocused();
        float volume = focused ? DEFAULT_VOLUME : 0.0f;

        // Play sounds based on player actions
        handleRailgunUsage(player, volume);
        handleRailgunCooldown(player, volume);
        handleHotbarSwitch(player, volume);
    }

    private void handleRailgunUsage(ClientPlayerEntity player, float volume) {
        // Check if the player is using the railgun
        Item currentItem = player.getActiveItem().getItem();
        boolean isUsingRailgun = !player.getActiveItem().isEmpty() && currentItem == railgunItem;

        // Play "scope_on" sound when starting to use the railgun
        if (isUsingRailgun && !wasUsing) {
            player.playSound(OrbitalRailgunSounds.SCOPE_ON, volume, DEFAULT_PITCH);
        }

        // Update usage state
        wasUsing = isUsingRailgun;
    }

    private void handleRailgunCooldown(ClientPlayerEntity player, float volume) {
        // Check if the railgun is on cooldown
        if (railgunItem != null) {
            boolean cooldownNow = player.getItemCooldownManager().isCoolingDown(railgunItem);

            // Play "railgun_shoot" sound when cooldown starts
            if (!lastCooldownActive && cooldownNow) {
                player.playSound(OrbitalRailgunSounds.RAILGUN_SHOOT, volume, DEFAULT_PITCH);
            }

            // Update cooldown state
            lastCooldownActive = cooldownNow;
        }
    }

    private void handleHotbarSwitch(ClientPlayerEntity player, float volume) {
        // Check if the player switched hotbar slots
        int selected = player.getInventory().selectedSlot;
        if (lastSelectedSlot != selected) {
            Item heldItem = player.getMainHandStack().getItem();

            // Play "equip" sound when switching to the railgun
            if (heldItem == railgunItem) {
                player.playSound(OrbitalRailgunSounds.EQUIP, volume, DEFAULT_PITCH);
            }

            // Update selected slot
            lastSelectedSlot = selected;
        }
    }
}

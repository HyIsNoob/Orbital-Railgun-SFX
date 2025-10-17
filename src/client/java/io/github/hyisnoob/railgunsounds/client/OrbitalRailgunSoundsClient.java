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
    private boolean wasUsing = false;
    private int lastSelectedSlot = -1;
    private boolean lastCooldownActive = false;
    private Item railgunItem;

    @Override
    public void onInitializeClient() {
        // cache the railgun item instance
        railgunItem = Registries.ITEM.get(ORBITAL_RAILGUN_ITEM_ID);
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
    }

    private void onEndTick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) return;
        if (client.world == null) return;
    boolean focused = client.isWindowFocused();
    float vol = focused ? 1.0f : 0.0f;

        Item currentItem = player.getActiveItem().getItem();
        boolean isUsingRailgun = !player.getActiveItem().isEmpty() && Registries.ITEM.getId(currentItem).equals(ORBITAL_RAILGUN_ITEM_ID);

        // Play scope_on when starting to use the railgun
        if (isUsingRailgun && !wasUsing) {
            // Keep playback timeline even when unfocused by using volume 0
            player.playSound(OrbitalRailgunSounds.SCOPE_ON, vol, 1.0f);
        }

        // Play shoot sound only when the railgun actually fires: detect cooldown start
        if (railgunItem != null) {
            boolean cooldownNow = player.getItemCooldownManager().isCoolingDown(railgunItem);
            if (!lastCooldownActive && cooldownNow) {
                // Keep playback timeline even when unfocused by using volume 0
                player.playSound(OrbitalRailgunSounds.RAILGUN_SHOOT, vol, 1.0f);
            }
            lastCooldownActive = cooldownNow;
        }

        // Update using state after processing
        wasUsing = isUsingRailgun;

        // Play equip sound when switching to the Orbital Railgun in hotbar
        int selected = player.getInventory().selectedSlot;
        if (lastSelectedSlot != selected) {
            Item held = player.getMainHandStack().getItem();
            if (Registries.ITEM.getId(held).equals(ORBITAL_RAILGUN_ITEM_ID)) {
                // Keep playback timeline even when unfocused by using volume 0
                player.playSound(OrbitalRailgunSounds.EQUIP, vol, 1.0f);
            }
            lastSelectedSlot = selected;
        }
    }
}

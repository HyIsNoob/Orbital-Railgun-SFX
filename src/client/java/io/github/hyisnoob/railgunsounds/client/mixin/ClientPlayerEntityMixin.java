package io.github.hyisnoob.railgunsounds.client.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Unique
    private static volatile boolean warningShown = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!warningShown) {
            warningShown = true;
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            
            MutableText modLink = Text.literal("Orbital Railgun Enhanced")
                .formatted(Formatting.GREEN, Formatting.BOLD, Formatting.UNDERLINE)
                .styled(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/orbital-railgun-enhanced"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open Modrinth page"))));
            
            player.sendMessage(
                Text.literal("[Orbital Railgun SFX] ")
                    .formatted(Formatting.GOLD)
                    .append(Text.literal("This mod is outdated! Please use ")
                        .formatted(Formatting.YELLOW))
                    .append(modLink)
                    .append(Text.literal(" instead.")
                        .formatted(Formatting.YELLOW)),
                false
            );
        }
    }
}

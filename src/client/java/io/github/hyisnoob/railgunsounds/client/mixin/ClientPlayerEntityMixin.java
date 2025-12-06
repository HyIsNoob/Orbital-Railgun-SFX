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
            
            MutableText modLink = Text.translatable("text.orbital_railgun_sounds.warning.mod_name")
                .formatted(Formatting.GREEN, Formatting.BOLD, Formatting.UNDERLINE)
                .styled(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/mod/orbital-railgun-enhanced"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("text.orbital_railgun_sounds.warning.hover"))));
            
            player.sendMessage(
                Text.translatable("text.orbital_railgun_sounds.warning.prefix")
                    .formatted(Formatting.GOLD)
                    .append(Text.translatable("text.orbital_railgun_sounds.warning.message")
                        .formatted(Formatting.YELLOW))
                    .append(modLink)
                    .append(Text.translatable("text.orbital_railgun_sounds.warning.suffix")
                        .formatted(Formatting.YELLOW)),
                false
            );
        }
    }
}

package io.github.hyisnoob.railgunsounds.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void renderWarning(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        TitleScreen screen = (TitleScreen) (Object) this;

        Text warningLine1 = Text.translatable("text.orbital_railgun_sounds.warning.title");
        Text warningLine2 = Text.translatable("text.orbital_railgun_sounds.warning.subtitle");

        int screenWidth = screen.width;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int textWidth1 = tr.getWidth(warningLine1);
        int textWidth2 = tr.getWidth(warningLine2);

        int x1 = (screenWidth - textWidth1) / 2;
        int x2 = (screenWidth - textWidth2) / 2;
        int y = 4;

        context.drawText(tr, warningLine1, x1, y, 0xFFFFFF, true);
        context.drawText(tr, warningLine2, x2, y + 12, 0xFFFFFF, true);
    }
}

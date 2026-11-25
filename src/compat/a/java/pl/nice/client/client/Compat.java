package pl.nice.client.client;

import net.minecraft.client.gui.GuiGraphics;

/** Minecraft 1.21 - 1.21.5: PoseStack. */
public final class Compat {
    private Compat() {}

    public static void push(GuiGraphics g) {
        g.pose().pushPose();
    }

    public static void pop(GuiGraphics g) {
        g.pose().popPose();
    }

    public static void translate(GuiGraphics g, float x, float y) {
        g.pose().translate(x, y, 0f);
    }

    public static void scale(GuiGraphics g, float s) {
        g.pose().scale(s, s, 1f);
    }
}

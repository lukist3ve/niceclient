package pl.nice.client.client;

import net.minecraft.client.gui.GuiGraphics;

/** Minecraft 1.21.6 - 1.21.8: Matrix3x2fStack. */
public final class Compat {
    private Compat() {}

    public static void push(GuiGraphics g) {
        g.pose().pushMatrix();
    }

    public static void pop(GuiGraphics g) {
        g.pose().popMatrix();
    }

    public static void translate(GuiGraphics g, float x, float y) {
        g.pose().translate(x, y);
    }

    public static void scale(GuiGraphics g, float s) {
        g.pose().scale(s, s);
    }
}

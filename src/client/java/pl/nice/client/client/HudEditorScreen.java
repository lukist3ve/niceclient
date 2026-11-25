package pl.nice.client.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Edytor HUD: przeciaganie myszka, scroll = rozmiar, PPM = reset, czipy na dole = pokaz/ukryj. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private Hud.Widget drag;
    private int offX;
    private int offY;

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Nice Client - HUD"));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x66000000);
    }

    private Hud.Widget at(double mx, double my) {
        for (int i = Hud.WIDGETS.size() - 1; i >= 0; i--) {
            Hud.Widget w = Hud.WIDGETS.get(i);
            Module m = w.mod();
            if (m == null || !m.enabled()) continue;
            int[] p = Hud.place(w, width, height);
            if (Ui.in(mx, my, p[0], p[1], p[2], p[3])) return w;
        }
        return null;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        Ui.alpha = 1f;
        renderBackground(g, mx, my, pt);
        Hud.Widget hover = drag != null ? drag : at(mx, my);
        for (Hud.Widget w : Hud.WIDGETS) {
            Module m = w.mod();
            if (m == null || !m.enabled()) continue;
            Hud.drawWidget(g, w, minecraft, true);
            int[] p = Hud.place(w, width, height);
            int c = w == hover ? Ui.ACCENT : 0x66FFFFFF;
            g.renderOutline(p[0] - 2, p[1] - 2, p[2] + 4, p[3] + 4, c);
        }
        String hint = "Przeciagnij, aby przesunac  ·  Scroll - rozmiar  ·  PPM - reset";
        int hw = font.width(hint) + 24;
        Ui.card(g, width / 2 - hw / 2, 8, hw, 22, 8, 0xF0101114, Ui.BORDER);
        Ui.center(g, font, hint, width / 2, 15, Ui.TEXT);

        // czipy: pokaz / ukryj kazdy element
        int total = 0;
        for (Hud.Widget w : Hud.WIDGETS) total += font.width(w.mod().name) + 18;
        int x = width / 2 - total / 2;
        int y = height - 34;
        for (Hud.Widget w : Hud.WIDGETS) {
            Module m = w.mod();
            int cw = font.width(m.name) + 14;
            boolean on = m.enabled();
            Ui.card(g, x, y, cw, 20, 8, on ? Ui.ACCENT_DARK : Ui.CARD, on ? Ui.ACCENT : Ui.BORDER);
            Ui.center(g, font, m.name, x + cw / 2, y + 6, on ? Ui.ACCENT_TXT : Ui.MUTED);
            x += cw + 4;
        }
        String back = "Gotowe";
        int bw = font.width(back) + 24;
        Ui.card(g, width - bw - 10, height - 34, bw, 20, 8, Ui.ACCENT, Ui.ACCENT);
        Ui.center(g, font, back, width - bw / 2 - 10, height - 28, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // czipy i przycisk Gotowe
        int total = 0;
        for (Hud.Widget w : Hud.WIDGETS) total += font.width(w.mod().name) + 18;
        int x = width / 2 - total / 2;
        int y = height - 34;
        for (Hud.Widget w : Hud.WIDGETS) {
            int cw = font.width(w.mod().name) + 14;
            if (Ui.in(mx, my, x, y, cw, 20) && button == 0) {
                w.mod().toggle();
                Cfg.save();
                return true;
            }
            x += cw + 4;
        }
        int bw = font.width("Gotowe") + 24;
        if (Ui.in(mx, my, width - bw - 10, height - 34, bw, 20)) {
            onClose();
            return true;
        }
        Hud.Widget w = at(mx, my);
        if (w == null) return false;
        if (button == 1) {
            Hud.resetPos(w.id);
            Cfg.save();
            return true;
        }
        if (button == 0) {
            int[] p = Hud.place(w, width, height);
            drag = w;
            offX = (int) mx - p[0];
            offY = (int) my - p[1];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drag != null && button == 0) {
            Cfg.Pos p = Cfg.pos(drag.id);
            p.custom = true;
            p.x = (int) mx - offX;
            p.y = (int) my - offY;
            int[] pl = Hud.place(drag, width, height);
            p.x = pl[0];
            p.y = pl[1];
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (drag != null) {
            drag = null;
            Cfg.save();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        Hud.Widget w = at(mx, my);
        if (w == null) return false;
        Cfg.Pos p = Cfg.pos(w.id);
        int[] before = Hud.place(w, width, height);
        if (!p.custom) {
            p.custom = true;
            p.x = before[0];
            p.y = before[1];
        }
        p.scale = Math.max(0.5f, Math.min(3f, p.scale + (sy > 0 ? 0.1f : -0.1f)));
        Cfg.save();
        return true;
    }

    @Override
    public void onClose() {
        Cfg.save();
        minecraft.setScreen(parent);
    }
}

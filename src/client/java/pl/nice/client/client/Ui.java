package pl.nice.client.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Pomocnicze rysowanie: zaokraglone prostokaty, kolory, globalna przezroczystosc (animacje). */
public final class Ui {
    public static final int BG = 0xF2101114;
    public static final int PANEL = 0xFF14151A;
    public static final int CARD = 0xFF1A1C22;
    public static final int BORDER = 0xFF262932;
    public static final int ACCENT = 0xFF3B82F6;
    public static final int ACCENT_DARK = 0xFF1E3A8A;
    public static final int ACCENT_TXT = 0xFF93C5FD;
    public static final int TEXT = 0xFFF2F4F8;
    public static final int MUTED = 0xFF8A90A0;
    public static final int OFF = 0xFF272A33;
    public static final int DANGER = 0xFFEF4444;
    public static float alpha = 1f;

    private Ui() {}

    public static int col(int c) {
        int a = (int) (((c >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, alpha)));
        return (a << 24) | (c & 0xFFFFFF);
    }

    public static void rr(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        int c = col(color);
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        for (int i = 0; i < r; i++) {
            double d = r - i - 0.5;
            int inset = r - (int) Math.round(Math.sqrt(r * (double) r - d * d));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, c);
        }
        g.fill(x, y + r, x + w, y + h - r, c);
    }

    /** Karta z ramka: zewnetrzny rr w kolorze ramki, wewnetrzny w kolorze wypelnienia. */
    public static void card(GuiGraphics g, int x, int y, int w, int h, int r, int fill, int border) {
        rr(g, x, y, w, h, r, border);
        rr(g, x + 1, y + 1, w - 2, h - 2, Math.max(0, r - 1), fill);
    }

    public static void text(GuiGraphics g, Font f, String s, int x, int y, int color) {
        g.drawString(f, s, x, y, col(color), false);
    }

    public static void textShadow(GuiGraphics g, Font f, String s, int x, int y, int color) {
        g.drawString(f, s, x, y, col(color), true);
    }

    public static void center(GuiGraphics g, Font f, String s, int cx, int y, int color) {
        g.drawString(f, s, cx - f.width(s) / 2, y, col(color), false);
    }

    /** Serce (ulubione) narysowane pikselami. */
    public static void heart(GuiGraphics g, int x, int y, boolean on, int color) {
        int c = col(on ? 0xFFEF4466 : color);
        String[] m = {".XX.XX.", "XXXXXXX", "XXXXXXX", ".XXXXX.", "..XXX..", "...X..."};
        for (int j = 0; j < m.length; j++) for (int i = 0; i < 7; i++) if (m[j].charAt(i) == 'X') g.fill(x + i, y + j, x + i + 1, y + j + 1, c);
    }

    /** Kolko zebate (ustawienia) z pikseli. */
    public static void gear(GuiGraphics g, int x, int y, int color) {
        int c = col(color);
        g.fill(x + 3, y, x + 6, y + 9, c);
        g.fill(x, y + 3, x + 9, y + 6, c);
        g.fill(x + 1, y + 1, x + 8, y + 8, c);
        g.fill(x + 3, y + 3, x + 6, y + 6, col(CARD));
    }

    public static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}

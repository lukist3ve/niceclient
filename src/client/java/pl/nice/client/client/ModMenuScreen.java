package pl.nice.client.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Glowne menu Nice Client: Mod Menu (karty), Ustawienia, Profile + przycisk edytora HUD. */
public class ModMenuScreen extends Screen {
    private static final class Hit {
        final int x, y, w, h;
        final Runnable left;
        final Runnable right;
        int cy1 = -1, cy2 = -1;

        Hit(int x, int y, int w, int h, Runnable left, Runnable right) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.left = left;
            this.right = right;
        }
    }

    private final List<Hit> hits = new ArrayList<>();
    private final long openedAt = System.currentTimeMillis();
    private int tab;
    private int cat;
    private String q = "";
    private boolean searchFocus;
    private boolean favOnly;
    private int scroll;
    private int maxScroll;
    private Module open;
    private String msg = "";
    private long msgUntil;

    public ModMenuScreen() {
        super(Component.literal("Nice Client"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        if (Cfg.d.blur) super.renderBackground(g, mx, my, pt);
        else g.fill(0, 0, width, height, 0xB0000000);
    }

    private void say(String s) {
        msg = s;
        msgUntil = System.currentTimeMillis() + 2500;
    }

    private void hit(int x, int y, int w, int h, Runnable l) {
        hits.add(new Hit(x, y, w, h, l, null));
    }

    private Hit hitClip(int x, int y, int w, int h, Runnable l, Runnable r, int cy1, int cy2) {
        Hit hh = new Hit(x, y, w, h, l, r);
        hh.cy1 = cy1;
        hh.cy2 = cy2;
        hits.add(hh);
        return hh;
    }

    private void button(GuiGraphics g, int mx, int my, int x, int y, int w, int h, String label, boolean active, Runnable r) {
        boolean hov = Ui.in(mx, my, x, y, w, h);
        int fill = active ? Ui.ACCENT : hov ? 0xFF22252D : Ui.CARD;
        Ui.card(g, x, y, w, h, 6, fill, active ? Ui.ACCENT : Ui.BORDER);
        Ui.center(g, font, label, x + w / 2, y + (h - 8) / 2, active ? 0xFFFFFFFF : Ui.TEXT);
        hit(x, y, w, h, r);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        hits.clear();
        float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / 220f);
        float e = 1f - (1f - t) * (1f - t) * (1f - t);
        Ui.alpha = e;
        renderBackground(g, mx, my, pt);
        try {
            Compat.push(g);
            Compat.translate(g, 0f, (1f - e) * 10f);
            draw(g, mx, my);
            Compat.pop(g);
        } finally {
            Ui.alpha = 1f;
        }
    }

    private void draw(GuiGraphics g, int mx, int my) {
        int pw = Math.min(width - 16, 600);
        int ph = Math.min(height - 16, 340);
        int px = (width - pw) / 2;
        int py = (height - ph) / 2;

        // naglowek: logo + nazwa + gracz + HUD + zamknij
        Ui.rr(g, px, py, 24, 24, 7, Ui.ACCENT);
        Compat.push(g);
        Compat.translate(g, px + 7, py + 5);
        Compat.scale(g, 1.6f);
        Ui.text(g, font, "N", 0, 0, 0xFFFFFFFF);
        Compat.pop(g);
        Compat.push(g);
        Compat.translate(g, px + 32, py + 4);
        Compat.scale(g, 1.3f);
        Ui.text(g, font, "NICE CLIENT", 0, 0, Ui.TEXT);
        Compat.pop(g);
        Ui.text(g, font, "HollyMC.PL", px + 32, py + 16, Ui.MUTED);

        int rx = px + pw;
        rx -= 24;
        button(g, mx, my, rx, py, 24, 24, "X", false, this::onClose);
        String nick = minecraft.getUser().getName();
        int cw = font.width(nick) + 16;
        rx -= cw + 6;
        Ui.card(g, rx, py, cw, 24, 7, Ui.CARD, Ui.BORDER);
        Ui.center(g, font, nick, rx + cw / 2, py + 8, Ui.TEXT);
        String hud = "Edytuj HUD";
        int hw = font.width(hud) + 18;
        rx -= hw + 6;
        button(g, mx, my, rx, py, hw, 24, hud, false, () -> minecraft.setScreen(new HudEditorScreen(this)));

        // zakladki
        String[] tabs = {"MOD MENU", "USTAWIENIA", "PROFILE"};
        int tx = px;
        for (int i = 0; i < tabs.length; i++) {
            int w = font.width(tabs[i]) + 24;
            final int ti = i;
            button(g, mx, my, tx, py + 32, w, 22, tabs[i], tab == i, () -> {
                tab = ti;
                open = null;
                scroll = 0;
            });
            tx += w + 6;
        }

        int cx = px;
        int cy = py + 60;
        int cwid = pw;
        int ch = ph - 60;
        Ui.card(g, cx, cy, cwid, ch, 10, Ui.PANEL, Ui.BORDER);

        if (tab == 0) {
            if (open != null) drawDetail(g, mx, my, cx, cy, cwid, ch);
            else drawMods(g, mx, my, cx, cy, cwid, ch);
        } else if (tab == 1) drawSettings(g, mx, my, cx, cy, cwid, ch);
        else drawProfiles(g, mx, my, cx, cy, cwid, ch);

        if (System.currentTimeMillis() < msgUntil) {
            int mw = font.width(msg) + 20;
            Ui.card(g, width / 2 - mw / 2, py + ph + 4 > height - 24 ? height - 24 : py + ph + 4, mw, 18, 7, 0xF0101114, Ui.ACCENT);
            Ui.center(g, font, msg, width / 2, (py + ph + 4 > height - 24 ? height - 24 : py + ph + 4) + 5, Ui.TEXT);
        }
    }

    // ------------------------------------------------------------ MOD MENU
    private List<Module> filtered() {
        List<Module> out = new ArrayList<>();
        for (Module m : Modules.LIST) {
            if (cat != 0 && !m.cat.equals(Modules.CATS[cat])) continue;
            if (favOnly && !m.fav()) continue;
            if (!q.isEmpty() && !(m.name + " " + m.desc).toLowerCase().contains(q.toLowerCase())) continue;
            out.add(m);
        }
        return out;
    }

    private void drawMods(GuiGraphics g, int mx, int my, int cx, int cy, int cw, int ch) {
        // kategorie + wyszukiwarka + ulubione
        int x = cx + 10;
        int y = cy + 10;
        for (int i = 0; i < Modules.CATS.length; i++) {
            String c = Modules.CATS[i];
            int w = font.width(c) + 14;
            final int ci = i;
            button(g, mx, my, x, y, w, 18, c, cat == i, () -> {
                cat = ci;
                scroll = 0;
            });
            x += w + 4;
        }
        int sx = cx + cw - 10 - 18;
        Ui.card(g, sx, y, 18, 18, 6, favOnly ? Ui.ACCENT_DARK : Ui.CARD, favOnly ? Ui.ACCENT : Ui.BORDER);
        Ui.heart(g, sx + 5, y + 6, favOnly, Ui.MUTED);
        hit(sx, y, 18, 18, () -> {
            favOnly = !favOnly;
            scroll = 0;
        });
        int fw = Math.max(70, Math.min(130, sx - 8 - x));
        int fx = sx - 6 - fw;
        Ui.card(g, fx, y, fw, 18, 6, Ui.CARD, searchFocus ? Ui.ACCENT : Ui.BORDER);
        String shown = q.isEmpty() && !searchFocus ? "Szukaj..." : q + (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        Ui.text(g, font, font.plainSubstrByWidth(shown, fw - 10), fx + 6, y + 5, q.isEmpty() ? Ui.MUTED : Ui.TEXT);
        hit(fx, y, fw, 18, () -> searchFocus = true);

        // siatka kart
        int gx = cx + 10;
        int gy = cy + 36;
        int gw = cw - 20;
        int gh = ch - 44;
        int cols = gw >= 420 ? 3 : 2;
        int gap = 8;
        int cardW = (gw - (cols - 1) * gap) / cols;
        int cardH = 76;
        List<Module> list = filtered();
        int rows = (list.size() + cols - 1) / cols;
        int content = rows * (cardH + gap);
        maxScroll = Math.max(0, content - gh);
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        if (list.isEmpty()) {
            Ui.center(g, font, favOnly ? "Brak ulubionych modulow." : "Brak modulow dla tych filtrow.", cx + cw / 2, cy + ch / 2, Ui.MUTED);
            return;
        }
        g.enableScissor(gx, gy, gx + gw, gy + gh);
        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);
            int col = i % cols;
            int row = i / cols;
            int x0 = gx + col * (cardW + gap);
            int y0 = gy + row * (cardH + gap) - scroll;
            if (y0 + cardH < gy || y0 > gy + gh) continue;
            drawCard(g, mx, my, m, x0, y0, cardW, cardH, gy, gy + gh);
        }
        g.disableScissor();
        if (maxScroll > 0) {
            int bh = Math.max(20, gh * gh / content);
            int by = gy + (gh - bh) * scroll / maxScroll;
            Ui.rr(g, cx + cw - 6, by, 3, bh, 1, 0xFF333844);
        }
    }

    private void drawCard(GuiGraphics g, int mx, int my, Module m, int x, int y, int w, int h, int clipY1, int clipY2) {
        boolean on = m.enabled();
        boolean hov = Ui.in(mx, my, x, y, w, h) && my >= clipY1 && my < clipY2;
        Ui.card(g, x, y, w, h, 8, hov ? 0xFF1E2028 : Ui.CARD, on ? 0xFF2B4A86 : Ui.BORDER);
        Ui.text(g, font, font.plainSubstrByWidth(m.name, w - 36), x + 10, y + 8, on ? Ui.TEXT : 0xFFB6BBC8);
        // serce
        int hx = x + w - 20;
        Ui.heart(g, hx, y + 9, m.fav(), Ui.MUTED);
        hitClip(hx - 3, y + 5, 14, 14, () -> {
            Cfg.state(m.id).fav = !Cfg.state(m.id).fav;
            Cfg.save();
        }, null, clipY1, clipY2);
        // ikona + opis
        g.renderItem(new ItemStack(m.icon), x + 10, y + 22);
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(m.desc), w - 44);
        for (int i = 0; i < Math.min(2, lines.size()); i++) g.drawString(font, lines.get(i), x + 32, y + 22 + i * 10, Ui.col(Ui.MUTED), false);
        // dol: zebatka + status
        int by = y + h - 24;
        Ui.card(g, x + 8, by, 20, 18, 6, Ui.OFF, Ui.BORDER);
        Ui.gear(g, x + 13, by + 4, Ui.MUTED);
        hitClip(x + 8, by, 20, 18, () -> {
            open = m;
            scroll = 0;
        }, null, clipY1, clipY2);
        int sw = w - 40;
        Ui.card(g, x + 32, by, sw, 18, 6, on ? Ui.ACCENT_DARK : Ui.OFF, on ? Ui.ACCENT : Ui.BORDER);
        Ui.center(g, font, on ? "Enabled" : "Disabled", x + 32 + sw / 2, by + 5, on ? Ui.ACCENT_TXT : Ui.MUTED);
        hitClip(x + 32, by, sw, 18, () -> {
            m.toggle();
            Cfg.save();
        }, null, clipY1, clipY2);
    }

    // ------------------------------------------------------------ USTAWIENIA MODULU
    private void drawDetail(GuiGraphics g, int mx, int my, int cx, int cy, int cw, int ch) {
        Module m = open;
        int x = cx + 12;
        int y = cy + 12;
        button(g, mx, my, x, y, 60, 20, "< Wroc", false, () -> open = null);
        g.renderItem(new ItemStack(m.icon), x + 70, y + 2);
        Ui.text(g, font, m.name + " - ustawienia", x + 92, y + 6, Ui.TEXT);
        y += 32;
        Ui.text(g, font, m.desc, x, y, Ui.MUTED);
        y += 18;
        boolean on = m.enabled();
        int bw = 90;
        Ui.card(g, x, y, bw, 20, 6, on ? Ui.ACCENT_DARK : Ui.OFF, on ? Ui.ACCENT : Ui.BORDER);
        Ui.center(g, font, on ? "Enabled" : "Disabled", x + bw / 2, y + 6, on ? Ui.ACCENT_TXT : Ui.MUTED);
        hit(x, y, bw, 20, () -> {
            m.toggle();
            Cfg.save();
        });
        y += 30;
        if (m.settings.isEmpty()) Ui.text(g, font, "Ten modul nie ma dodatkowych ustawien.", x, y, Ui.MUTED);
        for (Module.Setting s : m.settings) {
            Ui.text(g, font, s.label, x, y + 6, Ui.TEXT);
            int rx = cx + cw - 12;
            if (s.bool) {
                boolean v = m.flag(s.key);
                Ui.card(g, rx - 44, y, 44, 20, 10, v ? Ui.ACCENT_DARK : Ui.OFF, v ? Ui.ACCENT : Ui.BORDER);
                Ui.rr(g, v ? rx - 22 : rx - 41, y + 3, 14, 14, 7, v ? Ui.ACCENT : Ui.MUTED);
                hit(rx - 44, y, 44, 20, () -> {
                    m.set(s.key, v ? 0 : 1);
                    Cfg.save();
                });
            } else {
                double v = m.get(s.key);
                String vs = s.step >= 1 ? String.valueOf((int) v) : String.format("%.1f", v);
                Ui.card(g, rx - 24, y, 24, 20, 6, Ui.CARD, Ui.BORDER);
                Ui.center(g, font, "+", rx - 12, y + 6, Ui.TEXT);
                hit(rx - 24, y, 24, 20, () -> {
                    m.set(s.key, Math.min(s.max, Math.round((v + s.step) * 100.0) / 100.0));
                    Cfg.save();
                });
                int vw = 46;
                Ui.center(g, font, vs, rx - 24 - vw / 2 - 4, y + 6, Ui.ACCENT_TXT);
                Ui.card(g, rx - 24 - vw - 8 - 24, y, 24, 20, 6, Ui.CARD, Ui.BORDER);
                Ui.center(g, font, "-", rx - 24 - vw - 8 - 12, y + 6, Ui.TEXT);
                hit(rx - 24 - vw - 8 - 24, y, 24, 20, () -> {
                    m.set(s.key, Math.max(s.min, Math.round((v - s.step) * 100.0) / 100.0));
                    Cfg.save();
                });
            }
            y += 28;
        }
    }

    // ------------------------------------------------------------ USTAWIENIA KLIENTA
    private void drawSettings(GuiGraphics g, int mx, int my, int cx, int cy, int cw, int ch) {
        int x = cx + 14;
        int y = cy + 14;
        Ui.text(g, font, "Rozmycie tla gry w menu", x, y + 6, Ui.TEXT);
        boolean v = Cfg.d.blur;
        int rx = cx + cw - 14;
        Ui.card(g, rx - 44, y, 44, 20, 10, v ? Ui.ACCENT_DARK : Ui.OFF, v ? Ui.ACCENT : Ui.BORDER);
        Ui.rr(g, v ? rx - 22 : rx - 41, y + 3, 14, 14, 7, v ? Ui.ACCENT : Ui.MUTED);
        hit(rx - 44, y, 44, 20, () -> {
            Cfg.d.blur = !Cfg.d.blur;
            Cfg.save();
        });
        y += 34;
        Ui.text(g, font, "Klawisz otwierajacy menu: Esc > Opcje > Sterowanie > Nice Client.", x, y, Ui.MUTED);
        y += 22;
        button(g, mx, my, x, y, 190, 22, "Zresetuj pozycje HUD", false, () -> {
            Cfg.d.hud.clear();
            Cfg.save();
            say("Pozycje HUD przywrocone.");
        });
        y += 30;
        button(g, mx, my, x, y, 190, 22, "Przywroc domyslne moduly", false, () -> {
            Modules.resetAll();
            say("Przywrocono domyslne ustawienia.");
        });
        y += 40;
        Ui.text(g, font, "Nice Client 1.0 - HollyMC.PL", x, y, Ui.MUTED);
    }

    // ------------------------------------------------------------ PROFILE
    private void drawProfiles(GuiGraphics g, int mx, int my, int cx, int cy, int cw, int ch) {
        int x = cx + 14;
        int y = cy + 14;
        button(g, mx, my, x, y, 220, 22, "Zapisz biezace ustawienia jako profil", true, () -> {
            int n = Cfg.d.profiles.size() + 1;
            String name = "Profil " + n;
            while (Cfg.d.profiles.containsKey(name)) name = "Profil " + (++n);
            Cfg.d.profiles.put(name, Cfg.snapshot());
            Cfg.save();
            say("Zapisano: " + name);
        });
        y += 34;
        if (Cfg.d.profiles.isEmpty()) Ui.text(g, font, "Brak zapisanych profili.", x, y, Ui.MUTED);
        for (Map.Entry<String, Cfg.Snap> en : new ArrayList<>(Cfg.d.profiles.entrySet())) {
            String name = en.getKey();
            Ui.card(g, x, y, cw - 28, 26, 7, Ui.CARD, Ui.BORDER);
            Ui.text(g, font, name, x + 10, y + 9, Ui.TEXT);
            int bx = x + cw - 28 - 8;
            bx -= 54;
            button(g, mx, my, bx, y + 3, 54, 20, "Usun", false, () -> {
                Cfg.d.profiles.remove(name);
                Cfg.save();
                say("Usunieto: " + name);
            });
            bx -= 62;
            button(g, mx, my, bx, y + 3, 58, 20, "Wczytaj", true, () -> {
                Cfg.apply(en.getValue());
                say("Wczytano: " + name);
            });
            y += 32;
        }
    }

    // ------------------------------------------------------------ INPUT
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        searchFocus = false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!Ui.in(mx, my, h.x, h.y, h.w, h.h)) continue;
            if (h.cy1 >= 0 && (my < h.cy1 || my >= h.cy2)) continue;
            Runnable r = button == 1 && h.right != null ? h.right : h.left;
            if (r != null) {
                r.run();
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (tab == 0 && open == null) {
            scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (sy * 20)));
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (searchFocus && c >= 32 && c != 127 && q.length() < 24) {
            q += c;
            scroll = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (searchFocus) {
            if (key == 259 && !q.isEmpty()) { // BACKSPACE
                q = q.substring(0, q.length() - 1);
                scroll = 0;
                return true;
            }
            if (key == 257 || key == 256) { // ENTER / ESC: zdejmij fokus
                searchFocus = false;
                return true;
            }
        }
        if (NiceclientClient.menuKey != null && NiceclientClient.menuKey.matches(key, scan) && !searchFocus) {
            onClose();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public void removed() {
        Cfg.save();
    }
}

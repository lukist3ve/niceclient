package pl.nice.client.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Widgety HUD: rysowanie w grze i w edytorze (przeciaganie, skalowanie scrollem, reset PPM). */
public final class Hud {
    public abstract static class Widget {
        public final String id;
        public int lastW = 60;
        public int lastH = 20;

        Widget(String id) {
            this.id = id;
        }

        /** Rysuje widget w punkcie (0,0) i zwraca {szerokosc, wysokosc}; null = nic do pokazania. */
        abstract int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview);

        /** Domyslna pozycja zalezna od rozmiaru ekranu. */
        abstract int[] def(int sw, int sh, int w, int h);

        Module mod() {
            return Modules.get(id);
        }
    }

    public static final List<Widget> WIDGETS = new ArrayList<>();
    public static int combo;
    public static long lastHit;

    private Hud() {}

    // ---- pomocnicze ----
    private static int[] pill(GuiGraphics g, Font f, String... parts) {
        int w = 12;
        for (int i = 0; i < parts.length; i += 2) w += f.width(parts[i]) + (i + 2 < parts.length ? 4 : 0);
        int h = 20;
        Ui.card(g, 0, 0, w, h, 6, 0xC0101114, 0x55FFFFFF);
        int x = 6;
        for (int i = 0; i < parts.length; i += 2) {
            int color = i + 1 < parts.length ? (int) Long.parseLong(parts[i + 1], 16) : Ui.TEXT;
            Ui.textShadow(g, f, parts[i], x, 6, color);
            x += f.width(parts[i]) + 4;
        }
        return new int[]{w, h};
    }

    private static int pctColor(int p) {
        return p > 50 ? 0xFFFFFFFF : p > 25 ? 0xFFFFD54A : 0xFFFF7A59;
    }

    private static int count(Minecraft mc, Item item) {
        int n = 0;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack s = mc.player.getInventory().getItem(i);
            if (s.is(item)) n += s.getCount();
        }
        return n;
    }

    public static void init() {
        WIDGETS.add(new Widget("reach") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                double d = 3.0;
                boolean has = preview;
                HitResult hr = mc.hitResult;
                if (!preview && mc.player != null && hr != null && hr.getType() != HitResult.Type.MISS) {
                    d = hr.getLocation().distanceTo(mc.player.getEyePosition());
                    has = true;
                }
                int dec = (int) mod().get("decimals");
                String v = has ? String.format("%." + dec + "f", d) : "-";
                return pill(g, f, "Reach", "FF8A90A0", v, "FFFFFFFF", "blocks", "FF8A90A0");
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{8, sh / 2 - 40};
            }
        });
        WIDGETS.add(new Widget("armor") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                Item[] demo = {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
                int[] demoPct = {95, 62, 39, 14};
                boolean icons = mod().flag("icons");
                int cw = 30;
                int w = cw * 4 + 8;
                int h = icons ? 36 : 22;
                Ui.card(g, 0, 0, w, h, 8, 0xC0101114, 0x55FFFFFF);
                for (int i = 0; i < 4; i++) {
                    ItemStack s = preview || mc.player == null ? new ItemStack(demo[i]) : mc.player.getItemBySlot(slots[i]);
                    int pct = -1;
                    if (preview) pct = demoPct[i];
                    else if (!s.isEmpty() && s.isDamageableItem()) pct = (s.getMaxDamage() - s.getDamageValue()) * 100 / s.getMaxDamage();
                    else if (!s.isEmpty()) pct = 100;
                    int cx = 4 + i * cw + cw / 2;
                    int ty = icons ? 22 : 7;
                    if (icons) g.renderItem(s, cx - 8, 4);
                    String t = pct < 0 ? "-" : pct + "%";
                    Ui.textShadow(g, f, t, cx - f.width(t) / 2, ty, pct < 0 ? Ui.MUTED : pctColor(pct));
                }
                return new int[]{w, h};
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{8, sh / 2 + 2};
            }
        });
        WIDGETS.add(new Widget("inventory") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                Item[] items = {Items.TOTEM_OF_UNDYING, Items.GOLDEN_APPLE, Items.ENDER_PEARL, Items.END_CRYSTAL, Items.ARROW, Items.EXPERIENCE_BOTTLE};
                int[] demo = {3, 12, 16, 64, 32, 64};
                int cw = 28;
                int w = cw * items.length + 8;
                int h = 36;
                Ui.card(g, 0, 0, w, h, 8, 0xC0101114, 0x55FFFFFF);
                for (int i = 0; i < items.length; i++) {
                    int n = preview || mc.player == null ? demo[i] : count(mc, items[i]);
                    int cx = 4 + i * cw + cw / 2;
                    g.renderItem(new ItemStack(items[i]), cx - 8, 4);
                    String t = String.valueOf(n);
                    Ui.textShadow(g, f, t, cx - f.width(t) / 2, 22, n == 0 ? Ui.MUTED : 0xFFFFFFFF);
                }
                return new int[]{w, h};
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{sw - w - 8, sh - h - 8};
            }
        });
        WIDGETS.add(new Widget("fps") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                return pill(g, f, "FPS", "FF8A90A0", String.valueOf(mc.getFps()), "FFFFFFFF");
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{8, 8};
            }
        });
        WIDGETS.add(new Widget("coords") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                int dec = (int) mod().get("decimals");
                String fmt = "%." + dec + "f";
                double x = 0, y = 64, z = 0;
                if (mc.player != null) {
                    x = mc.player.getX();
                    y = mc.player.getY();
                    z = mc.player.getZ();
                }
                return pill(g, f, "XYZ", "FF8A90A0", String.format(fmt + ", " + fmt + ", " + fmt, x, y, z), "FFFFFFFF");
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{8, 32};
            }
        });
        WIDGETS.add(new Widget("ping") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                int ms = 0;
                if (mc.getConnection() != null && mc.player != null) {
                    PlayerInfo pi = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                    if (pi != null) ms = pi.getLatency();
                }
                return pill(g, f, "Ping", "FF8A90A0", ms + " ms", ms < 80 ? "FF7CFFB2" : ms < 150 ? "FFFFD54A" : "FFFF7A59");
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{8, 56};
            }
        });
        WIDGETS.add(new Widget("clock") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                String p = mod().flag("h24") ? "HH:mm" : "hh:mm a";
                return pill(g, f, LocalTime.now().format(DateTimeFormatter.ofPattern(p)), "FFFFFFFF");
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{sw - w - 8, 8};
            }
        });
        WIDGETS.add(new Widget("combo") {
            int[] draw(GuiGraphics g, Minecraft mc, Font f, boolean preview) {
                long timeout = (long) (mod().get("timeout") * 1000);
                int n = combo;
                if (!preview && (n <= 0 || System.currentTimeMillis() - lastHit > timeout)) {
                    combo = 0;
                    return null;
                }
                if (preview) n = 3;
                String t = n + "x";
                int w = f.width(t) + 16;
                Ui.card(g, 0, 0, w, 20, 6, 0xC0101114, 0x55FFFFFF);
                Ui.textShadow(g, f, t, 8, 6, Ui.ACCENT_TXT);
                return new int[]{w, 20};
            }

            int[] def(int sw, int sh, int w, int h) {
                return new int[]{sw / 2 - w / 2, sh / 2 + 24};
            }
        });
    }

    public static Widget get(String id) {
        for (Widget w : WIDGETS) if (w.id.equals(id)) return w;
        return null;
    }

    /** Pozycja widgetu: zapisana przez uzytkownika albo domyslna; przycieta do ekranu. */
    public static int[] place(Widget w, int sw, int sh) {
        Cfg.Pos p = Cfg.pos(w.id);
        float s = Math.max(0.5f, Math.min(3f, p.scale));
        int rw = Math.round(w.lastW * s);
        int rh = Math.round(w.lastH * s);
        int x;
        int y;
        if (p.custom) {
            x = p.x;
            y = p.y;
        } else {
            int[] d = w.def(sw, sh, rw, rh);
            x = d[0];
            y = d[1];
        }
        x = Math.max(0, Math.min(sw - rw, x));
        y = Math.max(0, Math.min(sh - rh, y));
        return new int[]{x, y, rw, rh};
    }

    public static void drawWidget(GuiGraphics g, Widget w, Minecraft mc, boolean preview) {
        Cfg.Pos p = Cfg.pos(w.id);
        float s = Math.max(0.5f, Math.min(3f, p.scale));
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        // pierwszy przebieg tylko do pomiaru rozmiaru (rysowany poza ekranem nie jest potrzebny: uzywamy ostatniego rozmiaru)
        int[] pl = place(w, sw, sh);
        Compat.push(g);
        Compat.translate(g, pl[0], pl[1]);
        Compat.scale(g, s);
        int[] size = w.draw(g, mc, mc.font, preview);
        Compat.pop(g);
        if (size != null) {
            w.lastW = size[0];
            w.lastH = size[1];
        }
    }

    public static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.screen instanceof HudEditorScreen) return;
        for (Widget w : WIDGETS) {
            Module m = w.mod();
            if (m != null && m.enabled()) drawWidget(g, w, mc, false);
        }
    }

    public static void resetPos(String id) {
        Cfg.Pos p = Cfg.pos(id);
        p.custom = false;
        p.scale = 1f;
    }
}

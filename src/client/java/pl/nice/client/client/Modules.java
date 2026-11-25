package pl.nice.client.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/** Rejestr modulow + logika modulow dzialajacych w tiku (Fullbright, Toggle Sprint). */
public final class Modules {
    public static final String[] CATS = {"All", "Combat", "HUD", "Animations", "Sound", "World", "Utility", "Client"};
    public static final List<Module> LIST = new ArrayList<>();
    private static boolean fbApplied;

    private Modules() {}

    public static void init() {
        LIST.add(new Module("reach", "Reach", "Pokazuje odleglosc do celu na celowniku w blokach.", "HUD", Items.IRON_SWORD, true).num("decimals", "Miejsca po przecinku", 0, 3, 1, 2));
        LIST.add(new Module("armor", "Armor HUD", "Procent wytrzymalosci kazdej czesci zbroi.", "HUD", Items.DIAMOND_CHESTPLATE, true).bool("icons", "Ikony zbroi", true));
        LIST.add(new Module("inventory", "Inventory HUD", "Liczba waznych przedmiotow w ekwipunku (totemy, jablka, perly...).", "HUD", Items.CHEST, true));
        LIST.add(new Module("fps", "FPS", "Aktualna liczba klatek na sekunde.", "HUD", Items.REPEATER, false));
        LIST.add(new Module("coords", "Coordinates", "Twoje wspolrzedne XYZ.", "HUD", Items.COMPASS, false).num("decimals", "Miejsca po przecinku", 0, 2, 1, 0));
        LIST.add(new Module("ping", "Ping", "Opoznienie do serwera w ms.", "HUD", Items.REDSTONE, false));
        LIST.add(new Module("clock", "Clock", "Aktualna godzina systemowa.", "HUD", Items.CLOCK, false).bool("h24", "Format 24h", true));
        LIST.add(new Module("combo", "Combo Counter", "Licznik kolejnych trafien w walce.", "Combat", Items.DIAMOND_SWORD, false).num("timeout", "Reset po (s)", 1, 5, 0.5, 2));
        LIST.add(new Module("hitsound", "Hit Sounds", "Dzwiek przy trafieniu przeciwnika.", "Sound", Items.NOTE_BLOCK, false).num("volume", "Glosnosc", 0.1, 1, 0.1, 0.6).num("pitch", "Ton", 0.5, 2, 0.1, 1.4));
        LIST.add(new Module("fullbright", "Fullbright", "Rozjasnia swiat (efekt widzenia w nocy po stronie klienta).", "World", Items.LANTERN, false));
        LIST.add(new Module("sprint", "Toggle Sprint", "Automatyczny sprint podczas chodzenia do przodu.", "Utility", Items.LEATHER_BOOTS, false));
        ensureDefaults();
    }

    public static void ensureDefaults() {
        for (Module m : LIST) {
            if (!Cfg.d.modules.containsKey(m.id)) Cfg.state(m.id).enabled = m.defOn;
        }
    }

    public static Module get(String id) {
        for (Module m : LIST) if (m.id.equals(id)) return m;
        return null;
    }

    public static void resetAll() {
        Cfg.d.modules.clear();
        Cfg.d.hud.clear();
        ensureDefaults();
        Cfg.save();
    }

    public static void tick(Minecraft mc) {
        if (mc.player == null) {
            fbApplied = false;
            return;
        }
        Module fb = get("fullbright");
        if (fb != null && fb.enabled()) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, false, false, false));
            fbApplied = true;
        } else if (fbApplied) {
            mc.player.removeEffect(MobEffects.NIGHT_VISION);
            fbApplied = false;
        }
        Module sp = get("sprint");
        if (sp != null && sp.enabled() && mc.options.keyUp.isDown()) mc.options.keySprint.setDown(true);
    }
}

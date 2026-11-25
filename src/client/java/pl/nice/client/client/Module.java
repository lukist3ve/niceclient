package pl.nice.client.client;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/** Jeden modul widoczny w Mod Menu. */
public final class Module {
    public static final class Setting {
        public final String key;
        public final String label;
        public final boolean bool;
        public final double min;
        public final double max;
        public final double step;
        public final double def;

        Setting(String key, String label, boolean bool, double min, double max, double step, double def) {
            this.key = key;
            this.label = label;
            this.bool = bool;
            this.min = min;
            this.max = max;
            this.step = step;
            this.def = def;
        }
    }

    public final String id;
    public final String name;
    public final String desc;
    public final String cat;
    public final Item icon;
    public final boolean defOn;
    public final List<Setting> settings = new ArrayList<>();

    Module(String id, String name, String desc, String cat, Item icon, boolean defOn) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.cat = cat;
        this.icon = icon;
        this.defOn = defOn;
    }

    public Module bool(String key, String label, boolean def) {
        settings.add(new Setting(key, label, true, 0, 1, 1, def ? 1 : 0));
        return this;
    }

    public Module num(String key, String label, double min, double max, double step, double def) {
        settings.add(new Setting(key, label, false, min, max, step, def));
        return this;
    }

    public boolean enabled() {
        return Cfg.state(id).enabled;
    }

    public boolean fav() {
        return Cfg.state(id).fav;
    }

    public double get(String key) {
        Double v = Cfg.state(id).set.get(key);
        if (v != null) return v;
        for (Setting s : settings) if (s.key.equals(key)) return s.def;
        return 0;
    }

    public boolean flag(String key) {
        return get(key) >= 0.5;
    }

    public void set(String key, double v) {
        Cfg.state(id).set.put(key, v);
    }

    public void toggle() {
        Cfg.state(id).enabled = !Cfg.state(id).enabled;
    }
}

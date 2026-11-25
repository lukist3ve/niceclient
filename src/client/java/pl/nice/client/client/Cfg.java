package pl.nice.client.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Konfiguracja zapisywana w config/niceclient.json: stan modulow, pozycje HUD, profile. */
public final class Cfg {
    public static class ModState {
        public boolean enabled;
        public boolean fav;
        public Map<String, Double> set = new LinkedHashMap<>();
    }

    public static class Pos {
        public int x;
        public int y;
        public float scale = 1f;
        public boolean custom;
    }

    public static class Snap {
        public Map<String, ModState> modules = new LinkedHashMap<>();
        public Map<String, Pos> hud = new LinkedHashMap<>();
    }

    public static class Data {
        public Map<String, ModState> modules = new LinkedHashMap<>();
        public Map<String, Pos> hud = new LinkedHashMap<>();
        public boolean blur = true;
        public Map<String, Snap> profiles = new LinkedHashMap<>();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("niceclient.json");
    public static Data d = new Data();

    private Cfg() {}

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Data x = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), Data.class);
                if (x != null) d = x;
            }
        } catch (Exception e) {
            System.err.println("[Nice Client] Nie udalo sie wczytac configu: " + e);
        }
        if (d.modules == null) d.modules = new LinkedHashMap<>();
        if (d.hud == null) d.hud = new LinkedHashMap<>();
        if (d.profiles == null) d.profiles = new LinkedHashMap<>();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(d), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[Nice Client] Nie udalo sie zapisac configu: " + e);
        }
    }

    public static ModState state(String id) {
        return d.modules.computeIfAbsent(id, k -> new ModState());
    }

    public static Pos pos(String id) {
        return d.hud.computeIfAbsent(id, k -> new Pos());
    }

    public static Snap snapshot() {
        Snap s = new Snap();
        s.modules = GSON.fromJson(GSON.toJson(d.modules), new com.google.gson.reflect.TypeToken<Map<String, ModState>>() {}.getType());
        s.hud = GSON.fromJson(GSON.toJson(d.hud), new com.google.gson.reflect.TypeToken<Map<String, Pos>>() {}.getType());
        return s;
    }

    public static void apply(Snap s) {
        Snap c = GSON.fromJson(GSON.toJson(s), Snap.class);
        d.modules = c.modules != null ? c.modules : new LinkedHashMap<>();
        d.hud = c.hud != null ? c.hud : new LinkedHashMap<>();
        Modules.ensureDefaults();
        save();
    }
}

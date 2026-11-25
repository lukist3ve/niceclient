package pl.nice.client.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

/** Punkt wejscia Nice Client (strona klienta). */
public class NiceclientClient implements ClientModInitializer {
    public static KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        Cfg.load();
        Modules.init();
        Hud.init();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.niceclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.niceclient"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ModMenuScreen());
            }
            Modules.tick(mc);
        });

        HudRenderCallback.EVENT.register(Hud::render);

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClientSide()) {
                Hud.combo = (System.currentTimeMillis() - Hud.lastHit > (long) (Modules.get("combo").get("timeout") * 1000)) ? 1 : Hud.combo + 1;
                Hud.lastHit = System.currentTimeMillis();
                Module hs = Modules.get("hitsound");
                if (hs != null && hs.enabled()) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, (float) hs.get("pitch"), (float) hs.get("volume")));
                }
            }
            return InteractionResult.PASS;
        });
    }
}

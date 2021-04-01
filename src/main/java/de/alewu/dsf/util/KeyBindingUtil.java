package de.alewu.dsf.util;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindingUtil {

    public static final KeyBinding KEY_BINDING_SECRET_BACK = new KeyBinding("key.secret.back", Keyboard.KEY_LEFT, "key.categories.dsf");
    public static final KeyBinding KEY_BINDING_SECRET_FORWARD = new KeyBinding("key.secret.forward", Keyboard.KEY_RIGHT, "key.categories.dsf");
    public static final KeyBinding KEY_BINDING_SECRET_DISPLAY_BACK = new KeyBinding("key.secret-display.back", Keyboard.KEY_DOWN, "key.categories.dsf");
    public static final KeyBinding KEY_BINDING_SECRET_DISPLAY_FORWARD = new KeyBinding("key.secret-display.forward", Keyboard.KEY_UP, "key.categories.dsf");
    private static boolean registered = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientRegistry.registerKeyBinding(KEY_BINDING_SECRET_BACK);
        ClientRegistry.registerKeyBinding(KEY_BINDING_SECRET_FORWARD);
        ClientRegistry.registerKeyBinding(KEY_BINDING_SECRET_DISPLAY_BACK);
        ClientRegistry.registerKeyBinding(KEY_BINDING_SECRET_DISPLAY_FORWARD);
    }

}

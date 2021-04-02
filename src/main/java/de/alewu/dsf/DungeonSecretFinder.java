package de.alewu.dsf;

import de.alewu.dsf.commands.MainModCommand;
import de.alewu.dsf.listener.Listeners;
import de.alewu.dsf.scanning.DungeonLayout;
import de.alewu.dsf.util.KeyBindingUtil;
import de.alewu.dsf.util.RuntimeContext;
import de.alewu.dsf.web.RemoteData;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = DungeonSecretFinder.MODID, version = DungeonSecretFinder.VERSION)
public class DungeonSecretFinder {

    public static final String MODID = "dsf";
    public static final String VERSION = "beta-0.4.0";
    private static Timer debugTimer;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        KeyBindingUtil.register();

        RemoteData.load();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        debug();

        MinecraftForge.EVENT_BUS.register(new Listeners());
        ClientCommandHandler.instance.registerCommand(new MainModCommand());
    }

    public static void debug() {
        if (debugTimer != null) {
            debugTimer.cancel();
        }
        debugTimer = new Timer();
        debugTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    DungeonLayout layout = RuntimeContext.getInstance().getCurrentDungeonLayout();
                    if (layout != null && Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.worldObj != null) {
                        World world = Minecraft.getMinecraft().thePlayer.worldObj;
                        layout.getRooms().forEach(r -> {
                            if (r.getRoomChunks().isEmpty()) {
                                return;
                            }
                            world.spawnParticle(EnumParticleTypes.CLOUD, r.getCenterPosition().getLeft(), 105, r.getCenterPosition().getRight(), 0, 0, 0);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Caught exception in debug timer:");
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }
}

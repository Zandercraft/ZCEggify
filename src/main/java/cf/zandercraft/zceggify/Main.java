package cf.zandercraft.zceggify;

import cf.zandercraft.zceggify.nms.NMSHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import cf.zandercraft.zceggify.commands.CommandZCEggify;
import cf.zandercraft.zceggify.events.EventManager;
import cf.zandercraft.zceggify.managers.PermissionManager;
import net.milkbowl.vault.economy.Economy;
import java.util.logging.Logger;

public final class Main extends JavaPlugin {
    public static Main plugin;
    public static Logger logger;

    public static EventManager eventManager = null;
    public static PermissionManager permissionManager = null;
    public static Economy economy = null;

    public static GriefPrevention griefPrevention;

    private static NMSHook nmsHook;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        try {
            String nmsVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
            nmsHook = Class.forName("cf.zandercraft.zceggify.nms.NMS_" + nmsVersion).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            getLogger().severe("Failed to initialize NMS hooks - This plugin may not be compatible with your server version");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        getCommand("ZCEggify").setExecutor(new CommandZCEggify());
        logger = getLogger();
        permissionManager = new PermissionManager();
        Settings.load();
        eventManager = new EventManager();
        eventManager.initialize();
        getLogger().info(getDescription().getName() + " is now enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info(getDescription().getName() + " is now disabled!");
    }

    public static NMSHook getNMSHook() {
        return nmsHook;
    }
}

package de.bananaco.permissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.bananaco.permissions.handlers.Handler;
import de.bananaco.permissions.ppackage.PPackage;
import de.bananaco.permissions.ppackage.PPermission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class Packages extends JavaPlugin implements Listener {

    private static String defaultPackage = "default";
    private static Map<String, PPackage> packageMap = new HashMap<String, PPackage>();

    public static Packages instance = null;

    public static String getDefaultPackage() {
        return defaultPackage;
    }

    public static Handler.DBType getType(String key) {
        return Handler.DBType.valueOf(key.toUpperCase());
    }

    public static String getType(Handler.DBType key) {
        return key.name().toLowerCase();
    }

    private Map<String, PermissionAttachment> permissions = new HashMap<String, PermissionAttachment>();
    public Handler.DBType packageType;
    public Handler.DBType databaseType;

	@Override
	public void onEnable() {
        instance = this;
        // register events
		getServer().getPluginManager().registerEvents(this, this);
        // default package is set in config.yml
        getConfig().set("defaultPackage", getConfig().getString("defaultPackage", defaultPackage));
        packageType = getType(getConfig().getString("packageType", getType(Handler.DBType.FILE)));
        databaseType = getType(getConfig().getString("databaseType", getType(Handler.DBType.FILE)));
        getConfig().set("packageType", getType(packageType));
        getConfig().set("databaseType", getType(databaseType));
        saveConfig();
	}

    // called externally by whatever package handling method is available, can also be called on world change
    @EventHandler
    public void onPackageLoad(PackageLoadEvent event) {
        setPermissions(event.getPlayer(), event.getPackages());
    }

    @EventHandler(priority =  EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        permissions.put(event.getPlayer().getName(), event.getPlayer().addAttachment(this));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        permissions.remove(event.getPlayer().getName());
    }

    // thanks for this method PermissionsBukkit
    private void setPermissions(Player player, List<PPackage> packages) {
        if (player == null) {
            return;
        }
        PermissionAttachment attachment = permissions.get(player.getName());
        if (attachment == null) {
            System.err.println("Calculating permissions on " + player.getName() + ": attachment was null");
            return;
        }
        for (String key : attachment.getPermissions().keySet()) {
            attachment.unsetPermission(key);
        }
        // load from the data we have
        for (PPackage pack : packages) {
            for(PPermission perm : pack.getPermissions()) {
                attachment.setPermission(perm.getName().toLowerCase(), perm.isTrue());
            }
        }
        player.recalculatePermissions();
    }
}
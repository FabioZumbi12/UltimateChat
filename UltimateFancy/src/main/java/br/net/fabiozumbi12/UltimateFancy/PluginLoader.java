package br.net.fabiozumbi12.UltimateFancy;

import org.bukkit.plugin.java.JavaPlugin;

public class PluginLoader extends JavaPlugin {

    public void onEnable() {
        getLogger().info("UltimateFancy v" + getDescription().getVersion() + " loaded!");
    }

    public void onDisable() {
        getLogger().info("UltimateFancy v" + getDescription().getVersion() + " disabled!");
    }

}

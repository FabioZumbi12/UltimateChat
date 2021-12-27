package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UCVaultChatHook {
    private final Player sender;

    UCVaultChatHook(Player sender) {
        this.sender = sender;
    }

    public String getGroupSuffixes() {
        if (UCVaultCache.gSuffix.containsKey(sender.getName())) {
            return UCVaultCache.gSuffix.get(sender.getName());
        }
        StringBuilder gsuffixes = new StringBuilder();
        for (String g : UCVaultCache.getVaultPerms(sender).getPlayerGroups()) {
            gsuffixes.append(UChat.get().getHooks().getVaultChat().getGroupSuffix(sender.getWorld().getName(), g));
        }
        String gps = gsuffixes.toString();

        setTempCacheGSuffix(sender.getName(), gps);
        return gps;
    }

    private void setTempCacheGSuffix(String sender, String gps) {
        UCVaultCache.gSuffix.put(sender, gps);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.gSuffix.remove(sender), 40);
    }


    public String getGroupPrefixes() {
        if (UCVaultCache.gPrefix.containsKey(sender.getName())) {
            return UCVaultCache.gPrefix.get(sender.getName());
        }
        StringBuilder gprefixes = new StringBuilder();
        for (String g : UCVaultCache.getVaultPerms(sender).getPlayerGroups()) {
            String pref = UChat.get().getHooks().getVaultChat().getGroupPrefix(sender.getWorld().getName(), g);
            if (pref != null)
                gprefixes.append(UChat.get().getHooks().getVaultChat().getGroupPrefix(sender.getWorld().getName(), g));
        }
        String gps = gprefixes.toString();

        setTempCacheGPrefix(sender.getName(), gps);
        return gps;
    }

    private void setTempCacheGPrefix(String sender, String gps) {
        UCVaultCache.gPrefix.put(sender, gps);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.gPrefix.remove(sender), 40);
    }


    public String getPlayerSuffix() {
        if (UCVaultCache.playerSuffix.containsKey(sender.getName())) {
            return UCVaultCache.playerSuffix.get(sender.getName());
        }
        String suff = UChat.get().getHooks().getVaultChat().getPlayerSuffix(sender);

        setTempCacheSuffix(sender.getName(), suff);
        return suff;
    }

    private void setTempCacheSuffix(String sender, String suff) {
        UCVaultCache.playerSuffix.put(sender, suff);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerSuffix.remove(sender), 40);
    }


    public String getPlayerPrefix() {
        if (UCVaultCache.playerPrefix.containsKey(sender.getName())) {
            return UCVaultCache.playerPrefix.get(sender.getName());
        }
        String pref = UChat.get().getHooks().getVaultChat().getPlayerPrefix(sender);

        getPlayerPrefix(sender.getName(), pref);
        return pref;
    }

    private void getPlayerPrefix(String sender, String pref) {
        UCVaultCache.playerPrefix.put(sender, pref);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerPrefix.remove(sender), 40);
    }

    public String getPlayerPrefixes() {
        if (UCVaultCache.playerPrefixes.containsKey(sender.getName())) {
            return UCVaultCache.playerPrefixes.get(sender.getName());
        }
        StringBuilder gps = new StringBuilder();
        String[] groups = UCVaultCache.getVaultPerms(sender).getPlayerGroups();
        for (String group : groups) {
            if (UChat.get().getUCConfig().getStringList("general.dont-show-groups").contains(group)) continue;
            String prefix = UChat.get().getHooks().getVaultChat().getGroupPrefix(sender.getWorld(), group);
            if (prefix != null)
                gps.append(UChat.get().getHooks().getVaultChat().getGroupPrefix(sender.getWorld(), group));
        }

        getPlayerPrefixes(sender.getName(), gps.toString());
        return gps.toString();
    }

    private void getPlayerPrefixes(String sender, String prefixes) {
        UCVaultCache.playerPrefixes.put(sender, prefixes);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerPrefixes.remove(sender), 40);
    }

    public String getPlayerSuffixes() {
        StringBuilder gps = new StringBuilder();
        String[] groups = UCVaultCache.getVaultPerms(sender).getPlayerGroups();
        for (String group : groups) {
            String suf = UChat.get().getHooks().getVaultChat().getGroupSuffix(sender.getWorld(), group);
            if (suf != null)
                gps.append(UChat.get().getHooks().getVaultChat().getGroupSuffix(sender.getWorld(), group));
        }

        getPlayerSuffixes(sender.getName(), gps.toString());
        return gps.toString();
    }

    private void getPlayerSuffixes(String sender, String prefixes) {
        UCVaultCache.playerPrefixes.put(sender, prefixes);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerPrefixes.remove(sender), 40);
    }

}
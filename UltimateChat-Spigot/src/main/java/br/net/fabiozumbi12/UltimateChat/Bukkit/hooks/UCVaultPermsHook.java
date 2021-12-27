package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UCVaultPermsHook {
    private final Player sender;

    UCVaultPermsHook(Player sender) {
        this.sender = sender;
    }

    public String[] getPlayerGroups() {
        if (UCVaultCache.playerGroups.containsKey(sender.getName())) {
            return UCVaultCache.playerGroups.get(sender.getName());
        }
        String[] pgs = UChat.get().getHooks().getVaultPerms().getPlayerGroups(sender.getWorld().getName(), sender);

        setTempCacheGroups(sender.getName(), pgs);
        return pgs;
    }

    public String getPrimaryGroup() {
        if (UCVaultCache.primaryGroups.containsKey(sender.getName())) {
            return UCVaultCache.primaryGroups.get(sender.getName());
        }
        String pmg = UChat.get().getHooks().getVaultPerms().getPrimaryGroup(sender.getWorld().getName(), sender);

        setTempCachePrimary(sender.getName(), pmg);
        return pmg;
    }

    private void setTempCacheGroups(String sender, String[] pgs) {
        UCVaultCache.playerGroups.put(sender, pgs);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerGroups.remove(sender), 40);
    }

    private void setTempCachePrimary(String sender, String pmg) {
        UCVaultCache.primaryGroups.put(sender, pmg);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.primaryGroups.remove(sender), 40);
    }
}

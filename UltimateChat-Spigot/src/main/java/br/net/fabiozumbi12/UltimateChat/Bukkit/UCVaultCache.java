package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UCVaultCache {
	static final HashMap<String, String> playerPrefixes = new HashMap<>();

	static final HashMap<String, String[]> playerGroups = new HashMap<>();
	static final HashMap<String, String> primaryGroups = new HashMap<>();
	
	static final HashMap<String, String> playerSuffix = new HashMap<>();
	static final HashMap<String, String> playerPrefix = new HashMap<>();
	
	static final HashMap<String, String> gSuffix = new HashMap<>();
	static final HashMap<String, String> gPrefix = new HashMap<>();
	
	public static VaultPerms getVaultPerms(Player sender){		
		return new VaultPerms(sender);
	}
	
	public static VaultChat getVaultChat(Player sender){
		return new VaultChat(sender);
	}
}

class VaultChat{
	private final Player sender;
	VaultChat(Player sender){
		this.sender = sender;
	}
	
	public String getGroupSuffixes(){
		if (UCVaultCache.gSuffix.containsKey(sender.getName())){
			return UCVaultCache.gSuffix.get(sender.getName());
		}
		StringBuilder gsuffixes = new StringBuilder();
		for (String g:UCVaultCache.getVaultPerms(sender).getPlayerGroups()){
			gsuffixes.append(UChat.get().getVaultChat().getGroupSuffix(sender.getWorld().getName(), g));
		}
		String gps = gsuffixes.toString();
		
		setTempCacheGSuffix(sender.getName(), gps);		
		return gps;
	}
	
	private void setTempCacheGSuffix(String sender, String gps){
		UCVaultCache.gSuffix.put(sender, gps);
		Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.gSuffix.remove(sender), 40);
	}
	
	
	public String getGroupPrefixes(){
		if (UCVaultCache.gPrefix.containsKey(sender.getName())){
			return UCVaultCache.gPrefix.get(sender.getName());
		}
		StringBuilder gprefixes = new StringBuilder();
		for (String g:UCVaultCache.getVaultPerms(sender).getPlayerGroups()){
			gprefixes.append(UChat.get().getVaultChat().getGroupPrefix(sender.getWorld().getName(), g));
		}
		String gps = gprefixes.toString();
		
		setTempCacheGPrefix(sender.getName(), gps);		
		return gps;
	}
	
	private void setTempCacheGPrefix(String sender, String gps){
		UCVaultCache.gPrefix.put(sender, gps);
		Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.gPrefix.remove(sender), 40);
	}
	
	
	public String getPlayerSuffix(){
		if (UCVaultCache.playerSuffix.containsKey(sender.getName())){
			return UCVaultCache.playerSuffix.get(sender.getName());
		}
		String suff = UChat.get().getVaultChat().getPlayerSuffix(sender);
		
		setTempCacheSuffix(sender.getName(), suff);
		return suff;
	}
	
	private void setTempCacheSuffix(String sender, String suff){
		UCVaultCache.playerSuffix.put(sender, suff);
		Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerSuffix.remove(sender), 40);
	}
	
	
	public String getPlayerPrefix(){
		if (UCVaultCache.playerPrefix.containsKey(sender.getName())){
			return UCVaultCache.playerPrefix.get(sender.getName());
		}
		String pref = UChat.get().getVaultChat().getPlayerPrefix(sender);
		
		getPlayerPrefix(sender.getName(), pref);
		return pref;
	}
	
	private void getPlayerPrefix(String sender, String pref){
		UCVaultCache.playerPrefix.put(sender, pref);
		Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerPrefix.remove(sender), 40);
	}

	public String getPlayerPrefixes(){
	    if (UCVaultCache.playerPrefixes.containsKey(sender.getName())){
	        return UCVaultCache.playerPrefixes.get(sender.getName());
        }
	    StringBuilder gps = new StringBuilder();
	    String[] groups = UCVaultCache.getVaultPerms(sender).getPlayerGroups();
	    for (String group:groups){
	        if (UChat.get().getUCConfig().getStringList("general.dont-show-groups").contains(group)) continue;
            gps.append(UChat.get().getVaultChat().getGroupPrefix(sender.getWorld(), group));
        }

        getPlayerPrefixes(sender.getName(), gps.toString());
        return gps.toString();
    }

    private void getPlayerPrefixes(String sender, String prefixes){
        UCVaultCache.playerPrefixes.put(sender, prefixes);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerPrefixes.remove(sender), 40);
    }

    public String getPlayerSuffixes(){
        StringBuilder gps = new StringBuilder();
        String[] groups = UCVaultCache.getVaultPerms(sender).getPlayerGroups();
        for (String group:groups){
            gps.append(UChat.get().getVaultChat().getGroupSuffix(sender.getWorld(), group));
        }

        getPlayerSuffixes(sender.getName(), gps.toString());
        return gps.toString();
    }

    private void getPlayerSuffixes(String sender, String prefixes){
        UCVaultCache.playerPrefixes.put(sender, prefixes);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerPrefixes.remove(sender), 40);
    }

}

class VaultPerms {	
	private final Player sender;
	VaultPerms(Player sender){
		this.sender = sender;
	}	
	
	public String[] getPlayerGroups(){
		if (UCVaultCache.playerGroups.containsKey(sender.getName())){
			return UCVaultCache.playerGroups.get(sender.getName());
		}		
		String[] pgs = UChat.get().getVaultPerms().getPlayerGroups(sender.getWorld().getName(), sender);
		
		setTempCacheGroups(sender.getName(), pgs);
		return pgs;
	}
	
	public String getPrimaryGroup(){
		if (UCVaultCache.primaryGroups.containsKey(sender.getName())){
			return UCVaultCache.primaryGroups.get(sender.getName());
		}
		String pmg = UChat.get().getVaultPerms().getPrimaryGroup(sender.getWorld().getName(),sender);
				
		setTempCachePrimary(sender.getName(), pmg);
		return pmg;
	}
	
	private void setTempCacheGroups(String sender, String[] pgs){
		UCVaultCache.playerGroups.put(sender, pgs);
		Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerGroups.remove(sender), 40);
	}
	
	private void setTempCachePrimary(String sender, String pmg){
		UCVaultCache.primaryGroups.put(sender, pmg);
		Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.primaryGroups.remove(sender), 40);
	}
}

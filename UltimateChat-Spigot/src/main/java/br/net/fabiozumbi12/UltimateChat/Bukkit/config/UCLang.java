package br.net.fabiozumbi12.UltimateChat.Bukkit.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;

public class UCLang {
	
	private static final HashMap<Player, String> DelayedMessage = new HashMap<Player, String>();
	private static Properties BaseLang = new Properties();
	private static Properties Lang = new Properties();
	private String pathLang; 
	private String resLang; 
	
	public SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<String>();
		for (Object help:Lang.keySet()){
			if (help.toString().startsWith("cmdmanager.help.")){
				values.add(help.toString().replace("cmdmanager.help.", ""));
			}
		}
		return values;
	}
	
	public UCLang() {		
		pathLang = UChat.get().getDataFolder() + File.separator + "lang" + UChat.get().getConfig().getString("language") + ".properties";
		resLang = "lang" + UChat.get().getConfig().getString("language") + ".properties";
		
		File lang = new File(pathLang);			
		if (!lang.exists()) {
			if (UChat.get().getResource("assets/ultimatechat/"+resLang) == null){
				resLang = "langEN-US.properties";	
				pathLang = UChat.get().getDataFolder() + File.separator + "langEN-US.properties";
			}
			//UChat.get().saveResource(resLang, false);//create lang file
			UCUtil.saveResource("/assets/ultimatechat/"+resLang, new File(UChat.get().getDataFolder(),resLang));
			UChat.get().getUCLogger().info("Created lang file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		UChat.get().getUCLogger().info("Language file loaded - Using: "+ UChat.get().getConfig().getString("language"));	
	}
	
	private void loadBaseLang(){
	    BaseLang.clear();
	    try {
	    	InputStream fileInput = UChat.get().getResource("assets/ultimatechat/langEN-US.properties");	      
	        Reader reader = new InputStreamReader(fileInput, "UTF-8");
	        BaseLang.load(reader);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }	    
	    updateLang();
	  }
	
	private void loadLang() {
		Lang.clear();
		try {
			FileInputStream fileInput = new FileInputStream(pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			Lang.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", ""));
			int rpv = Integer.parseInt(UChat.get().getPDF().getVersion().replace(".", ""));
			if (langv < rpv || langv == 0){
				UChat.get().getUCLogger().warning("Your lang file is outdated. Probally need strings updates!");
				UChat.get().getUCLogger().warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", UChat.get().getPDF().getVersion());
			}
		}		
	}
	
	private void updateLang(){
		for (Entry<Object, Object> linha:BaseLang.entrySet()) {
	    	if (!Lang.containsKey(linha.getKey())) { 
	    		Lang.put(linha.getKey(), linha.getValue());
	    	}
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", UChat.get().getPDF().getVersion());
    	}
		try {
			Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String get(String key){		
		String FMsg = "";

		if (!Lang.containsKey(key)){
			FMsg = "&c&oMissing language string for "+ ChatColor.GOLD + key;
		} else {
			FMsg = Lang.get(key).toString();
		}
				
		FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);
		
		return FMsg;
	}
	
	public void sendMessage(final Player p, String key){
		if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (!Lang.containsKey(key)){
			p.sendMessage(get("_UChat.prefix")+ " " + ChatColor.translateAlternateColorCodes('&', key));
		} else if (get(key).isEmpty()){
			return;
		} else {
			p.sendMessage(get("_UChat.prefix")+ " " + get(key));
		}		
		
		DelayedMessage.put(p, key);
		Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.get(), new Runnable() { 
			public void run() {
				if (DelayedMessage.containsKey(p)){
					DelayedMessage.remove(p);
				}
				} 
			}, 20);		
	}
	
	public void sendMessage(CommandSender sender, String key){		
		if (sender instanceof Player && DelayedMessage.containsKey((Player)sender) && DelayedMessage.get((Player)sender).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			sender.sendMessage(get("_UChat.prefix")+ " " + ChatColor.translateAlternateColorCodes('&', key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			sender.sendMessage(get("_UChat.prefix")+ " " + get(key));
		}		
		
		if (sender instanceof Player){
			final Player p = (Player)sender;
			DelayedMessage.put(p, key);
			Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.get(), new Runnable() { 
				public void run() {
					if (DelayedMessage.containsKey(p)){
						DelayedMessage.remove(p);
					}
					} 
				}, 20);	
		}
		
	}
	
	public boolean containsValue(String value){
		return Lang.containsValue(value);
	}
}

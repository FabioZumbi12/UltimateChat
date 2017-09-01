package br.net.fabiozumbi12.UltimateChat.Bukkit.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;

public class UCLang {
	
	private static final HashMap<Player, String> DelayedMessage = new HashMap<Player, String>();
	static HashMap<String, String> BaseLang = new HashMap<String, String>();
	public static HashMap<String, String> Lang = new HashMap<String, String>();
	//static List<String> langString = new ArrayList<String>();
    static String pathLang; 
    static String resLang; 
    private UCLogger logger;
	
	public SortedSet<String> helpStrings(){
		SortedSet<String> values = new TreeSet<String>();
		for (String help:Lang.keySet()){
			if (help.startsWith("cmdmanager.help.")){
				values.add(help.replace("cmdmanager.help.", ""));
			}
		}
		return values;
	}
	
	public UCLang(UChat plugin, UCLogger logger, String mainPath, UCConfig config) {
		this.logger = logger;
		
		pathLang = mainPath + File.separator + "lang" + config.getString("language") + ".ini";
		resLang = "lang" + config.getString("language") + ".ini";
		
		File lang = new File(pathLang);			
		if (!lang.exists()) {
			if (plugin.getResource(resLang) == null){		
				//config.setConfig("language", "EN-US");
				//config.save();
				resLang = "langPT-BR.ini";	
				pathLang = mainPath + File.separator + "langPT-BR.ini";
			}
			plugin.saveResource(resLang, false);//create lang file
            logger.info("Created lang file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		logger.info("Language file loaded - Using: "+ config.getString("language"));	
	}
	
	void loadBaseLang(){
	    BaseLang.clear();
	    Properties properties = new Properties();
	    try {
	    	InputStream fileInput = UChat.class.getClassLoader().getResourceAsStream("langPT-BR.ini");	      
	        Reader reader = new InputStreamReader(fileInput, "UTF-8");
	        properties.load(reader);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	    for (Object key : properties.keySet()) {
	      if ((key instanceof String)) {
	    	  BaseLang.put((String)key, properties.getProperty((String)key));
	      }
	    }
	    updateLang();
	  }
	
	void loadLang() {
		Lang.clear();
		Properties properties = new Properties();
		try {
			FileInputStream fileInput = new FileInputStream(pathLang);
			Reader reader = new InputStreamReader(fileInput, "UTF-8");
			properties.load(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Object key : properties.keySet()) {
			if (!(key instanceof String)) {
				continue;
			}			
			String keylang = properties.getProperty((String) key);
			Lang.put((String) key, keylang.replace("owner", "leader"));
		}		
		
		if (Lang.get("_lang.version") != null){
			int langv = Integer.parseInt(Lang.get("_lang.version").replace(".", ""));
			int rpv = Integer.parseInt(UChat.get().getPDF().getVersion().replace(".", ""));
			if (langv < rpv || langv == 0){
				logger.warning("Your lang file is outdated. Probally need strings updates!");
				logger.warning("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", UChat.get().getPDF().getVersion());
			}
		}		
	}
	
	void updateLang(){
	    for (String linha : BaseLang.keySet()) {	    	
	      if (!Lang.containsKey(linha)) {
	    	  Lang.put(linha, BaseLang.get(linha));
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", UChat.get().getPDF().getVersion());
    	}
	    try {
	      Properties properties = new Properties()
	      {
	        private static final long serialVersionUID = 1L;	        
	        public synchronized Enumeration<Object> keys(){
	          return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	        }
	      };
	      FileReader reader = new FileReader(pathLang);
	      BufferedReader bufferedReader = new BufferedReader(reader);
	      properties.load(bufferedReader);
	      bufferedReader.close();
	      reader.close();
	      properties.clear();
	      for (String key : Lang.keySet()) {
	        if ((key instanceof String)) {
	          properties.put(key, Lang.get(key));
	        }
	      }
	      properties.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
	  }
	
	public String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for "+ ChatColor.GOLD + key;
		} else {
			FMsg = Lang.get(key);
		}
				
		FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);
		
		return FMsg;
	}
	
	public void sendMessage(final Player p, String key){
		if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			p.sendMessage(get("_UChat.prefix")+ " " + ChatColor.translateAlternateColorCodes('&', key));
		} else if (get(key).equalsIgnoreCase("")){
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

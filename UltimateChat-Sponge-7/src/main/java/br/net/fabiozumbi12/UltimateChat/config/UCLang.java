package br.net.fabiozumbi12.UltimateChat.config;

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
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import br.net.fabiozumbi12.UltimateChat.UCUtil;
import br.net.fabiozumbi12.UltimateChat.UChat;

public class UCLang {
	
	private static final HashMap<CommandSource, String> DelayedMessage = new HashMap<CommandSource, String>();
	private static HashMap<String, String> BaseLang = new HashMap<String, String>();
	private static HashMap<String, String> Lang = new HashMap<String, String>();
	//static List<String> langString = new ArrayList<String>();
    private static String pathLang;
    private static File langFile;
    private static String resLang; 
        	
	public static void init() {
		pathLang = UChat.get().configDir() + File.separator + "lang" + UChat.get().getConfig().getString("language") + ".properties"; 
		langFile = new File(pathLang);
		resLang = "lang" + UChat.get().getConfig().getString("language") + ".properties";
		
		File lang = new File(pathLang);			
		if (!lang.exists()) {
			if (UChat.class.getResource(resLang) == null){		
				UChat.get().getConfig().getString("language");
				UChat.get().getConfig().save();
				resLang = "langEN-US.properties";	
				pathLang = UChat.get().configDir() + File.separator + "langEN-US.properties";
			}
			UCUtil.saveResource(resLang, langFile);
			UChat.plugin.getLogger().info("Created lang file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		UChat.get().getLogger().info("Language file loaded - Using: "+  UChat.get().getConfig().getString("language"));	
	}
	
	private static void loadBaseLang(){
	    BaseLang.clear();
	    Properties properties = new Properties();
	    try {
	    	InputStream fileInput = UChat.class.getResourceAsStream("langEN-US.properties");	      
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
	
	private static void loadLang() {
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
			int rpv = Integer.parseInt(UChat.plugin.getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				UChat.plugin.getLogger().info("Your lang file is outdated. Probally need strings updates!");
				UChat.plugin.getLogger().info("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", UChat.plugin.getVersion().get());
			}
		}		
	}
	
	private static void updateLang(){
	    for (String linha : BaseLang.keySet()) {	    	
	      if (!Lang.containsKey(linha)) {
	    	  Lang.put(linha, BaseLang.get(linha));
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", UChat.plugin.getVersion().get());
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
	
	public static String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key);
		}
		
		return FMsg;
	}
	
	public static Text getText(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key);
		}
		
		return UCUtil.toText(FMsg);
	}
	
	public static void sendMessage(final CommandSource p, String key){
		if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			p.sendMessage(UCUtil.toText(get("_UChat.prefix")+ " " + key));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(UCUtil.toText(get("_UChat.prefix")+ " " + get(key)));
		}		
		
		DelayedMessage.put(p,key);
		Sponge.getScheduler().createSyncExecutor(UChat.plugin).schedule(new Runnable() { 
			public void run() {
				if (DelayedMessage.containsKey(p)){
					DelayedMessage.remove(p);
				}
				} 
			},1, TimeUnit.SECONDS);	
	}
		
}

package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;

public class UCLang {
	
	private static final HashMap<CommandSource, String> DelayedMessage = new HashMap<CommandSource, String>();
	private static Properties BaseLang = new Properties();
	private static Properties Lang = new Properties();
    private static String pathLang;
    private static String resLang; 
        	
	public UCLang() {
		pathLang = UChat.get().configDir() + File.separator + "lang" + UChat.get().getConfig().getString("language") + ".properties"; 
		resLang = "lang" + UChat.get().getConfig().getString("language") + ".properties";
		
		File lang = new File(pathLang);			
		if (!lang.exists()) {
			if (!UChat.get().instance().getAsset(resLang).isPresent()){
				resLang = "langEN-US.properties";	
				pathLang = UChat.get().configDir() + File.separator + "langEN-US.properties";
			}
			try {
				UChat.get().instance().getAsset(resLang).get().copyToDirectory(new File(UChat.get().configDir()).toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			UChat.get().instance().getLogger().info("Created lang file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		UChat.get().getLogger().info("Language file loaded - Using: "+  UChat.get().getConfig().getString("language"));	
	}
	
	private void loadBaseLang(){
	    BaseLang.clear();
	    try { 
	    	BaseLang.load(UChat.get().instance().getAsset("langEN-US.properties").get().getUrl().openStream());	    		        
	    } catch (Exception e){
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
			int langv = Integer.parseInt(((String)Lang.get("_lang.version")).replace(".", ""));
			int rpv = Integer.parseInt(UChat.get().instance().getVersion().get().replace(".", ""));
			if (langv < rpv || langv == 0){
				UChat.get().instance().getLogger().info("Your lang file is outdated. Probally need strings updates!");
				UChat.get().instance().getLogger().info("Lang file version: "+Lang.get("_lang.version"));
				Lang.put("_lang.version", UChat.get().instance().getVersion().get());
			}
		}		
	}
	
	private void updateLang(){
	    for (Entry<Object, Object> linha : BaseLang.entrySet()) {	    	
	      if (!Lang.containsKey(linha.getKey())) {
	    	  Lang.put(linha.getKey(), linha.getValue());
	      }
	    }
		if (!Lang.containsKey("_lang.version")){
			Lang.put("_lang.version", UChat.get().instance().getVersion().get());
    	}
		try {
			Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), "UTF-8"), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	  }
	
	public String get(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key).toString();
		}
		
		return FMsg;
	}
	
	public Text getText(String key){		
		String FMsg = "";

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key).toString();
		}
		
		return UCUtil.toText(FMsg);
	}
	
	public void sendMessage(final CommandSource p, String key){
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
		Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(new Runnable() { 
			public void run() {
				if (DelayedMessage.containsKey(p)){
					DelayedMessage.remove(p);
				}
				} 
			},1, TimeUnit.SECONDS);	
	}
		
}

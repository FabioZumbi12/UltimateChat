package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class UCLang {
	
	private static final HashMap<CommandSource, String> DelayedMessage = new HashMap<>();
	private static final Properties BaseLang = new Properties();
	private static final Properties Lang = new Properties();
    private static String pathLang;
    private static String resLang; 
        	
	public UCLang() {
		resLang = "lang" + UChat.get().getConfig().root().language + ".properties";
		pathLang = UChat.get().configDir() + File.separator + resLang;

		File lang = new File(pathLang);
		if (!lang.exists()) {
			if (!UChat.get().instance().getAsset(resLang).isPresent()){
				resLang = "langEN-US.properties";	
				pathLang = UChat.get().configDir() + File.separator + resLang;
			}
			try {
				UChat.get().instance().getAsset(resLang).get().copyToDirectory(UChat.get().configDir().toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			UChat.get().instance().getLogger().info("Created lang file: " + pathLang);
        }
		
		loadLang();
		loadBaseLang();
		UChat.get().getLogger().info("Language file loaded - Using: "+  UChat.get().getConfig().root().language);
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
				UChat.get().instance().getLogger().info("Your lang file is outdated. Probably need strings updates!");
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

	public String get(CommandSource player, String key){
		return UCMessages.formatTags("", get(key), player, "", "", UChat.get().getPlayerChannel(player));
	}
	
	public String get(String key){		
		String FMsg;

		if (Lang.get(key) == null){
			FMsg = "&c&oMissing language string for &4"+ key;
		} else {
			FMsg = Lang.get(key).toString();
		}
		return FMsg;
	}
	
	public Text getText(String key, String additional){
		return UCUtil.toText(get(key)+additional);
	}
	
	public Text getText(String key){
		return UCUtil.toText(get(key));
	}
	
	public void sendMessage(final CommandSource p, String key){
		if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)){
			return;
		}
		
		if (Lang.get(key) == null){
			p.sendMessage(UCUtil.toText(get(p,"_UChat.prefix")+ " " + UCMessages.formatTags("", key, p, "", "", UChat.get().getPlayerChannel(p))));
		} else if (get(key).equalsIgnoreCase("")){
			return;
		} else {
			p.sendMessage(UCUtil.toText(get(p,"_UChat.prefix")+ " " + get(p, key)));
		}		
		
		DelayedMessage.put(p,key);
		Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(() -> {
            if (DelayedMessage.containsKey(p)){
                DelayedMessage.remove(p);
            }
            },1, TimeUnit.SECONDS);
	}
		
}

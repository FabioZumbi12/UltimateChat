package br.net.fabiozumbi12.UltimateChat.Bukkit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee.UChatBungee;
import br.net.fabiozumbi12.UltimateChat.Bukkit.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Bukkit.config.UCLang;

import com.lenis0012.bukkit.marriage2.Marriage;
import com.lenis0012.bukkit.marriage2.MarriageAPI;

public class UChat extends JavaPlugin {
	
    private static boolean Vault = false;	    
    protected static boolean SClans;
    protected static ClanManager sc;
    protected static boolean MarryReloded;
    protected static boolean MarryMaster;
	private static boolean ProtocolLib;
	protected static MarriageMaster mm;
	protected static Marriage mapi;
	protected static boolean PlaceHolderAPI;
	protected static boolean Factions;
	private FileConfiguration amConfig;
	private int index = 0;	

	//public static HashMap<String,String> pChannels = new HashMap<String,String>();
	protected HashMap<String,String> tempChannels = new HashMap<String,String>();
	protected HashMap<String,String> tellPlayers = new HashMap<String,String>();
	protected HashMap<String,String> tempTellPlayers = new HashMap<String,String>();
	protected HashMap<String,String> respondTell = new HashMap<String,String>();
	protected List<String> command = new ArrayList<String>();
	protected HashMap<String,List<String>> ignoringPlayer = new HashMap<String,List<String>>();
	protected List<String> mutes = new ArrayList<String>();
	protected List<String> isSpy = new ArrayList<String>();
	
	public FileConfiguration getAMConfig(){
		return this.amConfig;
	}
		
	private static UChat uchat;
	public static UChat get(){
		return uchat;
	}
	
	private UCLogger logger;
	public UCLogger getUCLogger(){
		return this.logger;
	}
	
	private UCConfig config;
	
	@Override
	public UCConfig getConfig(){
		return this.config;
	}
	
	private UCDiscord UCJDA;
	public UCDiscord getUCJDA(){
		return this.UCJDA;
	}
	
	private UCLang lang;		
	public UCLang getLang(){
		return this.lang;
	}
	
	private Permission perms;
	public Permission getVaultPerms(){
		if (Vault && perms != null){
			return this.perms;
		}
		return null;
	}
	
	private Economy econ;
	public Economy getVaultEco(){
		if (Vault && econ != null){
			return this.econ;
		}
		return null;
	}
	
	private Chat chat;
	public Chat getVaultChat(){
		if (Vault && chat != null){
			return this.chat;
		}
		return null;
	}
	
	private uChatAPI ucapi;
	public uChatAPI getAPI(){
		return this.ucapi;
	}
	
	public PluginDescriptionFile getPDF(){
		return this.getDescription();
	}
	/*
	private String mainPath;	
	public String configDir(){
		return this.mainPath;
	}
	*/
	public void onEnable() {
        try {
            uchat = this;
            logger = new UCLogger(this);
            //mainPath = this.getDataFolder().getPath();
            config = new UCConfig(this);
            lang = new UCLang();
            amConfig = new YamlConfiguration();
            //check hooks
            Vault = checkVault();            
            SClans = checkSC();
            MarryReloded = checkMR();
            MarryMaster = checkMM();
            ProtocolLib = checkPL();
            PlaceHolderAPI = checkPHAPI();
            Factions = checkFac();
            
            getServer().getPluginCommand("uchat").setExecutor(new UCListener());
            getServer().getPluginManager().registerEvents(new UCListener(), this);
            getServer().getPluginManager().registerEvents(new UCChatProtection(), this);
            getServer().getPluginManager().registerEvents(new UChatBungee(), this);
            
            getServer().getMessenger().registerOutgoingPluginChannel(this, "uChat");
            getServer().getMessenger().registerIncomingPluginChannel(this, "uChat", new UChatBungee());
            
            //register aliases
            registerAliases();
            
            if (ProtocolLib){
            	logger.info("ProtocolLib found. Hooked.");
            }
            
            if (PlaceHolderAPI){
            	new UCPlaceHolders(this).hook();
            	logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders.");
            }
            
            if (MarryReloded){
            	mapi = MarriageAPI.getInstance();
            	logger.info("Marriage Reloaded found. Hooked.");
            }
            
            if (MarryMaster){
            	mm = (MarriageMaster) Bukkit.getPluginManager().getPlugin("MarriageMaster");
            	logger.info("MarryMaster found. Hooked.");
            }
            
            if (SClans){
            	sc = SimpleClans.getInstance().getClanManager();
            	logger.info("SimpleClans found. Hooked.");
            }
            
            if (Vault){
            	RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            	RegisteredServiceProvider<Chat> rschat = getServer().getServicesManager().getRegistration(Chat.class);
            	RegisteredServiceProvider<Permission> rsperm = getServer().getServicesManager().getRegistration(Permission.class);
            	//Economy
                if (rsp == null) {
                	logger.warning("Vault found Economy, but for some reason cant be used.");
                } else {
                	econ = rsp.getProvider();
                	logger.info("Vault economy found. Hooked.");                	
                }
                //Chat
                if (rschat == null) {
                	logger.warning("Vault found chat, but for some reason cant be used.");
                } else {
                	chat = rschat.getProvider();
                	logger.info("Vault chat found. Hooked.");                	
                }
                //Perms
                if (rsperm == null) {
                	logger.warning("Vault found permissions, but for some reason cant be used.");
                } else {
                	perms = rsperm.getProvider();
                	logger.info("Vault perms found. Hooked.");                	
                }
            }
            
            logger.info("Init API module...");
            this.ucapi = new uChatAPI();
            
            for (Player p:getServer().getOnlinePlayers()){
            	if (config.getPlayerChannel(p) == null){
            		getConfig().getDefChannel().addMember(p);
            	}
            }
            
            //init other features
            registerJDA();            
            initAutomessage();
            
            getUCLogger().info("Server Version: "+getServer().getBukkitVersion());
            getUCLogger().logClear("\n"
            		+ "&b  _    _ _ _   _                 _        _____ _           _  \n"
            		+ " | |  | | | | (_)               | |      / ____| |         | |  \n"
            		+ " | |  | | | |_ _ _ __ ___   __ _| |_ ___| |    | |__   __ _| |_ \n"
            		+ " | |  | | | __| | '_ ` _ \\ / _` | __/ _ \\ |    | '_ \\ / _` | __|\n"
            		+ " | |__| | | |_| | | | | | | (_| | ||  __/ |____| | | | (_| | |_ \n"
            		+ "  \\____/|_|\\__|_|_| |_| |_|\\__,_|\\__\\___|\\_____|_| |_|\\__,_|\\__|\n"
            		+ "                                                                \n"
            		+ "&a"+getDescription().getFullName()+" enabled!\n");
            
            if (this.UCJDA != null){
            	this.UCJDA.sendRawToDiscord(lang.get("discord.start"));
    		} 
        } catch (Exception e){
        	e.printStackTrace();
        	super.setEnabled(false);
        }
	}
	
	protected void reload(){
		this.getServer().getScheduler().cancelTasks(UChat.get());
		try {
			this.config = new UCConfig(this);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
		this.lang = new UCLang();
		this.registerAliases();
		for (Player p:Bukkit.getOnlinePlayers()){
			if (this.getConfig().getPlayerChannel(p) == null){
				this.getConfig().getDefChannel().addMember(p);
			}
		}
		this.registerJDA();
		this.initAutomessage();
	}
	
	protected void registerJDA(){
		if (checkJDA()){
			this.logger.info("JDA LibLoader is present...");
			if (this.UCJDA != null){			
				this.UCJDA.shutdown();
				this.UCJDA = null;
			}
			if (config.getBool("discord.use")){
				this.UCJDA = new UCDiscord(this);
			}	
		}			
	}
	
	private void initAutomessage(){
		File am = new File(getDataFolder(),"automessages.yml");
		try {
			if (!am.exists()){
				am.createNewFile();
				this.amConfig.load(am);
			} else {
				this.amConfig.load(am);
			}
			getAMConfig().options().header("\n"
					+ "AutoMessages configuration for UltimateChat:\n"
					+ "\n"
					+ "You can use the placeholder {clicked} on \"onclick\" to get the name of player who clicked on message.\n"
					+ "\n"
					+ "This is the default configuration:\n"
					+ "\n"
					+ "interval: 60 #Interval in seconds.\n"
					+ "silent: true #Do not log the messages on console?"					
					+ "messages\n"
					+ "  '0': #The index (order) to show the messages.\n"
					+ "    minPlayers: 4 #Minimun players to show the message. Set to 0 to always send the message.\n"
					+ "    text: Your plain text message here! #Plain text.\n"
					+ "    hover: Your hover text message here! #Hover text.\n"
					+ "    onclick: Command on click here! #On click text with placeholder {clicked} to show who clicked.\n"
					+ "    suggest: Put the text to suggest on player chat on click.\n"
					+ "    url: http://google.com # Some url to go on click. Need to have \"http://\" to url work.\n"
					+ "\n"
					+ "*If you dont want hover message or click command, set to '' (blank quotes)\n"
					+ "\n");
			if (!getAMConfig().isConfigurationSection("messages")){
				getAMConfig().set("enable", true);
				getAMConfig().set("silent", true);
				getAMConfig().set("interval", 60);
				getAMConfig().set("messages.0.minPlayers", 4);
				getAMConfig().set("messages.0.text", "This is UChat Automessage! Put your plain text message here!");
				getAMConfig().set("messages.0.hover", "Your hover text message here!");
				getAMConfig().set("messages.0.onclick", "Command on click here!");
				getAMConfig().set("messages.0.suggest", "Text to suggest on click");
				getAMConfig().set("messages.0.url", "http://google.com");
			}		
			
			getAMConfig().save(am);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}		
		
		if (!getAMConfig().getBoolean("enable")){
			return;
		}
		
		int total = getAMConfig().getConfigurationSection("messages").getKeys(false).size();		
		int loop = getAMConfig().getInt("interval");
		boolean silent = getAMConfig().getBoolean("silent");
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(uchat, new Runnable(){
			@Override
			public void run() {
				if (getAMConfig().isConfigurationSection("messages."+index)){
					int plays = getAMConfig().getInt("messages."+index+".minPlayers");
					String text = getAMConfig().getString("messages."+index+".text", "");
					String hover = getAMConfig().getString("messages."+index+".hover", "");
					String onclick = getAMConfig().getString("messages."+index+".onclick", "");
					String suggest = getAMConfig().getString("messages."+index+".suggest", "");
					String url = getAMConfig().getString("messages."+index+".url", "");
					
					String cmd = text;
					if (hover.length() > 1){
						cmd = cmd+" "+getConfig().getString("broadcast.on-hover")+hover;
					}
					if (onclick.length() > 1){
						cmd = cmd+" "+getConfig().getString("broadcast.on-click")+onclick;
					}
					if (suggest.length() > 1){
						cmd = cmd+" "+getConfig().getString("broadcast.suggest")+suggest;
					}
					if (url.length() > 1){
						cmd = cmd+" "+getConfig().getString("broadcast.url")+url;
					}
					if (plays == 0 || getServer().getOnlinePlayers().size() >= plays){						
						UCUtil.sendBroadcast(getServer().getConsoleSender(), cmd.split(" "), silent);
					}
				}	
				if (index+1 >= total){
					index = 0;
				} else {
					index++;
				}
			}				
		}, loop*20, loop*20);
	}	
	
	public void onDisable() {
		if (this.UCJDA != null){
			this.UCJDA.sendRawToDiscord(lang.get("discord.stop"));
			this.UCJDA.shutdown();
		}
		getUCLogger().severe(getDescription().getFullName()+" disabled!");
	}
	
	private void registerAliases(){
		registerAliases("channel",config.getChAliases());
        registerAliases("tell",config.getTellAliases());
        registerAliases("umsg",config.getMsgAliases());
        if (config.getBool("broadcast.enable")){
        	registerAliases("ubroadcast",config.getBroadcastAliases());
        }
	}
	
	private void registerAliases(String name, List<String> aliases) {  
		List<String> aliases1 = new ArrayList<String>();
		aliases1.addAll(aliases);
		
		for (Command cmd:PluginCommandYamlParser.parse(uchat)){
			if (cmd.getName().equals(name)){
				cmd.setAliases(aliases1);
				cmd.setLabel(name);
				try {		        			        	
		        	Field field = SimplePluginManager.class.getDeclaredField("commandMap");
		            field.setAccessible(true);
		            CommandMap commandMap = (CommandMap)(field.get(getServer().getPluginManager()));		            
		            Method register = commandMap.getClass().getMethod("register", String.class, Command.class);
		            register.invoke(commandMap, cmd.getName(),cmd);
		            ((PluginCommand) cmd).setExecutor(new UCListener());
		        } catch(Exception e) {
		            e.printStackTrace();
		        }
			}			
		}        
    }	
	
	private boolean checkJDA(){
    	Plugin p = Bukkit.getPluginManager().getPlugin("JDALibLoaderBukkit");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
    	return false;
    }
	
	//check if plugin Vault is installed
    private boolean checkVault(){
    	Plugin p = Bukkit.getPluginManager().getPlugin("Vault");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
    	return false;
    }
    
	private boolean checkSC() {
		Plugin p = Bukkit.getPluginManager().getPlugin("SimpleClans");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
		return false;
	}
	
	private boolean checkMR() {
		Plugin p = Bukkit.getPluginManager().getPlugin("Marriage");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
		return false;
	}
	
	private boolean checkMM() {
		Plugin p = Bukkit.getPluginManager().getPlugin("MarriageMaster");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
		return false;
	}
	
	private boolean checkPL() {
		Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
		return false;
	}
	

	private boolean checkPHAPI() {
		Plugin p = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
		return false;
	}
	
	private boolean checkFac() {
		Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
    	if (p != null && p.isEnabled()){
    		return true;
    	}
		return false;
	}
}

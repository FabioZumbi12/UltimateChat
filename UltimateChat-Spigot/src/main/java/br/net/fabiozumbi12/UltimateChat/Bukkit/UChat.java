package br.net.fabiozumbi12.UltimateChat.Bukkit;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee.UChatBungee;
import br.net.fabiozumbi12.UltimateChat.Bukkit.Jedis.UCJedisLoader;
import br.net.fabiozumbi12.UltimateChat.Bukkit.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Bukkit.config.UCLang;
import com.lenis0012.bukkit.marriage2.Marriage;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UChat extends JavaPlugin {
	
    private static boolean Vault = false;
	static boolean SClans;
	static SimpleClans sc;
	static boolean MarryReloded;
	static boolean MarryMaster;
	private static boolean ProtocolLib;
	static MarriageMaster mm;
	static Marriage mapi;
	static boolean PlaceHolderAPI;
	static boolean Factions;
	private FileConfiguration amConfig;
	private int index = 0;	

	 HashMap<String,String> tempChannels = new HashMap<>();
	 HashMap<String,String> tellPlayers = new HashMap<>();
	 HashMap<String,String> tempTellPlayers = new HashMap<>();
	 HashMap<String,String> respondTell = new HashMap<>();
	protected  List<String> command = new ArrayList<>();
	 HashMap<String,List<String>> ignoringPlayer = new HashMap<>();
	 List<String> mutes = new ArrayList<>();
	public  List<String> isSpy = new ArrayList<>();
	 HashMap<String, Integer> timeMute = new HashMap<>();
	private UCListener listener;
	
	private HashMap<List<String>,UCChannel> channels;
	public HashMap<List<String>,UCChannel> getChannels(){
		return this.channels;
	}
	
	public void setChannels(HashMap<List<String>,UCChannel> channels){
		this.channels = channels;
	}

	private FileConfiguration getAMConfig(){
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
	public UCConfig getUCConfig(){
		return this.config;
	}
	
	private UCDInterface UCJDA;
	UCDInterface getUCJDA(){
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

	private boolean isRelation;
	public boolean isRelation(){
	    return this.isRelation;
    }

	private uChatAPI ucapi;
	public uChatAPI getAPI(){
		return this.ucapi;
	}
	
	private UCJedisLoader jedis;
	public UCJedisLoader getJedis(){
		return this.jedis;
	}
	
	public PluginDescriptionFile getPDF(){
		return this.getDescription();
	}
		
	public void onEnable() {
        try {
            uchat = this;
            logger = new UCLogger(this);
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
			listener = new UCListener();

            getServer().getPluginCommand("uchat").setExecutor(listener);
            getServer().getPluginManager().registerEvents(listener, this);
            getServer().getPluginManager().registerEvents(new UCChatProtection(), this);
            
            getServer().getMessenger().registerOutgoingPluginChannel(this, "uChat");
            getServer().getMessenger().registerIncomingPluginChannel(this, "uChat", new UChatBungee());
            
            //register aliases
            registerAliases();
            
            if (ProtocolLib){
            	logger.info("ProtocolLib found. Hooked.");
            }
            
            if (PlaceHolderAPI){
                try {
                    Class.forName("me.clip.placeholderapi.expansion.Relational");
                    if (new UCPlaceHoldersRelational(this).register()){
                        isRelation = true;
                        logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders with relational tag feature.");
                    }
                } catch (ClassNotFoundException ex){
                    if (new UCPlaceHolders(this).register()){
                        isRelation = false;
                        logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders.");
                    }
                }
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
            	sc = SimpleClans.getInstance();
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
            
            //init other features
            
            //Jedis
    		registerJedis();    		
            //JDA
            registerJDA();            
            
            initAutomessage();
            
            getUCLogger().info("Server Version: "+getServer().getBukkitVersion());
            getUCLogger().logClear(ChatColor.translateAlternateColorCodes('&',"\n"
            		+ "&b  _    _ _ _   _                 _        _____ _           _  \n"
            		+ " | |  | | | | (_)               | |      / ____| |         | |  \n"
            		+ " | |  | | | |_ _ _ __ ___   __ _| |_ ___| |    | |__   __ _| |_ \n"
            		+ " | |  | | | __| | '_ ` _ \\ / _` | __/ _ \\ |    | '_ \\ / _` | __|\n"
            		+ " | |__| | | |_| | | | | | | (_| | ||  __/ |____| | | | (_| | |_ \n"
            		+ "  \\____/|_|\\__|_|_| |_| |_|\\__,_|\\__\\___|\\_____|_| |_|\\__,_|\\__|\n"
            		+ "                                                                \n"
            		+ "&a"+getDescription().getFullName()+" enabled!\n"));
            
            if (this.UCJDA != null){
            	this.UCJDA.sendRawToDiscord(lang.get("discord.start"));
    		} 
        } catch (Exception e){
        	e.printStackTrace();
        	super.setEnabled(false);
        }
	}
	
	public void reload(){
		this.getServer().getScheduler().cancelTasks(this);
		try {
			this.config = new UCConfig(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.lang = new UCLang();
		this.registerAliases();
		
		this.registerJDA();
		this.registerJedis();
		this.initAutomessage();
	}

	private void registerJedis(){
		if (this.jedis != null){
			this.jedis.closePool();
			this.jedis = null;
		}
		if (getUCConfig().getBoolean("jedis.enable")){
			this.logger.info("Init REDIS...");
			try {
				this.jedis = new UCJedisLoader(getUCConfig().getString("jedis.ip"),
						getUCConfig().getInt("jedis.port"),
						getUCConfig().getString("jedis.pass"), new ArrayList<>(getChannels().values()));
			} catch (Exception e){
				e.printStackTrace();
				this.logger.warning("Could not connect to REDIS server! Check ip, password and port, and if the REDIS server is running.");
			}			
		}		
	}

	private void registerJDA(){
		if (checkJDA()){
			this.logger.info("JDA LibLoader is present...");
			if (this.UCJDA != null){			
				this.UCJDA.shutdown();
				this.UCJDA = null;
			}
			if (getUCConfig().getBoolean("discord.use")){
				this.UCJDA = new UCDiscord(this);
				if (!this.UCJDA.JDAAvailable()){
                    this.UCJDA = null;
                    this.logger.info("JDA is not available due errors before...");
				} else {
                    this.logger.info("JDA connected and ready to use!");
                }
			}	
		}			
	}
	
	private void initAutomessage(){
		File am = new File(getDataFolder(),"automessages.yml");
		try {
			if (!am.exists()){
				am.createNewFile();
			}
            this.amConfig.load(am);

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
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(uchat, () -> {
            if (getAMConfig().isConfigurationSection("messages."+index)){
                int plays = getAMConfig().getInt("messages."+index+".minPlayers");
                String text = getAMConfig().getString("messages."+index+".text", "");
                String hover = getAMConfig().getString("messages."+index+".hover", "");
                String onclick = getAMConfig().getString("messages."+index+".onclick", "");
                String suggest = getAMConfig().getString("messages."+index+".suggest", "");
                String url = getAMConfig().getString("messages."+index+".url", "");

                String cmd = text;
                if (hover.length() > 1){
                    cmd = cmd+" "+ getUCConfig().getString("broadcast.on-hover")+hover;
                }
                if (onclick.length() > 1){
                    cmd = cmd+" "+ getUCConfig().getString("broadcast.on-click")+onclick;
                }
                if (suggest.length() > 1){
                    cmd = cmd+" "+ getUCConfig().getString("broadcast.suggest")+suggest;
                }
                if (url.length() > 1){
                    cmd = cmd+" "+ getUCConfig().getString("broadcast.url")+url;
                }
                if (plays == 0 || getServer().getOnlinePlayers().size() >= plays){
                    UCUtil.sendBroadcast(cmd.split(" "), silent);
                }
            }
            if (index+1 >= total){
                index = 0;
            } else {
                index++;
            }
        }, loop*20, loop*20);
	}	
	
	public void onDisable() {
		if (this.jedis != null){
			this.jedis.closePool();
		}
		if (this.UCJDA != null){
			this.UCJDA.sendRawToDiscord(lang.get("discord.stop"));
			this.UCJDA.shutdown();
		}
		getUCLogger().severe(getDescription().getFullName()+" disabled!");
	}
	
	/** Needed to be called after register or unregister channels.
	 * 
	 */
	void registerAliases(){
		registerAliases("channel",getChAliases(), true);
        registerAliases("tell",config.getTellAliases(), true);
        registerAliases("umsg",config.getMsgAliases(), true);
        registerAliases("ubroadcast",config.getBroadcastAliases(), config.getBoolean("broadcast.enable"));
	}
	
	private void registerAliases(String name, List<String> aliases, boolean shouldReg) {
        List<String> aliases1 = new ArrayList<>(aliases);
		
		for (Command cmd:PluginCommandYamlParser.parse(uchat)){
			if (cmd.getName().equals(name)){
                if (shouldReg){
                    getServer().getPluginCommand(name).setExecutor(listener);
                    cmd.setAliases(aliases1);
                    cmd.setLabel(name);
                }
				try {		        			        	
		        	Field field = SimplePluginManager.class.getDeclaredField("commandMap");
		            field.setAccessible(true);
		            CommandMap commandMap = (CommandMap)(field.get(getServer().getPluginManager()));
		            if (shouldReg){
                        Method register = commandMap.getClass().getMethod("register", String.class, Command.class);
                        register.invoke(commandMap, cmd.getName(),cmd);
                        ((PluginCommand) cmd).setExecutor(listener);
                    } else if (getServer().getPluginCommand(name).isRegistered()){
                        getServer().getPluginCommand(name).unregister(commandMap);
                    }
		        } catch(Exception e) {
		            e.printStackTrace();
		        }
			}			
		}        
    }	
		
	//------- channels

	public UCChannel getChannel(String alias){		
		for (List<String> aliases:UChat.get().getChannels().keySet()){
			if (aliases.contains(alias.toLowerCase())){				
				return UChat.get().getChannels().get(aliases);
			}
		}
		return null;
	}

	List<String> getChAliases(){
        List<String> aliases = new ArrayList<>(Arrays.asList(config.getString("general.channel-cmd-aliases").replace(" ", "").split(",")));
		for (List<String> alias:UChat.get().getChannels().keySet()){
			aliases.addAll(alias);
		}
		return aliases;
	}
	
	public UCChannel getPlayerChannel(CommandSender p){
		for (UCChannel ch:UChat.get().getChannels().values()){
			if (ch.isMember(p)){
				return ch;
			}
		}
		return getDefChannel();
	}


	void unMuteInAllChannels(String player){
		for (UCChannel ch:UChat.get().getChannels().values()){
			if (ch.isMuted(player)){				
				ch.unMuteThis(player);
			}
		}
	}

	void muteInAllChannels(String player){
		for (UCChannel ch:UChat.get().getChannels().values()){
			if (!ch.isMuted(player)){				
				ch.muteThis(player);
			}
		}
	}

	UCChannel getDefChannel(){
		UCChannel ch = getChannel(config.getString("general.default-channel"));
		if (ch == null){
			UChat.get().getLogger().severe("Default channel not found with alias '"+config.getString("general.default-channel")+"'. Fix this setting to a valid channel alias.");			
		}
		return ch;
	}
		
	private boolean checkJDA(){
    	Plugin p = Bukkit.getPluginManager().getPlugin("JDALibLoaderBukkit");
		return p != null && p.isEnabled();
	}
	
	//check if plugin Vault is installed
    private boolean checkVault(){
    	Plugin p = Bukkit.getPluginManager().getPlugin("Vault");
		return p != null && p.isEnabled();
	}
    
	private boolean checkSC() {
		Plugin p = Bukkit.getPluginManager().getPlugin("SimpleClans");
		return p != null && p.isEnabled();
	}
	
	private boolean checkMR() {
		Plugin p = Bukkit.getPluginManager().getPlugin("Marriage");
		return p != null && p.isEnabled();
	}
	
	private boolean checkMM() {
		Plugin p = Bukkit.getPluginManager().getPlugin("MarriageMaster");
		return p != null && p.isEnabled();
	}
	
	private boolean checkPL() {
		Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
		return p != null && p.isEnabled();
	}

	private boolean checkPHAPI() {
		Plugin p = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
		return p != null && p.isEnabled();
	}
	
	private boolean checkFac() {
		Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
		return p != null && p.isEnabled();
	}
}

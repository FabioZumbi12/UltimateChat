package br.net.fabiozumbi12.UltimateChat.Sponge;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Sponge.Jedis.UCJedisLoader;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCLang;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.VersionData;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Plugin(id = "ultimatechat", 
name = "UltimateChat", 
version = VersionData.VERSION,
authors="FabioZumbi12", 
description="Complete and advanced chat plugin",
dependencies={
		@Dependency(id = "jdalibraryloader", optional = true),
		@Dependency(id = "nucleus", optional = true)})
public class UChat {
	
	private UCLogger logger;
	public UCLogger getLogger(){	
		return logger;
	}
		
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;
	public File configDir(){
		return this.configDir.toFile();
	}
	
	@Inject private Game game;	
	public Game getGame(){
		return this.game;
	}
	
	@Inject
	private PluginContainer plugin;	
	public PluginContainer instance(){
		return this.plugin;
	}
	
	private UCConfig config;
	public UCConfig getConfig(){
		return this.config;
	}
	
	private UCCommands cmds;
	private Server serv;
	private EconomyService econ;

	private UCPerms perms;
	public UCPerms getPerms(){
		return this.perms;
	}
	
	protected EconomyService getEco(){
		return this.econ;
	}
	
	public UCCommands getCmds(){
		return this.cmds;
	}
	
	private static UChat uchat;
	public static UChat get(){
		return uchat;
	}
	
	private uChatAPI ucapi;
	public uChatAPI getAPI(){
		return this.ucapi;
	}
	
	private UCDInterface UCJDA;	
	public UCDInterface getUCJDA(){
		return this.UCJDA;
	}
	
	private UCLang lang;
	public UCLang getLang(){
		return this.lang;
	}
	
	private UCVHelper helper;
	public UCVHelper getVHelper(){
		return this.helper;
	}
	
	private UCJedisLoader jedis;
	public UCJedisLoader getJedis(){
		return this.jedis;
	}
	
	private HashMap<List<String>,UCChannel> channels;
	public HashMap<List<String>,UCChannel> getChannels(){
		return this.channels;
	}
	
	public void setChannels(HashMap<List<String>,UCChannel> channels){
		this.channels = channels;
	}
	
	@Inject
    public GuiceObjectMapperFactory factory;
	
	protected static final HashMap<String,String> tempChannels = new HashMap<>();
	protected static final HashMap<String,String> tellPlayers = new HashMap<>();
	protected static final HashMap<String,String> tempTellPlayers = new HashMap<>();
	protected static final HashMap<String,String> respondTell = new HashMap<>();
	protected static final HashMap<String,List<String>> ignoringPlayer = new HashMap<>();
	protected final List<String> mutes = new ArrayList<>();
	public static final List<String> isSpy = new ArrayList<>();
	protected static final List<String> command = new ArrayList<>();
	protected final HashMap<String, Integer> timeMute = new HashMap<>();

	@Listener
    public void onServerStart(GamePostInitializationEvent event) {	
        try {
        	uchat = this;        	     	
        	this.serv = Sponge.getServer();
        	
        	//init logger
        	this.logger = new UCLogger(this.serv);
        	//init config
        	this.config = new UCConfig(factory);
    		//init lang
        	this.lang = new UCLang();

            //init perms
        	try {
                String v = this.game.getPlatform().getContainer(Component.API).getVersion().get();
                getLogger().info("Sponge version "+ v);

                if (v.startsWith("5") || v.startsWith("6")){
                    this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").newInstance();
                    this.helper = (UCVHelper)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper56").newInstance();
                }
                if (v.startsWith("7")){
                    this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms7").newInstance();
                    this.helper = (UCVHelper)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper7").newInstance();
                }
            } catch (Exception e){
        		switch (this.config.root().api.sponge_api){
        			case 5:
						this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").newInstance();
						this.helper = (UCVHelper)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper56").newInstance();
						getLogger().info("Permissions set to default classes for API 5");
					case 6:
						this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").newInstance();
						this.helper = (UCVHelper)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper56").newInstance();
						getLogger().info("Permissions set to default classes for API 6");
					case 7:
						this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms7").newInstance();
						this.helper = (UCVHelper)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper7").newInstance();
						getLogger().info("Permissions set to default classes for API 7");
				}
            }

            logger.info("Init commands module...");
    		this.cmds = new UCCommands(this);
    		
    		game.getEventManager().registerListeners(plugin, new UCListener());
    		
    		//init other features    		
    		//Jedis
    		registerJedis();    		
    		//JDA
    		registerJDA();  
    		
    		logger.info("Init API module...");
            this.ucapi = new uChatAPI();

            getLogger().logClear("\n"
            		+ "&b  _    _ _ _   _                 _        _____ _           _  \n"
            		+ " | |  | | | | (_)               | |      / ____| |         | |  \n"
            		+ " | |  | | | |_ _ _ __ ___   __ _| |_ ___| |    | |__   __ _| |_ \n"
            		+ " | |  | | | __| | '_ ` _ \\ / _` | __/ _ \\ |    | '_ \\ / _` | __|\n"
            		+ " | |__| | | |_| | | | | | | (_| | ||  __/ |____| | | | (_| | |_ \n"
            		+ "  \\____/|_|\\__|_|_| |_| |_|\\__,_|\\__\\___|\\_____|_| |_|\\__,_|\\__|\n"
            		+ "                                                                \n"
            		+ "&a"+plugin.getName()+" "+plugin.getVersion().get()+" enabled!\n");
            
            if (this.UCJDA != null){
            	this.UCJDA.sendRawToDiscord(lang.get("discord.start"));
    		}            
        } catch (Exception e){
        	e.printStackTrace();
        }
	}
	
	protected void reload() throws IOException{
		this.cmds.removeCmds();
		this.config = new UCConfig(factory);
		this.lang = new UCLang();
		this.cmds = new UCCommands(this);
				
		registerJedis();
		registerJDA();
	}
	
	protected void registerJedis(){
		if (this.jedis != null){
			this.jedis.closePool();
			this.jedis = null;
		}
		if (this.config.root().jedis.enable){
			this.logger.info("Init JEDIS...");	
			try {
				this.jedis = new UCJedisLoader(this.config.root().jedis.ip, 
						this.config.root().jedis.port, 
						this.config.root().jedis.pass, new ArrayList<>(getChannels().values()));
			} catch (Exception e){
				this.logger.warning("Could not connect to REDIS server! Check ip, password and port, and if the REDIS server is running.");
			}
		}		
	}
	
	protected void registerJDA(){
		if (checkJDA()){
			this.logger.info("JDA LibLoader is present...");
			if (this.UCJDA != null){			
				this.UCJDA.shutdown();
				this.UCJDA = null;
			}
			if (config.root().discord.use){
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
	
	private boolean checkJDA() {		
		return this.game.getPluginManager().getPlugin("jdalibraryloader").isPresent();
	}

	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
		
	@Listener
    public void onReloadPlugins(GameReloadEvent event) {
		try {
			reload();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Listener
	public void onStopServer(GameStoppingServerEvent e) {
		if (this.jedis != null){
			this.jedis.closePool();
		}
		if (this.UCJDA != null){
        	this.UCJDA.sendRawToDiscord(lang.get("discord.stop"));
		}		
	}		
	
	@Listener
	public void onStopServer(GameStoppedServerEvent e) {
		if (this.UCJDA != null){
        	this.UCJDA.shutdown();
		}		
		get().getLogger().severe(plugin.getName()+" disabled!");
	}
	
	//----------- channels
	public UCChannel getChannel(String alias){		
		for (List<String> aliases:UChat.get().getChannels().keySet()){
			if (aliases.contains(alias.toLowerCase())){				
				return UChat.get().getChannels().get(aliases);
			}
		}
		return null;
	}
	
	public void unMuteInAllChannels(String player){
		for (UCChannel ch:UChat.get().getChannels().values()){
			if (ch.isMuted(player)){				
				ch.unMuteThis(player);
            }
		}
	}
	
	public void muteInAllChannels(String player){
		for (UCChannel ch:UChat.get().getChannels().values()){
			if (!ch.isMuted(player)){				
				ch.muteThis(player);
            }
		}
	}
	
	public UCChannel getDefChannel(){
		UCChannel ch = getChannel(getConfig().root().general.default_channel);
		if (ch == null){
			UChat.get().getLogger().warning("Defalt channel not found with alias '"+getConfig().root().general.default_channel+"'. Fix this setting to a valid channel alias.");
		}
		return ch;
	}
	
	public List<String> getChAliases(){
		List<String> aliases = new ArrayList<>();
		for (List<String> alias:UChat.get().getChannels().keySet()){
			if (alias == null){
				continue;
			}
			aliases.addAll(alias);
		}
		return aliases;
	}
	
	public UCChannel getPlayerChannel(Player p){
		for (UCChannel ch:UChat.get().getChannels().values()){
			if (ch.isMember(p)){
				return ch;
			}
		}
		return getDefChannel();
	}
	
}

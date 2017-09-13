package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.api.Game;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
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

import br.net.fabiozumbi12.UltimateChat.Sponge.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCLang;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.VersionData;

import com.google.inject.Inject;

@Plugin(id = "ultimatechat", 
name = "UltimateChat", 
version = VersionData.VERSION,
authors="FabioZumbi12", 
description="Complete and advanced chat plugin",
dependencies={
		@Dependency(id = "jdalibraryloader", optional = true)})
public class UChat {
	
	private UCLogger logger;
	public UCLogger getLogger(){	
		return logger;
	}
		
	private String configDir;
	public String configDir(){
		return this.configDir;
	}
	
	@Inject private Game game;	
	public Game getGame(){
		return this.game;
	}
	
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
	protected UCPerms getPerms(){
		return this.perms;
	}
	
	protected EconomyService getEco(){
		return this.econ;
	}
	
	protected UCCommands getCmds(){
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
	
	private UCDiscord UCJDA;	
	public UCDiscord getUCJDA(){
		return this.UCJDA;
	}
	
	private UCLang lang;
	public UCLang getLang(){
		return this.lang;
	}
	
	//public HashMap<String,String> pChannels = new HashMap<String,String>();
	protected static HashMap<String,String> tempChannels = new HashMap<String,String>();
	protected static HashMap<String,String> tellPlayers = new HashMap<String,String>();
	protected static HashMap<String,String> tempTellPlayers = new HashMap<String,String>();
	protected static HashMap<String,String> respondTell = new HashMap<String,String>();
	protected static HashMap<String,List<String>> ignoringPlayer = new HashMap<String,List<String>>();
	protected static List<String> mutes = new ArrayList<String>();
	protected static List<String> isSpy = new ArrayList<String>();	
	protected static List<String> command = new ArrayList<String>();
		
	@Listener
    public void onServerStart(GamePostInitializationEvent event) {	
        try {
        	plugin = Sponge.getPluginManager().getPlugin("ultimatechat").get();
        	uchat = this;   
        	this.configDir = game.getConfigManager().getSharedConfig(plugin).getDirectory()+File.separator+plugin.getName()+File.separator;        	     	
        	this.serv = Sponge.getServer();
        	
        	//init logger
        	this.logger = new UCLogger(this.serv);
        	//init config
        	this.config = new UCConfig(this);
    		//init lang
        	this.lang = new UCLang();
            //init perms
            String v = this.game.getPlatform().getContainer(Component.API).getVersion().get();
            if (v.startsWith("5") || v.startsWith("6")){
            	this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").newInstance();
            }
            if (v.startsWith("7")){
            	this.perms = (UCPerms)Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms7").newInstance();
            }
            
            logger.info("Init commands module...");
    		this.cmds = new UCCommands(this);
    		
    		game.getEventManager().registerListeners(plugin, new UCListener());         
                     
    		//init other features
    		registerJDA();  
    		
    		logger.info("Init API module...");
            this.ucapi = new uChatAPI();
            
            for (Player p:serv.getOnlinePlayers()){
            	if (config.getPlayerChannel(p) == null){
            		getConfig().getDefChannel().addMember(p);
            	}
            }
            
            getLogger().info("Sponge version "+this.game.getPlatform().getContainer(Component.API).getVersion().get());
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
		this.config = new UCConfig(this);
		this.lang = new UCLang();
		this.cmds = new UCCommands(this);
		for (Player p:serv.getOnlinePlayers()){
			if (config.getPlayerChannel(p) == null){
        		getConfig().getDefChannel().addMember(p);
        	}					 
		}		
		registerJDA();
	}
	
	protected void registerJDA(){
		if (checkJDA()){
			this.logger.info("JDA LibLoader is present...");
			if (this.UCJDA != null){			
				this.UCJDA.shutdown();
				this.UCJDA = null;
			}
			if (config.getBool("discord","use")){
				this.UCJDA = new UCDiscord(this);
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
}

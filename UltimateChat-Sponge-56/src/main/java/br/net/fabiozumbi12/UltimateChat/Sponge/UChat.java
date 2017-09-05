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
dependencies=@Dependency(id = "nucleus", optional = true))
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
	
	public static PluginContainer plugin;	
	
	private UCConfig cfgs;
	public UCConfig getConfig(){
		return this.cfgs;
	}
	
	private UCCommands cmds;

	private Server serv;

	private EconomyService econ;

	private UCPerms perms;
	public UCPerms getPerms(){
		return this.perms;
	}
	
	public EconomyService getEco(){
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
	
	//public HashMap<String,String> pChannels = new HashMap<String,String>();
	public static HashMap<String,String> tempChannels = new HashMap<String,String>();
	public static HashMap<String,String> tellPlayers = new HashMap<String,String>();
	public static HashMap<String,String> tempTellPlayers = new HashMap<String,String>();
	public static HashMap<String,String> respondTell = new HashMap<String,String>();
	public static HashMap<String,List<String>> ignoringPlayer = new HashMap<String,List<String>>();
	public static List<String> mutes = new ArrayList<String>();
	public static List<String> isSpy = new ArrayList<String>();	
	public static List<String> command = new ArrayList<String>();
		
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
        	this.cfgs = new UCConfig(this);
    		//init lang
            UCLang.init();
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
                     
    		logger.info("Init API module...");
            this.ucapi = new uChatAPI();
            
            for (Player p:serv.getOnlinePlayers()){
            	if (cfgs.getPlayerChannel(p) == null){
            		getConfig().getDefChannel().addMember(p);
            	}
            }
            
            get().getLogger().info("Sponge version "+this.game.getPlatform().getContainer(Component.API).getVersion().get());
            get().getLogger().sucess(plugin.getName()+" "+plugin.getVersion().get()+" enabled!");
            
        } catch (Exception e){
        	e.printStackTrace();
        }
	}
	
	@Listener
	public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
		if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
		}
	}
	
	public void reload() throws IOException{
		this.cmds.removeCmds();
		this.cfgs = new UCConfig(this);
		UCLang.init();
		this.cmds = new UCCommands(this);
		for (Player p:serv.getOnlinePlayers()){
			if (cfgs.getPlayerChannel(p) == null){
        		getConfig().getDefChannel().addMember(p);
        	}					 
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
		get().getLogger().severe(plugin.getName()+" disabled!");
	}		
}

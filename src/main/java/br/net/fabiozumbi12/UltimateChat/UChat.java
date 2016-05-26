package br.net.fabiozumbi12.UltimateChat;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import br.net.fabiozumbi12.UltimateChat.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.config.UCLang;

import com.lenis0012.bukkit.marriage2.Marriage;
import com.lenis0012.bukkit.marriage2.MarriageAPI;

public class UChat extends JavaPlugin {
	
    private static boolean Vault = false;
	public static UChat plugin;
    public static UCLogger logger;
    public static Server serv;
    public static PluginDescriptionFile pdf;
    public static UCLang lang;
	public static UCConfig config;
	public static String mainPath;
	public static Economy econ;
	public static Chat chat;
	public static Permission perms;
	public static boolean SClans;
	public static ClanManager sc;
	public static boolean MarryReloded;
	public static boolean MarryMaster;
	public static boolean ProtocolLib;
	public static MarriageMaster mm;
	public static Marriage mapi;

	public void onEnable() {
        try {
            plugin = this;
            logger = new UCLogger();
            serv = getServer();
            pdf = getDescription();
            mainPath = "plugins" + File.separator + pdf.getName() + File.separator;
            config = new UCConfig(this, mainPath);
            lang = new UCLang(this, logger, mainPath, config);
            
            //check hooks
            Vault = checkVault();            
            SClans = checkSC();
            MarryReloded = checkMR();
            MarryMaster = checkMM();
            ProtocolLib = checkPL();
            
            serv.getPluginCommand("uchat").setExecutor(new UCListener());
            serv.getPluginManager().registerEvents(new UCListener(), this);
            serv.getPluginManager().registerEvents(new UCChatProtection(), this);
            
            //register aliases
            registerAliases();
            
            if (ProtocolLib){
            	logger.info("ProtocolLib found. Hooked.");
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
            
            for (Player p:serv.getOnlinePlayers()){
            	if (!UCMessages.pChannels.containsKey(p.getName())){
            		UCMessages.pChannels.put(p.getName(), UChat.config.getDefChannel().getAlias());
            	}
            }
            UChat.logger.sucess(pdf.getFullName()+" enabled!");
            
        } catch (Exception e){
        	e.printStackTrace();
        	super.setEnabled(false);
        }
	}
	
	public void onDisable() {
		UChat.logger.severe(pdf.getFullName()+" disabled!");
	}
	
	public static void registerAliases(){
		registerAliases("channel",config.getChAliases());
        registerAliases("tell",config.getTellAliases());
	}
	private static void registerAliases(String name, List<String> aliases) {  
		List<String> aliases1 = new ArrayList<String>();
		aliases1.addAll(aliases);
		for (Command cmd:PluginCommandYamlParser.parse(plugin)){
			if (cmd.getName().equals(name)){
				cmd.setAliases(aliases1);
				cmd.setLabel(name);
				try {		        			        	
		        	Field field = SimplePluginManager.class.getDeclaredField("commandMap");
		            field.setAccessible(true);
		            CommandMap commandMap = (CommandMap)(field.get(serv.getPluginManager()));		            
		            Method register = commandMap.getClass().getMethod("register", String.class, Command.class);
		            register.invoke(commandMap, cmd.getName(),cmd);
		            ((PluginCommand) cmd).setExecutor(new UCListener());
		        } catch(Exception e) {
		            e.printStackTrace();
		        }
			}			
		}        
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
}

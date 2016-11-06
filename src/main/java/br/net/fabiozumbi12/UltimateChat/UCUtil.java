package br.net.fabiozumbi12.UltimateChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.net.fabiozumbi12.UltimateChat.Fanciful.FancyMessage;
import br.net.fabiozumbi12.UltimateChat.config.TaskChain;

public class UCUtil {

	public static String colorize(String msg){
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static void performCommand(final CommandSender consoleCommandSender, final String command) {
	    TaskChain.newChain().add(new TaskChain.GenericTask() {
	        public void run() {
	        	UChat.plugin.serv.dispatchCommand(consoleCommandSender,command);
	        }
	    }).execute();
	}
	
	public static boolean sendBroadcast(CommandSender sender, String[] args, boolean silent){
		StringBuilder message = new StringBuilder();
		 StringBuilder hover = new StringBuilder();
		 StringBuilder cmdline = new StringBuilder();
		 StringBuilder url = new StringBuilder();
		 boolean isHover = false;
		 boolean isCmd = false;
		 boolean isUrl = false;
		 for (String arg:args){
			 if (arg.contains(UChat.config.getString("broadcast.on-hover"))){
				 hover.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.config.getString("broadcast.on-hover"), "")));
				 isHover = true;
				 isCmd = false;
				 isUrl = false;
				 continue;
			 }
			 if (arg.contains(UChat.config.getString("broadcast.on-click"))){
				 cmdline.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.config.getString("broadcast.on-click"), "")));
				 isCmd = true;
				 isHover = false;
				 isUrl = false;
				 continue;
			 }
			 if (arg.contains(UChat.config.getString("broadcast.url"))){
				 url.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.config.getString("broadcast.url"), "")));
				 isCmd = false;
				 isHover = false;
				 isUrl = true;
				 continue;
			 }
			 
			 if (isCmd){
				 cmdline.append(" "+ChatColor.translateAlternateColorCodes('&', arg));
			 } else
			 if (isHover){
				 hover.append(" "+ChatColor.translateAlternateColorCodes('&', arg));
			 } else
			 if (isUrl){
				 url.append(" "+ChatColor.translateAlternateColorCodes('&', arg));
			 } else {
				 message.append(" "+ChatColor.translateAlternateColorCodes('&', arg));
			 }
		 }
		 
		 if (message.toString().length() <= 1){			 
			 return false;
		 }
		 
		 if (!silent){
			 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> Broadcast: "+ChatColor.RESET+message.toString().substring(1));
		 }		 
		 			 
		 if (UChat.config.getBool("general.hover-events")){
			 FancyMessage fanci = new FancyMessage();
			 fanci.text(message.toString().substring(1), "message");
			 
			 if (hover.toString().length() > 1){
				 fanci.tooltip(hover.toString().substring(1));
				 if (!silent){
					 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> OnHover: "+ChatColor.RESET+hover.toString().substring(1));
				 }
			 }				 
			 
			 if (cmdline.toString().length() > 1 && !silent){
				 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> OnClick: "+ChatColor.RESET+cmdline.toString().substring(1));
			 }				 
			 
			 if (url.toString().length() > 1){
				 fanci.link(url.toString().substring(1));
				 if (!silent){
					 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> Url: "+ChatColor.RESET+url.toString().substring(1));
				 }				  
			 }	
			 
			 for (Player p:Bukkit.getOnlinePlayers()){
				 if (cmdline.toString().length() > 1){
					 fanci.command("/"+cmdline.toString().substring(1).replace("{clicked}", p.getName()));						 
				 }
				 fanci.send(p);
			 }
		 } else {
			 for (Player p:Bukkit.getOnlinePlayers()){
				 p.sendMessage(message.toString().substring(1));
			 }				 
		 }
		 return true;
	}
}

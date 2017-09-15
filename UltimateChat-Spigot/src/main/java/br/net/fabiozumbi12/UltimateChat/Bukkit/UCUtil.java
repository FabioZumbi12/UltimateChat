package br.net.fabiozumbi12.UltimateChat.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UCUtil {
		
	public static int getBukkitVersion(){
    	String name = Bukkit.getServer().getClass().getPackage().getName();
		String v = name.substring(name.lastIndexOf('.') + 1) + ".";
    	String[] version = v.replace('_', '.').split("\\.");
		
		int lesserVersion = 0;
		try {
			lesserVersion = Integer.parseInt(version[2]);
		} catch (NumberFormatException ex){				
		}
		return Integer.parseInt((version[0]+version[1]).substring(1)+lesserVersion);
    }
	
	public static String capitalize(String text){
		StringBuilder cap = new StringBuilder();
		text = text.replace("_", " ");
		for (String t:text.split(" ")){
			if (t.length() > 2){
				cap.append(t.substring(0, 1).toUpperCase() + t.substring(1)+" ");
			} else {
				cap.append(t+" ");
			}						
		}
		return cap.substring(0, cap.length()-1);
	}
	
	public static String colorize(String msg){
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static void saveResource(String name, File saveTo){
		try {
			InputStream isReader = UChat.class.getResourceAsStream(name);
			FileOutputStream fos = new FileOutputStream(saveTo);
			while (isReader.available() > 0) {  // write contents of 'is' to 'fos'
		        fos.write(isReader.read());
		    }
		    fos.close();
		    isReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void performCommand(final Player to, final CommandSender sender, final String command) {
		Bukkit.getScheduler().runTask(UChat.get(), new Runnable(){
			@Override
			public void run() {
				if (to == null || (to != null && to.isOnline())){
	        		UChat.get().getServ().dispatchCommand(sender, command);
	        	}
			}			
		});
	}
	
	public static void sendUmsg(CommandSender sender, String[] args){
		if (args.length < 2){
			sender.sendMessage(UChat.get().getLang().get("help.cmd.umsg"));
			return;
		}
		
		Player receiver = Bukkit.getPlayer(args[0]);
		if (receiver == null){
			sender.sendMessage(UChat.get().getLang().get("listener.invalidplayer"));
			return;
		}
		StringBuilder ssmsgs = new StringBuilder();	
		for (String st:args){
			ssmsgs.append(st+" ");
		}		
		String msgs = UCUtil.colorize(ssmsgs.toString().substring((args[0]).length()+1));
		
		receiver.sendMessage(msgs);
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> Private to "+ChatColor.GOLD+receiver.getName()+ChatColor.DARK_GRAY+": "+ChatColor.RESET+msgs);
	}
	
	public static boolean sendBroadcast(CommandSender sender, String[] args, boolean silent){
		StringBuilder message = new StringBuilder();
		 StringBuilder hover = new StringBuilder();
		 StringBuilder cmdline = new StringBuilder();
		 StringBuilder url = new StringBuilder();
		 StringBuilder suggest = new StringBuilder();
		 boolean isHover = false;
		 boolean isCmd = false;
		 boolean isUrl = false;
		 boolean isSug = false;
		 for (String arg:args){
			 if (arg.contains(UChat.get().getUCConfig().getString("broadcast.on-hover"))){
				 hover.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.on-hover"), "")));
				 isHover = true;
				 isCmd = false;
				 isUrl = false;
				 isSug = false;
				 continue;
			 }
			 if (arg.contains(UChat.get().getUCConfig().getString("broadcast.on-click"))){
				 cmdline.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.on-click"), "")));
				 isCmd = true;
				 isHover = false;
				 isUrl = false;
				 isSug = false;
				 continue;
			 }
			 if (arg.contains(UChat.get().getUCConfig().getString("broadcast.url"))){
				 url.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.url"), "")));
				 isCmd = false;
				 isHover = false;
				 isUrl = true;
				 isSug = false;
				 continue;
			 }
			 if (arg.contains(UChat.get().getUCConfig().getString("broadcast.suggest"))){
				 suggest.append(" "+ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.suggest"), "")));
				 isCmd = false;
				 isHover = false;
				 isUrl = false;
				 isSug = true;
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
			 } else 
		     if (isSug){
		    	 suggest.append(" "+ChatColor.translateAlternateColorCodes('&', arg));
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
		 			 
		 if (UChat.get().getUCConfig().getBool("general.json-events")){
			 UltimateFancy fanci = new UltimateFancy();
			 fanci.text(message.toString().substring(1));
			 
			 if (hover.toString().length() > 1){
				 fanci.hoverShowText(hover.toString().substring(1));
				 if (!silent){
					 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> OnHover: "+ChatColor.RESET+hover.toString().substring(1));
				 }
			 }				 
			 
			 if (cmdline.toString().length() > 1 && !silent){
				 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> OnClick: "+ChatColor.RESET+cmdline.toString().substring(1));
			 }				 
			 
			 if (url.toString().length() > 1){
				 fanci.clickOpenURL(url.toString().substring(1));
				 if (!silent){
					 Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY+"> Url: "+ChatColor.RESET+url.toString().substring(1));
				 }				  
			 }	
			 
			 for (Player p:Bukkit.getOnlinePlayers()){
				 if (cmdline.toString().length() > 1){
					 fanci.clickRunCmd("/"+cmdline.toString().substring(1).replace("{clicked}", p.getName()));						 
				 }
				 if (suggest.toString().length() > 1){
					 fanci.clickSuggestCmd(suggest.toString().substring(1).replace("{clicked}", p.getName()));						 
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

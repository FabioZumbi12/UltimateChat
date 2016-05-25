package br.net.fabiozumbi12.UltimateChat;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class UCPerms {

	public static boolean cmdPerm(CommandSender p, String cmd){
		return hasPerm(p, "cmd."+cmd);
	}
	
	public static boolean channelPerm(CommandSender p, UCChannel ch){
		UCChannel defCh = UChat.config.getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase());
	}
	
	public static boolean hasPerm(CommandSender p, String perm){
		return (p instanceof ConsoleCommandSender) || p.isOp() || p.hasPermission("uchat."+perm);
	}
}

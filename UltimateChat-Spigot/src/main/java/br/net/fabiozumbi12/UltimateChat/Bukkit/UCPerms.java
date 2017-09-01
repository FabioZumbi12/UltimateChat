package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class UCPerms {

	public static boolean cmdPerm(CommandSender p, String cmd){
		return hasPerm(p, "cmd."+cmd);
	}
	
	public static boolean channelReadPerm(CommandSender p, UCChannel ch){
		UCChannel defCh = UChat.get().getUCConfig().getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".read");
	}
	
	public static boolean channelSendPerm(CommandSender p, UCChannel ch){
		UCChannel defCh = UChat.get().getUCConfig().getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".write");
	}
	
	public static boolean canIgnore(CommandSender sender, Object toignore){
		if (toignore instanceof CommandSender && isAdmin((CommandSender)toignore)){
			return false;
		} else {
			return !sender.hasPermission("uchat.cant-ignore."+ (toignore instanceof Player?((Player)toignore).getName():((UCChannel)toignore).getName()));
		}
	}
	
	public static boolean hasPerm(CommandSender p, String perm){
		return isAdmin(p) || p.hasPermission("uchat."+perm);
	}
	
	private static boolean isAdmin(CommandSender p){
		return (p instanceof ConsoleCommandSender) || p.isOp() || p.hasPermission("uchat.admin");
	}
}

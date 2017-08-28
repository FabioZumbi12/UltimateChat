package br.net.fabiozumbi12.UltimateChat;

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
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".send");
	}
	
	public static boolean canIgnore(CommandSender sender, Object toignore){
		if ((sender instanceof ConsoleCommandSender) || sender.isOp() ||  sender.hasPermission("uchat.admin")){
			return true;
		} else {
			return !sender.hasPermission("uchat.cant-ignore."+ (toignore instanceof Player?((Player)toignore).getName():((UCChannel)toignore).getName()));
		}
	}
	
	public static boolean hasPerm(CommandSender p, String perm){
		return (p instanceof ConsoleCommandSender) || p.isOp() || p.hasPermission("uchat."+perm) || p.hasPermission("uchat.admin");
	}
}

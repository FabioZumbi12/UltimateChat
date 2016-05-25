package br.net.fabiozumbi12.UltimateChat;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import br.net.fabiozumbi12.UltimateChat.config.TaskChain;

public class UCUtil {

	public static String colorize(String msg){
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public static void performCommand(final ConsoleCommandSender consoleCommandSender, final String command) {
	    TaskChain.newChain().add(new TaskChain.GenericTask() {
	        public void run() {
	        	UChat.serv.dispatchCommand(consoleCommandSender,command);
	        }
	    }).execute();
	}
}

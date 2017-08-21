package br.net.fabiozumbi12.UltimateChat;

import org.spongepowered.api.Server;
import org.spongepowered.api.command.source.ConsoleSource;

public class UCLogger{
	
	private ConsoleSource console;
	
	UCLogger(Server serv){
		this.console = serv.getConsole();
	}
	 
	public void logClear(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"]"));
    }
	
	public void sucess(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"]"));
    }
    
    public void warning(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"]"));
    }
    
    public void debug(String s) {
        if (UChat.get().getConfig() != null && UChat.get().getConfig().getBool("debug-messages")) {
        	console.sendMessage(UCUtil.toText("UltimateChat: [&b"+s+"&r]"));
        }  
    }
}

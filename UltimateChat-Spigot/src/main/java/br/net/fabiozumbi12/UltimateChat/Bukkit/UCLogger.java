package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.ChatColor;

public class UCLogger{
	
	public enum timingType {
		START, END
	}
	private long start = 0;
	private UChat uchat;
	 
	public UCLogger(UChat uChat) {
		this.uchat = uChat;
	}

	public void logClear(String s) {
		uchat.getServ().getConsoleSender().sendMessage("UltimateChat: ["+s+"]");
    }
	
	public void sucess(String s) {
		uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: ["+s+"]"));
    }
    
    public void warning(String s) {
    	uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: ["+s+"]"));
    }
    
    public void debug(String s) {
        if (UChat.get().getUCConfig() != null && UChat.get().getUCConfig().getBool("debug.messages")) {
        	uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "UltimateChat: [&b"+s+"&r]"));
        }  
    }
    
    public void timings(timingType type, String message) {
        if (UChat.get().getUCConfig() != null && UChat.get().getUCConfig().getBool("debug.timings")) {
        	switch (type){
        	case START:
        		long diff = 0;
        		if (System.currentTimeMillis()-start > 5000) start = 0;
        		if (start != 0){
        			diff = System.currentTimeMillis()-start;
        		}        		
        		start = System.currentTimeMillis();
        		uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3UC Timings - "+type+": "+diff+"ms ("+message+"&3)"));
        		break;
			case END:
				uchat.getServ().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3UC Timings - "+type+": "+(System.currentTimeMillis()-start)+"ms ("+message+"&3)"));
				break;
			default:
				break;        		
        	}
        }  
    }
}

package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.Server;
import org.spongepowered.api.command.source.ConsoleSource;

public class UCLogger{
	
	public enum timingType {
		START, END
	}
	private long start = 0;
	private ConsoleSource console;
	
	UCLogger(Server serv){
		this.console = serv.getConsole();
	}
	 
	public void logClear(String s) {
    	console.sendMessage(UCUtil.toText(s));
    }
	
	public void sucess(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&a&l"+s+"&r]"));
    }
	
    public void info(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"&r]"));
    }
    
    public void warning(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&6"+s+"&r]"));
    }
    
    public void severe(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: [&c&l"+s+"&r]"));
    }
    
    public void log(String s) {
    	console.sendMessage(UCUtil.toText("UltimateChat: ["+s+"&r]"));
    }
    
    public void debug(String s) {
        if (UChat.get().getConfig() != null && UChat.get().getConfig().root().debug.messages) {
        	console.sendMessage(UCUtil.toText("UltimateChat: [&b"+s+"&r]"));
        }  
    }
    
    public void timings(timingType type, String message) {
        if (UChat.get().getConfig() != null && UChat.get().getConfig().root().debug.timings) {
        	switch (type){
        	case START:
        		long diff = 0;
        		if (System.currentTimeMillis()-start > 5000) start = 0;
        		if (start != 0){
        			diff = System.currentTimeMillis()-start;
        		}        		
        		start = System.currentTimeMillis();
        		console.sendMessage(UCUtil.toText("&3UC Timings - "+type+": "+diff+"ms ("+message+"&3)&r"));
        		break;
			case END:
				console.sendMessage(UCUtil.toText("&3UC Timings - "+type+": "+(System.currentTimeMillis()-start)+"ms ("+message+"&3)&r"));
				break;
			default:
				break;        		
        	}
        }  
    }
}

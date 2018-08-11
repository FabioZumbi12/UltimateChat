package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.stream.Collectors;

public class UCProxy extends Plugin implements Listener {

	@Override
    public void onEnable() {
		getProxy().registerChannel("bungee:uchat");
		getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("UChat Bungee enabled!");
    }
	
	@EventHandler	
	public void onPluginMessage(PluginMessageEvent e) {
		if (!e.getTag().equals("bungee:uchat")){
			return;
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
	    DataInputStream in = new DataInputStream(stream);
	    String id = "";
	    String ch = "";
	    String json = "";
	    try {
	    	id = in.readUTF();
	    	ch = in.readUTF();
	    	json = in.readUTF();
	    } catch (IOException ex){
	    	ex.printStackTrace();
	    }
	    sendMessage(ch, json, id);
	}
	
	public void sendMessage(String ch, String json, String id){
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    DataOutputStream out = new DataOutputStream(stream);
	    try {
	    	out.writeUTF(id);
	    	out.writeUTF(ch);
	        out.writeUTF(json);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    for (ServerInfo si:getProxy().getServers().values().stream().filter(si -> !si.getPlayers().isEmpty()).collect(Collectors.toList())){
			si.sendData("bungee:uchat", stream.toByteArray());
	    }	    
	}
}

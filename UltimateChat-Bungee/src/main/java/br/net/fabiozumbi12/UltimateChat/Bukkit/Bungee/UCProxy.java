package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class UCProxy extends Plugin implements Listener {

	@Override
    public void onEnable() {
		getProxy().registerChannel("uChat");
		getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("UChat Bungee enabled!");
    }
	
	@EventHandler	
	public void onPluginMessage(PluginMessageEvent e) {
		if (!e.getTag().equals("uChat")){
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
	    sendMessage(e.getSender(), ch, json, id); 
	}
	
	public void sendMessage(Connection server, String ch, String json, String id){	    
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    DataOutputStream out = new DataOutputStream(stream);
	    try {
	    	out.writeUTF(id);
	    	out.writeUTF(ch);
	        out.writeUTF(json);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    for (ServerInfo si:getProxy().getServers().values()){
	    	si.sendData("uChat", stream.toByteArray());
	    }	    
	}
}

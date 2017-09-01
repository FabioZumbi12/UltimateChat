package br.net.fabiozumbi12.UltimateChat.Bukkit.Bungee;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.Server;
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
		/*if (!(e.getSender() instanceof Server)) {
			return;
		}*/
		ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
	    DataInputStream in = new DataInputStream(stream);
	    String ch = "";
	    String sender = "";
	    String msg = "";
	    String ws = "";	    
	    try {
	    	ch = in.readUTF();
	    	sender = in.readUTF();
	    	msg = in.readUTF();
	    	ws = in.readUTF();
	    } catch (IOException ex){
	    	ex.printStackTrace();
	    }
	    sendMessage(e.getSender(), ch, sender, msg, ws); 
	}
	
	public void sendMessage(Connection server, String ch, String sender, String msg, String ws){	    
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    DataOutputStream out = new DataOutputStream(stream);
	    try {
	    	out.writeUTF(ch);
	        out.writeUTF(sender);
	        out.writeUTF(msg);
	        out.writeUTF(ws+","+((Server)server).getInfo().getName());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    for (ServerInfo si:getProxy().getServers().values()){
	    	si.sendData("uChat", stream.toByteArray());
	    	//getLogger().info("Sent message to "+si.getName());
	    	/*
	        if (!si.getName().equals(((Server)server).getInfo().getName())) {
	        	si.sendData("uChat", stream.toByteArray());
	        }*/
	    }	    
	}
}

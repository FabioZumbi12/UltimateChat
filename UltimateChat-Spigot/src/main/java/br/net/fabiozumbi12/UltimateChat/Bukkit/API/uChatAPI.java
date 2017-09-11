package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.entity.Player;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;

public class uChatAPI{

	public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages){
		if (UChat.get().getUCConfig().getString("tags."+tagName+".format") == null){
			UChat.get().getUCConfig().setConfig("tags."+tagName+".format", format);
			UChat.get().getUCConfig().setConfig("tags."+tagName+".click-cmd", clickCmd);
			UChat.get().getUCConfig().setConfig("tags."+tagName+".hover-messages", hoverMessages);
			UChat.get().getUCConfig().save();
			return true;
		}
		return false;
	}
	
	public boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance, String color, String tagBuilder, boolean needFocus, boolean receiverMsg, double cost, String ddmode, String ddformat, String ddhover, boolean ddallowcmds, boolean bungee) throws IOException{
		if (UChat.get().getUCConfig().getChannel(chName) != null){
			return false;
		}
		if (tagBuilder == null || tagBuilder.equals("")){
			tagBuilder = UChat.get().getUCConfig().getString("general.default-tag-builder");			
		}
		UCChannel ch = new UCChannel(chName, chAlias, crossWorlds, distance, color, tagBuilder, needFocus, receiverMsg, cost, bungee, false, false, "player", "", new ArrayList<String>(), new String(), ddmode, ddformat, ddhover, ddallowcmds, true);		
		UChat.get().getUCConfig().addChannel(ch);		
		return true;
	}	
	
	public UCChannel getChannel(String chName){
		return UChat.get().getUCConfig().getChannel(chName);
	}
	
	public UCChannel getPlayerChannel(Player player){
		return UChat.get().getUCConfig().getPlayerChannel(player);
	}
	
	public Collection<UCChannel> getChannels(){
		return UChat.get().getUCConfig().getChannels();
	}
	
	public Chat getVaultChat(){
		return UChat.get().getVaultChat();
	}
	
	public Economy getVaultEco(){
		return UChat.get().getVaultEco();
	}
	
	public Permission getVaultPerms(){
		return UChat.get().getVaultPerms();
	}
}

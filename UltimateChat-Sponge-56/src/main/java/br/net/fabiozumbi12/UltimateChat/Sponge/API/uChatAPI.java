package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.entity.living.player.Player;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;

public class uChatAPI {
	
	public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages){
		if (UChat.get().getConfig().getString("tags",tagName,"format") == null){
			UChat.get().getConfig().setConfig("tags",tagName,"format", format);
			UChat.get().getConfig().setConfig("tags",tagName,"click-cmd", clickCmd);
			UChat.get().getConfig().setConfig("tags",tagName,"hover-messages", hoverMessages);
			UChat.get().getConfig().save();
			return true;
		}
		return false;
	}
	
	public boolean registerNewChannel(UCChannel channel) throws IOException{
		UChat.get().getConfig().addChannel(channel);
		UChat.get().getCmds().registerChannelAliases();
		return true;
	}
	
	public boolean registerNewChannel(Map<String, Object> properties) throws IOException{
		UCChannel ch = new UCChannel(properties);	
		UChat.get().getConfig().addChannel(ch);		
		UChat.get().getCmds().registerChannelAliases();
		return true;
	}
	
	@Deprecated
	public boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance, String color, String tagBuilder, boolean needFocus, boolean receiverMsg, double cost, String ddmode, String ddmcformat, String mcddformat, String ddhover, boolean ddallowcmds, boolean bungee) throws IOException{
		if (UChat.get().getConfig().getChannel(chName) != null){
			return false;
		}
		if (tagBuilder == null || tagBuilder.equals("")){
			tagBuilder = UChat.get().getConfig().getString("general","default-tag-builder");			
		}
		UCChannel ch = new UCChannel(chName, chAlias, crossWorlds, distance, color, tagBuilder, needFocus, receiverMsg, cost, bungee, false, false, "player", "", new ArrayList<String>(), new String(), ddmode, ddmcformat, mcddformat, ddhover, ddallowcmds, true);	
		UChat.get().getConfig().addChannel(ch);		
		return true;
	}	
	
	public UCChannel getChannel(String chName){
		return UChat.get().getConfig().getChannel(chName);
	}
	
	public UCChannel getPlayerChannel(Player player){
		return UChat.get().getConfig().getPlayerChannel(player);
	}
	
	public Collection<UCChannel> getChannels(){
		return UChat.get().getConfig().getChannels();
	}
}

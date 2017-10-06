package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.spongepowered.api.entity.living.player.Player;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.TagsCategory;

public class uChatAPI {
	
	public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages, String permission, List<String> shoinworlds, List<String> hideinworlds){
		TagsCategory tagsCat = new TagsCategory(format, clickCmd, hoverMessages, permission, shoinworlds, hideinworlds);
		UChat.get().getConfig().root().tags.put(tagName, tagsCat);
		return true;
	}
	
	public boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages){		
		return registerNewTag(tagName, format, clickCmd, hoverMessages, null, null, null);
	}
	
	public boolean registerNewChannel(UCChannel channel) throws IOException{
		UChat.get().getConfig().addChannel(channel);
		UChat.get().getCmds().registerChannelAliases();
		return true;
	}
	
	public boolean registerNewChannel(Map<String, Object> properties) throws IOException{
		UCChannel ch = new UCChannel(properties);
		return registerNewChannel(ch);
	}
	
	@Deprecated
	public boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance, String color, String tagBuilder, boolean needFocus, boolean receiverMsg, double cost, String ddmode, String ddmcformat, String mcddformat, String ddhover, boolean ddallowcmds, boolean bungee) throws IOException{
		if (UChat.get().getConfig().getChannel(chName) != null){
			return false;
		}
		if (tagBuilder == null || tagBuilder.equals("")){
			tagBuilder = UChat.get().getConfig().root().general.default_tag_builder;			
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

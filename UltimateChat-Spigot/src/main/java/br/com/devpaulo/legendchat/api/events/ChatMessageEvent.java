package br.com.devpaulo.legendchat.api.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;

/**
 * Classe apenas para compatibilidade com o LegendChat para setar tags.
 * Só o método 'setTagValue' funciona nessa classe.
 * 
 * @author Fabio
 *
 */
public class ChatMessageEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancel = false;
	private HashMap<String, String> tags;
	private CommandSender sender;
	private String message;
	
	public ChatMessageEvent(CommandSender sender, HashMap<String, String> tags, String message){
		this.sender = sender;
		this.tags = tags;
		this.message = message;
	}
	
	public void setMessage(String message){
		this.message = message; 
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public Player getSender(){
		if (this.sender instanceof Player){
			return (Player) this.sender;
		}
		return null;
	}
	
	public boolean setTagValue(String tagname, String value){
		addTag(tagname, value);
		return true;
	}
	
	public String getTagValue(String tag){	
		tag = tag.toLowerCase();
		return tags.get(tag);
	}
	
	public void addTag(String tagname, String value){
		tagname = tagname.toLowerCase();
		UChat.get().getAPI().registerNewTag(tagname, tagname, "", new ArrayList<String>());
		tags.put(tagname, (value==null?"":value));
	}
		
	public HashMap<String,String> getTagMap(){
		return tags;
	}
	
	public List<String> getTags(){
		return UChat.get().getConfig().getTagList();
	}
	
	@Override
	public boolean isCancelled() {
		return this.cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}

}

package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;

/**This event listen to all chat messages sent by player and allow devs to change the message, tags, channel and canccel the chat event too.
 * @author FabioZumbi12
 *
 */
public class SendChannelMessageEvent extends Event implements Cancellable{

	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private CommandSender sender;
	private String msg;
	private UCChannel channel;
	private String[] defBuilder;
	private String[] defFormat;
	private HashMap<String,String> registeredTags;
	private boolean cancelChat = true;
	
	public SendChannelMessageEvent(HashMap<String,String> registeredReplacers, String[] defFormat, CommandSender sender, UCChannel channel, String msg){
		this.sender = sender;
		this.msg = msg;
		this.channel = channel;
		this.defBuilder = UChat.get().getConfig().getDefBuilder();
		this.defFormat = defFormat;
		this.registeredTags = registeredReplacers;
	}
	
	/**Option to cancel or not the AsynPlayerChatEvent, for debug or some reason.
	 * @param cancel {@code boolean} - Set the cancellation.
	 */
	public void cancelIncomingChat(boolean cancel){
		this.cancelChat = cancel;
	}
	
	/**Get if cancel the event AsynPlayerChatEvent.
	 * @return {@code boolean} - The cancellation state.
	 */
	public boolean getCancelIncomingChat(){
		return this.cancelChat;
	}
	
	/**Removes a custom registered tag {@code replacer}.
	 * @param tagname - Tag to remove.
	 * @return {@code true} if removed or {@code false} if dont contains the replacer.
	 */
	public boolean removeTag(String tagname){
		if (this.registeredTags.keySet().contains(tagname)){
			this.registeredTags.remove(tagname);
			return true;
		}
		return false;
	}
	
	/**Register a tag and value. Add your chat tags here.
	 * @param tagname {@code String} - The tag name.
	 * @param value {@code String} - Result to show on chat.
	 * @return {@code true} if added or {@code false} if already contains the tag.
	 */
	public boolean addTag(String tagname, String value){
		if (!this.registeredTags.keySet().contains(tagname)){
			this.registeredTags.put(tagname, value);
			return true;
		}
		return false;
	}
	
	public void setTags(HashMap<String,String> tags){
		this.registeredTags = tags;
	}
	
	/**Get all registered tags.
	 * @return {@code HashMap<String,String>} with all registered replacers.
	 */
	public HashMap<String,String> getResgisteredTags(){
		return this.registeredTags;
	}
	
	/**Get an array of default tags set by other plugins as array.
	 * @return String[] With default tags. 
	 */
	public String[] getDefFormat(){
		return this.defFormat;
	}
	
	/**Set the default tags array to send to chat. 
	 * Note: This will not change the tags added by uChat, only default tags and custom tags by other plugins.
	 * @param defFormat - The array as String[].
	 */
	public void setDefFormat(String[] defFormat){
		this.defFormat = defFormat;
	}
	
	/**This is the array of tag names in order what will be sent do chat. Add tag names here only you have added one on vonfiguration.
	 * @param builder - tag name.
	 */
	public void setDefBuilder(String[] builder){
		this.defBuilder = builder;
	}
	
	/**Get an array of the tag names in order to show on chat.
	 * @return String[] - Ordered array with all tag names.
	 */
	public String[] getDefaultBuilder(){
		return defBuilder;
	}
	
	/**Sets new channel to send the message
	 * @param newCh - New UCChannel component to send the message.
	 */
	public void setChannel(String newCh){
		this.channel = UChat.get().getConfig().getChannel(newCh);
	}
	
	/**Get the actual channel will be send the message.
	 * @return UCChannel to send the message or {@code null} if is a private message.
	 */
	public UCChannel getChannel(){
		return this.channel;
	}
	
	/**Change the message to send to chat.
	 * @param newMsg - String with new message.
	 */
	public void setMessage(String newMsg){
		this.msg = newMsg;
	}
	
	/**Get the message that will be send to chat.
	 * @return String with the message.
	 */
	public String getMessage(){
		return this.msg;
	}
	
	/**The sender.
	 * @return CommandSender - The sender.
	 */
	public CommandSender getSender(){
		return this.sender;
	}
	
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

}

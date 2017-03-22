package br.net.fabiozumbi12.UltimateChat.API;

import java.util.HashMap;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.UChat;

/**This event listen to all chat messages sent by player and allow devs to change the message, tags, channel and canccel the chat event too.
 * @author FabioZumbi12
 *
 */
public class SendChannelMessageEvent implements Cancellable, Event  {
	
	private boolean cancelled;
	private CommandSource sender;
	private String msg;
	private UCChannel channel;
	private String[] defBuilder;
	private String[] defFormat;
	private HashMap<String,String> registeredTags;
	private boolean cancelIncoming;
	
	public SendChannelMessageEvent(HashMap<String,String> registeredReplacers, String[] defFormat, CommandSource sender, UCChannel channel, String msg, boolean cancelIncoming){
		this.sender = sender;
		this.msg = msg;
		this.channel = channel;
		this.defBuilder = UChat.get().getConfig().getDefBuilder();
		this.defFormat = defFormat;
		this.registeredTags = registeredReplacers;
		this.cancelIncoming = cancelIncoming;
	}
	
	public CommandSource getSender(){
		return this.sender;
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
	
	/**Get an array of default vanilla tags set by other plugins as array.
	 * <p>
	 * [0] = Header, [1] = Body, [2] = Footer
	 * <p>
	 * @return String[] With default tags. 
	 */
	public String[] getDefFormat(){
		return this.defFormat;
	}
	
	/**Set the default tags array to send to chat. 
	 * <p>
	 * [0] = Header, [1] = Body, [2] = Footer
	 * <p>
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
	
	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public Cause getCause() {
		return Cause.of(NamedCause.simulated(this.sender));
	}

	public boolean getCancelIncomingChat() {		
		return cancelIncoming;
	}
	

}

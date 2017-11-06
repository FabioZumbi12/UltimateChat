package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UltimateFancy;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.HashMap;

/**This event listen all fancy messages to be sent to players formated and colored, ready to send to chat.<p>
 * This event includes console. This event will listen to private messages too.<p>
 *  
 * @author FabioZumbi12
 *
 */
public class PostFormatChatMessageEvent extends Event implements Cancellable  {
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;
	private CommandSender sender;
	private UCChannel channel;
	private HashMap<CommandSender, UltimateFancy> receivers;
	private String raw;
	
	public PostFormatChatMessageEvent(CommandSender sender, HashMap<CommandSender, UltimateFancy> receivers, UCChannel channel, String raw){
		this.sender = sender;
		this.channel = channel;
		this.receivers = receivers;
		this.raw = raw;
	}
	
	@Deprecated
	public PostFormatChatMessageEvent(CommandSender sender, HashMap<CommandSender, UltimateFancy> receivers, UCChannel channel){
		this.sender = sender;
		this.channel = channel;
		this.receivers = receivers;
	}
	
	public UCChannel getChannel(){
		return this.channel;
	}
	
	public CommandSender getSender(){
		return this.sender;
	}
	
	public String getRawMessage(){
		return this.raw;
	}
	
	/**Get the message of a receiver, or {@code null} if the receiver is not on list.
	 * 
	 * @param receiver {@CommandSender}
	 * @return {@code Text} or null if no receivers on this map.
	 */
	public UltimateFancy getReceiverMessage(CommandSender receiver){
		return this.receivers.getOrDefault(receiver, null);
	}
	
	/**Change or add a message and a receiver to the receivers list.
	 * 
	 * @param receiver {@code @CommandSender}
	 * @param message {@code Text}
	 */
	public void setReceiverMessage(CommandSender receiver, UltimateFancy message){		
		this.receivers.put(receiver, message);
	}
	
	/**Get all receivers with your messages. Change, add or remove a receiver in this map.
	 * 
	 * @return {@code HashMap<CommandSender, UltimateFancy>}
	 */
	public HashMap<CommandSender, UltimateFancy> getMessages(){
		return this.receivers;
	}
	
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;		
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

}

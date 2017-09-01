package br.net.fabiozumbi12.UltimateChat.Bukkit.API;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.Fanciful.FancyMessage;

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
	private HashMap<CommandSender, FancyMessage> receivers;
	
	public PostFormatChatMessageEvent(CommandSender sender, HashMap<CommandSender, FancyMessage> receivers, UCChannel channel){
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
	
	/**Get the message of a receiver, or {@code null} if the receiver is not on list.
	 * 
	 * @param receiver {@CommandSender}
	 * @return {@code Text} or null if no receivers on this map.
	 */
	public FancyMessage getReceiverMessage(CommandSender receiver){
		return this.receivers.getOrDefault(receiver, null);
	}
	
	/**Change or add a message and a receiver to the receivers list.
	 * 
	 * @param receiver {@code @CommandSender}
	 * @param message {@code Text}
	 */
	public void setReceiverMessage(CommandSender receiver, FancyMessage message){		
		this.receivers.put(receiver, message);
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

}

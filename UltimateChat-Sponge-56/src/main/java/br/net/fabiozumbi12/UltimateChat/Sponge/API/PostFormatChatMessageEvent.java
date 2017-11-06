package br.net.fabiozumbi12.UltimateChat.Sponge.API;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;

import java.util.HashMap;

/**This event listen all text to be sent to players formated and colored, ready to send to chat.<p>
 * This event includes console. This event will listen to private messages too.<p>
 * 
 * Cancelling this event will not cancel SendMessageEvent.Chat but only will replace the channel.
 * @author FabioZumbi12
 *
 */
public class PostFormatChatMessageEvent extends AbstractEvent implements Cancellable, Event  {
	private boolean cancelled;
	private CommandSource sender;
	private UCChannel channel;
	private HashMap<CommandSource, Text> receivers;
	
	public PostFormatChatMessageEvent(CommandSource sender, HashMap<CommandSource, Text> receivers, UCChannel channel){
		this.sender = sender;
		this.channel = channel;
		this.receivers = receivers;
	}
	
	public UCChannel getChannel(){
		return this.channel;
	}
	
	public CommandSource getSender(){
		return this.sender;
	}
	
	/**Get the message of a receiver, or {@code null} if the receiver is not on list.
	 * 
	 * @param receiver {@CommandSource}
	 * @return {@code Text} or null if no receivers on this map.
	 */
	public Text getReceiverMessage(CommandSource receiver){
		return this.receivers.getOrDefault(receiver, null);
	}
	
	/**Change or add a message and a receiver to the receivers list.
	 * 
	 * @param receiver {@code CommandSource}
	 * @param message {@code Text}
	 */
	public void setReceiverMessage(CommandSource receiver, Text message){		
		this.receivers.put(receiver, message);
	}
	
	@Override
	public Cause getCause() {
		return UChat.get().getVHelper().getCause(this.sender);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;		
	}

}

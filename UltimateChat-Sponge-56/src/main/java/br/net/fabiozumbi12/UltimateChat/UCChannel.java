package br.net.fabiozumbi12.UltimateChat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.World;

/**Represents a chat channel use by UltimateChat to control from where/to send/receive messages.
 * 
 * @author FabioZumbi12
 *
 */
public class UCChannel {

	private String name;
	private String alias;
	private boolean worlds = true;
	private int dist = 0;
	private String color = "&a";
	private String builder = "";
	private boolean focus = false;
	private boolean receiversMsg = false;
	private List<String> ignoring = new ArrayList<String>();
	private List<String> mutes = new ArrayList<String>();
	private double cost = 0.0;
	private boolean bungee = false;
	private boolean ownBuilder = false;
	private boolean isAlias = false;
	private String aliasSender = "";
	private String aliasCmd = "";
	private List<String> availableWorlds = new ArrayList<String>();
	private boolean canLock = true;

	public UCChannel(String name, String alias, boolean worlds, int dist, String color, String builder, boolean focus, boolean receiversMsg, double cost, boolean isbungee, boolean ownBuilder, boolean isAlias, String aliasSender, String aliasCmd, List<String> availableWorlds, boolean lock) {
		this.name = name;
		this.alias = alias;
		this.worlds = worlds;
		this.dist = dist;
		this.color = color;
		this.builder = builder;
		this.focus = focus;
		this.receiversMsg = receiversMsg;
		this.cost = cost;
		this.bungee = isbungee;
		this.ownBuilder  = ownBuilder;
		this.isAlias = isAlias;
		this.aliasCmd  = aliasCmd;
		this.aliasSender = aliasSender;
		this.availableWorlds = availableWorlds;	
		this.canLock = lock;
	}
			
	UCChannel(String name) {
		this.name = name;
		this.alias = name.substring(0, 1).toLowerCase();
	}
	
	public boolean canLock(){
		return this.canLock;
	}
	
	boolean availableInWorld(World w){
		return this.availableWorlds.contains(w.getName());
	}
	
	public List<String> availableWorlds(){
		return this.availableWorlds;
	}
	
	public String getAliasCmd(){
		return this.aliasCmd;
	}
	
	public String getAliasSender(){		
		return this.aliasSender;
	}
	
	public boolean isCmdAlias(){
		return this.isAlias;
	}
	
	public boolean useOwnBuilder(){
		return this.ownBuilder;
	}
	
	public double getCost(){
		return this.cost;
	}
	
	public void setCost(double cost){
		this.cost = cost;
	}
	
	public void setReceiversMsg(boolean show){
		this.receiversMsg = show;
	}
	
	public boolean getReceiversMsg(){
		return this.receiversMsg;
	}
	
	public void muteThis(String player){
		if (!this.mutes.contains(player)){
			this.mutes.add(player);
		}		
	}
	
	public void unMuteThis(String player){
		if (this.mutes.contains(player)){
			this.mutes.remove(player);
		}		
	}
	
	public boolean isMuted(String player){
		return this.mutes.contains(player);
	}
	
	void ignoreThis(String player){
		if (!this.ignoring.contains(player)){
			this.ignoring.add(player);
		}		
	}
	
	void unIgnoreThis(String player){
		if (this.ignoring.contains(player)){
			this.ignoring.remove(player);
		}		
	}
	
	boolean isIgnoring(String player){
		return this.ignoring.contains(player);
	}
	
	public String[] getBuilder(){
		return this.builder.split(",");
	}
	
	public String getRawBuilder(){
		return this.builder;
	}
	
	public boolean crossWorlds(){
		return this.worlds;
	}
	
	public int getDistance(){
		return this.dist;
	}
	
	public String getColor(){
		return this.color;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getAlias(){
		return this.alias;
	}

	public boolean neeFocus() {
		return this.focus;
	}
	
	public boolean matchChannel(String aliasOrName){
		return this.alias.equalsIgnoreCase(aliasOrName) || this.name.equalsIgnoreCase(aliasOrName);
	}
		
	public boolean isBungee() {		
		return this.bungee ;
	}
	
	/** Send a message from a channel as player.<p>
	 * <i>Use {@code sendMessage(Player, Text)} as replecement for this method</i>
	 * @param src {@code Player}
	 * @param message {@code Text}
	 */
	@Deprecated
	public void sendMessage(Player src, String message){
		sendMessage(src, message);
	}
	
	/** Send a message from a channel as player.
	 * @param src {@code Player}
	 * @param message {@code Text} - Message to send.
	 * @param direct {@code boolean} - Send message direct to players on channel.
	 */
	public void sendMessage(Player src, Text message, boolean direct){	
		if (direct){
			for (Entry<String, String> chEnt:UChat.get().pChannels.entrySet()){
				Player p = Sponge.getServer().getPlayer(chEnt.getKey()).get();
				if (UChat.get().getPerms().channelPerm(p, this) && !this.isIgnoring(chEnt.getKey()) && (this.neeFocus() && chEnt.getValue().equalsIgnoreCase(this.alias) || !this.neeFocus())){
					p.sendMessage(message);
				}
			}
			src.sendMessage(message);
		} else {
			MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
					Cause.source(src).named(NamedCause.notifier(src)).build(), 
					src.getMessageChannel(), 
					Optional.of(src.getMessageChannel()), 				    							
					new MessageEvent.MessageFormatter(Text.builder("<" + src.getName() + "> ")
							.onShiftClick(TextActions.insertText(src.getName()))
							.onClick(TextActions.suggestCommand("/msg " + src.getName()))
							.build(), message),
							message,  
					false);
			if (!Sponge.getEventManager().post(event)){
				UChat.tempChannels.put(src.getName(), this.alias);
			}
		}
			
		
	}
	
	/** Send a message from a channel as console.
	 * @param sender {@code ConsoleSource} - Console sender.
	 * @param message {@code Text} - Message to send.
	 * @param direct {@code boolean} - Send message direct to players on channel.
	 */
	public void sendMessage(ConsoleSource sender, Text message, boolean direct){
		if (direct){			
			for (Entry<String, String> chEnt:UChat.get().pChannels.entrySet()){
				Player p = Sponge.getServer().getPlayer(chEnt.getKey()).get();
				if (UChat.get().getPerms().channelPerm(p, this) && !this.isIgnoring(chEnt.getKey()) && (this.neeFocus() && chEnt.getValue().equalsIgnoreCase(this.alias) || !this.neeFocus())){
					p.sendMessage(message);
				}
			}
			sender.sendMessage(message);
		} else {
			UCMessages.sendFancyMessage(new String[0], message, this, sender, null);
		}
	}
	
	/** Send a message from a channel as console.<p>
	 * <i>Use {@code sendMessage(ConsoleSource, message, direct)} as replecement for this method</i>
	 * @param src {@code ConsoleSource} - Console sender.
	 * @param message {@code Text} - message to send.
	 */
	@Deprecated	
	public void sendMessage(ConsoleSource sender, String message){
		sendMessage(sender, Text.of(message), UChat.get().getConfig().getBool("api","format-console-messages"));		
	}
}

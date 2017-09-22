package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.world.World;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCLogger.timingType;

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
	private String ddchannel = new String();
	private String ddmode = "NONE";
	private List<CommandSource> members = new ArrayList<CommandSource>();
	private String ddmcformat = "{ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: ";
	private String mcddformat = ":thought_balloon: **{sender}**: {message}";
	private String ddhover = "&3Discord Channel: &a{dd-channel}\n&3Role Name: {dd-rolecolor}{dd-rolename}";
	private boolean ddallowcmds = false;

	public UCChannel(String name, String alias, boolean worlds, int dist, String color, String builder, boolean focus, boolean receiversMsg, double cost, boolean isbungee, boolean ownBuilder, boolean isAlias, String aliasSender, String aliasCmd, List<String> availableWorlds, String ddchannel, String ddmode, String ddmcformat, String mcddformat, String ddhover, boolean ddallowcmds, boolean lock) {
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
		this.ddchannel = ddchannel;
		this.ddmode = ddmode;
		this.ddmcformat = ddmcformat;
		this.mcddformat = mcddformat;
		this.ddhover = ddhover;
		this.ddallowcmds = ddallowcmds;
	}
			
	UCChannel(String name) {
		this.name = name;
		this.alias = name.substring(0, 1).toLowerCase();
	}
	
	public boolean getDiscordAllowCmds(){
		return this.ddallowcmds;
	}

	public boolean isTell(){
		return this.name.equals("tell");		
	}
	
	public String getDiscordChannelID(){
		return this.ddchannel;
	}
	
	public String getDiscordMode(){
		return this.ddmode;
	}
	
	public boolean matchDiscordID(String id){
		return this.ddchannel.equals(id);
	}
	
	public boolean isSendingDiscord(){
		return !ddchannel.isEmpty() && (ddmode.equalsIgnoreCase("both") || ddmode.equalsIgnoreCase("send"));
	}
	
	public boolean isListenDiscord(){
		return !ddchannel.isEmpty() && (ddmode.equalsIgnoreCase("both") || ddmode.equalsIgnoreCase("listen"));
	}
	
	public String getDiscordHover(){
		return this.ddhover;
	}
	
	public String getDiscordtoMCFormat(){
		return this.ddmcformat;
	}
	
	public String getMCtoDiscordFormat(){
		return this.mcddformat;
	}
	
	public List<CommandSource> getMembers(){
		return this.members;
	}
	
	public void clearMembers(){
		this.members.clear();
	}
	
	public boolean addMember(CommandSource p){
		for (UCChannel ch:UChat.get().getConfig().getChannels()){
			ch.removeMember(p);
		}
		return this.members.add(p);
	}
	
	public boolean removeMember(CommandSource p){
		return this.members.remove(p);
	}
	
	public boolean isMember(Player p){
		return this.members.contains(p);
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
			for (Player p:Sponge.getServer().getOnlinePlayers()){
				UCChannel chp = UChat.get().getConfig().getPlayerChannel(p);
				if (UChat.get().getPerms().channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){
					UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
					p.sendMessage(message);					
				}
			}
			src.sendMessage(message);
		} else {
			MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
					UChat.get().getVHelper().getCause(src), 
					src.getMessageChannel(), 
					Optional.of(src.getMessageChannel()), 				    							
					new MessageEvent.MessageFormatter(Text.builder("<" + src.getName() + "> ")
							.onShiftClick(TextActions.insertText(src.getName()))
							.onClick(TextActions.suggestCommand("/msg " + src.getName()))
							.build(), message),
							message,  
					false);
			UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire MessageChannelEvent");
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
			for (Player p:Sponge.getServer().getOnlinePlayers()){
				UCChannel chp = UChat.get().getConfig().getPlayerChannel(p);
				if (UChat.get().getPerms().channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){
					UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
					p.sendMessage(message);					
				}
			}
			sender.sendMessage(message);
		} else {
			UChat.get().getLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire MessageChannelEvent");
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

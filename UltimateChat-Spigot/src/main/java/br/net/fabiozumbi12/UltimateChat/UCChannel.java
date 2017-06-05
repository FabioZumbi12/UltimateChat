package br.net.fabiozumbi12.UltimateChat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
	
	public UCChannel(String name, String alias, String color) {
		this.name = name;
		this.alias = alias;
		this.color = color;
	}
	
	public UCChannel(String name) {
		this.name = name;
		this.alias = name.substring(0, 1).toLowerCase();
	}
	
	public boolean canLock(){
		return this.canLock;
	}
	
	public boolean availableInWorld(World w){
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
	
	public void ignoreThis(String player){
		if (!this.ignoring.contains(player)){
			this.ignoring.add(player);
		}		
	}
	
	public void unIgnoreThis(String player){
		if (this.ignoring.contains(player)){
			this.ignoring.remove(player);
		}		
	}
	
	public boolean isIgnoring(String player){
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
	
	public void sendMessage(Player sender, String message){
		Set<Player> pls = new HashSet<Player>();
		pls.addAll(Bukkit.getOnlinePlayers());
		UChat.tempChannels.put(sender.getName(), this.alias);
		AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, sender, message, pls);
		Bukkit.getPluginManager().callEvent(event); 
	}
	
	public void sendMessage(ConsoleCommandSender sender, String message){	
		if (UChat.config.getBool("api.format-console-messages")){
			UCMessages.sendFancyMessage(new String[0], message, this, sender, null);
		} else {
			for (Entry<String, String> chEnt:UChat.pChannels.entrySet()){
				if (!this.isIgnoring(chEnt.getKey()) && (this.neeFocus() && chEnt.getValue().equalsIgnoreCase(this.alias) || !this.neeFocus())){
					Bukkit.getPlayer(chEnt.getKey()).sendMessage(ChatColor.translateAlternateColorCodes('&', message));
				}
			}
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}
}

package br.net.fabiozumbi12.UltimateChat.Bukkit;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.Map.Entry;

/**Represents a chat channel use by UltimateChat to control from where/to send/receive messages.
 * 
 * @author FabioZumbi12
 *
 */
public class UCChannel {
	private List<String> ignoring = new ArrayList<>();
	private List<String> mutes = new ArrayList<>();
	private List<String> members = new ArrayList<>();
	private Properties properties = new Properties();
	
	private void addDefaults(){
		properties.put("name", "");
		properties.put("alias", "");
		properties.put("color", "&b");
		properties.put("across-worlds", true);
		properties.put("distance", 0);
		properties.put("use-this-builder", false);
		properties.put("tag-builder", "world,marry-tag,ch-tags,clan-tag,factions,group-prefix,nickname,group-suffix,message");
		properties.put("need-focus", false);
		properties.put("canLock", true);
		properties.put("receivers-message", true);
		properties.put("cost", 0.0);
		properties.put("bungee", false);
		properties.put("jedis", false);
		properties.put("password", "");
		properties.put("channelAlias.enable", false);
		properties.put("channelAlias.sendAs", "player");
		properties.put("channelAlias.cmd", "");
		properties.put("available-worlds", new ArrayList<String>());
		properties.put("discord.channelID", "");
		properties.put("discord.mode", "none");
		properties.put("discord.hover", "&3Discord Channel: &a{dd-channel}\n&3Role Name: {dd-rolecolor}{dd-rolename}");
		properties.put("discord.allow-server-cmds", false);
		properties.put("discord.format-to-mc", "{ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: ");
		properties.put("discord.format-to-dd", ":regional_indicator_g: **{sender}**: {message}");
	}
	
	@Deprecated()
	public UCChannel(String name, String alias, boolean worlds, int dist, String color, String builder, boolean focus, boolean receiversMsg, double cost, boolean isbungee, boolean ownBuilder, boolean isAlias, String aliasSender, String aliasCmd, List<String> availableWorlds, String ddchannel, String ddmode, String ddmcformat, String mcddformat, String ddhover, boolean ddallowcmds, boolean lock) {
		addDefaults();
		properties.put("name", name);
		properties.put("alias", alias);
		properties.put("color", color);
		properties.put("across-worlds", worlds);
		properties.put("distance", dist);
		properties.put("use-this-builder", ownBuilder);
		properties.put("tag-builder", builder);
		properties.put("need-focus", focus);
		properties.put("canLock", lock);
		properties.put("receivers-message", receiversMsg);
		properties.put("cost", cost);
		properties.put("bungee", isbungee);
		properties.put("channelAlias.enable", isAlias);
		properties.put("channelAlias.sendAs", aliasSender);
		properties.put("channelAlias.cmd", aliasCmd);
		properties.put("available-worlds", availableWorlds);
		properties.put("discord.channelID", ddchannel);
		properties.put("discord.mode", ddmode);
		properties.put("discord.hover", ddhover);
		properties.put("discord.allow-server-cmds", ddallowcmds);
		properties.put("discord.format-to-mc", ddmcformat);
		properties.put("discord.format-to-dd", mcddformat);
	}
	
	public UCChannel(String name, String alias, String color) {
		addDefaults();
		properties.put("name", name);
		properties.put("alias", alias);
		properties.put("color", color);
	}
	
	public UCChannel(String name) {
		addDefaults();
		properties.put("name", name);
		properties.put("alias", name.substring(0, 1).toLowerCase());
	}
	
	public UCChannel(Map<String, Object> props) {
		addDefaults();
		properties.keySet().stream().filter((key)->props.containsKey(key)).forEach((nkey)->{
			properties.put(nkey, props.get(nkey));
		});
	}

	public Properties getProperties(){
		return properties;
	}
	
	public void setProperty(String key, String value){
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
			properties.put(key, Boolean.getBoolean(value));
		} else {
			try {
				properties.put(key, Integer.parseInt(value));
			} catch (Exception ex){
				properties.put(key, value);
			}	
		}			
	}
	
	public void setPassword(String pass){
		properties.put("password", pass);
	}
	
	public String getPassword(){
		return (String) properties.get("password");
	}
	
	public void setMembers(List<String> members){
		this.members = members;
	}
	
	public boolean useJedis(){
		return (boolean) properties.get("jedis");
	}
	
	public void setJedis(boolean use){
		properties.put("jedis", use);
	}	
	
	public boolean getDiscordAllowCmds(){		
		return (boolean) properties.get("discord.allow-server-cmds");
	}
	
	public boolean isTell(){
		return properties.get("name").toString().equals("tell");		
	}
	
	public String getDiscordChannelID(){
		return properties.get("discord.channelID").toString();
	}
	
	public String getDiscordMode(){
		return properties.get("discord.mode").toString();
	}
	
	public boolean matchDiscordID(String id){
		return getDiscordChannelID().equals(id);
	}
	
	public boolean isSendingDiscord(){
		return !getDiscordChannelID().isEmpty() && (getDiscordMode().equalsIgnoreCase("both") || getDiscordMode().equalsIgnoreCase("send"));
	}
	
	public boolean isListenDiscord(){
		return !getDiscordChannelID().isEmpty() && (getDiscordMode().equalsIgnoreCase("both") || getDiscordMode().equalsIgnoreCase("listen"));
	}
	
	public String getDiscordHover(){
		return properties.get("discord.hover").toString();
	}
	
	public String getDiscordtoMCFormat(){
		return properties.get("discord.format-to-mc").toString();
	}
	
	public String getMCtoDiscordFormat(){
		return properties.get("discord.format-to-dd").toString();
	}
	
	public List<String> getMembers(){
		return this.members;
	}
	
	public void clearMembers(){
		this.members.clear();
	}
	
	public boolean addMember(CommandSender p){
		return addMember(p.getName());
	}

	public boolean addMember(String p){
        for (UCChannel ch:UChat.get().getChannels().values()){
            ch.removeMember(p);
        }
        return this.members.add(p);
	}
	
	public boolean removeMember(CommandSender p){
		return removeMember(p.getName());
	}

    public boolean removeMember(String p){
        return this.members.remove(p);
    }

	
	public boolean isMember(CommandSender p){
		return this.members.contains(p.getName());
	}
		
	public boolean canLock(){
		return (boolean) properties.get("canLock");
	}
	
	@SuppressWarnings("unchecked")
	public boolean availableInWorld(World w){
		return ((List<String>)properties.get("available-worlds")).contains(w.getName());
	}
	
	@SuppressWarnings("unchecked")
	public List<String> availableWorlds(){
		return ((List<String>)properties.get("available-worlds"));
	}
	
	public String getAliasCmd(){
		return properties.get("channelAlias.cmd").toString();
	}
	
	public String getAliasSender(){		
		return properties.get("channelAlias.sendAs").toString();
	}
	
	public boolean isCmdAlias(){
		return (boolean) properties.get("channelAlias.enable");
	}
	
	public boolean useOwnBuilder(){
		return (boolean) properties.get("use-this-builder");
	}
	
	public double getCost(){
		return Double.parseDouble(properties.get("cost").toString());
	}
	
	public void setCost(double cost){
		properties.put("cost", cost);
	}
	
	public void setReceiversMsg(boolean show){
		properties.put("receivers-message", show);
	}
	
	public boolean getReceiversMsg(){
		return (boolean) properties.get("receivers-message");
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
		return properties.get("tag-builder").toString().split(",");
	}
	
	public String getRawBuilder(){
		return properties.get("tag-builder").toString();
	}
	
	public boolean crossWorlds(){
		return (boolean) properties.get("across-worlds");
	}
	
	public int getDistance(){
		return (int) properties.get("distance");
	}
	
	public String getColor(){
		return properties.get("color").toString();
	}
	
	public String getName(){
		return properties.get("name").toString();
	}
	
	public String getAlias(){
		return properties.get("alias").toString();
	}

	public boolean neeFocus() {
		return (boolean) properties.get("need-focus");
	}
	
	public boolean matchChannel(String aliasOrName){
		return properties.get("alias").toString().equalsIgnoreCase(aliasOrName) || properties.get("name").toString().equalsIgnoreCase(aliasOrName);
	}

	public boolean isBungee() {		
		return (boolean) properties.get("bungee");
	}
	
	/** Send a message from a channel as player.
	 * @param sender {@code Player}
	 * @param message {@code String} - Message to send.
	 */
	@Deprecated
	public void sendMessage(Player sender, String message){
		sendMessage(sender, new UltimateFancy(message), false);
	}
	
	/** Send a message from a channel as player.
	 * @param sender {@code Player}
	 * @param message {@code FancyMessage} - Message to send.
	 * @param direct {@code boolean} - Send message direct to players on channel.
	 */
	public void sendMessage(Player sender, UltimateFancy message, boolean direct){
		if (direct){
			for (Player p:Bukkit.getOnlinePlayers()){
				UCChannel chp = UChat.get().getPlayerChannel(p);
				if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
					message.send(p);					
				}
			}			
			message.send(sender);	
		} else {
			Set<Player> pls = new HashSet<Player>();
			pls.addAll(Bukkit.getOnlinePlayers());
			UChat.get().tempChannels.put(sender.getName(), this.getAlias());
			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender, message.toOldFormat(), pls);
			Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){

				@Override
				public void run() {
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire AsyncPlayerChatEvent");
					UChat.get().getServer().getPluginManager().callEvent(event); 
				}			
			});
		}		
	}
	
	/** Send a message from a channel as console.
	 * @param sender {@code ConsoleCommandSender} - Console sender.
	 * @param message {@code FancyMessage} - Message to send.
	 * @param direct {@code boolean} - Send message direct to players on channel.
	 */
	public void sendMessage(ConsoleCommandSender sender, UltimateFancy message, boolean direct){	
		if (direct){
			for (Player p:Bukkit.getOnlinePlayers()){
				UCChannel chp = UChat.get().getPlayerChannel(p);
				if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
					message.send(p);					
				}
			}
			message.send(sender);	
		} else {			
			UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire MessageChannelEvent");
			UCMessages.sendFancyMessage(new String[0], message.toOldFormat(), this, sender, null);
		}
	}
	
	/** Send a message from a channel as console.
	 * @param sender {@code ConsoleCommandSender} - Console sender.
	 * @param message {@code FancyMessage} - Message to send.
	 */
	@Deprecated
	public void sendMessage(ConsoleCommandSender sender, String message){	
		if (UChat.get().getConfig().getBoolean("api.format-console-messages")){
			UCMessages.sendFancyMessage(new String[0], message, this, sender, null);
		} else {
			UltimateFancy fmsg = new UltimateFancy(message);
			for (Player p:Bukkit.getOnlinePlayers()){
				UCChannel chp = UChat.get().getPlayerChannel(p);
				if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (this.neeFocus() && chp.equals(this) || !this.neeFocus())){	
					UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire AsyncPlayerChatEvent");
					fmsg.send(p);					
				}
			}
			fmsg.send(sender);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString(){
		JSONArray array = new JSONArray();
		for (Entry<Object, Object> prop:properties.entrySet()){
			JSONObject json = new JSONObject();
			json.put((String) prop.getKey(),prop.getValue());
			array.add(json);
		}
		return array.toJSONString();		
	}
}

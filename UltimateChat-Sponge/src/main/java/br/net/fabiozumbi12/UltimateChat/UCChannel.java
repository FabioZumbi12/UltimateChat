package br.net.fabiozumbi12.UltimateChat;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.world.World;

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
		
	public boolean isBungee() {		
		return this.bungee ;
	}
}

package br.net.fabiozumbi12.UltimateChat.Sponge.Jedis;

import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import redis.clients.jedis.JedisPubSub;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;

public class ChatChannel extends JedisPubSub {
	private String[] channels;
	private String thisId;

	public ChatChannel(String[] channels){
		this.channels = channels;
		this.thisId = UChat.get().getConfig().getString("jedis","server-id").replace("$", "");
	}
	
    @Override
    public void onMessage(String channel, final String message) {
    	if (!UChat.get().getConfig().getBool("debug","messages") && message.startsWith(this.thisId)) return;
    	
    	if (Arrays.asList(channels).contains(channel)){
    		Sponge.getScheduler().createAsyncExecutor(UChat.get()).execute(new Runnable(){
				@Override
				public void run() {
					if (channel.equals("tellresponse")){
						String[] tellresp = message.split("@");
						if (tellresp[0].equals(thisId)) return;
						if (UChat.get().getJedis().tellPlayers.containsKey(tellresp[1])){
							Optional<Player> sender = Sponge.getServer().getPlayer(UChat.get().getJedis().tellPlayers.get(tellresp[1]));
							if (sender.isPresent() && sender.get().isOnline()){
								if (tellresp[2].equals("false")){
									UChat.get().getLang().sendMessage(sender.get(), UChat.get().getLang().get("listener.invalidplayer"));
								} else {		
									Sponge.getScheduler().createSyncExecutor(UChat.get()).execute(new Runnable(){
										@Override
										public void run() {
											Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+sender.get().getName()+" "+tellresp[3]);
										}											
									});										
								}
							}							
						}						
						return;
					}
					
					if (channel.equals("tellsend")){
						String[] msgc = message.split("\\$");
						
						String id = msgc[0];
						String tellrec = msgc[1];
						String messagef = msgc[2];
												
						Optional<Player> play = Sponge.getServer().getPlayer(tellrec);
						if (!play.isPresent()){
							UChat.get().getJedis().getPool().getResource().publish("tellresponse", thisId+"@"+tellrec+"@false");
							return;
						} else {
							UChat.get().getJedis().getPool().getResource().publish("tellresponse", thisId+"@"+tellrec+"@true@"+messagef.replace("@", ""));
						}							
						UChat.get().getJedis().tellPlayers.remove(tellrec);
						Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&7Private message from server "+id+" to player "+tellrec));
						
						//send
						Sponge.getScheduler().createSyncExecutor(UChat.get()).execute(new Runnable(){
							@Override
							public void run() {
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+play.get().getName()+" "+messagef);
							}											
						});	
						return;
					}

					if (!channel.equals("generic")){
						String[] msgc = message.split("\\$");
						
						String id = msgc[0];
						String messagef = msgc[1];
				    	
						UCChannel ch = UChat.get().getConfig().getChannel(channel);
						if (ch == null || !ch.useJedis()) return;
						
						if (ch.getDistance() == 0){
							if (ch.neeFocus()){
								for (CommandSource receiver:ch.getMembers()){
									Sponge.getScheduler().createSyncExecutor(UChat.get()).execute(new Runnable(){
										@Override
										public void run() {
											Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+receiver.getName()+" "+messagef);
										}											
									});	
								}
							} else {
								for (Player receiver:Sponge.getServer().getOnlinePlayers()){
									if (UChat.get().getPerms().channelReadPerm(receiver, ch)){
										Sponge.getScheduler().createSyncExecutor(UChat.get()).execute(new Runnable(){
											@Override
											public void run() {
												Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+receiver.getName()+" "+messagef);
											}											
										});										
									}									
								}
							}	
							Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&7Message to channel "+ch.getName()+" from: "+id));
						}						
					} else {
						String[] msgc = message.split("\\$");
						
						String id = msgc[0];
						String messagef = msgc[1];						
						for (Player receiver:Sponge.getServer().getOnlinePlayers()){
							Sponge.getScheduler().createSyncExecutor(UChat.get()).execute(new Runnable(){
								@Override
								public void run() {
									Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw "+receiver.getName()+" "+messagef);
								}											
							});						
						}
						Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&7Raw Message from: "+id));
					}	
				}	    		
	    	});
    	}
    }
}

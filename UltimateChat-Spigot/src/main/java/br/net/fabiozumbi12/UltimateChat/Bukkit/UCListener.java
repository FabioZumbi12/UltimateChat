package br.net.fabiozumbi12.UltimateChat.Bukkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.SendChannelMessageEvent;

public class UCListener implements CommandExecutor,Listener {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		 UChat.get().getUCLogger().debug("onCommand - Label: "+label+" - CmdName: "+cmd.getName());
		 
		 if (args.length == 1){
			 if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")){
				 sendHelp(sender);
				 return true;
			 }
					 
			 if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("uchat.cmd.reload")){
				 UChat.get().reload();
				 UChat.get().getLang().sendMessage(sender, "plugin.reloaded");
				 return true;
			 }			 
		 }		
		 		 		 
		 if (sender instanceof Player){
			 Player p = (Player) sender;
			 
			 if (cmd.getName().equalsIgnoreCase("channel")){
				 if (args.length == 0){
					 UCChannel ch = UChat.get().getConfig().getChannel(label);
					 if (ch != null){							
							if (!UCPerms.channelReadPerm(p, ch) && !UCPerms.channelWritePerm(p, ch)){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
								return true;
							}
							if (!ch.canLock()){
								UChat.get().getLang().sendMessage(p, "help.channels.send");
								return true;
							}
							if (ch.isMember(p)){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
								return true;
							} 
							ch.addMember(p);
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
					 } else {
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", label));						
					 }
					 return true;
				 }
				 
				 if (args.length >= 1){
						UCChannel ch = UChat.get().getConfig().getChannel(label);
						StringBuilder msgBuild = new StringBuilder();
						for (String arg:args){
							msgBuild.append(" "+arg);
						}
						String msg = msgBuild.toString().substring(1);
						
						if (ch != null && msg != null){
							if (!UCPerms.channelWritePerm(p, ch)){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
								return true;
							}					
							
							//run bukkit chat event
							Set<Player> pls = new HashSet<Player>();
							pls.addAll(Bukkit.getOnlinePlayers());
							UChat.get().tempChannels.put(p.getName(), ch.getAlias());
							AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, p, msg, pls);
							Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){

								@Override
								public void run() {
									UChat.get().getUCLogger().timings(timingType.START, "UCListener#onCommand()|Fire AsyncPlayerChatEvent");
									UChat.get().getServer().getPluginManager().callEvent(event); 
								}			
							});
							return true;
						}			
					}
				 
				 //if /ch or /channel
				 if (label.equalsIgnoreCase("ch") || label.equalsIgnoreCase("channel")){						
						if (args.length == 1){				
							UCChannel ch = UChat.get().getConfig().getChannel(args[0]);
							if (ch == null){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[0]));
								return true;
							}
							if (!UCPerms.channelReadPerm(p, ch) && !UCPerms.channelWritePerm(p, ch)){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
								return true;
							}
							if (!ch.canLock()){
								UChat.get().getLang().sendMessage(p, "help.channels.send");
								return true;
							}
							if (ch.isMember(p)){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
								return true;
							} 
							ch.addMember(p);
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
							return true;
						}
					}
				 
				 sendChannelHelp(p);
			 }
			 
			 //Listen cmd chat/uchat
			 if (cmd.getName().equalsIgnoreCase("uchat")){				 
				 if (args.length == 1){
					 
					 if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){						 
						 sendHelp(sender);
						 return true;
					 }
									 
					 if (args[0].equalsIgnoreCase("clear")){	
						 if (!UCPerms.cmdPerm(p, "clear")){
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
							 return true;
						 }
						 for (int i = 0; i < 100; i++){
							 if (!p.isOnline()){
								 break;
							 }
							 UCUtil.performCommand(p, Bukkit.getConsoleSender(), "tellraw " + p.getName() + " {\"text\":\" \"}");
						 }						 
						 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.clear.cleared"));
						 return true;
					 }
					 
					 if (args[0].equalsIgnoreCase("clear-all")){	
						 if (!UCPerms.cmdPerm(p, "clear-all")){
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
							 return true;
						 }
						 for (Player play:Bukkit.getOnlinePlayers()){
							 for (int i = 0; i < 100; i++){
								 if (!play.isOnline()){
									 continue;
								 }
								 UCUtil.performCommand(play, Bukkit.getConsoleSender(), "tellraw " + play.getName() + " {\"text\":\" \"}");
							 }
						 }						 						 
						 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.clear.cleared"));
						 return true;
					 }
					 
					 if (args[0].equalsIgnoreCase("spy")){
						if (!UCPerms.cmdPerm(p, "spy")){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
							return true;
						}
						
						if (!UChat.get().isSpy.contains(p.getName())){
							UChat.get().isSpy.add(p.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.spy.enabled"));
						} else {
							UChat.get().isSpy.remove(p.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.spy.disabled"));
						}
						return true;
					 }
				 }
				 
				 if (args.length == 2){
					 // chat ignore <channel/player>
					 if (args[0].equalsIgnoreCase("ignore")){						 						 
						 UCChannel ch = UChat.get().getConfig().getChannel(args[1]);
						 if (Bukkit.getPlayer(args[1]) != null){
							 Player pi = Bukkit.getPlayer(args[1]);
							 if (!UCPerms.cmdPerm(p, "ignore.player")){
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
								 return true;
							 }
							 if (!UCPerms.canIgnore(sender, pi)){
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
								 return true;
							 }
							 if (UCMessages.isIgnoringPlayers(p.getName(), pi.getName())){
								 UCMessages.unIgnorePlayer(p.getName(), pi.getName());
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.unignoring").replace("{player}", pi.getName()));
							 } else {
								 UCMessages.ignorePlayer(p.getName(), pi.getName());
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.ignoring").replace("{player}", pi.getName()));
							 }
							 return true;
						 } else if (ch != null){	
							 if (!UCPerms.cmdPerm(p, "ignore.channel")){
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
								 return true;
							 }
							 if (!UCPerms.canIgnore(sender, ch)){
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
								 return true;
							 }
							 if (ch.isIgnoring(p.getName())){
								 ch.unIgnoreThis(p.getName());
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.notignoring").replace("{channel}", ch.getName()));
							 } else {
								 ch.ignoreThis(p.getName());
								 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.ignoring").replace("{channel}", ch.getName()));
							 }
							 return true;
						 } else {
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[1]));
							 return true;
						 }
					 }
					 
					 //chat mute <player>
					 if (args[0].equalsIgnoreCase("mute")){
						 if (!UCPerms.cmdPerm(p, "mute")){
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
							 return true;
						 }
						 
						 String pname = args[1];
						 if (Bukkit.getPlayer(args[1]) != null){
							 pname = Bukkit.getPlayer(args[1]).getName();
						 }
						 
						 if (UChat.get().mutes.contains(pname)){
							 UChat.get().mutes.remove(pname);
							 UChat.get().getConfig().unMuteInAllChannels(pname);
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.unmuted.all").replace("{player}", pname));
							 if (Bukkit.getPlayer(pname) != null){
								 UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), "channel.player.unmuted.all");
							 }
						 } else {
							 UChat.get().mutes.add(pname);
							 UChat.get().getConfig().muteInAllChannels(pname);
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.muted.all").replace("{player}", pname));
							 if (Bukkit.getPlayer(pname) != null){
								 UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), "channel.player.muted.all");
							 }
						 }
						 return true;
					 }					 
				 }
				 
				 if (args.length == 3){
					 //chat tempmute <minutes> <player>
					 if (args[0].equalsIgnoreCase("tempmute")){
						 if (!UCPerms.cmdPerm(p, "tempmute")){
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
							 return true;
						 }
						 
						 int time = 0;
						 try {
							 time = Integer.parseInt(args[1]);
						 } catch (Exception ex){
							 UChat.get().getLang().sendMessage(p, "cmd.tempmute.number");
							 return true;
						 }
						 
						 String pname = args[2];
						 
						 if (Bukkit.getPlayer(args[2]) != null){
							 pname = Bukkit.getPlayer(args[2]).getName();
						 }
						 
						 if (UChat.get().mutes.contains(pname)){
							 UChat.get().getLang().sendMessage(p, "channel.already.muted");
						 } else {
							 final String fpname = pname;
							 UChat.get().mutes.add(pname);
							 UChat.get().getConfig().muteInAllChannels(pname);
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.tempmuted.all").replace("{player}", pname).replace("{time}", String.valueOf(time)));
							 if (Bukkit.getPlayer(pname) != null){
								 UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[2]), UChat.get().getLang().get("channel.player.tempmuted.all").replace("{time}", String.valueOf(time)));
							 }
							 Bukkit.getScheduler().runTaskLater(UChat.get(), new Runnable(){
								@Override
								public void run() {
									if (UChat.get().mutes.contains(fpname)){
										UChat.get().mutes.remove(fpname);
										UChat.get().getConfig().unMuteInAllChannels(fpname);
										if (Bukkit.getPlayer(fpname) != null){
											UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[2]), UChat.get().getLang().get("channel.player.unmuted.all"));
										}
									}
								}								 
							 }, (time*60)*20);
						 }
						 return true;
					 }
						 
					 //chat mute <player> <channel>
					 if (args[0].equalsIgnoreCase("mute")){
						 if (!UCPerms.cmdPerm(p, "mute")){
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
							 return true;
						 }
						 UCChannel ch = UChat.get().getConfig().getChannel(args[2]);
						 if (ch == null){
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[1]));
							 return true;
						 }
						 
						 String pname = args[1];
						 if (Bukkit.getPlayer(args[1]) != null){
							 pname = Bukkit.getPlayer(args[1]).getName();
						 }
						 
						 if (ch.isMuted(pname)){
							 ch.unMuteThis(pname);
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.unmuted.this").replace("{player}", pname).replace("{channel}", ch.getName()));
							 if (Bukkit.getPlayer(pname) != null){
								 UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), UChat.get().getLang().get("channel.player.unmuted.this").replace("{channel}", ch.getName()));
							 }
						 } else {
							 ch.muteThis(pname);
							 UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.muted.this").replace("{player}", pname).replace("{channel}", ch.getName()));
							 if (Bukkit.getPlayer(pname) != null){
								 UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), UChat.get().getLang().get("channel.player.muted.this").replace("{channel}", ch.getName()));
							 }
						 }
						 return true;
					 }
				 }
			 }			 
		 } else {
			 if (UChat.get().getConfig().getChAliases().contains(label.toLowerCase())){
				 if (args.length >= 1){
					UCChannel ch = UChat.get().getConfig().getChannel(label);
					
					StringBuilder msgb = new StringBuilder();
					for (String arg:args){
						msgb.append(" "+arg);
					}
					String msg = msgb.toString().substring(1);
					
					UCMessages.sendFancyMessage(new String[0], msg, ch, sender, null);					
					return true;							
				 }
			 }
		 }
		 
		 if (cmd.getName().equalsIgnoreCase("ubroadcast") && UCPerms.cmdPerm(sender, "broadcast")){
			 if (!UCUtil.sendBroadcast(sender, args, false)){
				 sendHelp(sender);
			 }
			 return true;
		 }
		 
		 if (cmd.getName().equalsIgnoreCase("umsg") && UCPerms.cmdPerm(sender, "umsg")){
			 UCUtil.sendUmsg(sender, args);
			 return true;
		 }
		 
		 if (args.length == 0){
			 sender.sendMessage(ChatColor.AQUA + "---------------- " + UChat.get().getPDF().getFullName() + " ----------------");
	         sender.sendMessage(ChatColor.AQUA + "Developed by " + ChatColor.GOLD + UChat.get().getPDF().getAuthors() + ".");
	         sender.sendMessage(ChatColor.AQUA + "For more information about the commands, type [" + ChatColor.GOLD + "/"+label+" ?"+ChatColor.AQUA+"].");
	         sender.sendMessage(ChatColor.AQUA + "---------------------------------------------------");
	         return true;
		 } else {
			 sendHelp(sender);
		 }		 
		 return true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCmdChat(PlayerCommandPreprocessEvent e){
		String[] args = e.getMessage().replace("/", "").split(" ");		
		Player p = e.getPlayer();
		UChat.get().getUCLogger().debug("PlayerCommandPreprocessEvent - Channel: "+args[0]);
				
		//check tell aliases
		if (UChat.get().getConfig().getTellAliases().contains(args[0])){
			e.setCancelled(true);
			
			Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){

				@Override
				public void run() {
					String msg = null;
					if (e.getMessage().length() > args[0].length()+2){
						msg = e.getMessage().substring(args[0].length()+2);
					}
					
					if (!UCPerms.cmdPerm(p, "tell")){
						UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
						return;
					}			
					
					if (args.length == 1){
						if (UChat.get().tellPlayers.containsKey(p.getName())){
							String tp = UChat.get().tellPlayers.get(p.getName());
							UChat.get().tellPlayers.remove(p.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", tp));
							return;
						}
					}
					
					if (args.length >= 2){
						if (args[0].equalsIgnoreCase("r")){
							if (UChat.get().respondTell.containsKey(p.getName())){
								String recStr = UChat.get().respondTell.get(p.getName());
								if (recStr.equals("CONSOLE")){
									UChat.get().respondTell.put("CONSOLE", p.getName());
									UChat.get().command.add(p.getName());
									sendPreTell(p, UChat.get().getServer().getConsoleSender(), msg);
								} else {
									Player receiver = UChat.get().getServer().getPlayer(UChat.get().respondTell.get(p.getName()));
									UChat.get().respondTell.put(receiver.getName(), p.getName());
									UChat.get().command.add(p.getName());
									sendPreTell(p, receiver, msg);
								}
								return;
								//sendTell(p, receiver, msg);						
							} else {
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.nonetorespond"));
								return;
							}
						}
					}
					
					if (args.length == 2){
						Player receiver = UChat.get().getServer().getPlayer(args[1]);
						if (receiver == null || !receiver.isOnline() || !p.canSee(receiver)){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("listener.invalidplayer"));
							return;
						}
						if (receiver.equals(p)){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.self"));
							return;
						}
						
						if (UChat.get().tellPlayers.containsKey(p.getName()) && UChat.get().tellPlayers.get(p.getName()).equals(receiver.getName())){
							UChat.get().tellPlayers.remove(p.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", receiver.getName()));
						} else {
							UChat.get().tellPlayers.put(p.getName(), receiver.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.locked").replace("{player}", receiver.getName()));
						}
						return;
					}
					
					//tell <nick> <mensagem...>
					if (args.length >= 3){
						
						//send to console
						if (args[1].equalsIgnoreCase("console")){
							msg = msg.substring(args[1].length()+1);
							
							UChat.get().tempTellPlayers.put(p.getName(), "CONSOLE");
							UChat.get().command.add(p.getName());
							sendPreTell(p, UChat.get().getServer().getConsoleSender(), msg);
							return;
						}
						
						//send to player
						Player receiver = UChat.get().getServer().getPlayer(args[1]);
							
						if (receiver == null || !receiver.isOnline() || !p.canSee(receiver)){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("listener.invalidplayer"));
							return;
						}
						
						if (receiver.equals(p)){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.self"));
							return;
						}
						
						//remove receiver name
						msg = msg.substring(args[1].length()+1);
						
						UChat.get().tempTellPlayers.put(p.getName(), receiver.getName());
						UChat.get().command.add(p.getName());
						//sendTell(p, receiver, msg);
						
						sendPreTell(p, receiver, msg);
						return;
					}	
					sendTellHelp(p);
				}			
			});				
		}		
	}
		
	private void sendPreTell(CommandSender sender, CommandSender receiver, String msg){
		Set<Player> pls = new HashSet<Player>();
		Player p = null;
		if (sender instanceof Player){
			p = (Player)sender;
		} else {
			p = (Player)receiver;
		}
		pls.add(p);
		AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, p, msg, pls);
		Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), new Runnable(){

			@Override
			public void run() {
				UChat.get().getUCLogger().timings(timingType.START, "UCListener#sendPreTell()|Fire AsyncPlayerChatEvent");
				UChat.get().getServer().getPluginManager().callEvent(event); 
			}			
		});		
	}
	
	@EventHandler
	public void onServerCmd(ServerCommandEvent e){
		String[] args = e.getCommand().replace("/", "").split(" ");
		String msg = null;
		if (e.getCommand().length() > args[0].length()+1){
			msg = e.getCommand().substring(args[0].length()+1);
		}
		
		if (UChat.get().getConfig().getTellAliases().contains(args[0])){
			if (args.length >= 3){
				Player p = UChat.get().getServer().getPlayer(args[1]);
				
				if (p == null || !p.isOnline()){
					UChat.get().getLang().sendMessage(e.getSender(), "listener.invalidplayer");
					return;
				}
								
				msg = msg.substring(args[1].length()+1);
				
				UChat.get().tempTellPlayers.put("CONSOLE", p.getName());
				UChat.get().command.add("CONSOLE");
				sendPreTell(UChat.get().getServer().getConsoleSender(), p, msg);
				e.setCancelled(true);				
			}
		}		
	}
	
	private void sendTell(CommandSender sender, CommandSender receiver, String msg){
		if (receiver == null 
				|| (receiver instanceof Player && (!((Player)receiver).isOnline() 
				|| (sender instanceof Player && receiver instanceof Player && !((Player)sender).canSee((Player)receiver))))
				){
			UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("listener.invalidplayer"));
			return;
		}
		UChat.get().respondTell.put(receiver.getName(), sender.getName());
		UCMessages.sendFancyMessage(new String[0], msg, null, sender, receiver);			
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e){
		if (e.isCancelled()){
			return;
		}
		
		UChat.get().getUCLogger().timings(timingType.START, "UCListener#onChat()|Listening AsyncPlayerChatEvent");
		
		//e.setCancelled(true);
		Player p = e.getPlayer();		
		
		if (UChat.get().tellPlayers.containsKey(p.getName()) && (!UChat.get().tempTellPlayers.containsKey("CONSOLE") || !UChat.get().tempTellPlayers.get("CONSOLE").equals(p.getName()))){
			Player tellreceiver = UChat.get().getServer().getPlayer(UChat.get().tellPlayers.get(p.getName()));
			sendTell(p, tellreceiver, e.getMessage());
			e.setCancelled(true);
		} 
		else if (UChat.get().command.contains(p.getName()) || UChat.get().command.contains("CONSOLE")){			
			if (UChat.get().tempTellPlayers.containsKey("CONSOLE")){
				String recStr = UChat.get().tempTellPlayers.get("CONSOLE");		
				Player pRec = UChat.get().getServer().getPlayer(recStr);
				if (pRec.equals(p)){
					sendTell(UChat.get().getServer().getConsoleSender(), p, e.getMessage());				
					UChat.get().tempTellPlayers.remove("CONSOLE");	
					UChat.get().command.remove("CONSOLE");
				}				
			} else if (UChat.get().tempTellPlayers.containsKey(p.getName())){
				String recStr = UChat.get().tempTellPlayers.get(p.getName());
				if (recStr.equals("CONSOLE")){
					sendTell(p, UChat.get().getServer().getConsoleSender(), e.getMessage());
				} else {
					sendTell(p, UChat.get().getServer().getPlayer(recStr), e.getMessage());
				}		
				UChat.get().tempTellPlayers.remove(p.getName());	
				UChat.get().command.remove(p.getName());
			} else if (UChat.get().respondTell.containsKey(p.getName())){
				String recStr = UChat.get().respondTell.get(p.getName());
				if (recStr.equals("CONSOLE")){
					sendTell(p, UChat.get().getServer().getConsoleSender(), e.getMessage());
				} else {
					sendTell(p, UChat.get().getServer().getPlayer(recStr), e.getMessage());
				}
				UChat.get().respondTell.remove(p.getName());
				UChat.get().command.remove(p.getName());
			}
			e.setCancelled(true);
		}
		
		else {
			UCChannel ch = UChat.get().getConfig().getPlayerChannel(p);
			if (UChat.get().tempChannels.containsKey(p.getName()) && !UChat.get().tempChannels.get(p.getName()).equals(ch.getAlias())){
				ch = UChat.get().getConfig().getChannel(UChat.get().tempChannels.get(p.getName()));
				UChat.get().getUCLogger().debug("AsyncPlayerChatEvent - TempChannel: "+UChat.get().tempChannels.get(p.getName()));
				UChat.get().tempChannels.remove(p.getName());
			}
			
			if (UChat.get().mutes.contains(p.getName()) || ch.isMuted(p.getName())){
				UChat.get().getLang().sendMessage(p, "channel.muted");
				e.setCancelled(true);
				return;
			}			
			
			if (ch.isCmdAlias()){
				String start = ch.getAliasCmd();
				if (start.startsWith("/")){
					start = start.substring(1);
				}
				if (ch.getAliasSender().equalsIgnoreCase("console")){					
					UCUtil.performCommand(null, Bukkit.getConsoleSender(), start+" "+e.getMessage());
				} else {
					UCUtil.performCommand(null, p, start+" "+e.getMessage());
				}				
				e.setCancelled(true);
			} else {
				boolean cancel = UCMessages.sendFancyMessage(e.getFormat().split(","), e.getMessage(), ch, p, null);
				if (cancel){
					e.setCancelled(true);
				}
			}
		}				
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();		
		UChat.get().getConfig().getDefChannel().addMember(p);
		if (UChat.get().getUCJDA() != null){
			UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.join").replace("{player}", p.getName()));
			if (UChat.get().getConfig().getBool("discord.update-status")){
				UChat.get().getUCJDA().updateGame(UChat.get().getLang().get("discord.game").replace("{online}", String.valueOf(UChat.get().getServer().getOnlinePlayers().size())));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void legendCompatEvent(SendChannelMessageEvent e){
		if (e.isCancelled()){
			return;
		}		
		ChatMessageEvent event = new ChatMessageEvent(e.getSender(), e.getResgisteredTags(), e.getMessage());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()){
			e.setCancelled(true);
		}
		e.setMessage(event.getMessage());
		e.setTags(event.getTagMap());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		Player p = e.getPlayer();	
		List<String> toRemove = new ArrayList<String>();
		for (String play:UChat.get().tellPlayers.keySet()){
			if (play.equals(p.getName()) || UChat.get().tellPlayers.get(play).equals(p.getName())){
				toRemove.add(play);				
			}
		}	
		for (String remove:toRemove){
			UChat.get().tellPlayers.remove(remove);
		}
		List<String> toRemove2 = new ArrayList<String>();
		for (String play:UChat.get().respondTell.keySet()){
			if (play.equals(p.getName()) || UChat.get().respondTell.get(play).equals(p.getName())){
				toRemove2.add(play);				
			}
		}	
		for (String remove:toRemove2){
			UChat.get().respondTell.remove(remove);
		}		
		UChat.get().getConfig().getPlayerChannel(p).removeMember(p);
		if (UChat.get().tempChannels.containsKey(p.getName())){
			UChat.get().tempChannels.remove(p.getName());
		}
		if (UChat.get().command.contains(p.getName())){
			UChat.get().command.remove(p.getName());
		}
		if (UChat.get().getUCJDA() != null){
			UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.leave").replace("{player}", p.getName()));
			if (UChat.get().getConfig().getBool("discord.update-status")){
				UChat.get().getUCJDA().updateGame(UChat.get().getLang().get("discord.game").replace("{online}", String.valueOf(UChat.get().getServer().getOnlinePlayers().size()-1)));
			}
		}
	}
		
	public void sendHelp(CommandSender p){		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7--------------- "+UChat.get().getLang().get("_UChat.prefix")+" Help &7---------------"));		
		p.sendMessage(UChat.get().getLang().get("help.channels.enter"));
		p.sendMessage(UChat.get().getLang().get("help.channels.send"));
		if (p.hasPermission("uchat.cmd.tell")){
			p.sendMessage(UChat.get().getLang().get("help.tell.lock"));
			p.sendMessage(UChat.get().getLang().get("help.tell.send"));
			p.sendMessage(UChat.get().getLang().get("help.tell.respond"));
		}
		if (p.hasPermission("uchat.cmd.broadcast")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.broadcast"));
		}
		if (p.hasPermission("uchat.cmd.umsg")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.umsg"));
		}
		if (p.hasPermission("uchat.cmd.clear")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.clear"));
		}
		if (p.hasPermission("uchat.cmd.clear-all")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.clear-all"));
		}
		if (p.hasPermission("uchat.cmd.spy")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.spy"));
		}
		if (p.hasPermission("uchat.cmd.mute")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.mute"));
		}
		if (p.hasPermission("uchat.cmd.tempmute")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.tempmute"));
		}
		if (p.hasPermission("uchat.cmd.ignore.player")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.ignore.player"));
		}
		if (p.hasPermission("uchat.cmd.ignore.channel")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.ignore.channel"));
		}
		if (p.hasPermission("uchat.cmd.reload")){
			p.sendMessage(UChat.get().getLang().get("help.cmd.reload"));
		}
		StringBuilder channels = new StringBuilder();
		for (UCChannel ch:UChat.get().getConfig().getChannels()){
			if (!(p instanceof Player) || UCPerms.channelReadPerm((Player)p, ch)){
				channels.append(", "+ch.getName());
			}
		}
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------------------------------------------ "));
		p.sendMessage(UChat.get().getLang().get("help.channels.available").replace("{channels}", channels.toString().substring(2)));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------------------------------------------ "));
	}

	private void sendChannelHelp(Player p) {
		StringBuilder channels = new StringBuilder();
		for (UCChannel ch:UChat.get().getConfig().getChannels()){
			if (!UCPerms.channelReadPerm(p, ch))continue;
			channels.append(", "+ch.getName());
		}
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7>> -------------- "+UChat.get().getLang().get("_UChat.prefix")+" Help &7-------------- <<"));
		p.sendMessage(UChat.get().getLang().get("help.channels.available").replace("{channels}", channels.toString().substring(2)));
		p.sendMessage(UChat.get().getLang().get("help.channels.enter"));
		p.sendMessage(UChat.get().getLang().get("help.channels.send"));
	}
	
	private void sendTellHelp(Player p) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7>> -------------- "+UChat.get().getLang().get("_UChat.prefix")+" Help &7-------------- <<"));
		p.sendMessage(UChat.get().getLang().get("help.tell.unlock"));
		p.sendMessage(UChat.get().getLang().get("help.tell.lock"));
		p.sendMessage(UChat.get().getLang().get("help.tell.send"));
		p.sendMessage(UChat.get().getLang().get("help.tell.respond"));
	}
	
}

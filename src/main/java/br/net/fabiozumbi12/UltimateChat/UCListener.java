package br.net.fabiozumbi12.UltimateChat;

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
import br.net.fabiozumbi12.UltimateChat.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.config.UCLang;

public class UCListener implements CommandExecutor,Listener {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		 UChat.logger.debug("onCommand - Label: "+label);
		 if (args.length == 0){
			 sender.sendMessage(ChatColor.AQUA + "---------------- " + UChat.pdf.getFullName() + " ----------------");
	         sender.sendMessage(ChatColor.AQUA + "Developed by " + ChatColor.GOLD + UChat.pdf.getAuthors() + ".");
	         sender.sendMessage(ChatColor.AQUA + "For more information about the commands, type [" + ChatColor.GOLD + "/"+label+" ?"+ChatColor.AQUA+"].");
	         sender.sendMessage(ChatColor.AQUA + "---------------------------------------------------");
	         return true;
		 }
		 
		 if (args.length == 1){
			 if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")){
				 sendHelp(sender);
				 return true;
			 }
					 
			 if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("uchat.cmd.reload")){
				 UChat.plugin.serv.getScheduler().cancelTasks(UChat.plugin);
				 UChat.config = new UCConfig(UChat.plugin, UChat.mainPath);
				 UChat.lang = new UCLang(UChat.plugin, UChat.logger, UChat.mainPath, UChat.config);
				 UChat.plugin.registerAliases();
				 for (Player p:Bukkit.getOnlinePlayers()){
					 if (UChat.config.getChannel(UChat.pChannels.get(p.getName())) == null){
						 UChat.pChannels.put(p.getName(), UChat.config.getDefChannel().getAlias());
					 }					 
				 }
				 UChat.plugin.initAutomessage();
				 UChat.lang.sendMessage(sender, "plugin.reloaded");
				 return true;
			 }			 
		 }		
		 		 		 
		 if (sender instanceof Player){
			 Player p = (Player) sender;
			 
			 //Listen cmd chat/uchat
			 if (cmd.getName().equalsIgnoreCase("uchat")){
				 
				 if (args.length == 1){
					 
					 if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){						 
						 sendHelp(sender);
						 return true;
					 }
									 
					 if (args[0].equalsIgnoreCase("clear")){	
						 if (!UCPerms.cmdPerm(p, "clear")){
							 UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
							 return true;
						 }
						 for (int i = 0; i < 100; i++){
							 UCMessages.sendPlayerFakeMessage(p, "{\"text\":\" \"}");
						 }						 
						 UChat.lang.sendMessage(p, UChat.lang.get("cmd.clear.cleared"));
						 return true;
					 }
					 
					 if (args[0].equalsIgnoreCase("clear-all")){	
						 if (!UCPerms.cmdPerm(p, "clear-all")){
							 UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
							 return true;
						 }
						 for (Player play:Bukkit.getOnlinePlayers()){
							 for (int i = 0; i < 100; i++){
								 if (!play.isOnline()){
									 break;
								 }
								 UCMessages.sendPlayerFakeMessage(play, "{\"text\":\" \"}");
							 }
						 }						 						 
						 UChat.lang.sendMessage(p, UChat.lang.get("cmd.clear.cleared"));
						 return true;
					 }
					 
					 if (args[0].equalsIgnoreCase("spy")){
						if (!UCPerms.cmdPerm(p, "spy")){
							UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
							return true;
						}
						
						if (!UChat.isSpy.contains(p.getName())){
							UChat.isSpy.add(p.getName());
							UChat.lang.sendMessage(p, UChat.lang.get("cmd.spy.enabled"));
						} else {
							UChat.isSpy.remove(p.getName());
							UChat.lang.sendMessage(p, UChat.lang.get("cmd.spy.disabled"));
						}
						return true;
					 }
				 }
				 
				 if (args.length == 2){
					 // chat ignore <channel/player>
					 if (args[0].equalsIgnoreCase("ignore")){						 						 
						 UCChannel ch = UChat.config.getChannel(args[1]);
						 if (Bukkit.getPlayer(args[1]) != null){
							 Player pi = Bukkit.getPlayer(args[1]);
							 if (!UCPerms.cmdPerm(p, "ignore.player")){
								 UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
								 return true;
							 }
							 if (UCMessages.isIgnoringPlayers(p.getName(), pi.getName())){
								 UCMessages.unIgnorePlayer(p.getName(), pi.getName());
								 UChat.lang.sendMessage(p, UChat.lang.get("player.unignoring").replace("{player}", pi.getName()));
							 } else {
								 UCMessages.ignorePlayer(p.getName(), pi.getName());
								 UChat.lang.sendMessage(p, UChat.lang.get("player.ignoring").replace("{player}", pi.getName()));
							 }
							 return true;
						 } else if (ch != null){	
							 if (!UCPerms.cmdPerm(p, "ignore.channel")){
								 UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
								 return true;
							 }
							 if (ch.isIgnoring(p.getName())){
								 ch.unIgnoreThis(p.getName());
								 UChat.lang.sendMessage(p, UChat.lang.get("channel.notignoring").replace("{channel}", ch.getName()));
							 } else {
								 ch.ignoreThis(p.getName());
								 UChat.lang.sendMessage(p, UChat.lang.get("channel.ignoring").replace("{channel}", ch.getName()));
							 }
							 return true;
						 } else {
							 UChat.lang.sendMessage(p, UChat.lang.get("channel.dontexist").replace("{channel}", args[1]));
							 return true;
						 }
					 }
					 
					 //chat mute <player>
					 if (args[0].equalsIgnoreCase("mute")){
						 if (!UCPerms.cmdPerm(p, "mute")){
							 UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
							 return true;
						 }
						 
						 String pname = args[1];
						 if (Bukkit.getPlayer(args[1]) != null){
							 pname = Bukkit.getPlayer(args[1]).getName();
						 }
						 
						 if (UChat.mutes.contains(pname)){
							 UChat.mutes.remove(pname);
							 UChat.config.unMuteInAllChannels(pname);
							 UChat.lang.sendMessage(p, UChat.lang.get("channel.unmuted.all").replace("{player}", pname));
						 } else {
							 UChat.mutes.add(pname);
							 UChat.config.muteInAllChannels(pname);
							 UChat.lang.sendMessage(p, UChat.lang.get("channel.muted.all").replace("{player}", pname));
						 }
						 return true;
					 }
				 }
				 
				 if (args.length == 3){
					 //chat mute <player>/<channel>
					 if (args[0].equalsIgnoreCase("mute")){
						 if (!UCPerms.cmdPerm(p, "mute")){
							 UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
							 return true;
						 }
						 UCChannel ch = UChat.config.getChannel(args[2]);
						 if (ch == null){
							 UChat.lang.sendMessage(p, UChat.lang.get("channel.dontexist").replace("{channel}", args[1]));
							 return true;
						 }
						 
						 String pname = args[1];
						 if (Bukkit.getPlayer(args[1]) != null){
							 pname = Bukkit.getPlayer(args[1]).getName();
						 }
						 
						 if (ch.isMuted(pname)){
							 ch.unMuteThis(pname);
							 UChat.lang.sendMessage(p, UChat.lang.get("channel.unmuted.this").replace("{player}", pname).replace("{channel}", ch.getName()));
						 } else {
							 ch.muteThis(pname);
							 UChat.lang.sendMessage(p, UChat.lang.get("channel.muted.this").replace("{player}", pname).replace("{channel}", ch.getName()));
						 }
						 return true;
					 }
				 }
			 }			 
		 } else {
			 if (UChat.config.getChAliases().contains(label.toLowerCase())){
				 if (args.length >= 1){
					UCChannel ch = UChat.config.getChannel(label);
					
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
		 
		 if (cmd.getName().equalsIgnoreCase("ubroadcast") && sender.hasPermission("uchat.broadcast")){
			 if (!UCUtil.sendBroadcast(sender, args, false)){
				 sendHelp(sender);
			 }
			 return true;
		 }
		 
		 if (cmd.getName().equalsIgnoreCase("umsg") && sender.hasPermission("uchat.cmd.umsg")){
			 UCUtil.sendUmsg(sender, args);
			 return true;
		 }
		 
		 sendHelp(sender);
		 return true;
	}
		
	@EventHandler(priority = EventPriority.LOWEST)
	public void onCmdChat(PlayerCommandPreprocessEvent e){
		String[] args = e.getMessage().replace("/", "").split(" ");
		String msg = null;
		if (e.getMessage().length() > args[0].length()+2){
			msg = e.getMessage().substring(args[0].length()+2);
		}
		Player p = e.getPlayer();
		UChat.logger.debug("PlayerCommandPreprocessEvent - Channel: "+args[0]);
		
		//check channels aliases
		if (UChat.config.getChAliases().contains(args[0].toLowerCase())){
			e.setCancelled(true);
			
			if (args.length == 1){
				UCChannel ch = UChat.config.getChannel(args[0]);
				if (ch != null){							
					if (!UCPerms.channelPerm(p, ch)){
						UChat.lang.sendMessage(p, UChat.lang.get("channel.nopermission").replace("{channel}", ch.getName()));
						return;
					}
					if (!ch.canLock()){
						UChat.lang.sendMessage(p, "help.channels.send");
						return;
					}
					if (UChat.pChannels.containsKey(p.getName()) && UChat.pChannels.get(p.getName()).equalsIgnoreCase(ch.getAlias())){
						UChat.lang.sendMessage(p, UChat.lang.get("channel.alreadyon").replace("{channel}", ch.getName()));
						return;
					}
					UChat.pChannels.put(p.getName(), ch.getAlias());
					UChat.lang.sendMessage(p, UChat.lang.get("channel.entered").replace("{channel}", ch.getName()));
				} else {
					UChat.lang.sendMessage(p, UChat.lang.get("channel.dontexist").replace("{channel}", args[0]));
					return;							
				}
				return;
			}
						
			if (args.length >= 1){
				UCChannel ch = UChat.config.getChannel(args[0]);
				if (ch != null && msg != null){
					if (!UCPerms.channelPerm(p, ch)){
						UChat.lang.sendMessage(p, UChat.lang.get("channel.nopermission").replace("{channel}", ch.getName()));
						return;
					}					
					
					//run bukkit chat event
					Set<Player> pls = new HashSet<Player>();
					pls.addAll(Bukkit.getOnlinePlayers());
					UChat.tempChannels.put(p.getName(), ch.getAlias());
					AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, p, msg, pls);
					Bukkit.getPluginManager().callEvent(event); 
					return;
				}			
			}
			
			//if /ch or /channel
			if (args[0].equalsIgnoreCase("ch") || args[0].equalsIgnoreCase("channel")){
				e.setCancelled(true);
				
				if (args.length == 2){				
					UCChannel ch = UChat.config.getChannel(args[1]);
					if (ch == null){
						UChat.lang.sendMessage(p, UChat.lang.get("channel.dontexist").replace("{channel}", args[1]));
						return;
					}
					if (!UCPerms.channelPerm(p, ch)){
						UChat.lang.sendMessage(p, UChat.lang.get("channel.nopermission").replace("{channel}", ch.getName()));
						return;
					}
					if (UChat.pChannels.containsKey(p.getName()) && UChat.pChannels.get(p.getName()).equals(ch.getAlias())){
						UChat.lang.sendMessage(p, UChat.lang.get("channel.alreadyon").replace("{channel}", ch.getName()));
						return;
					}
					
					UChat.pChannels.put(p.getName(), ch.getAlias());
					UChat.lang.sendMessage(p, UChat.lang.get("channel.entered").replace("{channel}", ch.getName()));
					return;
				}
			}
			sendChannelHelp(p);
		}
		
		//check tell aliases
		if (UChat.config.getTellAliases().contains(args[0])){
			e.setCancelled(true);
			
			if (!UCPerms.cmdPerm(p, "tell")){
				UChat.lang.sendMessage(p, UChat.lang.get("cmd.nopermission"));
				return;
			}			

			if (args.length >= 2){
				if (args[0].equalsIgnoreCase("r")){
					if (UChat.respondTell.containsKey(p.getName())){
						Player receiver = UChat.plugin.serv.getPlayer(UChat.respondTell.get(p.getName()));
						
						sendTell(p, receiver, msg);
						return;
					} else {
						UChat.lang.sendMessage(p, UChat.lang.get("cmd.tell.nonetorespond"));
						return;
					}
				}
			}
			
			if (args.length == 2){
				Player receiver = UChat.plugin.serv.getPlayer(args[1]);
				if (receiver == null || !receiver.isOnline()){
					UChat.lang.sendMessage(p, UChat.lang.get("listener.invalidplayer"));
					return;
				}
				if (receiver.equals(p)){
					UChat.lang.sendMessage(p, UChat.lang.get("cmd.tell.self"));
					return;
				}
				
				if (UChat.tellPlayers.containsKey(p.getName()) && UChat.tellPlayers.get(p.getName()).equals(receiver.getName())){
					UChat.tellPlayers.remove(p.getName());
					UChat.lang.sendMessage(p, UChat.lang.get("cmd.tell.unlocked").replace("{player}", receiver.getName()));
				} else {
					UChat.tellPlayers.put(p.getName(), receiver.getName());
					UChat.lang.sendMessage(p, UChat.lang.get("cmd.tell.locked").replace("{player}", receiver.getName()));
				}
				return;
			}
			
			//tell <nick> <mensagem...>
			if (args.length >= 3){
				if (args[1].equalsIgnoreCase("console")){
					msg = msg.substring(args[1].length()+1);
					
					String prefix = UChat.config.getString("tell.prefix");
					String format = UChat.config.getString("tell.format");
					
					prefix = UCMessages.formatTags("", prefix, p, UChat.plugin.serv.getConsoleSender(), msg, new UCChannel("tell"));
					format = UCMessages.formatTags("tell", format, p, UChat.plugin.serv.getConsoleSender(), msg, new UCChannel("tell"));
							
					p.sendMessage(prefix+format);
					UChat.plugin.serv.getConsoleSender().sendMessage(prefix+format);
					return;
				}
				
				//send to player
				Player receiver = UChat.plugin.serv.getPlayer(args[1]);
					
				if (receiver == null || !receiver.isOnline()){
					UChat.lang.sendMessage(p, UChat.lang.get("listener.invalidplayer"));
					return;
				}
				
				if (receiver.equals(p)){
					UChat.lang.sendMessage(p, UChat.lang.get("cmd.tell.self"));
					return;
				}
				
				//remove receiver name
				msg = msg.substring(args[1].length()+1);
				
				UChat.tempTellPlayers.put(p.getName(), receiver.getName());
				sendTell(p, receiver, msg);
				return;
			}	
			sendTellHelp(p);
		}		
	}
		
	@EventHandler
	public void onServerCmd(ServerCommandEvent e){
		String[] args = e.getCommand().replace("/", "").split(" ");
		String msg = null;
		if (e.getCommand().length() > args[0].length()+1){
			msg = e.getCommand().substring(args[0].length()+1);
		}
		
		if (UChat.config.getTellAliases().contains(args[0])){
			if (args.length >= 3){
				if (UChat.plugin.serv.getPlayer(args[1]) == null || !UChat.plugin.serv.getPlayer(args[1]).isOnline()){
					UChat.lang.sendMessage(e.getSender(), "listener.invalidplayer");
					return;
				}
				Player p = UChat.plugin.serv.getPlayer(args[1]);
				msg = msg.substring(args[1].length()+1);
				
				String prefix = UChat.config.getString("tell.prefix");
				String format = UChat.config.getString("tell.format");
				
				prefix = UCMessages.formatTags("", prefix, UChat.plugin.serv.getConsoleSender(), p, msg, new UCChannel("tell"));
				format = UCMessages.formatTags("tell", format, UChat.plugin.serv.getConsoleSender(), p, msg, new UCChannel("tell"));
						
				p.sendMessage(prefix+format);
				UChat.plugin.serv.getConsoleSender().sendMessage(prefix+format);
				return;				
			}
			return;
		}
		
	}
	
	private void sendTell(Player p, Player receiver, String msg){
		Player tellreceiver = receiver;	
		if (receiver == null || !receiver.isOnline() || !p.canSee(receiver)){
			UChat.lang.sendMessage(p, UChat.lang.get("listener.invalidplayer"));
			return;
		}							
		UChat.respondTell.put(tellreceiver.getName(),p.getName());
		UCMessages.sendFancyMessage(new String[0], msg, null, p, tellreceiver);			
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e){
		if (e.isCancelled()){
			return;
		}
		e.setCancelled(true);
		Player p = e.getPlayer();
		UChat.logger.debug("AsyncPlayerChatEvent - Channel: "+UChat.pChannels.get(p.getName()));		
		
		if (UChat.tellPlayers.containsKey(p.getName())){
			Player tellreceiver = UChat.plugin.serv.getPlayer(UChat.tellPlayers.get(p.getName()));
			sendTell(p, tellreceiver, e.getMessage());
			e.setCancelled(true);
		} else {
			UCChannel ch = UChat.config.getChannel(UChat.pChannels.get(p.getName()));
			if (UChat.tempChannels.containsKey(p.getName()) && !UChat.tempChannels.get(p.getName()).equals(ch.getAlias())){
				ch = UChat.config.getChannel(UChat.tempChannels.get(p.getName()));
				UChat.logger.debug("AsyncPlayerChatEvent - TempChannel: "+UChat.tempChannels.get(p.getName()));
				UChat.tempChannels.remove(p.getName());
			}
			
			if (UChat.mutes.contains(p.getName()) || ch.isMuted(p.getName())){
				UChat.lang.sendMessage(p, "channel.muted");
				return;
			}			
			
			if (ch.isCmdAlias()){
				String start = ch.getAliasCmd();
				if (start.startsWith("/")){
					start = start.substring(1);
				}
				if (ch.getAliasSender().equalsIgnoreCase("console")){					
					UCUtil.performCommand(Bukkit.getConsoleSender(), start+" "+e.getMessage());
				} else {
					UCUtil.performCommand(p, start+" "+e.getMessage());
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
		UChat.pChannels.put(p.getName(), UChat.config.getDefChannel().getAlias());
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
		for (String play:UChat.tellPlayers.keySet()){
			if (play.equals(p.getName()) || UChat.tellPlayers.get(play).equals(p.getName())){
				toRemove.add(play);				
			}
		}	
		for (String remove:toRemove){
			UChat.tellPlayers.remove(remove);
		}
		List<String> toRemove2 = new ArrayList<String>();
		for (String play:UChat.respondTell.keySet()){
			if (play.equals(p.getName()) || UChat.respondTell.get(play).equals(p.getName())){
				toRemove2.add(play);				
			}
		}	
		for (String remove:toRemove2){
			UChat.respondTell.remove(remove);
		}
		if (UChat.pChannels.containsKey(p.getName())){
			UChat.pChannels.remove(p.getName());
		}
		if (UChat.tempChannels.containsKey(p.getName())){
			UChat.tempChannels.remove(p.getName());
		}
	}
		
	public void sendHelp(CommandSender p){		
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7--------------- "+UChat.lang.get("_UChat.prefix")+" Help &7---------------"));		
		p.sendMessage(UChat.lang.get("help.channels.enter"));
		p.sendMessage(UChat.lang.get("help.channels.send"));
		if (p.hasPermission("uchat.cmd.tell")){
			p.sendMessage(UChat.lang.get("help.tell.lock"));
			p.sendMessage(UChat.lang.get("help.tell.send"));
			p.sendMessage(UChat.lang.get("help.tell.respond"));
		}
		if (p.hasPermission("uchat.broadcast")){
			p.sendMessage(UChat.lang.get("help.cmd.broadcast"));
		}
		if (p.hasPermission("uchat.cmd.umsg")){
			p.sendMessage(UChat.lang.get("help.cmd.umsg"));
		}
		if (p.hasPermission("uchat.cmd.clear")){
			p.sendMessage(UChat.lang.get("help.cmd.clear"));
		}
		if (p.hasPermission("uchat.cmd.clear-all")){
			p.sendMessage(UChat.lang.get("help.cmd.clear-all"));
		}
		if (p.hasPermission("uchat.cmd.spy")){
			p.sendMessage(UChat.lang.get("help.cmd.spy"));
		}
		if (p.hasPermission("uchat.cmd.mute")){
			p.sendMessage(UChat.lang.get("help.cmd.mute"));
		}
		if (p.hasPermission("uchat.cmd.ignore.player")){
			p.sendMessage(UChat.lang.get("help.cmd.ignore.player"));
		}
		if (p.hasPermission("uchat.cmd.ignore.channel")){
			p.sendMessage(UChat.lang.get("help.cmd.ignore.channel"));
		}
		if (p.hasPermission("uchat.cmd.reload")){
			p.sendMessage(UChat.lang.get("help.cmd.reload"));
		}
		StringBuilder channels = new StringBuilder();
		for (UCChannel ch:UChat.config.getChannels()){
			if (!(p instanceof Player) || UCPerms.channelPerm((Player)p, ch)){
				channels.append(", "+ch.getName());
			}
		}
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------------------------------------------ "));
		p.sendMessage(UChat.lang.get("help.channels.available").replace("{channels}", channels.toString().substring(2)));
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7------------------------------------------ "));
	}

	private void sendChannelHelp(Player p) {
		StringBuilder channels = new StringBuilder();
		for (UCChannel ch:UChat.config.getChannels()){
			if (!UCPerms.channelPerm(p, ch))continue;
			channels.append(", "+ch.getName());
		}
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7>> -------------- "+UChat.lang.get("_UChat.prefix")+" Help &7-------------- <<"));
		p.sendMessage(UChat.lang.get("help.channels.available").replace("{channels}", channels.toString().substring(2)));
		p.sendMessage(UChat.lang.get("help.channels.enter"));
		p.sendMessage(UChat.lang.get("help.channels.send"));
	}
	
	private void sendTellHelp(Player p) {
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7>> -------------- "+UChat.lang.get("_UChat.prefix")+" Help &7-------------- <<"));
		p.sendMessage(UChat.lang.get("help.tell.lock"));
		p.sendMessage(UChat.lang.get("help.tell.send"));
		p.sendMessage(UChat.lang.get("help.tell.respond"));
	}
	
}

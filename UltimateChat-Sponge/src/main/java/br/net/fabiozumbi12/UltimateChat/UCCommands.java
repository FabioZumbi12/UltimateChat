package br.net.fabiozumbi12.UltimateChat;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import br.net.fabiozumbi12.UltimateChat.config.UCLang;

public class UCCommands {
	
	UCCommands(UChat plugin) {
		
		unregisterCmd("uchat");
		Sponge.getCommandManager().register(plugin, uchat(),"ultimatechat","uchat","chat");	
		
		if (UChat.get().getConfig().getBool("tell","enable")){
			registerTellAliases();
		}
		if (UChat.get().getConfig().getBool("broadcast","enable")){
			registerUbroadcastAliases();
		}
		registerChannelAliases();		
		registerUmsgAliases();
		registerChAliases();
		
	}
	

	void removeCmds(){
		Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get("ultimatechat").get());
		
		if (UChat.get().getConfig().getBool("tell","enable")){
			for (String cmd:UChat.get().getConfig().getTellAliases()){			
				Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
			}
		}
		if (UChat.get().getConfig().getBool("broadcast","enable")){
			for (String cmd:UChat.get().getConfig().getBroadcastAliases()){
				Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
			}
		}
		for (String cmd:UChat.get().getConfig().getChAliases()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}		
		for (String cmd:UChat.get().getConfig().getMsgAliases()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
		for (String cmd:UChat.get().getConfig().getChCmd()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
	}
		
	private void unregisterCmd(String cmd){
		if (Sponge.getCommandManager().get(cmd).isPresent()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
	}
	
	private void registerTellAliases() {		
		//register tell aliases
		for (String tell:UChat.get().getConfig().getTellAliases()){
			unregisterCmd(tell);
			if (tell.equals("r")){
				Sponge.getCommandManager().register(UChat.plugin, CommandSpec.builder()
						.arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
						.permission("uchat.cmd.tell")
					    .description(Text.of("Respond tell of other players."))
					    .executor((src, args) -> { {
					    	if (src instanceof Player){
					    		Player p = (Player) src;						
					    		if (UChat.respondTell.containsKey(p.getName())){
									Optional<Player> receiver = Sponge.getServer().getPlayer(UChat.respondTell.get(p.getName()));
									
									sendTell(p, receiver, args.<String>getOne("message").get());
									return CommandResult.success();	
								} else {
									throw new CommandException(UCLang.getText("cmd.tell.nonetorespond"));
								}
					    	}				    	
					    	return CommandResult.success();	
					    }})
					    .build(), tell);
			} else {
				Sponge.getCommandManager().register(UChat.plugin, CommandSpec.builder()
						.arguments(GenericArguments.player(Text.of("player")), GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("message"))))
					    .description(Text.of("Lock your chat with a player."))
					    .permission("uchat.cmd.tell")
					    .executor((src, args) -> { {
					    	Player receiver = args.<Player>getOne("player").get();
					    	if (src instanceof Player){					    		
					    		Player p = (Player) src;
					    		if (args.<String>getOne("message").isPresent()){
					    			if (receiver.equals(p)){
					    				throw new CommandException(UCLang.getText("cmd.tell.self"), true);
									}
																		
									UChat.tempTellPlayers.put(p.getName(), receiver.getName());
									sendTell(p, args.<Player>getOne("player"), args.<String>getOne("message").get());
									return CommandResult.success();
					    		} else {
					    			if (receiver.equals(p)){
										throw new CommandException(UCLang.getText("cmd.tell.self"), true);
									}
									
									if (UChat.tellPlayers.containsKey(p.getName()) && UChat.tellPlayers.get(p.getName()).equals(receiver.getName())){
										UChat.tellPlayers.remove(p.getName());
										UCLang.sendMessage(p, UCLang.get("cmd.tell.unlocked").replace("{player}", receiver.getName()));
									} else {
										UChat.tellPlayers.put(p.getName(), receiver.getName());
										UCLang.sendMessage(p, UCLang.get("cmd.tell.locked").replace("{player}", receiver.getName()));
									}
					    		}					    		
					    	} else if (args.<String>getOne("message").isPresent()){
					    		String msg = args.<String>getOne("message").get();
					    		String prefix = UChat.get().getConfig().getString("tell","prefix");
								String format = UChat.get().getConfig().getString("tell","format");
								
								prefix = UCMessages.formatTags("", prefix, Sponge.getServer().getConsole(), receiver, msg, new UCChannel("tell"));
								format = UCMessages.formatTags("tell", format, Sponge.getServer().getConsole(), receiver, msg, new UCChannel("tell"));
										
								receiver.sendMessage(UCUtil.toText(prefix+format));
								Sponge.getServer().getConsole().sendMessage(UCUtil.toText(prefix+format));
					    	}
					    	return CommandResult.success();	
					    }})
					    .build(), tell);
			}
			
		}
	}

	private void registerChAliases() {
		//register ch cmds aliases
		for (String cmd:UChat.get().getConfig().getChCmd()){
			unregisterCmd(cmd);
			Sponge.getCommandManager().register(UChat.plugin, CommandSpec.builder()
					.arguments(new ChannelCommandElement(Text.of("channel")))
				    .description(Text.of("Join in a channel if you have permission."))
				    .executor((src, args) -> { {
				    	if (src instanceof Player){
				    		Player p = (Player) src;
				    		UCChannel ch = args.<UCChannel>getOne("channel").get();							
							if (!UChat.get().getPerms().channelPerm(p, ch)){
								throw new CommandException(UCUtil.toText(UCLang.get("channel.nopermission").replace("{channel}", ch.getName())));	
							}
							if (UChat.pChannels.containsKey(p.getName()) && UChat.pChannels.get(p.getName()).equals(ch.getAlias())){
								UCLang.sendMessage(p, UCLang.get("channel.alreadyon").replace("{channel}", ch.getName()));
								return CommandResult.success();	
							}
							
							UChat.pChannels.put(p.getName(), ch.getAlias());
							UCLang.sendMessage(p, UCLang.get("channel.entered").replace("{channel}", ch.getName()));
				    	} 
				    	return CommandResult.success();	
				    }})
				    .build(), cmd);
		}
	}

	private void registerUmsgAliases() {
		//register umsg aliases
		for (String msga:UChat.get().getConfig().getMsgAliases()){
			unregisterCmd(msga);
			Sponge.getCommandManager().register(UChat.plugin, CommandSpec.builder()
					.arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
					.permission("uchat.cmd.message")
				    .description(Text.of("Send a message directly to a player."))
				    .executor((src, args) -> { {
				    	Player receiver = args.<Player>getOne("player").get();
				    	String msg = args.<String>getOne("message").get();
				    	receiver.sendMessage(UCUtil.toText(msg));
						Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> Private to &6"+receiver.getName()+"&8: &r"+msg));
				    	return CommandResult.success();	
				    }})
				    .build(), msga);
		}
	}

	private void registerUbroadcastAliases() {
		//register ubroadcast aliases
		for (String brod:UChat.get().getConfig().getBroadcastAliases()){
			unregisterCmd(brod);
			Sponge.getCommandManager().register(UChat.plugin, CommandSpec.builder()
					.arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
					.permission("uchat.cmd.broadcast")
				    .description(Text.of("Command to send broadcast to server."))
				    .executor((src, args) -> { {
				    	if (!UCUtil.sendBroadcast(src, args.<String>getOne("message").get().split(" "), false)){
							sendHelp(src);
						}  
				    	return CommandResult.success();	
				    }})
				    .build(), brod);
		}
	}

	private void registerChannelAliases() {
		//register channel aliases
		for (String cha:UChat.get().getConfig().getChAliases()){
			unregisterCmd(cha);
			UCChannel ch = UChat.get().getConfig().getChannel(cha);
			Sponge.getCommandManager().register(UChat.plugin, CommandSpec.builder()
					.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("message"))))
					.permission("uchat.channel."+ch.getName())
				    .description(Text.of("Command to use channel "+ch.getName()+"."))
				    .executor((src, args) -> { {
				    	if (src instanceof Player){
				    		if (args.<String>getOne("message").isPresent()){
				    			UChat.tempChannels.put(src.getName(), ch.getAlias());
				    			/*
				    			Text msg = Text.of(args.<String>getOne("message").get());				    			
				    			MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
		    							Cause.source(src).named(NamedCause.notifier(src)).build(), 
		    							src.getMessageChannel(), 
		    							Optional.of(src.getMessageChannel()), 				    							
		    							new MessageEvent.MessageFormatter(Text.builder("<" + src.getName() + "> ")
		    		                            .onShiftClick(TextActions.insertText(src.getName()))
		    		                            .onClick(TextActions.suggestCommand("/msg " + src.getName()))
		    		                            .build(), msg), 
		    		                    msg, 
		    							false);
				    			Sponge.getEventManager().post(event);*/
				    					
				    			if (UChat.mutes.contains(src.getName()) || ch.isMuted(src.getName())){
				    				UCLang.sendMessage(src, "channel.muted");
				    				return CommandResult.success();
				    			}
				    			
				    			Object[] chArgs = UCMessages.sendFancyMessage(new String[0], args.<String>getOne("message").get(), ch, src, null);  
				    			if (chArgs != null){
				    				MutableMessageChannel msgCh = (MutableMessageChannel) chArgs[0];	
				    				msgCh.send(Text.join((Text)chArgs[1],(Text)chArgs[2],(Text)chArgs[3]));
				    			}
				    		} else {
				    			if (!ch.canLock()){
				    				UCLang.sendMessage(src, "help.channels.send");
									return CommandResult.success();
								}
					    		if (UChat.pChannels.containsKey(src.getName()) && UChat.pChannels.get(src.getName()).equalsIgnoreCase(ch.getAlias())){
					    			UChat.tempChannels.put(src.getName(), ch.getAlias());
					    			UCLang.sendMessage(src, UCLang.get("channel.alreadyon").replace("{channel}", ch.getName()));
									return CommandResult.success();
								}
					    		UChat.pChannels.put(src.getName(), ch.getAlias());
					    		UCLang.sendMessage(src, UCLang.get("channel.entered").replace("{channel}", ch.getName()));	
				    		}
				    	} else {
				    		UCMessages.sendFancyMessage(new String[0], args.<String>getOne("message").get(), ch, src, null);  
				    	}				    	
				    	return CommandResult.success();	
				    }})
				    .build(), cha);
		}
	}
	
	private CommandCallable uchat() {
		CommandSpec reload = CommandSpec.builder()
				.description(Text.of("Command to reload uchat."))
				.permission("uchat.cmd.reload")
				.executor((src,args) -> {{
					//uchat reload
					try {
						UChat.get().reload();
						UCLang.sendMessage(src, "plugin.reloaded");
					} catch (Exception e) {
						e.printStackTrace();
					}
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec clear = CommandSpec.builder()
				.description(Text.of("Clear your chat."))
				.permission("uchat.cmd.clear")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat clear
						for (int i = 0; i < 100; i++){
							p.sendMessage(Text.of(" "));
						}						 
			    		UCLang.sendMessage(src, "cmd.clear.cleared");	
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec clearAll = CommandSpec.builder()
				.description(Text.of("Clear the chat of all online players."))
				.permission("uchat.cmd.clear-all")
				.executor((src,args) -> {{
					//uchat clear-all
					for (Player play:Sponge.getServer().getOnlinePlayers()){
						for (int i = 0; i < 100; i++){
							if (!play.isOnline()){
								continue;
							}
							play.sendMessage(Text.of(" "));
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec spy = CommandSpec.builder()
				.description(Text.of("Turn on the social spy."))
				.permission("uchat.cmd.spy")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat spy
						if (!UChat.isSpy.contains(p.getName())){
							UChat.isSpy.add(p.getName());
							UCLang.sendMessage(src, "cmd.spy.enabled");
						} else {
							UChat.isSpy.remove(p.getName());
							UCLang.sendMessage(src, "cmd.spy.disabled");
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec ignorePlayer = CommandSpec.builder()
				.arguments(GenericArguments.player(Text.of("player")))
				.description(Text.of("Ignore a player."))
				.permission("uchat.cmd.ignore.player")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat ignore player
						Player play = args.<Player>getOne("player").get();
						if (play.equals(p)){
							throw new CommandException(UCLang.getText("cmd.ignore.self"), true);
						}
		    			if (UCMessages.isIgnoringPlayers(p.getName(), play.getName())){
							UCMessages.unIgnorePlayer(p.getName(), play.getName());
							UCLang.sendMessage(src, UCLang.get("player.unignoring").replace("{player}", play.getName()));
						} else {
							UCMessages.ignorePlayer(p.getName(), play.getName());
							UCLang.sendMessage(src, UCLang.get("player.ignoring").replace("{player}", play.getName()));
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec ignoreChannel = CommandSpec.builder()
				.arguments(new ChannelCommandElement(Text.of("channel")))
				.description(Text.of("Ignore a channel."))
				.permission("uchat.cmd.ignore.channel")
				.executor((src,args) -> {{
					if (src instanceof Player){
						Player p = (Player) src;
						//uchat ignore channel
						UCChannel ch = args.<UCChannel>getOne("channel").get();
		    			if (ch.isIgnoring(p.getName())){
							ch.unIgnoreThis(p.getName());
							UCLang.sendMessage(src, UCLang.get("channel.notignoring").replace("{channel}", ch.getName()));
						} else {
							ch.ignoreThis(p.getName());
							UCLang.sendMessage(src, UCLang.get("channel.ignoring").replace("{channel}", ch.getName()));
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec mute = CommandSpec.builder()
				.arguments(GenericArguments.player(Text.of("player")),GenericArguments.optional(new ChannelCommandElement(Text.of("channel"))))
				.description(Text.of("Mute a player."))
				.permission("uchat.cmd.mute")
				.executor((src,args) -> {{
					//uchat ignore channel
					Player play = args.<Player>getOne("player").get();
					if (args.<UCChannel>getOne("channel").isPresent()){
						UCChannel ch = args.<UCChannel>getOne("channel").get();
						if (ch.isMuted(play.getName())){
							ch.unMuteThis(play.getName());
							UCLang.sendMessage(src, UCLang.get("channel.unmuted.this").replace("{player}", play.getName()).replace("{channel}", ch.getName()));
						} else {
							ch.muteThis(play.getName());
							UCLang.sendMessage(src, UCLang.get("channel.muted.this").replace("{player}", play.getName()).replace("{channel}", ch.getName()));
						}
					} else {
						if (UChat.mutes.contains(play.getName())){
							 UChat.mutes.remove(play.getName());
							 UChat.get().getConfig().unMuteInAllChannels(play.getName());
							 UCLang.sendMessage(src, UCLang.get("channel.unmuted.all").replace("{player}", play.getName()));
						 } else {
							 UChat.mutes.add(play.getName());
							 UChat.get().getConfig().muteInAllChannels(play.getName());
							 UCLang.sendMessage(src, UCLang.get("channel.muted.all").replace("{player}", play.getName()));
						 }
					}										
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec help = CommandSpec.builder()
				.description(Text.of("Se help about commands."))
				.executor((src,args) -> {{
					sendHelp(src);
		    		return CommandResult.success();	
				}})
				.build();
		
		//uchat <args...>
		CommandSpec uchat = CommandSpec.builder()
			    .description(Text.of("Main command for uchat."))
			    .executor((src, args) -> { {	    	
			    	//no args
			    	src.sendMessage(UCUtil.toText("&b---------------- "+UChat.plugin.getName()+" "+UChat.plugin.getVersion().get()+" ---------------"));
			    	src.sendMessage(UCUtil.toText("&bDeveloped by &6" + UChat.plugin.getAuthors().get(0) + "."));
			    	src.sendMessage(UCUtil.toText("&bFor more information about the commands, type [" + "&6/uchat ?&b]."));
			    	src.sendMessage(UCUtil.toText("&b---------------------------------------------------"));			         
			    	return CommandResult.success();	
			    }})
			    .child(help, "?")
			    .child(reload, "reload")
			    .child(clear, "clear")
			    .child(clearAll, "clear-all")
			    .child(spy, "spy")
			    .child(CommandSpec.builder()
			    		.child(ignorePlayer, "player")
			    		.child(ignoreChannel, "channel")
			    		.build(), "ignore")
			    .child(mute, "mute")
			    .build();
		
		return uchat;
	}

	private void sendTell(Player p, Optional<Player> receiver, String msg){		
		if (!receiver.isPresent() || !receiver.get().isOnline() || !p.canSee(receiver.get())){
			UCLang.sendMessage(p, "listener.invalidplayer");
			return;
		}	
		Player tellreceiver = receiver.get();	
		UChat.respondTell.put(tellreceiver.getName(),p.getName());
		UCMessages.sendFancyMessage(new String[0], msg, null, p, tellreceiver);			
	}
	
	private void sendHelp(CommandSource p){		
		p.sendMessage(UCUtil.toText("&7--------------- "+UCLang.get("_UChat.prefix")+" Help &7---------------"));		
		p.sendMessage(UCLang.getText("help.channels.enter"));
		p.sendMessage(UCLang.getText("help.channels.send"));
		if (p.hasPermission("uchat.cmd.tell")){
			p.sendMessage(UCLang.getText("help.tell.lock"));
			p.sendMessage(UCLang.getText("help.tell.send"));
			p.sendMessage(UCLang.getText("help.tell.respond"));
		}
		if (p.hasPermission("uchat.broadcast")){
			p.sendMessage(UCLang.getText("help.cmd.broadcast"));
		}
		if (p.hasPermission("uchat.cmd.umsg")){
			p.sendMessage(UCLang.getText("help.cmd.umsg"));
		}
		if (p.hasPermission("uchat.cmd.clear")){
			p.sendMessage(UCLang.getText("help.cmd.clear"));
		}
		if (p.hasPermission("uchat.cmd.clear-all")){
			p.sendMessage(UCLang.getText("help.cmd.clear-all"));
		}
		if (p.hasPermission("uchat.cmd.spy")){
			p.sendMessage(UCLang.getText("help.cmd.spy"));
		}
		if (p.hasPermission("uchat.cmd.mute")){
			p.sendMessage(UCLang.getText("help.cmd.mute"));
		}
		if (p.hasPermission("uchat.cmd.ignore.player")){
			p.sendMessage(UCLang.getText("help.cmd.ignore.player"));
		}
		if (p.hasPermission("uchat.cmd.ignore.channel")){
			p.sendMessage(UCLang.getText("help.cmd.ignore.channel"));
		}
		if (p.hasPermission("uchat.cmd.reload")){
			p.sendMessage(UCLang.getText("help.cmd.reload"));
		}
		StringBuilder channels = new StringBuilder();
		for (UCChannel ch:UChat.get().getConfig().getChannels()){
			if (!(p instanceof Player) || UChat.get().getPerms().channelPerm((Player)p, ch)){
				channels.append(", "+ch.getName());
			}
		}
		p.sendMessage(UCUtil.toText("&7------------------------------------------ "));
		p.sendMessage(UCUtil.toText(UCLang.get("help.channels.available").replace("{channels}", channels.toString().substring(2))));
		p.sendMessage(UCUtil.toText("&7------------------------------------------ "));
	}
}

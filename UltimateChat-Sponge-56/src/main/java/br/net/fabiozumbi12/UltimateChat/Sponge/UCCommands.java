package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;

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
			Optional<? extends CommandMapping> cmdo = Sponge.getCommandManager().get(cmd);
			if (cmdo.isPresent())
			Sponge.getCommandManager().removeMapping(cmdo.get());
		}		
		for (String cmd:UChat.get().getConfig().getMsgAliases()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
		for (String cmd:UChat.get().getConfig().getChCmd()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
	}
		
	public void unregisterCmd(String cmd){
		if (Sponge.getCommandManager().get(cmd).isPresent()){
			Sponge.getCommandManager().removeMapping(Sponge.getCommandManager().get(cmd).get());
		}
	}
	
	private void registerTellAliases() {		
		//register tell aliases
		for (String tell:UChat.get().getConfig().getTellAliases()){
			unregisterCmd(tell);
			if (tell.equals("r")){
				Sponge.getCommandManager().register(UChat.get().instance(), CommandSpec.builder()
						.arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
						.permission("uchat.cmd.tell")
					    .description(Text.of("Respond private messages of other players."))
					    .executor((src, args) -> { {
					    	if (src instanceof Player){
					    		Player p = (Player) src;						
					    		if (UChat.respondTell.containsKey(p.getName())){
					    			String recStr = UChat.respondTell.get(p.getName());	
					    			Text msg = Text.of(args.<String>getOne("message").get());	
					    			
					    			if (recStr.equals("CONSOLE")){
										UChat.respondTell.put("CONSOLE", p.getName());
										UChat.command.add(p.getName());
										sendPreTell(p, Sponge.getServer().getConsole(), msg);										
									} else {
										Optional<Player> optRec = Sponge.getServer().getPlayer(recStr);
										if (!optRec.isPresent()){
											throw new CommandException(UChat.get().getLang().getText("cmd.tell.nonetorespond"));
										} else {
											Player receiver = optRec.get();
											UChat.respondTell.put(receiver.getName(), p.getName());
											UChat.command.add(p.getName());
											sendPreTell(p, receiver, msg);	
										}																			
									}
					    			
					    			return CommandResult.success();
								} else {
									throw new CommandException(UChat.get().getLang().getText("cmd.tell.nonetorespond"));
								}
					    	}				    	
					    	return CommandResult.success();	
					    }})
					    .build(), tell);
			} else {
				Sponge.getCommandManager().register(UChat.get().instance(), CommandSpec.builder()
						.arguments(GenericArguments.optional(GenericArguments.firstParsing(GenericArguments.player(Text.of("receiver"))), GenericArguments.string(Text.of("receiver"))), GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("message"))))
					    .description(Text.of("Lock your chat with a player or send private messages."))
					    .permission("uchat.cmd.tell")
					    .executor((src, args) -> { {					    	
					    	if (!args.<Object>getOne("receiver").isPresent()){
					    		if (UChat.tellPlayers.containsKey(src.getName())){
									String tp = UChat.tellPlayers.get(src.getName());
									UChat.tellPlayers.remove(src.getName());
									UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", tp));
									return CommandResult.success();
								}
					    	} else {
					    		Object recObj = args.<Object>getOne("receiver").get();
						    	if (src instanceof Player){
						    		Player p = (Player) src;					    		
						    		if (args.<String>getOne("message").isPresent()){
						    			Text msg = Text.of(args.<String>getOne("message").get());	
						    			
						    			//receiver as player
						    			if (recObj instanceof Player){
						    				Player receiver = (Player) recObj;
						    				if (receiver.equals(p)){
							    				throw new CommandException(UChat.get().getLang().getText("cmd.tell.self"), true);
											}									
											//sendTell(p, args.<Player>getOne("player"), args.<String>getOne("message").get());
																				
											if (!receiver.isOnline() || !p.canSee(receiver)){
												UChat.get().getLang().sendMessage(p, "listener.invalidplayer");
												return CommandResult.success();
											}
											
											UChat.tempTellPlayers.put(p.getName(), receiver.getName());
											UChat.command.add(p.getName());										
											
											sendPreTell(p, receiver, msg);
						    			} 
						    			
						    			//if receiver as console
						    			else if (recObj.toString().equalsIgnoreCase("console")){
						    				UChat.tempTellPlayers.put(p.getName(), "CONSOLE");
											UChat.command.add(p.getName());
											sendPreTell(p, Sponge.getServer().getConsole(), msg);
						    			}
						    			
						    			return CommandResult.success();					    			
						    		} 
						    		//lock tell
						    		else if (recObj instanceof Player ){
					    				Player receiver = (Player) recObj;
					    				
					    				if (!receiver.isOnline() || !p.canSee(receiver)){
											throw new CommandException(UChat.get().getLang().getText("listener.invalidplayer"), true);
										}
					    				
					    				if (receiver.equals(p)){
											throw new CommandException(UChat.get().getLang().getText("cmd.tell.self"), true);
										}
										
										if (UChat.tellPlayers.containsKey(p.getName()) && UChat.tellPlayers.get(p.getName()).equals(receiver.getName())){
											UChat.tellPlayers.remove(p.getName());
											UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", receiver.getName()));
										} else {
											UChat.tellPlayers.put(p.getName(), receiver.getName());
											UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.locked").replace("{player}", receiver.getName()));
										}
										return CommandResult.success();	
					    			}				    		
						    	} 
						    	//console to player
						    	else if (src instanceof ConsoleSource && recObj instanceof Player && args.<String>getOne("message").isPresent()){
						    		String msg = args.<String>getOne("message").get();
						    		Player receiver = (Player) recObj;
						    		if (!receiver.isOnline()){
										UChat.get().getLang().sendMessage(Sponge.getServer().getConsole(), "listener.invalidplayer");
										return CommandResult.success();
									}
						    		
						    		UChat.tempTellPlayers.put("CONSOLE", receiver.getName());
									UChat.command.add("CONSOLE");
									
						    		sendPreTell(Sponge.getServer().getConsole(), receiver, Text.of(msg));		
						    		return CommandResult.success();	
						    	}
					    	}					    	
					    	
					    	sendTellHelp(src);
					    	return CommandResult.success();	
					    }})
					    .build(), tell);
			}			
		}
	}
	
	private void sendPreTell(CommandSource sender, CommandSource receiver, Text msg){		
		CommandSource src = sender;
		if (sender instanceof ConsoleSource){
			src = receiver;
		} 
		MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
				UChat.get().getVHelper().getCause(src), 
				src.getMessageChannel(), 
				Optional.of(src.getMessageChannel()), 				    							
				new MessageEvent.MessageFormatter(Text.builder("<" + src.getName() + "> ")
						.onShiftClick(TextActions.insertText(src.getName()))
						.onClick(TextActions.suggestCommand("/msg " + src.getName()))
						.build(), msg),
				msg,  
				false);
		Sponge.getEventManager().post(event);
	}

	private void registerChAliases() {
		//register ch cmds aliases
		for (String cmd:UChat.get().getConfig().getChCmd()){
			unregisterCmd(cmd);
			Sponge.getCommandManager().register(UChat.get().instance(), CommandSpec.builder()
					.arguments(new ChannelCommandElement(Text.of("channel")))
				    .description(Text.of("Join in a channel if you have permission."))
				    .executor((src, args) -> { {
				    	if (src instanceof Player){
				    		Player p = (Player) src;
				    		if (!args.<UCChannel>getOne("channel").isPresent()){
				    			StringBuilder channels = new StringBuilder();
				    			for (UCChannel ch:UChat.get().getConfig().getChannels()){
				    				if (!(p instanceof Player) || UChat.get().getPerms().channelWritePerm((Player)p, ch)){
				    					channels.append(", "+ch.getName());
				    				}
				    			}
				    			throw new CommandException(getHelpChannel(src).build());
				    		}
				    		UCChannel ch = args.<UCChannel>getOne("channel").get();							
							if (!UChat.get().getPerms().channelReadPerm(p, ch) && !UChat.get().getPerms().channelWritePerm(p, ch)){
								throw new CommandException(UCUtil.toText(UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName())));	
							}
							if (ch.isMember(p)){
								UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
								return CommandResult.success();	
							}
							
							ch.addMember(p);
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
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
			Sponge.getCommandManager().register(UChat.get().instance(), CommandSpec.builder()
					.arguments(GenericArguments.player(Text.of("player")), GenericArguments.remainingJoinedStrings(Text.of("message")))
					.permission("uchat.cmd.umsg")
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
			Sponge.getCommandManager().register(UChat.get().instance(), CommandSpec.builder()
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
	
	public void registerChannelAlias(String cha){
		unregisterCmd(cha);
		UCChannel ch = UChat.get().getConfig().getChannel(cha);
		if (ch == null){
			return;
		}
		Sponge.getCommandManager().register(UChat.get().instance(), CommandSpec.builder()
				.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("message"))))
				.permission("uchat.channel."+ch.getName())
			    .description(Text.of("Command to use channel "+ch.getName()+"."))
			    .executor((src, args) -> { {				    	
			    	if (src instanceof Player){
			    		if (args.<String>getOne("message").isPresent()){
			    			if (UChat.mutes.contains(src.getName()) || ch.isMuted(src.getName())){
			    				UChat.get().getLang().sendMessage(src, "channel.muted");
			    				return CommandResult.success();
			    			}
			    			
			    			UChat.tempChannels.put(src.getName(), ch.getAlias());
			    			
			    			Text msg = Text.of(args.<String>getOne("message").get());				    			
			    			MessageChannelEvent.Chat event = SpongeEventFactory.createMessageChannelEventChat(
			    					UChat.get().getVHelper().getCause(src), 
	    							src.getMessageChannel(), 
	    							Optional.of(src.getMessageChannel()), 				    							
	    							new MessageEvent.MessageFormatter(Text.builder("<" + src.getName() + "> ")
	    									.onShiftClick(TextActions.insertText(src.getName()))
	    									.onClick(TextActions.suggestCommand("/msg " + src.getName()))
	    									.build(), msg),
	    							msg,  
	    							false);
			    			Sponge.getEventManager().post(event);
			    		} else {
			    			if (!ch.canLock()){
			    				UChat.get().getLang().sendMessage(src, "help.channels.send");
								return CommandResult.success();
							}
				    		if (ch.isMember((Player) src)){
				    			UChat.tempChannels.put(src.getName(), ch.getAlias());
				    			UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
								return CommandResult.success();
							}
				    		ch.addMember((Player) src);
				    		UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));	
			    		}
			    	} else if (args.<String>getOne("message").isPresent()){
			    		UCMessages.sendFancyMessage(new String[0], Text.of(args.<String>getOne("message").get()), ch, src, null);  
			    	} else {
			    		StringBuilder channels = new StringBuilder();
			    		for (UCChannel chan:UChat.get().getConfig().getChannels()){
			    			if (!(src instanceof Player) || UChat.get().getPerms().channelWritePerm((Player)src, chan)){
			    				channels.append(", "+chan.getName());
			    			}
			    		}
			    		throw new CommandException(getHelpChannel(src).build(), true);
			    	}
			    	return CommandResult.success();	
			    }})
			    .build(), cha);
	}
	
	public void registerChannelAliases() {
		//register channel aliases
		for (String cha:UChat.get().getConfig().getChAliases()){
			registerChannelAlias(cha);
		}
	}
	
	private Map<String, String> getChKeys(){
		Map<String, String> result = new HashMap<String, String>();
		for (String key:new UCChannel("keys","k","&b").getProperties().keySet()){
			result.put(key, key);
		}
		return result;
	}
	
	private CommandCallable uchat() {
		CommandSpec delchannel = CommandSpec.builder()
				.arguments(new ChannelCommandElement(Text.of("channel")))
				.description(Text.of("Deletes a channel."))
				.permission("uchat.cmd.delchannel")
				.executor((src,args) -> {{
					//uchat delchannel <channel>
					Optional<UCChannel> optch = args.<UCChannel>getOne("channel");
					if (!optch.isPresent()){
						throw new CommandException(UChat.get().getLang().getText("channel.dontexist"), true);
					}
					UCChannel ch = optch.get();					
					UChat.get().getConfig().delChannel(ch);
					UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.delchannel.success").replace("{channel}", ch.getName()));
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec newchannel = CommandSpec.builder()
				.arguments(GenericArguments.string(Text.of("channel")),
						GenericArguments.string(Text.of("alias")),
						GenericArguments.string(Text.of("color")))
				.description(Text.of("Creates a new channel channel."))
				.permission("uchat.cmd.newchannel")
				.executor((src,args) -> {{
					//uchat newchannel <channel> <alias> <color>
					String ch = args.<String>getOne("channel").get();
					String alias = args.<String>getOne("alias").get();
					String color = args.<String>getOne("color").get();
					if (color.length() != 2 || !color.matches("(&([a-fk-or0-9]))$")){
						throw new CommandException(UChat.get().getLang().getText("channel.invalidcolor"), true);
					}
					UCChannel newch = new UCChannel(ch, alias, color);
					try {
						UChat.get().getConfig().addChannel(newch);
					} catch (Exception e) {
						e.printStackTrace();
					}
					UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("cmd.newchannel.success").replace("{channel}", newch.getName()));
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec chconfig = CommandSpec.builder()
				.arguments(new ChannelCommandElement(Text.of("channel")),
						GenericArguments.choices(Text.of("key"), getChKeys()),
						GenericArguments.string(Text.of("value")))
				.description(Text.of("Edit the config of a channel."))
				.permission("uchat.cmd.chconfig")
				.executor((src,args) -> {{
					//uchat chconfig <channel> <key> <value>
					Optional<UCChannel> optch = args.<UCChannel>getOne("channel");
					if (!optch.isPresent()){
						throw new CommandException(UChat.get().getLang().getText("channel.dontexist"), true);
					}
					UCChannel ch = optch.get();
					String key = args.<String>getOne("key").get();
					String value = args.<String>getOne("value").get();
					if (!ch.getProperties().containsKey(key)){
						 throw new CommandException(UChat.get().getLang().getText("cmd.chconfig.invalidkey"), true);
					}
					
					UChat.get().getConfig().delChannel(ch);
					
					ch.setProperty(key, value);
					
					try {
						UChat.get().getConfig().addChannel(ch);
					} catch (Exception e) {
						e.printStackTrace();
					}
										
					UChat.get().getLang().sendMessage(src, "cmd.chconfig.success");
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec reload = CommandSpec.builder()
				.description(Text.of("Command to reload uchat."))
				.permission("uchat.cmd.reload")
				.executor((src,args) -> {{
					//uchat reload
					try {
						UChat.get().reload();
						UChat.get().getLang().sendMessage(src, "plugin.reloaded");
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
			    		UChat.get().getLang().sendMessage(src, "cmd.clear.cleared");	
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
							UChat.get().getLang().sendMessage(src, "cmd.spy.enabled");
						} else {
							UChat.isSpy.remove(p.getName());
							UChat.get().getLang().sendMessage(src, "cmd.spy.disabled");
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
						Player pi = args.<Player>getOne("player").get();
						if (pi.equals(p)){
							throw new CommandException(UChat.get().getLang().getText("cmd.ignore.self"), true);
						}
						if (!UChat.get().getPerms().canIgnore(p, pi)){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
							return CommandResult.success();
						 }
		    			if (UCMessages.isIgnoringPlayers(p.getName(), pi.getName())){
							UCMessages.unIgnorePlayer(p.getName(), pi.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.unignoring").replace("{player}", pi.getName()));
						} else {
							UCMessages.ignorePlayer(p.getName(), pi.getName());
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.ignoring").replace("{player}", pi.getName()));
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
						Optional<UCChannel> optch = args.<UCChannel>getOne("channel");
						if (!optch.isPresent()){
							throw new CommandException(UChat.get().getLang().getText("channel.dontexist"), true);
						}
						UCChannel ch = optch.get();
						if (!UChat.get().getPerms().canIgnore(p, ch)){
							UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
							 return CommandResult.success();
						 }
		    			if (ch.isIgnoring(p.getName())){
							ch.unIgnoreThis(p.getName());
							UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.notignoring").replace("{channel}", ch.getName()));
						} else {
							ch.ignoreThis(p.getName());
							UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.ignoring").replace("{channel}", ch.getName()));
						}
					}					
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec mute = CommandSpec.builder()
				.arguments(GenericArguments.player(Text.of("player")),GenericArguments.optional(new ChannelCommandElement(Text.of("channel"))))
				.description(Text.of("Mute a player."))
				.permission("uchat.cmd.mute")
				.executor((src,args) -> {{
					//uchat mute player channel
					Player play = args.<Player>getOne("player").get();
					if (args.<UCChannel>getOne("channel").isPresent()){
						UCChannel ch = args.<UCChannel>getOne("channel").get();
						if (ch.isMuted(play.getName())){
							ch.unMuteThis(play.getName());
							UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.unmuted.this").replace("{player}", play.getName()).replace("{channel}", ch.getName()));
							if (play.isOnline()){
								 UChat.get().getLang().sendMessage(play, UChat.get().getLang().get("channel.player.unmuted.this").replace("{channel}", ch.getName()));
							}
						} else {
							ch.muteThis(play.getName());
							UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.muted.this").replace("{player}", play.getName()).replace("{channel}", ch.getName()));
							if (play.isOnline()){
								UChat.get().getLang().sendMessage(play, UChat.get().getLang().get("channel.player.muted.this").replace("{channel}", ch.getName()));
							}
						}
					} else {
						if (UChat.mutes.contains(play.getName())){
							 UChat.mutes.remove(play.getName());
							 UChat.get().getConfig().unMuteInAllChannels(play.getName());
							 UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.unmuted.all").replace("{player}", play.getName()));
							 if (play.isOnline()){
								 UChat.get().getLang().sendMessage(play, "channel.player.unmuted.all");
							 }
						 } else {
							 UChat.mutes.add(play.getName());
							 UChat.get().getConfig().muteInAllChannels(play.getName());
							 UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.muted.all").replace("{player}", play.getName()));
							 if (play.isOnline()){
								 UChat.get().getLang().sendMessage(play, "channel.player.muted.all");
							 }
						 }
					}										
			    	return CommandResult.success();
				}}).build();
		
		CommandSpec tempmute = CommandSpec.builder()
				.arguments(GenericArguments.integer(Text.of("time")), GenericArguments.player(Text.of("player")))
				.description(Text.of("Temporary mute a player."))
				.permission("uchat.cmd.tempmute")
				.executor((src,args) -> {{
					//uchat tempmute time player
					Player play = args.<Player>getOne("player").get();
					int time = args.<Integer>getOne("time").get();
					if (UChat.mutes.contains(play.getName())){
						UChat.get().getLang().sendMessage(src, "channel.already.muted");
					} else {
						UChat.mutes.add(play.getName());
						UChat.get().getConfig().muteInAllChannels(play.getName());
						UChat.get().getLang().sendMessage(src, UChat.get().getLang().get("channel.tempmuted.all").replace("{player}", play.getName()).replace("{time}", String.valueOf(time)));
						if (play.isOnline()){
							UChat.get().getLang().sendMessage(play, UChat.get().getLang().get("channel.player.tempmuted.all").replace("{time}", String.valueOf(time)));
						}
						Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(new Runnable(){
							@Override
							public void run() {
								if (UChat.mutes.contains(play.getName())){
									UChat.mutes.remove(play.getName());
									UChat.get().getConfig().unMuteInAllChannels(play.getName());
									if (play.isOnline()){
										UChat.get().getLang().sendMessage(play, "channel.player.unmuted.all");
								    }
								}
							}							
						}, time, TimeUnit.MINUTES);
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
			    	src.sendMessage(UCUtil.toText("&b---------------- "+UChat.get().instance().getName()+" "+UChat.get().instance().getVersion().get()+" ---------------"));
			    	src.sendMessage(UCUtil.toText("&bDeveloped by &6" + UChat.get().instance().getAuthors().get(0) + "."));
			    	src.sendMessage(UCUtil.toText("&bFor more information about the commands, type [" + "&6/uchat ?&b]."));
			    	src.sendMessage(UCUtil.toText("&b---------------------------------------------------"));			         
			    	return CommandResult.success();	
			    }})
			    .child(help, "?")
			    .child(chconfig, "chconfig")
			    .child(delchannel, "delchannel")
			    .child(newchannel, "newchannel")
			    .child(reload, "reload")
			    .child(clear, "clear")
			    .child(clearAll, "clear-all")
			    .child(spy, "spy")
			    .child(CommandSpec.builder()
			    		.child(ignorePlayer, "player")
			    		.child(ignoreChannel, "channel")
			    		.build(), "ignore")
			    .child(mute, "mute")
			    .child(tempmute, "tempmute")
			    .build();
		
		return uchat;
	}
    
	private void sendHelp(CommandSource p){	
		Builder fancy = Text.builder();
		
		fancy.append(UCUtil.toText("\n&7--------------- "+UChat.get().getLang().get("_UChat.prefix")+" Help &7---------------\n"));		
		fancy.append(UChat.get().getLang().getText("help.channels.enter","\n"));
		fancy.append(UChat.get().getLang().getText("help.channels.send","\n"));
		fancy.append(UChat.get().getLang().getText("help.channels.list","\n"));
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.tell")){
			fancy.append(UChat.get().getLang().getText("help.tell.lock","\n"));
			fancy.append(UChat.get().getLang().getText("help.tell.send","\n"));
			fancy.append(UChat.get().getLang().getText("help.tell.respond","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.broadcast")){
			fancy.append(UChat.get().getLang().getText("help.cmd.broadcast","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.umsg")){
			fancy.append(UChat.get().getLang().getText("help.cmd.umsg","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.clear")){
			fancy.append(UChat.get().getLang().getText("help.cmd.clear","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.clear-all")){
			fancy.append(UChat.get().getLang().getText("help.cmd.clear-all","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.spy")){
			fancy.append(UChat.get().getLang().getText("help.cmd.spy","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.mute")){
			fancy.append(UChat.get().getLang().getText("help.cmd.mute","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.tempmute")){
			fancy.append(UChat.get().getLang().getText("help.cmd.tempmute","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.ignore.player")){
			fancy.append(UChat.get().getLang().getText("help.cmd.ignore.player","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.ignore.channel")){
			fancy.append(UChat.get().getLang().getText("help.cmd.ignore.channel","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.chconfig")){
			fancy.append(UChat.get().getLang().getText("help.cmd.chconfig","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "uchat.cmd.reload")){
			fancy.append(UChat.get().getLang().getText("help.cmd.reload","\n"));
		}
		if (UChat.get().getPerms().cmdPerm(p, "newchannel")){
			fancy.append(UChat.get().getLang().getText("help.cmd.newchannel","\n"));	
		}
		if (UChat.get().getPerms().cmdPerm(p, "delchannel")){
			fancy.append(UChat.get().getLang().getText("help.cmd.delchannel","\n"));	
		}
		getHelpChannel(p).applyTo(fancy);		
		p.sendMessage(fancy.build());
	}
	
	private Builder getHelpChannel(CommandSource p){
		Builder fancy = Text.builder();
		fancy.append(UCUtil.toText("&7------------------------------------------\n"));
		fancy.append(UCUtil.toText(UChat.get().getLang().get("help.channels.available").replace("{channels}", "")));
		
		boolean first = true;
		for (UCChannel ch:UChat.get().getConfig().getChannels()){
			if (!(p instanceof Player) || UChat.get().getPerms().channelWritePerm((Player)p, ch)){
				Builder fancych = Text.builder();
				if (first){
					fancych.append(UCUtil.toText(ch.getColor()+ch.getName()+"&a"));
					first = false;
				} else {
					fancych.append(UCUtil.toText(", "+ch.getColor()+ch.getName()+"&a"));					
				}
				fancych.onHover(TextActions.showText(UCUtil.toText(ch.getColor()+"Alias: "+ch.getAlias())));
				fancych.onClick(TextActions.runCommand("/"+ch.getAlias()));
				fancych.applyTo(fancy);
			}
		}
		fancy.append(UCUtil.toText("\n&7------------------------------------------ "));
		return fancy;
	}
	
	private void sendTellHelp(CommandSource p) {
		p.sendMessage(UCUtil.toText("&7--------------- "+UChat.get().getLang().get("_UChat.prefix")+" Tell Help &7---------------"));
		p.sendMessage(UCUtil.toText(UChat.get().getLang().get("help.tell.unlock")));
		p.sendMessage(UCUtil.toText(UChat.get().getLang().get("help.tell.lock")));
		p.sendMessage(UCUtil.toText(UChat.get().getLang().get("help.tell.send")));
		p.sendMessage(UCUtil.toText(UChat.get().getLang().get("help.tell.respond")));
	}
}

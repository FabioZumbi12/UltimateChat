package br.net.fabiozumbi12.UltimateChat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import nl.riebie.mcclans.api.ClanPlayer;
import nl.riebie.mcclans.api.ClanService;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import br.net.fabiozumbi12.UltimateChat.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.config.UCLang;

class UCMessages {

	private static HashMap<String, String> registeredReplacers = new HashMap<String,String>();
	private static String[] defFormat = new String[0];	
	
	/**This will return the Object[] in this order:
	 * <p>
	 * [0] = MessageChannel, [1] = Builder Format, [2] = Builder Player Name, [3] = Builder Message
	 * 
	 * @param format
	 * @param msg
	 * @param channel
	 * @param sender
	 * @param tellReceiver
	 * @return Object[]
	 */
	protected static Object[] sendFancyMessage(String[] format, String msg, UCChannel channel, CommandSource sender, Player tellReceiver){
		//Execute listener:
		HashMap<String,String> tags = new HashMap<String,String>();
		for (String str:UChat.get().getConfig().getStringList("general","custom-tags")){
			tags.put(str, str);
		}
		SendChannelMessageEvent event = new SendChannelMessageEvent(tags, format, sender, channel, msg, true);
		Sponge.getEventManager().post(event);
		if (event.isCancelled()){
			return null;
		}
		
		Builder[] toConsole = new Builder[0];
		registeredReplacers = event.getResgisteredTags();
		defFormat = event.getDefFormat();
		String evmsg = event.getMessage();
		
		//send to event
		MutableMessageChannel msgCh = MessageChannel.TO_CONSOLE.asMutable();
				
		evmsg = UCChatProtection.filterChatMessage(sender, evmsg, event.getChannel());
		if (evmsg == null){
			return null;
		}
		
		evmsg = composeColor(sender,evmsg);
						
		if (event.getChannel() != null){					
			
			UCChannel ch = event.getChannel();
			
			if (sender instanceof Player && !ch.availableWorlds().isEmpty() && !ch.availableInWorld(((Player)sender).getWorld())){
				UCLang.sendMessage(sender, UCLang.get("channel.notavailable").replace("{channel}", ch.getName()));
				return null;
			}
			
			if (!UChat.get().getPerms().channelPerm(sender, ch)){
				UCLang.sendMessage(sender, UCLang.get("channel.nopermission").replace("{channel}", ch.getName()));
				return null;
			}
			
			if (!UChat.get().getPerms().hasPerm(sender, "bypass.cost") && UChat.get().getEco() != null && sender instanceof Player && ch.getCost() > 0){
				UniqueAccount acc = UChat.get().getEco().getOrCreateAccount(((Player)sender).getUniqueId()).get();
				if (acc.getBalance(UChat.get().getEco().getDefaultCurrency()).doubleValue() < ch.getCost()){
					sender.sendMessage(UCUtil.toText(UCLang.get("channel.cost").replace("{value}", ""+ch.getCost())));
					return null;
				} else {
					acc.withdraw(UChat.get().getEco().getDefaultCurrency(), BigDecimal.valueOf(ch.getCost()), Cause.of(NamedCause.owner(UChat.plugin)));
				}
			}
				
			int noWorldReceived = 0;
			int vanish = 0;
			List<Player> receivers = new ArrayList<Player>();
			
			if (ch.getDistance() > 0 && sender instanceof Player){			
				for (Entity ent:((Player)sender).getNearbyEntities(ch.getDistance())){
					if (ent instanceof Player && UChat.get().getPerms().channelPerm((Player)ent, ch)){	
						Player p = (Player) ent;				
						if (((Player)sender).equals(p)){
							continue;
						}
						if (!ch.availableWorlds().isEmpty() && !ch.availableInWorld(p.getWorld())){
							continue;
						}
						if (ch.isIgnoring(p.getName())){
							continue;
						}
						if (!((Player)sender).canSee(p)){
							vanish++;
						}
						if ((ch.neeFocus() && UChat.pChannels.get(((Player)ent).getName()).equals(ch.getAlias())) || !ch.neeFocus()){					
							toConsole = sendMessage(sender,(Player)ent, evmsg, ch, false);
							receivers.add((Player)ent);
							msgCh.transformMessage(sender, (Player)ent, buildMessage(toConsole), ChatTypes.SYSTEM);
						}
					}				
				}
				toConsole = sendMessage(sender, sender, evmsg, ch, false);
			} else {
				for (Player receiver:Sponge.getServer().getOnlinePlayers()){	
					if (receiver.equals(sender) || !UChat.get().getPerms().channelPerm(receiver, ch) || (!ch.crossWorlds() && (sender instanceof Player && !receiver.getWorld().equals(((Player)sender).getWorld())))){				
						continue;
					}
					if (!ch.availableWorlds().isEmpty() && !ch.availableInWorld(receiver.getWorld())){
						continue;
					}
					if (ch.isIgnoring(receiver.getName())){
						continue;
					}
					if (sender instanceof Player && !((Player)sender).canSee(receiver)){
						vanish++;
					} else {
						noWorldReceived++;
					}					
					if ((ch.neeFocus() && UChat.pChannels.get(receiver.getName()).equals(ch.getAlias())) || !ch.neeFocus()){
						toConsole = sendMessage(sender, receiver, evmsg, ch, false);
						receivers.add(receiver);
						msgCh.transformMessage(sender, receiver, buildMessage(toConsole), ChatTypes.SYSTEM);
					}				
				}
				toConsole = sendMessage(sender, sender, evmsg, ch, false);
			}	
									
			//chat spy
			for (Player receiver:Sponge.getServer().getOnlinePlayers()){			
				if (!receiver.equals(sender) && !receivers.contains(receiver) && !receivers.contains(sender) && UChat.isSpy.contains(receiver.getName())){	
					String spyformat = UChat.get().getConfig().getString("general","spy-format");
					spyformat = spyformat.replace("{output}", UCUtil.stripColor(buildMessage(sendMessage(sender, receiver, evmsg, ch, true)).toPlain()));					
					receiver.sendMessage(UCUtil.toText(spyformat));
				}
			}
			
			
			if (!(sender instanceof ConsoleSource)){
				msgCh.transformMessage(sender, Sponge.getServer().getConsole(), buildMessage(toConsole), ChatTypes.SYSTEM);
			}			
			
			
			if (ch.getDistance() == 0 && noWorldReceived <= 0){
				if (ch.getReceiversMsg()){
					UCLang.sendMessage(sender, "channel.noplayer.world");
				}
			}		
			if ((receivers.size()-vanish) <= 0){
				if (ch.getReceiversMsg()){
					UCLang.sendMessage(sender, "channel.noplayer.near");	
				}				
				return new Object[]{msgCh,toConsole[0].build(),toConsole[1].build(),toConsole[2].build()};
			}
			
		} else {						
			//send tell
			UCChannel fakech = new UCChannel("tell");
			
			//send spy			
			for (Player receiver:Sponge.getServer().getOnlinePlayers()){			
				if (!receiver.equals(tellReceiver) && !receiver.equals(sender) && UChat.isSpy.contains(receiver.getName())){
					String spyformat = UChat.get().getConfig().getString("general","spy-format");
					if (isIgnoringPlayers(tellReceiver.getName(), sender.getName())){
						spyformat = UCLang.get("chat.ignored")+spyformat;
					}
					spyformat = spyformat.replace("{output}", UCUtil.stripColor(buildMessage(sendMessage(sender, tellReceiver, evmsg, fakech, true)).toPlain()));					
					receiver.sendMessage(UCUtil.toText(spyformat));					
				}
			}
			Text to = buildMessage(sendMessage(sender, tellReceiver, evmsg, fakech, false));
			if (isIgnoringPlayers(tellReceiver.getName(), sender.getName())){
				to = Text.of(UCUtil.toText(UCLang.get("chat.ignored")),to);
			}
			Sponge.getServer().getConsole().sendMessage(to);
			return null;
		}
		
		return new Object[]{msgCh,toConsole[0].build(),toConsole[1].build(),toConsole[2].build()};
	}
	
	private static Text buildMessage(Builder[] build){
		Builder build0 = build[0];
		Builder build1 = build[1];
		Builder build2 = build[2];
		build1.applyTo(build0);
		build2.applyTo(build0);
		return build0.build();
	}
	private static String composeColor(CommandSource sender, String evmsg){
		if (sender instanceof Player){			
			if (!UChat.get().getPerms().hasPerm((Player)sender, "chat.color")){
				evmsg = evmsg.replaceAll("(?i)&([a-f0-9r])", "");
			}
			if (!UChat.get().getPerms().hasPerm((Player)sender, "chat.color.formats")){
				evmsg = evmsg.replaceAll("(?i)&([l-o])", "");
			}
			if (!UChat.get().getPerms().hasPerm((Player)sender, "chat.color.magic")){
				evmsg = evmsg.replaceAll("(?i)&([k])", "");
			}
		}	
		return evmsg;
	}
	
	static boolean isIgnoringPlayers(String p, String victim){
		List<String> list = new ArrayList<String>();
		if (UChat.ignoringPlayer.containsKey(p)){
			list.addAll(UChat.ignoringPlayer.get(p));			
		}
		return list.contains(victim);
	}
	
	static void ignorePlayer(String p, String victim){
		List<String> list = new ArrayList<String>();
		if (UChat.ignoringPlayer.containsKey(p)){
			list.addAll(UChat.ignoringPlayer.get(p));
		}
		list.add(victim);
		UChat.ignoringPlayer.put(p, list);
	}
	
	static void unIgnorePlayer(String p, String victim){
		List<String> list = new ArrayList<String>();
		if (UChat.ignoringPlayer.containsKey(p)){
			list.addAll(UChat.ignoringPlayer.get(p));
		}
		list.remove(victim);
		UChat.ignoringPlayer.put(p, list);
	}
	
	private static Builder[] sendMessage(CommandSource sender, CommandSource receiver, String msg, UCChannel ch, boolean isSpy){
		Builder formatter = Text.builder();
		Builder playername = Text.builder();
		Builder message = Text.builder();
				
		if (!ch.getName().equals("tell")){
			String[] defaultBuilder = UChat.get().getConfig().getDefBuilder();
			if (ch.useOwnBuilder()){
				defaultBuilder = ch.getBuilder();
			}
			
			String lastColor = "";
			for (String tag:defaultBuilder){
				Builder tagBuilder = Text.builder();
				
				Builder msgBuilder = Text.builder();
				
				if (UChat.get().getConfig().getString("tags",tag,"format") == null){
					tagBuilder.append(Text.of(tag));
					continue;
				}
				
				String perm = UChat.get().getConfig().getString("tags",tag,"permission");
				String format = lastColor+UChat.get().getConfig().getString("tags",tag,"format");
				String execute = UChat.get().getConfig().getString("tags",tag,"click-cmd");
				List<String> messages = UChat.get().getConfig().getStringList("tags",tag,"hover-messages");
				List<String> showWorlds = UChat.get().getConfig().getStringList("tags",tag,"show-in-worlds");
				List<String> hideWorlds = UChat.get().getConfig().getStringList("tags",tag,"hide-in-worlds");
				
				//check perm
				if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)){
					continue;
				}
				
				//check show or hide in world
				if (sender instanceof Player){
					if (!showWorlds.isEmpty() && !showWorlds.contains(((Player)sender).getWorld().getName())){
						continue;
					}
					if (!hideWorlds.isEmpty() && hideWorlds.contains(((Player)sender).getWorld().getName())){
						continue;
					}
				}
							
				String tooltip = "";
				for (String tp:messages){
					tooltip = tooltip+"\n"+tp;
				}
				if (tooltip.length() > 2){
					tooltip = tooltip.substring(1);
				}			
							
				if (execute != null && execute.length() > 0){
					tagBuilder.onClick(TextActions.runCommand(formatTags(tag, "/"+execute, sender, receiver, msg, ch)));
				}
							
				msgBuilder = tagBuilder;
				
				if (tag.equals("message") && !msg.equals(mention(sender, receiver, msg))){
					tooltip = formatTags("", tooltip, sender, receiver, msg, ch);	
					format = formatTags(tag, format, sender, receiver, msg, ch);
					
					lastColor = getLastColor(format);
					
					//msg = mention(sender, receiver, msg);									
					if (UChat.get().getConfig().getString("mention","hover-message").length() > 0 && StringUtils.containsIgnoreCase(msg, receiver.getName())){
						tooltip = formatTags("", UChat.get().getConfig().getString("mention","hover-message"), sender, receiver, msg, ch);						
						msgBuilder.append(UCUtil.toText(format))
				   		   .onHover(TextActions.showText(UCUtil.toText(tooltip)));
					} else if (tooltip.length() > 0){				
						msgBuilder.append(UCUtil.toText(format))
							.onHover(TextActions.showText(UCUtil.toText(tooltip)));
					} else {
						msgBuilder.append(UCUtil.toText(format));
					}		
					msgBuilder.applyTo(message);
				} else {					
					format = formatTags(tag, format, sender, receiver, msg, ch);
					tooltip = formatTags("", tooltip, sender, receiver, msg, ch);
					
					lastColor = getLastColor(format);
					
					if (tooltip.length() > 0){				
						tagBuilder.append(UCUtil.toText(format))
						.onHover(TextActions.showText(UCUtil.toText(tooltip)));
					} else {						
						tagBuilder.append(UCUtil.toText(format));
					}
					
					if (tag.equals("{playername}") || tag.equals("{nickname}")){
						tagBuilder.applyTo(playername);
					} else {
						tagBuilder.applyTo(formatter);
					}					
				}
			}
		} else {
			//if tell
			String prefix = UChat.get().getConfig().getString("tell","prefix");
			String format = UChat.get().getConfig().getString("tell","format");
			List<String> messages = UChat.get().getConfig().getStringList("tell","hover-messages");
						
			String tooltip = "";
			if (!messages.isEmpty() && messages.get(0).length() > 1){
				for (String tp:messages){
					tooltip = tooltip+"\n"+tp;
				}
				if (tooltip.length() > 2){
					tooltip = tooltip.substring(1);
				}
			}			
			
			prefix = formatTags("", prefix, sender, receiver, msg, ch);						
			format = formatTags("tell", format, sender, receiver, msg, ch);
			tooltip = formatTags("", tooltip, sender, receiver, msg, ch);
			
			if (tooltip.length() > 0){				
				formatter.append(UCUtil.toText(prefix))
				.onHover(TextActions.showText(UCUtil.toText(tooltip)));
			} else {
				formatter.append(UCUtil.toText(prefix));
			}			
			message.append(UCUtil.toText(format));
			
			if (!isSpy){
				sender.sendMessage(Text.of(formatter, message));
			}			
		}
		
		if (!isSpy && !isIgnoringPlayers(receiver.getName(), sender.getName())){
			receiver.sendMessage(Text.of(formatter,playername,message));			
		}		
		return new Builder[]{formatter,playername,message};
	}
	
	private static String getLastColor(String str){
		if (str.length() > 2){
			str = str.substring(str.length()-2);
			if (str.matches("(&([a-fk-or0-9]))")){
				return str;
			}
		}
		return "";
	}
	
	private static String mention(Object sender, CommandSource receiver, String msg) {
		if (UChat.get().getConfig().getBool("mention","enable")){
		    for (Player p:Sponge.getServer().getOnlinePlayers()){			
				if (!sender.equals(p) && StringUtils.containsIgnoreCase(msg, p.getName())){
					if (receiver instanceof Player && receiver.equals(p)){
						
						String mentionc = UChat.get().getConfig().getColor("mention","color-template").replace("{mentioned-player}", p.getName());
						mentionc = formatTags("", mentionc, sender, receiver, "", new UCChannel("mention"));
						
						if (msg.contains(mentionc) || sender instanceof CommandSource && !UChat.get().getPerms().hasPerm((CommandSource)sender, "chat.mention")){
							msg = msg.replaceAll("(?i)\\b"+p.getName()+"\\b", p.getName());
							continue;
						}
						
						Optional<SoundType> sound = Sponge.getRegistry().getType(SoundType.class, UChat.get().getConfig().getString("mention","playsound"));
						if (sound.isPresent() && !msg.contains(mentionc)){
							((Player)receiver).playSound(sound.get(),((Player)receiver).getLocation().getPosition(), 1, 1);
						}
						
						msg = msg.replace(mentionc, p.getName());	
						msg = msg.replaceAll("(?i)\\b"+p.getName()+"\\b", mentionc);
					} else {
						msg = msg.replaceAll("(?i)\\b"+p.getName()+"\\b", p.getName());
					}					
				}
			}
		}				
		return msg;
	}
	
	static String formatTags(String tag, String text, Object cmdSender, Object receiver, String msg, UCChannel ch){	
		if (receiver instanceof CommandSource && tag.equals("message")){			
			text = text.replace("{message}", mention(cmdSender, (CommandSource)receiver, msg));
		} else {
			text = text.replace("{message}", msg);
		}
		
		text = text.replace("{ch-color}", ch.getColor())
		.replace("{ch-name}", ch.getName())
		.replace("{ch-alias}", ch.getAlias());		
		if (cmdSender instanceof CommandSource){
			text = text.replace("{playername}", ((CommandSource)cmdSender).getName())
					.replace("{receivername}", ((CommandSource)receiver).getName());
		} else {
			text = text.replace("{playername}", (String)cmdSender)
					.replace("{receivername}", (String)receiver);
		}
		for (String repl:registeredReplacers.keySet()){
			if (registeredReplacers.get(repl).equals(repl)){
				text = text.replace(repl, "");
				continue;
			}
			text = text.replace(repl, registeredReplacers.get(repl));			
		}	
		
		if (defFormat.length == 3){
			text = text.replace("{chat_header}", defFormat[0])
					.replace("{chat_body}", defFormat[1])
					.replace("{chat_footer}", defFormat[2])
					.replace("{chat_all}", defFormat[0]+defFormat[1]+defFormat[2]);
		}		
				
		if (cmdSender instanceof Player){
			Player sender = (Player)cmdSender;
			
			if (sender.get(Keys.DISPLAY_NAME).isPresent()){
				text = text.replace("{nickname}", sender.get(Keys.DISPLAY_NAME).get().toPlain());
			}				
			
			text = text.replace("{world}", sender.getWorld().getName());
			
			if (UChat.get().getEco() != null){
				UniqueAccount acc = UChat.get().getEco().getOrCreateAccount(sender.getUniqueId()).get();
				text = text
						.replace("{balance}", ""+acc.getBalance(UChat.get().getEco().getDefaultCurrency()).intValue());
			}
			
			Subject sub = UChat.get().getPerms().getGroupAndTag(sender);			
			if (sub != null){
				text = text.replace("{option_group}", sub.getIdentifier());
				if (sub.getOption("prefix").isPresent()){
					text = text.replace("{option_prefix}", sub.getOption("prefix").get());
				}
				
				if (sub.getOption("suffix").isPresent()){
					text = text.replace("{option_suffix}", sub.getOption("suffix").get());
				}
				
				if (sub.getOption("display_name").isPresent()){
					text = text.replace("{option_display_name}", sub.getOption("display_name").get());
				} else {
					text = text.replace("{option_display_name}", sub.getIdentifier());
				}
			}			
						
			if (UChat.get().getConfig().getBool("hooks","MCClans","enable")){
				Optional<ClanService> clanServiceOpt = Sponge.getServiceManager().provide(ClanService.class);
                if (clanServiceOpt.isPresent()) {
					ClanService clan = clanServiceOpt.get();
					ClanPlayer cp = clan.getClanPlayer(sender.getUniqueId());
					if (cp.isMemberOfAClan()){
						text = text
								.replace("{clan_name}", cp.getClan().getName())
								.replace("{clan_tag}", cp.getClan().getTag())
								.replace("{clan_tag_color}", TextSerializers.FORMATTING_CODE.serialize(cp.getClan().getTagColored()))
								.replace("{clan_kdr}", ""+cp.getClan().getKDR())
								.replace("{clan_player_rank}", ""+cp.getRank().getName())
								.replace("{clan_player_kdr}", ""+cp.getKillDeath().getKDR())
								.replace("{clan_player_ffprotected}", String.valueOf(cp.isFfProtected()))
								.replace("{clan_player_isowner}", String.valueOf(cp.getClan().getOwner().equals(cp)));
					}				
				}
			}			
		}		
		
		text = text.replace("{option_suffix}", "&r: ");
		
		if (cmdSender instanceof CommandSource){
			text = text.replace("{nickname}", UChat.get().getConfig().getString("general","console-tag").replace("{console}", ((CommandSource)cmdSender).getName()));
		} else {
			text = text.replace("{nickname}", UChat.get().getConfig().getString("general","console-tag").replace("{console}", (String)cmdSender));
		}
			
		/*
		//colorize tags (not message)
		if (!tag.equals("message")){
			text = UCUtil.toColor(text);
		}
		*/
		
		//remove blank items		
		text = text.replaceAll("\\{.*\\}", "");		
		if (!tag.equals("message")){
			for (String rpl:UChat.get().getConfig().getString("general","remove-from-chat").split(",")){
				text = text.replace(rpl, "");
			}
		}		
		if (text.equals(" ") || text.equals("  ")){
			return text = "";
		}
		return text;
	}
	
}

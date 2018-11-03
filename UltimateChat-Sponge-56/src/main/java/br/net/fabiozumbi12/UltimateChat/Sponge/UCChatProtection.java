package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class UCChatProtection {
	
	private static final HashMap<Player,String> chatSpam = new HashMap<>();
	private static final HashMap<String,Integer> msgSpam = new HashMap<>();
	private static final HashMap<Player,Integer> UrlSpam = new HashMap<>();
	
	public static String filterChatMessage(CommandSource source, String msg, UCChannel chan){
		if (!(source instanceof Player)){
			return msg;
		}
		
		final Player p = (Player) source;
		
		if (msg.length() <= 0){
			return msg;
		}
		
		//mute check
		if (UChat.get().mutes.contains(p.getName())){
			p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.punish.mute_msg));
			return null;
		}
		
		//antispam
		if (UChat.get().getConfig().protections().antispam.enable && !p.hasPermission("uchat.bypass-spam")
				&& (chan == null || !UChat.get().getConfig().protections().antispam.disable_on_channels.contains(chan.getName()))){
			
			//check spam messages
			if (!chatSpam.containsKey(p)){
				chatSpam.put(p, msg);				
				Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(() -> {
                    chatSpam.remove(p);
                },UChat.get().getConfig().protections().antispam.time_between_messages,TimeUnit.SECONDS);
			} else if (!chatSpam.get(p).equalsIgnoreCase(msg)){				
				p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().antispam.cooldown_msg));
				return null;
			}
			
			//check same message frequency
			if (!msgSpam.containsKey(msg)){
				msgSpam.put(msg, 1);
				final String nmsg = msg;
				Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(() -> {
                    msgSpam.remove(nmsg);
                },UChat.get().getConfig().protections().antispam.time_between_same_messages, TimeUnit.SECONDS);
			} else {
				msgSpam.put(msg, msgSpam.get(msg)+1);				
				if (msgSpam.get(msg) >= UChat.get().getConfig().protections().antispam.count_of_same_message){
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(),UChat.get().getConfig().protections().antispam.cmd_action.replace("{player}", p.getName()));
					msgSpam.remove(msg);
				} else {
					p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().antispam.wait_message));
					
				}
				return null;
			}			
		}
				
		//censor
		if (UChat.get().getConfig().protections().censor.enable && !p.hasPermission("uchat.bypass-censor")
				&& (chan == null || !UChat.get().getConfig().protections().censor.disable_on_channels.contains(chan.getName()))){
			int act = 0;
			for (Entry<String, String> word:UChat.get().getConfig().protections().censor.replace_words.entrySet()){
				if (!Pattern.compile("(?i)"+word.getKey()).matcher(msg).find()){
					continue;
				} 				
				
				String replaceby = word.getValue();
				if (UChat.get().getConfig().protections().censor.replace_by_symbol){
					replaceby = word.getKey().replaceAll("(?i).", UChat.get().getConfig().protections().censor.by_symbol);
				}

				if (UChat.get().getConfig().protections().censor.use_pre_actions){
					if (!UChat.get().getConfig().protections().censor.replace_partial_word){
						msg = msg.replaceAll("(?i)"+"\\b"+Pattern.quote(word.getKey())+"\\b", replaceby);
						if (UChat.get().getConfig().protections().censor.action.on_partial_words){
							act++;
						}
					} else {
						msg = msg.replaceAll("(?i)"+ word.getKey(), replaceby);
						act++;
					}
				} else {
					msg = msg.replaceAll(word.getKey(), replaceby);
					act++;
				}
			}
			if (act > 0){
				String action = UChat.get().getConfig().protections().censor.action.cmd;
				if (action.length() > 1){
					List<String> chs = UChat.get().getConfig().protections().censor.action.only_on_channels;
					if (chs.size() > 0 && chs.get(0).length() > 1 && chan != null){
						for (String ch:chs){
							if (ch.length() > 1 && (ch.equalsIgnoreCase(chan.getName()) || ch.equalsIgnoreCase(chan.getAlias()))){
								Sponge.getCommandManager().process(Sponge.getServer().getConsole(), action.replace("{player}", p.getName()));
								break;
							}
						}
					} else {
						Sponge.getCommandManager().process(Sponge.getServer().getConsole(), action.replace("{player}", p.getName()));
					}					
				}
			}
		}
		
		String regexIP = UChat.get().getConfig().protections().anti_ip.custom_ip_regex;
		String regexUrl = UChat.get().getConfig().protections().anti_ip.custom_url_regex;
		
		//check ip and website
		if (UChat.get().getConfig().protections().anti_ip.enable && !p.hasPermission("uchat.bypass-anti-ip")
				&& (chan == null || !UChat.get().getConfig().protections().anti_ip.disable_on_channels.contains(chan.getName()))){
			
			//check whitelist
			int cont = 0;
			for (String check:UChat.get().getConfig().protections().anti_ip.whitelist_words){
				if (Pattern.compile(check).matcher(msg).find()){	
					cont++;
				}
			}
			
			//continue
			if (UChat.get().getConfig().protections().anti_ip.whitelist_words.isEmpty() || cont == 0){
				if (Pattern.compile(regexIP).matcher(msg).find()){	
					addURLspam(p);
					if (UChat.get().getConfig().protections().anti_ip.cancel_or_replace.equalsIgnoreCase("cancel")){
						p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.cancel_msg));
						return null;
					} else {
						msg = msg.replaceAll(regexIP, UChat.get().getConfig().protections().anti_ip.replace_by_word);
					}
				}
				if (Pattern.compile(regexUrl).matcher(msg).find()){
					addURLspam(p);
					if (UChat.get().getConfig().protections().anti_ip.cancel_or_replace.equalsIgnoreCase("cancel")){
						p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.cancel_msg));
						return null;
					} else {
						msg = msg.replaceAll(regexUrl, UChat.get().getConfig().protections().anti_ip.replace_by_word);
					}
				}
				
				for (String word:UChat.get().getConfig().protections().anti_ip.check_for_words){
					if (Pattern.compile("(?i)"+"\\b"+word+"\\b").matcher(msg).find()){
						addURLspam(p);
						if (UChat.get().getConfig().protections().anti_ip.cancel_or_replace.equalsIgnoreCase("cancel")){
							p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.cancel_msg));
							return null;
						} else {
							msg = msg.replaceAll("(?i)"+word, UChat.get().getConfig().protections().anti_ip.replace_by_word);
						}
					}
				}
			}					
		}	
		
		//capitalization verify
		if (UChat.get().getConfig().protections().chat_enhancement.enable && !p.hasPermission("uchat.bypass-enhancement")
				&& (chan == null || !UChat.get().getConfig().protections().chat_enhancement.disable_on_channels.contains(chan.getName()))){
			int lenght = UChat.get().getConfig().protections().chat_enhancement.minimum_length;
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msg.length() > lenght){
				msg = msg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ").substring(0, 1).toUpperCase()+msg.substring(1);
				if (UChat.get().getConfig().protections().chat_enhancement.end_with_dot && !msg.endsWith("?") && !msg.endsWith("!") && !msg.endsWith(".") && msg.split(" ").length > 2){
					msg = msg+".";
				}
			}				
		}
		
		//anti-caps
		if (UChat.get().getConfig().protections().caps_filter.enable && !p.hasPermission("uchat.bypass-enhancement")
				&& (chan == null || !UChat.get().getConfig().protections().caps_filter.disable_on_channels.contains(chan.getName()))){
			int lenght = UChat.get().getConfig().protections().caps_filter.minimum_length;
			int msgUppers = msg.replaceAll("\\p{P}", "").replaceAll("[a-z ]+", "").length();
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msgUppers >= lenght){
				msg = msg.substring(0, 1).toUpperCase()+msg.substring(1).toLowerCase();
			}
		}
		
		//antiflood
		if (UChat.get().getConfig().protections().anti_flood.enable
				&& (chan == null || !UChat.get().getConfig().protections().anti_flood.disable_on_channels.contains(chan.getName()))){
			for (String flood:UChat.get().getConfig().protections().anti_flood.whitelist_flood_characs){
				if (Pattern.compile("(["+flood+"])\\1+").matcher(msg).find()){	
					return msg;
				}
			}
			msg = msg.replaceAll("([A-Za-z])\\1+", "$1$1");
		}
		return msg;
	}	
	
	private static void addURLspam(final Player p){
		if (UChat.get().getConfig().protections().anti_ip.punish.enable){
			if (!UrlSpam.containsKey(p)){
				UrlSpam.put(p, 1);
			} else {
				UrlSpam.put(p, UrlSpam.get(p)+1);
				if (UrlSpam.get(p) >= UChat.get().getConfig().protections().anti_ip.punish.max_attempts){
					if (UChat.get().getConfig().protections().anti_ip.punish.mute_or_cmd.equalsIgnoreCase("mute")){
						
						int time = UChat.get().getConfig().protections().anti_ip.punish.mute_duration;
						UChat.get().mutes.add(p.getName());
						UChat.get().muteInAllChannels(p.getName());
						p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.punish.mute_msg));
						
						//mute counter
						Task.builder().execute(new MuteCountDown(p.getName(), time)).interval(1, TimeUnit.SECONDS).name("Chat Protection Mute Counter").submit(UChat.get().instance());								
					} else {
						Sponge.getCommandManager().process(Sponge.getServer().getConsole(), UChat.get().getConfig().protections().anti_ip.punish.cmd_punish.replace("{player}", p.getName()));
					}	
					UrlSpam.remove(p);
				}
			}
		}		
	}
}

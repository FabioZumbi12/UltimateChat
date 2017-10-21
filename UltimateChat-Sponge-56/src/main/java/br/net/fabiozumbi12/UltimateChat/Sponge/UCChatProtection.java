package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

class UCChatProtection {
	
	private static HashMap<Player,String> chatSpam = new HashMap<Player,String>();
	private static HashMap<String,Integer> msgSpam = new HashMap<String,Integer>();
	private static HashMap<Player,Integer> UrlSpam = new HashMap<Player,Integer>();
	private static List<String> muted = new ArrayList<String>();
	
	public static String filterChatMessage(CommandSource source, String msg, UCChannel chan){
		if (!(source instanceof Player)){
			return msg;
		}
		
		final Player p = (Player) source;
		
		if (msg.length() <= 1){
			return msg;
		}
		
		//mute check
		if (muted.contains(p.getName())){
			p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.punish.mute_msg));
			return null;
		}
		
		//antispam
		if (UChat.get().getConfig().protections().antispam.enable && !p.hasPermission("uchat.bypass-spam")
				&& !UChat.get().getConfig().protections().antispam.disable_on_chanels.contains(chan.getName())){	
			
			//check spam messages
			if (!chatSpam.containsKey(p)){
				chatSpam.put(p, msg);				
				Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(new Runnable() { 
					public void run() {
						if (chatSpam.containsKey(p)){
							chatSpam.remove(p);
						}						
					}						
				},UChat.get().getConfig().protections().antispam.time_beteween_messages,TimeUnit.SECONDS);
			} else if (!chatSpam.get(p).equalsIgnoreCase(msg)){				
				p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().antispam.cooldown_msg));
				return null;
			}
			
			//check same message frequency
			if (!msgSpam.containsKey(msg)){
				msgSpam.put(msg, 1);
				final String nmsg = msg;
				Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(new Runnable() { 
					public void run() {
						if (msgSpam.containsKey(nmsg)){
							msgSpam.remove(nmsg);
						}						
					}						
					},UChat.get().getConfig().protections().antispam.time_beteween_same_messages, TimeUnit.SECONDS);
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
				&& !UChat.get().getConfig().protections().censor.disable_on_chanels.contains(chan.getName())){
			int act = 0;
			for (Entry<String, String> word:UChat.get().getConfig().protections().censor.replace_words.entrySet()){
				if (!StringUtils.containsIgnoreCase(msg, word.getKey())){
					continue;
				} 				
				
				String replaceby = word.getValue();
				if (UChat.get().getConfig().protections().censor.replace_by_symbol){
					replaceby = word.getKey().toString().replaceAll("(?s).", UChat.get().getConfig().protections().censor.by_symbol);
				}
				
				if (!UChat.get().getConfig().protections().censor.replace_partial_word){
					msg = msg.replaceAll("(?i)"+"\\b"+Pattern.quote(word.getKey().toString())+"\\b", replaceby);
					if (UChat.get().getConfig().protections().censor.action.on_partial_words){
						act++;
					}
				} else {
					msg = msg.replaceAll("(?i)"+word.getKey().toString(), replaceby);
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
				&& !UChat.get().getConfig().protections().anti_ip.disable_on_chanels.contains(chan.getName())){
			
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
				&& !UChat.get().getConfig().protections().chat_enhancement.disable_on_chanels.contains(chan.getName())){
			int lenght = UChat.get().getConfig().protections().chat_enhancement.minimum_lenght;
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msg.length() > lenght){
				msg = msg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ").substring(0, 1).toUpperCase()+msg.substring(1);
				if (UChat.get().getConfig().protections().chat_enhancement.end_with_dot && !msg.endsWith("?") && !msg.endsWith("!") && !msg.endsWith(".") && msg.split(" ").length > 2){
					msg = msg+".";
				}
			}				
		}
		
		//anti-caps
		if (UChat.get().getConfig().protections().caps_filter.enable && !p.hasPermission("uchat.bypass-enhancement")
				&& !UChat.get().getConfig().protections().caps_filter.disable_on_chanels.contains(chan.getName())){
			int lenght = UChat.get().getConfig().protections().caps_filter.minimum_lenght;
			int msgUppers = msg.replaceAll("\\p{P}", "").replaceAll("[a-z ]+", "").length();
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find() && msgUppers >= lenght){
				msg = msg.substring(0, 1).toUpperCase()+msg.substring(1).toLowerCase();
			}
		}
		
		//antiflood
		if (UChat.get().getConfig().protections().anti_flood.enable
				&& !UChat.get().getConfig().protections().anti_flood.disable_on_chanels.contains(chan.getName())){						
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
						muted.add(p.getName());
						p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.punish.mute_msg));
						Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(new Runnable() { 
							public void run() {
								if (muted.contains(p.getName())){						
									muted.remove(p.getName());
									p.sendMessage(UCUtil.toText(UChat.get().getConfig().protections().anti_ip.punish.unmute_msg));
								}
							}						
						},UChat.get().getConfig().protections().anti_ip.punish.mute_duration,TimeUnit.MINUTES);
					} else {
						Sponge.getCommandManager().process(Sponge.getServer().getConsole(),UChat.get().getConfig().protections().anti_ip.punish.cmd_punish.replace("{player}", p.getName()));
					}	
					UrlSpam.remove(p);
				}
			}
		}		
	}
}

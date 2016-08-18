package br.net.fabiozumbi12.UltimateChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import br.net.fabiozumbi12.UltimateChat.API.SendChannelMessageEvent;

class UCChatProtection implements Listener{
	
	private HashMap<Player,String> chatSpam = new HashMap<Player,String>();
	private HashMap<String,Integer> msgSpam = new HashMap<String,Integer>();
	private HashMap<Player,Integer> UrlSpam = new HashMap<Player,Integer>();
	private List<String> muted = new ArrayList<String>();

	@EventHandler
	public void onPlayerChat(SendChannelMessageEvent e){
		if (!(e.getSender() instanceof Player)){
			return;
		}
		final Player p = (Player) e.getSender();
		String msg = e.getMessage();
		
		//mute check
		if (muted.contains(p.getName())){
			UChat.lang.sendMessage(p, UChat.config.getProtMsg("chat-protection.anti-ip.mute-msg"));
			e.setCancelled(true);
			return;
		}
		
		//antispam
		if (UChat.config.getProtBool("chat-protection.antispam.enabled") && !p.hasPermission("uchat.bypass-spam")){	
			
			//check spam messages
			if (!chatSpam.containsKey(p)){
				chatSpam.put(p, msg);				
				Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.plugin, new Runnable() { 
					public void run() {
						if (chatSpam.containsKey(p)){
							chatSpam.remove(p);
						}						
					}						
				},UChat.config.getProtInt("chat-protection.antispam.time-beteween-messages")*20);
			} else if (!chatSpam.get(p).equalsIgnoreCase(msg)){				
				UChat.lang.sendMessage(p, UChat.config.getProtMsg("chat-protection.antispam.colldown-msg"));
				e.setCancelled(true);
				return;
			}
			
			//check same message frequency
			if (!msgSpam.containsKey(msg)){
				msgSpam.put(msg, 1);
				final String nmsg = msg;
				Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.plugin, new Runnable() { 
					public void run() {
						if (msgSpam.containsKey(nmsg)){
							msgSpam.remove(nmsg);
						}						
					}						
					},UChat.config.getProtInt("chat-protection.antispam.time-beteween-same-messages")*20);
			} else {
				msgSpam.put(msg, msgSpam.get(msg)+1);
				e.setCancelled(true);				
				if (msgSpam.get(msg) >= UChat.config.getProtInt("chat-protection.antispam.count-of-same-message")){
					UCUtil.performCommand(UChat.serv.getConsoleSender(),UChat.config.getProtString("chat-protection.antispam.cmd-action").replace("{player}", p.getName()));
					msgSpam.remove(msg);
				} else {
					UChat.lang.sendMessage(p, UChat.config.getProtMsg("chat-protection.antispam.wait-message"));
				}
				return;
			}			
		}
				
		//censor
		if (UChat.config.getProtBool("chat-protection.censor.enabled") && !p.hasPermission("uchat.bypass-censor")){
			for (String word:UChat.config.getProtStringList("chat-protection.censor.replace-words")){
				if (!StringUtils.containsIgnoreCase(msg, word)){
					continue;
				} 				
				String replaceby = UChat.config.getProtString("chat-protection.censor.by-word");
				if (UChat.config.getProtBool("chat-protection.censor.replace-by-symbol")){
					replaceby = word.replaceAll("(?s).", UChat.config.getProtString("chat-protection.censor.by-symbol"));
				}
				
				if (!UChat.config.getProtBool("chat-protection.censor.replace-partial-word")){
					msg = msg.replaceAll("(?i)"+"\\b"+Pattern.quote(word)+"\\b", replaceby);
				} else {
					msg = msg.replaceAll("(?i)"+word, replaceby);
				}
			}
		}
		
		String regexIP = UChat.config.getProtString("chat-protection.anti-ip.custom-ip-regex");
		String regexUrl = UChat.config.getProtString("chat-protection.anti-ip.custom-url-regex");
		
		//check ip and website
		if (UChat.config.getProtBool("chat-protection.anti-ip.enabled") && !p.hasPermission("uchat.bypass-anti-ip")){
			
			//check whitelist
			for (String check:UChat.config.getProtStringList("chat-protection.anti-ip.whitelist-words")){
				if (Pattern.compile(check).matcher(msg).find()){	
					return;
				}
			}
			
			//continue
			if (Pattern.compile(regexIP).matcher(msg).find()){	
				addURLspam(p);
				if (UChat.config.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")){
					e.setCancelled(true);
					UChat.lang.sendMessage(p, UChat.config.getProtMsg("chat-protection.anti-ip.cancel-msg"));
					return;
				} else {
					msg = msg.replaceAll(regexIP, UChat.config.getProtMsg("chat-protection.anti-ip.replace-by-word"));
				}
			}
			if (Pattern.compile(regexUrl).matcher(msg).find()){
				addURLspam(p);
				if (UChat.config.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")){
					e.setCancelled(true);
					UChat.lang.sendMessage(p, UChat.config.getProtMsg("chat-protection.anti-ip.cancel-msg"));
					return;
				} else {
					msg = msg.replaceAll(regexUrl, UChat.config.getProtMsg("chat-protection.anti-ip.replace-by-word"));
				}
			}
			
			for (String word:UChat.config.getProtStringList("chat-protection.anti-ip.check-for-words")){
				if (Pattern.compile("(?i)"+"\\b"+word+"\\b").matcher(msg).find()){
					addURLspam(p);
					if (UChat.config.getProtString("chat-protection.anti-ip.cancel-or-replace").equalsIgnoreCase("cancel")){
						e.setCancelled(true);
						UChat.lang.sendMessage(p, UChat.config.getProtMsg("chat-protection.anti-ip.cancel-msg"));
						return;
					} else {
						msg = msg.replaceAll("(?i)"+word, UChat.config.getProtMsg("chat-protection.anti-ip.replace-by-word"));
					}
				}
			}		
		}	
		
		//capitalization verify
		if (UChat.config.getProtBool("chat-protection.chat-enhancement.enabled") && !p.hasPermission("uchat.bypass-enhancement")){
			if (!Pattern.compile(regexIP).matcher(msg).find() && !Pattern.compile(regexUrl).matcher(msg).find()){
				msg = msg.replaceAll("([.!?])\\1+", "$1").replaceAll(" +", " ").substring(0, 1).toUpperCase()+msg.substring(1).toLowerCase();
				/*String[] messages = msg.split("(?<=[.!?])");
				StringBuilder finalmsg = new StringBuilder(); 
				boolean first = true;
				for (String msgw:messages){
					if (msgw.length() <= 0){
						continue;
					}
					if (first){
						finalmsg.append(msgw.substring(0, 1).toUpperCase()+msgw.substring(1).toLowerCase());
						first = false;
					} else if (msgw.startsWith(" ")){
						finalmsg.append(msgw.substring(0, 2).toUpperCase()+msgw.substring(2).toLowerCase());
					} else {
						finalmsg.append(" "+msgw.substring(0, 1).toUpperCase()+msgw.substring(1).toLowerCase());
					}
				}					
				msg = finalmsg.toString();*/
				if (UChat.config.getProtBool("chat-protection.chat-enhancement.end-with-dot") && !msg.endsWith("?") && !msg.endsWith("!") && !msg.endsWith(".") && msg.split(" ").length > 2){
					msg = msg+".";
				}
								
				if (UChat.config.getProtBool("chat-protection.chat-enhancement.anti-flood.enable")){						
					for (String flood:UChat.config.getProtStringList("chat-protection.chat-enhancement.anti-flood.whitelist-flood-characs")){
						if (Pattern.compile("(["+flood+"])\\1+").matcher(msg).find()){
							e.setMessage(msg);	
							return;
						}
					}
					msg = msg.replaceAll("([A-Za-z])\\1+", "$1$1");
				}
			}				
		}
		e.setMessage(msg);	
	}	
	
	private void addURLspam(final Player p){
		if (UChat.config.getProtBool("chat-protection.anti-ip.punish.enable")){
			if (!UrlSpam.containsKey(p)){
				UrlSpam.put(p, 1);
			} else {
				UrlSpam.put(p, UrlSpam.get(p)+1);
				p.sendMessage("UrlSpam: "+UrlSpam.get(p));
				if (UrlSpam.get(p) >= UChat.config.getProtInt("chat-protection.anti-ip.punish.max-attempts")){
					if (UChat.config.getProtString("chat-protection.anti-ip.punish.mute-or-cmd").equalsIgnoreCase("mute")){
						muted.add(p.getName());
						p.sendMessage(UChat.config.getProtMsg("chat-protection.anti-ip.punish.mute-msg"));
						Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.plugin, new Runnable() { 
							public void run() {
								if (muted.contains(p.getName())){						
									muted.remove(p.getName());
									p.sendMessage(UChat.config.getProtMsg("chat-protection.anti-ip.punish.unmute-msg"));
								}
							}						
						},(UChat.config.getProtInt("chat-protection.anti-ip.punish.mute-duration")*60)*20);
					} else {
						UCUtil.performCommand(UChat.serv.getConsoleSender(),UChat.config.getProtString("chat-protection.anti-ip.punish.cmd-punish"));
					}	
					UrlSpam.remove(p);
				}
			}
		}		
	}
}

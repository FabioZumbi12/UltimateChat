package br.net.fabiozumbi12.UltimateChat.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.security.auth.login.LoginException;

import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.entities.Game;
import jdalib.jda.core.entities.Role;
import jdalib.jda.core.entities.TextChannel;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.PermissionException;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UCDiscord extends ListenerAdapter implements UCDInterface{	
	private JDA jda;
	private UChat uchat;
	
	public UCDiscord(UChat plugin){
		this.uchat = plugin;
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(this.uchat.getConfig().getString("discord.token")).buildBlocking();
			jda.addEventListener(this);
			if (plugin.getConfig().getBoolean("discord.update-status")){
				jda.getPresence().setGame(Game.of(plugin.getLang().get("discord.game").replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()))));
			}			
		} catch (LoginException e) {
			uchat.getLogger().severe("The TOKEN is wrong or empty! Check you config and your token.");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RateLimitedException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
    public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getMember().getUser().isFake())return;
		
		String message = e.getMessage().getRawContent();
		int used = 0;
		
		for (UCChannel ch:this.uchat.getConfig().getChannels()){
			if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())){				
				//check if is cmd
				if (message.startsWith(this.uchat.getConfig().getString("discord.server-commands.alias")) && ch.getDiscordAllowCmds()){
					message = message.replace(this.uchat.getConfig().getString("discord.server-commands.alias")+" ", "");
					if (!this.uchat.getConfig().getStringList("discord.server-commands.whitelist").isEmpty()){
						int count = 0;
						for (String cmd:this.uchat.getConfig().getStringList("discord.server-commands.whitelist")){
							if (message.startsWith(cmd)) count++;
						}
						if (count == 0) continue;
					}
					if (!this.uchat.getConfig().getStringList("discord.server-commands.blacklist").isEmpty()){
						int count = 0;
						for (String cmd:this.uchat.getConfig().getStringList("discord.server-commands.blacklist")){
							if (message.startsWith(cmd)) count++;
						}
						if (count > 0) continue;
					}
					UCUtil.performCommand(null, Bukkit.getServer().getConsoleSender(), message);					
					used++;
				} else {
					UltimateFancy fancy = new UltimateFancy();
					
					//format prefixes tags
					String formated = formatTags(ch.getDiscordtoMCFormat(), ch, e, "", "");
					
					//add dd channel name to hover
					String hovered = formatTags(ch.getDiscordHover(), ch, e, "", "");
					fancy.text(formated);
					if (!hovered.isEmpty()){
						fancy.hoverShowText(hovered);	
					}				
					fancy.next();
					
					//format message							
					if (!e.getMessage().getAttachments().isEmpty()){
						if (message.isEmpty()){
							fancy.text("- Attachment -");
						} else {
							fancy.text(message);
						}
						fancy.clickOpenURL(e.getMessage().getAttachments().get(0).getUrl());
						fancy.hoverShowText(e.getMessage().getAttachments().get(0).getFileName());
					} else {
						fancy.text(message);	
					}
					ch.sendMessage(uchat.getServer().getConsoleSender(), fancy, true);	
					used++;
				}										
			}
		}
		
		if (used == 0 && e.getChannel().getId().equals(this.uchat.getConfig().getString("discord.commands-channel-id"))){
			if (!this.uchat.getConfig().getStringList("discord.server-commands.whitelist").isEmpty()){
				int count = 0;
				for (String cmd:this.uchat.getConfig().getStringList("discord.server-commands.whitelist")){
					if (message.startsWith(cmd)) count++;
				}
				if (count == 0) return;
			}
			if (!this.uchat.getConfig().getStringList("discord.server-commands.blacklist").isEmpty()){
				int count = 0;
				for (String cmd:this.uchat.getConfig().getStringList("discord.server-commands.blacklist")){
					if (message.startsWith(cmd)) count++;
				}
				if (count > 0) return;
			}
			UCUtil.performCommand(null, Bukkit.getServer().getConsoleSender(), message);
		}
	}
	
	public void updateGame(String text){
		this.jda.getPresence().setGame(Game.of(text));
	}
	
	public void sendTellToDiscord(String text){
		if (!uchat.getConfig().getString("discord.tell-channel-id").isEmpty()){			
			sendToChannel(uchat.getConfig().getString("discord.tell-channel-id"), text);
		}
	}
	
	public void sendCommandsToDiscord(String text){
		if (!uchat.getConfig().getString("discord.commands-channel-id").isEmpty()){
			sendToChannel(uchat.getConfig().getString("discord.commands-channel-id"), text);
		}			
	}
	
	public void sendRawToDiscord(String text){
		if (!uchat.getConfig().getString("discord.log-channel-id").isEmpty()){
			sendToChannel(uchat.getConfig().getString("discord.log-channel-id"), text);
		}			
	}
	
	public void sendToDiscord(CommandSender sender, String text, UCChannel ch){
		if (ch.isSendingDiscord()){
			if (!UCPerms.hasPerm(sender, "discord.mention")){
				text = text.replace("@everyone", "everyone")
						.replace("@here", "here");
			}
			text = formatTags(ch.getMCtoDiscordFormat(), ch, null, sender.getName(), text);
					
			sendToChannel(ch.getDiscordChannelID(), text);
		}		
	}
	
	public void sendToChannel(String id, String text){
		text = text.replaceAll("([&"+ChatColor.COLOR_CHAR+"]([a-fk-or0-9]))", "");
		TextChannel ch = jda.getTextChannelById(id);
		try {
			ch.sendMessage(text).queue();
        } catch (PermissionException e) {
        	uchat.getUCLogger().severe("JDA: No permission to send messages to channel "+ch.getName()+".");
        } catch (Exception e) {
        	uchat.getUCLogger().warning("JDA: The channel ID is incorrect, not available or Discord is offline, in maintance or some other connection problem.");
        	e.printStackTrace();
        }
	}
	
	private String formatTags(String format, UCChannel ch, MessageReceivedEvent e, String sender, String message){
		format = format.replace("{ch-color}", ch.getColor())
				.replace("{ch-alias}", ch.getAlias())
				.replace("{ch-name}", ch.getName());				
		if (e != null){
			format = format.replace("{sender}", e.getMember().getEffectiveName())				
					.replace("{dd-channel}", e.getChannel().getName())				
					.replace("{message}", e.getMessage().getRawContent());
			if (!e.getMember().getRoles().isEmpty()){
				Role role = e.getMember().getRoles().get(0);
				if (role.getColor() != null){
					format = format.replace("{dd-rolecolor}", fromRGB(
							role.getColor().getRed(),
							role.getColor().getGreen(),
							role.getColor().getBlue()).toString());
				}
				format = format.replace("{dd-rolename}", role.getName());			
			}
			if (e.getMember().getNickname() != null){
				format = format.replace("{nickname}", e.getMember().getNickname());
			}
		}		
		//if not filtered 
		format = format
				.replace("{sender}", sender)
				.replace("{message}", message);		
		format = format.replaceAll("\\{.*\\}", "");	
		return ChatColor.translateAlternateColorCodes('&', format);
	}	

	public void shutdown(){
		this.uchat.getUCLogger().info("Shutdown JDA...");
		this.jda.shutdown();
		this.uchat.getUCLogger().info("JDA disabled!");
	}
	
	/*   ------ color util --------   */
	public static ChatColor fromRGB(int r, int g, int b) {
		TreeMap<Integer, ChatColor> closest = new TreeMap<Integer, ChatColor>();
		colorMap.forEach((color, set) -> {
			int red = Math.abs(r - set.getRed());
			int green = Math.abs(g - set.getGreen());
			int blue = Math.abs(b - set.getBlue());
			closest.put(red + green + blue, color);
		});
		return closest.firstEntry().getValue();
	}
	
	private static class ColorSet<R, G, B> {
		R red = null;
		G green = null;
		B blue = null;

		ColorSet(R red, G green, B blue) {
			this.red = red;
			this.green = green;
			this.blue = blue;
		}
		public R getRed() {
			return red;
		}
		public G getGreen() {
			return green;
		}
		public B getBlue() {
			return blue;
		}
	}
	
	private static Map<ChatColor, ColorSet<Integer, Integer, Integer>> colorMap = new HashMap<ChatColor, ColorSet<Integer, Integer, Integer>>();
	static {
		colorMap.put(ChatColor.BLACK, new ColorSet<Integer, Integer, Integer>(0, 0, 0));
		colorMap.put(ChatColor.DARK_BLUE, new ColorSet<Integer, Integer, Integer>(0, 0, 170));
		colorMap.put(ChatColor.DARK_GREEN, new ColorSet<Integer, Integer, Integer>(0, 170, 0));
		colorMap.put(ChatColor.DARK_AQUA, new ColorSet<Integer, Integer, Integer>(0, 170, 170));
		colorMap.put(ChatColor.DARK_RED, new ColorSet<Integer, Integer, Integer>(170, 0, 0));
		colorMap.put(ChatColor.DARK_PURPLE, new ColorSet<Integer, Integer, Integer>(170, 0, 170));
		colorMap.put(ChatColor.GOLD, new ColorSet<Integer, Integer, Integer>(255, 170, 0));
		colorMap.put(ChatColor.GRAY, new ColorSet<Integer, Integer, Integer>(170, 170, 170));
		colorMap.put(ChatColor.DARK_GRAY, new ColorSet<Integer, Integer, Integer>(85, 85, 85));
		colorMap.put(ChatColor.BLUE, new ColorSet<Integer, Integer, Integer>(85, 85, 255));
		colorMap.put(ChatColor.GREEN, new ColorSet<Integer, Integer, Integer>(85, 255, 85));
		colorMap.put(ChatColor.AQUA, new ColorSet<Integer, Integer, Integer>(85, 255, 255));
		colorMap.put(ChatColor.RED, new ColorSet<Integer, Integer, Integer>(255, 85, 85));
		colorMap.put(ChatColor.LIGHT_PURPLE, new ColorSet<Integer, Integer, Integer>(255, 85, 255));
		colorMap.put(ChatColor.YELLOW, new ColorSet<Integer, Integer, Integer>(255, 255, 85));
		colorMap.put(ChatColor.WHITE, new ColorSet<Integer, Integer, Integer>(255, 255, 255));
	}	
}

package br.net.fabiozumbi12.UltimateChat.Bukkit;

import javax.security.auth.login.LoginException;

import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UCDiscord extends ListenerAdapter {	
	private JDA jda;
	private UChat uchat;
	public JDA getJDA(){
		return this.jda;
	}
	
	public UCDiscord(UChat plugin){
		this.uchat = plugin;
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(this.uchat.getUCConfig().getString("discord.token")).buildBlocking();
			jda.addEventListener(this);
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
		
		for (UCChannel ch:this.uchat.getUCConfig().getChannels()){
			if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())){
				String message = e.getMessage().getRawContent();
				
				if (message.startsWith(this.uchat.getUCConfig().getString("discord.server-commands.alias")) && ch.getDiscordAllowCmds()){
					message = message.replace(this.uchat.getUCConfig().getString("discord.server-commands.alias")+" ", "");
					if (!this.uchat.getUCConfig().getStringList("discord.server-commands.whitelist").isEmpty()){
						int count = 0;
						for (String cmd:this.uchat.getUCConfig().getStringList("discord.server-commands.whitelist")){
							if (message.startsWith(cmd)) count++;
						}
						if (count == 0)return;
					}
					if (!this.uchat.getUCConfig().getStringList("discord.server-commands.blacklist").isEmpty()){
						for (String cmd:this.uchat.getUCConfig().getStringList("discord.server-commands.blacklist")){
							if (message.startsWith(cmd)) return;
						}
					}
					UCUtil.performCommand(null, Bukkit.getServer().getConsoleSender(), message);
				} else {
					UltimateFancy fancy = new UltimateFancy();
					
					//format prefixes tags
					String formated = formatTags(ch.getDiscordFormat(), ch, e);
					
					//add dd channel name to hover
					String hovered = formatTags(ch.getDiscordHover(), ch, e);
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
					ch.sendMessage(uchat.getServ().getConsoleSender(), fancy, true);	
				}										
			}
		}
	}
	
	public void sendRawToDiscord(String text){
		if (!uchat.getUCConfig().getString("discord.log-channel-id").isEmpty()){
			jda.getTextChannelById(uchat.getUCConfig().getString("discord.log-channel-id")).sendMessage(text).queue();
		}			
	}
	
	public void sendToDiscord(CommandSender sender, String text, UCChannel ch){
		if (ch.isSendingDiscord()){
			if (!UCPerms.hasPerm(sender, "discord.mention")){
				text = text.replace("@everyone", "everyone")
						.replace("@here", "here");
			}
			jda.getTextChannelById(ch.getDiscordChannelID()).sendMessage(":thought_balloon: **"+sender.getName()+"**: "+text).queue();
		}		
	}
	
	private String formatTags(String text, UCChannel ch, MessageReceivedEvent e){
		text = text.replace("{ch-color}", ch.getColor())
				.replace("{ch-alias}", ch.getAlias())
				.replace("{ch-name}", ch.getName())
				.replace("{sender}", e.getMember().getEffectiveName())				
				.replace("{dd-channel}", e.getChannel().getName())				
				.replace("{message}", e.getMessage().getRawContent());
		if (e.getMember().getNickname() != null){
			text = text.replace("{nickname}", e.getMember().getNickname());
		}
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	public void shutdown(){
		this.uchat.getUCLogger().info("Shutdown JDA...");
		this.jda.shutdown();
		this.uchat.getUCLogger().info("JDA disabled!");
	}
}

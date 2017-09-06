package br.net.fabiozumbi12.UltimateChat.Bukkit.util.Discord;

import javax.security.auth.login.LoginException;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UltimateFancy;
import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;

public class UCDiscord extends ListenerAdapter {	
	private JDA jda;
	private UChat uchat;
	public JDA getJDA(){
		return this.jda;
	}
	
	public UCDiscord(UChat plugin){
		this.uchat = plugin;
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(this.uchat.getUCConfig().getString("discord.OAuth")).buildBlocking();
			jda.addEventListener(this);
		} catch (LoginException e) {
			e.printStackTrace();
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
					if (e.getMessage().getRawContent().isEmpty()){
						fancy.text("- Attachment -");
					} else {
						fancy.text(e.getMessage().getRawContent());
					}
					fancy.clickOpenURL(e.getMessage().getAttachments().get(0).getUrl());
					fancy.hoverShowText(e.getMessage().getAttachments().get(0).getFileName());
				} else {
					fancy.text(e.getMessage().getRawContent());	
				}
				ch.sendMessage(uchat.getServ().getConsoleSender(), fancy, true);							
			}
		}
	}
	
	public void sendToDsicord(CommandSender sender, String text, UCChannel ch){
		if (ch.isSendingDiscord()){
			jda.getTextChannelById(ch.getDiscordChannelID()).sendMessage(sender.getName()+": "+text).queue();
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
		this.jda.removeEventListener(this);
		this.jda.shutdownNow();
		this.uchat.getUCLogger().info("JDA disabled!");
	}
}

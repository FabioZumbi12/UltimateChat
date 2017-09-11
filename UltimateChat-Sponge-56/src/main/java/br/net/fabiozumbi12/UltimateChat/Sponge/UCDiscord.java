package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.net.MalformedURLException;
import java.net.URL;

import javax.security.auth.login.LoginException;

import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;

public class UCDiscord extends ListenerAdapter {	
	private JDA jda;
	private UChat uchat;
	public JDA getJDA(){
		return this.jda;
	}
	
	public UCDiscord(UChat plugin){
		this.uchat = plugin;
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(this.uchat.getConfig().getString("discord","token")).buildBlocking();
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
		
		for (UCChannel ch:this.uchat.getConfig().getChannels()){
			if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())){
				String message = e.getMessage().getRawContent();
				
				//check if is cmd
				if (message.startsWith(this.uchat.getConfig().getString("discord","server-commands","alias")) && ch.getDiscordAllowCmds()){
					message = message.replace(this.uchat.getConfig().getString("discord","server-commands","alias")+" ", "");
					if (!this.uchat.getConfig().getStringList("discord","server-commands","whitelist").isEmpty()){
						int count = 0;
						for (String cmd:this.uchat.getConfig().getStringList("discord","server-commands","whitelist")){
							if (message.startsWith(cmd)) count++;
						}
						if (count == 0)return;
					}
					if (!this.uchat.getConfig().getStringList("discord","server-commands","blacklist").isEmpty()){
						for (String cmd:this.uchat.getConfig().getStringList("discord","server-commands","blacklist")){
							if (message.startsWith(cmd)) return;
						}
					}
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(), message);
				} else {
					Builder text = Text.builder();
					
					//format prefixes tags
					String formated = formatTags(ch.getDiscordFormat(), ch, e);
					
					//add dd channel name to hover
					String hovered = formatTags(ch.getDiscordHover(), ch, e);
					text.append(Text.of(formated));
					if (!hovered.isEmpty()){
						text.onHover(TextActions.showText(Text.of(hovered)));
					}				
									
					//format message							
					if (!e.getMessage().getAttachments().isEmpty()){
						if (message.isEmpty()){
							text.append(Text.of("- Attachment -"));
						} else {
							text.append(Text.of(message));
						}
						try {
							text.onClick(TextActions.openUrl(new URL(e.getMessage().getAttachments().get(0).getUrl())));
						} catch (MalformedURLException e1) {}
						text.onHover(TextActions.showText(Text.of(e.getMessage().getAttachments().get(0).getFileName())));
					} else {
						text.append(Text.of(message));
					}
					ch.sendMessage(Sponge.getServer().getConsole(), text.build(), true);	
				}										
			}
		}
	}
	
	public void sendRawToDiscord(String text){
		if (!uchat.getConfig().getString("discord","log-channel-id").isEmpty()){
			jda.getTextChannelById(uchat.getConfig().getString("discord","log-channel-id")).sendMessage(text).queue();
		}			
	}
	
	public void sendToDiscord(CommandSource sender, String text, UCChannel ch){
		if (ch.isSendingDiscord()){
			if (!UChat.get().getPerms().hasPerm(sender, "discord.mention")){
				text = text.replace("@everyone", "everyone")
						.replace("@here", "here");
			}
			jda.getTextChannelById(ch.getDiscordChannelID()).sendMessage(":thought_balloon: **"+sender.getName()+":** "+text).queue();
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
		return UCUtil.toColor(text);
	}
	
	public void shutdown(){
		this.uchat.getLogger().info("Shutdown JDA...");
		this.jda.shutdown();
		this.uchat.getLogger().info("JDA disabled!");
	}
}
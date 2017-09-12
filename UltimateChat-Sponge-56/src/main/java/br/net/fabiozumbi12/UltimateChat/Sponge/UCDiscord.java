package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.security.auth.login.LoginException;

import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.entities.Role;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

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
		if (!e.getMember().getRoles().isEmpty()){
			Role role = e.getMember().getRoles().get(0);
			if (role.getColor() != null){
				text = text.replace("{dd-rolecolor}", fromRGB(
						role.getColor().getRed(),
						role.getColor().getGreen(),
						role.getColor().getBlue()));
			}
			text = text.replace("{dd-rolename}", role.getName());			
		}		
		if (e.getMember().getNickname() != null){
			text = text.replace("{nickname}", e.getMember().getNickname());
		}TextColors.RED.toString();
		return UCUtil.toColor(text);
	}
	
	public void shutdown(){
		this.uchat.getLogger().info("Shutdown JDA...");
		this.jda.shutdown();
		this.uchat.getLogger().info("JDA disabled!");
	}
	
	/*   ------ color util --------   */
	public static String fromRGB(int r, int g, int b) {
		TreeMap<Integer, String> closest = new TreeMap<Integer, String>();		
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
	
	private static Map<String, ColorSet<Integer, Integer, Integer>> colorMap = new HashMap<String, ColorSet<Integer, Integer, Integer>>();
	static {
		colorMap.put("&0", new ColorSet<Integer, Integer, Integer>(0, 0, 0));
		colorMap.put("&1", new ColorSet<Integer, Integer, Integer>(0, 0, 170));
		colorMap.put("&2", new ColorSet<Integer, Integer, Integer>(0, 170, 0));
		colorMap.put("&3", new ColorSet<Integer, Integer, Integer>(0, 170, 170));
		colorMap.put("&4", new ColorSet<Integer, Integer, Integer>(170, 0, 0));
		colorMap.put("&5", new ColorSet<Integer, Integer, Integer>(170, 0, 170));
		colorMap.put("&6", new ColorSet<Integer, Integer, Integer>(255, 170, 0));
		colorMap.put("&7", new ColorSet<Integer, Integer, Integer>(170, 170, 170));
		colorMap.put("&8", new ColorSet<Integer, Integer, Integer>(85, 85, 85));
		colorMap.put("&9", new ColorSet<Integer, Integer, Integer>(85, 85, 255));
		colorMap.put("&a", new ColorSet<Integer, Integer, Integer>(85, 255, 85));
		colorMap.put("&b", new ColorSet<Integer, Integer, Integer>(85, 255, 255));
		colorMap.put("&c", new ColorSet<Integer, Integer, Integer>(255, 85, 85));
		colorMap.put("&d", new ColorSet<Integer, Integer, Integer>(255, 85, 255));
		colorMap.put("&e", new ColorSet<Integer, Integer, Integer>(255, 255, 85));
		colorMap.put("&f", new ColorSet<Integer, Integer, Integer>(255, 255, 255));
	}
}
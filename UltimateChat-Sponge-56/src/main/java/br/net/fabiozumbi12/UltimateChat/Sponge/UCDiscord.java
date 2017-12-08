package br.net.fabiozumbi12.UltimateChat.Sponge;

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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;

import javax.security.auth.login.LoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class UCDiscord extends ListenerAdapter implements UCDInterface {	
	private JDA jda;
	private UChat uchat;

    public boolean JDAAvailable(){
        return this.jda != null;
    }
	
	public UCDiscord(UChat plugin){
		this.uchat = plugin;
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(this.uchat.getConfig().root().discord.token).buildBlocking();
			jda.addEventListener(this);
			if (plugin.getConfig().root().discord.update_status){
				jda.getPresence().setGame(Game.of(plugin.getLang().get("discord.game").replace("{online}", String.valueOf(Sponge.getServer().getOnlinePlayers().size()))));
			}			
		} catch (LoginException e) {
			uchat.getLogger().severe("The TOKEN is wrong or empty! Check you config and your token.");
		} catch (IllegalArgumentException | InterruptedException | RateLimitedException e) {
			e.printStackTrace();
		}
    }
	
	@Override
    public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getMember().getUser().isFake())return;
		
		String message = e.getMessage().getRawContent();	
		int used = 0;
		
		for (UCChannel ch:this.uchat.getChannels().values()){
			if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())){				
				//check if is cmd
				if (message.startsWith(this.uchat.getConfig().root().discord.server_commands.alias) && ch.getDiscordAllowCmds()){
					message = message.replace(this.uchat.getConfig().root().discord.server_commands.alias+" ", "");
					if (!this.uchat.getConfig().root().discord.server_commands.withelist.isEmpty()){
						int count = 0;
						for (String cmd:this.uchat.getConfig().root().discord.server_commands.withelist){
							if (message.startsWith(cmd)) count++;
						}
						if (count == 0) continue;
					}
					if (!this.uchat.getConfig().root().discord.server_commands.blacklist.isEmpty()){
						int count = 0;
						for (String cmd:this.uchat.getConfig().root().discord.server_commands.blacklist){
							if (message.startsWith(cmd)) count++;
						}
						if (count > 0) continue;
					}
					Sponge.getCommandManager().process(Sponge.getServer().getConsole(), message);
					used++;
				} else {
					Builder text = Text.builder();
					
					//format prefixes tags
					String formated = formatTags(ch.getDiscordtoMCFormat(), ch, e, "", "");
					
					//add dd channel name to hover
					String hovered = formatTags(ch.getDiscordHover(), ch, e, "", "");
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
						} catch (MalformedURLException ignored) {}
						text.onHover(TextActions.showText(Text.of(e.getMessage().getAttachments().get(0).getFileName())));
					} else {
						text.append(Text.of(message));
					}
					ch.sendMessage(Sponge.getServer().getConsole(), text.build(), true);	
					used++;
				}										
			}
		}
		
		//check if is from log command chanel
		if (used == 0 && e.getChannel().getId().equals(UChat.get().getConfig().root().discord.commands_channel_id)){
			if (!this.uchat.getConfig().root().discord.server_commands.withelist.isEmpty()){
				int count = 0;
				for (String cmd:this.uchat.getConfig().root().discord.server_commands.withelist){
					if (message.startsWith(cmd)) count++;
				}
				if (count == 0) return;
			}
			if (!this.uchat.getConfig().root().discord.server_commands.blacklist.isEmpty()){
				int count = 0;
				for (String cmd:this.uchat.getConfig().root().discord.server_commands.blacklist){
					if (message.startsWith(cmd)) count++;
				}
				if (count > 0) return;
			}
			Sponge.getCommandManager().process(Sponge.getServer().getConsole(), message);
		} 	
	}
	
	public void updateGame(String text){
		this.jda.getPresence().setGame(Game.of(text));
	}
	
	public void sendTellToDiscord(String text){
		if (!uchat.getConfig().root().discord.tell_channel_id.isEmpty()){
			sendToChannel(uchat.getConfig().root().discord.tell_channel_id, text);
		}
	}
	
	public void sendCommandsToDiscord(String text){
		if (!uchat.getConfig().root().discord.commands_channel_id.isEmpty()){
			sendToChannel(uchat.getConfig().root().discord.commands_channel_id, text);
		}			
	}
	
	public void sendRawToDiscord(String text){
		if (!uchat.getConfig().root().discord.log_channel_id.isEmpty()){
			sendToChannel(uchat.getConfig().root().discord.log_channel_id, text);
		}			
	}
	
	public void sendToDiscord(CommandSource sender, String text, UCChannel ch){
		if (ch.isSendingDiscord()){
			if (!uchat.getPerms().hasPerm(sender, "discord.mention")){
				text = text.replace("@everyone", "everyone")
						.replace("@here", "here");
			}
			text = text.replaceAll("([&§]([a-fk-or0-9]))", "");
			text = formatTags(ch.getMCtoDiscordFormat(), ch, null, sender.getName(), text);
			
			sendToChannel(ch.getDiscordChannelID(), text);
		}		
	}
	
	private void sendToChannel(String id, String text){
		TextChannel ch = jda.getTextChannelById(id);
		try {
			ch.sendMessage(text).queue();
        } catch (PermissionException e) {
        	uchat.getLogger().severe("JDA: No permission to send messages to channel "+ch.getName()+".");
        } catch (Exception e) {
        	uchat.getLogger().warning("JDA: The channel ID is incorrect, not available or Discord is offline, in maintance or some other connection problem.");
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
                            role.getColor().getBlue()));
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
		return UCUtil.toColor(format);
	}
	
	public void shutdown(){
		this.uchat.getLogger().info("Shutdown JDA...");
		this.jda.shutdown();
		this.uchat.getLogger().info("JDA disabled!");
	}
	
	/*   ------ color util --------   */
	private static String fromRGB(int r, int g, int b) {
		TreeMap<Integer, String> closest = new TreeMap<>();
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
	
	private static final Map<String, ColorSet<Integer, Integer, Integer>> colorMap = new HashMap<>();
	static {
		colorMap.put("&0", new ColorSet<>(0, 0, 0));
		colorMap.put("&1", new ColorSet<>(0, 0, 170));
		colorMap.put("&2", new ColorSet<>(0, 170, 0));
		colorMap.put("&3", new ColorSet<>(0, 170, 170));
		colorMap.put("&4", new ColorSet<>(170, 0, 0));
		colorMap.put("&5", new ColorSet<>(170, 0, 170));
		colorMap.put("&6", new ColorSet<>(255, 170, 0));
		colorMap.put("&7", new ColorSet<>(170, 170, 170));
		colorMap.put("&8", new ColorSet<>(85, 85, 85));
		colorMap.put("&9", new ColorSet<>(85, 85, 255));
		colorMap.put("&a", new ColorSet<>(85, 255, 85));
		colorMap.put("&b", new ColorSet<>(85, 255, 255));
		colorMap.put("&c", new ColorSet<>(255, 85, 85));
		colorMap.put("&d", new ColorSet<>(255, 85, 255));
		colorMap.put("&e", new ColorSet<>(255, 255, 85));
		colorMap.put("&f", new ColorSet<>(255, 255, 255));
	}
}
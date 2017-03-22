package br.net.fabiozumbi12.UltimateChat.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.UCUtil;
import br.net.fabiozumbi12.UltimateChat.UChat;

public class UCConfig{
	
	private HashMap<List<String>,UCChannel> channels = null;
	
	private File defConfig = new File(UChat.get().configDir()+"config.conf");
	private CommentedConfigurationNode config;	
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	
	private File defProt = new File(UChat.get().configDir()+"protections.conf");
	private CommentedConfigurationNode prots;	
	private ConfigurationLoader<CommentedConfigurationNode> protsManager;
	
	public UCConfig(UChat plugin) throws IOException {
		
		UChat.get().getLogger().info("-> Config module");
		try {
			Files.createDirectories(new File(UChat.get().configDir()).toPath());
			if (!defConfig.exists()){
				UChat.get().getLogger().info("Creating config file...");
				defConfig.createNewFile();
			}
			
			/*--------------------- config.conf ---------------------------*/
			configManager = HoconConfigurationLoader.builder().setFile(defConfig).build();	
			config = configManager.load();
			
			config.getNode("_config-version").setValue(config.getNode("_config-version").getDouble(1.0));
			config.getNode("debug-messages").setValue(config.getNode("debug-messages").getBoolean(false));
			config.getNode("language").setValue(config.getNode("language").getString("EN-US"));
			
			config.getNode("mention").setComment("Use mentions on chat to change the player name color and play a sound on mention.");
			config.getNode("mention","enable").setValue(config.getNode("mention","enable").getBoolean(true));
			config.getNode("mention","color-template").setValue(config.getNode("mention","color-template").getString("&e@{mentioned-player}&r"));
			config.getNode("mention","playsound").setValue(config.getNode("mention","playsound").getString("minecraft:block.note.pling"));
			config.getNode("mention","hover-message").setValue(config.getNode("mention","hover-message").getString("&e{playername} mentioned you!"));
			
			config.getNode("general").setComment("General settings.");
			config.getNode("general","URL-template").setValue(config.getNode("general","URL-template").getString("Click to open &n{url}&r"))
			.setComment("Template to show when players send links or urls.");
			config.getNode("general","console-tag").setValue(config.getNode("general","console-tag").getString("&6 {console}&3"))
			.setComment("Tag to show when sent messagens from console to channels.");
			config.getNode("general","remove-from-chat").setValue(config.getNode("general","remove-from-chat").getString("[]"))
			.setComment("Remove this from chat (like empty tags)");
			config.getNode("general","channel-cmd-aliases").setValue(config.getNode("general","channel-cmd-aliases").getString("channel, ch"))
			.setComment("Command and aliases for /channel command.");
			config.getNode("general","umsg-cmd-aliases").setValue(config.getNode("general","umsg-cmd-aliases").getString("umsg"))
			.setComment("Aliases to send commands from system to players (without any format, good to send messages from other plugins direct to players).");
			config.getNode("general","default-channel").setValue(config.getNode("general","default-channel").getString("l"))
			.setComment("Set the efault channel for new players or when players join on server.");
			config.getNode("general","spy-format").setValue(config.getNode("general","spy-format").getString("&c[Spy] {output}"))
			.setComment("Chat spy format.");
			config.getNode("general","default-tag-builder").setValue(config.getNode("general","default-tag-builder").getString("world,ch-tags,prefix,nickname,suffix,message"))
			.setComment("This is the main tag builder.\n"
					+ "Change the order of this tags to change how tag is displayed on chat.\n"
					+ "This tags represent the names of tag in this configuration.");
			
			config.getNode("tell","enable").setValue(config.getNode("tell","enable").getBoolean(true))
			.setComment("Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.");
			config.getNode("tell","cmd-aliases").setValue(config.getNode("tell","cmd-aliases").getString("tell,t,w,m,msg,private,priv"))
			.setComment("Aliases for tell command.");
			config.getNode("tell","prefix").setValue(config.getNode("tell","prefix").getString("&6[&c{playername} &6-> &c{receivername}&6]: "))
			.setComment("Prefix of tell messages.");
			config.getNode("tell","format").setValue(config.getNode("tell","format").getString("{message}"))
			.setComment("Suffix (or message) of tell.");
			config.getNode("tell","hover-messages").setValue(config.getNode("tell","hover-messages").getString(""))
			.setComment("Hover messages to show on tell messages.");
			
			config.getNode("broadcast","enable").setValue(config.getNode("broadcast","enable").getBoolean(true))
			.setComment("Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.");
			config.getNode("broadcast","on-hover").setValue(config.getNode("broadcast","on-hover").getString("hover:"))
			.setComment("Tag to use on broadcast message to set a hover message.");
			config.getNode("broadcast","on-click").setValue(config.getNode("broadcast","on-click").getString("click:"))
			.setComment("Tag to use on broadcast message to set a click event.");
			config.getNode("broadcast","url").setValue(config.getNode("broadcast","url").getString("url:"))
			.setComment("Tag to use on broadcast message to set a hover event.");
			config.getNode("broadcast","aliases").setValue(config.getNode("broadcast","aliases").getString("broadcast,broad,announce,say,action,all,anunciar,todos"))
			.setComment("Aliases to use for broadcast.");
			
			config.getNode("hooks").setComment("Enable hook with other plugins here. Only enable if installed.");
			config.getNode("hooks","MCClans","enable").setValue(config.getNode("hooks","MCClans","enable").getBoolean(false));
			
			config.getNode("tags").setComment("This is where you will create as many tags you want.\n"
					+ "You can use the tag \"custon-tag\" as base to create your own tags.\n"
					+ "When finish, get the name of your tag and put on \"general.default-tag-build\" \n"
					+ "or on channel builder on \"channels\" folder.");
			if (!config.getNode("tags").hasMapChildren()){
				config.getNode("tags","prefix","format").setValue("{option_prefix}");
				config.getNode("tags","prefix","hover-messages").setValue(Arrays.asList("&3Rank: &f{option_display_name}"));
				
				config.getNode("tags","nickname","format").setValue("{nickname}");
				config.getNode("tags","nickname","hover-messages").setValue(Arrays.asList("&3Player: &f{playername}","&3Money: &7{balance}"));
				
				config.getNode("tags","playername","format").setValue("{playername}");
				config.getNode("tags","nickname","hover-messages").setValue(Arrays.asList("&3Player: &f{playername}","&3Money: &7{balance}"));
				
				config.getNode("tags","suffix","format").setValue("{option_suffix}");
												
				config.getNode("tags","world","format").setValue("&7[{world}]&r");
				config.getNode("tags","world","hover-messages").setValue(Arrays.asList("&7Sent from world {world}"));
								
				config.getNode("tags","message","format").setValue("{message}");
				
				config.getNode("tags","ch-tags","format").setValue("{ch-color}[{ch-alias}]&r");
				config.getNode("tags","ch-tags","click-cmd").setValue("ch {ch-alias}");
				config.getNode("tags","ch-tags","hover-messages").setValue(Arrays.asList("&3Channel name: {ch-color}{ch-name}","&bClick to join this channel"));
				
				config.getNode("tags","admin-chat","format").setValue("&b[&r{playername}&b]&r: &b");
												
				config.getNode("tags","custom-tag","format").setValue("&7[&2MyTag&7]");
				config.getNode("tags","custom-tag","click-cmd").setValue("");
				config.getNode("tags","custom-tag","hover-messages").setValue(Arrays.asList(""));
				config.getNode("tags","custom-tag","permission").setValue("any-name-perm.custom-tag");
				config.getNode("tags","custom-tag","show-in-worlds").setValue(Arrays.asList(""));
				config.getNode("tags","custom-tag","hide-in-worlds").setValue(Arrays.asList(""));				
			}
			
			/*------------------------ add new configs -------------------------*/
			int update = 0;
			if (config.getNode("_config-version").getDouble() < 1.1){
				config.getNode("_config-version").setValue(1.1);
				
				config.getNode("tags","vannila-chat").setComment("This is the default vanilla chat format.\n"
						+ "Add this tag name to the default-builder if you want to use \n"
						+ "vanilla or if other plugins have modificed the tags like nickname of Nucleus.");
				config.getNode("tags","vannila-chat","format").setValue("{chat_header}{chat_body}");
				update++;
			}
			
			if (update > 0){
				UChat.get().getLogger().warning("Configuration updated with new options.");
			}
			
			/*--------------------- protections.conf ---------------------------*/
			protsManager = HoconConfigurationLoader.builder().setFile(defProt).build();	
			prots = protsManager.load();
			
			prots.getNode("chat-protection","chat-enhancement","enable").setValue(prots.getNode("chat-protection","chat-enhancement","enable").getBoolean(true));
			prots.getNode("chat-protection","chat-enhancement","end-with-dot").setValue(prots.getNode("chat-protection","chat-enhancement","end-with-dot").getBoolean(true));
			prots.getNode("chat-protection","chat-enhancement","minimum-lenght").setValue(prots.getNode("chat-protection","chat-enhancement","minimum-lenght").getInt(3));
			
			prots.getNode("chat-protection","anti-flood","enable").setValue(prots.getNode("chat-protection","anti-flood","enable").getBoolean(true));
			prots.getNode("chat-protection","anti-flood","whitelist-flood-characs")
			.setValue(prots.getNode("chat-protection","anti-flood","whitelist-flood-characs").getList(TypeToken.of(String.class), Arrays.asList("k")));
			
			prots.getNode("chat-protection","caps-filter","enable").setValue(prots.getNode("chat-protection","caps-filter","enable").getBoolean(true));
			prots.getNode("chat-protection","caps-filter","minimum-lenght").setValue(prots.getNode("chat-protection","caps-filter","minimum-lenght").getInt(3));
			
			prots.getNode("chat-protection","antispam","enable").setValue(prots.getNode("chat-protection","antispam","enable").getBoolean(false));
			prots.getNode("chat-protection","antispam","time-beteween-messages").setValue(prots.getNode("chat-protection","antispam","time-beteween-messages").getInt(1));
			prots.getNode("chat-protection","antispam","count-of-same-message").setValue(prots.getNode("chat-protection","antispam","count-of-same-message").getInt(5));
			prots.getNode("chat-protection","antispam","time-beteween-same-messages").setValue(prots.getNode("chat-protection","antispam","time-beteween-same-messages").getInt(10));
			prots.getNode("chat-protection","antispam","colldown-msg").setValue(prots.getNode("chat-protection","antispam","colldown-msg").getString("&6Slow down your messages!"));
			prots.getNode("chat-protection","antispam","wait-message").setValue(prots.getNode("chat-protection","antispam","wait-message").getString("&cWait to send the same message again!"));
			prots.getNode("chat-protection","antispam","cmd-action").setValue(prots.getNode("chat-protection","antispam","cmd-action").getString("kick {player} Relax, slow down your messages frequency ;)"));
			
			prots.getNode("chat-protection","censor","enable").setValue(prots.getNode("chat-protection","censor","enable").getBoolean(true));
			prots.getNode("chat-protection","censor","replace-by-symbol").setValue(prots.getNode("chat-protection","censor","replace-by-symbol").getBoolean(true));
			prots.getNode("chat-protection","censor","by-symbol").setValue(prots.getNode("chat-protection","censor","by-symbol").getString("*"));
			prots.getNode("chat-protection","censor","by-word").setValue(prots.getNode("chat-protection","censor","by-word").getString("censored"));
			prots.getNode("chat-protection","censor","replace-partial-word").setValue(prots.getNode("chat-protection","censor","replace-partial-word").getBoolean(false));
			prots.getNode("chat-protection","censor","action","cmd").setValue(prots.getNode("chat-protection","censor","action","cmd").getString(""));
			prots.getNode("chat-protection","censor","action","only-on-channels").setValue(prots.getNode("chat-protection","censor","action","only-on-channels").getList(TypeToken.of(String.class), Arrays.asList("global")));
			prots.getNode("chat-protection","censor","action","partial-words").setValue(prots.getNode("chat-protection","censor","action","partial-words").getBoolean(false));
			prots.getNode("chat-protection","censor","replace-words")
			.setValue(prots.getNode("chat-protection","censor","replace-words").getList(TypeToken.of(String.class), Arrays.asList("word1")));
			
			prots.getNode("chat-protection","anti-ip","enable").setValue(prots.getNode("chat-protection","anti-ip","enable").getBoolean(true));
			prots.getNode("chat-protection","anti-ip","custom-ip-regex").setValue(prots.getNode("chat-protection","anti-ip","custom-ip-regex").getString("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"));
			prots.getNode("chat-protection","anti-ip","custom-url-regex").setValue(prots.getNode("chat-protection","anti-ip","custom-url-regex").getString("((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)"));
			prots.getNode("chat-protection","anti-ip","check-for-words")
			.setValue(prots.getNode("chat-protection","anti-ip","check-for-words").getList(TypeToken.of(String.class), Arrays.asList("www.google.com")));
			prots.getNode("chat-protection","anti-ip","whitelist-words")
			.setValue(prots.getNode("chat-protection","anti-ip","whitelist-words").getList(TypeToken.of(String.class), Arrays.asList("www.myserver.com","prntscr.com","gyazo.com","www.youtube.com")));
			prots.getNode("chat-protection","anti-ip","cancel-or-replace").setValue(prots.getNode("chat-protection","anti-ip","cancel-or-replace").getString("cancel"));
			prots.getNode("chat-protection","anti-ip","cancel-msg").setValue(prots.getNode("chat-protection","anti-ip","cancel-msg").getString("&cYou cant send websites or ips on chat"));
			prots.getNode("chat-protection","anti-ip","replace-by-word").setValue(prots.getNode("chat-protection","anti-ip","replace-by-word").getString("-removed-"));
			prots.getNode("chat-protection","anti-ip","punish","enable").setValue(prots.getNode("chat-protection","anti-ip","punish","enable").getBoolean(false));
			prots.getNode("chat-protection","anti-ip","punish","max-attempts").setValue(prots.getNode("chat-protection","anti-ip","punish","max-attempts").getInt(3));
			prots.getNode("chat-protection","anti-ip","punish","mute-or-cmd").setValue(prots.getNode("chat-protection","anti-ip","punish","mute-or-cmd").getString("mute"));
			prots.getNode("chat-protection","anti-ip","punish","mute-duration").setValue(prots.getNode("chat-protection","anti-ip","punish","mute-duration").getInt(1));
			prots.getNode("chat-protection","anti-ip","punish","mute-msg").setValue(prots.getNode("chat-protection","anti-ip","punish","mute-msg").getString("&cYou have been muted for send IPs or URLs on chat!"));
			prots.getNode("chat-protection","anti-ip","punish","unmute-msg").setValue(prots.getNode("chat-protection","anti-ip","punish","unmute-msg").getString("&aYou can chat again!"));
			prots.getNode("chat-protection","anti-ip","punish","cmd-punish").setValue(prots.getNode("chat-protection","anti-ip","punish","cmd-punish").getString("tempban {player} 10m &cYou have been warned about send links or IPs on chat!"));
			
		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		}
				
    	    File chfolder = new File(UChat.get().configDir()+File.separator+"channels");
    	            
    	    if (!chfolder.exists()) {
            	chfolder.mkdir();
                UChat.get().getLogger().info("Created folder: " +chfolder.getPath());
            }    	            
    	            
    	  //--------------------------------------- Load Aliases -----------------------------------//
            
            channels = new HashMap<List<String>,UCChannel>();
            File[] listOfFiles = chfolder.listFiles();
            if (listOfFiles.length == 0){
            	UCUtil.saveResource("global.conf", new File(chfolder+File.separator+"global.conf"));
            	UCUtil.saveResource("local.conf", new File(chfolder+File.separator+"local.conf"));
            	UCUtil.saveResource("admin.conf", new File(chfolder+File.separator+"admin.conf"));
            	listOfFiles = chfolder.listFiles();
            }
            
            CommentedConfigurationNode channel;	
        	ConfigurationLoader<CommentedConfigurationNode> channelManager;
            
    		for (File file:listOfFiles){
    			if (file.getName().endsWith(".conf")){
    				channelManager = HoconConfigurationLoader.builder().setFile(file).build();	
    				channel = channelManager.load();
    				
					try {
						UCChannel ch = new UCChannel(channel.getNode("name").getString(), 
								channel.getNode("alias").getString(), 
								channel.getNode("across-worlds").getBoolean(true),
								channel.getNode("distance").getInt(0),
								channel.getNode("color").getString("&b"),
								channel.getNode("tag-builder").getString(config.getNode("general","default-tag-builder").getString()),
								channel.getNode("need-focus").getBoolean(false),
								channel.getNode("receivers-message").getBoolean(true),
								channel.getNode("cost").getDouble(0.0),
								channel.getNode("bungee").getBoolean(false),
								channel.getNode("use-this-builder").getBoolean(false),
								channel.getNode("channelAlias","enable").getBoolean(false),
								channel.getNode("channelAlias","sendAs").getString("player"),
								channel.getNode("channelAlias","cmd").getString(""),
								channel.getNode("available-worlds").getList(TypeToken.of(String.class), new ArrayList<String>()),
								channel.getNode("canLock").getBoolean(true));
						addChannel(ch);
					} catch (ObjectMappingException e1) {
						e1.printStackTrace();
					}
    			}
    		}
                    
    		//-------------------------------- Change config Header ----------------------------------//
            
    		String lang = config.getNode("language").getString("EN-US");
    		if (lang.equalsIgnoreCase("EN-US")){
    			config.getNode("_config-version").setComment(""
    					+ "Uchat configuration file\n"
    					+ "Author: FabioZumbi12\n"
    					+ "We recommend you to use NotePad++ to edit this file and avoid TAB errors!\n"
    					+ "------------------------------------------------------------------------\n"
    					+ "\n"
    					+ "Tags is where you can customize what will show on chat, on hover or on click on tag.\n"
    					+ "To add a tag, you can copy an existent and change the name and the texts.\n"
    					+ "After add and customize your tag, put the tag name on 'general > default-tag-builder'.\n"
    					+ "------------------------------------------------------------------------\n"
    					+ "###### Do not rename the tags 'playername', 'nickname' and 'message' ########\n"
    					+ "############ or the plugin will not parse the tag correctly! ################\n"
    					+ "------------------------------------------------------------------------\n"
    					+ "\n"
    					+ "Available replacers:\n"
    					+ " - {world}: Replaced by sender world;\n"
    					+ " - {message}: Message sent by player;\n"
    					+ " - {playername}: The name of player;\n"
    					+ " - {nickname}: The nickname of player. If not set, will show realname;\n"
    					+ " - {ch-name}: Channel name;\n"
    					+ " - {ch-alias}: Channel alias;\n"
    					+ " - {ch-color}: Channel color;\n"
    					+ " - {balance}: Get the sender money;\n"
    					+ "\n"
    					+ "Permissions:\n"
    					+ " - {option_group}: Get the group name;\n"
    					+ " - {option_prefix}: Get the prefix of group (if set);\n"
    					+ " - {option_suffix}: Get the suffix of group (if set);\n"
    					+ " - {option_display_name}: Get the custom name of group (if set);\n"
    					+ " - {option_<key option>}: Get some custom key option from your group in permissions like {option_home-count} to get home count from Nucleus;\n"
    					+ "\n"
    					+ "Vanilla Chat:\n"
    					+ " - {chat_header}: Get the header of chat;\n"
    					+ " - {chat_body}: Get the body of chat;\n"
    					+ " - {chat_footer}: Get the footer of chat;\n"
    					+ " - {chat_all}: Get all default formats;\n"
    					+ "\n"
    					+ "MCClans:\n"
    					+ " - {clan_name}: The name of clan;\n"
    					+ " - {clan_tag}: Clan tag;\n"
    					+ " - {clan_tag_color}: Clan tag with colors;\n"
    					+ " - {clan_kdr}: Clan KDR;\n"
    					+ " - {clan_player_rank}: Get the player rank on Clan;\n"
    					+ " - {clan_player_kdr}: Get the player KDR;\n"
    					+ " - {clan_player_ffprotected}: Get if player is friendly fire protected;\n"
    					+ " - {clan_player_isowner}: Get if this player os owner of this Clan;\n"
    					+ "\n");
    		}
    		if (lang.equalsIgnoreCase("PT-BR")){
    			config.getNode("_config-version").setComment(""
    					+ "Arquivo de configuração do Uchat\n"
    					+ "Autor: FabioZumbi12\n"
    					+ "Recomando usar o Notepad++ para editar este arquivo!\n"
    					+ "------------------------------------------------------------------------\n"
    					+ "\n"
    					+ "Tags é onde voce vai personalizar os textos pra aparecer no chat, ao passar o mouse ou clicar na tag.\n"
    					+ "Para adicionar uma tag, copie uma existente e troque o nome para um de sua escolha.\n"
    					+ "Depois de criar e personalizar a tag, adicione ela em 'general > default-tag-builder'.\n"
    					+ "------------------------------------------------------------------------\n"
    					+ "###### Não renomeie as tags 'playername', 'nickname' e 'message' ########\n"
    					+ "######## ou o plugin não vai dar replace nas tags corretamente! #########\n"
    					+ "------------------------------------------------------------------------\n"
    					+ "\n"
    					+ "Replacers disponíveis:\n"
    					+ " - {world}: O mundo de quem enviou a mensagem;\n"
    					+ " - {message}: Mensagem enviada;\n"
    					+ " - {playername}: O nome de quem enviou;\n"
    					+ " - {nickname}: O nick de quem enviou. Se o nick não foi definido irá mostrar o nome;\n"
    					+ " - {ch-name}: Nome do canal;\n"
    					+ " - {ch-alias}: Atalho do canal;\n"
    					+ " - {ch-color}: Cor do canal;\n"
    					+ " - {balance}: Dinheiro do player;\n"
    					+ "\n"
    					+ "Permissões:\n"
    					+ " - {option_group}: Pega o nome do grupo;\n"
    					+ " - {option_prefix}: Pega o prefix do grupo (se usado);\n"
    					+ " - {option_suffix}: Pega o suffix do grupo (se usado);\n"
    					+ " - {option_display_name}: Pega o nome customizado do grupo (se usado);\n"
    					+ " - {option_<key option>}: Pega qualquer opção customizada se estiver sendo usada nas permissões como {option_home-count} pra pegar as homes do Nucleus;\n"
    					+ "\n"
    					+ "Vanilla Chat:\n"
    					+ " - {chat_header}: Pega a header do chat;\n"
    					+ " - {chat_body}: Pega o body do chat;\n"
    					+ " - {chat_footer}: Pega o footer do chat;\n"
    					+ " - {chat_all}: Pega todos formatos padrão do chat;\n"
    					+ "\n"
    					+ "MCClans:\n"
    					+ " - {clan_name}: O nome do Clan;\n"
    					+ " - {clan_tag}: Tag do Clan;\n"
    					+ " - {clan_tag_color}: Tag do Clan com cores;\n"
    					+ " - {clan_kdr}: Clan KDR;\n"
    					+ " - {clan_player_rank}: Pega o rank do player no clan Clan;\n"
    					+ " - {clan_player_kdr}: O KDR do player;\n"
    					+ " - {clan_player_ffprotected}: Pega se o player esta protegido pelo fogo-amigo;\n"
    					+ " - {clan_player_isowner}: Pega se o player é dono do Clan;\n"
    					+ "\n");
    		}
    		
    		/*------------------------------------------------------------------------------------*/
    		
    		//----------------------------------------------------------------------------------------//
			save();        			
            UChat.get().getLogger().info("All configurations loaded!");
	}
    
	public List<String> getTagList(){
		List<String> tags = new ArrayList<String>();
		config.getChildrenMap().keySet().forEach(key -> {
			if (key.toString().startsWith("tags.") && config.getNode(key).hasMapChildren()){
				tags.add(key.toString().replace("tags.", ""));
			}
		});		
		getStringList("general.custom-tags").forEach(key -> {
			tags.add(key);
		});
		return tags;
	}
	
	public UCChannel getChannel(String alias){		
		for (List<String> aliases:channels.keySet()){
			if (aliases.contains(alias)){				
				return channels.get(aliases);
			}
		}
		return null;
	}
	
	public Collection<UCChannel> getChannels(){
		return this.channels.values();
	}
	
	public void addChannel(UCChannel ch) throws IOException{
		
		CommentedConfigurationNode chFile;	
    	ConfigurationLoader<CommentedConfigurationNode> channelManager;		
		File defch = new File(UChat.get().configDir()+File.separator+"channels"+File.separator+ch.getName()+".conf");	
		
		channelManager = HoconConfigurationLoader.builder().setFile(defch).build();	
		chFile = channelManager.load();
		
		chFile.getNode("across-worlds").setComment(""
				+ "###################################################\n"
				+ "############## Channel Configuration ##############\n"
				+ "###################################################\n"
				+ "\n"
				+ "This is the channel configuration.\n"
				+ "You can change and copy this file to create as many channels you want.\n"
				+ "This is the default options:\n"
				+ "\n"
				+ "name: Global - The name of channel.\n"
				+ "alias: g - The alias to use the channel\n"
				+ "across-worlds: true - Send messages of this channel to all worlds?\n"
				+ "distance: 0 - If across worlds is false, distance to receive this messages.\n"
				+ "color: &b - The color of channel\n"
				+ "tag-builder: ch-tags,world,clan-tag,marry-tag,group-prefix,nickname,group-suffix,message - Tags of this channel\n"
				+ "need-focus: false - Player can use the alias or need to use '/ch g' to use this channel?\n"
				+ "canLock: true - Change if the player can use /<channel> to lock on channel."
				+ "receivers-message: true - Send chat messages like if no player near to receive the message?\n"
				+ "cost: 0.0 - Cost to player use this channel.\n"
				+ "use-this-builder: false - Use this tag builder or use the 'config.yml' tag-builder?\n"
				+ "\n"
				+ "channelAlias - Use this channel as a command alias.\n"
				+ "  enable: true - Enable this execute a command alias?\n"
				+ "  sendAs: player - Send the command alias as 'player' or 'console'?\n"
				+ "  cmd: '' - Command to send on every message send by this channel.\n"
				+ "available-worlds - Worlds and only this world where this chat can be used and messages sent/received.\n");
		chFile.getNode("name").setValue(ch.getName());
		chFile.getNode("alias").setValue(ch.getAlias());
		chFile.getNode("across-worlds").setValue(ch.crossWorlds());
		chFile.getNode("distance").setValue(ch.getDistance());
		chFile.getNode("color").setValue(ch.getColor());
		chFile.getNode("use-this-builder").setValue(ch.useOwnBuilder());
		chFile.getNode("tag-builder").setValue(ch.getRawBuilder());
		chFile.getNode("need-focus").setValue(ch.neeFocus());
		chFile.getNode("canLock").setValue(ch.canLock());
		chFile.getNode("receivers-message").setValue(ch.getReceiversMsg());
		chFile.getNode("cost").setValue(ch.getCost());
		chFile.getNode("bungee").setValue(ch.isBungee());
		chFile.getNode("channelAlias","enable").setValue(ch.isCmdAlias());
		chFile.getNode("channelAlias","sendAs").setValue(ch.getAliasSender());
		chFile.getNode("channelAlias","cmd").setValue(ch.getAliasCmd());
		chFile.getNode("available-worlds").setValue(ch.availableWorlds());		
		channelManager.save(chFile);
		channels.put(Arrays.asList(ch.getName(), ch.getAlias().toLowerCase()), ch);
	}
	
	public void unMuteInAllChannels(String player){
		for (UCChannel ch:channels.values()){
			if (ch.isMuted(player)){				
				ch.unMuteThis(player);;
			}
		}
	}
	
	public void muteInAllChannels(String player){
		for (UCChannel ch:channels.values()){
			if (!ch.isMuted(player)){				
				ch.muteThis(player);;
			}
		}
	}
	
	public UCChannel getDefChannel(){
		UCChannel ch = getChannel(getString("general","default-channel"));
		if (ch == null){
			UChat.get().getLogger().warning("Defalt channel not found with alias '"+getString("general","default-channel")+"'. Fix this setting to a valid channel alias.");
		}
		return ch;
	}
	
	public String[] getDefBuilder(){
		return getString("general","default-tag-builder").replace(" ", "").split(",");
	}
	
	public List<String> getChAliases(){
		List<String> aliases = new ArrayList<String>();
		for (List<String> alias:channels.keySet()){
			aliases.addAll(alias);
		}
		return aliases;
	}
	
	public List<String> getChCmd(){
		return Arrays.asList(config.getNode("general","channel-cmd-aliases").getString().replace(" ", "").split(","));
	}
	
	public List<String> getBroadcastAliases() {
		return Arrays.asList(config.getNode("broadcast","aliases").getString().replace(" ", "").split(","));
	}
	
	public List<String> getTellAliases() {
		return Arrays.asList((config.getNode("tell","cmd-aliases").getString()+",r").replace(" ", "").split(","));
	}
	
	public List<String> getMsgAliases() {
		return Arrays.asList(config.getNode("general","umsg-cmd-aliases").getString().replace(" ", "").split(","));
	}
	
    public Boolean getBool(Object... key){		
		return config.getNode(key).getBoolean(false);
	}
    
    public void setConfig(Object value, Object... key){
    	config.getNode(key).setValue(value);
    }
    
    public String getString(Object... key){		
		return config.getNode(key).getString();
	}
    
    public Integer getInt(Object... key){		
		return config.getNode(key).getInt();
	}
    
	public String getColor(Object... key){
		return config.getNode(key).getString();
	}
	
    public List<String> getStringList(Object... key){		
		try {
			return config.getNode(key).getList(TypeToken.of(String.class), new ArrayList<String>());
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
    /*
    public ItemType getMaterial(String key){
    	return Material.getMaterial(configs.getString(key));
    }
    */
    public void save(){
    	try {
    		configManager.save(config);
    		protsManager.save(prots);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    //protection methods
	public int getProtInt(Object... key){
		return prots.getNode(key).getInt();
	}
	
	public boolean getProtBool(Object... key){
		return prots.getNode(key).getBoolean();
	}
	
	public List<String> getProtStringList(Object... key){
		try {
			return prots.getNode(key).getList(TypeToken.of(String.class), new ArrayList<String>());
		} catch (ObjectMappingException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}
	        
	public String getProtString(Object... key){
		return prots.getNode(key).getString(key.toString());
	}
	
	public Text getProtMsg(Object... key){
		return UCUtil.toText(prots.getNode(key).getString());
	}
	
	public Text getURLTemplate() {
		return UCUtil.toText(prots.getNode("general","URL-template").getString());
	}	
}
   

package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.ProtectionsCategory.Chatprotection;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class UCConfig{
	
	private File defConfig = new File(UChat.get().configDir(),"config.conf");
	private File defProt = new File(UChat.get().configDir(),"protections.conf");
	
	private CommentedConfigurationNode configRoot;
	private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
	private MainCategory root;
	public MainCategory root(){
		return this.root;
	}
	
	private CommentedConfigurationNode protsRoot;	
	private ConfigurationLoader<CommentedConfigurationNode> protLoader;
	private ProtectionsCategory protections;
	public Chatprotection protections(){
		return this.protections.chat_protection;
	}
	
	public UCConfig(GuiceObjectMapperFactory factory) throws IOException {
		UChat.get().getLogger().info("-> Config module");
		try {
			Files.createDirectories(UChat.get().configDir().toPath());
			if (!defConfig.exists()){
				UChat.get().getLogger().info("Creating config file...");
				defConfig.createNewFile();
			}
			
			/*--------------------- config.conf ---------------------------*/
			String header = ""
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
					+ " - {nick-symbol}: The symbol to use before nickname;\n"
					+ " - {ch-name}: Channel name;\n"
					+ " - {ch-alias}: Channel alias;\n"
					+ " - {ch-color}: Channel color;\n"
					+ " - {balance}: Get the sender money;\n"
					+ " - {hand-type}: Item type;\n"
					+ " - {hand-name}: Item name;\n"
					+ " - {hand-amount}: Item quantity;\n"
					+ " - {hand-lore}: Item description (lore);\n"
					+ " - {hand-durability}: Item durability;\n"
					+ " - {hand-enchants}: Item enchantments;\n"
					+ " - {time-now}: Prints the time now on server;\n"
					+ "\n"    					
					+ "Permissions Group Options:\n"
					+ " - {option_group}: Get the group name;\n"
					+ " - {option_prefix}: Get the prefix of group (if set);\n"
					+ " - {option_all_prefixes}: Get and show all prefixes the player has;\n"
					+ " - {option_suffix}: Get the suffix of group (if set);\n"
					+ " - {option_all_suffixes}:  Get and show all suffixes the player has;\n"
					+ " - {option_display_name}: Get the custom name of group (if set);\n"
					+ " - {option_<key option>}: Get some custom key option from your group in permissions like {option_home-count} to get home count from Nucleus;\n"
					+ "\n"
					+ "Permissions Player Options:\n"
					+ " - {player_option_prefix}: Get the prefix of player (if set);\n"
					+ " - {player_option_suffix}: Get the suffix of player (if set);\n"
					+ " - {player_option_<key option>}: Get some custom key option from your permissions like {player_option_viptime} or what you want;\n"
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
					+ "Jedis (Redis):\n"
					+ "- {jedis-id} - The ID of this server;\n"
					+ "\n";

			cfgLoader = HoconConfigurationLoader.builder().setFile(defConfig).build();	
			configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(header));			
			root = configRoot.getValue(TypeToken.of(MainCategory.class), new MainCategory());

			for (World w:Sponge.getServer().getWorlds()){
				if (!root.general.default_channels.worlds.containsKey(w.getName())){
					root.general.default_channels.worlds.put(w.getName(), new MainCategory.WorldInfo(root.general.default_channels.default_channel, false));
				}
			}

			//update old configs
			double update = 0;
			if (configRoot.getNode("_config-version").getDouble() < 1.2D){
				configRoot.getNode("_config-version").setValue(1.2D);

				configRoot.getNode("debug-messages").setValue(null);

				update = 1.2D;
			}

			if (update > 0){
				UChat.get().getLogger().warning("Configuration updated to "+update);
			}

			/*--------------------- protections.conf ---------------------------*/
			protLoader = HoconConfigurationLoader.builder().setFile(defProt).build();	
			protsRoot = protLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true));
			protections = protsRoot.getValue(TypeToken.of(ProtectionsCategory.class), new ProtectionsCategory());

		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		}
		
		/* Load Channels */
	    loadChannels();
	    
		//----------------------------------------------------------------------------------------//
		save();        			
        UChat.get().getLogger().info("All configurations loaded!");
	}
    
	private void loadChannels() throws IOException {
		File chfolder = new File(UChat.get().configDir(),"channels");
        
	    if (!chfolder.exists()) {
        	chfolder.mkdir();
            UChat.get().getLogger().info("Created folder: " +chfolder.getPath());
        }    	            
	            
	    //--------------------------------------- Load Aliases -----------------------------------//
        
	    if (UChat.get().getChannels() == null){
			UChat.get().setChannels(new HashMap<>());
		}
	    
        File[] listOfFiles = chfolder.listFiles();
                    
        CommentedConfigurationNode channel;	
    	ConfigurationLoader<CommentedConfigurationNode> channelManager;
        
    	if (listOfFiles.length == 0){
    		//create default channels
        	File g = new File(chfolder, "global.conf"); 
        	channelManager = HoconConfigurationLoader.builder().setFile(g).build();	
			channel = channelManager.load();
			channel.getNode("name").setValue("Global");
			channel.getNode("alias").setValue("g");
			channel.getNode("color").setValue("&2");
			channel.getNode("jedis").setValue(true);
			channelManager.save(channel);
        	
        	File l = new File(chfolder, "local.conf");
        	channelManager = HoconConfigurationLoader.builder().setFile(l).build();	
			channel = channelManager.load();
			channel.getNode("name").setValue("Local");
			channel.getNode("alias").setValue("l");
			channel.getNode("across-worlds").setValue(false);
			channel.getNode("distance").setValue(40);
			channel.getNode("color").setValue("&e");
			channelManager.save(channel);
        	
        	File ad = new File(chfolder, "admin.conf");
        	channelManager = HoconConfigurationLoader.builder().setFile(ad).build();	
			channel = channelManager.load();
			channel.getNode("name").setValue("Admin");
			channel.getNode("alias").setValue("ad");
			channel.getNode("color").setValue("&b");
			channel.getNode("jedis").setValue(true);
			channelManager.save(channel);
			
        	listOfFiles = chfolder.listFiles();
        }
    	
		for (File file:listOfFiles){
			if (file.getName().endsWith(".conf")){
				channelManager = HoconConfigurationLoader.builder().setFile(file).build();	
				channel = channelManager.load();
				    				
				Map<String, Object> chProps = new HashMap<>();
				channel.getChildrenMap().forEach((key,value)->{
					if (value.hasMapChildren()){
						String rkey = key.toString();
						for (Entry<Object, ? extends CommentedConfigurationNode> vl:value.getChildrenMap().entrySet()){													
							chProps.put(rkey+"."+vl.getKey(), vl.getValue().getValue());
						}											
					} else {
						chProps.put(key.toString(), value.getValue());
					}
				});
				
				UCChannel ch = new UCChannel(chProps);
				addChannel(ch);
			}
		}
	}

	public void delChannel(UCChannel ch){
		UChat.get().getCmds().unregisterCmd(ch.getAlias());
		UChat.get().getCmds().unregisterCmd(ch.getName());
		for (Entry<List<String>, UCChannel> ch0:UChat.get().getChannels().entrySet()){
			if (ch0.getValue().equals(ch)){
				UChat.get().getChannels().remove(ch0.getKey());
				break;
			}
		}
		File defch = new File(UChat.get().configDir(),"channels"+File.separator+ch.getName().toLowerCase()+".conf");	
		if (defch.exists()){
			defch.delete();
		}
	}
	
	public void addChannel(UCChannel ch) throws IOException{
		CommentedConfigurationNode chFile;	
    	ConfigurationLoader<CommentedConfigurationNode> channelManager;		
		File defch = new File(UChat.get().configDir(),"channels" + File.separator + ch.getName().toLowerCase() + ".conf");
		
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
				+ "canLock: true - Change if the player can use /<channel> to lock on channel.\n"
				+ "receivers-message: true - Send chat messages like if no player near to receive the message?\n"
				+ "cost: 0.0 - Cost to player use this channel.\n"
				+ "use-this-builder: false - Use this tag builder or use the 'config.yml' tag-builder?\n"
				+ "\n"
				+ "channelAlias - Use this channel as a command alias.\n"
				+ "  enable: true - Enable this execute a command alias?\n"
				+ "  sendAs: player - Send the command alias as 'player' or 'console'?\n"
				+ "  cmd: '' - Command to send on every message send by this channel.\n"
				+ "available-worlds - Worlds and only this world where this chat can be used and messages sent/received.\n"
				+ "discord:\n"
				+ "  mode: NONE - The options are NONE, SEND, LISTEN, BOTH. If enabled and token code set and the channel ID matches with one discord channel, will react according the choosen mode.\n"
				+ "  hover: &3Discord Channel: &a{dd-channel}\n"
				+ "  format-to-mc: {ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: \n"
				+ "  format-to-dd: :thought_balloon: **{sender}**: {message} \n"
				+ "  allow-server-cmds: false - Use this channel to send commands from discord > minecraft.\n"
				+ "  channelID: '' - The IDs of your Discord Channels. Enable debug on your discord to get the channel ID.\n"
				+ "  Note: You can add more than one discord id, just separate by \",\" like: 13246579865498,3216587898754\n");

		ch.getProperties().forEach((key,value)-> chFile.getNode((Object[])key.toString().split("\\.")).setValue(value));
		channelManager.save(chFile);
		
		if (UChat.get().getChannel(ch.getName()) != null){
			ch.setMembers(UChat.get().getChannel(ch.getName()).getMembers());
			UChat.get().getChannels().remove(Arrays.asList(ch.getName().toLowerCase(), ch.getAlias().toLowerCase()));
		}		
		UChat.get().getChannels().put(Arrays.asList(ch.getName().toLowerCase(), ch.getAlias().toLowerCase()), ch);
	}
	
	public String[] getDefBuilder(){
		return root.general.default_tag_builder.replace(" ", "").split(",");
	}
	
	public List<String> getChCmd(){
		return Arrays.asList(root.general.channel_cmd_aliases.replace(" ", "").split(","));
	}
	
	public List<String> getBroadcastAliases() {
		return Arrays.asList(root.broadcast.aliases.replace(" ", "").split(","));
	}
	
	public List<String> getTellAliases() {
		return Arrays.asList((root.tell.cmd_aliases+",r").replace(" ", "").split(","));
	}
	
	public List<String> getMsgAliases() {
		return Arrays.asList(root.general.umsg_cmd_aliases.replace(" ", "").split(","));
	}
	
    private void save(){
    	try {
			configRoot.setValue(TypeToken.of(MainCategory.class), root);
    		cfgLoader.save(configRoot);

			protsRoot.setValue(TypeToken.of(ProtectionsCategory.class), protections);
    		protLoader.save(protsRoot);
		} catch (IOException | ObjectMappingException e) {
			e.printStackTrace();
		}
	}
}
   

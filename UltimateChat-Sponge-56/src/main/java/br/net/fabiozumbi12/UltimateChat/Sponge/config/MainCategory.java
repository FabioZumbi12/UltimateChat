package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {
	
	public MainCategory(){
		defaultTags();
	}
		
	@Setting(value="_config-version")
	public Double config_version = 1.0;	
	@Setting()
	public DebugCat debug = new DebugCat();
	
	//debug
	@ConfigSerializable
	public static class DebugCat{
		
		@Setting()
		public boolean messages = false;
		@Setting()
		public boolean timings = false;
	}

	@Setting(comment="Available languages: EN-US, PT-BR, FR-FR, FR-ES, HU-HU, RU, SP-ES, ZH-CN and KO-KR")
	public String language = "EN-US";

	// jedis
	@Setting(comment="Jedis configuration.\nUse Jedis to send messages between other servers running Jedis.\nConsider a replecement as Bungeecoord.")
	public JedisCat jedis = new JedisCat();
	
	@ConfigSerializable
	public static class JedisCat{
		
		@Setting
		public boolean enable = false;
		@Setting(value="server-id")
		public String server_id = "&e-ChangeThis-&r ";
		@Setting
		public String ip = "localhost";
		@Setting
		public int port = 6379;
		@Setting
		public String pass = "";
	}
	
	//discord
	@Setting(comment="Enable the two way chat into discord and minecraft.\nGenerate your bot token following this instructions: https://goo.gl/utfRRv")
	public DiscordCat discord = new DiscordCat();
	@ConfigSerializable
	public static class DiscordCat{
		
		@Setting
		public boolean use = false;
		@Setting(value="update-status")
		public boolean update_status = true;
		@Setting(value="game-type", comment = "The default status of bot. Available status: DEFAULT, LISTENING, WATCHING and STREAMING")
		public String game_type = "DEFAULT";
        @Setting(value="twitch", comment = "If game-type = STREAMING, set the twitch url.")
        public String twitch = "";
		@Setting
		public String token = "";
		@Setting(value = "vanish-perm", comment = "Set your vanish plugin permissions here to do not announce player join/leave players with this permission.")
		public String vanish_perm = "nucleus.vanish.onlogin";
		@Setting(value="log-channel-id", comment="Channel id to send server start/stop and player join/leave messages")
		public String log_channel_id = "";
		@Setting(value="tell-channel-id", comment="Channel id to spy private messages")
		public String tell_channel_id = "";
		@Setting(value="commands-channel-id", comment="Channel id to send commands issued by players")
		public String commands_channel_id = "";
		@Setting(value="server-commands", comment="Put the id on 'commands-channel-id' option or/and enable server commands on channel configuration to use this.")
		public ServerCmds server_commands = new ServerCmds();
		@ConfigSerializable
		public static class ServerCmds{
			
			@Setting(comment="This alias is not needed if using the channel set on 'commands-channel-id' option.")
			public String alias = "!cmd";
			@Setting
			public List<String> withelist = new ArrayList<>();
			@Setting
			public List<String> blacklist = Arrays.asList("stop","whitelist");
		}						
	}
	
	//mention
	@Setting(comment="Use mentions on chat to change the player name color and play a sound on mention.")
	public MentionCat mention = new MentionCat();
	@ConfigSerializable
	public static class MentionCat{
		
		@Setting
		public boolean enable = true;
		@Setting(value="color-template")
		public String color_template = "&e@{mentioned-player}&r";
		@Setting
		public String playsound = "minecraft:block.note.pling";
		@Setting(value="hover-message")
		public String hover_message = "&e{playername} mentioned you!";
	}
	
	//api
	@Setting(comment="API configurations.")
	public ApiCat api = new ApiCat();
	@ConfigSerializable
	public static class ApiCat{
		
		@Setting(value="format-console-messages")
		public boolean format_console_messages = false;
		@Setting(value="sponge-api", comment = "Change this if using SpongeVanilla or some issues to UChat detect the API versions.\n" +
				"- Available: 5, 6, 7, 8")
		public int sponge_api = 7;
	}
	
	//general
	@Setting(comment="General settings.")
	public GeneralCat general = new GeneralCat();
	
	@ConfigSerializable
	public static class GeneralCat{

		@Setting(value="URL-template", comment="Template to show when players send links or urls.")
		public String URL_template = "&3Click to open &n{url}&r";
		@Setting(value="console-tag", comment="Tag to show when sent messages from console to channels.")
		public String console_tag = "&6 {console}&3";
		@Setting(value="custom-tags")
		public List<String> custom_tags = new ArrayList<>();
		@Setting(value="remove-from-chat", comment="Remove this from chat (like empty tags)")
		public List<String> remove_from_chat = Arrays.asList("[]","&7[]","&7[&7]");
		@Setting(value="channel-cmd-aliases", comment="Command and aliases for /channel command.")
		public String channel_cmd_aliases = "channel, ch";
		@Setting(value="umsg-cmd-aliases", comment="Aliases to send commands from system to players (without any format, good to send messages from other plugins direct to players).")
		public String umsg_cmd_aliases = "umsg";
		@Setting(value="default-channel", comment="Set the default channel for new players or when players join on server.")
		public String default_channel = "l";
		@Setting(value="spy-format", comment="Chat spy format.")
		public String spy_format = "&c[Spy] {output}";
		@Setting(value="spy-enabled-onjoin", comment="Enable spy on join?")
		public boolean spy_enabled_onjoin = true;
		@Setting(value="enable-tags-on-messages", comment="Enable to allow parse tags and placeholders on messages.")
		public boolean enable_tags_on_messages = false;
		@Setting(value="nick-symbol")
		public String nick_symbol = "&6~&f";
		@Setting(value="persist-channels")
		public boolean persist_channels = true;
		@Setting(value="item-hand")
		public ItemHandCat item_hand = new ItemHandCat();
		@Setting(value = "world-names", comment = "Example alias for rename world name to other name. Support color codes.")
		public Map<String, String> world_names = createMapWorlds();
        private HashMap<String, String> createMapWorlds(){
            HashMap<String,String> myMap = new HashMap<>();
            myMap.put("my-end", "&5The-End&r");
            myMap.put("my-nether", "&4Hell&r");
            return myMap;
        }
		@Setting(value="check-channel-change-world", comment = "This will make a check if the player channel is available on destination world and put on the world channel if is not available.")
		public boolean check_channel_change_world = false;

		@ConfigSerializable
		public static class ItemHandCat{
			
			@Setting
			public boolean enable = true;
			@Setting(comment="Text to show on chat on hover the tag.")
			public String format = "&6[{hand-amount} {hand-type}]{group-suffix}";
			@Setting(comment="Placeholder to use on chat by players to show your item in hand.")
			public String placeholder = "@hand";
		}
		
		@Setting(value="default-tag-builder", comment="This is the main tag builder.\n"
					+ "Change the order of this tags to change how tag is displayed on chat.\n"
					+ "This tags represent the names of tag in this configuration.")
		public String default_tag_builder = "world,ch-tags,prefix,nickname,suffix,message";
	}
	
	//tell
	@Setting
	public TellCat tell = new TellCat();
	
	@ConfigSerializable
	public static class TellCat{
		
		@Setting(comment="Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
		public boolean enable = true;
		@Setting(value="cmd-aliases", comment="Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
		public String cmd_aliases = "tell,t,w,m,msg,private,priv";
		@Setting(comment="Prefix of tell messages.")
		public String prefix = "&6[&c{playername} &6-> &c{receivername}&6]: ";
		@Setting(comment="Suffix (or message) of tell.")
		public String format = "{message}";
		@Setting(value="hover-messages", comment="Hover messages to show on tell messages.")
		public List<String> hover_messages = new ArrayList<>();
	}
	
	@Setting
	public BroadcastCat broadcast = new BroadcastCat();
	
	@ConfigSerializable
	public static class BroadcastCat{
		
		@Setting(comment="Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
		public boolean enable = true;
		@Setting(value="on-hover", comment="Tag to use on broadcast message to set a hover message.")
		public String on_hover = "hover:";
		@Setting(value="on-click", comment="Tag to use on broadcast message to set a click event.")
		public String on_click = "click:";
		@Setting(comment="Tag to use on broadcast message to set a website url on click.")
		public String url = "url:";
		@Setting(comment="Aliases to use for broadcast.")
		public String aliases = "broadcast,broad,ubroad,announce,say,action,all,anunciar,todos";
	}
	
	@Setting(comment="Enable hook with other plugins here. Only enable if installed.")
	public HooksCat hooks = new HooksCat();
	
	@ConfigSerializable
	public static class HooksCat{
		
		@Setting
		public McclansCat MCClans = new McclansCat();
		
		@ConfigSerializable
		public static class McclansCat{
			
			@Setting(comment="Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
			public boolean enable = false;
		}
	}
	
	@Setting(comment="This is where you will create as many tags you want.\n"
					+ "You can use the tag \"custom-tag\" as base to create your own tags.\n"
					+ "When finish, get the name of your tag and put on \"general.default-tag-build\" \n"
					+ "or on channel builder on \"channels\" folder.")	
	public Map<String, TagsCategory> tags = new HashMap<>();
	
	private void defaultTags(){		
		tags.put("prefix", new TagsCategory("{option_prefix}", null, Collections.singletonList("&3Rank: &f{option_display_name}"), null, null, null, null));
		tags.put("nickname", new TagsCategory("{nick-symbol}{nickname}", null, Arrays.asList("&3Player: &f{playername}","&3Money: &7{balance}"), null, null, null, null));
		tags.put("playername", new TagsCategory("{playername}", null, Arrays.asList("&3Player: &f{playername}","&3Money: &7{balance}"), null, null, null, null));		
		tags.put("suffix", new TagsCategory("{option_suffix}", null, null, null, null, null, null));		
		tags.put("world", new TagsCategory("&7[{world}&7]&r", null, Collections.singletonList("&7Sent from world &8{world}"), null, null, null, null));
		tags.put("message", new TagsCategory("{message}", null, null, null, null, null, null));		
		tags.put("ch-tags", new TagsCategory("{ch-color}[{ch-alias}]&r", "ch {ch-alias}", Arrays.asList("&3Channel name: {ch-color}{ch-name}","&bClick to join this channel"), null, null, null, null));		
		tags.put("admin-chat", new TagsCategory("&b[&r{playername}&b]&r: &b", null, null, null, null, null, null));
		tags.put("custom-tag", new TagsCategory("&7[&2MyTag&7]", "say I created an awesome tag!", Collections.singletonList("You discovered me :P"), "any-name-perm.custom-tag", Collections.singletonList("world-show"), Collections.singletonList("world-hide"), "www.google.com"));
		tags.put("vanilla-chat", new TagsCategory("{chat_header}{chat_body}", null, null, null, null, null, null));	
		tags.put("jedis", new TagsCategory("{server-id}", null, Arrays.asList("&7Server: {jedis-id}","&cChange me on configuration!"), null, null, null, null));
	}		
}

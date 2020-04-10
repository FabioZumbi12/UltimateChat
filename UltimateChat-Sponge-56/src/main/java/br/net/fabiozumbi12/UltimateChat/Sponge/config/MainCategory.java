/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class MainCategory {

    @Setting(value = "_config-version")
    public Double config_version = 1.0;
    @Setting()
    public DebugCat debug = new DebugCat();
    @Setting(comment = "Available languages: EN-US, PT-BR, FR-FR, FR-ES, HU-HU, RU, SP-ES, ZH-CN and KO-KR")
    public String language = "EN-US";
    // jedis
    @Setting(comment = "Jedis configuration.\nUse Jedis to send messages between other servers running Jedis.\nConsider a replecement as Bungeecoord.")
    public JedisCat jedis = new JedisCat();
    //discord
    @Setting(comment = "Enable the two way chat into discord and minecraft.\nGenerate your bot token following this instructions: https://goo.gl/utfRRv")
    public DiscordCat discord = new DiscordCat();
    //mention
    @Setting(comment = "Use mentions on chat to change the player name color and play a sound on mention.")
    public MentionCat mention = new MentionCat();
    //api
    @Setting(comment = "API configurations.")
    public ApiCat api = new ApiCat();
    //general
    @Setting(comment = "General settings.")
    public GeneralCat general = new GeneralCat();
    //tell
    @Setting
    public TellCat tell = new TellCat();
    @Setting
    public BroadcastCat broadcast = new BroadcastCat();
    @Setting(comment = "Enable hook with other plugins here. Only enable if installed.")
    public HooksCat hooks = new HooksCat();
    @Setting(comment = "This is where you will create as many tags you want.\n"
            + "You can use the tag \"custom-tag\" as base to create your own tags.\n"
            + "When finish, get the name of your tag and put on \"general.default-tag-build\" \n"
            + "or on channel builder on \"channels\" folder.")
    public Map<String, TagsCategory> tags = defaultTags();
    //BungeeCoord
    @Setting()
    public BungeeCat bungee = new BungeeCat();

    public MainCategory() {
    }

    private Map<String, TagsCategory> defaultTags() {
        Map<String, TagsCategory> myMap = new HashMap<>();
        myMap.put("prefix", new TagsCategory("{option_prefix}", null, Collections.singletonList("&3Rank: &f{option_display_name}"), null, null, null, null));
        myMap.put("nickname", new TagsCategory("{nick-symbol}{nickname}", null, Arrays.asList("&3Player: &f{playername}", "&3Money: &7{balance}"), null, null, null, null));
        myMap.put("playername", new TagsCategory("{playername}", null, Arrays.asList("&3Player: &f{playername}", "&3Money: &7{balance}"), null, null, null, null));
        myMap.put("suffix", new TagsCategory("{option_suffix}", null, null, null, null, null, null));
        myMap.put("world", new TagsCategory("&7[{world}&7]&r", null, Collections.singletonList("&7Sent from world &8{world}"), null, null, null, null));
        myMap.put("message", new TagsCategory("{message}", null, null, null, null, null, null));
        myMap.put("ch-tags", new TagsCategory("{ch-color}[{ch-alias}]&r", "ch {ch-alias}", Arrays.asList("&3Channel name: {ch-color}{ch-name}", "&bClick to join this channel"), null, null, null, null));
        myMap.put("admin-chat", new TagsCategory("&b[&r{playername}&b]&r: &b", null, null, null, null, null, null));
        myMap.put("custom-tag", new TagsCategory("&7[&2MyTag&7]", "say I created an awesome tag!", Collections.singletonList("You discovered me :P"), "any-name-perm.custom-tag", Collections.singletonList("world-show"), Collections.singletonList("world-hide"), "www.google.com"));
        myMap.put("vanilla-chat", new TagsCategory("{chat_header}{chat_body}", null, null, null, null, null, null));
        myMap.put("jedis", new TagsCategory("{server-id}", null, Arrays.asList("&7Server: {jedis-id}", "&cChange me on configuration!"), null, null, null, null));
        return myMap;
    }

    @ConfigSerializable
    public static class BungeeCat {

        @Setting()
        public String server_id = "Sponge";
        @Setting()
        public boolean enable = false;
    }

    //debug
    @ConfigSerializable
    public static class DebugCat {

        @Setting()
        public boolean messages = false;
        @Setting()
        public boolean timings = false;
    }

    @ConfigSerializable
    public static class JedisCat {

        @Setting
        public boolean enable = false;
        @Setting(value = "server-id")
        public String server_id = "&e-ChangeThis-&r ";
        @Setting
        public String ip = "localhost";
        @Setting
        public int port = 6379;
        @Setting
        public String pass = "";
    }

    @ConfigSerializable
    public static class DiscordCat {

        @Setting
        public boolean use = false;
        @Setting(value = "update-status")
        public boolean update_status = true;
        @Setting(value = "game-type", comment = "The default status of bot. Available status: DEFAULT, LISTENING, WATCHING and STREAMING")
        public String game_type = "DEFAULT";
        @Setting(value = "twitch", comment = "If game-type = STREAMING, set the twitch url.")
        public String twitch = "";
        @Setting
        public String token = "";
        @Setting(value = "vanish-perm", comment = "Set your vanish plugin permissions here to do not announce player join/leave players with this permission.")
        public String vanish_perm = "nucleus.vanish.onlogin";
        @Setting(value = "log-channel-id", comment = "Channel id to send server start/stop and player join/leave messages")
        public String log_channel_id = "";
        @Setting(value = "tell-channel-id", comment = "Channel id to spy private messages")
        public String tell_channel_id = "";
        @Setting(value = "commands-channel-id", comment = "Channel id to send commands issued by players")
        public String commands_channel_id = "";

        @Setting(comment = "Pixelmon announces")
        public Pixelmon pixelmon = new Pixelmon();
        @Setting(value = "server-commands", comment = "Put the id on 'commands-channel-id' option or/and enable server commands on channel configuration to use this.")
        public ServerCmds server_commands = new ServerCmds();

        @ConfigSerializable
        public static class Pixelmon {

            @Setting(value = "legendary-text", comment = "Text to show on Legendary Spawn")
            public String legendary_text = ":loudspeaker: A Legendary **%s** has spawned on biome **%s** in world **%s**";
            @Setting(value = "legendary-channel-id", comment = "Announce to a discord channel when a Legendary Spawns")
            public String legendary_channel_id = "";
        }

        @ConfigSerializable
        public static class ServerCmds {

            @Setting(comment = "This alias is not needed if using the channel set on 'commands-channel-id' option.")
            public String alias = "!cmd";
            @Setting
            public List<String> withelist = new ArrayList<>();
            @Setting
            public List<String> blacklist = Arrays.asList("stop", "whitelist");
        }
    }

    @ConfigSerializable
    public static class MentionCat {

        @Setting
        public boolean enable = true;
        @Setting(value = "color-template")
        public String color_template = "&e@{mentioned-player}&r";
        @Setting
        public String playsound = "minecraft:block.note.pling";
        @Setting(value = "hover-message")
        public String hover_message = "&e{playername} mentioned you!";
    }

    @ConfigSerializable
    public static class ApiCat {

        @Setting(value = "format-console-messages")
        public boolean format_console_messages = false;
        @Setting(value = "sponge-api", comment = "Change this if using SpongeVanilla or some issues to UChat detect the API versions.\n" +
                "- Available: 5, 6, 7, 8")
        public int sponge_api = 7;
    }

    @ConfigSerializable
    public static class GeneralCat {

        @Setting(value = "fakeplayer-channel", comment = "The default channel for fakeplayers from mods and other plugins.")
        public String fakeplayer_channel = "l";
        @Setting(value = "URL-template", comment = "Template to show when players send links or urls.")
        public String URL_template = "&3Click to open &n{url}&r";
        @Setting(value = "console-tag", comment = "Tag to show when sent messages from console to channels.")
        public String console_tag = "&6 {console}&3";
        @Setting(value = "custom-tags")
        public List<String> custom_tags = new ArrayList<>();
        @Setting(value = "dont-show-groups", comment = "If using the placeholder \"{option_all_prefixes/suffixes}\", exclude this groups from tags.")
        public List<String> dont_show_groups = Collections.singletonList("default");
        @Setting(value = "remove-from-chat", comment = "Remove this from chat (like empty tags)")
        public List<String> remove_from_chat = Arrays.asList("[]", "&7[]", "&7[&7]");
        @Setting(value = "channel-cmd-aliases", comment = "Command and aliases for /channel command.")
        public String channel_cmd_aliases = "channel, ch";
        @Setting(value = "umsg-cmd-aliases", comment = "Aliases to send commands from system to players (without any format, good to send messages from other plugins direct to players).")
        public String umsg_cmd_aliases = "umsg";
        @Setting(value = "spy-format", comment = "Chat spy format.")
        public String spy_format = "&c[Spy] {output}";
        @Setting(value = "spy-enabled-onjoin", comment = "Enable spy on join?")
        public boolean spy_enabled_onjoin = true;
        @Setting(value = "enable-tags-on-messages", comment = "Enable to allow parse tags and placeholders on messages.")
        public boolean enable_tags_on_messages = false;
        @Setting(value = "nick-symbol")
        public String nick_symbol = "&6~&f";
        @Setting(value = "persist-channels")
        public boolean persist_channels = true;
        @Setting(value = "item-hand")
        public ItemHandCat item_hand = new ItemHandCat();
        @Setting(value = "world-names", comment = "Example alias for rename world name to other name. Support color codes.")
        public Map<String, String> world_names = createMapWorlds();
        @Setting(value = "group-names", comment = "Example alias for rename group name to other name. Support color codes.")
        public Map<String, String> group_names = createMapGroup();
        @Setting(value = "default-channels")
        public DefaultChannels default_channels = new DefaultChannels();
        @Setting(value = "check-channel-change-world", comment = "This will make a check if the player channel is available on destination world and put on the world channel if is not available.")
        public boolean check_channel_change_world = false;
        @Setting(value = "default-tag-builder", comment = "This is the main tag builder.\n"
                + "Change the order of this tags to change how tag is displayed on chat.\n"
                + "This tags represent the names of tag in this configuration.")
        public String default_tag_builder = "world,ch-tags,prefix,nickname,suffix,message";

        private HashMap<String, String> createMapWorlds() {
            HashMap<String, String> myMap = new HashMap<>();
            myMap.put("my-end", "&5The-End&r");
            myMap.put("my-nether", "&4Hell&r");
            return myMap;
        }

        private Map<String, String> createMapGroup() {
            Map<String, String> myMap = Maps.newHashMap();
            myMap.put("my-admin", "&4Admin&r");
            myMap.put("my-moderator", "&2MOD&r");
            return myMap;
        }

        @ConfigSerializable
        public static class DefaultChannels {

            @Setting(value = "default-channel", comment = "Default channel for new added worlds")
            public String default_channel = "l";

            @Setting
            public Map<String, WorldInfo> worlds = new HashMap<>();
        }

        @ConfigSerializable
        public static class ItemHandCat {

            @Setting
            public boolean enable = true;
            @Setting(comment = "Text to show on chat on hover the tag.")
            public String format = "&6[{hand-amount} {hand-type}]{group-suffix}";
            @Setting(comment = "Placeholder to use on chat by players to show your item in hand.\n" +
                    "Placeholders: {hand-amount}, {hand-type}, {hand-name}")
            public String placeholder = "@hand";
        }
    }

    @ConfigSerializable
    public static class TellCat {

        @Setting(comment = "Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
        public boolean enable = true;
        @Setting(value = "cmd-aliases", comment = "Enabling tell will unregister other plugins using tell like nucleus, and will use only this tell.")
        public String cmd_aliases = "tell,t,w,m,msg,private,priv";
        @Setting(comment = "Prefix of tell messages.")
        public String prefix = "&6[&c{playername} &6-> &c{receivername}&6]: ";
        @Setting(comment = "Suffix (or message) of tell.")
        public String format = "{message}";
        @Setting(value = "hover-messages", comment = "Hover messages to show on tell messages.")
        public List<String> hover_messages = new ArrayList<>();
    }

    @ConfigSerializable
    public static class BroadcastCat {

        @Setting(comment = "Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
        public boolean enable = true;
        @Setting(value = "on-hover", comment = "Tag to use on broadcast message to set a hover message.")
        public String on_hover = "hover:";
        @Setting(value = "on-click", comment = "Tag to use on broadcast message to set a click event.")
        public String on_click = "click:";
        @Setting(comment = "Tag to use on broadcast message to set a website url on click.")
        public String url = "url:";
        @Setting(comment = "Aliases to use for broadcast.")
        public String aliases = "broadcast,broad,ubroad,announce,say,action,all,anunciar,todos";
    }

    @ConfigSerializable
    public static class HooksCat {

        @Setting
        public McclansCat MCClans = new McclansCat();

        @ConfigSerializable
        public static class McclansCat {

            @Setting(comment = "Enable broadcast. Enabling this will unregister any other broadcasts commands using the same aliases.")
            public boolean enable = false;
        }
    }

    @ConfigSerializable
    public static class WorldInfo {
        @Setting
        public String channel = "l";
        @Setting
        public boolean force = false;

        public WorldInfo() {
        }

        public WorldInfo(String ch, boolean force) {
            this.channel = ch;
            this.force = force;
        }
    }
}

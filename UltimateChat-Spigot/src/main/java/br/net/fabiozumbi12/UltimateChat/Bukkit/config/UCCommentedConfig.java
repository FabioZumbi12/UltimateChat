package br.net.fabiozumbi12.UltimateChat.Bukkit.config;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UCCommentedConfig {
    private HashMap<String, String> comments;

    UCCommentedConfig(){
        this.comments = new HashMap<>();
    }

    public void addDefaults() {
        File config = new File(UChat.get().getDataFolder(), "config.yml");
        if (config.exists()) {
            try {
                UChat.get().getConfig().load(config);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        setDefault("config-version", 1.6, "Dont touch <3");

        setDefault("debug.messages", false, null);
        setDefault("debug.timings", false, null);

        setDefault("language", "EN-US", "Available languages: EN-US, PT-BR, FR, HU-HU, RU and ZH-CN");

        setDefault("jedis", null, "Jedis configuration.\nUse Jedis to send messages between other servers running Jedis.\nConsider a replecement as Bungeecoord.");
        setDefault("jedis.enable", false, null);
        setDefault("jedis.server-id", "&e[ChangeThis]&r ", "Change to a unique identification and use on tags with {jedis-id}.");
        setDefault("jedis.ip", "localhost", null);
        setDefault("jedis.port", "6379", null);
        setDefault("jedis.pass", "", null);
        setDefault("jedis.response-symbol", "&7« ", null);

        setDefault("discord", null, "Enable the two way chat into discord and minecraft.\nGenerate your bot token following this instructions: https://goo.gl/utfRRv");
        setDefault("discord.use", false, null);
        setDefault("discord.update-status", true, null);
        setDefault("discord.token", "", null);
        setDefault("discord.log-channel-id", "", null);
        setDefault("discord.tell-channel-id", "", "Channel id to spy private messages");
        setDefault("discord.commands-channel-id", "", "Channel id to send commands issued by players.");
        setDefault("discord.server-commands", null, "Put the id on 'commands-channel-id' option or/and enable server commands on channel configuration to use this.");
        setDefault("discord.server-commands.alias", "!cmd", "This alias is not needed if using the channel set on 'commands-channel-id' option.");
        setDefault("discord.server-commands.whitelist", new ArrayList<String>(), null);
        setDefault("discord.server-commands.blacklist", Arrays.asList("stop", "whitelist"), null);

        setDefault("api", null, "UChat Api configs.");
        setDefault("api.format-console-messages", false, "Using uchat api, format messages sent to console?");
        setDefault("api.legendchat-tags", new ArrayList<String>(), "If using tag plugins from legendchat, put the tags here.");

        setDefault("mention", null, "Use mentions on chat to change the player name color and play a sound on mention.");
        setDefault("mention.enable", true, null);
        setDefault("mention.color-template", "&e@{mentioned-player}{group-suffix}", null);
        setDefault("mention.playsound", "note_pling", "May change if using old minecraft version.");
        setDefault("mention.hover-message", "&e{playername} mentioned you!", null);

        setDefault("general.URL-template", "Click to open &n{url}&r", "Template to show when players send links or urls.");
        setDefault("general.console-tag", "&6 {console}&3", "Tag to show when sent messages from console to channels.");
        setDefault("general.remove-from-chat", Arrays.asList("[]","&7[]","&7[&7]"), "Remove this from chat (like empty tags)");
        setDefault("general.remove-unnused-placeholderapi", true, "Remove not converted PlaceholdersAPI from tags.");
        setDefault("general.channel-cmd-aliases", "channel, ch", null);
        setDefault("general.umsg-cmd-aliases", "umsg, pv", "Aliases to send commands from system to players (without any format, good to send messages from other plugins direct to players)");
        setDefault("general.json-events", true, "False if your server don't support json or if /tellraw is not available.");
        setDefault("general.default-channel", "l", "Set the default channel for new players or when players join on server.");
        setDefault("general.spy-format", "&c&o[Spy] {output}", "Chat spy format.");
        setDefault("general.use-channel-tag-builder", true, "Use the tag builder from channel configuration and ignore this tag builder.");
        setDefault("general.default-tag-builder", "world,marry-tag,ch-tags,clan-tag,factions,group-prefix,nickname,group-suffix,message", "" +
                "This is the main tag builder.\n" +
                "Change the order of this tags to change how tag is displayed on chat.\n" +
                "This tags represent the names of tag in this configuration.");
        setDefault("general.enable-tags-on-messages", false, "Enable to allow parse tags and placeholders on messages.");
        setDefault("general.persist-channels", true, "Remember the channel the player is when logout/login until server restart?");
        setDefault("general.item-hand.enable", true, null);
        setDefault("general.item-hand.format", "&6[{hand-amount} {hand-type}]{group-suffix}", "Text to show on chat.");
        setDefault("general.item-hand.placeholder", "@hand", "Placeholder to use on chat by players to show your item in hand.");

        setDefault("tell.cmd-aliases", "tell,t,w,m,msg,private,priv,r", null);
        setDefault("tell.prefix", "&6[&c{playername} &6-> &c{receivername}&6]: ", null);
        setDefault("tell.format", "{message}", null);
        setDefault("tell.hover-messages", new ArrayList<>(), null);

        setDefault("bungee.server-id", "&4ChangeMe", "Change to a unique identification, and use on tags with {bungee-id}.");

        setDefault("broadcast.enable", true, "Enable custom broadcasts.");
        setDefault("broadcast.on-hover", "hover:", "Tag to use on broadcast message to set a hover message.");
        setDefault("broadcast.on-click", "click:", "Tag to use on broadcast message to set a click event.");
        setDefault("broadcast.suggest", "suggest:", "Tag to use on broadcast message to suggest a command.");
        setDefault("broadcast.url", "url:", "Tag to use on broadcast message to set a website url on click (with www).");
        setDefault("broadcast.aliases", "ubroad,uannounce,usay,uaction,all,anunciar,todos", "Aliases to use for broadcast.");

        setDefault("tags", null, "" +
                "This is where you will create as many tags you want.\n" +
                "You can use the tag \"custom-tag\" as base to create your own tags.\n" +
                "When finish, get the name of your tag and put on \"general.default-tag-build\" \n" +
                "or on channel builder on \"channels\" folder.");
        setDefault("tags.group-prefix.format", "&7[{group-prefix}&7]&r", null);
        setDefault("tags.group-prefix.hover-messages", Arrays.asList("&bRank: &e{prim-group}"), null);

        setDefault("tags.playername.format", "{playername}", null);
        setDefault("tags.playername.click-cmd", "tpa {playername}", null);
        setDefault("tags.playername.hover-messages", Arrays.asList("&7Click to send teleport request"), null);

        setDefault("tags.nickname.format", "{nickname}", null);
        setDefault("tags.nickname.hover-messages", Arrays.asList("&6Realname: {playername}"), null);

        setDefault("tags.group-suffix.format", "&r{group-suffix}: ", null);

        setDefault("tags.world.format", "&7[{world}]&r", null);
        setDefault("tags.world.hover-messages", "&7[{world}]&r", null);

        setDefault("tags.message.format", "{message}", null);

        setDefault("tags.ch-tags.format", "{ch-color}[{ch-alias}]&r", null);
        setDefault("tags.ch-tags.click-cmd", "ch {ch-alias}", null);
        setDefault("tags.ch-tags.hover-messages", Arrays.asList("&3Channel name: {ch-color}{ch-name}","&bClick to go to this channel!"), null);

        setDefault("tags.clan-tag.format", "{clan-tag}", null);
        setDefault("tags.clan-tag.click-cmd", "clan search {playername}", null);
        setDefault("tags.clan-tag.hover-messages", Arrays.asList("" +
                        "&bClan Tag: &7{clan-tag}",
                "&bClan Name: &7{clan-name}",
                "&bClan KDR: &7{clan-totalkdr}",
                "&bPlayer KDR: &7{clan-kdr}",
                "&bPlayer Rank: &7{clan-rank}",
                "&bIs Leader: &7{clan-isleader}",
                "&3Click for more info about this player"), null);

        setDefault("tags.marry-tag.format", "{marry-prefix}{marry-suffix}", null);
        setDefault("tags.marry-tag.hover-messages", Arrays.asList("&cMarried with {marry-partner}"), null);

        setDefault("tags.admin-chat.format", "&b[&r{playername}&b] ", null);

        setDefault("tags.bungee.format", "&7[{world}]{ch-color}[Bungee-{bungee-id}] {playername}: &7", null);
        setDefault("tags.bungee.hover-messages", Arrays.asList("{ch-color}Sent from server -{bungee-id}-"), null);

        setDefault("tags.factions.format", "&7[{fac-relation-color}{fac-relation-name}&7]&r", null);
        setDefault("tags.factions.hover-messages", Arrays.asList("&7Faction name: {fac-relation-color}{fac-name}","&7Motd: &a{fac-motd}","&7Description: {fac-description}"), null);

        setDefault("tags.jedis.format", "{jedis-id}", null);
        setDefault("tags.jedis.hover-messages", Arrays.asList("&7Server: {jedis-id}","&cChange me on configuration!"), null);

        setDefault("tags.custom-tag", null, "Use this tag as reference to create other new tags.");
        setDefault("tags.custom-tag.format", "&7[&2MyTag&7]&r", null);
        setDefault("tags.custom-tag.click-cmd", "", null);
        setDefault("tags.custom-tag.click-url", "", null);
        setDefault("tags.custom-tag.suggest-cmd", "", null);
        setDefault("tags.custom-tag.permission", "any-name-perm.custom-tag", null);
        setDefault("tags.custom-tag.hover-messages", new ArrayList<>(), null);
        setDefault("tags.custom-tag.show-in-worlds", new ArrayList<>(), null);
        setDefault("tags.custom-tag.hide-in-worlds", new ArrayList<>(), null);
    }

    private void setDefault(String key, Object def, String comment){
        if (def != null){
            UChat.get().getConfig().set(key, UChat.get().getConfig().get(key, def));
        }
        if (comment != null){
            setComment(key, comment);
        }
    }

    private void setComment(String key, String comment){
        comments.put(key, comment);
    }

    public void saveConfig() {
        StringBuilder b = new StringBuilder();
        UChat.get().getConfig().options().header(null);

        String lang = UChat.get().getConfig().getString("language");
        if (lang.equalsIgnoreCase("EN-US")){
            b.append(""
                    + "# Uchat configuration file\n"
                    + "# Author: FabioZumbi12\n"
                    + "# We recommend you to use NotePad++ to edit this file and avoid TAB errors!\n"
                    + "# ------------------------------------------------------------------------\n"
                    + "# Tags is where you can customize what will show on chat, on hover or on click on tag.\n"
                    + "# To add a tag, you can copy an existent and change the name and the texts.\n"
                    + "# After add and customize your tag, put the tag name on 'general > default-tag-builder'.\n"
                    + "# ------------------------------------------------------------------------\n"
                    + "\n"
                    + "# Available replacers:\n"
                    + "\n"
                    + "# uChat:\n"
                    + "#  - {default-format-full}: Use this tag to see all plugin tags using the default bukkit format. "
                    + "# Normally used by 'myth' plugins and temporary tags."
                    + "# If you want to use only of this tags you can use the replacer bellow and get number of tag separated by spaces;\n"
                    + "#  - {default-format-0}: use this tag to show only one of the tags described on '{default-format-full}'. "
                    + "# The number is the posiotion separated by spaces;\n"
                    + "#  - {world}: Replaced by sender world;\n"
                    + "#  - {message}: Message sent by player;\n"
                    + "#  - {playername}: The name of player;\n"
                    + "#  - {nickname}: The nickname of player. If not set, will show realname;\n"
                    + "#  - {ch-name}: Channel name;\n"
                    + "#  - {ch-alias}: Channel alias;\n"
                    + "#  - {ch-color}: Channel color;\n"
                    + "#  - {hand-type}: Item type;\n"
                    + "#  - {hand-name}: Item name;\n"
                    + "#  - {hand-amount}: Item quantity;\n"
                    + "#  - {hand-lore}: Item description (lore);\n"
                    + "#  - {hand-durability}: Item durability;\n"
                    + "#  - {hand-enchants}: Item enchantments;\n"
                    + "\n"
                    + "# Vault:\n"
                    + "#  - {group-prefix}: Get group prefix;\n"
                    + "#  - {group-suffix}: Get group suffix;\n"
                    + "#  - {balance}: Get the sender money;\n"
                    + "#  - {prim-group}: Get the primary group tag;\n"
                    + "#  - {player-groups}: Get all groups names the sender has;\n"
                    + "#  - {player-groups-prefixes}: Get all group prefixes the sender has;\n"
                    + "#  - {player-groups-suffixes}: Get all group suffixes the sender has;\n"
                    + "\n"
                    + "# Simpleclans:\n"
                    + "#  - {clan-tag}: Clan tag without colors;\n"
                    + "#  - {clan-fulltag}: Clan tag with brackets, colors and separator;\n"
                    + "#  - {clan-ctag}: Clan with colors;\n"
                    + "#  - {clan-name}: Clan name;\n"
                    + "#  - {clan-kdr}: Player clan KDR;\n"
                    + "#  - {clan-isleader}: The player is leader;\n"
                    + "#  - {clan-rank}: Player rank on Clan;\n"
                    + "#  - {clan-totalkdr}: Clan KDR (not player kdr);\n"
                    + "\n"
                    + "# Marry Plugins:\n"
                    + "#  - {marry-partner}: Get the partner name;\n"
                    + "#  - {marry-prefix}: Get the married prefix tag, normally the heart;\n"
                    + "#  - {marry-suffix}: Get the married suffix tag, or male tag for Marriage Reloaded;\n"
                    + "\n"
                    + "# BungeeCord:\n"
                    + "# - {bungee-id}: Server ID from sender;\n"
                    + "\n"
                    + "# Jedis (redis):\n"
                    + "# - {jedis-id}: This server id;\n"
                    + "\n"
                    + "# Factions:\n"
                    + "# Gets the info of faction to show on chat.\n"
                    + "# - {fac-id}: Faction ID;\n"
                    + "# - {fac-name}: Faction Name;\n"
                    + "# - {fac-motd}: Faction MOTD;\n"
                    + "# - {fac-description}: Faction Description;\n"
                    + "# - {fac-relation-name}: Faction name in relation of reader of tag;\n"
                    + "# - {fac-relation-color}: Faction color in relation of reader of tag;\n"
                    + "\n");
        }
        if (lang.equalsIgnoreCase("PT-BR")){
            b.append(""
                    + "# Arquivo de configuração do Uchat\n"
                    + "# Autor: FabioZumbi12\n"
                    + "# Recomando usar o Notepad++ para editar este arquivo!\n"
                    + "# ------------------------------------------------------------------------\n"
                    + "# Tags é onde voce vai personalizar os textos pra aparecer no chat, ao passar o mouse ou clicar na tag.\n"
                    + "# Para adicionar uma tag, copie uma existente e troque o nome para um de sua escolha.\n"
                    + "# Depois de criar e personalizar a tag, adicione ela em 'general > default-tag-builder'.\n"
                    + "# ------------------------------------------------------------------------\n"
                    + "\n"
                    + "# Replacers disponíveis:\n"
                    + "\n"
                    + "# uChat:\n"
                    + "#  - {default-format-full}: Use esta tag para ver todas tags de plugins que estão usando o formato padrão do bukkit. "
                    + "# Normalmente usado por plugins de 'mito' e tags temporárias. "
                    + "# Caso queira apenas usar uma delas elas são separadas por espaços e abaixo vc pode usar apenas uma de cada.\n"
                    + "#  - {default-format-0}: Use esta tag para usar apenas uma tag das descritas acima. O numero é a posição dela entre os espaços;\n"
                    + "#  - {world}: O mundo de quem enviou a mensagem;\n"
                    + "#  - {message}: Mensagem enviada;\n"
                    + "#  - {playername}: O nome de quem enviou;\n"
                    + "#  - {nickname}: O nick de quem enviou. Se o nick não foi definido irá mostrar o nome;\n"
                    + "#  - {ch-name}: Nome do canal;\n"
                    + "#  - {ch-alias}: Atalho do canal;\n"
                    + "#  - {ch-color}: Cor do canal;\n"
                    + "#  - {hand-type}: Tipo do item;\n"
                    + "#  - {hand-name}: Nome do item;\n"
                    + "#  - {hand-amount}: Quantidade do item;\n"
                    + "#  - {hand-lore}: Descrição do item(lore);\n"
                    + "#  - {hand-durability}: Durabilidade do item;\n"
                    + "#  - {hand-enchants}: Encantamentos do item;\n"
                    + "\n"
                    + "# Vault:\n"
                    + "#  - {group-prefix}: Prefixo do grupo do player;\n"
                    + "#  - {group-suffix}: Sufixo do grupo do player;\n"
                    + "#  - {balance}: Dinheiro do player;\n"
                    + "#  - {prim-group}: Tag do grupo primário;\n"
                    + "#  - {player-groups}: Lista todos grupos que o player faz parte;\n"
                    + "#  - {player-groups-prefixes}: Lista todo prefixes dos grupos que o player esta;\n"
                    + "#  - {player-groups-suffixes}: Lista todo suffixes dos grupos que o player esta;\n"
                    + "\n"
                    + "# Simpleclans:\n"
                    + "#  - {clan-tag}: Tag do Clan sem cores;\n"
                    + "#  - {clan-fulltag}: Tag do clan com os colchetes, cores e separador;\n"
                    + "#  - {clan-ctag}: Tag do Clan com cores;\n"
                    + "#  - {clan-name}: Nome do Clan;\n"
                    + "#  - {clan-kdr}: KDR do player do Clan;\n"
                    + "#  - {clan-isleader}: O player é lider;\n"
                    + "#  - {clan-rank}: Rank do player no Clan;\n"
                    + "#  - {clan-totalkdr}: Clan KDR (não do player);\n"
                    + "\n"
                    + "# Marry Plugins:\n"
                    + "#  - {marry-partner}: Mostra o nome do(a) parceiro(a);\n"
                    + "#  - {marry-prefix}: Pega a tag de prefixo, normalmente o coração;\n"
                    + "#  - {marry-suffix}: Pega a tag de sufixo, ou simbolo masculino do Marriage Reloaded;\n"
                    + "\n"
                    + "# BungeeCord:\n"
                    + "# - {bungee-id}: O ID do Server de quem enviou;\n"
                    + "\n"
                    + "# Jedis (redis):\n"
                    + "# - {jedis-id}: O ID deste server;\n"
                    + "\n"
                    + "# Factions:\n"
                    + "# Pega as informações das Factions pra mostrar no chat.\n"
                    + "# - {fac-id}: Faction ID;\n"
                    + "# - {fac-name}: Nome da Faction;\n"
                    + "# - {fac-motd}: MOTD da faction;\n"
                    + "# - {fac-description}: Descrição da Faction;\n"
                    + "# - {fac-relation-name}: Nome da Faction em relação á quem ta lendo a tag;\n"
                    + "# - {fac-relation-color}: Cor da Faction em relação á quem ta lendo a tag;\n"
                    + "\n");
        }

        for (String line:UChat.get().getConfig().getKeys(true)){
            String[] key = line.split("\\"+UChat.get().getConfig().options().pathSeparator());
            String spaces = "";
            for (int i = 0; i < key.length; i++){
                if (i == 0) continue;
                spaces = spaces+" ";
            }
            if (comments.containsKey(line)){
                if (spaces.isEmpty()){
                    b.append("\n# "+comments.get(line).replace("\n", "\n# ")).append('\n');
                } else {
                    b.append(spaces+"# "+comments.get(line).replace("\n", "\n"+spaces+"# ")).append('\n');
                }
            }
            Object value = UChat.get().getConfig().get(line);
            if (!UChat.get().getConfig().isConfigurationSection(line)){
                if (value instanceof String){
                    b.append(spaces+key[key.length-1]+": '"+value+"'\n");
                } else if (value instanceof List<?>) {
                    if (((List<?>)value).isEmpty()){
                        b.append(spaces+key[key.length-1]+": []\n");
                    } else {
                        b.append(spaces+key[key.length-1]+":\n");
                        for (Object lineCfg:(List<?>)value){
                            if (lineCfg instanceof String){
                                b.append(spaces+"- '"+lineCfg+"'\n");
                            } else {
                                b.append(spaces+"- "+lineCfg+"\n");
                            }
                        }
                    }
                } else {
                    b.append(spaces+key[key.length-1]+": "+value+"\n");
                }
            } else {
                b.append(spaces+key[key.length-1]+":\n");
            }
        }

        try {
            Files.write(b, new File(UChat.get().getDataFolder(), "config.yml"), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

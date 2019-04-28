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

package br.net.fabiozumbi12.UltimateChat.Bukkit;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.Map.Entry;

/**
 * Represents a chat channel use by UltimateChat to control from where/to send/receive messages.
 *
 * @author FabioZumbi12
 */
public class UCChannel {
    private final List<String> ignoring = new ArrayList<>();
    private final List<String> mutes = new ArrayList<>();
    private final Properties properties = new Properties();
    private List<String> members = new ArrayList<>();

    @Deprecated()
    public UCChannel(String name, String alias, boolean worlds, int dist, String color, String builder, boolean focus, boolean receiversMsg, double cost, boolean isbungee, boolean ownBuilder, boolean isAlias, String aliasSender, String aliasCmd, List<String> availableWorlds, String ddchannel, String ddmode, String ddmcformat, String mcddformat, String ddhover, boolean ddallowcmds, boolean lock) {
        addDefaults();
        properties.put("name", name);
        properties.put("alias", alias);
        properties.put("color", color);
        properties.put("across-worlds", worlds);
        properties.put("distance", dist);
        properties.put("use-this-builder", ownBuilder);
        properties.put("tag-builder", builder);
        properties.put("need-focus", focus);
        properties.put("canLock", lock);
        properties.put("receivers-message", receiversMsg);
        properties.put("cost", cost);
        properties.put("bungee", isbungee);
        properties.put("channelAlias.enable", isAlias);
        properties.put("channelAlias.sendAs", aliasSender);
        properties.put("channelAlias.cmd", aliasCmd);
        properties.put("available-worlds", availableWorlds);
        properties.put("discord.channelID", ddchannel);
        properties.put("discord.mode", ddmode);
        properties.put("discord.hover", ddhover);
        properties.put("discord.allow-server-cmds", ddallowcmds);
        properties.put("discord.format-to-mc", ddmcformat);
        properties.put("discord.format-to-dd", mcddformat);
    }

    public UCChannel(String name, String alias, String color) {
        addDefaults();
        properties.put("name", name);
        properties.put("alias", alias);
        properties.put("color", color);
    }

    public UCChannel(String name) {
        addDefaults();
        properties.put("name", name);
        properties.put("alias", name.substring(0, 1).toLowerCase());
    }

    public UCChannel(Map<String, Object> props) {
        addDefaults();
        properties.keySet().stream().filter(props::containsKey).forEach((nkey) -> properties.put(nkey, props.get(nkey)));
    }

    private void addDefaults() {
        properties.put("name", "");
        properties.put("alias", "");
        properties.put("char-alias", "");
        properties.put("color", "&b");
        properties.put("across-worlds", true);
        properties.put("distance", 0);
        properties.put("use-this-builder", false);
        properties.put("tag-builder", "world,marry-tag,ch-tags,clan-tag,factions,group-prefix,nickname,group-suffix,message");
        properties.put("need-focus", false);
        properties.put("canLock", true);
        properties.put("receivers-message", true);
        properties.put("cost", 0.0);
        properties.put("bungee", false);
        properties.put("jedis", false);
        properties.put("password", "");
        properties.put("channelAlias.enable", false);
        properties.put("channelAlias.sendAs", "player");
        properties.put("channelAlias.cmd", "");
        properties.put("available-worlds", new ArrayList<String>());
        properties.put("discord.channelID", "");
        properties.put("discord.mode", "none");
        properties.put("discord.allow-bot", true);
        properties.put("discord.hover", "&3Discord Channel: &a{dd-channel}\n&3Role Name: {dd-rolecolor}{dd-rolename}");
        properties.put("discord.allow-server-cmds", false);
        properties.put("discord.format-to-mc", "{ch-color}[{ch-alias}]&b{dd-rolecolor}[{dd-rolename}]{sender}&r: ");
        properties.put("discord.format-to-dd", ":regional_indicator_g: **{sender}**: {message}");
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperty(String key, String value) {
        if (properties.get(key) instanceof List) {
            properties.put(key, Arrays.asList(value.split(",")));
        } else if (properties.get(key) instanceof Boolean) {
            properties.put(key, Boolean.getBoolean(value));
        } else if (properties.get(key) instanceof Integer) {
            try {
                properties.put(key, Integer.parseInt(value));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        } else if (properties.get(key) instanceof String[]) {
            properties.put(key, value.split(","));
        } else {
            properties.put(key, value);
        }
    }

    public String getCharAlias() {
        return this.properties.get("char-alias").toString();
    }

    public boolean AllowBot() {
        return (boolean) properties.get("allow-bot");
    }

    public String getPassword() {
        return (String) properties.get("password");
    }

    public void setPassword(String pass) {
        properties.put("password", pass);
    }

    public boolean useJedis() {
        return (boolean) properties.get("jedis");
    }

    public void setJedis(boolean use) {
        properties.put("jedis", use);
    }

    public boolean getDiscordAllowCmds() {
        return (boolean) properties.get("discord.allow-server-cmds");
    }

    public boolean isTell() {
        return properties.get("name").toString().equals("tell");
    }

    public List<String> getDiscordChannelID() {
        return Arrays.asList(properties.get("discord.channelID").toString().split(","));
    }

    public String getDiscordMode() {
        return properties.get("discord.mode").toString();
    }

    public boolean matchDiscordID(String id) {
        return getDiscordChannelID().contains(id);
    }

    public boolean isSendingDiscord() {
        return !getDiscordChannelID().isEmpty() && (getDiscordMode().equalsIgnoreCase("both") || getDiscordMode().equalsIgnoreCase("send"));
    }

    public boolean isListenDiscord() {
        return !getDiscordChannelID().isEmpty() && (getDiscordMode().equalsIgnoreCase("both") || getDiscordMode().equalsIgnoreCase("listen"));
    }

    public String getDiscordHover() {
        return properties.get("discord.hover").toString();
    }

    public String getDiscordtoMCFormat() {
        return properties.get("discord.format-to-mc").toString();
    }

    public String getMCtoDiscordFormat() {
        return properties.get("discord.format-to-dd").toString();
    }

    public List<String> getMembers() {
        return this.members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void clearMembers() {
        this.members.clear();
    }

    public UCChannel addMember(CommandSender p) {
        addMember(p.getName());
        return this;
    }

    public UCChannel addMember(String p) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            ch.removeMember(p);
        }
        this.members.add(p);
        return this;
    }

    public UCChannel removeMember(CommandSender p) {
        removeMember(p.getName());
        return this;
    }

    public UCChannel removeMember(String p) {
        this.members.remove(p);
        return this;
    }


    public boolean isMember(CommandSender p) {
        return this.members.contains(p.getName());
    }

    public boolean canLock() {
        return (boolean) properties.get("canLock");
    }

    @SuppressWarnings("unchecked")
    public boolean availableInWorld(World w) {
        List<String> channels = ((List<String>) properties.get("available-worlds"));
        return channels.isEmpty() || ((List<String>) properties.get("available-worlds")).contains(w.getName());
    }

    @SuppressWarnings("unchecked")
    public List<String> availableWorlds() {
        return ((List<String>) properties.get("available-worlds"));
    }

    public String getAliasCmd() {
        return properties.get("channelAlias.cmd").toString();
    }

    public String getAliasSender() {
        return properties.get("channelAlias.sendAs").toString();
    }

    public boolean isCmdAlias() {
        return (boolean) properties.get("channelAlias.enable");
    }

    public boolean useOwnBuilder() {
        return (boolean) properties.get("use-this-builder");
    }

    public double getCost() {
        return Double.parseDouble(properties.get("cost").toString());
    }

    public void setCost(double cost) {
        properties.put("cost", cost);
    }

    public boolean getReceiversMsg() {
        return (boolean) properties.get("receivers-message");
    }

    public void setReceiversMsg(boolean show) {
        properties.put("receivers-message", show);
    }

    public void muteThis(String player) {
        if (!this.mutes.contains(player)) {
            this.mutes.add(player);
        }
    }

    public void unMuteThis(String player) {
        this.mutes.remove(player);
    }

    public boolean isMuted(String player) {
        return this.mutes.contains(player);
    }

    public void ignoreThis(String player) {
        if (!this.ignoring.contains(player)) {
            this.ignoring.add(player);
        }
    }

    public void unIgnoreThis(String player) {
        this.ignoring.remove(player);
    }

    public boolean isIgnoring(String player) {
        return this.ignoring.contains(player);
    }

    public String[] getBuilder() {
        return properties.get("tag-builder").toString().replace(" ", "").split(",");
    }

    public String getRawBuilder() {
        return properties.get("tag-builder").toString();
    }

    public boolean crossWorlds() {
        return (boolean) properties.get("across-worlds");
    }

    public int getDistance() {
        return (int) properties.get("distance");
    }

    public String getColor() {
        return properties.get("color").toString();
    }

    public String getName() {
        return properties.get("name").toString();
    }

    public String getAlias() {
        return properties.get("alias").toString();
    }

    public boolean neeFocus() {
        return (boolean) properties.get("need-focus");
    }

    public boolean matchChannel(String aliasOrName) {
        return properties.get("alias").toString().equalsIgnoreCase(aliasOrName) || properties.get("name").toString().equalsIgnoreCase(aliasOrName);
    }

    public boolean isBungee() {
        return (boolean) properties.get("bungee");
    }

    /**
     * Send a message from a channel as player.
     *
     * @param sender  {@code Player}
     * @param message {@code String} - Message to send.
     */
    @Deprecated
    public void sendMessage(Player sender, String message) {
        sendMessage(sender, new UltimateFancy(message), false);
    }

    /**
     * Send a message from a channel as player.
     *
     * @param sender  {@code Player}
     * @param message {@code FancyMessage} - Message to send.
     * @param direct  {@code boolean} - Send message direct to players on channel.
     */
    public void sendMessage(Player sender, UltimateFancy message, boolean direct) {
        if (direct) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                UCChannel chp = UChat.get().getPlayerChannel(p);
                if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (!this.neeFocus() || chp.equals(this))) {
                    UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
                    message.send(p);
                }
            }
            message.send(sender);
        } else {
            Set<Player> pls = new HashSet<>(Bukkit.getOnlinePlayers());
            UChat.get().tempChannels.put(sender.getName(), this.getAlias());
            AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender, message.toOldFormat(), pls);
            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire AsyncPlayerChatEvent");
                UChat.get().getServer().getPluginManager().callEvent(event);
            });
        }
    }

    /**
     * Send a message from a channel as console.
     *
     * @param sender  {@code ConsoleCommandSender} - Console sender.
     * @param message {@code FancyMessage} - Message to send.
     * @param direct  {@code boolean} - Send message direct to players on channel.
     */
    public void sendMessage(ConsoleCommandSender sender, UltimateFancy message, boolean direct) {
        if (direct) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                UCChannel chp = UChat.get().getPlayerChannel(p);
                if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (!this.neeFocus() || chp.equals(this))) {
                    UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Direct Message");
                    message.send(p);
                }
            }
            message.send(sender);
        } else {
            UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire MessageChannelEvent");
            Bukkit.getScheduler().runTask(UChat.get(), () -> UCMessages.sendFancyMessage(new String[0], message.toOldFormat(), this, sender, null));
        }
    }

    /**
     * Send a message from a channel as console.
     *
     * @param sender  {@code ConsoleCommandSender} - Console sender.
     * @param message {@code FancyMessage} - Message to send.
     */
    @Deprecated
    public void sendMessage(ConsoleCommandSender sender, String message) {
        if (UChat.get().getUCConfig().getBoolean("api.format-console-messages")) {
            Bukkit.getScheduler().runTask(UChat.get(), () -> UCMessages.sendFancyMessage(new String[0], message, this, sender, null));
        } else {
            UltimateFancy fmsg = new UltimateFancy(message);
            for (Player p : Bukkit.getOnlinePlayers()) {
                UCChannel chp = UChat.get().getPlayerChannel(p);
                if (UCPerms.channelReadPerm(p, this) && !this.isIgnoring(p.getName()) && (!this.neeFocus() || chp.equals(this))) {
                    UChat.get().getUCLogger().timings(timingType.START, "UCChannel#sendMessage()|Fire AsyncPlayerChatEvent");
                    fmsg.send(p);
                }
            }
            fmsg.send(sender);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        JSONArray array = new JSONArray();
        for (Entry<Object, Object> prop : properties.entrySet()) {
            JSONObject json = new JSONObject();
            json.put(prop.getKey(), prop.getValue());
            array.add(json);
        }
        return array.toJSONString();
    }
}

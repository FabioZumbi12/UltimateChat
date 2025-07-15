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

package br.net.fabiozumbi12.UltimateChat.Bukkit.discord;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCPerms;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.util.UChatColor;
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import jdalib.jda.api.JDA;
import jdalib.jda.api.JDABuilder;
import jdalib.jda.api.entities.*;
import jdalib.jda.api.entities.channel.ChannelType;
import jdalib.jda.api.entities.channel.concrete.TextChannel;
import jdalib.jda.api.events.message.MessageReceivedEvent;
import jdalib.jda.api.exceptions.ErrorResponseException;
import jdalib.jda.api.exceptions.PermissionException;
import jdalib.jda.api.exceptions.RateLimitedException;
import jdalib.jda.api.hooks.ListenerAdapter;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static br.net.fabiozumbi12.UltimateChat.Bukkit.util.UCUtil.fromRGB;

public class UCDiscord extends ListenerAdapter implements UCDInterface {
    private JDA jda;
    private final UChat uchat;
    private int taskId;

    public UCDiscord(UChat plugin) {
        this.uchat = plugin;
        try {
            jda = JDABuilder.createDefault(this.uchat.getUCConfig().getString("discord.token").trim())
                    .addEventListeners(this)
                    .build().awaitReady();

            if (plugin.getUCConfig().getBoolean("discord.update-status")) {
                String game = plugin.getLang().get("discord.game").replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
                Activity activity = Activity.playing(game);
                jda.getPresence().setActivity(activity);
            }
        } catch (IllegalArgumentException | InterruptedException e) {
            e.printStackTrace();
            return;
        }

        if (this.uchat.getUCConfig().getBoolean("discord.update-status")) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () ->
                    updateGame(this.uchat.getLang().get("discord.game").replace("{online}",
                            String.valueOf(this.uchat.getServer().getOnlinePlayers().stream().filter(p->!p.hasPermission("uchat.discord.hide")).count()))), 40, 40);
        }
    }

    public void shutdown() {
        this.uchat.getUCLogger().info("Shutdown JDA...");
        this.jda.getRegisteredListeners().forEach(l -> this.jda.removeEventListener(l));
        this.jda.shutdown();
        this.uchat.getUCLogger().info("JDA disabled!");
    }

    public boolean JDAAvailable() {
        return this.jda != null;
    }

    public int getTaskId() {
        return this.taskId;
    }

    public JDA getJda() {
        return this.jda;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId())) return;

        String message = e.getMessage().getContentRaw();
        String[] args = message.split(" ");

        //listen bot privates for dd sync
        if (e.getMessage().isFromType(ChannelType.PRIVATE)) {

            Guild gc = e.getJDA().getGuildById(this.uchat.getDDSync().getGuidId());
            try {
                if (gc.retrieveMemberById(e.getAuthor().getId()).complete(true).getRoles().stream().anyMatch(r -> this.uchat.getDDSync().isDDAdminRole(r.getId()))){
                    //listen for ;;help
                    if (args.length == 1 && args[0].equalsIgnoreCase(this.uchat.getDDSync().getDDHelpCommand())){
                        try {
                            e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-admin-help")).complete(true);
                        } catch (RateLimitedException e1) {
                            e1.printStackTrace();
                        }
                        return;
                    }


                    if (args[0].equalsIgnoreCase(this.uchat.getDDSync().getDDAdminCommand())){

                        //listen ;;admin list
                        if (args.length == 2 && args[1].equalsIgnoreCase("list")){
                            try {
                                e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-admin-list") + "\n" + this.uchat.getDDSync().getConnectedPlayers()).complete(true);
                            } catch (RateLimitedException e1) {
                                e1.printStackTrace();
                            }
                        }

                        //listen ;;admin remove <player>|<user id>
                        if (args.length == 3 && args[1].equalsIgnoreCase("remove")){
                            if (this.uchat.getDDSync().getSyncNickName(args[2]) != null || this.uchat.getDDSync().getPlayerDDId(args[2]) != null){
                                String nickName = this.uchat.getDDSync().getSyncNickName(args[2]);
                                if (nickName == null)
                                    nickName = this.uchat.getDDSync().getSyncNickName(this.uchat.getDDSync().getPlayerDDId(args[2]));
                                this.uchat.getDDSync().unlink(nickName);
                                try {
                                    e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-admin-removed").replace("{player}", nickName)).complete(true);
                                } catch (RateLimitedException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                try {
                                    e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-admin-notremoved").replace("{player}", args[2])).complete(true);
                                } catch (RateLimitedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            return;
                        }

                        //listen ;;admin add player id
                        if (args.length == 4 && args[1].equalsIgnoreCase("add")){
                            if (this.uchat.getDDSync().getSyncNickName(args[3]) == null && this.uchat.getDDSync().getPlayerDDId(args[2]) == null){
                                try {
                                    this.uchat.getDDSync().setPlayerDDId(args[3], args[2], null);
                                    e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-admin-added")
                                            .replace("{player}", args[2])
                                            .replace("{id}", args[3]))
                                            .complete(true);
                                } catch (RateLimitedException e1) {
                                    e1.printStackTrace();
                                }
                            } else {
                                try {
                                    e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-admin-notadded")
                                            .replace("{player}", args[2]))
                                            .complete(true);
                                } catch (RateLimitedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                            return;
                        }
                        return;
                    }
                } else if (args[0].equalsIgnoreCase(this.uchat.getDDSync().getDDHelpCommand()) || args[0].equalsIgnoreCase(this.uchat.getDDSync().getDDAdminCommand())){
                    try {
                        e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-noperm")).complete(true);
                    } catch (RateLimitedException e1) {
                        e1.printStackTrace();
                    }
                    return;
                }
            } catch (RateLimitedException rateLimitedException) {
                rateLimitedException.printStackTrace();
            }

            //listen ;;connect code
            if (args[0].equalsIgnoreCase(this.uchat.getDDSync().getDDCommand())){
                if (args.length == 2){
                    String code = args[1];
                    try {
                        if (this.uchat.getDDSync().getPendentCode(code) == null) {
                            e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-nopendent").replace("{code}", code)).complete(true);
                            return;
                        }

                        if (this.uchat.getDDSync().getSyncNickName(e.getAuthor().getId()) != null) {
                            e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-already")).complete(true);
                            return;
                        }

                        String player = this.uchat.getDDSync().getPendentCode(code);
                        this.uchat.getDDSync().setPlayerDDId(e.getAuthor().getId(), player, code);
                        e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-connectok")).complete(true);
                    } catch (RateLimitedException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    try {
                        e.getChannel().sendMessage(this.uchat.getLang().get("discord.sync-usage").replace("{command}","`" + this.uchat.getDDSync().getDDCommand() + " <code>`")).complete(true);
                    } catch (RateLimitedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return;
        }

        if (e.getMember() == null || e.getMember().getUser().isBot()) return;

        // Check clan channel
        String channelId = e.getChannel().getId();
        Optional<Map.Entry<String, Object>> clanCfg = UChat.get().getDDSync().getConfig().getConfigurationSection("simple-clans-sync.clans").getValues(true)
                .entrySet().stream().filter(entry -> entry.getValue().equals(channelId)).findFirst();
        if (clanCfg.isPresent()) {
            Clan clan = UChat.get().getHooks().getSc().getClanManager().getClan(clanCfg.get().getKey().split("\\.")[0]);
            if (clan != null) {

                String leaderColor = UChat.get().getHooks().getSc().getSettingsManager().getString(SettingsManager.ConfigField.ALLYCHAT_LEADER_COLOR);
                String memberColor = UChat.get().getHooks().getSc().getSettingsManager().getString(SettingsManager.ConfigField.CLANCHAT_MEMBER_COLOR);
                String memberRank = UChat.get().getHooks().getSc().getSettingsManager().getString(SettingsManager.ConfigField.CLANCHAT_RANK);

                final String[] chatTemplate = {UChat.get().getHooks().getSc().getSettingsManager().getString(SettingsManager.ConfigField.CLANCHAT_FORMAT)
                        .replace("%clan%", clan.getTag().toUpperCase())};

                String nickColor = memberColor;

                String sender = UChat.get().getDDSync().getSyncNickName(e.getAuthor().getId());

                Optional<ClanPlayer> optionalClanPlayer = clan.getMembers().stream().filter(cp -> cp.getName().equalsIgnoreCase(sender)).findFirst();
                if (sender != null && optionalClanPlayer.isPresent()) {
                    ClanPlayer member = optionalClanPlayer.get();
                    if (member.isLeader()) {
                        nickColor = leaderColor;
                    }
                    chatTemplate[0] = chatTemplate[0]
                            .replace("%player%", member.getName())
                            .replace("%nick-color%", nickColor)
                            .replace("%message%", message)
                            .replace("%rank%", member.getRankDisplayName().isEmpty() ? "" : memberRank.replace("%rank%", member.getRankDisplayName()));

                    clan.getMembers().forEach(members -> {
                        if (members.toPlayer() != null) {
                            UltimateFancy fancy = formatDDMessage(e, ChatColor.translateAlternateColorCodes('&', chatTemplate[0]), null);
                            fancy.send(members.toPlayer());
                        }
                    });
                    UltimateFancy consoleFancy = formatDDMessage(e, ChatColor.AQUA + "Discord -> " + ChatColor.DARK_GRAY + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', chatTemplate[0])), null);
                    consoleFancy.send(Bukkit.getConsoleSender());
                } else {
                    // If not member or sender is not sync
                    String senderString = ChatColor.AQUA + "Discord -> ";
                    if (sender != null) {
                        senderString += sender;
                    } else {
                        senderString += e.getAuthor().getName();
                    }
                    String finalMessage1 = message;
                    String finalSenderString = senderString;
                    clan.getMembers().forEach(members -> {
                        if (members.toPlayer() != null) {
                            UltimateFancy fancy = formatDDMessage(e, ChatColor.translateAlternateColorCodes('&', finalSenderString + ": " + finalMessage1), null);
                            fancy.send(members.toPlayer());
                        }
                    });
                    UltimateFancy consoleFancy = formatDDMessage(e, finalSenderString + ": " + ChatColor.DARK_GRAY + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)), null);
                    consoleFancy.send(Bukkit.getConsoleSender());
                }
            }
            return;
        }

        int used = 0;

        for (UCChannel ch : this.uchat.getChannels().values()) {
            if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())) {
                if (e.getMember().getUser().isBot() && !ch.AllowBot()) {
                    continue;
                }
                //check if is cmd
                if (message.startsWith(this.uchat.getUCConfig().getString("discord.server-commands.alias")) && ch.getDiscordAllowCmds()) {
                    message = message.replace(this.uchat.getUCConfig().getString("discord.server-commands.alias") + " ", "");
                    if (!this.uchat.getUCConfig().getStringList("discord.server-commands.whitelist").isEmpty()) {
                        int count = 0;
                        for (String cmd : this.uchat.getUCConfig().getStringList("discord.server-commands.whitelist")) {
                            if (message.startsWith(cmd)) count++;
                        }
                        if (count == 0) continue;
                    }
                    if (!this.uchat.getUCConfig().getStringList("discord.server-commands.blacklist").isEmpty()) {
                        int count = 0;
                        for (String cmd : this.uchat.getUCConfig().getStringList("discord.server-commands.blacklist")) {
                            if (message.startsWith(cmd)) count++;
                        }
                        if (count > 0) continue;
                    }
                    UCUtil.performCommand(Bukkit.getServer().getConsoleSender(), message);
                } else {
                    UltimateFancy fancy = formatDDMessage(e, message, ch);
                    ch.sendMessage(uchat.getServer().getConsoleSender(), fancy, true);
                }
                used++;
            }
        }

        if (used == 0 && e.getChannel().getId().equals(this.uchat.getUCConfig().getString("discord.commands-channel-id"))) {
            if (!this.uchat.getUCConfig().getStringList("discord.server-commands.whitelist").isEmpty()) {
                int count = 0;
                for (String cmd : this.uchat.getUCConfig().getStringList("discord.server-commands.whitelist")) {
                    if (message.startsWith(cmd)) count++;
                }
                if (count == 0) return;
            }
            if (!this.uchat.getUCConfig().getStringList("discord.server-commands.blacklist").isEmpty()) {
                int count = 0;
                for (String cmd : this.uchat.getUCConfig().getStringList("discord.server-commands.blacklist")) {
                    if (message.startsWith(cmd)) count++;
                }
                if (count > 0) return;
            }
            UCUtil.performCommand(Bukkit.getServer().getConsoleSender(), message);
        }
    }

    private UltimateFancy formatDDMessage(MessageReceivedEvent e, String message, UCChannel ch) {
        UltimateFancy fancy = new UltimateFancy(UChat.get());

        if (ch != null) {
            //format prefixes tags
            String formated = formatTags(ch.getDiscordtoMCFormat(), ch, e, "", "");

            //add dd channel name to hover
            String hovered = formatTags(ch.getDiscordHover(), ch, e, "", "");
            fancy.text(formated);
            if (!hovered.isEmpty()) {
                fancy.hoverShowText(hovered);
            }
            fancy.next();
        }

        //format message
        if (!e.getMessage().getAttachments().isEmpty()) {
            try {
                fancy.clickOpenURL(new URL(e.getMessage().getAttachments().get(0).getUrl()));
                fancy.hoverShowText(e.getMessage().getAttachments().get(0).getFileName());
                if (message.isEmpty()) {
                    fancy.text("- Attachment -");
                } else {
                    fancy.text(message);
                }
            } catch (MalformedURLException ignore) {
            }
        } else {
            message = message.replace("\\n", "\n");

            //emoji
            Pattern pe = Pattern.compile(":(.+?):");
            Matcher me = pe.matcher(message);
            if (me.find())
                message = message.replaceAll("<:(.+?)>", ":" + me.group(1) + ":");
            else
                message = message.replaceAll("<:(.+?)>", ":?:");

            //user and role name
            Pattern pp = Pattern.compile("<@(.+?)>");
            Matcher mp = pp.matcher(message);
            if (mp.find()){
                if (e.getMessage().getMentions().getRoles().stream().findFirst().isPresent()){
                    String role = e.getMessage().getMentions().getRoles().stream().findFirst().get().getName();
                    message = message.replaceAll("<@(.+?)>", "@" + role);
                } else
                if (e.getMessage().getMentions().getMembers().stream().findFirst().isPresent()){
                    String nick = e.getMessage().getMentions().getMembers().stream().findFirst().get().getNickname();
                    if (nick == null || nick.isEmpty())
                        nick = e.getMessage().getMentions().getMembers().stream().findFirst().get().getEffectiveName();

                    message = message.replaceAll("<@(.+?)>", "@" + nick);
                }
                else
                    message = message.replaceAll("<@(.+?)>", "@?");
            }

            //channel
            Pattern pc = Pattern.compile("<#(.+?)>");
            Matcher mc = pc.matcher(message);
            if (mc.find() && e.getMessage().getMentions().getChannels().stream().anyMatch(chg -> chg.getId().equals(mc.group(1))))
                message = message.replaceAll("<#(.+?)>", "#" + e.getMessage().getMentions().getChannels().stream().filter(chg -> chg.getId().equals(mc.group(1))).findFirst().get().getName());
            else
                message = message.replaceAll("<#(.+?)>", "#?");

            fancy.text(message);
        }
        return fancy;
    }

    public void updateGame(String text) {
        String game = uchat.getLang().get("discord.game").replace("{online}", String.valueOf(uchat.getServer().getOnlinePlayers().size()));
        Activity activity = Activity.playing(game);
        jda.getPresence().setActivity(activity);
    }

    public void sendTellToDiscord(String text) {
        if (!uchat.getUCConfig().getString("discord.tell-channel-id").isEmpty()) {
            sendToChannel(uchat.getUCConfig().getString("discord.tell-channel-id"), text);
        }
    }

    public void sendCommandsToDiscord(String text) {
        if (!uchat.getUCConfig().getString("discord.commands-channel-id").isEmpty()) {
            sendToChannel(uchat.getUCConfig().getString("discord.commands-channel-id"), text);
        }
    }

    public void sendRawToDiscord(String text) {
        if (!uchat.getUCConfig().getString("discord.log-channel-id").isEmpty()) {
            sendToChannel(uchat.getUCConfig().getString("discord.log-channel-id"), text);
        }
    }

    public void sendToDiscord(CommandSender sender, String text, UCChannel ch) {
        if (ch.isSendingDiscord()) {
            if (!UCPerms.hasPerm(sender, "discord.mention")) {
                text = text.replace("@everyone", "everyone")
                        .replace("@here", "here");
            }
            text = formatTags(ch.getMCtoDiscordFormat(), ch, null, sender.getName(), text);
            for (String ddid : ch.getDiscordChannelID()) {
                sendToChannel(ddid, text);
            }
        }
    }

    public void sendToChannel(String id, String text) {
        try {
            if (id.isEmpty()) return;
            text = text.replaceAll("([&" + ChatColor.COLOR_CHAR + "]([a-fA-Fk-oK-ORr0-9]))", "");
            TextChannel ch = jda.getTextChannelById(id);
            ch.sendMessage(text).queue();
        } catch (PermissionException e) {
            uchat.getUCLogger().severe("JDA: No permission to send messages to channel " + id + ".");
        } catch (Exception e) {
            uchat.getUCLogger().warning("JDA: The channel ID [" + id + "] is incorrect, not available or Discord is offline, in maintenance or some other connection problem.");
            e.printStackTrace();
        }
    }

    @Override
    public String getSyncNickName(String ddId) {
        return null;
    }

    public void setPlayerRole(String ddUser, List<String> ddRoleIds, String nick, List<String> configRoles) {
        try {
            Guild gc = this.jda.getGuildById(this.uchat.getDDSync().getGuidId());
            Member member;
            try {
                member = gc.retrieveMemberById(ddUser).complete(true);
            } catch (RateLimitedException ignored) {
                member = gc.getMemberById(ddUser);
            }
            if (!nick.isEmpty() && (member.getNickname() == null || !member.getNickname().equals(nick))) {
                member.modifyNickname(nick).complete(true);
            }

            List<Role> roles = new ArrayList<>();
            ddRoleIds.forEach(r -> roles.add(gc.getRoleById(r)));
            gc.modifyMemberRoles(member, roles, configRoles.stream().map(gc::getRoleById).filter(r-> r != null && !roles.contains(r)).collect(Collectors.toList())).complete(true);
        } catch (ErrorResponseException e) {
            UChat.get().getUCLogger().warning("Jda response error: " + e.getLocalizedMessage());
            UChat.get().getUCLogger().warning("Additional info: User ID:" + ddUser);
            if (this.uchat.getDDSync().getSyncNickName(ddUser) != null)
                this.uchat.getDDSync().unlink(this.uchat.getDDSync().getSyncNickName(ddUser));
        } catch (RateLimitedException e) {
            UChat.get().getUCLogger().warning("Jda Rate Limited Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private String formatTags(String format, UCChannel ch, MessageReceivedEvent e, String sender, String message) {
        format = format.replace("{ch-color}", ch.getColor())
                .replace("{ch-alias}", ch.getAlias())
                .replace("{ch-name}", ch.getName());
        if (e != null) {
            sender = UChatColor.stripColor(UChatColor.translateAlternateColorCodes(e.getMember().getEffectiveName()));
            format = format.replace("{sender}", sender)
                    .replace("{dd-channel}", e.getChannel().getName())
                    .replace("{message}", e.getMessage().getContentRaw());
            if (!e.getMember().getRoles().isEmpty()) {
                Role role = e.getMember().getRoles().get(0);
                if (role.getColor() != null) {
                    format = format.replace("{dd-rolecolor}", fromRGB(
                            role.getColor().getRed(),
                            role.getColor().getGreen(),
                            role.getColor().getBlue()).toString());
                }
                format = format.replace("{dd-rolename}", role.getName());
            }
            if (e.getMember().getNickname() != null) {
                format = format.replace("{nickname}", UChatColor.stripColor(UChatColor.translateAlternateColorCodes(e.getMember().getNickname())));
            } else {
                format = format.replace("{nickname}", sender);
            }
        }
        //if not filtered
        format = format
                .replace("{sender}", sender)
                .replace("{message}", message);
        format = format.replaceAll("\\{.*\\}", "");
        return UChatColor.translateAlternateColorCodes(format);
    }
}

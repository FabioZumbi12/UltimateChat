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

import br.net.fabiozumbi12.UltimateChat.Bukkit.*;
import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.entities.*;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.PermissionException;
import jdalib.jda.core.exceptions.RateLimitedException;
import jdalib.jda.core.hooks.ListenerAdapter;
import jdalib.jda.core.managers.GuildController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.security.auth.login.LoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UCDiscord extends ListenerAdapter implements UCDInterface {
    private static final Map<ChatColor, ColorSet<Integer, Integer, Integer>> colorMap = new HashMap<>();

    static {
        colorMap.put(ChatColor.BLACK, new ColorSet<>(0, 0, 0));
        colorMap.put(ChatColor.DARK_BLUE, new ColorSet<>(0, 0, 170));
        colorMap.put(ChatColor.DARK_GREEN, new ColorSet<>(0, 170, 0));
        colorMap.put(ChatColor.DARK_AQUA, new ColorSet<>(0, 170, 170));
        colorMap.put(ChatColor.DARK_RED, new ColorSet<>(170, 0, 0));
        colorMap.put(ChatColor.DARK_PURPLE, new ColorSet<>(170, 0, 170));
        colorMap.put(ChatColor.GOLD, new ColorSet<>(255, 170, 0));
        colorMap.put(ChatColor.GRAY, new ColorSet<>(170, 170, 170));
        colorMap.put(ChatColor.DARK_GRAY, new ColorSet<>(85, 85, 85));
        colorMap.put(ChatColor.BLUE, new ColorSet<>(85, 85, 255));
        colorMap.put(ChatColor.GREEN, new ColorSet<>(85, 255, 85));
        colorMap.put(ChatColor.AQUA, new ColorSet<>(85, 255, 255));
        colorMap.put(ChatColor.RED, new ColorSet<>(255, 85, 85));
        colorMap.put(ChatColor.LIGHT_PURPLE, new ColorSet<>(255, 85, 255));
        colorMap.put(ChatColor.YELLOW, new ColorSet<>(255, 255, 85));
        colorMap.put(ChatColor.WHITE, new ColorSet<>(255, 255, 255));
    }

    private JDA jda;
    private UChat uchat;
    private int taskId;

    public UCDiscord(UChat plugin) {
        this.uchat = plugin;
        try {
            jda = new JDABuilder(AccountType.BOT).setToken(this.uchat.getUCConfig().getString("discord.token")).buildBlocking();
            jda.addEventListener(this);
            if (plugin.getUCConfig().getBoolean("discord.update-status")) {
                Game.GameType type = Game.GameType.valueOf(plugin.getUCConfig().getString("discord.game-type").toUpperCase());
                if (type.equals(Game.GameType.STREAMING) && Game.isValidStreamingUrl(plugin.getUCConfig().getString("discord.twitch"))) {
                    jda.getPresence().setGame(Game.of(type, plugin.getLang().get("discord.game").replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size())), plugin.getUCConfig().getString("discord.twitch")));
                } else {
                    jda.getPresence().setGame(Game.of(type, plugin.getLang().get("discord.game").replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()))));
                }
            }
        } catch (LoginException e) {
            uchat.getLogger().severe("The TOKEN is wrong or empty! Check you config and your token.");
        } catch (IllegalArgumentException | InterruptedException e) {
            e.printStackTrace();
        }

        if (this.uchat.getUCConfig().getBoolean("discord.update-status")) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () ->
                    updateGame(this.uchat.getLang().get("discord.game").replace("{online}",
                            String.valueOf(this.uchat.getServer().getOnlinePlayers().stream().filter(p->!p.hasPermission("uchat.discord.hide")).count()))), 40, 40);
        }
    }

    /*   ------ color util --------   */
    private static ChatColor fromRGB(int r, int g, int b) {
        TreeMap<Integer, ChatColor> closest = new TreeMap<>();
        colorMap.forEach((color, set) -> {
            int red = Math.abs(r - set.getRed());
            int green = Math.abs(g - set.getGreen());
            int blue = Math.abs(b - set.getBlue());
            closest.put(red + green + blue, color);
        });
        return closest.firstEntry().getValue();
    }

    public boolean JDAAvailable() {
        return this.jda != null;
    }

    public int getTaskId() {
        return this.taskId;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId())) return;

        String message = e.getMessage().getContentRaw();
        String[] args = message.split(" ");

        //listen bot privates for dd sync
        if (e.getMessage().isFromType(ChannelType.PRIVATE)) {

            GuildController gc = this.jda.getGuildById(this.uchat.getDDSync().getGuidId()).getController();
            if (gc.getGuild().getMemberById(e.getAuthor().getId()).getRoles().stream().anyMatch(r -> this.uchat.getDDSync().isDDAdminRole(r.getId()))){
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

        if (e.getMember().getUser().isFake()) return;
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
                    UCUtil.performCommand(null, Bukkit.getServer().getConsoleSender(), message);
                    used++;
                } else {
                    UltimateFancy fancy = new UltimateFancy();

                    //format prefixes tags
                    String formated = formatTags(ch.getDiscordtoMCFormat(), ch, e, "", "");

                    //add dd channel name to hover
                    String hovered = formatTags(ch.getDiscordHover(), ch, e, "", "");
                    fancy.text(formated);
                    if (!hovered.isEmpty()) {
                        fancy.hoverShowText(hovered);
                    }
                    fancy.next();

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
                        message = message.replace("/n", "\n");

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
                            if (e.getMessage().getMentionedRoles().stream().findFirst().isPresent()){
                                String role = e.getMessage().getMentionedRoles().stream().findFirst().get().getName();
                                message = message.replaceAll("<@(.+?)>", "@" + role);
                            } else
                            if (e.getMessage().getMentionedMembers().stream().findFirst().isPresent()){
                                String nick = e.getMessage().getMentionedMembers().stream().findFirst().get().getNickname();
                                if (nick == null || nick.isEmpty())
                                    nick = e.getMessage().getMentionedMembers().stream().findFirst().get().getEffectiveName();

                                message = message.replaceAll("<@(.+?)>", "@" + nick);
                            }
                            else
                                message = message.replaceAll("<@(.+?)>", "@?");
                        }

                        //channel
                        Pattern pc = Pattern.compile("<#(.+?)>");
                        Matcher mc = pc.matcher(message);
                        if (mc.find() && e.getMessage().getMentionedChannels().stream().anyMatch(chg -> chg.getId().equals(mc.group(1))))
                            message = message.replaceAll("<#(.+?)>", "#" + e.getMessage().getMentionedChannels().stream().filter(chg -> chg.getId().equals(mc.group(1))).findFirst().get().getName());
                        else
                            message = message.replaceAll("<#(.+?)>", "#?");

                        fancy.text(message);
                    }
                    ch.sendMessage(uchat.getServer().getConsoleSender(), fancy, true);
                    used++;
                }
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
            UCUtil.performCommand(null, Bukkit.getServer().getConsoleSender(), message);
        }
    }

    public void updateGame(String text) {
        Game.GameType type = Game.GameType.valueOf(uchat.getUCConfig().getString("discord.game-type").toUpperCase());
        if (type.equals(Game.GameType.STREAMING) && Game.isValidStreamingUrl(uchat.getUCConfig().getString("discord.twitch"))) {
            jda.getPresence().setGame(Game.of(type, uchat.getLang().get("discord.game").replace("{online}", String.valueOf(uchat.getServer().getOnlinePlayers().size())), uchat.getUCConfig().getString("discord.twitch")));
        } else {
            jda.getPresence().setGame(Game.of(type, uchat.getLang().get("discord.game").replace("{online}", String.valueOf(uchat.getServer().getOnlinePlayers().size()))));
        }
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

    private void sendToChannel(String id, String text) {
        if (id.isEmpty()) return;
        text = text.replaceAll("([&" + ChatColor.COLOR_CHAR + "]([a-fA-Fk-oK-ORr0-9]))", "");
        TextChannel ch = jda.getTextChannelById(id);
        try {
            ch.sendMessage(text).queue();
        } catch (PermissionException e) {
            uchat.getUCLogger().severe("JDA: No permission to send messages to channel " + ch.getName() + ".");
        } catch (Exception e) {
            uchat.getUCLogger().warning("JDA: The channel ID [" + id + "] is incorrect, not available or Discord is offline, in maintenance or some other connection problem.");
            e.printStackTrace();
        }
    }

    public void setPlayerRole(String ddUser, List<String> ddRoleIds, String nick, List<String> configRoles) {
        try {
            GuildController gc = this.jda.getGuildById(this.uchat.getDDSync().getGuidId()).getController();
            Member member = gc.getGuild().getMemberById(ddUser);
            if (!nick.isEmpty() && (member.getNickname() == null || !member.getNickname().equals(nick))) {
                gc.setNickname(member, nick).complete(true);
            }

            List<Role> roles = new ArrayList<>();
            ddRoleIds.forEach(r -> roles.add(gc.getGuild().getRoleById(r)));
            gc.modifyMemberRoles(member, roles, configRoles.stream().map(r->gc.getGuild().getRoleById(r)).filter(r->!roles.contains(r)).collect(Collectors.toList())).complete(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String formatTags(String format, UCChannel ch, MessageReceivedEvent e, String sender, String message) {
        format = format.replace("{ch-color}", ch.getColor())
                .replace("{ch-alias}", ch.getAlias())
                .replace("{ch-name}", ch.getName());
        if (e != null) {
            sender = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMember().getEffectiveName()));
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
                format = format.replace("{nickname}", ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', e.getMember().getNickname())));
            } else {
                format = format.replace("{nickname}", sender);
            }
        }
        //if not filtered
        format = format
                .replace("{sender}", sender)
                .replace("{message}", message);
        format = format.replaceAll("\\{.*\\}", "");
        return ChatColor.translateAlternateColorCodes('&', format);
    }

    public void shutdown() {
        this.uchat.getUCLogger().info("Shutdown JDA...");
        this.jda.shutdown();
        this.uchat.getUCLogger().info("JDA disabled!");
    }

    private static class ColorSet<R, G, B> {
        R red;
        G green;
        B blue;

        ColorSet(R red, G green, B blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        private R getRed() {
            return red;
        }

        private G getGreen() {
            return green;
        }

        private B getBlue() {
            return blue;
        }
    }
}

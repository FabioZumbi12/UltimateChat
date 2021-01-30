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

package br.net.fabiozumbi12.UltimateChat.Sponge.discord;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import jdalib.jda.api.JDA;
import jdalib.jda.api.JDABuilder;
import jdalib.jda.api.entities.*;
import jdalib.jda.api.events.message.MessageReceivedEvent;
import jdalib.jda.api.exceptions.ErrorResponseException;
import jdalib.jda.api.exceptions.PermissionException;
import jdalib.jda.api.exceptions.RateLimitedException;
import jdalib.jda.api.hooks.ListenerAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;

import javax.security.auth.login.LoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UCDiscord extends ListenerAdapter implements UCDInterface {
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

    private JDA jda;
    private final UChat uchat;

    public UCDiscord(UChat plugin) {
        this.uchat = plugin;
        try {
            jda = JDABuilder.createDefault(this.uchat.getConfig().root().discord.token)
                    .addEventListeners(this)
                    .build().awaitReady();

            if (plugin.getConfig().root().discord.update_status) {
                String game = plugin.getLang().get("discord.game").replace("{online}", String.valueOf(Sponge.getServer().getOnlinePlayers().size()));
                Activity activity = Activity.playing(game);
                jda.getPresence().setActivity(activity);
            }
        } catch (IllegalArgumentException | InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (LoginException e) {
            e.printStackTrace();
            uchat.getLogger().warning(e.getLocalizedMessage());
            uchat.getLogger().severe("The TOKEN is wrong or empty! Check you config and your token.");
            return;
        }

        if (UChat.get().getConfig().root().discord.update_status) {
            Sponge.getScheduler().createSyncExecutor(plugin).scheduleAtFixedRate(() ->
                    updateGame(UChat.get().getLang().get("discord.game").replace("{online}", String.valueOf(Sponge.getServer().getOnlinePlayers().stream().filter(p->!p.hasPermission("uchat.discord.hide")).count()))), 2, 2, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        this.uchat.getLogger().info("Shutdown JDA...");
        this.jda.getRegisteredListeners().forEach(l -> this.jda.removeEventListener(l));
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

    public boolean JDAAvailable() {
        return this.jda != null && this.jda.getStatus().isInit();
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

        if (e.getMember() == null || e.getMember().getUser().isFake()) return;
        int used = 0;

        for (UCChannel ch : this.uchat.getChannels().values()) {
            if (ch.isListenDiscord() && ch.matchDiscordID(e.getChannel().getId())) {
                if (e.getMember().getUser().isBot() && !ch.AllowBot()) {
                    continue;
                }
                //check if is cmd
                if (message.startsWith(this.uchat.getConfig().root().discord.server_commands.alias) && ch.getDiscordAllowCmds()) {
                    message = message.replace(this.uchat.getConfig().root().discord.server_commands.alias + " ", "");
                    if (!this.uchat.getConfig().root().discord.server_commands.withelist.isEmpty()) {
                        int count = 0;
                        for (String cmd : this.uchat.getConfig().root().discord.server_commands.withelist) {
                            if (message.startsWith(cmd)) count++;
                        }
                        if (count == 0) continue;
                    }
                    if (!this.uchat.getConfig().root().discord.server_commands.blacklist.isEmpty()) {
                        int count = 0;
                        for (String cmd : this.uchat.getConfig().root().discord.server_commands.blacklist) {
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
                    if (!hovered.isEmpty()) {
                        text.onHover(TextActions.showText(Text.of(hovered)));
                    }

                    //format message
                    if (!e.getMessage().getAttachments().isEmpty()) {
                        try {
                            text.onClick(TextActions.openUrl(new URL(e.getMessage().getAttachments().get(0).getUrl())));
                            text.onHover(TextActions.showText(Text.of(e.getMessage().getAttachments().get(0).getFileName())));
                            if (message.isEmpty()) {
                                text.append(Text.of("- Attachment -"));
                            } else {
                                text.append(Text.of(message));
                            }
                        } catch (MalformedURLException ignored) {
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

                        text.append(Text.of(message));
                    }
                    ch.sendMessage(Sponge.getServer().getConsole(), text.build(), true);
                    used++;
                }
            }
        }

        //check if is from log command chanel
        if (used == 0 && e.getChannel().getId().equals(UChat.get().getConfig().root().discord.commands_channel_id)) {
            if (!this.uchat.getConfig().root().discord.server_commands.withelist.isEmpty()) {
                int count = 0;
                for (String cmd : this.uchat.getConfig().root().discord.server_commands.withelist) {
                    if (message.startsWith(cmd)) count++;
                }
                if (count == 0) return;
            }
            if (!this.uchat.getConfig().root().discord.server_commands.blacklist.isEmpty()) {
                int count = 0;
                for (String cmd : this.uchat.getConfig().root().discord.server_commands.blacklist) {
                    if (message.startsWith(cmd)) count++;
                }
                if (count > 0) return;
            }
            Sponge.getCommandManager().process(Sponge.getServer().getConsole(), message);
        }
    }

    public void updateGame(String text) {
        String game = uchat.getLang().get("discord.game").replace("{online}", String.valueOf(Sponge.getServer().getOnlinePlayers().size()));
        Activity activity = Activity.playing(game);
        jda.getPresence().setActivity(activity);
    }

    public void sendTellToDiscord(String text) {
        if (!uchat.getConfig().root().discord.tell_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.tell_channel_id, text);
        }
    }

    public void sendCommandsToDiscord(String text) {
        if (!uchat.getConfig().root().discord.commands_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.commands_channel_id, text);
        }
    }

    public void sendRawToDiscord(String text) {
        if (!uchat.getConfig().root().discord.log_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.log_channel_id, text);
        }
    }

    public void sendPixelmonLegendary(String text) {
        if (!uchat.getConfig().root().discord.pixelmon.legendary_channel_id.isEmpty()) {
            sendToChannel(uchat.getConfig().root().discord.pixelmon.legendary_channel_id, text);
        }
    }

    public void sendToDiscord(CommandSource sender, String text, UCChannel ch) {
        if (ch.isSendingDiscord()) {
            if (!uchat.getPerms().hasPerm(sender, "discord.mention")) {
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
        text = text.replaceAll("([&§]([a-fk-or0-9]))", "");
        TextChannel ch = jda.getTextChannelById(id);
        try {
            ch.sendMessage(text).queue();
        } catch (PermissionException e) {
            uchat.getLogger().severe("JDA: No permission to send messages to channel " + ch.getName() + ".");
        } catch (Exception e) {
            uchat.getLogger().warning("JDA: The channel ID is incorrect, not available or Discord is offline, in maintenance or some other connection problem.");
            e.printStackTrace();
        }
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
                gc.retrieveMember(member.getUser()).complete(true).modifyNickname(nick).complete(true);
            }

            List<Role> roles = new ArrayList<>();
            ddRoleIds.forEach(r -> roles.add(gc.getRoleById(r)));
            gc.modifyMemberRoles(member, roles, configRoles.stream().map(r->gc.getRoleById(r)).filter(r-> r != null && !roles.contains(r)).collect(Collectors.toList())).complete(true);
        } catch (ErrorResponseException e) {
            UChat.get().getLogger().warning("Jda response error: " + e.getLocalizedMessage());
            UChat.get().getLogger().warning("Additional info: User ID:" + ddUser);
            if (this.uchat.getDDSync().getSyncNickName(ddUser) != null)
                this.uchat.getDDSync().unlink(this.uchat.getDDSync().getSyncNickName(ddUser));
        } catch (RateLimitedException e) {
            UChat.get().getLogger().warning("Jda Rate Limited Exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private String formatTags(String format, UCChannel ch, MessageReceivedEvent e, String sender, String message) {
        format = format.replace("{ch-color}", ch.getColor())
                .replace("{ch-alias}", ch.getAlias())
                .replace("{ch-name}", ch.getName());
        if (e != null) {
            sender = UCUtil.stripColor('§', UCUtil.toColor(e.getMember().getEffectiveName()));
            format = format.replace("{sender}", sender)
                    .replace("{dd-channel}", e.getChannel().getName())
                    .replace("{message}", e.getMessage().getContentRaw());
            if (!e.getMember().getRoles().isEmpty()) {
                Role role = e.getMember().getRoles().get(0);
                if (role.getColor() != null) {
                    format = format.replace("{dd-rolecolor}", fromRGB(
                            role.getColor().getRed(),
                            role.getColor().getGreen(),
                            role.getColor().getBlue()));
                }
                format = format.replace("{dd-rolename}", role.getName());
            }
            if (e.getMember().getNickname() != null) {
                format = format.replace("{nickname}", UCUtil.stripColor('§', UCUtil.toColor(e.getMember().getNickname())));
            } else {
                format = format.replace("{nickname}", sender);
            }
        }
        //if not filtered
        format = format
                .replace("{sender}", sender)
                .replace("{message}", message);
        format = format.replaceAll("\\{.*\\}", "");
        return UCUtil.toColor(format);
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
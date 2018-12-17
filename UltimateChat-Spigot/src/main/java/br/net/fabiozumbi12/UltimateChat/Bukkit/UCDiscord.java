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

import jdalib.jda.core.AccountType;
import jdalib.jda.core.JDA;
import jdalib.jda.core.JDABuilder;
import jdalib.jda.core.entities.Game;
import jdalib.jda.core.entities.Role;
import jdalib.jda.core.entities.TextChannel;
import jdalib.jda.core.events.message.MessageReceivedEvent;
import jdalib.jda.core.exceptions.PermissionException;
import jdalib.jda.core.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import javax.security.auth.login.LoginException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (UChat.get().getUCConfig().getBoolean("discord.update-status")) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () ->
                    updateGame(UChat.get().getLang().get("discord.game").replace("{online}", String.valueOf(UChat.get().getServer().getOnlinePlayers().size()))), 40, 40);
        }
    }

    /*   ------ color util --------   */
    public static ChatColor fromRGB(int r, int g, int b) {
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
        if (e.getAuthor().getId().equals(e.getJDA().getSelfUser().getId()) || e.getMember().getUser().isFake()) return;

        String message = e.getMessage().getContentRaw();
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

                        //user name
                        Pattern pp = Pattern.compile("<@(.+?)>");
                        Matcher mp = pp.matcher(message);
                        if (mp.find())
                            message = message.replaceAll("<@(.+?)>", "@" + e.getAuthor().getName());
                        else
                            message = message.replaceAll("<@(.+?)>", "@?");

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

    public void sendToChannel(String id, String text) {
        text = text.replaceAll("([&" + ChatColor.COLOR_CHAR + "]([a-fA-Fk-oK-ORr0-9]))", "");
        TextChannel ch = jda.getTextChannelById(id);
        try {
            ch.sendMessage(text).queue();
        } catch (PermissionException e) {
            uchat.getUCLogger().severe("JDA: No permission to send messages to channel " + ch.getName() + ".");
        } catch (Exception e) {
            uchat.getUCLogger().warning("JDA: The channel ID is incorrect, not available or Discord is offline, in maintance or some other connection problem.");
            e.printStackTrace();
        }
    }

    private String formatTags(String format, UCChannel ch, MessageReceivedEvent e, String sender, String message) {
        format = format.replace("{ch-color}", ch.getColor())
                .replace("{ch-alias}", ch.getAlias())
                .replace("{ch-name}", ch.getName())
                .replace("{ch-nickname}", ch.getNickName());
        if (e != null) {
            format = format.replace("{sender}", e.getMember().getEffectiveName())
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
                format = format.replace("{nickname}", e.getMember().getNickname());
            } else {
                format = format.replace("{nickname}", e.getMember().getEffectiveName());
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
}

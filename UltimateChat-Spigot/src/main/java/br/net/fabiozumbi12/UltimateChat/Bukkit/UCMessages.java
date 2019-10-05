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

import br.net.fabiozumbi12.UltimateChat.Bukkit.API.PostFormatChatMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;
import br.net.fabiozumbi12.UltimateChat.Bukkit.bungee.UChatBungee;
import me.clip.placeholderapi.PlaceholderAPI;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UCMessages {

    private static HashMap<String, String> registeredReplacers = new HashMap<>();
    private static String[] defFormat = new String[0];

    static boolean sendFancyMessage(String[] format, String msg, UCChannel channel, CommandSender sender, CommandSender tellReceiver) {
        //Execute listener:
        HashMap<String, String> tags = new HashMap<>();
        for (String str : UChat.get().getUCConfig().getStringList("api.legendchat-tags")) {
            tags.put(str, str);
        }
        SendChannelMessageEvent event = new SendChannelMessageEvent(tags, format, sender, channel, msg);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return event.getCancelIncomingChat();
        }

        boolean cancel = event.getCancelIncomingChat();
        //String toConsole = "";
        registeredReplacers = event.getResgisteredTags();
        defFormat = event.getDefFormat();
        String evmsg = event.getMessage();

        if (evmsg == null || evmsg.isEmpty()) return cancel;

        HashMap<CommandSender, UltimateFancy> msgPlayers = new HashMap<>();
        evmsg = composeColor(sender, evmsg);

        String finalEvmsg = evmsg;
        Bukkit.getScheduler().runTask(UChat.get(), () -> {
            UCChannel ch;
            if (event.getChannel() != null) {

                ch = event.getChannel();

                if (sender instanceof Player && !ch.availableWorlds().isEmpty() && !ch.availableInWorld(((Player) sender).getWorld())) {
                    UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
                    return;
                }

                if (!UCPerms.channelWritePerm(sender, ch)) {
                    UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
                    return;
                }

                if (ch.isMuted(sender.getName())) {
                    UChat.get().getLang().sendMessage(sender, "channel.muted");
                    return;
                }

                if (!UCPerms.hasPerm(sender, "bypass.cost") && UChat.get().getVaultEco() != null && sender instanceof Player && ch.getCost() > 0) {
                    if (UChat.get().getVaultEco().getBalance((Player) sender, ((Player) sender).getWorld().getName()) < ch.getCost()) {
                        UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.cost").replace("{value}", "" + ch.getCost()));
                        return;
                    } else {
                        UChat.get().getVaultEco().withdrawPlayer((Player) sender, ((Player) sender).getWorld().getName(), ch.getCost());
                    }
                }

                List<CommandSender> receivers = new ArrayList<>();
                int noWorldReceived = 0;
                int vanish = 0;

                //put sender
                msgPlayers.put(sender, sendMessage(sender, sender, finalEvmsg, ch));
                if (ch.getDistance() > 0 && sender instanceof Player) {
                    for (Player play : ((Player) sender).getNearbyEntities(ch.getDistance(), ch.getDistance(), ch.getDistance()).stream()
                            .filter(ent -> ent instanceof Player)
                            .map(ent -> (Player) ent)
                            .collect(Collectors.toList())) {
                        if (UCPerms.channelReadPerm(play, ch)) {
                            if (!ch.availableWorlds().isEmpty() && !ch.availableInWorld(play.getWorld())) {
                                continue;
                            }
                            if (ch.isIgnoring(play.getName())) {
                                continue;
                            }
                            if (isIgnoringPlayers(play.getName(), sender.getName())) {
                                noWorldReceived++;
                                continue;
                            }
                            if (!((Player) sender).canSee(play) && !UCPerms.hasPermission(sender, "uchat.see-vanish")) {
                                vanish++;
                            } else {
                                noWorldReceived++;
                            }
                            if (!ch.neeFocus() || ch.isMember(play)) {
                                msgPlayers.put(play, sendMessage(sender, play, finalEvmsg, ch));
                                receivers.add(play);
                            }
                        }
                    }
                } else {
                    for (Player receiver : UChat.get().getServer().getOnlinePlayers()) {
                        if (receiver.equals(sender) || !UCPerms.channelReadPerm(receiver, ch) || (!ch.crossWorlds() && (sender instanceof Player && !receiver.getWorld().equals(((Player) sender).getWorld())))) {
                            continue;
                        }
                        if (!ch.availableWorlds().isEmpty() && !ch.availableInWorld(receiver.getWorld())) {
                            continue;
                        }
                        if (ch.isIgnoring(receiver.getName())) {
                            continue;
                        }
                        if (isIgnoringPlayers(receiver.getName(), sender.getName())) {
                            noWorldReceived++;
                            continue;
                        }
                        if (sender instanceof Player && (!((Player) sender).canSee(receiver) && !UCPerms.hasPermission(sender, "uchat.see-vanish"))) {
                            vanish++;
                        } else {
                            noWorldReceived++;
                        }
                        if (!ch.neeFocus() || ch.isMember(receiver)) {
                            msgPlayers.put(receiver, sendMessage(sender, receiver, finalEvmsg, ch));
                            receivers.add(receiver);
                        }
                    }
                }

                //chat spy
                for (Player receiver : UChat.get().getServer().getOnlinePlayers()) {
                    if (!receiver.equals(sender) && !receivers.contains(receiver) && !receivers.contains(sender) &&
                            (UChat.get().isSpy.contains(receiver.getName()) && UCPerms.hasSpyPerm(receiver, ch.getName()) || UCPerms.hasPermission(receiver, "uchat.chat-spy.bypass"))) {
                        String spyformat = UChat.get().getUCConfig().getString("general.spy-format");
                        spyformat = spyformat.replace("{output}", ChatColor.stripColor(sendMessage(sender, receiver, finalEvmsg, ch).toOldFormat()));
                        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', spyformat));
                    }
                }

                if (ch.getDistance() == 0 && noWorldReceived <= 0) {
                    if (ch.getReceiversMsg()) {
                        UChat.get().getLang().sendMessage(sender, "channel.noplayer.world");
                    }
                }
                if ((receivers.size() - vanish) <= 0) {
                    if (ch.getReceiversMsg()) {
                        UChat.get().getLang().sendMessage(sender, "channel.noplayer.near");
                    }
                }

            } else {
                //send tell
                if (UChat.get().msgTogglePlayers.contains(tellReceiver.getName()) && !sender.hasPermission("uchat.msgtoggle.exempt")) {
                    UChat.get().getLang().sendMessage(sender, "cmd.msgtoggle.msgdisabled");
                    return;
                }

                ch = new UCChannel("tell");

                //send spy
                for (Player receiver : UChat.get().getServer().getOnlinePlayers()) {
                    if (!receiver.equals(tellReceiver) && !receiver.equals(sender) &&
                            (UChat.get().isSpy.contains(receiver.getName()) && UCPerms.hasSpyPerm(receiver, "private") || UCPerms.hasPermission(receiver, "uchat.chat-spy.bypass"))) {
                        String spyformat = UChat.get().getUCConfig().getString("general.spy-format");
                        if (isIgnoringPlayers(tellReceiver.getName(), sender.getName())) {
                            spyformat = UChat.get().getLang().get("chat.ignored") + spyformat;
                        }
                        spyformat = spyformat.replace("{output}", ChatColor.stripColor(sendMessage(sender, tellReceiver, finalEvmsg, ch).toOldFormat()));
                        receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', spyformat));
                    }
                }

                msgPlayers.put(sender, sendMessage(sender, tellReceiver, finalEvmsg, ch));
                if (!isIgnoringPlayers(tellReceiver.getName(), sender.getName())) {
                    msgPlayers.put(tellReceiver, sendMessage(sender, tellReceiver, finalEvmsg, ch));
                }
                if (isIgnoringPlayers(tellReceiver.getName(), sender.getName())) {
                    msgPlayers.put(UChat.get().getServer().getConsoleSender(), new UltimateFancy(UChat.get().getLang().get("chat.ignored") + msgPlayers.get(sender).toOldFormat()));
                }
            }

            if (!msgPlayers.containsKey(UChat.get().getServer().getConsoleSender())) {
                msgPlayers.put(UChat.get().getServer().getConsoleSender(), msgPlayers.get(sender));
            }

            //fire post event
            PostFormatChatMessageEvent postEvent = new PostFormatChatMessageEvent(sender, msgPlayers, ch, msg, false);
            Bukkit.getPluginManager().callEvent(postEvent);

            if (!postEvent.isCancelled()) {

                if (!ch.isTell() && ch.isBungee()) {
                    UChatBungee.sendBungee(ch, msgPlayers.get(sender));
                } else {
                    msgPlayers.forEach((send, text) -> {
                        UChat.get().getUCLogger().timings(timingType.END, "UCMessages#send()|before send");
                        text.send(send);
                        UChat.get().getUCLogger().timings(timingType.END, "UCMessages#send()|after send");
                    });
                }

                //send to jda
                if (UChat.get().getUCJDA() != null) {
                    if (ch.isTell()) {
                        UChat.get().getUCJDA().sendTellToDiscord(msgPlayers.get(sender).toOldFormat());
                    } else {
                        UChat.get().getUCJDA().sendToDiscord(sender, finalEvmsg, ch);
                    }
                }
            }
        });
        return cancel;
    }

    private static String composeColor(CommandSender sender, String evmsg) {
        if (evmsg == null || evmsg.isEmpty()) return "";

        if (sender instanceof Player) {
            Pattern mat1 = Pattern.compile("(?i)&([A-Fa-f0-9Rr])");
            Pattern mat2 = Pattern.compile("(?i)&([L-Ol-o])");
            Pattern mat3 = Pattern.compile("(?i)&([Kk])");

            if (!UCPerms.hasPerm(sender, "chat.color")) {
                while (mat1.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat1.pattern(), "");
                }
            }
            if (!UCPerms.hasPerm(sender, "chat.color.formats")) {
                while (mat2.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat2.pattern(), "");
                }
            }
            if (!UCPerms.hasPerm(sender, "chat.color.magic")) {
                while (mat3.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat3.pattern(), "");
                }
            }

            if (!UCPerms.hasPerm(sender, "chat.newline")) {
                evmsg = evmsg.replace("/n", " ");
            } else {
                evmsg = evmsg.replace("/n", "\n");
            }
        }
        evmsg = ChatColor.translateAlternateColorCodes('&', evmsg);
        return evmsg;
    }

    public static boolean isIgnoringPlayers(String p, String victim) {
        Player play = Bukkit.getPlayer(p);
        if (play != null && (play.isOp() || UCPerms.hasPermission(play, "uchat.admin"))) {
            return false;
        }

        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        return list.contains(victim);
    }

    public static void ignorePlayer(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        list.add(victim);
        UChat.get().ignoringPlayer.put(p, list);
    }

    public static void unIgnorePlayer(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        list.remove(victim);
        UChat.get().ignoringPlayer.put(p, list);
    }

    @SuppressWarnings("deprecation")
    public static UltimateFancy sendMessage(CommandSender sender, Object receiver, String msg, UCChannel ch) {
        UltimateFancy fanci = new UltimateFancy();

        if (!ch.getName().equals("tell")) {
            String[] defaultBuilder = UChat.get().getUCConfig().getDefBuilder();
            if (ch.useOwnBuilder()) {
                defaultBuilder = ch.getBuilder();
            }

            for (String tag : defaultBuilder) {
                if (UChat.get().getUCConfig().getString("tags." + tag + ".format") == null) {
                    fanci.text(tag).next();
                    continue;
                }

                String perm = UChat.get().getUCConfig().getString("tags." + tag + ".permission");
                String format = UChat.get().getUCConfig().getString("tags." + tag + ".format");
                String execute = UChat.get().getUCConfig().getString("tags." + tag + ".click-cmd");
                String suggest = UChat.get().getUCConfig().getString("tags." + tag + ".suggest-cmd");
                String url = UChat.get().getUCConfig().getString("tags." + tag + ".click-url");
                List<String> messages = UChat.get().getUCConfig().getStringList("tags." + tag + ".hover-messages");
                List<String> showWorlds = UChat.get().getUCConfig().getStringList("tags." + tag + ".show-in-worlds");
                List<String> hideWorlds = UChat.get().getUCConfig().getStringList("tags." + tag + ".hide-in-worlds");

                //check perm
                if (perm != null && !perm.isEmpty() && !UCPerms.hasPermission(sender, perm)) {
                    continue;
                }

                //check show or hide in world
                if (sender instanceof Player) {
                    if (!showWorlds.isEmpty() && !showWorlds.contains(((Player) sender).getWorld().getName())) {
                        continue;
                    }
                    if (!hideWorlds.isEmpty() && hideWorlds.contains(((Player) sender).getWorld().getName())) {
                        continue;
                    }
                }

                StringBuilder tooltip = new StringBuilder();
                for (String tp : messages) {
                    tooltip.append("\n").append(tp.replace("/n", "\n"));
                }
                if (tooltip.length() > 2) {
                    tooltip = new StringBuilder(tooltip.substring(1));
                }

                if (execute != null && !execute.isEmpty()) {
                    fanci.clickRunCmd(formatTags(tag, "/" + execute, sender, receiver, msg, ch));
                }

                if (suggest != null && !suggest.isEmpty()) {
                    fanci.clickSuggestCmd(formatTags(tag, suggest, sender, receiver, msg, ch));
                }

                if (url != null && !url.isEmpty()) {
                    try {
                        fanci.clickOpenURL(new URL(formatTags(tag, url, sender, receiver, msg, ch)));
                    } catch (MalformedURLException ignored) {
                    }
                }

                if (!tag.equals("message") || UCPerms.hasPerm(sender, "chat.click-urls")) {
                    for (String arg : msg.split(" ")) {
                        try {
                            fanci.clickOpenURL(new URL(formatTags(tag, arg, sender, receiver, msg, ch)));
                            fanci.hoverShowText(UCUtil.colorize(formatTags(tag, UChat.get().getUCConfig().getString("general.URL-template").replace("{url}", arg), sender, receiver, msg, ch)));
                        } catch (MalformedURLException ignored) {
                        }
                    }
                }

                if (tag.equals("message") && (!msg.equals(mention(sender, (CommandSender) receiver, msg)) || msg.contains(UChat.get().getUCConfig().getString("general.item-hand.placeholder")))) {
                    tooltip = new StringBuilder(formatTags("", tooltip.toString(), sender, receiver, msg, ch));
                    format = formatTags(tag, format, sender, receiver, msg, ch);

                    if (ch.allowHand() && UChat.get().getUCConfig().getBoolean("general.item-hand.enable") && msg.contains(UChat.get().getUCConfig().getString("general.item-hand.placeholder")) && sender instanceof Player) {
                        if (!((Player) sender).getItemInHand().getType().equals(Material.AIR)) fanci.text(format).hoverShowItem(((Player) sender).getItemInHand()).next();
                    } else if (!msg.equals(mention(sender, (CommandSender) receiver, msg)) && UChat.get().getUCConfig().getString("mention.hover-message").length() > 0 && StringUtils.containsIgnoreCase(msg, ((CommandSender) receiver).getName())) {
                        tooltip = new StringBuilder(formatTags("", UChat.get().getUCConfig().getString("mention.hover-message"), sender, receiver, msg, ch));
                        fanci.text(format).hoverShowText(tooltip.toString()).next();
                    } else if (tooltip.length() > 0) {
                        fanci.text(format).hoverShowText(tooltip.toString()).next();
                    } else {
                        fanci.text(format).next();
                    }
                } else {
                    format = formatTags(tag, format, sender, receiver, msg, ch);
                    tooltip = new StringBuilder(formatTags("", tooltip.toString(), sender, receiver, msg, ch));
                    if (tooltip.length() > 0) {
                        fanci.text(format).hoverShowText(tooltip.toString()).next();
                    } else {
                        fanci.text(format).next();
                    }
                }
            }
        } else {
            //if tell
            String prefix = UChat.get().getUCConfig().getString("tell.prefix");
            String format = UChat.get().getUCConfig().getString("tell.format");
            List<String> messages = UChat.get().getUCConfig().getStringList("tell.hover-messages");

            StringBuilder tooltip = new StringBuilder();
            for (String tp : messages) {
                tooltip.append("\n").append(tp.replace("/n", "\n"));
            }
            if (tooltip.length() > 2) {
                tooltip = new StringBuilder(tooltip.substring(1));
            }


            prefix = formatTags("", prefix, sender, receiver, msg, ch);
            format = formatTags("tell", format, sender, receiver, msg, ch);
            tooltip = new StringBuilder(formatTags("", tooltip.toString(), sender, receiver, msg, ch));

            if (tooltip.length() > 0) {
                fanci.text(format).hoverShowText(tooltip.toString()).next();
            } else {
                fanci.text(prefix).next();
            }
            fanci.text(ChatColor.stripColor(format)).next();
        }
        return fanci;
    }

    public static String mention(Object sender, CommandSender receiver, String msg) {
        if (UChat.get().getUCConfig().getBoolean("mention.enable")) {
            for (Player p : UChat.get().getServer().getOnlinePlayers()) {
                if (!sender.equals(p) && Arrays.stream(msg.split(" ")).anyMatch(p.getName()::equalsIgnoreCase)) {
                    if (receiver instanceof Player && receiver.equals(p)) {

                        String mentionc = UChat.get().getUCConfig().getColorStr("mention.color-template").replace("{mentioned-player}", p.getName());
                        mentionc = formatTags("", mentionc, sender, receiver, "", new UCChannel("mention"));

                        if (msg.contains(mentionc) || sender instanceof CommandSender && !UCPerms.hasPerm((CommandSender) sender, "chat.mention")) {
                            msg = msg.replaceAll("(?i)\\b" + p.getName() + "\\b", p.getName());
                            continue;
                        }

                        for (Sound sound : Sound.values()) {
                            if (StringUtils.containsIgnoreCase(sound.toString(), UChat.get().getUCConfig().getString("mention.playsound"))) {
                                p.playSound(p.getLocation(), sound, 1F, 1F);
                                break;
                            }
                        }
                        msg = msg.replace(mentionc, p.getName());
                        msg = msg.replaceAll("(?i)\\b" + p.getName() + "\\b", mentionc);
                    } else {
                        msg = msg.replaceAll("(?i)\\b" + p.getName() + "\\b", p.getName());
                    }
                }
            }
        }
        return msg;
    }

    @SuppressWarnings("deprecation")
    public static String formatTags(String tag, String text, Object cmdSender, Object receiver, String msg, UCChannel ch) {
        if (receiver instanceof CommandSender && tag.equals("message")) {
            text = text.replace("{message}", mention(cmdSender, (CommandSender) receiver, msg));
            if (UChat.get().getUCConfig().getBoolean("general.item-hand.enable")) {
                text = text.replace(UChat.get().getUCConfig().getString("general.item-hand.placeholder"), formatTags("", ChatColor.translateAlternateColorCodes('&', UChat.get().getUCConfig().getString("general.item-hand.format")), cmdSender, receiver, msg, ch));
            }
        } else {
            text = text.replace("{message}", msg);
        }
        if (tag.equals("message") && !UChat.get().getUCConfig().getBoolean("general.enable-tags-on-messages")) {
            return text;
        }
        text = text.replace("{ch-color}", ch.getColor())
                .replace("{ch-name}", ch.getName())
                .replace("{ch-alias}", ch.getAlias());

        if (UChat.get().getUCConfig().getBoolean("jedis.enable") && ch.useJedis()) {
            text = text.replace("{jedis-id}", UChat.get().getUCConfig().getString("jedis.server-id"));
        }

        if (ch.isBungee()) {
            text = text.replace("{bungee-id}", UChat.get().getUCConfig().getString("bungee.server-id"));
        }

        if (cmdSender instanceof CommandSender) {
            text = text.replace("{playername}", ((CommandSender) cmdSender).getName());
            if (receiver instanceof CommandSender)
                text = text.replace("{receivername}", ((CommandSender) receiver).getName());
            if (receiver instanceof String)
                text = text.replace("{receivername}", (String) receiver);
        } else {
            text = text.replace("{playername}", (String) cmdSender)
                    .replace("{receivername}", (String) receiver);
        }
        for (String repl : registeredReplacers.keySet()) {
            if (registeredReplacers.get(repl).equals(repl)) {
                text = text.replace(repl, "");
                continue;
            }
            text = text.replace(repl, registeredReplacers.get(repl));
        }
        StringBuilder def = new StringBuilder();
        for (int i = 0; i < defFormat.length; i++) {
            text = text.replace("{default-format-" + i + "}", defFormat[i]);
            def.append(" ").append(defFormat[i]);
        }
        if (def.length() > 0) {
            text = text.replace("{default-format-full}", def.substring(1));
        }

        if (text.contains("{time-now}")) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            text = text.replace("{time-now}", sdf.format(cal.getTime()));
        }

        if (cmdSender instanceof Player) {
            Player sender = (Player) cmdSender;

            if (text.contains("{world}")) {
                String world = UChat.get().getUCConfig().getString("general.world-names." + sender.getWorld().getName());
                text = text.replace("{world}", world != null ? world : sender.getWorld().getName());
            }

            text = text.replace("{nickname}", sender.getDisplayName());

            //replace item hand
            text = text.replace(UChat.get().getUCConfig().getString("general.item-hand.placeholder"), ChatColor.translateAlternateColorCodes('&', UChat.get().getUCConfig().getString("general.item-hand.format")));

            if (text.contains("{hand-") && !sender.getItemInHand().getType().equals(Material.AIR)) {
                ItemStack item = sender.getItemInHand();

                text = text.replace("{hand-durability}", String.valueOf(item.getDurability()));
                if (item.hasItemMeta()) {
                    ItemMeta meta = item.getItemMeta();
                    if (UCUtil.getBukkitVersion() >= 1112 && meta.hasLocalizedName()) {
                        text = text.replace("{hand-name}", item.getItemMeta().getLocalizedName());
                    } else if (meta.hasDisplayName()) {
                        text = text.replace("{hand-name}", item.getItemMeta().getDisplayName());
                    } else {
                        text = text.replace("{hand-name}", translateItem(item));
                    }
                    if (meta.hasLore()) {
                        StringBuilder lorestr = new StringBuilder();
                        for (String lore : meta.getLore()) {
                            lorestr.append("\n ").append(lore);
                        }
                        if (lorestr.length() >= 2) {
                            text = text.replace("{hand-lore}", lorestr.toString().substring(0, lorestr.length() - 1));
                        }
                    }
                    if (meta.hasEnchants()) {
                        StringBuilder str = new StringBuilder();
                        for (Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
                            str.append("\n ").append(enchant.getKey().getName()).append(": ").append(enchant.getValue());
                        }
                        if (str.length() >= 2) {
                            text = text.replace("{hand-enchants}", str.toString().substring(0, str.length() - 1));
                        }
                    }
                }
                text = text.replace("{hand-amount}", String.valueOf(item.getAmount()));
                text = text.replace("{hand-name}", translateItem(item));
                text = text.replace("{hand-type}", translateItem(item));
            } else {
                text = text.replace("{hand-name}", UChat.get().getLang().get("chat.emptyslot"));
                text = text.replace("{hand-type}", "Air");
            }

            if (UChat.get().getVaultChat() != null && (text.contains("-prefix") || text.contains("-suffix"))) {

                text = text.replace("{group-all-prefixes}", UCVaultCache.getVaultChat(sender).getPlayerPrefixes());
                text = text.replace("{group-all-suffixes}", UCVaultCache.getVaultChat(sender).getPlayerSuffix());

                text = text
                        .replace("{group-suffix}", UCVaultCache.getVaultChat(sender).getPlayerSuffix())
                        .replace("{group-prefix}", UCVaultCache.getVaultChat(sender).getPlayerPrefix());
                String[] pgs = UCVaultCache.getVaultPerms(sender).getPlayerGroups();
                if (pgs.length > 0) {
                    text = text
                            .replace("{player-groups-prefixes}", UCVaultCache.getVaultChat(sender).getGroupPrefixes())
                            .replace("{player-groups-suffixes}", UCVaultCache.getVaultChat(sender).getGroupSuffixes());
                }
            }
            if (UChat.get().getVaultEco() != null && text.contains("{balance}")) {
                text = text
                        .replace("{balance}", "" + UChat.get().getVaultEco().getBalance(sender, sender.getWorld().getName()));
            }
            if (UChat.get().getVaultPerms() != null && (text.contains("-group}") || text.contains("-groups}"))) {
                String[] pgs = UCVaultCache.getVaultPerms(sender).getPlayerGroups();
                if (pgs.length > 0) {
                    StringBuilder groups = new StringBuilder();
                    for (String g : pgs) {
                        groups.append(g).append(",");
                    }
                    text = text.replace("{player-groups}", groups.toString().substring(0, groups.length() - 1));

                }
                String primGroup = UCVaultCache.getVaultPerms(sender).getPrimaryGroup();
                String group = UChat.get().getUCConfig().getString("general.group-names." + primGroup);
                text = text.replace("{prim-group}", group.isEmpty() ? primGroup : group);

            }
            if (text.contains("{clan-") && UChat.SClans) {
                ClanPlayer cp = UChat.sc.getClanManager().getClanPlayer(sender.getUniqueId());
                SettingsManager scm = UChat.sc.getSettingsManager();
                if (cp != null) {
                    String fulltag =
                            scm.getTagBracketColor()
                                    + scm.getTagBracketLeft()
                                    + scm.getTagDefaultColor()
                                    + cp.getClan().getColorTag()
                                    + scm.getTagBracketColor()
                                    + scm.getTagBracketRight()
                                    + scm.getTagSeparatorColor()
                                    + scm.getTagSeparator();

                    text = text
                            .replace("{clan-tag}", cp.getTag())
                            .replace("{clan-fulltag}", fulltag)
                            .replace("{clan-name}", checkEmpty(cp.getClan().getName()))
                            .replace("{clan-kdr}", "" + cp.getKDR())
                            .replace("{clan-rank}", checkEmpty(cp.getRank()))
                            .replace("{clan-totalkdr}", "" + cp.getClan().getTotalKDR())
                            .replace("{clan-deaths}", "" + cp.getDeaths())
                            .replace("{clan-isleader}", "" + cp.isLeader())
                            .replace("{clan-totalkdr}", "" + cp.getClan().getTotalKDR())
                            .replace("{clan-ctag}", cp.getClan().getColorTag());
                }
            }
            if (UChat.Factions) {
                text = UCFactionsHook.formatFac(text, sender, receiver);
            }
            if (text.contains("{marry-")) {
                if (UChat.MarryReloded && sender.hasMetadata("marriedTo")) {
                    String partner = sender.getMetadata("marriedTo").get(0).asString();
                    String prefix = UChat.mapi.getBukkitConfig("config.yml").getString("chat.status-format");
                    UChat.mapi.getMPlayer(sender.getUniqueId()).getGender().getChatPrefix();
                    prefix = prefix
                            .replace("{icon:male}", "♂")
                            .replace("{icon:female}", "♀")
                            .replace("{icon:genderless}", "⚤")
                            .replace("{icon:heart}", "❤");
                    String gender = UChat.mapi.getMPlayer(sender.getUniqueId()).getGender().getChatPrefix();
                    gender = gender
                            .replace("{icon:male}", "♂")
                            .replace("{icon:female}", "♀")
                            .replace("{icon:genderless}", "⚤")
                            .replace("{icon:heart}", "❤");
                    text = text
                            .replace("{marry-partner}", partner)
                            .replace("{marry-prefix}", prefix)
                            .replace("{marry-suffix}", gender);
                }
                if (UChat.MarryMasterV1) {
                    if (UChat.mm.HasPartner(sender)) {
                        text = text
                                .replace("{marry-partner}", UChat.mm.DB.GetPartner(sender))
                                .replace("{marry-prefix}", UChat.mm.config.GetPrefix().replace("<heart>", ChatColor.RED + "❤" + ChatColor.WHITE))
                                .replace("{marry-suffix}", UChat.mm.config.GetSuffix().replace("<heart>", ChatColor.RED + "❤" + ChatColor.WHITE));
                    }
                }
	            if (UChat.MarryMasterV2) {
		            if (UChat.mm2.getPlayerData(sender).isMarried() && UChat.mm2.getPlayerData(sender).getPartner() != null) {
			            text = text.replace("{marry-partner}", UChat.mm2.getPlayerData(sender).getPartner().getName())
					            .replace("{marry-prefix}", UChat.mm2.getPrefixSuffixFormatter().formatPrefix(UChat.mm2.getPlayerData(sender).getMarriageData(UChat.mm2.getPlayerData(sender).getPartner()), UChat.mm2.getPlayerData(sender).getPartner()))
					            .replace("{marry-suffix}", UChat.mm2.getPrefixSuffixFormatter().formatSuffix(UChat.mm2.getPlayerData(sender).getMarriageData(UChat.mm2.getPlayerData(sender).getPartner()), UChat.mm2.getPlayerData(sender).getPartner()));
		            }
	            }
            }

            if (UChat.PlaceHolderAPI) {
                if (receiver instanceof Player && UChat.get().isRelation()) {
                    while (text.contains("%rel_")) {
                        text = PlaceholderAPI.setRelationalPlaceholders(sender, (Player) receiver, text);
                    }
                }
                text = PlaceholderAPI.setPlaceholders(sender, text);
                if (tag.equals("message")) {
                    text = composeColor(sender, text);
                }
            }
        }

        if (cmdSender instanceof CommandSender) {
            text = text.replace("{nickname}", UChat.get().getUCConfig().getString("general.console-tag").replace("{console}", ((CommandSender) cmdSender).getName()));
        } else {
            text = text.replace("{nickname}", UChat.get().getUCConfig().getString("general.console-tag").replace("{console}", (String) cmdSender));
        }

        //colorize tags (not message)
        if (!tag.equals("message")) {
            text = ChatColor.translateAlternateColorCodes('&', text);
        }

        //remove blank items
        text = text.replaceAll("\\{.*\\}", "");

        //remove remain PlaceholderAPI
        if (UChat.get().getUCConfig().getBoolean("general.remove-unnused-placeholderapi")) {
            text = text.replaceAll("\\%.*\\%", "");
        }

        if (!tag.equals("message")) {
            for (String rpl : UChat.get().getUCConfig().getStringList("general.remove-from-chat")) {
                text = text.replace(ChatColor.translateAlternateColorCodes('&', rpl), "");
            }
        }
        if (text.equals(" ") || text.equals("  ")) {
            text = "";
        }
        return text;
    }

    private static String checkEmpty(String tag) {
        if (tag.length() <= 0) {
            return UChat.get().getLang().get("tag.notset");
        }
        return tag;
    }

    private static String translateItem(ItemStack itemStack) {
        return UChat.tapi == null ? UCUtil.capitalize(itemStack.getType().toString()) : UChat.tapi.translateItem(itemStack.getType(), "en-us", true);
    }
}

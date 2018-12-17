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

package br.net.fabiozumbi12.UltimateChat.Sponge;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.PostFormatChatMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCLogger.timingType;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import me.rojo8399.placeholderapi.PlaceholderService;
import nl.riebie.mcclans.api.ClanPlayer;
import nl.riebie.mcclans.api.ClanService;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UCMessages {

    private static HashMap<String, String> registeredReplacers = new HashMap<>();
    private static String[] defFormat = new String[0];

    public static MutableMessageChannel sendFancyMessage(String[] format, Text msg, UCChannel channel, CommandSource sender, CommandSource tellReceiver) {
        //Execute listener:
        HashMap<String, String> tags = new HashMap<>();
        for (String str : UChat.get().getConfig().root().general.custom_tags) {
            tags.put(str, str);
        }
        SendChannelMessageEvent event = new SendChannelMessageEvent(tags, format, sender, channel, msg, true);
        Sponge.getEventManager().post(event);
        if (event.isCancelled()) {
            return null;
        }

        registeredReplacers = event.getResgisteredTags();
        defFormat = event.getDefFormat();

        String evmsg = event.getMessage().toPlain();

        //send to event
        MutableMessageChannel msgCh = MessageChannel.TO_CONSOLE.asMutable();

        evmsg = UCChatProtection.filterChatMessage(sender, evmsg, event.getChannel());
        if (evmsg == null) {
            return null;
        }

        HashMap<CommandSource, Text> msgPlayers = new HashMap<>();
        evmsg = composeColor(sender, evmsg);

        Text srcText = Text.builder(event.getMessage(), evmsg).build();

        if (event.getChannel() != null) {

            UCChannel ch = event.getChannel();

            if (sender instanceof Player && !ch.availableWorlds().isEmpty() && !ch.availableInWorld(((Player) sender).getWorld())) {
                UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
                return null;
            }

            if (!UChat.get().getPerms().channelWritePerm(sender, ch)) {
                UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
                return null;
            }

            if (!UChat.get().getPerms().hasPerm(sender, "bypass.cost") && UChat.get().getEco() != null && sender instanceof Player && ch.getCost() > 0) {
                UniqueAccount acc = UChat.get().getEco().getOrCreateAccount(((Player) sender).getUniqueId()).get();
                if (acc.getBalance(UChat.get().getEco().getDefaultCurrency()).doubleValue() < ch.getCost()) {
                    sender.sendMessage(UCUtil.toText(UChat.get().getLang().get("channel.cost").replace("{value}", "" + ch.getCost())));
                    return null;
                } else {
                    acc.withdraw(UChat.get().getEco().getDefaultCurrency(), BigDecimal.valueOf(ch.getCost()), UChat.get().getVHelper().getCause(UChat.get().instance()));
                }
            }

            int noWorldReceived = 0;
            int vanish = 0;
            List<Player> receivers = new ArrayList<>();

            //put sender
            msgPlayers.put(sender, sendMessage(sender, sender, srcText, ch, false));
            msgCh.addMember(sender);

            if (ch.getDistance() > 0 && sender instanceof Player) {
                for (Player play : Sponge.getServer().getOnlinePlayers().stream().filter(p -> p.getWorld().equals(((Player) sender).getWorld())
                        && p.getLocation().getPosition().distance(((Player) sender).getLocation().getPosition()) <= ch.getDistance())
                        .collect(Collectors.toList())) {
                    if (UChat.get().getPerms().channelReadPerm(play, ch)) {
                        if (sender.equals(play)) {
                            continue;
                        }
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
                        if (!((Player) sender).canSee(play)) {
                            vanish++;
                        } else {
                            noWorldReceived++;
                        }
                        if (!ch.neeFocus() || ch.isMember(play)) {
                            msgPlayers.put(play, sendMessage(sender, play, srcText, ch, false));
                            receivers.add(play);
                            msgCh.addMember(play);
                        }
                    }
                }
            } else {
                for (Player receiver : Sponge.getServer().getOnlinePlayers()) {
                    if (receiver.equals(sender) || !UChat.get().getPerms().channelReadPerm(receiver, ch) || (!ch.crossWorlds() && (sender instanceof Player && !receiver.getWorld().equals(((Player) sender).getWorld())))) {
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
                    if (sender instanceof Player && !((Player) sender).canSee(receiver)) {
                        vanish++;
                    } else {
                        noWorldReceived++;
                    }
                    if (!ch.neeFocus() || ch.isMember(receiver)) {
                        msgPlayers.put(receiver, sendMessage(sender, receiver, srcText, ch, false));
                        receivers.add(receiver);
                        msgCh.addMember(receiver);
                    }
                }
            }

            //chat spy
            if (!sender.hasPermission("uchat.chat-spy.bypass")) {
                for (Player receiver : Sponge.getServer().getOnlinePlayers()) {
                    if (!receiver.equals(sender) && !receivers.contains(receiver) && !receivers.contains(sender) &&
                            UChat.get().isSpy.contains(receiver.getName()) && UChat.get().getPerms().hasSpyPerm(receiver, ch.getName())) {
                        String spyformat = UChat.get().getConfig().root().general.spy_format;
                        spyformat = spyformat.replace("{output}", UCUtil.stripColor(sendMessage(sender, receiver, srcText, ch, true).toPlain()));
                        receiver.sendMessage(UCUtil.toText(spyformat));
                    }
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
                return null;
            }

            channel = new UCChannel("tell");

            //send spy
            if (!sender.hasPermission("uchat.chat-spy.bypass")) {
                for (Player receiver : Sponge.getServer().getOnlinePlayers()) {
                    if (!receiver.equals(tellReceiver) && !receiver.equals(sender) &&
                            UChat.get().isSpy.contains(receiver.getName()) && UChat.get().getPerms().hasSpyPerm(receiver, "private")) {
                        String spyformat = UChat.get().getConfig().root().general.spy_format;
                        if (isIgnoringPlayers(tellReceiver.getName(), sender.getName())) {
                            spyformat = UChat.get().getLang().get("chat.ignored") + spyformat;
                        }
                        spyformat = spyformat.replace("{output}", UCUtil.stripColor(sendMessage(sender, tellReceiver, srcText, channel, true).toPlain()));
                        receiver.sendMessage(UCUtil.toText(spyformat));
                    }
                }
            }

            Text to = sendMessage(sender, tellReceiver, srcText, channel, false);
            msgPlayers.put(tellReceiver, to);
            msgPlayers.put(sender, to);
            if (isIgnoringPlayers(tellReceiver.getName(), sender.getName())) {
                to = Text.of(UCUtil.toText(UChat.get().getLang().get("chat.ignored")), to);
            }

            msgPlayers.put(Sponge.getServer().getConsole(), to);
        }

        if (!msgPlayers.keySet().contains(Sponge.getServer().getConsole())) {
            msgPlayers.put(Sponge.getServer().getConsole(), msgPlayers.values().stream().findAny().get());
        }
        PostFormatChatMessageEvent postEvent = new PostFormatChatMessageEvent(sender, msgPlayers, channel);
        Sponge.getEventManager().post(postEvent);
        if (postEvent.isCancelled()) {
            return null;
        }

        msgPlayers.forEach((send, text) -> {
            UChat.get().getLogger().timings(timingType.END, "UCMessages#send()|before send");
            send.sendMessage(text);
            UChat.get().getLogger().timings(timingType.END, "UCMessages#send()|after send");
        });

        //send to jedis
        if (channel != null && !channel.isTell() && UChat.get().getJedis() != null) {
            UChat.get().getJedis().sendMessage(channel.getName().toLowerCase(), msgPlayers.get(sender));
        }

        //send to jda
        if (channel != null && UChat.get().getUCJDA() != null) {
            if (channel.isTell()) {
                UChat.get().getUCJDA().sendTellToDiscord(msgPlayers.get(sender).toPlain());
            } else {
                UChat.get().getUCJDA().sendToDiscord(sender, evmsg, channel);
            }
        }

        return msgCh;
    }

    private static String composeColor(CommandSource sender, String evmsg) {
        if (sender instanceof Player) {
            Pattern mat1 = Pattern.compile("(?i)&([A-Fa-f0-9Rr])");
            Pattern mat2 = Pattern.compile("(?i)&([L-Ol-o])");
            Pattern mat3 = Pattern.compile("(?i)&([Kk])");

            if (!UChat.get().getPerms().hasPerm(sender, "chat.color")) {
                while (mat1.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat1.pattern(), "");
                }
            }
            if (!UChat.get().getPerms().hasPerm(sender, "chat.color.formats")) {
                while (mat2.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat2.pattern(), "");
                }
            }
            if (!UChat.get().getPerms().hasPerm(sender, "chat.color.magic")) {
                while (mat3.matcher(evmsg).find()) {
                    evmsg = evmsg.replaceAll(mat3.pattern(), "");
                }
            }
            if (!UChat.get().getPerms().hasPerm(sender, "chat.newline")) {
                evmsg = evmsg.replace("/n", " ");
            } else {
                evmsg = evmsg.replace("/n", "\n");
            }
        }
        evmsg = evmsg.replace("§", "&");
        return evmsg;
    }

    static boolean isIgnoringPlayers(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        return list.contains(victim);
    }

    static void ignorePlayer(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        list.add(victim);
        UChat.get().ignoringPlayer.put(p, list);
    }

    static void unIgnorePlayer(String p, String victim) {
        List<String> list = new ArrayList<>();
        if (UChat.get().ignoringPlayer.containsKey(p)) {
            list.addAll(UChat.get().ignoringPlayer.get(p));
        }
        list.remove(victim);
        UChat.get().ignoringPlayer.put(p, list);
    }

    public static Text sendMessage(CommandSource sender, Object receiver, Text srcTxt, UCChannel ch, boolean isSpy) {
        Builder formatter = Text.builder();
        Builder playername = Text.builder();
        Builder message = Text.builder();

        if (srcTxt.getClickAction().isPresent()) {
            message.onClick(srcTxt.getClickAction().get());
        }
        if (srcTxt.getHoverAction().isPresent()) {
            message.onHover(srcTxt.getHoverAction().get());
        }
        if (srcTxt.getShiftClickAction().isPresent()) {
            message.onShiftClick(srcTxt.getShiftClickAction().get());
        }

        String msg = srcTxt.toPlain();

        if (!ch.getName().equals("tell")) {
            String[] defaultBuilder = UChat.get().getConfig().getDefBuilder();
            if (ch.useOwnBuilder()) {
                defaultBuilder = ch.getBuilder();
            }

            String lastColor = "";
            for (String tag : defaultBuilder) {
                Builder tagBuilder = Text.builder();

                Builder msgBuilder;

                if (UChat.get().getConfig().root().tags.get(tag) == null) {
                    tagBuilder.append(Text.of(tag));
                    continue;
                }

                String format = lastColor + UChat.get().getConfig().root().tags.get(tag).format;
                String perm = UChat.get().getConfig().root().tags.get(tag).permission;
                String execute = UChat.get().getConfig().root().tags.get(tag).click_cmd;
                String url = UChat.get().getConfig().root().tags.get(tag).click_url;
                String suggest = UChat.get().getConfig().root().tags.get(tag).suggest;
                List<String> messages = UChat.get().getConfig().root().tags.get(tag).hover_messages;
                List<String> showWorlds = UChat.get().getConfig().root().tags.get(tag).show_in_worlds;
                List<String> hideWorlds = UChat.get().getConfig().root().tags.get(tag).hide_in_worlds;

                //check perm
                if (perm != null && !perm.isEmpty() && !sender.hasPermission(perm)) {
                    continue;
                }

                //check show or hide in world
                if (sender instanceof Player) {
                    if (showWorlds != null && !showWorlds.contains(((Player) sender).getWorld().getName())) {
                        continue;
                    }
                    if (hideWorlds != null && hideWorlds.contains(((Player) sender).getWorld().getName())) {
                        continue;
                    }
                }

                StringBuilder tooltip = new StringBuilder();
                if (messages != null && !messages.isEmpty()) {
                    for (String tp : messages) {
                        tooltip.append("\n").append(tp.replace("/n", "\n"));
                    }
                    if (tooltip.length() > 2) {
                        tooltip = new StringBuilder(tooltip.substring(1));
                    }
                }

                if (suggest != null && !suggest.isEmpty()) {
                    tagBuilder.onClick(TextActions.suggestCommand(formatTags(tag, "/" + suggest, sender, receiver, msg, ch)));
                }

                if (execute != null && !execute.isEmpty()) {
                    tagBuilder.onClick(TextActions.runCommand(formatTags(tag, "/" + execute, sender, receiver, msg, ch)));
                }


                if (url != null && !url.isEmpty()) {
                    try {
                        tagBuilder.onClick(TextActions.openUrl(new URL(formatTags(tag, url, sender, receiver, msg, ch))));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                if (!tag.equals("message") || UChat.get().getPerms().hasPerm(sender, "chat.click-urls")) {
                    for (String arg : msg.split(" ")) {
                        try {
                            tagBuilder.onClick(TextActions.openUrl(new URL(formatTags(tag, arg, sender, receiver, msg, ch))));
                            tagBuilder.onHover(TextActions.showText(UCUtil.toText(formatTags(tag, UChat.get().getConfig().root().general.URL_template.replace("{url}", arg), sender, receiver, msg, ch))));
                        } catch (MalformedURLException ignored) {
                        }
                    }
                }

                msgBuilder = tagBuilder;

                if (tag.equals("message") && (!msg.equals(mention(sender, receiver, msg)) || msg.contains(UChat.get().getConfig().root().general.item_hand.placeholder))) {
                    tooltip = new StringBuilder(formatTags("", tooltip.toString(), sender, receiver, msg, ch));
                    format = formatTags(tag, format, sender, receiver, msg, ch);

                    lastColor = getLastColor(format);

                    //append text
                    msgBuilder.append(UCUtil.toText(format));

                    if (UChat.get().getConfig().root().general.item_hand.enable && msg.contains(UChat.get().getConfig().root().general.item_hand.placeholder) && sender instanceof Player) {
                        ItemStack hand = UChat.get().getVHelper().getItemInHand(((Player) sender));
                        msgBuilder.onHover(TextActions.showItem(hand.createSnapshot()));
                    } else if (UChat.get().getConfig().root().mention.hover_message.length() > 0 && StringUtils.containsIgnoreCase(msg, ((CommandSource) receiver).getName())) {
                        tooltip = new StringBuilder(formatTags("", UChat.get().getConfig().root().mention.hover_message, sender, receiver, msg, ch));
                        msgBuilder.onHover(TextActions.showText(UCUtil.toText(tooltip.toString())));
                    } else if (tooltip.length() > 0) {
                        msgBuilder.onHover(TextActions.showText(UCUtil.toText(tooltip.toString())));
                    }
                    msgBuilder.applyTo(message);
                } else {
                    format = formatTags(tag, format, sender, receiver, msg, ch);
                    tooltip = new StringBuilder(formatTags("", tooltip.toString(), sender, receiver, msg, ch));

                    lastColor = getLastColor(format);

                    if (tooltip.length() > 0) {
                        tagBuilder.append(UCUtil.toText(format))
                                .onHover(TextActions.showText(UCUtil.toText(tooltip.toString())));
                    } else {
                        tagBuilder.append(UCUtil.toText(format));
                    }

                    if (tag.equals("{playername}") || tag.equals("{nickname}")) {
                        tagBuilder.applyTo(playername);
                    } else {
                        tagBuilder.applyTo(formatter);
                    }
                }
            }
        } else {
            //if tell
            String prefix = UChat.get().getConfig().root().tell.prefix;
            String format = UChat.get().getConfig().root().tell.format;
            List<String> messages = UChat.get().getConfig().root().tell.hover_messages;

            StringBuilder tooltip = new StringBuilder();
            if (!messages.isEmpty() && messages.get(0).length() > 1) {
                for (String tp : messages) {
                    tooltip.append("\n").append(tp.replace("/n", "\n"));
                }
                if (tooltip.length() > 2) {
                    tooltip = new StringBuilder(tooltip.substring(1));
                }
            }

            prefix = formatTags("", prefix, sender, receiver, msg, ch);
            format = formatTags("tell", format, sender, receiver, msg, ch);
            tooltip = new StringBuilder(formatTags("", tooltip.toString(), sender, receiver, msg, ch));

            if (tooltip.length() > 0) {
                formatter.append(UCUtil.toText(prefix))
                        .onHover(TextActions.showText(UCUtil.toText(tooltip.toString())));
            } else {
                formatter.append(UCUtil.toText(prefix));
            }
            message.append(UCUtil.toText(format));
        }

        playername.applyTo(formatter);
        message.applyTo(formatter);
        return formatter.build();
    }

    private static String getLastColor(String str) {
        if (str.length() > 2) {
            str = str.substring(str.length() - 2);
            if (str.matches("(&([A-Fa-fK-Ok-oRr0-9]))")) {
                return str;
            }
        }
        return "";
    }

    private static String mention(Object sender, Object receiver, String msg) {
        if (UChat.get().getConfig().root().mention.enable) {
            for (Player p : Sponge.getServer().getOnlinePlayers()) {
                if (!sender.equals(p) && Arrays.stream(msg.split(" ")).anyMatch(p.getName()::equalsIgnoreCase)) {
                    if (receiver instanceof Player && receiver.equals(p)) {

                        String mentionc = UChat.get().getConfig().root().mention.color_template.replace("{mentioned-player}", p.getName());
                        mentionc = formatTags("", mentionc, sender, p, "", new UCChannel("mention"));

                        if (msg.contains(mentionc) || sender instanceof CommandSource && !UChat.get().getPerms().hasPerm((CommandSource) sender, "chat.mention")) {
                            msg = msg.replaceAll("(?i)\\b" + p.getName() + "\\b", p.getName());
                            continue;
                        }

                        Optional<SoundType> sound = Sponge.getRegistry().getType(SoundType.class, UChat.get().getConfig().root().mention.playsound);
                        if (sound.isPresent() && !msg.contains(mentionc)) {
                            p.playSound(sound.get(), p.getLocation().getPosition(), 1, 1);
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

    public static String formatTags(String tag, String text, Object cmdSender, Object receiver, String msg, UCChannel ch) {
        if (receiver instanceof CommandSource && tag.equals("message")) {
            text = text.replace("{message}", mention(cmdSender, receiver, msg));
            if (UChat.get().getConfig().root().general.item_hand.enable) {
                text = text.replace(UChat.get().getConfig().root().general.item_hand.placeholder, formatTags("", UCUtil.toColor(UChat.get().getConfig().root().general.item_hand.format), cmdSender, receiver, msg, ch));
            }
        } else {
            text = text.replace("{message}", msg);
        }
        if (tag.equals("message") && !UChat.get().getConfig().root().general.enable_tags_on_messages) {
            return text;
        }
        text = text.replace("{ch-color}", ch.getColor())
                .replace("{ch-name}", ch.getName())
                .replace("{ch-alias}", ch.getAlias())
                .replace("{ch-nickname}", ch.getNickName());

        if (UChat.get().getConfig().root().jedis.enable && ch.useJedis()) {
            text = text.replace("{jedis-id}", UChat.get().getConfig().root().jedis.server_id);
        }

        if (cmdSender instanceof CommandSource) {
            text = text.replace("{playername}", ((CommandSource) cmdSender).getName());
            if (receiver instanceof CommandSource) {
                text = text.replace("{receivername}", ((CommandSource) receiver).getName());
            }
            if (receiver instanceof String) {
                text = text.replace("{receivername}", receiver.toString());
            }
        } else {
            text = text.replace("{playername}", (String) cmdSender);
            if (receiver instanceof CommandSource)
                text = text.replace("{receivername}", ((CommandSource) receiver).getName());
            if (receiver instanceof String)
                text = text.replace("{receivername}", (String) receiver);
        }
        for (String repl : registeredReplacers.keySet()) {
            if (registeredReplacers.get(repl).equals(repl)) {
                text = text.replace(repl, "");
                continue;
            }
            text = text.replace(repl, registeredReplacers.get(repl));
        }

        if (defFormat.length > 0) {
            StringBuilder all = new StringBuilder();
            for (int i = 0; i < defFormat.length; i++) {
                if (i == 0) text = text.replace("{chat_header}", defFormat[i]);
                if (i == 1) text = text.replace("{chat_body}", defFormat[i]);
                if (i == 2) text = text.replace("{chat_footer}", defFormat[i]);

                all.append(defFormat[i]);
            }
            text = text.replace("{chat_all}", all.toString());
        }

        if (text.contains("{time-now}")) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            text = text.replace("{time-now}", sdf.format(cal.getTime()));
        }

        if (cmdSender instanceof Player) {
            Player sender = (Player) cmdSender;

            if (text.contains("{nickname}") && sender.get(Keys.DISPLAY_NAME).isPresent()) {
                String nick = sender.get(Keys.DISPLAY_NAME).get().toPlain();

                if (!nick.equals(sender.getName())) {
                    text = text.replace("{nickname}", nick);
                    text = text.replace("{nick-symbol}", UChat.get().getConfig().root().general.nick_symbol);
                } else if (Sponge.getPluginManager().getPlugin("nucleus").isPresent() && NucleusAPI.getNicknameService().isPresent()) {
                    Optional<Text> opNick = NucleusAPI.getNicknameService().get().getNickname(sender);
                    if (opNick.isPresent()) {
                        nick = TextSerializers.FORMATTING_CODE.serialize(opNick.get());
                        text = text.replace("{nick-symbol}", UChat.get().getConfig().root().general.nick_symbol);
                    }
                }
                text = text.replace("{nickname}", nick);
            }

            //replace item hand
            text = text.replace(UChat.get().getConfig().root().general.item_hand.placeholder, UCUtil.toColor(UChat.get().getConfig().root().general.item_hand.format));
            ItemStack item;

            item = UChat.get().getVHelper().getItemInHand(sender);

            if (text.contains("{hand-") && !item.isEmpty()) {
                text = text
                        .replace("{hand-durability}", item.get(Keys.ITEM_DURABILITY).isPresent() ? String.valueOf(item.get(Keys.ITEM_DURABILITY).get()) : "")
                        .replace("{hand-name}", UChat.get().getVHelper().getItemName(item).getTranslation().get());
                if (item.get(Keys.ITEM_LORE).isPresent()) {
                    StringBuilder lorestr = new StringBuilder();
                    for (Text line : item.get(Keys.ITEM_LORE).get()) {
                        lorestr.append("\n ").append(line.toPlain());
                    }
                    if (lorestr.length() >= 2) {
                        text = text.replace("{hand-lore}", lorestr.toString().substring(0, lorestr.length() - 1));
                    }
                }
                if (item.get(Keys.ITEM_ENCHANTMENTS).isPresent()) {
                    StringBuilder str = new StringBuilder();
                    str.append(UChat.get().getVHelper().getEnchantments(str, item));
                    if (str.length() >= 2) {
                        text = text.replace("{hand-enchants}", str.toString().substring(0, str.length() - 1));
                    }
                }
                text = text.replace("{hand-amount}", String.valueOf(item.getQuantity()));
                text = text.replace("{hand-name}", UChat.get().getVHelper().getItemName(item).getName());
                text = text.replace("{hand-type}", UChat.get().getVHelper().getItemName(item).getTranslation().get());
            } else {
                text = text.replace("{hand-name}", UChat.get().getLang().get("chat.emptyslot"));
                text = text.replace("{hand-type}", "Air");
            }

            if (text.contains("{world}")) {
                String world = sender.getWorld().getName();
                text = text.replace("{world}", UChat.get().getConfig().root().general.world_names.getOrDefault(world, world));
            }

            if (text.contains("{balance}") && UChat.get().getEco() != null) {
                UniqueAccount acc = UChat.get().getEco().getOrCreateAccount(sender.getUniqueId()).get();
                text = text
                        .replace("{balance}", "" + acc.getBalance(UChat.get().getEco().getDefaultCurrency()).intValue());
            }

            //parse permissions options
            if (text.contains("option_") || text.contains("player_option_")) {
                try {
                    //all groups
                    StringBuilder groupPrefs = new StringBuilder();
                    StringBuilder groupSuffs = new StringBuilder();

                    for (Subject group : UChat.get().getPerms().getPlayerGroups(sender).values()) {
                        if (UChat.get().getConfig().root().general.dont_show_groups.contains(group.getIdentifier()))
                            continue;

                        if (group.getOption("prefix").isPresent()) {
                            groupPrefs.append(group.getOption("prefix").get());
                        }
                        if (group.getOption("suffix").isPresent()) {
                            groupSuffs.append(group.getOption("suffix").get());
                        }
                    }
                    text = text.replace("{option_all_prefixes}", groupPrefs.toString());
                    text = text.replace("{option_all_suffixes}", groupSuffs.toString());

                    //player options
                    Pattern pp = Pattern.compile("\\{player_option_(.+?)\\}");
                    Matcher pm = pp.matcher(text);

                    while (pm.find()) {
                        if (sender.getOption(pm.group(1)).isPresent() && !text.contains(sender.getOption(pm.group(1)).get())) {
                            text = text.replace("{player_option_" + pm.group(1) + "}", sender.getOption(pm.group(1)).get());
                            pm = pp.matcher(text);
                        }
                    }

                    //group options
                    Subject sub = UChat.get().getPerms().getGroupAndTag(sender);
                    if (sub != null) {

                        text = text.replace("{option_group}", UChat.get().getConfig().root().general.group_names.getOrDefault(sub.getIdentifier(), sub.getIdentifier()));

                        if (sub.getOption("display_name").isPresent()) {
                            text = text.replace("{option_display_name}", sub.getOption("display_name").get());
                        } else {
                            text = text.replace("{option_display_name}", UChat.get().getConfig().root().general.group_names.getOrDefault(sub.getIdentifier(), sub.getIdentifier()));
                        }

                        Pattern gp = Pattern.compile("\\{option_(.+?)\\}");
                        Matcher gm = gp.matcher(text);

                        while (gm.find()) {
                            String gmp = gm.group(1);
                            if (sub.getOption(gmp).isPresent() && !text.contains(sub.getOption(gmp).get())) {
                                text = text.replace("{option_" + gmp + "}", sub.getOption(gmp).get());
                                gm = gp.matcher(text);
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (text.contains("{clan_") && UChat.get().getConfig().root().hooks.MCClans.enable) {
                Optional<ClanService> clanServiceOpt = Sponge.getServiceManager().provide(ClanService.class);
                if (clanServiceOpt.isPresent()) {
                    ClanService clan = clanServiceOpt.get();
                    ClanPlayer cp = clan.getClanPlayer(sender.getUniqueId());
                    if (cp != null && cp.isMemberOfAClan()) {
                        text = text
                                .replace("{clan_name}", checkEmpty(cp.getClan().getName()))
                                .replace("{clan_tag}", cp.getClan().getTag())
                                .replace("{clan_tag_color}", TextSerializers.FORMATTING_CODE.serialize(cp.getClan().getTagColored()))
                                .replace("{clan_kdr}", "" + cp.getClan().getKDR())
                                .replace("{clan_player_rank}", checkEmpty(cp.getRank().getName()))
                                .replace("{clan_player_kdr}", "" + cp.getKillDeath().getKDR())
                                .replace("{clan_player_ffprotected}", String.valueOf(cp.isFfProtected()))
                                .replace("{clan_player_isowner}", String.valueOf(cp.getClan().getOwner().equals(cp)));
                    }
                }
            }

            if (Sponge.getPluginManager().getPlugin("placeholderapi").isPresent()) {
                Optional<PlaceholderService> phapiOpt = Sponge.getServiceManager().provide(PlaceholderService.class);
                Pattern p = Pattern.compile("\\%([^\\%]*)\\%");
                if (phapiOpt.isPresent() && p.matcher(text).find()) {
                    PlaceholderService phapi = phapiOpt.get();

                    //fix last color from string
                    String lastcolor = text.length() > 1 ? text.substring(text.length() - 2, text.length()) : "";
                    if (!Pattern.compile("(&([A-Fa-fK-Ok-oRr0-9]))").matcher(lastcolor).find()) lastcolor = "";

                    text = TextSerializers.FORMATTING_CODE.serialize(phapi.replacePlaceholders(text, sender, receiver, p)) + lastcolor;
                }
            }
        }

        text = text.replace("{option_suffix}", "&r: ");

        if (cmdSender instanceof CommandSource) {
            text = text.replace("{nickname}", UChat.get().getConfig().root().general.console_tag.replace("{console}", ((CommandSource) cmdSender).getName()));
        } else {
            text = text.replace("{nickname}", UChat.get().getConfig().root().general.console_tag.replace("{console}", (String) cmdSender));
        }

        //remove blank items
        text = text.replaceAll("\\{.*\\}", "");
        if (!tag.equals("message")) {
            for (String rpl : UChat.get().getConfig().root().general.remove_from_chat) {
                text = text.replace(UCUtil.toColor(rpl), "");
            }
        }
        if (text.equals(" ") || text.equals("  ")) {
            return text = "";
        }
        return text;
    }

    private static String checkEmpty(String tag) {
        if (tag.length() <= 0) {
            return UChat.get().getLang().get("tag.notset");
        }
        return tag;
    }

}

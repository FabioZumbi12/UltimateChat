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

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.PlayerChangeChannelEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.SendChannelMessageEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UCListener implements CommandExecutor, Listener, TabCompleter {


    private void addPlayerToChannel(UCChannel ch, Player p) {
        if (!UCPerms.channelReadPerm(p, ch) && !UCPerms.channelWritePerm(p, ch)) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
            return;
        }
        if (!ch.availableInWorld(p.getWorld())) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
            return;
        }
        if (!ch.canLock()) {
            UChat.get().getLang().sendMessage(p, "help.channels.send");
            return;
        }
        if (ch.isMember(p)) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
            return;
        }
        if (!ch.getPassword().isEmpty()) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.password").replace("{channel}", ch.getAlias()));
            return;
        }

        //listen change channel event
        PlayerChangeChannelEvent postEvent = new PlayerChangeChannelEvent(p, UChat.get().getPlayerChannel(p), ch);
        Bukkit.getPluginManager().callEvent(postEvent);
        if (postEvent.isCancelled()) {
            return;
        }
        ch.addMember(p);
        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
    }

    private void sendMessageToPlayer(UCChannel ch, Player p, String[] args, String msg) {
        if (UChat.get().mutes.contains(p.getName()) || ch.isMuted(p.getName())) {
            if (UChat.get().timeMute.containsKey(p.getName())) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.tempmuted").replace("{time}", String.valueOf(UChat.get().timeMute.get(p.getName()))));
            } else {
                UChat.get().getLang().sendMessage(p, "channel.muted");
            }
            return;
        }

        if (!UCPerms.channelWritePerm(p, ch)) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
            return;
        }

        if (!ch.getPassword().isEmpty() && !ch.isMember(p)) {
            if (args.length != 1 || !ch.getPassword().equals(args[0])) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.password").replace("{channel}", ch.getAlias()));
                return;
            }
            if (!UCPerms.hasPerm(p, "password")) {
                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.nopermission"));
                return;
            }

            //listen change channel event
            PlayerChangeChannelEvent postEvent = new PlayerChangeChannelEvent(p, UChat.get().getPlayerChannel(p), ch);
            Bukkit.getPluginManager().callEvent(postEvent);
            if (postEvent.isCancelled()) {
                return;
            }
            ch.addMember(p);
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
        }

        //run bukkit chat event
        Set<Player> pls = new HashSet<>(Bukkit.getOnlinePlayers());

        UChat.get().tempChannels.put(p.getName(), ch.getAlias());

        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, p, msg, pls);
        Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
            UChat.get().getUCLogger().timings(timingType.START, "UCListener#onCommand()|Fire AsyncPlayerChatEvent");
            UChat.get().getServer().getPluginManager().callEvent(event);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldLoadEvent e) {
        if (!UChat.get().getConfig().contains("general.default-channels.worlds." + e.getWorld().getName())) {
            UChat.get().getConfig().set("general.default-channels.worlds." + e.getWorld().getName() + ".channel", UChat.get().getConfig().contains("general.default-channels.default-channel"));
            UChat.get().getConfig().set("general.default-channels.worlds." + e.getWorld().getName() + ".force", false);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        UChat.get().getUCLogger().debug("onCommand - Label: " + label + " - CmdName: " + cmd.getName());

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload") && UCPerms.hasPermission(sender, "uchat.cmd.reload")) {
                UChat.get().reload();
                UChat.get().getLang().sendMessage(sender, "plugin.reloaded");
                return true;
            }
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (cmd.getName().equalsIgnoreCase("channel")) {
                if (args.length == 0) {
                    UCChannel ch = UChat.get().getChannel(label);
                    if (ch != null) {
                        addPlayerToChannel(ch, p);
                    } else {
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", label));
                    }
                    return true;
                }

                if (args.length >= 1) {
                    UCChannel ch = UChat.get().getChannel(label);
                    StringBuilder msgBuild = new StringBuilder();
                    for (String arg : args) {
                        msgBuild.append(" ").append(arg);
                    }
                    String msg = msgBuild.toString().substring(1);
                    if (ch != null) {
                        sendMessageToPlayer(ch, p, args, msg);
                        return true;
                    }
                }

                //if /ch or /channel
                if (label.equalsIgnoreCase("ch") || label.equalsIgnoreCase("channel")) {
                    if (args.length == 1) {
                        //ch list
                        if (args[0].equalsIgnoreCase("list")) {
                            UltimateFancy fancy = new UltimateFancy();
                            fancy.coloredText("&7------------------------------------------\n");
                            fancy.coloredText(UChat.get().getLang().get("help.channels.available").replace("{channels}", "")).next();
                            boolean first = true;
                            for (UCChannel ch : UChat.get().getChannels().values()) {
                                if (UCPerms.channelReadPerm(p, ch) && ch.availableInWorld(p.getWorld())) {
                                    if (first) {
                                        fancy.coloredText(ch.getColor() + ch.getName() + "&a");
                                        first = false;
                                    } else {
                                        fancy.coloredText(", " + ch.getColor() + ch.getName() + "&a");
                                    }
                                    fancy.hoverShowText(ch.getColor() + "Alias: " + ch.getAlias())
                                            .clickRunCmd("/" + ch.getAlias())
                                            .next();
                                }
                            }
                            fancy.coloredText("\n&7------------------------------------------").send(p);
                            return true;
                        }

                        UCChannel ch = UChat.get().getChannel(args[0]);
                        if (ch == null) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[0]));
                            return true;
                        }
                        if (!UCPerms.channelReadPerm(p, ch) && !UCPerms.channelWritePerm(p, ch)) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.nopermission").replace("{channel}", ch.getName()));
                            return true;
                        }
                        if (!ch.availableInWorld(p.getWorld())) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.notavailable").replace("{channel}", ch.getName()));
                            return true;
                        }
                        if (!ch.canLock()) {
                            UChat.get().getLang().sendMessage(p, "help.channels.send");
                            return true;
                        }
                        if (ch.isMember(p)) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyon").replace("{channel}", ch.getName()));
                            return true;
                        }
                        if (!ch.getPassword().isEmpty()) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.password").replace("{channel}", ch.getAlias()));
                            return true;
                        }

                        //listen change channel event
                        PlayerChangeChannelEvent postEvent = new PlayerChangeChannelEvent(p, UChat.get().getPlayerChannel(p), ch);
                        Bukkit.getPluginManager().callEvent(postEvent);
                        if (postEvent.isCancelled()) {
                            return true;
                        }
                        ch.addMember(p);
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", ch.getName()));
                        return true;
                    }
                }

                sendChannelHelp(p);
            }

            //Listen cmd chat/uchat
            if (cmd.getName().equalsIgnoreCase("uchat")) {
                if (args.length == 1) {

                    if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
                        sendHelp(sender);
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("msgtoggle")) {
                        if (!UCPerms.cmdPerm(p, "msgtoggle")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        if (UChat.get().msgTogglePlayers.contains(p.getName())) {
                            UChat.get().msgTogglePlayers.remove(p.getName());
                            UChat.get().getLang().sendMessage(p, "cmd.msgtoggle.enabled");
                        } else {
                            UChat.get().msgTogglePlayers.add(p.getName());
                            UChat.get().getLang().sendMessage(p, "cmd.msgtoggle.disabled");
                        }
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("clear")) {
                        if (!UCPerms.cmdPerm(p, "clear")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        for (int i = 0; i < 100; i++) {
                            if (!p.isOnline()) {
                                break;
                            }
                            UCUtil.performCommand(p, Bukkit.getConsoleSender(), "tellraw " + p.getName() + " {\"text\":\" \"}");
                        }
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.clear.cleared"));
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("clear-all")) {
                        if (!UCPerms.cmdPerm(p, "clear-all")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        for (Player play : Bukkit.getOnlinePlayers()) {
                            for (int i = 0; i < 100; i++) {
                                if (!play.isOnline()) {
                                    continue;
                                }
                                UCUtil.performCommand(play, Bukkit.getConsoleSender(), "tellraw " + play.getName() + " {\"text\":\" \"}");
                            }
                        }
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.clear.cleared"));
                        return true;
                    }

                    if (args[0].equalsIgnoreCase("spy")) {
                        if (!UCPerms.cmdPerm(p, "spy")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }

                        if (!UChat.get().isSpy.contains(p.getName())) {
                            UChat.get().isSpy.add(p.getName());
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.spy.enabled"));
                        } else {
                            UChat.get().isSpy.remove(p.getName());
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.spy.disabled"));
                        }
                        return true;
                    }
                }

                if (args.length == 2) {

                    //chat msgtoggle <player>
                    if (args[0].equalsIgnoreCase("msgtoggle")) {
                        if (!UCPerms.hasPerm(p, "msgtoggle.others")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        Player vict = Bukkit.getPlayer(args[1]);
                        if (vict != null) {
                            if (UChat.get().msgTogglePlayers.contains(vict.getName())) {
                                UChat.get().msgTogglePlayers.remove(vict.getName());
                                UChat.get().getLang().sendMessage(vict, "cmd.msgtoggle.enabled");
                            } else {
                                UChat.get().msgTogglePlayers.add(vict.getName());
                                UChat.get().getLang().sendMessage(vict, "cmd.msgtoggle.disabled");
                            }
                            return true;
                        }
                    }

                    //chat delchannel <channel-name>
                    if (args[0].equalsIgnoreCase("delchannel")) {
                        if (!UCPerms.cmdPerm(p, "delchannel")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        UCChannel ch = UChat.get().getChannel(args[1]);
                        if (ch == null) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[1]));
                            return true;
                        }
                        List<String> toAdd = new ArrayList<>(ch.getMembers());
                        toAdd.forEach(m -> {
                            UChat.get().getDefChannel(p.getWorld().getName()).addMember(m);

                            //listen change channel event
                            PlayerChangeChannelEvent postEvent = new PlayerChangeChannelEvent(p, null, UChat.get().getDefChannel(p.getWorld().getName()));
                            Bukkit.getPluginManager().callEvent(postEvent);
                        });

                        UChat.get().getUCConfig().delChannel(ch);
                        UChat.get().registerAliases();
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.delchannel.success").replace("{channel}", ch.getName()));
                        return true;
                    }

                    // chat ignore <channel/player>
                    if (args[0].equalsIgnoreCase("ignore")) {
                        UCChannel ch = UChat.get().getChannel(args[1]);
                        if (Bukkit.getPlayer(args[1]) != null) {
                            Player pi = Bukkit.getPlayer(args[1]);
                            if (!UCPerms.cmdPerm(p, "ignore.player")) {
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                                return true;
                            }
                            if (!UCPerms.canIgnore(sender, pi)) {
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
                                return true;
                            }
                            if (UCMessages.isIgnoringPlayers(p.getName(), pi.getName())) {
                                UCMessages.unIgnorePlayer(p.getName(), pi.getName());
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.unignoring").replace("{player}", pi.getName()));
                            } else {
                                UCMessages.ignorePlayer(p.getName(), pi.getName());
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("player.ignoring").replace("{player}", pi.getName()));
                            }
                            return true;
                        } else if (ch != null) {
                            if (!UCPerms.cmdPerm(p, "ignore.channel")) {
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                                return true;
                            }
                            if (!UCPerms.canIgnore(sender, ch)) {
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("chat.cantignore"));
                                return true;
                            }
                            if (ch.isIgnoring(p.getName())) {
                                ch.unIgnoreThis(p.getName());
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.notignoring").replace("{channel}", ch.getName()));
                            } else {
                                ch.ignoreThis(p.getName());
                                UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.ignoring").replace("{channel}", ch.getName()));
                            }
                            return true;
                        } else {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[1]));
                            return true;
                        }
                    }

                    //chat mute <player>
                    if (args[0].equalsIgnoreCase("mute")) {
                        if (!UCPerms.cmdPerm(p, "mute")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }

                        String pname = args[1];
                        if (Bukkit.getPlayer(args[1]) != null) {
                            pname = Bukkit.getPlayer(args[1]).getName();
                        }

                        if (UChat.get().mutes.contains(pname)) {
                            UChat.get().mutes.remove(pname);
                            UChat.get().unMuteInAllChannels(pname);
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.unmuted.all").replace("{player}", pname));
                            if (Bukkit.getPlayer(pname) != null) {
                                UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), "channel.player.unmuted.all");
                            }
                        } else {
                            UChat.get().mutes.add(pname);
                            UChat.get().muteInAllChannels(pname);
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.muted.all").replace("{player}", pname));
                            if (Bukkit.getPlayer(pname) != null) {
                                UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), "channel.player.muted.all");
                            }
                        }
                        return true;
                    }
                }

                if (args.length == 3) {
                    //chat tempmute <minutes> <player>
                    if (args[0].equalsIgnoreCase("tempmute")) {
                        if (!UCPerms.cmdPerm(p, "tempmute")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }

                        int time = 0;
                        try {
                            time = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            UChat.get().getLang().sendMessage(p, "cmd.tempmute.number");
                            return true;
                        }

                        String pname = args[2];

                        if (Bukkit.getPlayer(args[2]) != null) {
                            pname = Bukkit.getPlayer(args[2]).getName();
                        }

                        if (UChat.get().mutes.contains(pname)) {
                            UChat.get().getLang().sendMessage(p, "channel.already.muted");
                        } else {
                            UChat.get().mutes.add(pname);
                            UChat.get().muteInAllChannels(pname);
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.tempmuted.all").replace("{player}", pname).replace("{time}", String.valueOf(time)));
                            if (Bukkit.getPlayer(pname) != null) {
                                UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[2]), UChat.get().getLang().get("channel.player.tempmuted.all").replace("{time}", String.valueOf(time)));
                            }

                            //mute counter
                            new MuteCountDown(pname, time).runTaskTimer(UChat.get(), 20, 20);
                        }
                        return true;
                    }

                    //chat mute <player> <channel>
                    if (args[0].equalsIgnoreCase("mute")) {
                        if (!UCPerms.cmdPerm(p, "mute")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        UCChannel ch = UChat.get().getChannel(args[2]);
                        if (ch == null) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[2]));
                            return true;
                        }

                        String pname = args[1];
                        if (Bukkit.getPlayer(args[1]) != null) {
                            pname = Bukkit.getPlayer(args[1]).getName();
                        }

                        if (ch.isMuted(pname)) {
                            ch.unMuteThis(pname);
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.unmuted.this").replace("{player}", pname).replace("{channel}", ch.getName()));
                            if (Bukkit.getPlayer(pname) != null) {
                                UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), UChat.get().getLang().get("channel.player.unmuted.this").replace("{channel}", ch.getName()));
                            }
                        } else {
                            ch.muteThis(pname);
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.muted.this").replace("{player}", pname).replace("{channel}", ch.getName()));
                            if (Bukkit.getPlayer(pname) != null) {
                                UChat.get().getLang().sendMessage(Bukkit.getPlayer(args[1]), UChat.get().getLang().get("channel.player.muted.this").replace("{channel}", ch.getName()));
                            }
                        }
                        return true;
                    }
                }

                if (args.length == 4) {

                    //chat newchannel <channel-name> <alias> <color>
                    if (args[0].equalsIgnoreCase("newchannel")) {
                        if (!UCPerms.cmdPerm(p, "newchannel")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        UCChannel ch = UChat.get().getChannel(args[1]);
                        if (ch != null) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.alreadyexists").replace("{channel}", ch.getName()));
                            return true;
                        }
                        String color = args[3];
                        if (color.length() != 2 || !color.matches("(&([A-Fa-fK-Ok-oRr0-9]))$")) {
                            UChat.get().getLang().sendMessage(p, "channel.invalidcolor");
                            return true;
                        }

                        UCChannel newch = new UCChannel(args[1], args[2], args[3]);
                        try {
                            UChat.get().getUCConfig().addChannel(newch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        UChat.get().registerAliases();
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.newchannel.success").replace("{channel}", newch.getName()));
                        return true;
                    }

                    //chat chconfig <channel> <key> <value>
                    if (args[0].equalsIgnoreCase("chconfig")) {
                        if (!UCPerms.cmdPerm(p, "chconfig")) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                            return true;
                        }
                        UCChannel ch = UChat.get().getChannel(args[1]);
                        if (ch == null) {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.dontexist").replace("{channel}", args[1]));
                            return true;
                        }
                        if (!ch.getProperties().containsKey(args[2])) {
                            UChat.get().getLang().sendMessage(p, "cmd.chconfig.invalidkey");
                            return true;
                        }

                        UChat.get().getUCConfig().delChannel(ch);
                        ch.setProperty(args[2], args[3]);
                        try {
                            UChat.get().getUCConfig().addChannel(ch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        UChat.get().registerAliases();
                        UChat.get().getLang().sendMessage(p, "cmd.chconfig.success");
                        return true;
                    }
                }
            }
        } else {
            if (UChat.get().getChAliases().contains(label.toLowerCase())) {
                if (args.length >= 1) {
                    UCChannel ch = UChat.get().getChannel(label);

                    StringBuilder msgb = new StringBuilder();
                    for (String arg : args) {
                        msgb.append(" ").append(arg);
                    }
                    String msg = msgb.toString().substring(1);

                    UCMessages.sendFancyMessage(new String[0], msg, ch, sender, null);
                    return true;
                }
            }
        }

        if (cmd.getName().equalsIgnoreCase("ubroadcast") && UCPerms.cmdPerm(sender, "broadcast")) {
            if (!UCUtil.sendBroadcast(sender, args, false)) {
                sendHelp(sender);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("umsg") && UCPerms.cmdPerm(sender, "umsg")) {
            if (!UCUtil.sendUmsg(sender, args)) {
                sendHelp(sender);
            }
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "---------------- " + UChat.get().getPDF().getFullName() + " ----------------");
            sender.sendMessage(ChatColor.AQUA + "Developed by " + ChatColor.GOLD + UChat.get().getPDF().getAuthors() + ".");
            sender.sendMessage(ChatColor.AQUA + "For more information about the commands, type [" + ChatColor.GOLD + "/" + label + " ?" + ChatColor.AQUA + "].");
            sender.sendMessage(ChatColor.AQUA + "---------------------------------------------------");
            return true;
        } else {
            sendHelp(sender);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCmdChat(PlayerCommandPreprocessEvent e) {
        String[] args = e.getMessage().replace("/", "").split(" ");
        Player p = e.getPlayer();
        UChat.get().getUCLogger().debug("PlayerCommandPreprocessEvent - Channel: " + args[0]);

        //check tell aliases
        if (UChat.get().getUCConfig().getTellAliases().contains(args[0])) {
            e.setCancelled(true);

            Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
                String msg = null;
                if (e.getMessage().length() > args[0].length() + 2) {
                    msg = e.getMessage().substring(args[0].length() + 2);
                }

                if (!UCPerms.cmdPerm(p, "tell")) {
                    UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.nopermission"));
                    return;
                }

                if (args.length == 1) {
                    if (UChat.get().tellPlayers.containsKey(p.getName())) {
                        String tp = UChat.get().tellPlayers.get(p.getName());
                        UChat.get().tellPlayers.remove(p.getName());
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", tp));
                        return;
                    }
                }

                if (args.length >= 2) {
                    if (args[0].equalsIgnoreCase("r")) {
                        if (UChat.get().respondTell.containsKey(p.getName())) {
                            String recStr = UChat.get().respondTell.get(p.getName());
                            if (recStr.equals("CONSOLE")) {
                                UChat.get().respondTell.put("CONSOLE", p.getName());
                                UChat.get().command.add(p.getName());
                                sendPreTell(p, UChat.get().getServer().getConsoleSender(), msg);
                            } else {
                                Player receiver = UChat.get().getServer().getPlayer(UChat.get().respondTell.get(p.getName()));
                                UChat.get().respondTell.put(receiver.getName(), p.getName());
                                UChat.get().command.add(p.getName());
                                sendPreTell(p, receiver, msg);
                            }
                            return;
                        } else {
                            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.nonetorespond"));
                            return;
                        }
                    }
                }

                if (args.length == 2) {
                    Player receiver = UChat.get().getServer().getPlayer(args[1]);
                    if (receiver == null || !receiver.isOnline() || !p.canSee(receiver)) {
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("listener.invalidplayer"));
                        return;
                    }
                    if (receiver.equals(p)) {
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.self"));
                        return;
                    }

                    if (UChat.get().tellPlayers.containsKey(p.getName()) && UChat.get().tellPlayers.get(p.getName()).equals(receiver.getName())) {
                        UChat.get().tellPlayers.remove(p.getName());
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.unlocked").replace("{player}", receiver.getName()));
                    } else {
                        UChat.get().tellPlayers.put(p.getName(), receiver.getName());
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.locked").replace("{player}", receiver.getName()));
                    }
                    return;
                }

                //tell <nick> <mensagem...>
                if (args.length >= 3) {

                    //send to console
                    if (args[1].equalsIgnoreCase("console")) {
                        msg = msg.substring(args[1].length() + 1);

                        UChat.get().tempTellPlayers.put(p.getName(), "CONSOLE");
                        UChat.get().command.add(p.getName());
                        sendPreTell(p, UChat.get().getServer().getConsoleSender(), msg);
                        return;
                    }

                    //send to player
                    Player receiver = UChat.get().getServer().getPlayer(args[1]);

                    if (receiver == null || !receiver.isOnline()) {
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("listener.invalidplayer"));
                        return;
                    }

                    if (!p.canSee(receiver)) {
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("listener.invalidplayer"));
                        return;
                    }

                    if (receiver.equals(p)) {
                        UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.tell.self"));
                        return;
                    }

                    //remove receiver name
                    msg = msg.substring(args[1].length() + 1);

                    UChat.get().tempTellPlayers.put(p.getName(), receiver.getName());
                    UChat.get().command.add(p.getName());
                    //sendTell(p, receiver, msg);

                    sendPreTell(p, receiver, msg);
                    return;
                }
                sendTellHelp(p);
            });
        }
    }

    private void sendPreTell(CommandSender sender, CommandSender receiver, String msg) {
        Set<Player> pls = new HashSet<>();
        Player p;
        if (sender instanceof Player) {
            p = (Player) sender;
        } else {
            p = (Player) receiver;
        }
        pls.add(p);
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, p, msg, pls);
        Bukkit.getScheduler().runTaskAsynchronously(UChat.get(), () -> {
            UChat.get().getUCLogger().timings(timingType.START, "UCListener#sendPreTell()|Fire AsyncPlayerChatEvent");
            UChat.get().getServer().getPluginManager().callEvent(event);
        });
    }

    @EventHandler
    public void onServerCmd(ServerCommandEvent e) {
        String[] args = e.getCommand().replace("/", "").split(" ");
        String msg = null;
        if (e.getCommand().length() > args[0].length() + 1) {
            msg = e.getCommand().substring(args[0].length() + 1);
        }

        if (msg != null && UChat.get().getUCConfig().getTellAliases().contains(args[0])) {
            if (args.length >= 3) {
                Player p = UChat.get().getServer().getPlayer(args[1]);

                if (p == null || !p.isOnline()) {
                    UChat.get().getLang().sendMessage(e.getSender(), "listener.invalidplayer");
                    return;
                }

                msg = msg.substring(args[1].length() + 1);

                UChat.get().tempTellPlayers.put("CONSOLE", p.getName());
                UChat.get().command.add("CONSOLE");
                sendPreTell(UChat.get().getServer().getConsoleSender(), p, msg);
                e.setCancelled(true);
            }
        }
    }

    private void sendTell(CommandSender sender, CommandSender receiver, String msg) {
        if (receiver == null
                || (receiver instanceof Player && (!((Player) receiver).isOnline()
                || (sender instanceof Player && !((Player) sender).canSee((Player) receiver))))
        ) {
            UChat.get().getLang().sendMessage(sender, UChat.get().getLang().get("listener.invalidplayer"));
            return;
        }
        UChat.get().respondTell.put(receiver.getName(), sender.getName());
        UCMessages.sendFancyMessage(new String[0], msg, null, sender, receiver);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {

        UChat.get().getUCLogger().debug("AsyncPlayerChatEvent: " + e.getMessage());
        UChat.get().getUCLogger().timings(timingType.START, "UCListener#onChat()|Listening AsyncPlayerChatEvent");

        //e.setCancelled(true);
        Player p = e.getPlayer();

        //check channel char
        String rawMsg = e.getMessage();
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (ch.getCharAlias().isEmpty()) continue;

            if (ch.getCharAlias().length() == rawMsg.length() && rawMsg.equalsIgnoreCase(ch.getCharAlias())) {
                addPlayerToChannel(ch, p);
                e.setCancelled(true);
                return;
            }
            if (rawMsg.startsWith(ch.getCharAlias())) {
                String msg = rawMsg.substring(ch.getCharAlias().length());
                sendMessageToPlayer(ch, p, e.getMessage().split(" "), msg);
                e.setCancelled(true);
                return;
            }
        }

        if (UChat.get().tellPlayers.containsKey(p.getName()) && (!UChat.get().tempTellPlayers.containsKey("CONSOLE") || !UChat.get().tempTellPlayers.get("CONSOLE").equals(p.getName()))) {
            Player tellreceiver = UChat.get().getServer().getPlayer(UChat.get().tellPlayers.get(p.getName()));
            sendTell(p, tellreceiver, e.getMessage());
            e.setCancelled(true);
        } else if (UChat.get().command.contains(p.getName()) || UChat.get().command.contains("CONSOLE")) {
            if (UChat.get().tempTellPlayers.containsKey("CONSOLE")) {
                String recStr = UChat.get().tempTellPlayers.get("CONSOLE");
                Player pRec = UChat.get().getServer().getPlayer(recStr);
                if (pRec.equals(p)) {
                    sendTell(UChat.get().getServer().getConsoleSender(), p, e.getMessage());
                    UChat.get().tempTellPlayers.remove("CONSOLE");
                    UChat.get().command.remove("CONSOLE");
                }
            } else if (UChat.get().tempTellPlayers.containsKey(p.getName())) {
                String recStr = UChat.get().tempTellPlayers.get(p.getName());
                if (recStr.equals("CONSOLE")) {
                    sendTell(p, UChat.get().getServer().getConsoleSender(), e.getMessage());
                } else {
                    sendTell(p, UChat.get().getServer().getPlayer(recStr), e.getMessage());
                }
                UChat.get().tempTellPlayers.remove(p.getName());
                UChat.get().command.remove(p.getName());
            } else if (UChat.get().respondTell.containsKey(p.getName())) {
                String recStr = UChat.get().respondTell.get(p.getName());
                if (recStr.equals("CONSOLE")) {
                    sendTell(p, UChat.get().getServer().getConsoleSender(), e.getMessage());
                } else {
                    sendTell(p, UChat.get().getServer().getPlayer(recStr), e.getMessage());
                }
                //UChat.get().respondTell.remove(p.getName());
                UChat.get().command.remove(p.getName());
            }
            e.setCancelled(true);
        } else {
            UCChannel ch = UChat.get().getPlayerChannel(p);
            if (UChat.get().tempChannels.containsKey(p.getName()) && !UChat.get().tempChannels.get(p.getName()).equals(ch.getAlias())) {
                ch = UChat.get().getChannel(UChat.get().tempChannels.get(p.getName()));
                UChat.get().getUCLogger().debug("AsyncPlayerChatEvent - TempChannel: " + UChat.get().tempChannels.get(p.getName()));
                UChat.get().tempChannels.remove(p.getName());
            }

            if (UChat.get().mutes.contains(p.getName()) || ch.isMuted(p.getName())) {
                if (UChat.get().timeMute.containsKey(p.getName())) {
                    UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.tempmuted").replace("{time}", String.valueOf(UChat.get().timeMute.get(p.getName()))));
                } else {
                    UChat.get().getLang().sendMessage(p, "channel.muted");
                }
                e.setCancelled(true);
                return;
            }

            if (ch.isCmdAlias()) {
                String start = ch.getAliasCmd();
                if (start.startsWith("/")) {
                    start = start.substring(1);
                }
                if (ch.getAliasSender().equalsIgnoreCase("console")) {
                    UCUtil.performCommand(null, Bukkit.getConsoleSender(), start + " " + e.getMessage());
                } else {
                    UCUtil.performCommand(null, p, start + " " + e.getMessage());
                }
                e.setCancelled(true);
            } else {
                boolean cancel = UCMessages.sendFancyMessage(e.getFormat().split(","), e.getMessage(), ch, p, null);
                if (cancel) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if (UChat.get().getUCJDA() != null && !p.hasPermission(UChat.get().getUCConfig().getString("discord.vanish-perm"))) {
            UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.death").replace("{player}", p.getName()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        Command cmd = Bukkit.getPluginCommand(e.getMessage().split(" ")[0].substring(1));
        if (UChat.get().getUCJDA() != null && cmd != null && !UChat.get().getUCConfig().getString("discord.log-ignored-commands").contains(cmd.getName())) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            UChat.get().getUCJDA().sendCommandsToDiscord(UChat.get().getLang().get("discord.command")
                    .replace("{player}", p.getName())
                    .replace("{cmd}", e.getMessage())
                    .replace("{time-now}", sdf.format(cal.getTime())));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (!UChat.get().getUCConfig().getBoolean("general.persist-channels")) {
            UChat.get().getDefChannel(p.getWorld().getName()).addMember(p);
        }

        if (UChat.get().getUCJDA() != null && !p.hasPermission(UChat.get().getUCConfig().getString("discord.vanish-perm"))) {
            UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.join").replace("{player}", p.getName()));
        }
        if (UChat.get().getUCConfig().getBoolean("general.spy-enable-onjoin") && p.hasPermission("uchat.cmd.spy") && !UChat.get().isSpy.contains(p.getName())) {
            UChat.get().isSpy.add(p.getName());
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("cmd.spy.enabled"));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void legendCompatEvent(SendChannelMessageEvent e) {
        if (e.isCancelled()) {
            return;
        }
        ChatMessageEvent event = new ChatMessageEvent(e.getSender(), e.getResgisteredTags(), e.getMessage());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            e.setCancelled(true);
        }
        e.setMessage(event.getMessage());
        e.setTags(event.getTagMap());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        if (!UChat.get().getUCConfig().getBoolean("general.persist-channels")) {
            UChat.get().getPlayerChannel(p).removeMember(p);
        }

        List<String> toRemove = new ArrayList<>();
        for (String play : UChat.get().tellPlayers.keySet()) {
            if (play.equals(p.getName())) {
                toRemove.add(play);
            }
        }
        for (String remove : toRemove) {
            UChat.get().tellPlayers.remove(remove);
        }

        List<String> toRemove2 = new ArrayList<>();
        for (String play : UChat.get().respondTell.keySet()) {
            if (play.equals(p.getName())) {
                toRemove2.add(play);
            }
        }
        for (String remove : toRemove2) {
            UChat.get().respondTell.remove(remove);
        }

        List<String> toRemove3 = new ArrayList<>();
        for (String play : UChat.get().tempChannels.keySet()) {
            if (play.equals(p.getName())) {
                toRemove3.add(play);
            }
        }
        for (String remove : toRemove3) {
            UChat.get().tempChannels.remove(remove);
        }

        UChat.get().command.remove(p.getName());
        if (UChat.get().getUCJDA() != null && !p.hasPermission(UChat.get().getUCConfig().getString("discord.vanish-perm"))) {
            UChat.get().getUCJDA().sendRawToDiscord(UChat.get().getLang().get("discord.leave").replace("{player}", p.getName()));
        }
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent e) {

        Player p = e.getPlayer();
        World tw = p.getWorld();
        String toCh = "";

        if (UChat.get().getUCConfig().getBoolean("general.default-channels.worlds." + tw.getName() + ".force")) {
            UChat.get().getDefChannel(tw.getName()).addMember(p);
        }

        UCChannel pch = UChat.get().getPlayerChannel(p);

        if (!UChat.get().getUCConfig().getBoolean("general.check-channel-change-world")) {
            if (!pch.availableInWorld(tw)) {
                if (UChat.get().getDefChannel(tw.getName()).availableInWorld(tw)) {
                    pch = UChat.get().getDefChannel(tw.getName()).addMember(p);
                    toCh = pch.getName();
                } else {
                    for (UCChannel ch : UChat.get().getChannels().values()) {
                        if (ch.availableInWorld(tw)) {
                            pch = ch.addMember(p);
                            toCh = ch.getName();
                            break;
                        }
                    }
                }
            }
        }


        if (!toCh.isEmpty()) {
            UChat.get().getLang().sendMessage(p, UChat.get().getLang().get("channel.entered").replace("{channel}", toCh));
        } else if (!pch.availableInWorld(tw)) {
            pch.removeMember(p);
            UChat.get().getLang().sendMessage(p, "channel.world.none");
        }
    }

    private void sendHelp(CommandSender p) {
        UltimateFancy fancy = new UltimateFancy();
        fancy.coloredText("\n&7--------------- " + UChat.get().getLang().get("_UChat.prefix") + " Help &7---------------\n");
        fancy.coloredText(UChat.get().getLang().get("help.channels.enter") + "\n");
        fancy.coloredText(UChat.get().getLang().get("help.channels.send") + "\n");
        fancy.coloredText(UChat.get().getLang().get("help.channels.list") + "\n");
        if (UCPerms.cmdPerm(p, "tell")) {
            fancy.coloredText(UChat.get().getLang().get("help.tell.lock") + "\n");
            fancy.coloredText(UChat.get().getLang().get("help.tell.send") + "\n");
            fancy.coloredText(UChat.get().getLang().get("help.tell.respond") + "\n");
        }
        if (UCPerms.cmdPerm(p, "broadcast")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.broadcast") + "\n");
        }
        if (UCPerms.cmdPerm(p, "umsg")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.umsg") + "\n");
        }
        if (UCPerms.cmdPerm(p, "clear")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.clear") + "\n");
        }
        if (UCPerms.cmdPerm(p, "clear-all")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.clear-all") + "\n");
        }
        if (UCPerms.cmdPerm(p, "spy")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.spy") + "\n");
        }
        if (UCPerms.cmdPerm(p, "mute")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.mute") + "\n");
        }
        if (UCPerms.cmdPerm(p, "tempmute")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.tempmute") + "\n");
        }
        if (UCPerms.cmdPerm(p, "ignore.player")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.ignore.player") + "\n");
        }
        if (UCPerms.cmdPerm(p, "ignore.channel")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.ignore.channel") + "\n");
        }
        if (UCPerms.cmdPerm(p, "msgtoggle")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.msgtoggle") + "\n");
        }
        if (UCPerms.cmdPerm(p, "cmd.reload")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.reload") + "\n");
        }
        if (UCPerms.cmdPerm(p, "chconfig")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.chconfig") + "\n");
        }
        if (UCPerms.cmdPerm(p, "newchannel")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.newchannel") + "\n");
        }
        if (UCPerms.cmdPerm(p, "delchannel")) {
            fancy.coloredText(UChat.get().getLang().get("help.cmd.delchannel") + "\n");
        }
        fancy.coloredText("&7------------------------------------------\n");
        fancy.coloredText(UChat.get().getLang().get("help.channels.available").replace("{channels}", "") + " ").next();
        boolean first = true;
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (!(p instanceof Player) || UCPerms.channelReadPerm(p, ch)) {
                if (first) {
                    fancy.coloredText(ch.getColor() + ch.getName() + "&a");
                    first = false;
                } else {
                    fancy.coloredText(", " + ch.getColor() + ch.getName() + "&a");
                }
                fancy.hoverShowText(ch.getColor() + "Alias: " + ch.getAlias())
                        .clickRunCmd("/" + ch.getAlias())
                        .next();
            }
        }
        fancy.coloredText("\n&7------------------------------------------");
        if (UCPerms.hasPerm(p, "admin")) {
            String jarversion = new java.io.File(UCListener.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath())
                    .getName();
            fancy.coloredText("\n&8&o- UChat full version: " + jarversion);
        }
        fancy.send(p);
    }

    private void sendChannelHelp(Player p) {
        StringBuilder channels = new StringBuilder();
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (!UCPerms.channelReadPerm(p, ch)) continue;
            channels.append(", ").append(ch.getName());
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7>> -------------- " + UChat.get().getLang().get("_UChat.prefix") + " Help &7-------------- <<"));
        p.sendMessage(UChat.get().getLang().get("help.channels.available").replace("{channels}", channels.toString().substring(2)));
        p.sendMessage(UChat.get().getLang().get("help.channels.enter"));
        p.sendMessage(UChat.get().getLang().get("help.channels.send"));
    }

    private void sendTellHelp(Player p) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7>> -------------- " + UChat.get().getLang().get("_UChat.prefix") + " Help &7-------------- <<"));
        p.sendMessage(UChat.get().getLang().get("help.tell.unlock"));
        p.sendMessage(UChat.get().getLang().get("help.tell.lock"));
        p.sendMessage(UChat.get().getLang().get("help.tell.send"));
        p.sendMessage(UChat.get().getLang().get("help.tell.respond"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();
        if (command.getName().equals("tell") || command.getName().equals("channel")) {
            if (args.length > 0) {
                for (String arg : args) {
                    if (Bukkit.getPlayer(arg) != null) {
                        tab.add(Bukkit.getPlayer(arg).getName());
                    }
                }
            }
        }
        if (command.getName().equals("uchat")) {
            if (args.length == 1) {
                tab.addAll(UChat.get().getLang().helpStrings());
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("chconfig")) {
                if (args.length == 2) {
                    for (UCChannel ch : UChat.get().getChannels().values()) {
                        tab.add(ch.getName());
                    }
                }
                if (args.length == 3) {
                    UCChannel ch = UChat.get().getChannel(args[1]);
                    if (ch == null) return null;
                    ch.getProperties().keySet().forEach((key) -> tab.add(key.toString()));
                }
            }
        }
        return tab;
    }
}

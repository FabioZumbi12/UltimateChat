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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class UCUtil {

    public static int getBukkitVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String v = name.substring(name.lastIndexOf('.') + 1) + ".";
        String[] version = v.replace('_', '.').split("\\.");

        int lesserVersion = 0;
        try {
            lesserVersion = Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {
        }
        return Integer.parseInt((version[0] + version[1]).substring(1) + lesserVersion);
    }

    public static String capitalize(String text) {
        StringBuilder cap = new StringBuilder();
        text = text.replace("_", " ");
        for (String t : text.split(" ")) {
            if (t.length() > 2) {
                cap.append(t.substring(0, 1).toUpperCase()).append(t.substring(1).toLowerCase()).append(" ");
            } else {
                cap.append(t).append(" ");
            }
        }
        return cap.substring(0, cap.length() - 1);
    }

    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static void saveResource(String name, File saveTo) {
        try {
            InputStream isReader = UChat.class.getResourceAsStream(name);
            FileOutputStream fos = new FileOutputStream(saveTo);
            while (isReader.available() > 0) {
                fos.write(isReader.read());
            }
            fos.close();
            isReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void performCommand(final Player to, final CommandSender sender, final String command) {
        Bukkit.getScheduler().runTask(UChat.get(), () -> {
            if (to == null || to.isOnline()) {
                UChat.get().getServer().dispatchCommand(sender, command);
            }
        });
    }

    public static boolean sendUmsg(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(UChat.get().getLang().get("help.cmd.umsg"));
            return false;
        }

        Player receiver = Bukkit.getPlayer(args[0]);
        if (receiver == null) {
            sender.sendMessage(UChat.get().getLang().get("listener.invalidplayer"));
            return true;
        }

        UltimateFancy fancy = new UltimateFancy();
        boolean first = true;
        for (String arg : args) {
            if (first) {
                first = false;
                continue;
            }

            arg = arg.replace("/n", "\n");
            fancy.coloredText(arg + " ");
            try {
                fancy.clickOpenURL(new URL(arg));
                fancy.hoverShowText(UCUtil.colorize(UChat.get().getUCConfig().getString("general.URL-template").replace("{url}", arg)));
            } catch (MalformedURLException ignored) {
            }
        }

        fancy.send(receiver);
        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> Private to " + ChatColor.GOLD + receiver.getName() + ChatColor.DARK_GRAY + ": " + ChatColor.RESET + fancy.toOldFormat());
        return true;
    }

    static boolean sendBroadcast(CommandSender src, String[] args, boolean silent) {
        StringBuilder message = new StringBuilder();
        StringBuilder hover = new StringBuilder();
        StringBuilder cmdline = new StringBuilder();
        StringBuilder url = new StringBuilder();
        StringBuilder suggest = new StringBuilder();
        boolean isHover = false;
        boolean isCmd = false;
        boolean isUrl = false;
        boolean isSug = false;
        for (String arg : args) {
            arg = arg.replace("/n", "\n");
            if (arg.contains(UChat.get().getUCConfig().getString("broadcast.on-hover"))) {
                hover.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.on-hover"), "")));
                isHover = true;
                isCmd = false;
                isUrl = false;
                isSug = false;
                continue;
            }
            if (arg.contains(UChat.get().getUCConfig().getString("broadcast.on-click"))) {
                cmdline.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.on-click"), "")));
                isCmd = true;
                isHover = false;
                isUrl = false;
                isSug = false;
                continue;
            }
            if (arg.contains(UChat.get().getUCConfig().getString("broadcast.url"))) {
                url.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.url"), "")));
                isCmd = false;
                isHover = false;
                isUrl = true;
                isSug = false;
                continue;
            }
            if (arg.contains(UChat.get().getUCConfig().getString("broadcast.suggest"))) {
                suggest.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg.replace(UChat.get().getUCConfig().getString("broadcast.suggest"), "")));
                isCmd = false;
                isHover = false;
                isUrl = false;
                isSug = true;
                continue;
            }

            if (isCmd) {
                cmdline.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg));
            } else if (isHover) {
                hover.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg));
            } else if (isUrl) {
                url.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg));
            } else if (isSug) {
                suggest.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg));
            } else {
                message.append(" ").append(ChatColor.translateAlternateColorCodes('&', arg));
            }
        }

        if (message.toString().length() <= 1) {
            return false;
        }

        String finalMsg = UCMessages.formatTags("", message.toString().substring(1), src, src, message.toString().substring(1), new UCChannel("broadcast"));

        if (!silent) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> Broadcast: " + ChatColor.RESET + finalMsg);
        }

        if (UChat.get().getUCConfig().getBoolean("general.json-events")) {
            UltimateFancy fanci = new UltimateFancy();
            fanci.coloredText(finalMsg);

            if (hover.toString().length() > 1) {
                fanci.hoverShowText(hover.toString().substring(1));
                if (!silent) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> OnHover: " + ChatColor.RESET + hover.toString().substring(1));
                }
            }

            if (cmdline.toString().length() > 1 && !silent) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> OnClick: " + ChatColor.RESET + cmdline.toString().substring(1));
            }

            if (url.toString().length() > 1) {
                try {
                    fanci.clickOpenURL(new URL(url.toString().substring(1)));
                    if (!silent) {
                        Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_GRAY + "> Url: " + ChatColor.RESET + url.toString().substring(1));
                    }
                } catch (MalformedURLException ignore) {
                }
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (cmdline.toString().length() > 1) {
                    fanci.clickRunCmd("/" + cmdline.toString().substring(1).replace("{clicked}", p.getName()));
                }
                if (suggest.toString().length() > 1) {
                    fanci.clickSuggestCmd(suggest.toString().substring(1).replace("{clicked}", p.getName()));
                }

                fanci.send(p);
            }
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(finalMsg);
            }
        }
        return true;
    }
}

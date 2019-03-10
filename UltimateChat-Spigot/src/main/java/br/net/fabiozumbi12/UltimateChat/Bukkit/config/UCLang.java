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

package br.net.fabiozumbi12.UltimateChat.Bukkit.config;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

public class UCLang {

    private static final HashMap<Player, String> DelayedMessage = new HashMap<>();
    private static final Properties BaseLang = new Properties();
    private static final Properties Lang = new Properties();
    private String pathLang;
    private String resLang;

    public UCLang() {
        pathLang = UChat.get().getDataFolder() + File.separator + "lang" + UChat.get().getUCConfig().getString("language") + ".properties";
        resLang = "lang" + UChat.get().getUCConfig().getString("language") + ".properties";

        File lang = new File(pathLang);
        if (!lang.exists()) {
            if (UChat.get().getResource("assets/ultimatechat/" + resLang) == null) {
                resLang = "langEN-US.properties";
                pathLang = UChat.get().getDataFolder() + File.separator + "langEN-US.properties";
            }
            //UChat.get().saveResource(resLang, false);//create lang file
            UCUtil.saveResource("/assets/ultimatechat/" + resLang, new File(UChat.get().getDataFolder(), resLang));
            UChat.get().getUCLogger().info("Created lang file: " + pathLang);
        }

        loadLang();
        loadBaseLang();
        UChat.get().getUCLogger().info("Language file loaded - Using: " + UChat.get().getUCConfig().getString("language"));
    }

    public SortedSet<String> helpStrings() {
        SortedSet<String> values = new TreeSet<>();
        for (Object help : Lang.keySet()) {
            if (help.toString().startsWith("help.cmd.")) {
                String helpStr = help.toString().replace("help.cmd.", "");
                if (helpStr.equals("broadcast") || helpStr.equals("umsg")) continue;
                if (helpStr.split("\\.").length >= 2) {
                    values.add(helpStr.split("\\.")[0]);
                } else {
                    values.add(helpStr);
                }
            }
        }
        return values;
    }

    private void loadBaseLang() {
        BaseLang.clear();
        try {
            InputStream fileInput = UChat.get().getResource("assets/ultimatechat/langEN-US.properties");
            Reader reader = new InputStreamReader(fileInput, "UTF-8");
            BaseLang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLang();
    }

    private void loadLang() {
        Lang.clear();
        try {
            FileInputStream fileInput = new FileInputStream(pathLang);
            Reader reader = new InputStreamReader(fileInput, "UTF-8");
            Lang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Lang.get("_lang.version") != null) {
            int langv = Integer.parseInt(Lang.get("_lang.version").toString().replace(".", ""));
            int rpv = Integer.parseInt(UChat.get().getPDF().getVersion().replace(".", ""));
            if (langv < rpv || langv == 0) {
                UChat.get().getUCLogger().warning("Your lang file is outdated. Probally need strings updates!");
                UChat.get().getUCLogger().warning("Lang file version: " + Lang.get("_lang.version"));
                Lang.put("_lang.version", UChat.get().getPDF().getVersion());
            }
        }
    }

    private void updateLang() {
        for (Entry<Object, Object> linha : BaseLang.entrySet()) {
            if (!Lang.containsKey(linha.getKey())) {
                Lang.put(linha.getKey(), linha.getValue());
            }
        }
        if (!Lang.containsKey("_lang.version")) {
            Lang.put("_lang.version", UChat.get().getPDF().getVersion());
        }
        try {
            Lang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(CommandSender player, String key) {
        return UCMessages.formatTags("", get(key), player, "", "", UChat.get().getPlayerChannel(player));
    }

    public String get(String key) {
        String FMsg;

        if (!Lang.containsKey(key)) {
            FMsg = "&c&oMissing language string for " + ChatColor.GOLD + key;
        } else {
            FMsg = Lang.get(key).toString();
        }

        FMsg = ChatColor.translateAlternateColorCodes('&', FMsg);

        return FMsg.replace("/n", "\n");
    }

    public void sendMessage(final Player p, String key) {
        if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)) {
            return;
        }

        if (!Lang.containsKey(key)) {
            p.sendMessage(get(p, "_UChat.prefix") + " " + UCMessages.formatTags("", ChatColor.translateAlternateColorCodes('&', key), p, "", "", UChat.get().getPlayerChannel(p)));
        } else if (get(key).isEmpty()) {
            return;
        } else {
            p.sendMessage(get(p, "_UChat.prefix") + " " + get(p, key));
        }

        DelayedMessage.put(p, key);
        Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.get(), () -> {
            if (DelayedMessage.containsKey(p)) {
                DelayedMessage.remove(p);
            }
        }, 20);
    }

    public void sendMessage(CommandSender sender, String key) {
        if (sender instanceof Player && DelayedMessage.containsKey(sender) && DelayedMessage.get(sender).equals(key)) {
            return;
        }

        if (Lang.get(key) == null) {
            sender.sendMessage(get(sender, "_UChat.prefix") + " " + UCMessages.formatTags("", ChatColor.translateAlternateColorCodes('&', key), sender, "", "", UChat.get().getPlayerChannel(sender)));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            sender.sendMessage(get(sender, "_UChat.prefix") + " " + get(sender, key));
        }

        if (sender instanceof Player) {
            final Player p = (Player) sender;
            DelayedMessage.put(p, key);
            Bukkit.getScheduler().scheduleSyncDelayedTask(UChat.get(), () -> {
                if (DelayedMessage.containsKey(p)) {
                    DelayedMessage.remove(p);
                }
            }, 20);
        }
    }

    public boolean containsValue(String value) {
        return Lang.containsValue(value);
    }
}

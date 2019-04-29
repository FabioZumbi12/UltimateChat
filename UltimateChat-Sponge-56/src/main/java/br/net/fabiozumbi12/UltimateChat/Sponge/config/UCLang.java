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

package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class UCLang {

    private static final HashMap<CommandSource, String> DelayedMessage = new HashMap<>();
    private final Properties loadedLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private final Properties baseLang = new Properties() {
        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>(super.keySet()));
        }
    };
    private static String pathLang;

    public UCLang() {
        String resLang = "lang" + UChat.get().getConfig().root().language + ".properties";
        pathLang = UChat.get().configDir() + File.separator + resLang;

        File lang = new File(pathLang);
        if (!lang.exists()) {
            try {
                if (UChat.get().instance().getAsset(resLang).isPresent()) {
                    UChat.get().instance().getAsset(resLang).get().copyToDirectory(UChat.get().configDir().toPath());
                } else {
                    UChat.get().instance().getAsset("langEN-US.properties").get().copyToDirectory(UChat.get().configDir().toPath());
                    new File(UChat.get().configDir(),"langEN-US.properties" ).renameTo(lang);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            UChat.get().instance().getLogger().info("Created lang file: " + pathLang);
        }

        loadLang();
        loadbaseLang();
        updateLang();

        UChat.get().getLogger().info("Language file loaded - Using: " + UChat.get().getConfig().root().language);
    }

    private void loadbaseLang() {
        baseLang.clear();
        try {
            baseLang.load(UChat.get().instance().getAsset("langEN-US.properties").get().getUrl().openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLang() {
        loadedLang.clear();
        try {
            FileInputStream fileInput = new FileInputStream(pathLang);
            Reader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8);
            loadedLang.load(reader);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (loadedLang.get("_lang.version") != null) {
            int langv = Integer.parseInt(((String) loadedLang.get("_lang.version")).replace(".", ""));
            int rpv = Integer.parseInt(UChat.get().instance().getVersion().get().replace(".", ""));
            if (langv < rpv || langv == 0) {
                UChat.get().instance().getLogger().info("Your lang file is outdated. Probably need strings updates!");
                UChat.get().instance().getLogger().info("Lang file version: " + loadedLang.get("_lang.version"));
                loadedLang.put("_lang.version", UChat.get().instance().getVersion().get());
            }
        }
    }

    private void updateLang() {
        for (Entry<Object, Object> linha : baseLang.entrySet()) {
            if (!loadedLang.containsKey(linha.getKey())) {
                loadedLang.put(linha.getKey(), linha.getValue());
            }
        }
        if (!loadedLang.containsKey("_lang.version")) {
            loadedLang.put("_lang.version", UChat.get().instance().getVersion().get());
        }
        try {
            loadedLang.store(new OutputStreamWriter(new FileOutputStream(pathLang), StandardCharsets.UTF_8), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(CommandSource player, String key) {
        return UCMessages.formatTags("", get(key), player, "", "", UChat.get().getPlayerChannel(player));
    }

    public String get(String key) {
        String FMsg;

        if (loadedLang.get(key) == null) {
            FMsg = "&c&oMissing language string for &4" + key;
        } else {
            FMsg = loadedLang.get(key).toString();
        }
        return FMsg.replace("/n", "\n");
    }

    public Text getText(String key, String additional) {
        return UCUtil.toText(get(key) + additional);
    }

    public Text getText(String key) {
        return UCUtil.toText(get(key));
    }

    public void sendMessage(final CommandSource p, String key) {
        if (DelayedMessage.containsKey(p) && DelayedMessage.get(p).equals(key)) {
            return;
        }

        if (loadedLang.get(key) == null) {
            p.sendMessage(UCUtil.toText(get(p, "_UChat.prefix") + " " + UCMessages.formatTags("", key, p, "", "", UChat.get().getPlayerChannel(p))));
        } else if (get(key).equalsIgnoreCase("")) {
            return;
        } else {
            p.sendMessage(UCUtil.toText(get(p, "_UChat.prefix") + " " + get(p, key)));
        }

        DelayedMessage.put(p, key);
        Sponge.getScheduler().createSyncExecutor(UChat.get().instance()).schedule(() -> {
            DelayedMessage.remove(p);
        }, 1, TimeUnit.SECONDS);
    }

}

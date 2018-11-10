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

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.*;

@ConfigSerializable
public class ProtectionsCategory {

    @Setting(value = "chat-protection")
    public Chatprotection chat_protection = new Chatprotection();

    public ProtectionsCategory() {
    }

    @ConfigSerializable
    public static class Chatprotection {

        //chat_enhancement
        @Setting(value = "chat-enhancement")
        public ChatEnhancementCat chat_enhancement = new ChatEnhancementCat();
        //anti-flood
        @Setting(value = "anti-flood")
        public AntiFloodCat anti_flood = new AntiFloodCat();
        //caps-filter
        @Setting(value = "caps-filter")
        public CapsFilterCat caps_filter = new CapsFilterCat();
        //anti-spam
        @Setting
        public AntiSpamCat antispam = new AntiSpamCat();
        //censor
        @Setting
        public CensorCat censor = new CensorCat();
        //anti-ip
        @Setting(value = "anti-ip")
        public AntiIpCat anti_ip = new AntiIpCat();

        @ConfigSerializable
        public static class ChatEnhancementCat {

            @Setting
            public boolean enable = true;
            @Setting(value = "disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting(value = "end-with-dot")
            public boolean end_with_dot = true;
            @Setting(value = "minimum-length")
            public int minimum_length = 3;
        }

        @ConfigSerializable
        public static class AntiFloodCat {

            @Setting
            public boolean enable = true;
            @Setting(value = "disable-on-channels")
            public List<String> disable_on_channels = Collections.singletonList("Local");
            @Setting(value = "whitelist-flood-characs")
            public List<String> whitelist_flood_characs = Arrays.asList("k", "w");
        }

        @ConfigSerializable
        public static class CapsFilterCat {

            @Setting
            public boolean enable = true;
            @Setting(value = "disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting(value = "minimum-length")
            public int minimum_length = 3;
        }

        @ConfigSerializable
        public static class AntiSpamCat {

            @Setting
            public boolean enable = true;
            @Setting(value = "disable-on-channels")
            public List<String> disable_on_channels = Collections.singletonList("Local");
            @Setting(value = "time-between-messages", comment = "In seconds")
            public int time_between_messages = 1;
            @Setting(value = "count-of-same-message")
            public int count_of_same_message = 5;
            @Setting(value = "time-between-same-messages", comment = "In seconds.")
            public int time_between_same_messages = 10;
            @Setting(value = "cooldown-msg")
            public String cooldown_msg = "&6Slow down your messages!";
            @Setting(value = "wait-message")
            public String wait_message = "&cWait to send the same message again!";
            @Setting(value = "cmd-action")
            public String cmd_action = "kick {player} Relax, slow down your messages frequency ;)";
        }

        @ConfigSerializable
        public static class CensorCat {
            @Setting
            public boolean enable = true;
            @Setting(value = "disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting(value = "replace-by-symbol", comment = "Disable to use the replace-words list.")
            public boolean replace_by_symbol = true;
            @Setting(value = "by-symbol")
            public String by_symbol = "*";
            @Setting(value = "replace-partial-word", comment = "Use uchat pre actions to replace partial words?")
            public boolean replace_partial_word = false;
            @Setting(value = "use-pre-actions", comment = "Use uchat pre-actions regex or your custom regex on replace-words?")
            public boolean use_pre_actions = true;
            @Setting(value = "replace-words", comment = "List of words to replace (case insensitive). Accept regex. When using regex, add an extra \"\\\" for each \"\\\" or will break your configuration'.")
            public HashMap<String, String> replace_words = createMap();
            //action
            @Setting
            public ActionCat action = new ActionCat();

            private HashMap<String, String> createMap() {
                HashMap<String, String> myMap = new HashMap<>();
                myMap.put("fuck", "*flower*");
                myMap.put("ass", "*finger*");
                return myMap;
            }

            @ConfigSerializable
            public static class ActionCat {

                @Setting
                public String cmd = "";
                @Setting(value = "only-on-channels")
                public List<String> only_on_channels = Collections.singletonList("global");
                @Setting(value = "on-partial-words")
                public boolean on_partial_words = false;
            }
        }

        @ConfigSerializable
        public static class AntiIpCat {

            @Setting
            public boolean enable = true;
            @Setting(value = "disable-on-channels")
            public List<String> disable_on_channels = new ArrayList<>();
            @Setting(value = "custom-ip-regex")
            public String custom_ip_regex = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
            @Setting(value = "custom-url-regex")
            public String custom_url_regex = "((http:\\/\\/|https:\\/\\/)?(www.)?(([a-zA-Z0-9-]){2,}\\.){1,4}([a-zA-Z]){2,6}(\\/([a-zA-Z-_\\/\\.0-9#:?=&;,]*)?)?)";
            @Setting(value = "check-for-words")
            public List<String> check_for_words = Collections.singletonList("www.google.com");
            @Setting(value = "whitelist-words")
            public List<String> whitelist_words = Arrays.asList("www.myserver.com", "prntscr.com", "gyazo.com", "www.youtube.com");
            @Setting(value = "cancel-or-replace", comment = "The options are: \"cancel\" or \"replace\"")
            public String cancel_or_replace = "cancel";
            @Setting(value = "cancel-msg")
            public String cancel_msg = "&cYou cant send websites or ips on chat";
            @Setting(value = "replace-by-word")
            public String replace_by_word = "-removed-";

            @Setting
            public PunishCat punish = new PunishCat();

            @ConfigSerializable
            public static class PunishCat {

                @Setting
                public boolean enable = true;
                @Setting(value = "max-attempts")
                public int max_attempts = 3;
                @Setting(value = "mute-or-cmd", comment = "The options are: \"mute\" or \"cmd\"")
                public String mute_or_cmd = "mute";
                @Setting(value = "mute-duration", comment = "In minutes.")
                public int mute_duration = 1;
                @Setting(value = "mute-msg")
                public String mute_msg = "&cYou have been muted for send IPs or URLs on chat!";
                @Setting(value = "unmute-msg")
                public String unmute_msg = "&aYou can chat again!";
                @Setting(value = "cmd-punish")
                public String cmd_punish = "tempban {player} 10m &cYou have been warned about send links or IPs on chat!";
            }
        }
    }


}

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

import org.bukkit.ChatColor;

public class UCLogger {

    private final UChat uchat;
    private long start = 0;

    public UCLogger(UChat uChat) {
        this.uchat = uChat;
    }

    public void logClear(String s) {
        uchat.getServer().getConsoleSender().sendMessage(s);
    }

    public void sucess(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[UltimateChat] &a&l" + s + "&r"));
    }

    public void info(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[UltimateChat] " + s ));
    }

    public void warning(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[UltimateChat] &6" + s + "&r"));
    }

    public void severe(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[UltimateChat] &c&l" + s + "&r"));
    }

    public void log(String s) {
        uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[UltimateChat] " + s));
    }

    public void debug(String s) {
        if (UChat.get().getUCConfig() != null && UChat.get().getUCConfig().getBoolean("debug.messages")) {
            uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[UltimateChat] &b" + s + "&r"));
        }
    }

    public void timings(timingType type, String message) {
        if (UChat.get().getUCConfig() != null && UChat.get().getUCConfig().getBoolean("debug.timings")) {
            switch (type) {
                case START:
                    long diff = 0;
                    if (System.currentTimeMillis() - start > 5000) start = 0;
                    if (start != 0) {
                        diff = System.currentTimeMillis() - start;
                    }
                    start = System.currentTimeMillis();
                    uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3UC Timings - " + type + ": " + diff + "ms (" + message + "&3)&r"));
                    break;
                case END:
                    uchat.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&3UC Timings - " + type + ": " + (System.currentTimeMillis() - start) + "ms (" + message + "&3)&r"));
                    break;
                default:
                    break;
            }
        }
    }

    public enum timingType {
        START, END
    }
}

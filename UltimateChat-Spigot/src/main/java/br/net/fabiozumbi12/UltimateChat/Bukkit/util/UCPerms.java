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

package br.net.fabiozumbi12.UltimateChat.Bukkit.util;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class UCPerms {

    private static final HashMap<String, Map<String, Boolean>> cachedPerm = new HashMap<>();

    public static boolean hasPermission(CommandSender sender, String perm) {
        if (cachedPerm.containsKey(sender.getName())) {
            Map<String, Boolean> perms = cachedPerm.get(sender.getName());
            if (perms.containsKey(perm)) {
                UChat.get().getUCLogger().debug("UCPerms#hasPermission - Get from Cache");
                return perms.get(perm);
            }
        }
        return testPerm(sender, perm);
    }

    private static boolean testPerm(CommandSender sender, String perm) {
        if (UChat.get().getVaultPerms() != null) {
            UChat.get().getUCLogger().debug("UCPerms#hasPermission - Get from Vault");
            return UChat.get().getVaultPerms().has(sender, perm);
        }
        UChat.get().getUCLogger().debug("UCPerms#hasPermission - Get directly from Player");
        return sender.hasPermission(perm);
    }

    public static boolean hasSpyPerm(CommandSender receiver, String ch) {
        return hasPerm(receiver, "chat-spy." + ch) || hasPerm(receiver, "chat-spy.all");
    }

    public static boolean cmdPerm(CommandSender p, String cmd) {
        return hasPerm(p, "cmd." + cmd);
    }

    public static boolean channelReadPerm(CommandSender p, UCChannel ch) {
        UCChannel defCh = UChat.get().getDefChannel(p instanceof Player ? ((Player) p).getWorld().getName() : null);
        return defCh.equals(ch) || hasPerm(p, "channel." + ch.getName().toLowerCase() + ".read");
    }

    public static boolean channelWritePerm(CommandSender p, UCChannel ch) {
        UCChannel defCh = UChat.get().getDefChannel(p instanceof Player ? ((Player) p).getWorld().getName() : null);
        return defCh.equals(ch) || hasPerm(p, "channel." + ch.getName().toLowerCase() + ".write");
    }

    public static boolean canIgnore(CommandSender sender, Object toignore) {
        return !(toignore instanceof CommandSender) || !hasPermission(sender, "uchat.cant-ignore." + (toignore instanceof Player ? ((Player) toignore).getName() : ((UCChannel) toignore).getName()));
    }

    public static boolean hasPerm(CommandSender p, String perm) {
        return isAdmin(p) || hasPermission(p, "uchat." + perm);
    }

    private static boolean isAdmin(CommandSender p) {
        return (p instanceof ConsoleCommandSender) || hasPermission(p, "uchat.admin");
    }
}

/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 16/07/2025 18:07
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.UltimateChat.Bukkit.hooks;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UCVaultPermsHook {
    private final Player sender;

    UCVaultPermsHook(Player sender) {
        this.sender = sender;
    }

    public String[] getPlayerGroups() {
        if (UCVaultCache.playerGroups.containsKey(sender.getName())) {
            return UCVaultCache.playerGroups.get(sender.getName());
        }
        String[] pgs = UChat.get().getHooks().getVaultPerms().getPlayerGroups(sender.getWorld().getName(), sender);

        setTempCacheGroups(sender.getName(), pgs);
        return pgs;
    }

    public String getPrimaryGroup() {
        if (UCVaultCache.primaryGroups.containsKey(sender.getName())) {
            return UCVaultCache.primaryGroups.get(sender.getName());
        }
        String pmg = UChat.get().getHooks().getVaultPerms().getPrimaryGroup(sender.getWorld().getName(), sender);

        setTempCachePrimary(sender.getName(), pmg);
        return pmg;
    }

    private void setTempCacheGroups(String sender, String[] pgs) {
        UCVaultCache.playerGroups.put(sender, pgs);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.playerGroups.remove(sender), 40);
    }

    private void setTempCachePrimary(String sender, String pmg) {
        UCVaultCache.primaryGroups.put(sender, pmg);
        Bukkit.getScheduler().runTaskLater(UChat.get(), () -> UCVaultCache.primaryGroups.remove(sender), 40);
    }
}

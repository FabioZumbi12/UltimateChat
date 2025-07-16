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
import br.net.fabiozumbi12.translationapi.TranslationAPI;
import br.net.fabiozumbi12.translationapi.TranslationCore;
import com.lenis0012.bukkit.marriage2.Marriage;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public class HooksManager {

    private boolean isRelation;
    private SimpleClans sc;
    private UCMarriageMasterHook mm2;
    private Marriage mapi;
    private TranslationCore tapi;
    private UCPHAPIHook phapi;
    private UCFactionsHookInterface FactionHook;
    private Permission perms;
    private Economy econ;
    private Chat chat;

    public SimpleClans getSc() {
        return this.sc;
    }

    public UCMarriageMasterHook getMarriage2() {
        return this.mm2;
    }

    public Marriage getMarriageReloaded() {
        return this.mapi;
    }

    public TranslationCore getTAPI() {
        return this.tapi;
    }

    public UCFactionsHookInterface getFactions() {
        return this.FactionHook;
    }

    public boolean isRelation() {
        return this.isRelation;
    }

    public Permission getVaultPerms() {
        return this.perms;
    }

    public Economy getVaultEco() {
        return this.econ;
    }

    public Chat getVaultChat() {
        return this.chat;
    }

    public UCPHAPIHook getPHAPI() {
        return this.phapi;
    }

    public HooksManager(UChat plugin){

        if (checkVault()) {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
            RegisteredServiceProvider<Chat> rschat = plugin.getServer().getServicesManager().getRegistration(Chat.class);
            RegisteredServiceProvider<Permission> rsperm = plugin.getServer().getServicesManager().getRegistration(Permission.class);
            //Economy
            if (rsp == null) {
                plugin.getUCLogger().warning("Vault found Economy, but for some reason cant be used.");
            } else {
                econ = rsp.getProvider();
                plugin.getUCLogger().info("Vault economy found. Hooked.");
            }
            //Chat
            if (rschat == null) {
                plugin.getUCLogger().warning("Vault found chat, but for some reason cant be used.");
            } else {
                chat = rschat.getProvider();
                plugin.getUCLogger().info("Vault chat found. Hooked.");
            }
            //Perms
            if (rsperm == null) {
                plugin.getUCLogger().warning("Vault found permissions, but for some reason cant be used.");
            } else {
                perms = rsperm.getProvider();
                plugin.getUCLogger().info("Vault perms found. Hooked.");
            }
        }

        if (checkPL()) {
            plugin.getUCLogger().info("ProtocolLib found. Hooked.");
        }

        if (checkPHAPI()) {
            phapi = new UCPHAPIHook();
            try {
                Class.forName("me.clip.placeholderapi.expansion.Relational");
                if (new UCPlaceHoldersRelational(plugin).register()) {
                    isRelation = true;
                    plugin.getUCLogger().info("PlaceHolderAPI found. Hooked and registered some chat placeholders with relational tag feature.");
                }
            } catch (ClassNotFoundException ex) {
                if (new UCPlaceHolders(plugin).register()) {
                    isRelation = false;
                    plugin.getUCLogger().info("PlaceHolderAPI found. Hooked and registered some chat placeholders.");
                }
            }
        }

        if (checkSC()) {
            sc = SimpleClans.getInstance();
            plugin.getUCLogger().info("SimpleClans found. Hooked.");
        }

        if(checkFac()) {
            FactionHook = new UCFactionsHook();
        }

        if(checkSFac()) {
            FactionHook = new UCSFactionsHook();
        }

        if (checkMR()) {
            mapi = MarriageAPI.getInstance();
            plugin.getUCLogger().info("Marriage Reloaded found. Hooked.");
        }

        if (checkMM2()) {
            Plugin mm2pl = Bukkit.getPluginManager().getPlugin("MarriageMaster");
            if (mm2pl != null) {
                mm2 = new UCMarriageMasterHook(mm2pl);
                plugin.getUCLogger().info("MarryMaster found. Hooked.");
            } else {
                plugin.getUCLogger().info("MarryMaster not compatible or not installed.");
            }
        }

        if (checkTAPI()) {
            tapi = TranslationAPI.getAPI();
            plugin.getUCLogger().info("Translation API found. We will use for item translations.");
        }

        if (checkDynmap()) {
            plugin.getUCLogger().info("Dynmap found. Hooked.");
        }
    }

    private boolean checkDynmap() {
        Plugin p = Bukkit.getPluginManager().getPlugin("dynmap");
        return p != null && p.isEnabled();
    }

    private boolean checkVault() {
        Plugin p = Bukkit.getPluginManager().getPlugin("Vault");
        return p != null && p.isEnabled();
    }

    private boolean checkSC() {
        Plugin p = Bukkit.getPluginManager().getPlugin("SimpleClans");
        return p != null && p.isEnabled();
    }

    private boolean checkMR() {
        Plugin p = Bukkit.getPluginManager().getPlugin("Marriage");
        return p != null && p.isEnabled();
    }

    private boolean checkMM2() {
        Plugin p = Bukkit.getPluginManager().getPlugin("MarriageMaster");
        return p != null && p.isEnabled() && !p.getDescription().getVersion().startsWith("1.");
    }

    private boolean checkPL() {
        Plugin p = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        return p != null && p.isEnabled();
    }

    private boolean checkPHAPI() {
        Plugin p = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return p != null && p.isEnabled();
    }

    private boolean checkTAPI() {
        Plugin p = Bukkit.getPluginManager().getPlugin("TranslationAPI");
        return p != null && p.isEnabled();
    }

    private boolean checkFac() {
        Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
        try {
            Class.forName("com.massivecraft.factions.RelationParticipator");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return p != null && p.isEnabled();
    }

    private boolean checkSFac() {
        Plugin p = Bukkit.getPluginManager().getPlugin("Factions");
        try {
            Class.forName("com.massivecraft.factions.FPlayer");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return p != null && p.isEnabled();
    }
}

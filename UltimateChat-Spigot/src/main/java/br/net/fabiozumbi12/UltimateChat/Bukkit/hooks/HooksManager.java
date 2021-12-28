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

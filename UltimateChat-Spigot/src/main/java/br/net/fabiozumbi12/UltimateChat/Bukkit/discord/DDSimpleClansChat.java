package br.net.fabiozumbi12.UltimateChat.Bukkit.discord;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.events.ChatEvent;
import net.sacredlabyrinth.phaed.simpleclans.managers.SettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DDSimpleClansChat implements Listener {

    @EventHandler
    public void onClanChat(ChatEvent event) {
        ClanPlayer clanPlayer = event.getSender();
        Clan clan = clanPlayer.getClan();
        String chatId = UChat.get().getDDSync().getConfig().getString("simple-clans-sync.clans." + clan.getTag().toUpperCase() + ".text-channel");
        if (chatId != null) {
            String rank = "";
            if (!clanPlayer.getRankDisplayName().isEmpty()) {
                rank = UChat.get().getHooks().getSc().getSettingsManager().getString(SettingsManager.ConfigField.CLANCHAT_RANK)
                        .replace("%rank%", clanPlayer.getRankDisplayName());
            }

            String message = UChat.get().getDDSync().getConfig().getString("simple-clans-sync.templates.chat-to-discord-member");
            if (clanPlayer.isLeader()) {
                message = UChat.get().getDDSync().getConfig().getString("simple-clans-sync.templates.chat-to-discord-leader");
            }
            message = message
                    .replace("{player}", clanPlayer.toPlayer().getName())
                    .replace("{rank}", rank)
                    .replace("%nick-color%", "")
                    .replace("{message}", event.getMessage());

            UChat.get().getUCJDA().sendToChannel(chatId,
                    message.replaceAll("([&" + ChatColor.COLOR_CHAR + "]([a-fA-Fk-oK-ORr0-9]))", ""));
        }
    }
}

package br.net.fabiozumbi12.UltimateChat.Bukkit;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class MuteCountDown extends BukkitRunnable {
    final String p;
    int time;

    MuteCountDown(String p, int t) {
        this.p = p;
        this.time = t * 60;
    }

    @Override
    public void run() {
        if (UChat.get().timeMute.containsKey(p)) {
            time = UChat.get().timeMute.get(p) - 1;
        }
        if (UChat.get().mutes.contains(p)) {
            if (time > 0) {
                UChat.get().timeMute.put(p, time);
            } else {
                UChat.get().timeMute.remove(p);
                UChat.get().mutes.remove(p);
                UChat.get().unMuteInAllChannels(p);
                if (Bukkit.getPlayer(p) != null) {
                    UChat.get().getLang().sendMessage(Bukkit.getPlayer(p), UChat.get().getLang().get("channel.player.unmuted.all"));
                }
                this.cancel();
            }
        } else {
            this.cancel();
        }
    }
}

package br.net.fabiozumbi12.UltimateChat.Sponge;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Arrays;

public class UCPlaceHoldersRelational {

    public UCPlaceHoldersRelational(UChat plugin) {
        PlaceholderService service = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);

        service.loadAll(this, plugin).forEach(builder -> {
            if (builder.getId().startsWith("uchat-")) {
                builder.author("FabioZumbi12");
                builder.version(plugin.instance().getVersion().get());
                try {
                    builder.buildAndRegister();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Placeholder(id = "uchat-channelname")
    public String channelName(@Source CommandSource p) {
        return UChat.get().getPlayerChannel(p).getName();
    }

    @Placeholder(id = "uchat-channelalias")
    public String channelAlias(@Source CommandSource p) {
        return UChat.get().getPlayerChannel(p).getAlias();
    }

    @Placeholder(id = "uchat-channelcolor")
    public String channelColor(@Source CommandSource p) {
        return UChat.get().getPlayerChannel(p).getColor();
    }

    @Placeholder(id = "uchat-tellwith")
    public String tellWith(@Source CommandSource p) {
        if (UChat.get().tellPlayers.containsKey(p.getName())) {
            return UChat.get().tellPlayers.get(p.getName());
        }
        return "--";
    }

    @Placeholder(id = "uchat-ignoring")
    public String ignoringPlayer(@Source CommandSource p) {
        if (UChat.get().ignoringPlayer.containsKey(p.getName())) {
            return Arrays.toString(UChat.get().ignoringPlayer.get(p.getName()).toArray());
        }
        return "--";
    }

    @Placeholder(id = "uchat-defaultchannel")
    public String defaultChannel(@Source CommandSource p) {
        return UChat.get().getDefChannel(p instanceof Player ? ((Player) p).getWorld().getName() : null).getName();
    }
}

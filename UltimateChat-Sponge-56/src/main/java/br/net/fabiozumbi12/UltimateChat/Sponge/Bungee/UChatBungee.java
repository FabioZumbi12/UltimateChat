package br.net.fabiozumbi12.UltimateChat.Sponge.Bungee;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.util.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class UChatBungee implements RawDataListener {
    private ChannelBinding.RawDataChannel chan;
    private UChatBungee listener;

    public UChatBungee(UChat plugin) {
        Sponge.getEventManager().registerListener(plugin.instance(), GameStartedServerEvent.class, (event) -> {
            this.chan = Sponge.getChannelRegistrar().createRawChannel(plugin.instance(), "bungee:uchat");
            this.listener = this;
            this.chan.addListener(Platform.Type.SERVER, this.listener);
        });
    }

    public void sendBungee(UCChannel ch, Text text) {
        this.chan.sendToAll((buf) -> {
            buf.writeUTF(UChat.get().getConfig().root().bungee.server_id);
            buf.writeUTF(ch.getAlias());
            buf.writeUTF(TextSerializers.JSON.serialize(text));
        });
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Platform.Type side) {
        if (!side.equals(Platform.Type.SERVER)) {
            return;
        }

        String id = data.readUTF();
        String ch = data.readUTF();
        String json = data.readUTF();

        UCChannel chan = UChat.get().getChannel(ch);
        if (chan == null || !chan.isBungee()) {
            return;
        }

        if (chan.getDistance() == 0) {
            if (chan.neeFocus()) {
                for (String receiver : chan.getMembers()) {
                    if (Sponge.getServer().getPlayer(receiver).isPresent()) {
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw " + receiver + " " + json);
                    }
                }
            } else {
                for (Player receiver : Sponge.getServer().getOnlinePlayers()) {
                    if (UChat.get().getPerms().channelReadPerm(receiver, chan)) {
                        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "tellraw " + receiver.getName() + " " + json);
                    }
                }
            }
        }
        Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&7Bungee message to channel " + chan.getName() + " from: " + id));
    }
}

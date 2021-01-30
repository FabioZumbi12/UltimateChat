package br.net.fabiozumbi12.ultimatechat.ultimatechatvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.io.*;
import java.util.stream.Collectors;

@Plugin(
        id = "ultimatechat_velocity",
        name = "UltimateChat Velocity",
        version = "1.9.1"
)
public class UltimateChatVelocity {

    private static final String UCHAT_CHANNEL = "bungee:uchat";

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    private final MinecraftChannelIdentifier ci = MinecraftChannelIdentifier.from(UCHAT_CHANNEL);

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getChannelRegistrar().register(ci);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent e) {

        if (!UCHAT_CHANNEL.equals(e.getIdentifier().getId())) {
            return;
        }

        ByteArrayInputStream stream = new ByteArrayInputStream(e.getData());
        DataInputStream in = new DataInputStream(stream);
        String id = "";
        String ch = "";
        String json = "";
        try {
            id = in.readUTF();
            ch = in.readUTF();
            json = in.readUTF();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        sendMessage(ch, json, id);
    }

    public void sendMessage(String ch, String json, String id) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(id);
            out.writeUTF(ch);
            out.writeUTF(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (RegisteredServer si : proxy.getAllServers().stream().filter(si -> !si.getPlayersConnected().isEmpty()).collect(Collectors.toList())) {
            si.sendPluginMessage(ci, stream.toByteArray());
        }
    }
}

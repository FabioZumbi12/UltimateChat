package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;

public interface UCDInterface {
    boolean JDAAvailable();

    void sendTellToDiscord(String text);

    void sendRawToDiscord(String text);

    void sendToDiscord(CommandSource sender, String text, UCChannel ch);

    void updateGame(String text);

    void sendCommandsToDiscord(String text);

    void sendPixelmonLegendary(String text);

    void shutdown();
}

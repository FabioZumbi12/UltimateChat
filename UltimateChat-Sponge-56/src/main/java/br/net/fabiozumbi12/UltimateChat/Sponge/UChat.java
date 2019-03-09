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

package br.net.fabiozumbi12.UltimateChat.Sponge;

import br.net.fabiozumbi12.UltimateChat.Sponge.API.UChatReloadEvent;
import br.net.fabiozumbi12.UltimateChat.Sponge.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Sponge.Bungee.UChatBungee;
import br.net.fabiozumbi12.UltimateChat.Sponge.Listeners.UCListener;
import br.net.fabiozumbi12.UltimateChat.Sponge.Listeners.UCPixelmonListener;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.UCLang;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.VersionData;
import com.google.inject.Inject;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform.Component;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Plugin(id = "ultimatechat",
        name = "UltimateChat",
        version = VersionData.VERSION,
        authors = "FabioZumbi12",
        description = "Complete and advanced chat plugin",
        dependencies = {
                @Dependency(id = "jdalibraryloader", optional = true),
                @Dependency(id = "mcclans", optional = true),
                @Dependency(id = "placeholderapi", optional = true),
                @Dependency(id = "pixelmon", optional = true),
                @Dependency(id = "nucleus", optional = true)})
public class UChat {

    private static UChat uchat;
    @Inject
    public GuiceObjectMapperFactory factory;
    public HashMap<String, String> tempChannels = new HashMap<>();
    public HashMap<String, String> tellPlayers = new HashMap<>();
    public HashMap<String, String> tempTellPlayers = new HashMap<>();
    public HashMap<String, String> respondTell = new HashMap<>();
    public List<String> mutes = new ArrayList<>();
    public List<String> isSpy = new ArrayList<>();
    public List<String> command = new ArrayList<>();
    public HashMap<String, Integer> timeMute = new HashMap<>();
    public List<String> msgTogglePlayers = new ArrayList<>();
    protected HashMap<String, List<String>> ignoringPlayer = new HashMap<>();
    private UCLogger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;
    @Inject
    private Game game;
    @Inject
    private PluginContainer plugin;
    private UCConfig config;
    private UCCommands cmds;
    private Server serv;
    private EconomyService econ;
    private UCPerms perms;
    private uChatAPI ucapi;
    private UCDInterface UCJDA;
    private UCLang lang;
    private UCVHelper helper;
    private HashMap<List<String>, UCChannel> channels;
    private UCPixelmonListener pixelListener;
    private UChatBungee bungee;
    public UChatBungee getBungee(){
        return this.bungee;
    }

    public static UChat get() {
        return uchat;
    }

    public UCLogger getLogger() {
        return logger;
    }

    public File configDir() {
        return this.configDir.toFile();
    }

    public Game getGame() {
        return this.game;
    }

    public PluginContainer instance() {
        return this.plugin;
    }

    public UCConfig getConfig() {
        return this.config;
    }

    public UCPerms getPerms() {
        return this.perms;
    }

    EconomyService getEco() {
        return this.econ;
    }

    public UCCommands getCmds() {
        return this.cmds;
    }

    public uChatAPI getAPI() {
        return this.ucapi;
    }

    public UCDInterface getUCJDA() {
        return this.UCJDA;
    }

    public UCLang getLang() {
        return this.lang;
    }

    public UCVHelper getVHelper() {
        return this.helper;
    }

    public HashMap<List<String>, UCChannel> getChannels() {
        return this.channels;
    }

    public void setChannels(HashMap<List<String>, UCChannel> channels) {
        this.channels = channels;
    }

    @Listener
    public void onServerStart(GamePostInitializationEvent event) {
        try {
            uchat = this;
            this.serv = Sponge.getServer();

            this.logger = new UCLogger(this.serv);
            this.config = new UCConfig(this.factory);
            this.lang = new UCLang();

            //set compat perms
            this.setCompatperms();

            logger.info("Init commands module...");
            this.cmds = new UCCommands(this);

            game.getEventManager().registerListeners(plugin, new UCListener());

            //init other features
            //JDA
            registerJDA();

            logger.info("Init API module...");
            this.ucapi = new uChatAPI();

            //register placeholdersapi
            if (Sponge.getPluginManager().getPlugin("placeholderapi").isPresent()) {
                new UCPlaceHoldersRelational(this);
            }

            getLogger().logClear("\n"
                    + "&b  _    _ _ _   _                 _        _____ _           _  \n"
                    + " | |  | | | | (_)               | |      / ____| |         | |  \n"
                    + " | |  | | | |_ _ _ __ ___   __ _| |_ ___| |    | |__   __ _| |_ \n"
                    + " | |  | | | __| | '_ ` _ \\ / _` | __/ _ \\ |    | '_ \\ / _` | __|\n"
                    + " | |__| | | |_| | | | | | | (_| | ||  __/ |____| | | | (_| | |_ \n"
                    + "  \\____/|_|\\__|_|_| |_| |_|\\__,_|\\__\\___|\\_____|_| |_|\\__,_|\\__|\n"
                    + "                                                                \n"
                    + "&a" + plugin.getName() + " " + plugin.getVersion().get() + " enabled!\n");

            if (this.UCJDA != null) {
                this.UCJDA.sendRawToDiscord(lang.get("discord.start"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        this.bungee = new UChatBungee(get());

        if (this.UCJDA != null) {
            this.UCJDA.sendRawToDiscord(lang.get("discord.online"));
        }
    }

    private void setCompatperms() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        //init perms
        try {
            String v = this.game.getPlatform().getContainer(Component.API).getVersion().get();
            getLogger().info("Sponge version " + v);

            if (v.startsWith("5") || v.startsWith("6")) {
                this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").getConstructor().newInstance();
                this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper56").getConstructor().newInstance();
                getLogger().info("Permissions set for API 5 and 6");
            }
            if (v.startsWith("7")) {
                this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms7").getConstructor().newInstance();
                this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper7").getConstructor().newInstance();
                getLogger().info("Permissions set for API 7");
            }
            if (v.startsWith("8")) {
                this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms8").getConstructor().newInstance();
                this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper8").getConstructor().newInstance();
                getLogger().info("Permissions set for API 8");
            }
        } catch (Exception e) {
            try {
                switch (this.config.root().api.sponge_api) {
                    case 5:
                        this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").getConstructor().newInstance();
                        this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper56").getConstructor().newInstance();
                        getLogger().info("Permissions set to default classes for API 5");
                    case 6:
                        this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms56").getConstructor().newInstance();
                        this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper56").getConstructor().newInstance();
                        getLogger().info("Permissions set to default classes for API 6");
                    case 7:
                        this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms7").getConstructor().newInstance();
                        this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper7").getConstructor().newInstance();
                        getLogger().info("Permissions set to default classes for API 7");
                    case 8:
                        this.perms = (UCPerms) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCPerms8").getConstructor().newInstance();
                        this.helper = (UCVHelper) Class.forName("br.net.fabiozumbi12.UltimateChat.Sponge.UCVHelper8").getConstructor().newInstance();
                        getLogger().info("Permissions set to default classes for API 8");
                }
            } catch (NoSuchMethodException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void reload() throws IOException {
        this.cmds.removeCmds();
        this.channels = null;
        this.config = new UCConfig(factory);
        this.lang = new UCLang();

        try {
            setCompatperms();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.cmds = new UCCommands(this);

        registerJDA();

        //fire event
        UChatReloadEvent event = new UChatReloadEvent();
        Sponge.getEventManager().post(event);
    }

    protected void registerJDA() {
        if (checkJDA()) {
            this.logger.info("JDA LibLoader is present...");
            if (this.UCJDA != null) {
                this.UCJDA.shutdown();
                this.UCJDA = null;
                if (Sponge.getPluginManager().getPlugin("pixelmon").isPresent()) {
                    game.getEventManager().unregisterListeners(pixelListener);
                }
            }
            if (config.root().discord.use) {
                if (Sponge.getPluginManager().getPlugin("pixelmon").isPresent()) {
                    pixelListener = new UCPixelmonListener(this);
                    game.getEventManager().registerListeners(plugin, pixelListener);
                    logger.info("Pixelmon Legendary announces enabled!");
                }

                this.UCJDA = new UCDiscord(this);
                if (!this.UCJDA.JDAAvailable()) {
                    this.UCJDA = null;
                    this.logger.info("JDA is not available due errors before.\n" +
                            "Hint: If you updated UChat, check if you need to update JDALibLoader too!");
                } else {
                    this.logger.info("JDA connected and ready to use!");
                }
            }
        }
    }

    private boolean checkJDA() {
        return this.game.getPluginManager().getPlugin("jdalibraryloader").isPresent();
    }

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    @Listener
    public void onReloadPlugins(GameReloadEvent event) {
        try {
            reload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Listener
    public void onStopServer(GameStoppingServerEvent e) {
        if (this.UCJDA != null) {
            this.UCJDA.sendRawToDiscord(lang.get("discord.stop"));
        }
    }

    @Listener
    public void onStopServer(GameStoppedServerEvent e) {
        if (this.UCJDA != null) {
            this.UCJDA.shutdown();
        }
        get().getLogger().info(plugin.getName() + " disabled!");
    }

    //----------- channels
    public UCChannel getChannel(String alias) {
        for (List<String> aliases : UChat.get().getChannels().keySet()) {
            if (aliases.contains(alias.toLowerCase())) {
                return UChat.get().getChannels().get(aliases);
            }
        }
        return null;
    }

    public void unMuteInAllChannels(String player) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (ch.isMuted(player)) {
                ch.unMuteThis(player);
            }
        }
    }

    public void muteInAllChannels(String player) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (!ch.isMuted(player)) {
                ch.muteThis(player);
            }
        }
    }

    public UCChannel getDefChannel(String world) {
        UCChannel ch = getChannel(getConfig().root().general.default_channels.default_channel);
        if (world != null) {
            UCChannel wch = getChannel(getConfig().root().general.default_channels.worlds.get(world).channel);
            if (wch == null) {
                UChat.get().getLogger().warning("Defalt channel not found with alias '" + getConfig().root().general.default_channels.worlds.get(world).channel + "'. Fix this setting to a valid channel alias.");
            } else {
                ch = wch;
            }
        }
        return ch;
    }

    public List<String> getChAliases() {
        List<String> aliases = new ArrayList<>();
        for (List<String> alias : UChat.get().getChannels().keySet()) {
            if (alias == null) {
                continue;
            }
            aliases.addAll(alias);
        }
        return aliases;
    }

    public UCChannel getPlayerChannel(CommandSource p) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (ch.isMember(p)) {
                return ch;
            }
        }
        return p instanceof Player ? getDefChannel(((Player) p).getWorld().getName()) : getDefChannel(null);
    }

}

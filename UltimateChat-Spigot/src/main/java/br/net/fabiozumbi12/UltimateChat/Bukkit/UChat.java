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

package br.net.fabiozumbi12.UltimateChat.Bukkit;

import at.pcgamingfreaks.MarriageMaster.Bukkit.API.MarriageMasterPlugin;
import at.pcgamingfreaks.MarriageMaster.Bukkit.MarriageMaster;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.UChatReloadEvent;
import br.net.fabiozumbi12.UltimateChat.Bukkit.API.uChatAPI;
import br.net.fabiozumbi12.UltimateChat.Bukkit.bungee.UChatBungee;
import br.net.fabiozumbi12.UltimateChat.Bukkit.config.UCConfig;
import br.net.fabiozumbi12.UltimateChat.Bukkit.config.UCLang;
import br.net.fabiozumbi12.UltimateChat.Bukkit.discord.UCDInterface;
import br.net.fabiozumbi12.UltimateChat.Bukkit.discord.UCDiscord;
import br.net.fabiozumbi12.UltimateChat.Bukkit.discord.UCDiscordSync;
import br.net.fabiozumbi12.UltimateChat.Bukkit.metrics.Metrics;
import br.net.fabiozumbi12.translationapi.TranslationAPI;
import br.net.fabiozumbi12.translationapi.TranslationCore;
import com.lenis0012.bukkit.marriage2.Marriage;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class UChat extends JavaPlugin {

    static boolean SClans;
    static SimpleClans sc;
    static boolean MarryReloded;
    static boolean MarryMasterV1;
	static boolean MarryMasterV2;
    static MarriageMaster mm;
    static MarriageMasterPlugin mm2;
    static Marriage mapi;
    static TranslationCore tapi;
    static boolean PlaceHolderAPI;
    static boolean Factions;
    private static boolean Vault = false;
    private static UChat uchat;
    List<String> isSpy = new ArrayList<>();
    List<String> msgTogglePlayers = Collections.synchronizedList(new ArrayList<>());
    protected List<String> command = Collections.synchronizedList(new ArrayList<>());
    Map<String, String> tempChannels = Collections.synchronizedMap(new HashMap<>());
    Map<String, String> tellPlayers = Collections.synchronizedMap(new HashMap<>());
    Map<String, String> tempTellPlayers = Collections.synchronizedMap(new HashMap<>());
    Map<String, String> respondTell = Collections.synchronizedMap(new HashMap<>());
    Map<String, List<String>> ignoringPlayer = Collections.synchronizedMap(new HashMap<>());
    List<String> mutes = new ArrayList<>();
    HashMap<String, Integer> timeMute = new HashMap<>();
    private FileConfiguration amConfig;
    private int index = 0;
    private UCListener listener;
    private HashMap<List<String>, UCChannel> channels;
    private UCLogger logger;
    private UCConfig config;
    private UCDInterface UCJDA;
    private UCLang lang;
    private Permission perms;
    private Economy econ;
    private Chat chat;
    private boolean isRelation;
    private uChatAPI ucapi;
    private UCDiscordSync sync;

    public static UChat get() {
        return uchat;
    }

    public UCDiscordSync getDDSync() {
        return this.sync;
    }

    public HashMap<List<String>, UCChannel> getChannels() {
        return this.channels;
    }

    public void setChannels(HashMap<List<String>, UCChannel> channels) {
        this.channels = channels;
    }

    private FileConfiguration getAMConfig() {
        return this.amConfig;
    }

    public UCLogger getUCLogger() {
        return this.logger;
    }

    public UCConfig getUCConfig() {
        return this.config;
    }

    public UCDInterface getUCJDA() {
        return this.UCJDA;
    }

    public UCLang getLang() {
        return this.lang;
    }

    public Permission getVaultPerms() {
        if (Vault && perms != null) {
            return this.perms;
        }
        return null;
    }

    public Economy getVaultEco() {
        if (Vault && econ != null) {
            return this.econ;
        }
        return null;
    }

    public Chat getVaultChat() {
        if (Vault && chat != null) {
            return this.chat;
        }
        return null;
    }

    public boolean isRelation() {
        return this.isRelation;
    }

    public uChatAPI getAPI() {
        return this.ucapi;
    }

    public PluginDescriptionFile getPDF() {
        return this.getDescription();
    }

    public void onEnable() {
        try {
            uchat = this;
            logger = new UCLogger(this);
            config = new UCConfig(this);
            lang = new UCLang();
            amConfig = new YamlConfiguration();
            //check hooks
            Vault = checkVault();
            SClans = checkSC();
            MarryReloded = checkMR();
            MarryMasterV1 = checkMM();
            MarryMasterV2 = checkMM2();
            boolean protocolLib = checkPL();
            PlaceHolderAPI = checkPHAPI();
            Factions = checkFac();
            listener = new UCListener();

            getServer().getPluginCommand("uchat").setExecutor(listener);
            getServer().getPluginManager().registerEvents(listener, this);
            getServer().getPluginManager().registerEvents(new UCChatProtection(), this);

            getServer().getMessenger().registerOutgoingPluginChannel(this, "bungee:uchat");
            getServer().getMessenger().registerIncomingPluginChannel(this, "bungee:uchat", new UChatBungee());

            //register aliases
            registerAliases();

            if (protocolLib) {
                logger.info("ProtocolLib found. Hooked.");
            }

            if (PlaceHolderAPI) {
                try {
                    Class.forName("me.clip.placeholderapi.expansion.Relational");
                    if (new UCPlaceHoldersRelational(this).register()) {
                        isRelation = true;
                        logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders with relational tag feature.");
                    }
                } catch (ClassNotFoundException ex) {
                    if (new UCPlaceHolders(this).register()) {
                        isRelation = false;
                        logger.info("PlaceHolderAPI found. Hooked and registered some chat placeholders.");
                    }
                }
            }

            if (MarryReloded) {
                mapi = MarriageAPI.getInstance();
                logger.info("Marriage Reloaded found. Hooked.");
            }

            if (MarryMasterV1) {
	            mm = (MarriageMaster) Bukkit.getPluginManager().getPlugin("MarriageMaster");
	            logger.info("MarryMaster v1.x found. Hooked.");
            }
	        if (MarryMasterV2) {
		        mm2 = (MarriageMasterPlugin) Bukkit.getPluginManager().getPlugin("MarriageMaster");
		        logger.info("MarryMaster found. Hooked.");
	        }

            if (SClans) {
                sc = SimpleClans.getInstance();
                logger.info("SimpleClans found. Hooked.");
            }

            if (checkTAPI()) {
                tapi = TranslationAPI.getAPI();
                logger.info("Translation API found. We will use for item translations.");
            }

            if (Vault) {
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                RegisteredServiceProvider<Chat> rschat = getServer().getServicesManager().getRegistration(Chat.class);
                RegisteredServiceProvider<Permission> rsperm = getServer().getServicesManager().getRegistration(Permission.class);
                //Economy
                if (rsp == null) {
                    logger.warning("Vault found Economy, but for some reason cant be used.");
                } else {
                    econ = rsp.getProvider();
                    logger.info("Vault economy found. Hooked.");
                }
                //Chat
                if (rschat == null) {
                    logger.warning("Vault found chat, but for some reason cant be used.");
                } else {
                    chat = rschat.getProvider();
                    logger.info("Vault chat found. Hooked.");
                }
                //Perms
                if (rsperm == null) {
                    logger.warning("Vault found permissions, but for some reason cant be used.");
                } else {
                    perms = rsperm.getProvider();
                    logger.info("Vault perms found. Hooked.");
                }
            }

            logger.info("Init API module...");
            this.ucapi = new uChatAPI();

            //init other features
            //JDA
            registerJDA();

            initAutomessage();

            getUCLogger().info("Server Version: " + getServer().getBukkitVersion());
            getUCLogger().logClear(ChatColor.translateAlternateColorCodes('&', "\n"
                    + "&b  _    _ _ _   _                 _        _____ _           _  \n"
                    + " | |  | | | | (_)               | |      / ____| |         | |  \n"
                    + " | |  | | | |_ _ _ __ ___   __ _| |_ ___| |    | |__   __ _| |_ \n"
                    + " | |  | | | __| | '_ ` _ \\ / _` | __/ _ \\ |    | '_ \\ / _` | __|\n"
                    + " | |__| | | |_| | | | | | | (_| | ||  __/ |____| | | | (_| | |_ \n"
                    + "  \\____/|_|\\__|_|_| |_| |_|\\__,_|\\__\\___|\\_____|_| |_|\\__,_|\\__|\n"
                    + "                                                                \n"
                    + "&a" + getDescription().getFullName() + " enabled!\n"));

            if (this.UCJDA != null) {
                this.UCJDA.sendRawToDiscord(lang.get("discord.start"));
            }

            //Metrics
            try {
                Metrics metrics = new Metrics(this);
                metrics.addCustomChart(new Metrics.SingleLineChart("chat_channels", () -> this.channels.size()));
                if (metrics.isEnabled())
                    logger.info("Metrics enabled! See our stats here: https://bstats.org/plugin/bukkit/UChat");
            } catch (Exception ex) {
                logger.info("Metrics not enabled due errors: " + ex.getLocalizedMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            super.setEnabled(false);
        }
    }

    public void reload() {
        this.getServer().getScheduler().cancelTasks(this);
        try {
            this.config = new UCConfig(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.lang = new UCLang();
        this.registerAliases();

        this.registerJDA();
        this.initAutomessage();

        //ping other plugins when uchat reload
        UChatReloadEvent reloadEvent = new UChatReloadEvent();
        Bukkit.getPluginManager().callEvent(reloadEvent);
    }

    private void registerJDA() {
        if (checkJDA()) {
            this.logger.info("JDA LibLoader is present...");
            if (this.UCJDA != null) {
                Bukkit.getScheduler().cancelTask(this.UCJDA.getTaskId());
                this.UCJDA.shutdown();
                this.UCJDA = null;
            }
            if (getUCConfig().getBoolean("discord.use")) {
                this.UCJDA = new UCDiscord(this);
                if (!this.UCJDA.JDAAvailable()) {
                    this.UCJDA = null;
                    this.logger.info("JDA is not available due errors before...");
                } else {
                    this.sync = new UCDiscordSync();
                    this.logger.info("JDA connected and ready to use!");
                }
            }
        }
    }

    private void initAutomessage() {
        File am = new File(getDataFolder(), "automessages.yml");
        try {
            if (!am.exists()) {
                am.createNewFile();
            }
            this.amConfig.load(am);

            getAMConfig().options().header("\n"
                    + "AutoMessages configuration for UltimateChat:\n"
                    + "\n"
                    + "You can use the placeholder {clicked} on \"onclick\" to get the name of player who clicked on message.\n"
                    + "\n"
                    + "This is the default configuration:\n"
                    + "\n"
                    + "interval: 60 #Interval in seconds.\n"
                    + "silent: true #Do not log the messages on console?"
                    + "messages\n"
                    + "  '0': #The index (order) to show the messages.\n"
                    + "    minPlayers: 4 #Minimun players to show the message. Set to 0 to always send the message.\n"
                    + "    text: Your plain text message here! #Plain text.\n"
                    + "    hover: Your hover text message here! #Hover text.\n"
                    + "    onclick: Command on click here! #On click text with placeholder {clicked} to show who clicked.\n"
                    + "    suggest: Put the text to suggest on player chat on click.\n"
                    + "    url: http://google.com # Some url to go on click. Need to have \"http://\" to url work.\n"
                    + "\n"
                    + "*If you dont want hover message or click command, set to '' (blank quotes)\n"
                    + "\n");
            if (!getAMConfig().isConfigurationSection("messages")) {
                getAMConfig().set("enable", true);
                getAMConfig().set("silent", true);
                getAMConfig().set("interval", 60);
                getAMConfig().set("messages.0.minPlayers", 4);
                getAMConfig().set("messages.0.text", "This is UChat Automessage! Put your plain text message here!");
                getAMConfig().set("messages.0.hover", "Your hover text message here!");
                getAMConfig().set("messages.0.onclick", "Command on click here!");
                getAMConfig().set("messages.0.suggest", "Text to suggest on click");
                getAMConfig().set("messages.0.url", "http://google.com");
            }

            getAMConfig().save(am);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (!getAMConfig().getBoolean("enable")) {
            return;
        }

        int total = getAMConfig().getConfigurationSection("messages").getKeys(false).size();
        int loop = getAMConfig().getInt("interval");
        boolean silent = getAMConfig().getBoolean("silent");

        Bukkit.getScheduler().scheduleSyncRepeatingTask(uchat, () -> {
            if (getAMConfig().isConfigurationSection("messages." + index)) {
                int plays = getAMConfig().getInt("messages." + index + ".minPlayers");
                String text = getAMConfig().getString("messages." + index + ".text", "");
                String hover = getAMConfig().getString("messages." + index + ".hover", "");
                String onclick = getAMConfig().getString("messages." + index + ".onclick", "");
                String suggest = getAMConfig().getString("messages." + index + ".suggest", "");
                String url = getAMConfig().getString("messages." + index + ".url", "");

                String cmd = text;
                if (hover.length() > 1) {
                    cmd = cmd + " " + getUCConfig().getString("broadcast.on-hover") + hover;
                }
                if (onclick.length() > 1) {
                    cmd = cmd + " " + getUCConfig().getString("broadcast.on-click") + onclick;
                }
                if (suggest.length() > 1) {
                    cmd = cmd + " " + getUCConfig().getString("broadcast.suggest") + suggest;
                }
                if (url.length() > 1) {
                    cmd = cmd + " " + getUCConfig().getString("broadcast.url") + url;
                }
                if (plays == 0 || getServer().getOnlinePlayers().size() >= plays) {
                    UCUtil.sendBroadcast(Bukkit.getConsoleSender(), cmd.split(" "), silent);
                }
            }
            if (index + 1 >= total) {
                index = 0;
            } else {
                index++;
            }
        }, loop * 20, loop * 20);
    }

    public void onDisable() {
        if (this.UCJDA != null) {
            this.UCJDA.sendRawToDiscord(lang.get("discord.stop"));
            this.sync.unload();
            this.UCJDA.shutdown();
        }
        getUCLogger().info(getDescription().getFullName() + " disabled!");
    }

    /**
     * Needed to be called after register or unregister channels.
     */
    void registerAliases() {
        registerAliases("channel", getChAliases(), true, null, listener);
        registerAliases("tell", config.getTellAliases(), true, null, listener);
        registerAliases("umsg", config.getMsgAliases(), true, null, listener);
        registerAliases("ubroadcast", config.getBroadcastAliases(), config.getBoolean("broadcast.enable"), null, listener);
    }

    public void registerAliases(String name, List<String> aliases, boolean shouldReg, String perm, Object cmdListener) {
        List<String> aliases1 = new ArrayList<>(aliases);

        for (Command cmd : PluginCommandYamlParser.parse(uchat)) {
            if (cmd.getName().equals(name)) {
                if (shouldReg) {
                    getServer().getPluginCommand(name).setExecutor((CommandExecutor) cmdListener);
                    getServer().getPluginCommand(name).setTabCompleter((TabCompleter) cmdListener);
                    cmd.setAliases(aliases1);
                    cmd.setLabel(name);
                    if (perm != null) cmd.setPermission(perm);
                }
                try {
                    Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                    field.setAccessible(true);
                    CommandMap commandMap = (CommandMap) (field.get(getServer().getPluginManager()));
                    if (shouldReg) {
                        Method register = commandMap.getClass().getMethod("register", String.class, Command.class);
                        register.invoke(commandMap, cmd.getName(), cmd);
                        ((PluginCommand) cmd).setExecutor((CommandExecutor) cmdListener);
                        ((PluginCommand) cmd).setTabCompleter((TabCompleter) cmdListener);
                    } else if (getServer().getPluginCommand(name).isRegistered()) {
                        getServer().getPluginCommand(name).unregister(commandMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //------- channels

    public UCChannel getChannel(String alias) {
        for (List<String> aliases : UChat.get().getChannels().keySet()) {
            if (aliases.contains(alias.toLowerCase())) {
                return UChat.get().getChannels().get(aliases);
            }
        }
        return null;
    }

    List<String> getChAliases() {
        List<String> aliases = new ArrayList<>(Arrays.asList(config.getString("general.channel-cmd-aliases").replace(" ", "").split(",")));
        for (List<String> alias : UChat.get().getChannels().keySet()) {
            aliases.addAll(alias);
        }
        return aliases;
    }

    public UCChannel getPlayerChannel(CommandSender p) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (ch.isMember(p)) {
                return ch;
            }
        }
        return p instanceof Player ? getDefChannel(((Player) p).getWorld().getName()) : getDefChannel(null);
    }


    void unMuteInAllChannels(String player) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (ch.isMuted(player)) {
                ch.unMuteThis(player);
            }
        }
    }

    void muteInAllChannels(String player) {
        for (UCChannel ch : UChat.get().getChannels().values()) {
            if (!ch.isMuted(player)) {
                ch.muteThis(player);
            }
        }
    }

    UCChannel getDefChannel(String world) {
        UCChannel ch = getChannel(config.getString("general.default-channels.default-channel"));
        if (world != null) {
            UCChannel wch = getChannel(config.getString("general.default-channels.worlds." + world + ".channel"));
            if (wch == null) {
                UChat.get().getLogger().severe("Default channel not found with alias '" + config.getString("general.default-channels.worlds." + world) + "'. Fix this setting to a valid channel alias.");
            } else {
                ch = wch;
            }
        }
        return ch;
    }

    private boolean checkJDA() {
        Plugin p = Bukkit.getPluginManager().getPlugin("JDALibLoaderBukkit");
        return p != null && p.isEnabled();
    }

    //check if plugin Vault is installed
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

    private boolean checkMM() {
        Plugin p = Bukkit.getPluginManager().getPlugin("MarriageMaster");
        return p != null && p.isEnabled() && p.getDescription().getVersion().startsWith("1.");
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
}

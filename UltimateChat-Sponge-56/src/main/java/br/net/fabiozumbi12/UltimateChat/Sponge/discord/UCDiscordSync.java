package br.net.fabiozumbi12.UltimateChat.Sponge.discord;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCCommands;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.MainCategory;
import br.net.fabiozumbi12.UltimateChat.Sponge.config.ProtectionsCategory;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class UCDiscordSync {
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
    private DiscordSync sync;
    private UUID taskId = null;
    private File syncFile = new File(UChat.get().configDir(), "discord-sync.conf");
    private final CommandManager manager;

    public UCDiscordSync(ObjectMapperFactory factory) {
        manager = Sponge.getCommandManager();
        try {
            if (!syncFile.exists()) {
                syncFile.createNewFile();
            }

            String header = ""
                    + "Uchat Discord Syncronization file\n"
                    + "Author: FabioZumbi12\n"
                    + "We recommend you to use NotePad++ to edit this file and avoid TAB errors!\n"
                    + "------------------------------------------------------------------------\n"
                    + "    In this file you can configure the discord roles synchronization    \n"
                    + "             between online players and your Discord groups             \n"
                    + "------------------------------------------------------------------------\n"
                    + "\n"
                    + "IMPORTANT NOTE: The BOT need to have a role assigned with MANAGE ROLES and MANAGE NICKNAMES\n"
                    + "granted, and this role need to be UNDER other roles you want to give to your Discord members!\n"
                    + "Check our WIKI to see how to setup Synchronization: https://bit.ly/2F3UoRf"
                    + "\n";

            cfgLoader = HoconConfigurationLoader.builder().setFile(syncFile).build();
            configRoot = cfgLoader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory).setShouldCopyDefaults(true).setHeader(header));
            sync = configRoot.getValue(TypeToken.of(DiscordSync.class), new DiscordSync());
        } catch (Exception ignored){}

        if (manager.get("discord-sync").isPresent()) {
            manager.removeMapping(manager.get("discord-sync").get());
        }

        if (this.sync.enable_sync) {
            CommandSpec listCmd = CommandSpec.builder()
                    .description(Text.of("Command to list all group ids"))
                    .permission("uchat.discord-sync.cmd.addgroup")
                    .executor((src, args) -> {
                        StringBuilder list = new StringBuilder();
                        for (Map.Entry<String, List<String>> key : this.sync.group_ids.entrySet()) {
                            list.append("&3- ").append(key.getKey()).append(": &b").append(key.getValue()).append("\n");
                        }
                        UChat.get().getLang().sendMessage(src,  UChat.get().getLang().get("discord.sync-group-list") + "\n" + list.toString());
                        return CommandResult.success();
                            })
                    .build();

            CommandSpec addGroupCmd = CommandSpec.builder()
                    .arguments(GenericArguments.string(Text.of("group")), GenericArguments.string(Text.of("id")))
                    .description(Text.of("Command to add in-game groups"))
                    .permission("uchat.discord-sync.cmd.addgroup")
                    .executor((src, args) -> {
                        String group = args.<String>getOne("group").get();
                        String id = args.<String>getOne("id").get();
                        List<String> groups = this.sync.group_ids.getOrDefault(group, new ArrayList<>());
                        if (!groups.contains(id)) {
                            groups.add(id);
                        }
                        this.sync.group_ids.put(group, groups);
                        saveConfig();
                        UChat.get().getLang().sendMessage(src,  UChat.get().getLang().get("discord.sync-groupadded")
                                .replace("{group}", group)
                                .replace("{id}", id));
                        return CommandResult.success();
                    })
                    .build();

            CommandSpec remGroupCmd = CommandSpec.builder()
                    .arguments(GenericArguments.string(Text.of("group")))
                    .description(Text.of("Command to remove in-game groups"))
                    .permission("uchat.discord-sync.cmd.addgroup")
                    .executor((src, args) -> {
                        String group = args.<String>getOne("group").get();
                        this.sync.group_ids.remove(group);
                        saveConfig();
                        UChat.get().getLang().sendMessage(src,  UChat.get().getLang().get("discord.sync-groupremoved")
                                .replace("{group}", group));
                        return CommandResult.success();
                    })
                    .build();

            CommandSpec generateCmd = CommandSpec.builder()
                    .arguments(GenericArguments.string(Text.of("player")))
                    .description(Text.of("Command to generate a new key to sync an in-game nick to discord id"))
                    .permission("uchat.discord-sync.cmd.generate")
                    .executor((src, args) -> {
                        if (!args.hasAny("group") && src instanceof Player){
                            Player p = (Player) src;
                            String code = getSaltString();
                            if (getPlayerPending(p.getName()) != null) {
                                code = getPlayerPending(p.getName());
                            }
                            if (addPendingCode(p.getName(), code)) {
                                Text fancy = Text.builder().append(UCUtil.toText(UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-done").replace("{code}", code)))
                                        .onClick(TextActions.suggestCommand(code))
                                        .onHover(TextActions.showText(UCUtil.toText(UChat.get().getLang().get("discord.sync-click"))))
                                        .build();
                                p.sendMessage(fancy);
                            } else {
                                Text fancy = Text.builder().append(UCUtil.toText(UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-fail"))).build();
                                p.sendMessage(fancy);
                            }
                            return CommandResult.success();
                        }
                        if (args.hasAny("group") && src.hasPermission("uchat.discord-sync.cmd.generate.others")){
                            String p = args.<String>getOne("group").get();
                            String code = getSaltString();
                            if (getPlayerPending(p) != null) {
                                code = getPlayerPending(p);
                            }
                            if (addPendingCode(p, code)) {
                                Text fancy = Text.builder().append(UCUtil.toText(UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-done").replace("{code}", code)))
                                        .onClick(TextActions.suggestCommand(code))
                                        .onHover(TextActions.showText(UCUtil.toText(UChat.get().getLang().get("discord.sync-click"))))
                                        .build();
                                src.sendMessage(fancy);
                            } else {
                                Text fancy = Text.builder().append(UCUtil.toText(UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-fail"))).build();
                                src.sendMessage(fancy);
                            }
                            return CommandResult.success();
                        }
                        throw new CommandException(Text.of(), true);
                    })
                    .build();

            CommandSpec unlinkCmd = CommandSpec.builder()
                    .arguments(GenericArguments.string(Text.of("player")))
                    .description(Text.of("Command to disconnect form discord"))
                    .permission("uchat.discord-sync.cmd.unlink")
                    .executor((src, args) -> {
                        if (!args.hasAny("player") && src instanceof Player){
                            Player p = (Player) src;
                            if (getPlayerDDId(p.getName()) != null) {
                                unlink(p.getName());
                                UChat.get().getLang().sendMessage(p, "discord.sync-unlink");
                            } else {
                                UChat.get().getLang().sendMessage(p, "discord.sync-notlink");
                            }
                            return CommandResult.success();
                        }
                        if (args.hasAny("player") && src.hasPermission("uchat.discord-sync.cmd.unlink.others")){
                            String p = args.<String>getOne("player").get();
                            if (getPlayerDDId(p) != null) {
                                unlink(p);
                                UChat.get().getLang().sendMessage(src, "discord.sync-unlink");
                            } else {
                                UChat.get().getLang().sendMessage(src, "discord.sync-notlink");
                            }
                            return CommandResult.success();
                        }
                        throw new CommandException(Text.of(), true);
                    })
                    .build();

            manager.register(UChat.get().instance(), CommandSpec.builder()
                    .permission("uchat.discord-sync.cmd.base")
                    .description(Text.of("Commando to manage uchat Discord Synchronization"))
                    .executor((src, args) -> {
                        src.sendMessage(UCUtil.toText("&b---------------- " + UChat.get().instance().getName() + " " + UChat.get().instance().getVersion().get() + " ----------------"));
                        src.sendMessage(UCUtil.toText("&bDeveloped by &6" + UChat.get().instance().getAuthors().get(0) + "."));
                        src.sendMessage(UCUtil.toText("&bDiscord Sync Commands [&6/dd-sync gen|unlink&b]."));
                        src.sendMessage(UCUtil.toText("&b-----------------------------------------------------"));
                        return CommandResult.success();
                    })
                    .child(listCmd, "list")
                    .child(addGroupCmd, "addgroup")
                    .child(remGroupCmd, "removegroup")
                    .child(generateCmd, "gen")
                    .child(unlinkCmd, "unlink")
                    .build(), "discord-sync", "dd-sync");

            final int interval = this.sync.update_interval;


            taskId = Sponge.getScheduler().createAsyncExecutor(UChat.get()).scheduleAtFixedRate(() -> {
                if (this.sync.group_ids.isEmpty())
                    return;

                final int[] delay = {0};
                Sponge.getServer().getOnlinePlayers().forEach(p -> {
                    if (getPlayerDDId(p.getName()) != null) {
                        String pId = getPlayerDDId(p.getName());
                        Sponge.getScheduler().createAsyncExecutor(UChat.get()).schedule(() -> {

                            String nick = "";
                            if (this.sync.name.to_discord) {
                                if (this.sync.name.use_display_name)
                                    nick = p.getDisplayNameData().displayName().get().toPlain();
                                else
                                    nick = p.getName();
                            }
                            String group = null;
                            try {
                                group = UChat.get().getPerms().getGroupAndTag(p).getIdentifier();
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            List<String> roles = getDDRoleByInGameGroup(group);

                            if (this.sync.require_perms)
                                roles.removeIf(r -> !p.hasPermission("uchat.discord-sync.role." + r));

                            if (!roles.isEmpty()) {
                                UChat.get().getUCJDA().setPlayerRole(pId, roles, nick, getConfigRoles());
                            }

                            delay[0] += 500;
                        }, delay[0], TimeUnit.MILLISECONDS);
                    }
                });
            }, 1, interval < 1 ? 1 : interval, TimeUnit.SECONDS).getTask().getUniqueId();

            UChat.get().getLogger().info("- Discord Sync in use!");
        }
        saveConfig();
    }

    public void unload() {
        Sponge.getScheduler().getTaskById(this.taskId).get().cancel();
    }

    String getConnectedPlayers(){
        StringBuilder list = new StringBuilder();
        try {
            for (Map.Entry<String, String> key : this.sync.sync_database.sync_players.entrySet()) {
                list.append(key.getKey()).append(": `").append(key.getValue()).append("`\n");
            }
        } catch (Exception ignored){}
        return list.toString();
    }

    @Nullable String getGuidId(){
        return this.sync.guild_id;
    }

    boolean isDDAdminRole(String role) {
        return this.sync.discord_cmds.admin_roles.contains(role);
    }

    String getDDAdminCommand() {
        return this.sync.discord_cmds.admin;
    }

    String getDDCommand() {
        return this.sync.discord_cmds.connect;
    }

    String getDDHelpCommand() {
        return this.sync.discord_cmds.help;
    }

    private boolean addPendingCode(String player, String code) {
        if (getPlayerDDId(player) != null) {
            return false;
        }
        this.sync.sync_database.pending_codes.put(code, player);
        saveConfig();
        return true;
    }

    @Nullable
    private String getPlayerPending(String player) {
        try {
            for (Map.Entry<String, String> key : this.sync.sync_database.pending_codes.entrySet()) {
                if (key.getValue().equals(player)) return key.getKey();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    String getPendentCode(String code) {
        return this.sync.sync_database.pending_codes.get(code);
    }

    void setPlayerDDId(String ddId, String nickName,@Nullable String code) {
        if (code != null) this.sync.sync_database.pending_codes.remove(code);
        this.sync.sync_database.sync_players.put(nickName, ddId);
        saveConfig();
    }

    @Nullable String getSyncNickName(String ddId) {
        try {
            for (Map.Entry<String, String> key : this.sync.sync_database.sync_players.entrySet()) {
                if (key.getValue().equals(ddId)) return key.getKey();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    String getPlayerDDId(String player) {
        return this.sync.sync_database.sync_players.get(player);
    }

    private List<String> getDDRoleByInGameGroup(String group) {
        return this.sync.group_ids.get(group);
    }

    private List<String> getConfigRoles() {
        List<String> roles = new ArrayList<>();
        this.sync.group_ids.forEach((key, value) -> roles.addAll(value));
        return roles;
    }

    void unlink(String player){
        this.sync.sync_database.sync_players.remove(player);
        saveConfig();
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    private void saveConfig() {
        try {
            configRoot.setValue(TypeToken.of(DiscordSync.class), sync);
            cfgLoader.save(configRoot);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }
}

@ConfigSerializable
class DiscordSync {

    @Setting(value = "enable_sync", comment = "Enable Discord Sync?\n" +
            "You need to setup the BOT TOKEN on config.yml and enable Discord first")
    public boolean enable_sync = false;

    @Setting(value = "discord-cmds")
    public discordCmdsCat discord_cmds = new discordCmdsCat();
    @ConfigSerializable
    public static class discordCmdsCat{
        @Setting(value = "admin-roles", comment = "Roles allowed to use the admin commands")
        public List<String> admin_roles = new ArrayList<>();
        @Setting(comment = "Private BOT command to manage user connections")
        public String admin = ";;admin";
        @Setting(comment = "Private BOT command to admin commands")
        public String help = ";;help";
        @Setting(comment = "Private BOT command to allow users to use the code for Discord in-game connection")
        public String connect = ";;connect";
    }

    @Setting(comment = "The discord guild/channel ID")
    public String guild_id = "";
    @Setting(value = "require-perms", comment = "Use permissions or just set the rank if the player is on group set on config?")
    public boolean require_perms = false;

    @Setting(value = "group-ids",comment = "To get a role ID, mention the role with a \\ before it in a Discord channel (e.g. \\@rolename)\n" +
            "The role need to be MENTIONABLE to allow you to get the id")
    public Map<String, List<String>> group_ids = new HashMap<>();

    @Setting(value = "sync-database", comment = "All stored players and pendent codes! Try to do not edit this manually!")
    public syncDBCat sync_database = new syncDBCat();
    @ConfigSerializable
    public static class syncDBCat {

        @Setting(comment = "Pending connection codes will be here!")
        public Map<String, String> pending_codes = new HashMap<>();
        @Setting(comment = "Connected players will be here!")
        public Map<String, String> sync_players = new HashMap<>();
    }

    @Setting(value = "update-interval", comment = "Interval in minutes to send role updates to Discord")
    public int update_interval = 5;

    @Setting
    public nameCat name = new nameCat();
    @ConfigSerializable
    public static class nameCat{

        @Setting(value = "to-discord", comment = "Change the discord nickname to IN-GAME name?")
        public boolean to_discord = false;
        @Setting(value = "use-display-name", comment = "Should send the nickname instead real player name?")
        public boolean use_display_name = true;
    }
}

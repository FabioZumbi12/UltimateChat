package br.net.fabiozumbi12.UltimateChat.Bukkit.discord;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UChat;
import br.net.fabiozumbi12.UltimateFancy.UltimateFancy;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import jdalib.jda.api.Permission;
import jdalib.jda.api.entities.*;
import jdalib.jda.api.exceptions.RateLimitedException;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.SimplePluginManager;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import static org.bukkit.Bukkit.getServer;

public class UCDiscordSync implements CommandExecutor, Listener, TabCompleter {
    private final HashMap<String, String> comments;
    private final YamlConfiguration sync;
    private int taskId = 0;
    public DDSimpleClansChat ddSimpleClansChat;

    public UCDiscordSync() {
        this.comments = new HashMap<>();
        this.sync = new YamlConfiguration();
        this.ddSimpleClansChat = new DDSimpleClansChat();

        File config = new File(UChat.get().getDataFolder(), "discord-sync.yml");
        if (config.exists()) {
            try {
                this.sync.load(config);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        setDefault("enable-sync", false, "Enable Discord Sync?\n" +
                "You need to setup the BOT TOKEN on config.yml and enable Discord first");

        setDefault("discord-cmds.admin-roles", new ArrayList<>(), "Roles allowed to use the admin commands");
        setDefault("discord-cmds.admin", ";;admin", "Private BOT command to manage user connections");
        setDefault("discord-cmds.help", ";;help", "Private BOT command to admin commands");
        setDefault("discord-cmds.connect", ";;connect", "Private BOT command to allow users to use the code for Discord in-game connection");

        setDefault("guild-id", "", "The guild ID");
        setDefault("require-perms", false, "Use permissions or just set the rank if the player is on group set on config?");

        setDefault("sync-database.pending-codes", null, "Pending connection codes will be here!");
        setDefault("sync-database.sync-players", null, "Connected players will be here!");
        setDefault("sync-database", null, "All stored players and pendent codes! Try to do not edit this manually!");
        setDefault("update-interval", 5, "Interval in minutes to send role updates to Discord");
        setDefault("name.to-discord", false, "Change the discord nickname to IN-GAME name?");
        setDefault("name.use-display-name", true, "Should send the nickname instead real player name?");

        setDefault("group-ids", null,
                "To get a role ID, mention the role with a \\ before it in a Discord channel (e.g. \\@rolename)\n" +
                        "The role need to be MENTIONABLE to allow you to get the id");
        setDefault("group-ids.group-example", Collections.singletonList("1234567890123"), null);

        // Sync simple clans
        setDefault("simple-clans-sync", null, "Enable SimpleClans role synchronization with sync players?\n" +
                "BY ENABLING THIS, ALL CLAN CHANNELS AND CATEGORIES WILL BE CREATED AT ONCE!! Test in a test guild if possible!!\n" +
                "Before enable this, check if you bot have the ADMINISTRATION permission, needed to handle role permissions");
        setDefault("simple-clans-sync.enable", false, null);
        setDefault("simple-clans-sync.whitelist", Collections.singletonList("DEFAULT"), "Only this clans tags will be sync with server.\n" +
                "Add \"ALL\" to list to allow all clans.\n" +
                "Remove a clan from here to delete the role and clan channels from discord.");

        setDefault("simple-clans-sync.templates", null, "Templates for Discord category and channels.\nPlaceholders: {tag}, {name} and {clan-members}");
        setDefault("simple-clans-sync.templates.role", "\uD83D\uDEA9 Clan - {tag}", null);
        setDefault("simple-clans-sync.templates.role-color", "0fc5f0", null);
        setDefault("simple-clans-sync.templates.category", "\uD83D\uDEA9 {name} ({clan-members})", null);
        setDefault("simple-clans-sync.templates.text-channel", "chat", null);
        setDefault("simple-clans-sync.templates.voice-channel", "audio", null);
        setDefault("simple-clans-sync.templates.topic", "{name}({tag}) - {clan-members} members", null);
        setDefault("simple-clans-sync.templates.chat-to-discord-member", ":shield: **{player}**{rank}: {message}", null);
        setDefault("simple-clans-sync.templates.chat-to-discord-leader", ":crown: **{player}**{rank}: {message}", null);

        setDefault("simple-clans-sync.clans.DEFAULT", null, "Don't change this, this is an example of a synchronized clan\nDon't remove clan channels manually from discord!!");
        setDefault("simple-clans-sync.clans.DEFAULT.role", "1234567890", null);
        setDefault("simple-clans-sync.clans.DEFAULT.category", "1234567890", null);
        setDefault("simple-clans-sync.clans.DEFAULT.text-channel", "1234567890", null);
        setDefault("simple-clans-sync.clans.DEFAULT.voice-channel", "1234567890", null);

        // Register command
        if (getServer().getPluginCommand("discord-sync").isRegistered()) {
            try {
                Field field = SimplePluginManager.class.getDeclaredField("commandMap");
                field.setAccessible(true);
                getServer().getPluginCommand("discord-sync").unregister((CommandMap) (field.get(getServer().getPluginManager())));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Enable sync
        if (this.sync.getBoolean("enable-sync")) {
            UChat.get().registerAliases("discord-sync", Arrays.asList("discord-sync","dd-sync"), true, "uchat.discord-sync.cmd.base", this);

            final int interval = this.sync.getInt("update-interval");
            taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(UChat.get(), () -> {

                // Init SimpleClans integration
                if (this.sync.getBoolean("simple-clans-sync.enable") && UChat.get().getHooks().getSc() != null) {
                    // Update clans
                    updateClans();
                }

                final int[] delay = {0};
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (getPlayerDDId(p.getName()) != null) {
                        String playerDDId = getPlayerDDId(p.getName());
                        Bukkit.getScheduler().runTaskLaterAsynchronously(UChat.get(), () -> {

                            // Set role by Clan tag
                            if (UChat.get().getHooks().getSc() != null && this.sync.getBoolean("simple-clans-sync.enable")) {
                                List<String> whitelistClans = this.sync.getStringList("simple-clans-sync.whitelist");
                                ClanPlayer playerClan = UChat.get().getHooks().getSc().getClanManager().getClanPlayer(p);
                                Guild guild = UChat.get().getUCJDA().getJda().getGuildById(this.sync.getString("guild-id"));

                                if (playerClan != null) {
                                    try {
                                        Member member = guild.retrieveMemberById(playerDDId).complete(true);
                                        List<Role> memberRoles = new ArrayList<>(member.getRoles());

                                        if (playerClan.getClan().isVerified() &&
                                                (whitelistClans.contains("ALL") || whitelistClans.contains(playerClan.getClan().getTag().toUpperCase()))) {
                                            Clan clan = playerClan.getClan();
                                            String roleId = this.sync.getString("simple-clans-sync.clans." + clan.getTag().toUpperCase() + ".role");
                                            Role role = UChat.get().getUCJDA().getJda().getRoleById(roleId);

                                            if (role != null && !memberRoles.contains(role)) {
                                                // Remove other clan roles
                                                memberRoles.removeIf(r -> this.sync.getConfigurationSection("simple-clans-sync.clans").getValues(true).entrySet().stream().anyMatch(map -> map.getValue().equals(r.getId())));
                                                memberRoles.add(role);
                                                guild.modifyMemberRoles(member, memberRoles).complete(true);
                                            }
                                        } else {
                                            // Remove clan roles if clan is not verified or is not whitelisted
                                            if (memberRoles.removeIf(r -> this.sync.getConfigurationSection("simple-clans-sync.clans").getValues(true).entrySet().stream().anyMatch(map -> map.getValue().equals(r.getId())))) {
                                                guild.modifyMemberRoles(member, memberRoles).complete(true);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        Member member = guild.retrieveMemberById(playerDDId).complete(true);
                                        if (member != null) {
                                            List<Role> memberRoles = new ArrayList<>(member.getRoles());
                                            // Remove clan roles if found
                                            if (memberRoles.removeIf(r -> this.sync.getConfigurationSection("simple-clans-sync.clans").getValues(true).entrySet().stream().anyMatch(map -> map.getValue().equals(r.getId())))) {
                                                guild.modifyMemberRoles(member, memberRoles).complete(true);
                                            }
                                        } else {
                                            this.sync.set("sync-database.sync-players." + p.getName(), null);
                                            saveConfig();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // Set nick
                            String nick = "";
                            if (this.sync.getBoolean("name.to-discord")) {
                                if (this.sync.getBoolean("name.display-name"))
                                    nick = p.getDisplayName();
                                else
                                    nick = p.getName();
                            }

                            // Set role by in-game group
                            String group = UChat.get().getHooks().getVaultPerms().getPrimaryGroup(p);
                            List<String> roles = getDDRoleByInGameGroup(group);
                            if (!roles.isEmpty()) {
                                if (this.sync.getBoolean("require-perms"))
                                    roles.removeIf(r -> !p.hasPermission("uchat.discord-sync.role." + r));

                                UChat.get().getUCJDA().setPlayerRole(playerDDId, roles, nick, getConfigRoles());
                            }

                            delay[0] += 10;
                        }, delay[0]);
                    }
                });
            }, 20, 20 * (30 * (Math.max(interval, 1)))/*secs*/).getTaskId();

            UChat.get().getUCLogger().info("- Discord Sync in use!");
        }
        saveConfig();
    }

    private void updateClans(){

        // Clear deleted clans
        Set<String> delClans = this.sync.getConfigurationSection("simple-clans-sync.clans").getKeys(false);
        List<String> whitelistClans = this.sync.getStringList("simple-clans-sync.whitelist");
        delClans.forEach(dc -> {
            if (dc.equals("DEFAULT")) return;

            if (UChat.get().getHooks().getSc().getClanManager().getClan(dc) == null || (!whitelistClans.contains("ALL") && !whitelistClans.contains(dc))) {
                String roleId = this.sync.getString("simple-clans-sync.clans." + dc + ".role", "0");
                String categoryId = this.sync.getString("simple-clans-sync.clans." + dc + ".category", "0");
                String chatId = this.sync.getString("simple-clans-sync.clans." + dc + ".text-channel", "0");
                String audioId = this.sync.getString("simple-clans-sync.clans." + dc + ".voice-channel", "0");
                try {
                    // Remove before queue delete on dd
                    this.sync.set("simple-clans-sync.clans." + dc, null);
                    saveConfig();

                    UChat.get().getUCJDA().getJda().getRoleById(roleId).delete().complete(true);
                    UChat.get().getUCJDA().getJda().getCategoryById(categoryId).delete().complete(true);
                    UChat.get().getUCJDA().getJda().getTextChannelById(chatId).delete().complete(true);
                    UChat.get().getUCJDA().getJda().getVoiceChannelById(audioId).delete().complete(true);
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create new clans
        UChat.get().getHooks().getSc().getClanManager().getClans().stream().filter(Clan::isVerified).forEach(clan -> {
            if (!clan.isVerified()) return;

            String tag = clan.getTag().toUpperCase();
            String name = clan.getName();
            String members = String.valueOf(clan.getMembers().size());
            Guild guild = UChat.get().getUCJDA().getJda().getGuildById(this.sync.getString("guild-id"));

            if (!this.sync.contains("simple-clans-sync.clans." + tag)) {
                if (!whitelistClans.contains("ALL") && !whitelistClans.contains(tag)) return;

                try {
                    // Role creation
                    Role role = guild.createRole().complete(true);
                    role.getManager().setName(this.sync.getString("simple-clans-sync.templates.role","Clan " + tag)
                            .replace("{clan-members}", members)
                            .replace("{tag}", tag)
                            .replace("{name}", name)
                    )
                            .setColor(Color.decode("#"+this.sync.getString("simple-clans-sync.templates.role-color")))
                            .setMentionable(false)
                            .complete(true);

                    // Category and channels creation
                    Category category = guild.createCategory(this.sync.getString("simple-clans-sync.templates.category")
                            .replace("{clan-members}", members)
                            .replace("{tag}", tag)
                            .replace("{name}", name)).complete(true);
                    TextChannel text = category.createTextChannel(this.sync.getString("simple-clans-sync.templates.text-channel")
                            .replace("{clan-members}", members)
                            .replace("{name}", name)).complete(true);
                    VoiceChannel voice = category.createVoiceChannel(this.sync.getString("simple-clans-sync.templates.voice-channel")
                            .replace("{clan-members}", members)
                            .replace("{tag}", tag)
                            .replace("{name}", name)).complete(true);

                    Role publicRole = guild.getPublicRole();

                    // Need to assign role to category
                    category.putPermissionOverride(publicRole).complete(true);
                    category.putPermissionOverride(role).complete(true);

                    // Assign role to text channel
                    text.putPermissionOverride(publicRole).complete(true);
                    text.putPermissionOverride(role).complete(true);
                    text.getRolePermissionOverrides().forEach(roleOver -> {
                        try {
                            if (roleOver.getRole().equals(role)) {
                                roleOver.getManager().grant(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE).complete(true);
                            }
                            if (roleOver.getRole().equals(publicRole)) {
                                roleOver.getManager().deny(Permission.MESSAGE_READ, Permission.MESSAGE_WRITE).complete(true);
                            }
                        } catch (RateLimitedException e) {
                            e.printStackTrace();
                        }
                    });

                    // Assign role to voice channel
                    voice.putPermissionOverride(publicRole).complete(true);
                    voice.putPermissionOverride(role).complete(true);
                    voice.getRolePermissionOverrides().forEach(roleOver -> {
                        try {
                            if (roleOver.getRole().equals(role)) {
                                roleOver.getManager().grant(Permission.VOICE_CONNECT).complete(true);
                            }
                            if (roleOver.getRole().equals(publicRole)) {
                                roleOver.getManager().deny(Permission.VOICE_CONNECT).complete(true);
                            }
                        } catch (RateLimitedException e) {
                            e.printStackTrace();
                        }
                    });

                    // Save all
                    this.sync.set("simple-clans-sync.clans." + tag + ".role", role.getId());
                    this.sync.set("simple-clans-sync.clans." + tag + ".category", category.getId());
                    this.sync.set("simple-clans-sync.clans." + tag + ".text-channel", text.getId());
                    this.sync.set("simple-clans-sync.clans." + tag + ".voice-channel", voice.getId());
                    saveConfig();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Update text channel description
                String chatId = this.sync.getString("simple-clans-sync.clans." + tag + ".text-channel", "0");
                String categoryId = this.sync.getString("simple-clans-sync.clans." + tag + ".category", "0");
                try {
                    TextChannel textChanel = guild.getTextChannelById(chatId);
                    Category category = guild.getCategoryById(categoryId);
                    if (textChanel != null) {
                        textChanel.getManager()
                                .setTopic(this.sync.getString("simple-clans-sync.templates.topic")
                                        .replace("{clan-members}", members)
                                        .replace("{tag}", tag)
                                        .replace("{name}", name))
                                .setName(this.sync.getString("simple-clans-sync.templates.text-channel")
                                        .replace("{clan-members}", members)
                                        .replace("{tag}", tag)
                                        .replace("{name}", name)).complete(true);
                    }
                    if (category != null) {
                        category.getManager().setName(this.sync.getString("simple-clans-sync.templates.category")
                                .replace("{clan-members}", members)
                                .replace("{tag}", tag)
                                .replace("{name}", name)).complete(true);
                    }
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public YamlConfiguration getConfig() {
        return this.sync;
    }
    
    public void unload() {
        Bukkit.getScheduler().cancelTask(this.taskId);
    }

    String getConnectedPlayers(){
        StringBuilder list = new StringBuilder();
        try {
            for (String key : this.sync.getConfigurationSection("sync-database.sync-players").getKeys(false)) {
                list.append(key).append(": `").append(this.sync.getString("sync-database.sync-players." + key)).append("`\n");
            }
        } catch (Exception ignored){}
        return list.toString();
    }

    String getGuidId(){
        return this.sync.getString("guild-id");
    }

    boolean isDDAdminRole(String role) {
        return this.sync.getStringList("discord-cmds.admin-roles").contains(role);
    }

    String getDDAdminCommand() {
        return this.sync.getString("discord-cmds.admin");
    }

    String getDDCommand() {
        return this.sync.getString("discord-cmds.connect");
    }

    String getDDHelpCommand() {
        return this.sync.getString("discord-cmds.help");
    }

    private boolean addPendingCode(String player, String code) {
        if (getPlayerDDId(player) != null) {
            return false;
        }
        this.sync.set("sync-database.pending-codes." + code, player);
        saveConfig();
        return true;
    }

    private String getPlayerPending(String player) {
        try {
            for (String key : this.sync.getConfigurationSection("sync-database.pending-codes").getKeys(false)) {
                if (this.sync.getString("sync-database.pending-codes." + key).equals(player)) return key;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    String getPendentCode(String code) {
        return this.sync.getString("sync-database.pending-codes." + code);
    }

    void setPlayerDDId(String ddId, String nickName,String code) {
        if (code != null) this.sync.set("sync-database.pending-codes." + code, null);
        this.sync.set("sync-database.sync-players." + nickName, ddId);
        saveConfig();
    }

    public String getSyncNickName(String ddId) {
        try {
            for (String key : this.sync.getConfigurationSection("sync-database.sync-players").getKeys(false)) {
                if (this.sync.getString("sync-database.sync-players." + key).equals(ddId)) return key;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    String getPlayerDDId(String player) {
        return this.sync.getString("sync-database.sync-players." + player);
    }

    private List<String> getDDRoleByInGameGroup(String group) {
        return this.sync.getStringList("group-ids." + group);
    }

    private List<String> getConfigRoles() {
        List<String> roles = new ArrayList<>();
        this.sync.getConfigurationSection("group-ids").getKeys(false).forEach(k -> roles.addAll(this.sync.getStringList("group-ids." + k)));
        return roles;
    }

    void unlink(String player){
        String playerId = this.sync.getString("sync-database.sync-players." + player);
        if (playerId != null) {
            Guild guild = UChat.get().getUCJDA().getJda().getGuildById(this.sync.getString("guild-id"));
            Member member = guild.getMemberById(playerId);
            if (member != null) {
                List<Role> memberRoles = new ArrayList<>(member.getRoles());

                // Remove clan roles
                memberRoles.removeIf(r -> this.sync.getConfigurationSection("simple-clans-sync.clans").getValues(true).entrySet().stream().anyMatch(map -> map.getValue().equals(r.getId())));

                // Remove in-game groups roles
                memberRoles.removeIf(r -> this.sync.getConfigurationSection("group-ids").getValues(true).entrySet().stream().anyMatch(map -> map.getValue().equals(r.getId())));

                try {
                    guild.modifyMemberRoles(member, memberRoles).complete(true);
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.sync.set("sync-database.sync-players." + player, null);
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

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.AQUA + "---------------- " + UChat.get().getPDF().getFullName() + " ----------------");
            commandSender.sendMessage(ChatColor.AQUA + "Developed by " + ChatColor.GOLD + UChat.get().getPDF().getAuthors() + ".");
            commandSender.sendMessage(ChatColor.AQUA + "Discord Sync Commands [" + ChatColor.GOLD + "/" + s + " gen|unlink" + ChatColor.AQUA + "].");
            commandSender.sendMessage(ChatColor.AQUA + "-----------------------------------------------------");
            return true;
        }

        //dd-sync list
        if (args.length == 1 && args[0].equalsIgnoreCase("list") && commandSender.hasPermission("uchat.discord-sync.cmd.addgroup")) {
            StringBuilder list = new StringBuilder();
            for (String key : this.sync.getConfigurationSection("group-ids").getKeys(false)) {
                list.append("&3- ").append(key).append(": &b").append(this.sync.getString("group-ids." + key)).append("\n");
            }
            UChat.get().getLang().sendMessage(commandSender,  UChat.get().getLang().get("discord.sync-group-list") + "\n" + list.toString());
            return true;
        }

        //dd-sync addgroup group id
        if (args.length == 3 && args[0].equalsIgnoreCase("addgroup") && commandSender.hasPermission("uchat.discord-sync.cmd.addgroup")) {
            List<String> groups = this.sync.getStringList("group-ids." + args[1]);
            if (!groups.contains(args[2])) groups.add(args[2]);
            this.sync.set("group-ids." + args[1], groups);
            saveConfig();
            UChat.get().getLang().sendMessage(commandSender,  UChat.get().getLang().get("discord.sync-groupadded")
                    .replace("{group}", args[1])
                    .replace("{id}", args[2]));
            return true;
        }

        //dd-sync removegroup group
        if (args.length == 2 && args[0].equalsIgnoreCase("removegroup") && commandSender.hasPermission("uchat.discord-sync.cmd.addgroup")) {
            this.sync.set("group-ids." + args[1], null);
            saveConfig();
            UChat.get().getLang().sendMessage(commandSender,  UChat.get().getLang().get("discord.sync-groupremoved")
                    .replace("{group}", args[1]));
            return true;
        }

        //dd-sync generate
        if (args[0].equalsIgnoreCase("gen") && commandSender.hasPermission("uchat.discord-sync.cmd.generate")) {
            if (args.length == 1 && commandSender instanceof Player) {
                Player p = (Player) commandSender;
                String code = getSaltString();
                if (getPlayerPending(p.getName()) != null) {
                    code = getPlayerPending(p.getName());
                }
                if (addPendingCode(p.getName(), code)) {
                    UltimateFancy fancy = new UltimateFancy(UChat.get() ,UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-done").replace("{code}", code));
                    fancy.hoverShowText(UChat.get().getLang().get("discord.sync-click"));
                    fancy.clickSuggestCmd(this.sync.getString("discord-cmds.connect") + " " + code);
                    fancy.send(p);
                } else {
                    UltimateFancy fancy = new UltimateFancy(UChat.get(), UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-fail"));
                    fancy.send(p);
                }
            }
            if (args.length == 2 && commandSender.hasPermission("uchat.discord-sync.cmd.generate.others")) {
                String p = args[1];
                String code = getSaltString();
                if (getPlayerPending(p) != null) {
                    code = getPlayerPending(p);
                }
                if (addPendingCode(p, code)) {
                    UltimateFancy fancy = new UltimateFancy(UChat.get(), UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-done").replace("{code}", code));
                    fancy.hoverShowText(UChat.get().getLang().get("discord.sync-click"));
                    fancy.clickSuggestCmd(code);
                    fancy.send(commandSender);
                } else {
                    UltimateFancy fancy = new UltimateFancy(UChat.get(), UChat.get().getLang().get("_UChat.prefix") + " " + UChat.get().getLang().get("discord.sync-fail"));
                    fancy.send(commandSender);
                }
            }
            return true;
        }

        //dd-sync unlink
        if (args[0].equalsIgnoreCase("unlink") && commandSender.hasPermission("uchat.discord-sync.cmd.unlink")) {
            if (args.length == 1 && commandSender instanceof Player) {
                Player p = (Player) commandSender;
                if (getPlayerDDId(p.getName()) != null) {
                    unlink(p.getName());
                    UChat.get().getLang().sendMessage(p, "discord.sync-unlink");
                } else {
                    UChat.get().getLang().sendMessage(p, "discord.sync-notlink");
                }
            }
            if (args.length == 2 && commandSender.hasPermission("uchat.discord-sync.cmd.unlink.others")) {
                String p = args[1];
                if (getPlayerDDId(p) != null) {
                    unlink(p);
                    UChat.get().getLang().sendMessage(commandSender, "discord.sync-unlink");
                } else {
                    UChat.get().getLang().sendMessage(commandSender, "discord.sync-notlink");
                }
            }
            return true;
        }

        // Simple Clans sync
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("addclan") && commandSender.hasPermission("uchat.discord-sync.cmd.addclan")) {
                List<String> clans = this.sync.getStringList("simple-clans-sync.whitelist");
                clans.add(args[1].toUpperCase());
                this.sync.set("simple-clans-sync.whitelist", clans);
                saveConfig();
                UChat.get().getLang().sendMessage(commandSender,  UChat.get().getLang().get("discord.sync-add-clan").replace("{clan}", args[1].toUpperCase()));
                return true;
            }
            if (args[0].equalsIgnoreCase("delclan") && commandSender.hasPermission("uchat.discord-sync.cmd.delclan")) {
                List<String> clans = this.sync.getStringList("simple-clans-sync.whitelist");
                if (clans.remove(args[1].toUpperCase())) {
                    this.sync.set("simple-clans-sync.whitelist", clans);
                    UChat.get().getLang().sendMessage(commandSender,  UChat.get().getLang().get("discord.sync-removed-clan").replace("{clan}", args[1].toUpperCase()));
                } else {
                    UChat.get().getLang().sendMessage(commandSender,  UChat.get().getLang().get("discord.sync-no-clan").replace("{clan}", args[1].toUpperCase()));
                }
                saveConfig();
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            if (commandSender.hasPermission("uchat.discord-sync.cmd.addclan"))
                tab.add("addclan");
            if (commandSender.hasPermission("uchat.discord-sync.cmd.delclan"))
                tab.add("delclan");
            if (commandSender.hasPermission("uchat.discord-sync.cmd.generate"))
                tab.add("gen");
            if (commandSender.hasPermission("uchat.discord-sync.cmd.unlink"))
                tab.add("unlink");
            if (commandSender.hasPermission("uchat.discord-sync.cmd.addgroup")){
                tab.add("addgroup");
                tab.add("removegroup");
                tab.add("list");
            }

        }
        return tab;
    }


    //config manager
    private void setDefault(String key, Object def, String comment) {
        if (def != null) {
            this.sync.set(key, this.sync.get(key, def));
        }
        if (comment != null) {
            setComment(key, comment);
        }
    }

    private void setComment(String key, String comment) {
        comments.put(key, comment);
    }

    public void saveConfig() {
        StringBuilder b = new StringBuilder();
        this.sync.options().header(null);

        b.append(""
                + "# Uchat Discord Syncronization file\n"
                + "# Author: FabioZumbi12\n"
                + "# We recommend you to use NotePad++ to edit this file and avoid TAB errors!\n"
                + "# ------------------------------------------------------------------------\n"
                + "#     In this file you can configure the discord roles synchronization    \n"
                + "#              between online players and your Discord groups             \n"
                + "# ------------------------------------------------------------------------\n"
                + "\n"
                + "# IMPORTANT NOTE: The BOT need to have a role assigned with MANAGE ROLES and MANAGE NICKNAMES\n"
                + "# granted, and this role need to be UNDER other roles you want to give to your Discord members!\n"
                + "# Check our WIKI to see how to setup Synchronization: https://bit.ly/2F3UoRf"
                + "\n");

        for (String line : this.sync.getKeys(true)) {
            String[] key = line.split("\\" + this.sync.options().pathSeparator());
            StringBuilder spaces = new StringBuilder();
            for (int i = 0; i < key.length; i++) {
                if (i == 0) continue;
                spaces.append("  ");
            }
            if (comments.containsKey(line)) {
                if (spaces.length() == 0) {
                    b.append("\n# ").append(comments.get(line).replace("\n", "\n# ")).append('\n');
                } else {
                    b.append(spaces).append("# ").append(comments.get(line).replace("\n", "\n" + spaces + "# ")).append('\n');
                }
            }
            Object value = this.sync.get(line);
            if (!this.sync.isConfigurationSection(line)) {
                if (value instanceof String) {
                    b.append(spaces).append(key[key.length - 1]).append(": '").append(value).append("'\n");
                } else if (value instanceof List<?>) {
                    if (((List<?>) value).isEmpty()) {
                        b.append(spaces).append(key[key.length - 1]).append(": []\n");
                    } else {
                        b.append(spaces).append(key[key.length - 1]).append(":\n");
                        for (Object lineCfg : (List<?>) value) {
                            if (lineCfg instanceof String) {
                                b.append(spaces).append("- '").append(lineCfg).append("'\n");
                            } else {
                                b.append(spaces).append("- ").append(lineCfg).append("\n");
                            }
                        }
                    }
                } else {
                    b.append(spaces).append(key[key.length - 1]).append(": ").append(value).append("\n");
                }
            } else {
                b.append(spaces).append(key[key.length - 1]).append(":\n");
            }
        }

        try {
            Files.write(b, new File(UChat.get().getDataFolder(), "discord-sync.yml"), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

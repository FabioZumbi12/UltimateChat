/*
 * Copyright (c) 2012-2025 - @FabioZumbi12
 * Last Modified: 02/12/2025 15:59
 *
 * This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
 *  damages arising from the use of this class.
 *
 * Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 * redistribute it freely, subject to the following restrictions:
 * 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 * use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 * 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 * 3 - This notice may not be removed or altered from any source distribution.
 *
 * Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 * responsabilizados por quaisquer danos decorrentes do uso desta classe.
 *
 * É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 * alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 * 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
 *  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 * 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 * classe original.
 * 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.UltimateFancy;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UltimateFancy
 *
 * - Internals use Component only (no unsafe casting)
 * - Hover/click built with Adventure events
 * - SNBT for hover items via reflection (keeps compatibility)
 *
 * NOTE:
 *   - Before using UltimateFancy you MUST call UltimateFancy.init(plugin)
 *   - At plugin disable call UltimateFancy.shutdown()
 *
 * Author: FabioZumbi12 (refactor)
 */

public class UltimateFancy implements Listener {

    private BukkitAudiences audiences;

    private final List<Component> constructor;
    private HashMap<String, Boolean> lastFormats;
    private List<Component> workingGroup;
    private List<ExtraElement> pendentElements;
    private final JavaPlugin plugin;

    public UltimateFancy(JavaPlugin plugin) {
        this.plugin = plugin;
        this.constructor = new ArrayList<>();
        this.workingGroup = new ArrayList<>();
        this.lastFormats = new HashMap<>();
        this.pendentElements = new ArrayList<>();

        this.audiences = BukkitAudiences.create(plugin);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent e) {
        if (e.getPlugin() == plugin) {
            if (audiences != null) {
                audiences.close();
                audiences = null;
            }
        }
    }

    public UltimateFancy(JavaPlugin plugin, String text) {
        this(plugin);
        text(text);
    }

    public UltimateFancy coloredTextAndNext(String text) {
        text = UChatColor.translateAlternateColorCodes(text);
        return textAndNext(text);
    }

    public UltimateFancy textAndNext(String text) {
        this.text(text);
        return next();
    }

    public UltimateFancy coloredText(String text) {
        text = UChatColor.translateAlternateColorCodes(text);
        return this.text(text);
    }

    public UltimateFancy text(String text) {
        List<Component> parts = parseColorsToComponents(text);
        workingGroup.addAll(parts);
        return this;
    }

    public UltimateFancy textAtStart(String text) {
        List<Component> parts = parseColorsToComponents(text);
        List<Component> newList = new ArrayList<>(parts);
        newList.addAll(this.constructor);
        this.constructor.clear();
        this.constructor.addAll(newList);
        return this;
    }

    public UltimateFancy appendObject(Component comp) {
        workingGroup.add(comp);
        return this;
    }

    public UltimateFancy appendString(String json) {
        try {
            Component c = GsonComponentSerializer.gson().deserialize(json);
            workingGroup.add(c);
        } catch (Throwable ignored) {}
        return this;
    }

    public List<Component> getWorkingElements() {
        return this.workingGroup;
    }

    public List<Component> getStoredElements() {
        return new ArrayList<>(this.constructor);
    }

    public UltimateFancy removeObject(Component comp) {
        this.workingGroup.remove(comp);
        this.constructor.remove(comp);
        return this;
    }

    public UltimateFancy appendAtFirst(Component comp) {
        List<Component> newList = new ArrayList<>();
        newList.add(comp);
        newList.addAll(getStoredElements());
        constructor.clear();
        constructor.addAll(newList);
        return this;
    }

    public UltimateFancy appendAtEnd(Component comp) {
        List<Component> newList = new ArrayList<>(getWorkingElements());
        newList.add(comp);
        this.workingGroup = newList;
        return this;
    }

    public List<UltimateFancy> getFancyElements() {
        next();
        List<UltimateFancy> list = new ArrayList<>();
        for (Component c : this.constructor) {
            UltimateFancy f = new UltimateFancy(plugin);
            f.appendAtEnd(c);
            list.add(f);
        }
        return list;
    }

    public UltimateFancy appendFancy(UltimateFancy fancy) {
        this.appendAtEnd(fancy.toComponent());
        return this;
    }

    private List<Component> parseColorsToComponents(String text) {
        List<Component> out = new ArrayList<>();
        List<String> parts = splitVanillaColors(text);
        for (String part : parts) {
            Component c = buildComponentFromColoredPart(part);
            if (c != null) out.add(c);
        }
        return out;
    }

    private List<String> splitVanillaColors(String text) {
        List<String> vanillaColors = new ArrayList<>();
        int hexCount = 0;
        StringBuilder hexValue = new StringBuilder();
        for (String vColor : text.split("(?=§)")) {
            if (hexCount > 0) {
                if (hexCount >= 7) {
                    String lastValue = vanillaColors.get(vanillaColors.size() - 1);
                    vanillaColors.set(vanillaColors.size() - 1, lastValue.replace("%uchathex-color%", hexValue.toString()));
                    hexValue = new StringBuilder();
                    hexCount = 0;
                } else {
                    if (vColor.startsWith("§")) {
                        hexValue.append(vColor.replace("§", ""));
                        hexCount++;
                        continue;
                    } else {
                        String lastValue = vanillaColors.get(vanillaColors.size() - 1);
                        vanillaColors.set(vanillaColors.size() - 1, lastValue.replace("#%uchathex-color%", ""));
                        hexValue = new StringBuilder();
                        hexCount = 0;
                    }
                }
            }
            if (vColor.startsWith("§x")) {
                vanillaColors.add("#%uchathex-color%" + vColor.replaceAll("§x", ""));
                hexCount = 1;
                continue;
            }
            if (vColor.startsWith("§#")) {
                vColor = vColor.substring(1);
            }
            vanillaColors.add(vColor);
        }
        return vanillaColors;
    }

    private Component buildComponentFromColoredPart(String part) {
        try {
            Matcher m = Pattern.compile("#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{3}").matcher(part);
            String rawText;
            TextColor color = null;
            Set<TextDecoration> decorations = new HashSet<>();

            if (m.find()) {
                String hex = m.group();
                // normalize 3-digit hex
                if (hex.length() == 4) {
                    String shortHex = hex.substring(1);
                    String full = "" + shortHex.charAt(0) + shortHex.charAt(0)
                            + shortHex.charAt(1) + shortHex.charAt(1)
                            + shortHex.charAt(2) + shortHex.charAt(2);
                    hex = "#" + full;
                }
                color = TextColor.color(Integer.parseInt(hex.substring(1), 16));
                rawText = part.replace(m.group(), "");
            } else {
                rawText = part;
                Matcher matcher1 = Pattern.compile("^§([0-9a-fA-Fk-oK-ORr]).*$").matcher(part);
                if (matcher1.find()) {
                    char code = matcher1.group(1).charAt(0);
                    ChatColor cc = ChatColor.getByChar(code);
                    if (cc != null) {
                        if (cc.isColor()) {
                            NamedTextColor ntc = mapNamedColor(cc);
                            if (ntc != null) color = ntc;
                        } else {
                            TextDecoration dec = mapDecoration(cc);
                            if (dec != null) decorations.add(dec);
                        }
                    }
                    rawText = UChatColor.stripColor(part);
                } else {
                    rawText = UChatColor.stripColor(part);
                }
            }

            Component comp = Component.text(rawText);
            if (color != null) comp = comp.color(color);
            for (TextDecoration dec : decorations) comp = comp.decorate(dec);

            for (Entry<String, Boolean> fmt : lastFormats.entrySet()) {
                try {
                    TextDecoration d = TextDecoration.valueOf(fmt.getKey().toUpperCase());
                    comp = comp.decorate(d);
                } catch (Throwable ignored) {}
            }

            return comp;
        } catch (Throwable t) {
            return Component.text(UChatColor.stripColor(part));
        }
    }

    private NamedTextColor mapNamedColor(ChatColor cc) {
        if (cc == null) return null;
        try {
            switch (cc) {
                case BLACK: return NamedTextColor.BLACK;
                case DARK_BLUE: return NamedTextColor.DARK_BLUE;
                case DARK_GREEN: return NamedTextColor.DARK_GREEN;
                case DARK_AQUA: return NamedTextColor.DARK_AQUA;
                case DARK_RED: return NamedTextColor.DARK_RED;
                case DARK_PURPLE: return NamedTextColor.DARK_PURPLE;
                case GOLD: return NamedTextColor.GOLD;
                case GRAY: return NamedTextColor.GRAY;
                case DARK_GRAY: return NamedTextColor.DARK_GRAY;
                case BLUE: return NamedTextColor.BLUE;
                case GREEN: return NamedTextColor.GREEN;
                case AQUA: return NamedTextColor.AQUA;
                case RED: return NamedTextColor.RED;
                case LIGHT_PURPLE: return NamedTextColor.LIGHT_PURPLE;
                case YELLOW: return NamedTextColor.YELLOW;
                case WHITE: return NamedTextColor.WHITE;
                default: return null;
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    private TextDecoration mapDecoration(ChatColor cc) {
        if (cc == null) return null;
        switch (cc) {
            case BOLD: return TextDecoration.BOLD;
            case ITALIC: return TextDecoration.ITALIC;
            case STRIKETHROUGH: return TextDecoration.STRIKETHROUGH;
            case UNDERLINE: return TextDecoration.UNDERLINED;
            case MAGIC: return TextDecoration.OBFUSCATED;
            default: return null;
        }
    }

    public UltimateFancy next() {
        if (!workingGroup.isEmpty()) {
            for (Component base : workingGroup) {
                Component withExtras = base;
                for (ExtraElement ex : pendentElements) {
                    if (ex.type == ExtraElement.Type.CLICK) {
                        withExtras = applyClick(withExtras, ex.type, ex.clickPayload);
                    } else if (ex.type == ExtraElement.Type.HOVER_TEXT) {
                        withExtras = applyHoverText(withExtras, ex.hoverComponent);
                    } else if (ex.type == ExtraElement.Type.HOVER_ITEM) {
                        withExtras = applyHoverItem(withExtras, ex.hoverEvent);
                    }
                }
                constructor.add(withExtras);
            }
        }
        workingGroup = new ArrayList<>();
        pendentElements = new ArrayList<>();
        return this;
    }

    private Component applyClick(Component base, ExtraElement.Type kind, String payload) {
        try {
            return switch (kind) {
                case CLICK -> base.clickEvent(ClickEvent.runCommand(payload));
                case HOVER_TEXT -> base.clickEvent(ClickEvent.suggestCommand(payload));
                default -> base.clickEvent(ClickEvent.openUrl(URI.create(payload).toURL()));
            };
        } catch (Throwable ignored) {
        }
        return base;
    }

    private Component applyHoverItem(Component base, HoverEvent hover) {
        try {
            return base.hoverEvent(hover);
        } catch (Throwable ignored) {
            // fallback to text
            try {
                return base.hoverEvent(HoverEvent.showText(Component.text(hover.toString())));
            } catch (Throwable ex) {
                return base;
            }
        }
    }

    private Component applyHoverText(Component base, Component hover) {
        try {
            return base.hoverEvent(HoverEvent.showText(hover));
        } catch (Throwable ignored) {
            // fallback to text
            try {
                return base.hoverEvent(HoverEvent.showText(Component.text(hover.toString())));
            } catch (Throwable ex) {
                return base;
            }
        }
    }

    public UltimateFancy clickRunCmd(String cmd) {
        pendentElements.add(ExtraElement.click(cmd));
        return this;
    }

    public UltimateFancy clickSuggestCmd(String cmd) {
        pendentElements.add(ExtraElement.click(cmd));
        return this;
    }

    public UltimateFancy clickOpenURL(URL url) {
        pendentElements.add(ExtraElement.click(url.toString()));
        return this;
    }

    public UltimateFancy hoverShowText(String text) {
        String t = UChatColor.translateAlternateColorCodes(text);
        Component hover = mergeComponents(parseColorsToComponents(t));
        pendentElements.add(ExtraElement.hoverText(hover));
        return this;
    }

    public UltimateFancy hoverShowItem(ItemStack item) {
        String snbt = toManualSNBT(item);
        Key key = Key.key("minecraft:" + item.getType().name().toLowerCase());

        pendentElements.add(
                ExtraElement.hoverItem(
                        HoverEvent.showItem(
                                key,
                                item.getAmount(),
                                BinaryTagHolder.binaryTagHolder(snbt)
                        )
                )
        );
        return this;
    }

    private Component mergeComponents(List<Component> parts) {
        Component acc = Component.empty();
        for (Component c : parts) acc = acc.append(c);
        return acc;
    }

    public void send(CommandSender to) {
        next();
        Component comp = toComponent();
        if (to instanceof Player) performSend((Player) to, comp);
        else performSendConsole(comp);
    }

    public void send(CommandSender to, boolean json) {
        next();
        Component comp = toComponent();
        if (to instanceof Player) {
            if (json) performSend((Player) to, comp);
            else to.sendMessage(toOldFormat());
        } else {
            performSendConsole(comp);
        }
    }

    public Component toComponent() {
        next();
        return mergeComponents(constructor);
    }

    private void performSend(Player to, Component comp) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Audience aud = audiences.player(to);
            aud.sendMessage(comp);
        });
    }

    private void performSendConsole(Component comp) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Audience aud = audiences.console();
            aud.sendMessage(comp);
        });
    }

    public String toOldFormat() {
        try {
            Component merged = toComponent();
            return LegacyComponentSerializer.legacySection().serialize(merged);
        } catch (Throwable t) {
            StringBuilder sb = new StringBuilder();
            for (Component c : constructor) {
                try {
                    sb.append(GsonComponentSerializer.gson().serialize(c));
                } catch (Throwable ignored) {
                    sb.append(c.toString());
                }
            }
            return sb.toString();
        }
    }

    static class ExtraElement {
        enum Type { CLICK, HOVER_TEXT, HOVER_ITEM }

        final Type type;

        String clickPayload;
        Component hoverComponent;
        HoverEvent<?> hoverEvent;

        private ExtraElement(Type type) {
            this.type = type;
        }


        static ExtraElement click(String payload) {
            ExtraElement e = new ExtraElement(Type.CLICK);
            e.clickPayload = payload;
            return e;
        }

        static ExtraElement hoverText(Component c) {
            ExtraElement e = new ExtraElement(Type.HOVER_TEXT);
            e.hoverComponent = c;
            return e;
        }

        static ExtraElement hoverItem(HoverEvent<?> event) {
            ExtraElement e = new ExtraElement(Type.HOVER_ITEM);
            e.hoverEvent = event;
            return e;
        }
    }

    static class UChatColor {
        public static final String HEX_PATTERN = "&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})";

        public static String stripColor(String text) {
            text = ChatColor.stripColor(text);
            text = text.replaceAll(HEX_PATTERN, "");
            return text;
        }

        public static String translateAlternateColorCodes(String text) {
            Matcher matcher = Pattern.compile(HEX_PATTERN).matcher(text);
            StringBuilder buffer = new StringBuilder();
            while (matcher.find()) {
                String find = matcher.group(1);
                if (find.length() == 3) {
                    find = "" + find.charAt(0) + find.charAt(0)
                            + find.charAt(1) + find.charAt(1)
                            + find.charAt(2) + find.charAt(2);
                }
                matcher.appendReplacement(buffer, "§x§" + find.charAt(0) + "§" +
                        find.charAt(1) + "§" + find.charAt(2) + "§" +
                        find.charAt(3) + "§" + find.charAt(4) + "§" +
                        find.charAt(5));
            }
            matcher.appendTail(buffer);
            return ChatColor.translateAlternateColorCodes('&', buffer.toString());
        }
    }

    public static String toManualSNBT(ItemStack item) {
        StringBuilder sb = new StringBuilder();
        String id = "minecraft:" + item.getType().name().toLowerCase();
        sb.append("{");
        sb.append("id:\"").append(id).append("\",");
        sb.append("Count:").append(item.getAmount()).append("b");

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> tagParts = new ArrayList<>();

            try {
                Component nameComponent;
                if ((getBukkitVersion() >= 1112 || getBukkitVersion() == 0) && meta.hasLocalizedName()) {
                    nameComponent = LegacyComponentSerializer.legacySection().deserialize(meta.getLocalizedName());
                } else if (meta.hasDisplayName()) {
                    nameComponent = LegacyComponentSerializer.legacySection().deserialize(meta.getDisplayName());
                } else {
                    nameComponent = LegacyComponentSerializer.legacySection().deserialize(capitalize(item.getType().toString()));
                }

                List<String> loreJsonList = new ArrayList<>();

                if (meta.hasLore()) {
                    List<String> loreStrings = meta.getLore();
                    if (loreStrings != null) {
                        for (String s : loreStrings) {
                            Component c = LegacyComponentSerializer.legacySection().deserialize(s);
                            loreJsonList.add(escapeForSNBT(GsonComponentSerializer.gson().serialize(c)));
                        }
                    }
                }

                List<String> displayParts = new ArrayList<>();
                String nameJson = GsonComponentSerializer.gson().serialize(nameComponent);
                displayParts.add("Name:'" + escapeForSNBT(nameJson) + "'");
                if (!loreJsonList.isEmpty()) {
                    displayParts.add("Lore:[" + String.join(",", wrapWithQuotes(loreJsonList)) + "]");
                }
                tagParts.add("display:{" + String.join(",", displayParts) + "}");
            } catch (Throwable t) {
                t.printStackTrace();
            }

            try {
                if (meta.hasEnchants()) {
                    List<String> enchParts = new ArrayList<>();
                    for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
                        String enchId;
                        try {
                            enchId = e.getKey().getKey().toString();
                        } catch (Throwable ex) {
                            enchId = "minecraft:" + e.getKey().getName().toLowerCase();
                        }
                        enchParts.add("{id:\"" + enchId + "\",lvl:" + e.getValue() + "}");
                    }
                    tagParts.add("Enchantments:[" + String.join(",", enchParts) + "]");
                }
            } catch (Throwable ignored) {}

            try {
                if (meta instanceof Damageable) {
                    int damage = ((Damageable) meta).getDamage();
                    tagParts.add("Damage:" + damage);
                }
            } catch (Throwable ignored) {}

            if (!tagParts.isEmpty()) {
                sb.append(",tag:{").append(String.join(",", tagParts)).append("}");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    public static int getBukkitVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String v = name.substring(name.lastIndexOf('.') + 1) + ".";
        String[] version = v.replace('_', '.').split("\\.");

        if (Objects.equals(version[0], "craftbukkit")) return 0;

        int lesserVersion = 0;
        try {
            lesserVersion = Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {
        }
        return Integer.parseInt((version[0] + version[1]).substring(1) + lesserVersion);
    }

    public static String capitalize(String text) {
        StringBuilder cap = new StringBuilder();
        text = text.replace("_", " ");
        for (String t : text.split(" ")) {
            if (t.length() > 2) {
                cap.append(t.substring(0, 1).toUpperCase()).append(t.substring(1).toLowerCase()).append(" ");
            } else {
                cap.append(t).append(" ");
            }
        }
        return cap.substring(0, cap.length() - 1);
    }

    private static String escapeForSNBT(String json) {
        return json.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static List<String> wrapWithQuotes(List<String> raw) {
        List<String> out = new ArrayList<>();
        for (String s : raw) {
            out.add("'" + s + "'");
        }
        return out;
    }
}

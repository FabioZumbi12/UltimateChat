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

package br.net.fabiozumbi12.UltimateFancy;

import com.google.common.base.Utf8;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to generate JSON elements.
 *
 * @author FabioZumbi12
 */

@SuppressWarnings({"unchecked"})
public class UltimateFancy {

    private String hexColor = "";
    private String lastRGBColor = "";
    private ChatColor lastColor = ChatColor.WHITE;
    private ChatColor last2Color = null;
    private JSONArray constructor;
    private HashMap<String, Boolean> lastFormats;
    private List<JSONObject> workingGroup;
    private List<ExtraElement> pendentElements;
    private final JavaPlugin plugin;

    /**
     * Creates a new instance of UltimateFancy.
     */
    public UltimateFancy(JavaPlugin plugin) {
        this.plugin = plugin;
        constructor = new JSONArray();
        workingGroup = new ArrayList<>();
        lastFormats = new HashMap<>();
        pendentElements = new ArrayList<>();
    }

    /**
     * Creates a new instance of UltimateFancy with an initial text.
     *
     * @param text {@code String}
     */
    public UltimateFancy(JavaPlugin plugin, String text) {
        this.plugin = plugin;
        constructor = new JSONArray();
        workingGroup = new ArrayList<>();
        lastFormats = new HashMap<>();
        pendentElements = new ArrayList<>();
        text(text);
    }

    /**
     * Root text to show with the colors parsed, close the last text properties and start a new text block.
     *
     * @param text text to show with the colors parsed
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy coloredTextAndNext(String text) {
        text = UChatColor.translateAlternateColorCodes(text);
        return this.textAndNext(text);
    }

    /**
     * Root text to show and close the last text properties and start a new text block.
     *
     * @param text text to show
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy textAndNext(String text) {
        this.text(text);
        return next();
    }

    /**
     * Root text to show with the colors parsed.
     *
     * @param text text to show with the colors parsed
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy coloredText(String text) {
        text = UChatColor.translateAlternateColorCodes(text);
        return this.text(text);
    }

    private List<JSONObject> parseColors(String text) {
        List<JSONObject> jsonList = new ArrayList<>();
        List<String> vanillaColors = new ArrayList<>();

        int hexCount = 0;
        StringBuilder hexValue = new StringBuilder();
        for (String vColor:text.split("(?=§)")){
            if (hexCount > 0) {
                if (hexCount >= 7) {
                    String lastValue = vanillaColors.get(vanillaColors.size()-1);
                    vanillaColors.set(vanillaColors.size()-1, lastValue.replace("%uchathex-color%", hexValue.toString()));
                    hexValue = new StringBuilder();
                    hexCount = 0;
                }
                else {
                    if (vColor.startsWith("§")){
                        hexValue.append(vColor.replace("§", ""));
                        hexCount++;
                        continue;
                    } else {
                        String lastValue = vanillaColors.get(vanillaColors.size()-1);
                        vanillaColors.set(vanillaColors.size()-1, lastValue.replace("#%uchathex-color%", ""));
                        hexValue = new StringBuilder();
                        hexCount = 0;
                    }
                }
            }
            if (vColor.startsWith("§x")) {
                vanillaColors.add("#%uchathex-color%"+vColor.replaceAll("§x", ""));
                hexCount = 1;
                continue;
            }
            if (vColor.startsWith("§#")) {
                vColor = vColor.substring(1);
            }
            vanillaColors.add(vColor);
        }

        for (String part : vanillaColors) {
            JSONObject workingText = new JSONObject();
            Matcher m = Pattern.compile("#[A-Fa-f0-9]{6}|#[A-Fa-f0-9]{3}").matcher(part);
            // Hex colors
            if (m.find()) {

                // Fix colors before
                filterColors(workingText);

                workingText.put("color", m.group());
                workingText.put("text", part.replace(m.group(), ""));

                hexColor = m.group();
                lastRGBColor = hexColor;
                if (lastColor.isColor()) lastColor = ChatColor.WHITE;
                if (last2Color != null && last2Color.isColor()) last2Color = null;

                //fix colors from latest
                filterColors(workingText);
                if (part.length() == 2) continue;
            } else {
                hexColor = "";

                // Fix colors before
                filterColors(workingText);

                // Vanilla collors
                Matcher matcher1 = Pattern.compile("^§([0-9a-fA-Fk-oK-ORr]).*$").matcher(part);
                if (matcher1.find()) {
                    ChatColor color = ChatColor.getByChar(matcher1.group(1).charAt(0));
                    if (color.isColor()) {
                        lastColor = color;
                        last2Color = null;
                        lastRGBColor = "";
                    } else {
                        // Set a second color if the first color is format
                        if (lastColor.isColor()) last2Color = lastColor;
                        lastColor = color;
                        lastRGBColor = "";
                    }
                    //fix colors from latest
                    filterColors(workingText);
                    if (part.length() == 2) continue;
                }

                //continue if empty
                if (UChatColor.stripColor(part).isEmpty()) {
                    continue;
                }

                workingText.put("text", UChatColor.stripColor(part));

                //fix colors after
                filterColors(workingText);
            }

            if (!workingText.containsKey("color")) {
                workingText.put("color", "white");
            }
            jsonList.add(workingText);
        }
        return jsonList;
    }

    /**
     * Root text to show on chat.
     *
     * @param text Root text to show on chat
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy text(String text) {
        workingGroup.addAll(parseColors(text));
        return this;
    }

    /**
     * Root text to show on chat, but in first position.
     *
     * @param text Root text to show on chat in first position
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy textAtStart(String text) {
        JSONArray jarray = new JSONArray();
        jarray.addAll(parseColors(text));
        jarray.addAll(getStoredElements());
        this.constructor = jarray;
        return this;
    }

    /**
     * Root text to show on chat, on last position.
     *
     * @param json Append other json
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendObject(JSONObject json) {
        workingGroup.add(json);
        return this;
    }

    /**
     * Root text to show on chat, on last position.
     *
     * @param jsonObject Append other json string
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendString(String jsonObject) {
        Object obj = JSONValue.parse(jsonObject);
        if (obj instanceof JSONObject) {
            workingGroup.add((JSONObject) obj);
        }
        if (obj instanceof JSONArray) {
            for (Object object : ((JSONArray) obj)) {
                if (object.toString().isEmpty()) continue;
                if (object instanceof JSONArray) {
                    appendString(object.toString());
                } else {
                    workingGroup.add((JSONObject) JSONValue.parse(object.toString()));
                }
            }
        }
        return this;
    }

    /**
     * Raw working elements from this object
     *
     * @return list with raw elements of actual working elements.
     */
    public List<JSONObject> getWorkingElements() {
        return this.workingGroup;
    }

    /**
     * Raw elements already stored into this object
     *
     * @return list with raw elements of stored json elements.
     */
    public List<JSONObject> getStoredElements() {
        return new ArrayList<JSONObject>(this.constructor);
    }

    /**
     * Remove an json element from actual elements
     *
     * @param json json element to remove
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy removeObject(JSONObject json) {
        this.workingGroup.remove(json);
        this.constructor.remove(json);
        return this;
    }

    /**
     * Append text at beginning of all elements stored
     *
     * @param json string element to remove
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendAtFirst(String json) {
        Object obj = JSONValue.parse(json);
        if (obj instanceof JSONObject) {
            appendAtFirst((JSONObject) obj);
        }
        if (obj instanceof JSONArray) {
            for (Object object : ((JSONArray) obj)) {
                if (object.toString().isEmpty()) continue;
                appendAtFirst((JSONObject) JSONValue.parse(object.toString()));
            }
        }
        return this;
    }

    /**
     * Append text at beginning of all elements stored
     *
     * @param json json element to remove
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendAtFirst(JSONObject json) {
        JSONArray jarray = new JSONArray();
        jarray.add(json);
        jarray.addAll(getStoredElements());
        this.constructor = jarray;
        return this;
    }

    /**
     * Append text at the end of all elements stored
     *
     * @param json json element to remove
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendAtEnd(String json) {
        Object obj = JSONValue.parse(json);
        if (obj instanceof JSONObject) {
            appendAtEnd((JSONObject) obj);
        }
        if (obj instanceof JSONArray) {
            for (Object object : ((JSONArray) obj)) {
                if (object.toString().isEmpty()) continue;
                appendAtEnd((JSONObject) JSONValue.parse(object.toString()));
            }
        }
        return this;
    }

    /**
     * Append text at the end of all elements stored
     *
     * @param json json element to remove
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendAtEnd(JSONObject json) {
        List<JSONObject> jarray = new ArrayList<>(getWorkingElements());
        jarray.add(json);
        this.workingGroup = jarray;
        return this;
    }

    /**
     * Get all elements stored as Fancy elements
     *
     * @return instance of same {@link UltimateFancy}.
     */
    public List<UltimateFancy> getFancyElements() {
        next();
        List<UltimateFancy> list = new ArrayList<>();
        for (Object obj : this.constructor) {
            if (obj instanceof JSONObject) {
                list.add(new UltimateFancy(plugin).appendAtEnd((JSONObject) obj));
            }
        }
        return list;
    }

    /**
     * Append other fancy element
     *
     * @param fancy json element to remove
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy appendFancy(UltimateFancy fancy) {
        this.appendAtEnd(fancy.toJson());
        return this;
    }

    private void filterColors(JSONObject obj) {
        for (Entry<String, Boolean> format : lastFormats.entrySet()) {
            obj.put(format.getKey(), format.getValue());
        }

        // Parse last color
        if (!hexColor.isEmpty()) {
            obj.put("color", hexColor);
        } else {
            if (lastColor.isColor()) obj.put("color", lastColor.name().toLowerCase());
        }

        if (lastColor.equals(ChatColor.RESET) || (lastColor.isColor() || lastColor.isFormat())) {
            for (String format : lastFormats.keySet()) {
                if (lastColor.isFormat() && format.equalsIgnoreCase("color")) continue;
                obj.remove(format);
            }
            if (lastColor.equals(ChatColor.RESET)) {
                last2Color = null;
                lastRGBColor = "";
            }
            lastFormats.clear();
        }

        if (lastColor.isFormat()) {
            String formatStr = lastColor.name().toLowerCase();
            if (lastColor.equals(ChatColor.MAGIC)) {
                formatStr = "obfuscated";
            }
            if (lastColor.equals(ChatColor.UNDERLINE)) {
                formatStr = "underlined";
            }
            lastFormats.put(formatStr, true);
            obj.put(formatStr, true);
        }

        // Parse second color if the first color is format
        if (last2Color != null)
            obj.put("color", last2Color.name().toLowerCase());
        else if (!lastRGBColor.isEmpty()) {
            obj.put("color", lastRGBColor);
        }
    }

    /**
     * Send the JSON message to a {@link CommandSender} via {@code tellraw}.
     *
     * @param to {@link CommandSender}
     */
    public void send(CommandSender to) {
        next();
        if (to instanceof Player) {
            performCommand((Player) to);
        } else {
            to.sendMessage(toOldFormat());
        }
    }

    public void send(CommandSender to, boolean json) {
        next();
        if (to instanceof Player) {
            if (json) {
                performCommand((Player) to);
            } else {
                to.sendMessage(toOldFormat());
            }
        } else {
            to.sendMessage(toOldFormat());
        }
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    private String toJson() {
        next();
        return "[\"\"," + constructor.toJSONString().substring(1);
    }

    /**
     * Close the last text properties and start a new text element.
     *
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy next() {
        if (workingGroup.size() > 0) {
            for (JSONObject obj : workingGroup) {
                if (obj.containsKey("text") && obj.get("text").toString().length() > 0) {
                    for (ExtraElement element : pendentElements) {
                        obj.put(element.getAction(), element.getJson());
                    }
                    constructor.add(obj);
                }
            }
        }
        workingGroup = new ArrayList<>();
        pendentElements = new ArrayList<>();
        return this;
    }

    /**
     * Add a command to execute on click the text.
     *
     * @param cmd {@link String}
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy clickRunCmd(String cmd) {
        pendentElements.add(new ExtraElement("clickEvent", parseJson("run_command", cmd)));
        return this;
    }

    /**
     * @param cmd {@link String}
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy clickSuggestCmd(String cmd) {
        pendentElements.add(new ExtraElement("clickEvent", parseJson("suggest_command", cmd)));
        return this;
    }

    /**
     * URL to open on external browser when click this text.
     *
     * @param url {@link String}
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy clickOpenURL(URL url) {
        pendentElements.add(new ExtraElement("clickEvent", parseJson("open_url", url.toString())));
        return this;
    }

    /**
     * Text to show on hover the mouse under this text.
     *
     * @param text {@link String}
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy hoverShowText(String text) {
        pendentElements.add(new ExtraElement("hoverEvent", parseHoverText(text)));
        return this;
    }

    /**
     * Item to show on chat message under this text.
     *
     * @param item {@link ItemStack}
     * @return instance of same {@link UltimateFancy}.
     */
    public UltimateFancy hoverShowItem(ItemStack item) {
        JSONObject jItem = parseHoverItem(item);
        if (Utf8.encodedLength(jItem.toJSONString()) > 32767)
            pendentElements.add(new ExtraElement("hoverEvent", parseHoverItem(new ItemStack(item.getType()))));

        pendentElements.add(new ExtraElement("hoverEvent", jItem));
        return this;
    }

    /**
     * Convert JSON string to Minecraft string.
     *
     * @return {@code String} with traditional Minecraft colors.
     */
    public String toOldFormat() {
        StringBuilder result = new StringBuilder();
        for (Object mjson : constructor) {
            JSONObject json = (JSONObject) mjson;
            if (!json.containsKey("text")) continue;

            try {
                //get color
                String colorStr = json.get("color").toString();
                try {
                    ChatColor color = ChatColor.valueOf(colorStr.toUpperCase());
                    if (color.equals(ChatColor.WHITE)) {
                        result.append(ChatColor.RESET);
                    } else {
                        result.append(color);
                    }
                } catch (Exception ignored) {}

                //get format
                for (ChatColor frmt : ChatColor.values()) {
                    if (frmt.isColor()) continue;
                    String frmtStr = frmt.name().toLowerCase();
                    if (frmt.equals(ChatColor.MAGIC)) {
                        frmtStr = "obfuscated";
                    }
                    if (frmt.equals(ChatColor.UNDERLINE)) {
                        frmtStr = "underlined";
                    }
                    if (json.containsKey(frmtStr)) {
                        result.append(frmt);
                    }
                }
            } catch (Exception ignored) {
            }

            result.append(json.get("text").toString());
        }
        return result.toString();
    }

    private void performCommand(Player to) {
        Bukkit.getScheduler().callSyncMethod(plugin, () -> {
            if (Bukkit.getOnlinePlayers().stream().anyMatch(p -> p.equals(to))) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + to.getName() + " " + toJson());
            }
            return null;
        });
    }
    private JSONObject parseHoverText(String text) {
        JSONArray extraArr = addColorToArray(UChatColor.translateAlternateColorCodes(text));
        JSONObject objExtra = new JSONObject();
        objExtra.put("text", "");
        objExtra.put("extra", extraArr);
        JSONObject obj = new JSONObject();
        obj.put("action", "show_text");
        obj.put("value", objExtra);
        return obj;
    }

    private JSONObject parseJson(String action, String value) {
        JSONObject obj = new JSONObject();
        obj.put("action", action);
        obj.put("value", value);
        return obj;
    }

    private JSONObject parseHoverItem(ItemStack item) {
        JSONObject obj = new JSONObject();
        obj.put("action", "show_item");
        String jItem = convertItemStackToJson(item);
        if (Utf8.encodedLength(jItem) > 32767)
            obj.put("value", convertItemStackToJson(new ItemStack(item.getType())));

        obj.put("value", jItem);
        return obj;
    }

    private String convertItemStackToJson(ItemStack itemStack) {
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        Object nmsNbtTagCompoundObj;
        Object nmsItemStackObj;
        Object itemAsJsonObject;

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            return null;
        }
        return itemAsJsonObject.toString();
    }

    private JSONArray addColorToArray(String text) {
        JSONArray extraArr = new JSONArray();
        ChatColor color = ChatColor.WHITE;
        for (String part : text.split("(?=" + ChatColor.COLOR_CHAR + "[0-9a-fA-Fk-oK-ORr])")) {
            JSONObject objExtraTxt = new JSONObject();
            Matcher match = Pattern.compile("^" + ChatColor.COLOR_CHAR + "([0-9a-fA-Fk-oK-ORr]).*$").matcher(part);
            if (match.find()) {
                color = ChatColor.getByChar(match.group(1).charAt(0));
                if (part.length() == 2) continue;
            }
            objExtraTxt.put("text", UChatColor.stripColor(part));
            if (color.isColor()) {
                objExtraTxt.put("color", color.name().toLowerCase());
                objExtraTxt.remove("obfuscated");
                objExtraTxt.remove("underlined");
                objExtraTxt.remove(ChatColor.STRIKETHROUGH.name().toLowerCase());
            }
            if (color.equals(ChatColor.RESET)) {
                objExtraTxt.put("color", "white");
                objExtraTxt.remove("obfuscated");
                objExtraTxt.remove("underlined");
                objExtraTxt.remove(ChatColor.STRIKETHROUGH.name().toLowerCase());
            }
            if (color.isFormat()) {
                if (color.equals(ChatColor.MAGIC)) {
                    objExtraTxt.put("obfuscated", true);
                } else if (color.equals(ChatColor.UNDERLINE)) {
                    objExtraTxt.put("underlined", true);
                } else {
                    objExtraTxt.put(color.name().toLowerCase(), true);
                }
            }
            extraArr.add(objExtraTxt);
        }
        return extraArr;
    }

    public void setContructor(JSONArray array) {
        this.constructor = array;
    }

    @Override
    public UltimateFancy clone() {
        UltimateFancy newFanci = new UltimateFancy(plugin);
        newFanci.constructor = this.constructor;
        newFanci.pendentElements = this.pendentElements;
        newFanci.workingGroup = this.workingGroup;
        newFanci.lastFormats = this.lastFormats;
        return newFanci;
    }

    /**
     * An immutable pair of actions and {@link JSONObject} values.
     *
     * @author FabioZumbi12
     */
    public static class ExtraElement {
        private final String action;
        private final JSONObject json;

        public ExtraElement(String action, JSONObject json) {
            this.action = action;
            this.json = json;
        }

        public String getAction() {
            return this.action;
        }

        public JSONObject getJson() {
            return this.json;
        }
    }
}

/**
 * Class to use with new rgb colors from 1.16
 */
class UChatColor {

    public static final String HEX_PATTERN = "&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})";

    public static String stripColor(String text) {
        text = ChatColor.stripColor(text);
        text = text.replaceAll(HEX_PATTERN, "");
        return text;
    }

    public static String translateAlternateColorCodes(String text) {
        Matcher matcher = Pattern.compile(HEX_PATTERN).matcher(text);
        while (matcher.find()) {
            String toReplace = matcher.group(0);
            String find = matcher.group(1);
            text = text.replace(toReplace, "§#"+find);
        }
        return ChatColor.translateAlternateColorCodes('&',text);
    }
}

/**
 * Help get native itemstack properties
 * 
 * @author FabioZumbi12
 */
class ReflectionUtil {
    private static String versionString;
    private static final Map<String, Class<?>> loadedNMSClasses = new HashMap<>();
    private static final Map<String, Class<?>> loadedOBCClasses = new HashMap<>();
    private static final Map<Class<?>, Map<String, Method>> loadedMethods = new HashMap<>();

    public static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    public static Class<?> getNMSClass(String nmsClassName) {
        if (loadedNMSClasses.containsKey(nmsClassName)) {
            return loadedNMSClasses.get(nmsClassName);
        }

        String clazzName = "net.minecraft.server." + getVersion() + nmsClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return loadedNMSClasses.put(nmsClassName, null);
        }

        loadedNMSClasses.put(nmsClassName, clazz);
        return clazz;
    }

    public static Class<?> getOBCClass(String obcClassName) {
        if (loadedOBCClasses.containsKey(obcClassName)) {
            return loadedOBCClasses.get(obcClassName);
        }

        String clazzName = "org.bukkit.craftbukkit." + getVersion() + obcClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            loadedOBCClasses.put(obcClassName, null);
            return null;
        }

        loadedOBCClasses.put(obcClassName, clazz);
        return clazz;
    }

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        if (!loadedMethods.containsKey(clazz)) {
            loadedMethods.put(clazz, new HashMap<>());
        }

        Map<String, Method> methods = loadedMethods.get(clazz);

        if (methods.containsKey(methodName)) {
            return methods.get(methodName);
        }

        try {
            Method method = clazz.getMethod(methodName, params);
            methods.put(methodName, method);
            loadedMethods.put(clazz, methods);
            return method;
        } catch (Exception e) {
            e.printStackTrace();
            methods.put(methodName, null);
            loadedMethods.put(clazz, methods);
            return null;
        }
    }
}

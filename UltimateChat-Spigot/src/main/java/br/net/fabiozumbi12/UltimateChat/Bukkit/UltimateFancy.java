package br.net.fabiozumbi12.UltimateChat.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import br.net.fabiozumbi12.UltimateChat.Bukkit.UCLogger.timingType;

/**Class to generate JSON elements to use with UltimateChat.
 * @author FabioZumbi12
 *
 */

@SuppressWarnings({"deprecation","unchecked"})
public class UltimateFancy {

	private ChatColor lastColor = ChatColor.WHITE;
	private JSONArray constructor;
	private HashMap<String, Boolean> lastformats;
	private List<JSONObject> workingGroup;
	private List<ExtraElement> pendentElements;
	
	/**
	 * Creates a new instance of UltimateFancy.
	 */
	public UltimateFancy(){
		constructor = new JSONArray();
		workingGroup = new ArrayList<JSONObject>();
		lastformats = new HashMap<String, Boolean>();
		pendentElements = new ArrayList<ExtraElement>();
	}
	
	/**Creates a new instance of UltimateFancy with an initial text.
	 * @param text {@code String}
	 */
	public UltimateFancy(String text){
		constructor = new JSONArray();
		workingGroup = new ArrayList<JSONObject>();
		lastformats = new HashMap<String, Boolean>();
		pendentElements = new ArrayList<ExtraElement>();
		text(text);
	}
	
	/**Root text to show with the colors parsed, close the last text properties and start a new text block.
	 * @param text
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy coloredTextAndNext(String text){
		text = ChatColor.translateAlternateColorCodes('&', text);
		return this.textAndNext(text);
	}
	
	/**Root text to show and close the last text properties and start a new text block.
	 * @param text
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy textAndNext(String text){	
		this.text(text);
		return next();
	}
	
	/**Root text to show with the colors parsed.
	 * @param text
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy coloredText(String text){
		text = ChatColor.translateAlternateColorCodes('&', text);
		return this.text(text);
	}
	
	/**Root text to show on chat.
	 * @param text
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy text(String text){		
		for (String part:text.split("(?="+ChatColor.COLOR_CHAR+")")){
			JSONObject workingText = new JSONObject();	
			
			//fix colors before
			filterColors(workingText);
			
			Matcher match = Pattern.compile("^"+ChatColor.COLOR_CHAR+"([0-9a-fk-or]).*$").matcher(part);
			if (match.find()){
				lastColor = ChatColor.getByChar(match.group(1).charAt(0));
				//fix colors from latest
				filterColors(workingText);
				if (part.length() == 2) continue;
			}
			//continue if empty
			if (ChatColor.stripColor(part).isEmpty()){
				continue;
			}
			workingText.put("text", ChatColor.stripColor(part));
						
			//fix colors after
			filterColors(workingText);
			
			if (!workingText.containsKey("color")){
				workingText.put("color", "white");
			}					
			workingGroup.add(workingText);
		}			
		return this;
	}
	
	private JSONObject filterColors(JSONObject obj){
		for (Entry<String, Boolean> format:lastformats.entrySet()){
			obj.put(format.getKey(), format.getValue());
		}
		if (lastColor.isFormat()){
			String formatStr = lastColor.name().toLowerCase();
			if (lastColor.equals(ChatColor.MAGIC)){
				formatStr = "obfuscated";
			}
			if (lastColor.equals(ChatColor.UNDERLINE)){
				formatStr = "underlined";
			}
			lastformats.put(formatStr, true);
			obj.put(formatStr, true);
		} 
		if (lastColor.isColor()){
			obj.put("color", lastColor.name().toLowerCase());
		}
		if (lastColor.equals(ChatColor.RESET)){
			obj.put("color", "white");
			for (String format:lastformats.keySet()){
				lastformats.put(format, false);
				obj.put(format, false);
			}
		}
		return obj;
	}
	
	/**Send the JSON message to a {@link CommandSender} via {@code tellraw}.
	 * @param to {@link CommandSender}
	 */
	public void send(CommandSender to){
		next();
		if (to instanceof Player){
			if (UChat.get().getConfig().getBool("general.json-events")){
				UChat.get().getUCLogger().timings(timingType.END, "UltimateFancy#send()|json-events:true|before tellraw");
				UCUtil.performCommand((Player)to, Bukkit.getConsoleSender(), "tellraw " + to.getName() + " " + toJson());
				UChat.get().getUCLogger().timings(timingType.END, "UltimateFancy#send()|json-events:true|after tellraw");
			} else {
				UChat.get().getUCLogger().timings(timingType.END, "UltimateFancy#send()|json-events:false|before send");
				to.sendMessage(toOldFormat());
				UChat.get().getUCLogger().timings(timingType.END, "UltimateFancy#send()|json-events:false|after send");
			}			
		} else {
			to.sendMessage(toOldFormat());
		}		
		UChat.get().getUCLogger().debug("JSON: "+toJson());
	}
	
	private String toJson(){
		return "[\"\","+constructor.toJSONString().substring(1);
	}
	
	/**Close the last text properties and start a new text block.
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy next(){
		if (workingGroup.size() > 0){
			for (JSONObject obj:workingGroup){
				if (obj.containsKey("text") && obj.get("text").toString().length() > 0){					
					for (ExtraElement element:pendentElements){							
						obj.put(element.getAction(), element.getJson());
					}
					constructor.add(obj);
				}
			}
		}		
		workingGroup = new ArrayList<JSONObject>();
		pendentElements = new ArrayList<ExtraElement>();		
		return this;
	}
	
	/**Add a command to execute on click the text.
	 * @param cmd {@link String}
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy clickRunCmd(String cmd){
		pendentElements.add(new ExtraElement("clickEvent",parseJson("run_command", cmd)));
		return this;
	}
	
	/**
	 * @param cmd {@link String}
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy clickSuggestCmd(String cmd){
		pendentElements.add(new ExtraElement("clickEvent",parseJson("suggest_command", cmd)));
		return this;
	}
	
	/**URL to open on external browser when click this text.
	 * @param url {@link String}
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy clickOpenURL(String url){
		pendentElements.add(new ExtraElement("clickEvent",parseJson("open_url", url)));
		return this;
	}
	
	/**Text to show on hover the mouse under this text.
	 * @param text {@link String}
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy hoverShowText(String text){
		pendentElements.add(new ExtraElement("hoverEvent",parseHoverText(text)));
		return this;
	}
	
	/**Item to show on chat message under this text.
	 * @param item {@link ItemStack}
	 * @return instance of same {@link UltimateFancy}.
	 */
	public UltimateFancy hoverShowItem(ItemStack item){
		pendentElements.add(new ExtraElement("hoverEvent",parseHoverItem(item)));
		return this;
	}
	
	/**Convert JSON string to Minecraft string.
	 * @return {@code String} with traditional Minecraft colors.
	 */
	public String toOldFormat(){
		StringBuilder result = new StringBuilder();
		for (Object mjson:constructor){	
			JSONObject json = (JSONObject)mjson;
			if (!json.containsKey("text")) continue;	
			
			//get color
			String colorStr = json.get("color").toString();
			if (ChatColor.valueOf(colorStr.toUpperCase()) != null){				
				ChatColor color = ChatColor.valueOf(colorStr.toUpperCase());
				if (color.equals(ChatColor.WHITE)){
					result.append(String.valueOf(ChatColor.RESET));
				} else {
					result.append(String.valueOf(color));
				}
			}
			
			//get format
			for (ChatColor frmt:ChatColor.values()){
				if (frmt.isColor()) continue;
				String frmtStr = frmt.name().toLowerCase();
				if (frmt.equals(ChatColor.MAGIC)){
					frmtStr = "obfuscated";
				}
				if (frmt.equals(ChatColor.UNDERLINE)){
					frmtStr = "underlined";
				}
				if (json.containsKey(frmtStr)){
					result.append(String.valueOf(frmt));
				}
			}
			result.append(json.get("text").toString());				
		}
		return result.toString();
	}
	
	private JSONObject parseHoverText(String text){
		JSONArray extraArr = addColorToArray(ChatColor.translateAlternateColorCodes('&', text));		
		JSONObject objExtra = new JSONObject();
		objExtra.put("text", "");
		objExtra.put("extra", extraArr);
		JSONObject obj = new JSONObject();
		obj.put("action", "show_text");	
		obj.put("value", objExtra);	
		return obj;
	}
	
	private JSONObject parseJson(String action, String value){
		JSONObject obj = new JSONObject();
		obj.put("action", action);
		obj.put("value", value);
		return obj;
	}
	
	private JSONObject parseHoverItem(ItemStack item){		
		StringBuilder itemBuild = new StringBuilder();
		StringBuilder itemTag = new StringBuilder();		
		//serialize itemstack
		String itemType = item.getType().toString()
				.replace("_ITEM", "")
				.replace("_SPADE", "_SHOVEL")
				.replace("GOLD_", "GOLDEN_");		
		itemBuild.append("id:"+itemType+",Count:"+1+",Damage:"+item.getDurability()+",");		
		if (item.hasItemMeta()){
			ItemMeta meta = item.getItemMeta();
			if (meta.hasLore()){
				StringBuilder lore = new StringBuilder();
				for (String lorep:meta.getLore()){
					lore.append(lorep+",");
				}
				itemTag.append("display:{Lore:["+lore.toString().substring(0, lore.length()-1)+"]},");
			} else if (meta.hasDisplayName()){
				itemTag.append("display:{Name:"+meta.getDisplayName()+"},");
			} else if (meta.hasLore() && meta.hasDisplayName()){
				StringBuilder lore = new StringBuilder();
				for (String lorep:meta.getLore()){
					lore.append(lorep+",");
				}
				itemTag.append("display:{Name:"+meta.getDisplayName()+",Lore:["+lore.toString().substring(0, lore.length()-1)+"]},");
			}
			
			//enchants
			if (meta instanceof PotionMeta){
				StringBuilder itemEnch = new StringBuilder();
				itemEnch.append("CustomPotionEffects:[");
				if (UCUtil.getBukkitVersion() >= 190){
					PotionData pot = ((PotionMeta)meta).getBasePotionData();
					itemEnch.append("{Id:"+pot.getType().getEffectType().getId()+",Duration:"+pot.getType().getEffectType().getDurationModifier()+",Ambient:true,},");					
				} else {
					Potion pot = Potion.fromItemStack(item);
					itemEnch.append("{Id:"+pot.getType().getEffectType().getId()+",Duration:"+pot.getType().getEffectType().getDurationModifier()+",Ambient:true,},");
				}
				itemTag.append(itemEnch.toString().substring(0, itemEnch.length()-1)+"],");						
			} else if (meta instanceof EnchantmentStorageMeta){
				StringBuilder itemEnch = new StringBuilder();
				itemEnch.append("ench:[");
				for (Entry<Enchantment, Integer> ench:((EnchantmentStorageMeta)meta).getStoredEnchants().entrySet()){
					itemEnch.append("{id:"+ench.getKey().getId()+",lvl:"+ench.getValue()+"},");
				}
				itemTag.append(itemEnch.toString().substring(0, itemEnch.length()-1)+"],");				
			} else if (meta.hasEnchants()){
				StringBuilder itemEnch = new StringBuilder();
				itemEnch.append("ench:[");
				for (Entry<Enchantment, Integer> ench:meta.getEnchants().entrySet()){
					itemEnch.append("{id:"+ench.getKey().getId()+",lvl:"+ench.getValue()+"},");
				}
				itemTag.append(itemEnch.toString().substring(0, itemEnch.length()-1)+"],");
			}			
		}		
		if (itemTag.length() > 0){
			itemBuild.append("tag:{"+itemTag.toString().substring(0, itemTag.length()-1)+"},");
		}		
		JSONObject obj = new JSONObject();
		obj.put("action", "show_item");	
		obj.put("value", ChatColor.stripColor("{"+itemBuild.toString().substring(0, itemBuild.length()-1).replace(" ", "_")+"}"));
		return obj;
	}
		
	private JSONArray addColorToArray(String text){
		JSONArray extraArr = new JSONArray();
		ChatColor color = ChatColor.WHITE;
		for (String part:text.split("(?="+ChatColor.COLOR_CHAR+"[0-9a-fk-or])")){	
			JSONObject objExtraTxt = new JSONObject();
			Matcher match = Pattern.compile("^"+ChatColor.COLOR_CHAR+"([0-9a-fk-or]).*$").matcher(part);			
			if (match.find()){
				color = ChatColor.getByChar(match.group(1).charAt(0));
				if (part.length() == 2) continue;
			} 		
			objExtraTxt.put("text", ChatColor.stripColor(part));
			if (color.isColor()){	
				objExtraTxt.put("color", color.name().toLowerCase());			
			}
			if (color.equals(ChatColor.RESET)){
				objExtraTxt.put("color", "white");								
			}		
			if (color.isFormat()){
				if (color.equals(ChatColor.MAGIC)){
					objExtraTxt.put("obfuscated", true);
				} else {
					objExtraTxt.put(color.name().toLowerCase(), true);
				}				
			}
			extraArr.add(objExtraTxt);
		}		
		return extraArr;
	}
	
	/**An imutable pair of actions and {@link JSONObject} values.
	 * @author FabioZumbi12
	 *
	 */
	public class ExtraElement{
		private String action;
		private JSONObject json;
		
		public ExtraElement(String action, JSONObject json){
			this.action = action;
			this.json = json;
		}		
		public String getAction(){
			return this.action;
		}
		public JSONObject getJson(){
			return this.json;
		}
	}

}

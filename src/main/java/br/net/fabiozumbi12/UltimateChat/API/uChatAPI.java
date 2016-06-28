package br.net.fabiozumbi12.UltimateChat.API;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.UChat;
import br.net.fabiozumbi12.UltimateChat.config.UCYaml;

public class uChatAPI {

	/**Register a new tag and write on uChat configuration.
	 * @param tagName - {@code String} with tag name.
	 * @param format - {@code String} format to show on chat.
	 * @param clickCmd - {@code String} for click commands.
	 * @param hoverMessages - {@code List<String>} list with messages to show on mouse hover under tag. 
	 * @return {@code true} if sucess or {@code false} if already registred.
	 */
	public static boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages){
		if (UChat.config.getString("tags."+tagName+".format") == null){
			UChat.config.setConfig("tags."+tagName+".format", format);
			UChat.config.setConfig("tags."+tagName+".click-cmd", clickCmd);
			UChat.config.setConfig("tags."+tagName+".hover-messages", hoverMessages);
			UChat.config.save();
			return true;
		}
		return false;
	}
	
	/**Register a new channel and save on channels folder.
	 * @param chName {@code String} - Channel name.
	 * @param chAlias {@code String} - Channel alias.
	 * @param crossWorlds {@code boolean} - Messages in this channel cross worlds
	 * @param distance {@code int} - Distance the player will receive this channel messages.
	 * @param color {@code String} - Channel color.
	 * @param tagBuilder {@code String} - Tags names (set on main config) to show on chat.
	 * @param needFocus {@code boolean} - Need to use {@code /ch <alias>} to send messages or not.
	 * @param receiverMsg {@code boolean} - Send message if theres no player to receive the chat message.
	 * @param cost {@code double} - Cost to use this channel.
	 * @return {@code true} - If registered with sucess or {@code false} if channel alerady registered.
	 * @throws IOException - If can't save the channel file on channels folder.
	 */
	public static boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance, String color, String tagBuilder, boolean needFocus, boolean receiverMsg, double cost) throws IOException{
		if (UChat.config.getChannel(chName) != null){
			return false;
		}
		if (tagBuilder == null || tagBuilder.equals("")){
			tagBuilder = UChat.config.getString("general.default-tag-builder");			
		}
		UCChannel ch = new UCChannel(chName, chAlias, crossWorlds, distance, color, tagBuilder, needFocus, receiverMsg, cost);		
		UChat.config.addChannel(ch);
		
		File defch = new File(UChat.mainPath+File.separator+"channels"+File.separator+chName+".yml");		
		YamlConfiguration chFile = UCYaml.loadConfiguration(defch);
		chFile.set("name", ch.getName());
		chFile.set("alias", ch.getAlias());
		chFile.set("across-worlds", ch.crossWorlds());
		chFile.set("distance", ch.getDistance());
		chFile.set("color", ch.getColor());
		chFile.set("need-focus", ch.neeFocus());
		chFile.set("tag-builder", ch.getRawBuilder());
		chFile.set("cost", ch.getCost());
		chFile.save(defch);
		return true;
	}
}

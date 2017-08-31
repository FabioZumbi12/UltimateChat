package br.net.fabiozumbi12.UltimateChat.API;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;

import br.net.fabiozumbi12.UltimateChat.UCChannel;
import br.net.fabiozumbi12.UltimateChat.UChat;

public class uChatAPI {

	/**Register a new tag and write on uChat configuration.
	 * @param tagName - {@code String} with tag name.
	 * @param format - {@code String} format to show on chat.
	 * @param clickCmd - {@code String} for click commands.
	 * @param hoverMessages - {@code List<String>} list with messages to show on mouse hover under tag. 
	 * @return {@code true} if sucess or {@code false} if already registred.
	 */
	public static boolean registerNewTag(String tagName, String format, String clickCmd, List<String> hoverMessages){
		if (UChat.get().getUCConfig().getString("tags."+tagName+".format") == null){
			UChat.get().getUCConfig().setConfig("tags."+tagName+".format", format);
			UChat.get().getUCConfig().setConfig("tags."+tagName+".click-cmd", clickCmd);
			UChat.get().getUCConfig().setConfig("tags."+tagName+".hover-messages", hoverMessages);
			UChat.get().getUCConfig().save();
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
	public static boolean registerNewChannel(String chName, String chAlias, boolean crossWorlds, int distance, String color, String tagBuilder, boolean needFocus, boolean receiverMsg, double cost, boolean bungee) throws IOException{
		if (UChat.get().getUCConfig().getChannel(chName) != null){
			return false;
		}
		if (tagBuilder == null || tagBuilder.equals("")){
			tagBuilder = UChat.get().getUCConfig().getString("general.default-tag-builder");			
		}
		UCChannel ch = new UCChannel(chName, chAlias, crossWorlds, distance, color, tagBuilder, needFocus, receiverMsg, cost, bungee, false, false, "player", "", new ArrayList<String>(), new String(), false, true);		
		UChat.get().getUCConfig().addChannel(ch);		
		return true;
	}
	
	/**Gets an existing channel, or null if no channel matching name or alias
	 * @param chName - Chanel name or alias.
	 * @return {@link UCChanel} - The channel.
	 */
	public static UCChannel getChannel(String chName){
		return UChat.get().getUCConfig().getChannel(chName);
	}
		
	/**Gets the actual player channel 
	 * @param player - Player name.
	 * @return {@link UCChannel} - The player channel.
	 */
	public static UCChannel getPlayerChannel(String player){
		return UChat.get().getUCConfig().getPlayerChannel(Bukkit.getPlayer(player));
	}
	
	/**Get all available and loaded channels. 
	 * @return {@code Collection<UCChannel>} - Available channels or an empty collection.
	 */
	public static Collection<UCChannel> getChannels(){
		return UChat.get().getUCConfig().getChannels();
	}
	
	/**Get Vault chat hook instance from uchat.
	 * @return {@link Chat} or null if Vault not found.
	 */
	public static Chat getVaultChat(){
		return UChat.get().getVaultChat();
	}
	
	/**Get Vault economy hook instance from uchat.
	 * @return {@link Economy} or null if Vault not found.
	 */
	public static Economy getVaultEco(){
		return UChat.get().getVaultEco();
	}
	
	/**Get Vault permissions hook instance from uchat.
	 * @return {@link Permission} or null if Vault not found.
	 */
	public static Permission getVaultPerms(){
		return UChat.get().getVaultPerms();
	}
}

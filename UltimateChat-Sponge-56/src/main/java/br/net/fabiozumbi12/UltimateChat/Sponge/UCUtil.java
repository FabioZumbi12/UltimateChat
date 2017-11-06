package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;

public class UCUtil {

	public static Text toText(String str){
		str = str.replace("§", "&");
    	return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

	public static String toColor(String str){
    	return str.replaceAll("(&([a-fk-or0-9]))", "§$2"); 
    }
	
	public static String stripColor(String str) {
		return str.replaceAll("(&([a-fk-or0-9]))", "");
	}
	
	static boolean sendBroadcast(CommandSource sender, String[] args, boolean silent){
		StringBuilder message = new StringBuilder();
		 StringBuilder hover = new StringBuilder();
		 StringBuilder cmdline = new StringBuilder();
		 StringBuilder url = new StringBuilder();
		 boolean isHover = false;
		 boolean isCmd = false;
		 boolean isUrl = false;
		 for (String arg:args){
			 if (arg.contains(UChat.get().getConfig().root().broadcast.on_hover)){
				 hover.append(" "+arg.replace(UChat.get().getConfig().root().broadcast.on_hover, ""));
				 isHover = true;
				 isCmd = false;
				 isUrl = false;
				 continue;
			 }
			 if (arg.contains(UChat.get().getConfig().root().broadcast.on_click)){
				 cmdline.append(" "+arg.replace(UChat.get().getConfig().root().broadcast.on_click, ""));
				 isCmd = true;
				 isHover = false;
				 isUrl = false;
				 continue;
			 }
			 if (arg.contains(UChat.get().getConfig().root().broadcast.url)){
				 url.append(" "+arg.replace(UChat.get().getConfig().root().broadcast.url, ""));
				 isCmd = false;
				 isHover = false;
				 isUrl = true;
				 continue;
			 }
			 
			 if (isCmd){
				 cmdline.append(" "+arg);
			 } else
			 if (isHover){
				 hover.append(" "+arg);
			 } else
			 if (isUrl){
				 url.append(" "+arg);
			 } else {
				 message.append(" "+arg);
			 }
		 }
		 
		 if (message.toString().length() <= 1){			 
			 return false;
		 }
		 
		 if (!silent){
			 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("> Broadcast: &r"+message.toString().substring(1)));
		 }		 
		 			 
		 Builder fanci = Text.builder();
		 fanci.append(UCUtil.toText(message.toString().substring(1)));
		 
		 if (hover.toString().length() > 1){
			 fanci.onHover(TextActions.showText(UCUtil.toText(hover.toString().substring(1))));
			 if (!silent){
				 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> OnHover: &r"+hover.toString().substring(1)));
			 }
		 }				 
		 
		 if (cmdline.toString().length() > 1 && !silent){
			 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> OnClick: &r"+cmdline.toString().substring(1)));
		 }				 
		 
		 if (url.toString().length() > 1){
			 try {
				fanci.onClick(TextActions.openUrl(new URL(url.toString().substring(1))));
			} catch (MalformedURLException e) {}
			 if (!silent){
				 Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> Url: &r"+url.toString().substring(1)));
			 }				  
		 }	
		 
		 for (Player p:Sponge.getServer().getOnlinePlayers()){
			 if (cmdline.toString().length() > 1){
				 fanci.onClick(TextActions.runCommand("/"+cmdline.toString().substring(1).replace("{clicked}", p.getName())));						 
			 }
			 p.sendMessage(fanci.build());
		 }
		 return true;
	}
}

package br.net.fabiozumbi12.UltimateChat.Sponge.config;

import java.util.List;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class TagsCategory {
	
	public TagsCategory(){}
	
	@Setting
	public String format;
	
	@Setting(value="click-url")
	public String click_url;
	
	@Setting
	public String suggest;
	
	@Setting(value="click-cmd")
	public String click_cmd;
	
	@Setting(value="hover-messages")
	public List<String> hover_messages;
	
	@Setting
	public String permission;
	
	@Setting(value="show-in-worlds")
	public List<String> show_in_worlds;
	
	@Setting(value="hide-in-worlds")
	public List<String> hide_in_worlds;
	
	/**
	 * @param format
	 * @param click_cmd
	 * @param hover_messages
	 * @param permission
	 * @param show_in_worlds
	 * @param hide_in_worlds
	 */	
	public TagsCategory(String format, String click_cmd, List<String> hover_messages, String permission, List<String> show_in_worlds, List<String> hide_in_worlds){
		this.format = format;
		this.click_cmd = click_cmd;
		this.hover_messages = hover_messages;
		this.permission = permission;
		this.show_in_worlds = show_in_worlds;
		this.hide_in_worlds = hide_in_worlds;
	}
}
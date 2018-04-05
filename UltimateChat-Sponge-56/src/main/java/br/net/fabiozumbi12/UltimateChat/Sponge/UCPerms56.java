package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.util.Tristate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class UCPerms56 implements UCPerms{
	private final PermissionService permissionService;

	public UCPerms56(){
		this.permissionService = UChat.get().getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.local.read", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.local.write", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.global.read", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.global.write", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.tell", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.chat.click-urls", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cant-ignore.local", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.ignore.channel", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.ignore.player", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.clear", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.msgtoggle", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.password", Tristate.TRUE);
	}
	
	public boolean hasSpyPerm(CommandSource receiver, String ch){
		return hasPerm(receiver, "chat-spy."+ch) || hasPerm(receiver, "chat-spy.all");
	}
	
	public boolean cmdPerm(CommandSource p, String cmd){
		return hasPerm(p, "cmd."+cmd);
	}
	
	public boolean channelReadPerm(CommandSource p, UCChannel ch){
		UCChannel defCh = UChat.get().getDefChannel(p instanceof  Player ? ((Player)p).getWorld().getName() : null);
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".read");
	}
	
	public boolean channelWritePerm(CommandSource p, UCChannel ch){
		UCChannel defCh = UChat.get().getDefChannel(p instanceof  Player ? ((Player)p).getWorld().getName() : null);
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".write");
	}
	
	public boolean canIgnore(CommandSource sender, Object toignore) {
		return (!(toignore instanceof CommandSource) || !isAdmin((CommandSource) toignore)) && !sender.hasPermission("uchat.cant-ignore." + (toignore instanceof Player ? ((Player) toignore).getName() : ((UCChannel) toignore).getName()));
	}
	
	public boolean hasPerm(CommandSource p, String perm){
		return (p instanceof ConsoleSource) || p.hasPermission("uchat."+perm) || p.hasPermission("uchat.admin");
	}
	
	public Subject getGroupAndTag(User player) {
		HashMap<Integer, Subject> subs = new HashMap<>();
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				subs.put(sub.getParents().size(), sub);				
			}			
		}
		return subs.isEmpty() ? null : subs.get(Collections.max(subs.keySet()));
	}
	
	private static boolean isAdmin(CommandSource p){
		return (p instanceof ConsoleSource) || p.hasPermission("uchat.admin");
	}
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}

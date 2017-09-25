package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;

class UCPerms7 implements UCPerms {
	private PermissionService permissionService;
	
	UCPerms7(){
		this.permissionService = UChat.get().getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.local.read", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.local.write", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.global.read", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.channel.global.write", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.tell", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cant-ignore.local", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.ignore.channel", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.ignore.player", Tristate.TRUE);
		this.permissionService.getDefaults().getTransientSubjectData().setPermission(new HashSet<>(), "uchat.cmd.clear", Tristate.TRUE);
	}
	
	public boolean cmdPerm(CommandSource p, String cmd){
		return hasPerm(p, "cmd."+cmd);
	}
	
	public boolean channelReadPerm(CommandSource p, UCChannel ch){
		UCChannel defCh = UChat.get().getConfig().getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".read");
	}
	
	public boolean channelWritePerm(CommandSource p, UCChannel ch){
		UCChannel defCh = UChat.get().getConfig().getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase()+".write");
	}
	
	public boolean canIgnore(CommandSource sender, Object toignore){
		if (toignore instanceof CommandSource && isAdmin((CommandSource)toignore)){
			return false;
		} else {
			return !sender.hasPermission("uchat.cant-ignore."+ (toignore instanceof Player?((Player)toignore).getName():((UCChannel)toignore).getName()));
		}
	}
	
	public boolean hasPerm(CommandSource p, String perm){
		return (p instanceof ConsoleSource) || p.hasPermission("uchat."+perm) || p.hasPermission("uchat.admin");
	}
		
	public Subject getGroupAndTag(User player) throws InterruptedException, ExecutionException{
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();		
		for (SubjectReference sub:player.getParents()){
			if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)){
				Subject subj = sub.resolve().get();
				subs.put(subj.getParents().size(), subj);				
			}			
		}
		return subs.isEmpty() ? null : subs.get(Collections.max(subs.keySet()));
	}
	
	private static boolean isAdmin(CommandSource p){
		return (p instanceof CommandSource) || p.hasPermission("uchat.admin");
	}
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}

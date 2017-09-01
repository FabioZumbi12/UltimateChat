package br.net.fabiozumbi12.UltimateChat.Sponge;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

class UCPerms56 implements UCPerms{
	private PermissionService permissionService;

	UCPerms56(){
		this.permissionService = UChat.get().getGame().getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	public boolean cmdPerm(CommandSource p, String cmd){
		return hasPerm(p, "cmd."+cmd);
	}
	
	public boolean channelPerm(CommandSource p, UCChannel ch){
		UCChannel defCh = UChat.get().getConfig().getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase());
	}
	
	public boolean channelPerm(CommandSource p, String ch){
		UCChannel defCh = UChat.get().getConfig().getDefChannel();
		return defCh.getName().equals(ch) || defCh.getAlias().equals(ch) || hasPerm(p, "channel."+ch.toLowerCase());
	}
	
	public boolean hasPerm(CommandSource p, String perm){
		return (p instanceof ConsoleSource) || p.hasPermission("uchat."+perm) || p.hasPermission("uchat.admin");
	}
	
	public Subject getGroupAndTag(User player) throws InterruptedException, ExecutionException{
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();		
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				subs.put(sub.getParents().size(), sub);				
			}			
		}
		return subs.get(Collections.max(subs.keySet()));
	}
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}

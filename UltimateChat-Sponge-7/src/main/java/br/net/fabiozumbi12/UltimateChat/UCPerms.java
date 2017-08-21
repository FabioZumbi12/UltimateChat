package br.net.fabiozumbi12.UltimateChat;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;

class UCPerms {
	private PermissionService permissionService;

	UCPerms(Game game){
		this.permissionService = game.getServiceManager().getRegistration(PermissionService.class).get().getProvider();
	}
	
	boolean cmdPerm(CommandSource p, String cmd){
		return hasPerm(p, "cmd."+cmd);
	}
	
	boolean channelPerm(CommandSource p, UCChannel ch){
		UCChannel defCh = UChat.get().getConfig().getDefChannel();
		return defCh.equals(ch) || hasPerm(p, "channel."+ch.getName().toLowerCase());
	}
	
	boolean channelPerm(CommandSource p, String ch){
		UCChannel defCh = UChat.get().getConfig().getDefChannel();
		return defCh.getName().equals(ch) || defCh.getAlias().equals(ch) || hasPerm(p, "channel."+ch.toLowerCase());
	}
	
	boolean hasPerm(CommandSource p, String perm){
		return (p instanceof ConsoleSource) || p.hasPermission("uchat."+perm) || p.hasPermission("uchat.admin");
	}
		
	Subject getGroupAndTag(User player) throws InterruptedException, ExecutionException{
		HashMap<Integer, Subject> subs = new HashMap<Integer, Subject>();		
		for (SubjectReference sub:player.getParents()){
			if (sub.getCollectionIdentifier().equals(getGroups().getIdentifier()) && (sub.getSubjectIdentifier() != null)){
				Subject subj = sub.resolve().get();
				subs.put(subj.getParents().size(), subj);				
			}			
		}
		return subs.get(Collections.max(subs.keySet()));
	}
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}

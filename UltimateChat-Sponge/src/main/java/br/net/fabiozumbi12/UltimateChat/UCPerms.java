package br.net.fabiozumbi12.UltimateChat;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

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
		
	List<String> getGroups(User player){
		List<String> gps = new ArrayList<String>();
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				gps.add(sub.getIdentifier());
			}
		}
		return gps;
	}
	
	Subject getGroupAndTag(User player){
		Subject best = null;
		int beb = -1;
		for (Subject sub:player.getParents()){
			if (sub.getContainingCollection().equals(getGroups()) && (sub.getIdentifier() != null)){
				if(beb < sub.getParents().size()){
					beb = sub.getParents().size();
					best = sub;
				}
			}
		}
		//return gps;
		return best;
	}
	
	private SubjectCollection getGroups(){
		return permissionService.getGroupSubjects();		
	}
}

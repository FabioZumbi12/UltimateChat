package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public interface UCPerms {
    boolean hasSpyPerm(CommandSource receiver, String ch);

    boolean cmdPerm(CommandSource p, String cmd);

    boolean channelReadPerm(CommandSource p, UCChannel ch);

    boolean channelWritePerm(CommandSource p, UCChannel ch);

    boolean hasPerm(CommandSource p, String perm);

    boolean canIgnore(CommandSource sender, Object toignore);

    Subject getGroupAndTag(User player) throws InterruptedException, ExecutionException;

    HashMap<Integer, Subject> getPlayerGroups(User player) throws ExecutionException, InterruptedException;
}

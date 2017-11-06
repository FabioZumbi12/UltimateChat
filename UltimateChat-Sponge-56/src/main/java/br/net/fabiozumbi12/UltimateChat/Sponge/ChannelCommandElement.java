package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ChannelCommandElement extends CommandElement {

	public ChannelCommandElement(Text key) {
		super(key);
	}

	@Override
	protected Object parseValue(CommandSource source, CommandArgs args)
			throws ArgumentParseException {		
		return UChat.get().getChannel(args.next());
	}

	@Override
	public List<String> complete(CommandSource src, CommandArgs args,
			CommandContext context) {
		return UChat.get().getChannels().values().stream().filter(key->UChat.get().getPerms().channelWritePerm(src, key)).sorted(Comparator.comparing(UCChannel::getName)).map(UCChannel::getName).collect(Collectors.toList());
	}
	
	@Override
    public Text getUsage(CommandSource src) {
        return Text.of("<channel>");
    }
}

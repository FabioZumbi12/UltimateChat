package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

public class UCVHelper56 implements UCVHelper{

	@Override
	public Cause getCause(CommandSource src) {
		return Cause.source(src).named(NamedCause.notifier(src)).build();
	}

	@Override
	public Cause getCause(PluginContainer instance) {
		return Cause.of(NamedCause.owner(instance));
	}

	@Override
	public StringBuilder getEnchantments(StringBuilder str, ItemStack item) {
		for (ItemEnchantment enchant:item.get(Keys.ITEM_ENCHANTMENTS).get()){
			str.append("\n ").append(enchant.getEnchantment().getTranslation().get()).append(": ").append(enchant.getLevel());
		}
		return str;
	}

	@Override
	public ItemStack getItemInHand(Player sender){
		if (sender.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
			return sender.getItemInHand(HandTypes.MAIN_HAND).get();
		} else if (sender.getItemInHand(HandTypes.OFF_HAND).isPresent()){
			return sender.getItemInHand(HandTypes.OFF_HAND).get();
		}
		return ItemStack.empty();
	}

	@Override
	public ItemType getItemName(ItemStack itemStack){
		return itemStack.getItem();
	}
}

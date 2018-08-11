package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

public class UCVHelper8 implements UCVHelper {

	@Override
	public Cause getCause(CommandSource src) {
		return Cause.of(EventContext.empty(), src);
	}

	@Override
	public Cause getCause(PluginContainer instance) {
		return Cause.of(EventContext.empty(), instance);
	}

    @Override
    public StringBuilder getEnchantments(StringBuilder str, ItemStack item) {
        for (Enchantment enchant:item.get(Keys.ITEM_ENCHANTMENTS).get()){
            str.append("\n ").append(enchant.getType().getTranslation().get()).append(": ").append(enchant.getLevel());
        }
        return str;
    }

	@Override
	public ItemStack getItemInHand(Player sender) {
		if (!sender.getItemInHand(HandTypes.MAIN_HAND).isEmpty()){
			return sender.getItemInHand(HandTypes.MAIN_HAND);
		} else if (!sender.getItemInHand(HandTypes.OFF_HAND).isEmpty()){
			return sender.getItemInHand(HandTypes.OFF_HAND);
		}
		return ItemStack.empty();
	}

	@Override
	public ItemType getItemName(ItemStack itemStack){
		return itemStack.getType();
	}
}

package br.net.fabiozumbi12.UltimateChat.Sponge;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;

public class UCVHelper78 implements UCVHelper {

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
            str.append("\n "+enchant.getType().getTranslation().get()+": "+enchant.getLevel());
        }
        return str;
    }
}

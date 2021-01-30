/*
 Copyright @FabioZumbi12

 This class is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any
  damages arising from the use of this class.

 Permission is granted to anyone to use this class for any purpose, including commercial plugins, and to alter it and
 redistribute it freely, subject to the following restrictions:
 1 - The origin of this class must not be misrepresented; you must not claim that you wrote the original software. If you
 use this class in other plugins, an acknowledgment in the plugin documentation would be appreciated but is not required.
 2 - Altered source versions must be plainly marked as such, and must not be misrepresented as being the original class.
 3 - This notice may not be removed or altered from any source distribution.

 Esta classe é fornecida "como está", sem qualquer garantia expressa ou implícita. Em nenhum caso os autores serão
 responsabilizados por quaisquer danos decorrentes do uso desta classe.

 É concedida permissão a qualquer pessoa para usar esta classe para qualquer finalidade, incluindo plugins pagos, e para
 alterá-lo e redistribuí-lo livremente, sujeito às seguintes restrições:
 1 - A origem desta classe não deve ser deturpada; você não deve afirmar que escreveu a classe original. Se você usar esta
  classe em um plugin, uma confirmação de autoria na documentação do plugin será apreciada, mas não é necessária.
 2 - Versões de origem alteradas devem ser claramente marcadas como tal e não devem ser deturpadas como sendo a
 classe original.
 3 - Este aviso não pode ser removido ou alterado de qualquer distribuição de origem.
 */

package br.net.fabiozumbi12.UltimateChat.Sponge.util;

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
        for (Enchantment enchant : item.get(Keys.ITEM_ENCHANTMENTS).get()) {
            str.append("\n ").append(enchant.getType().getTranslation().get()).append(": ").append(enchant.getLevel());
        }
        return str;
    }

    @Override
    public ItemStack getItemInHand(Player sender) {
        if (!sender.getItemInHand(HandTypes.MAIN_HAND).isEmpty()) {
            return sender.getItemInHand(HandTypes.MAIN_HAND);
        } else if (!sender.getItemInHand(HandTypes.OFF_HAND).isEmpty()) {
            return sender.getItemInHand(HandTypes.OFF_HAND);
        }
        return ItemStack.empty();
    }

    @Override
    public ItemType getItemName(ItemStack itemStack) {
        return itemStack.getType();
    }
}

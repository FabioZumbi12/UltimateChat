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

package br.net.fabiozumbi12.UltimateChat.Sponge.Listeners;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UCPixelmonListener {
    private UChat plugin;
    private List<String> legendaries =
            Arrays.asList("Moltres", "Articuno", "Zapdos", "Mewtwo", "Mew", "Raikou", "Entei", "Suicune", "Lugia",
                    "Ho-Oh", "Celebi", "Regirock", "Regice", "Registeel", "Latias", "Latios", "Kyogre", "Groudon", "Rayquaza", "Jirachi", "Deoxys",
                    "Uxie", "Mesprit", "Azelf", "Dialga", "Palkia", "Heatran", "Regigigas", "Giratina", "Cresselia", "Manaphy", "Darkrai", "Shaymin", "Arceus",
                    "Victini", "Cobalion", "Terrakion", "Virizion", "Tornadus", "Thundurus", "Reshiram", "Zekrom", "Landorus", "Kyurem", "Keldeo", "Meloetta",
                    "Genesect", "Xerneas", "Yveltal", "Zygarde", "Diancie", "Hoopa", "Volcanion", "Type: Null", "Silvally", "Tapu Koko", "Tapu Lele", "Tapu Bulu", "Tapu Fini", "Cosmog",
                    "Cosmoem", "Solgaleo", "Lunala", "Nihilego", "Buzzwole", "Pheromosa", "Xurkitree", "Celesteela", "Kartana", "Guzzlord", "Necrozma", "Magearna",
                    "Marshadow", "Poipole", "Naganadel", "Stakataka", "Blacephalon");

    public UCPixelmonListener(UChat plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPixelmonSpawn(SpawnEntityEvent event) {
        if (event.getCause().root() instanceof Player) return;
        if (event.getEntities().size() > 0 && event.getEntities().get(0) instanceof Living) {
            Entity ent = event.getEntities().get(0);
            if (ent.getType().getName().equalsIgnoreCase("pixelmon")) {
                Matcher m = Pattern.compile("'(.*?)'").matcher(ent.toString());
                if (m.find()) {
                    String name = m.group(1);
                    if (legendaries.contains(name)) {
                        plugin.getUCJDA().sendPixelmonLegendary(String.format(plugin.getConfig().root().discord.pixelmon.legendary_text,
                                name,
                                ent.getTransform().getLocation().getBiome().getName(),
                                plugin.getConfig().root().general.world_names.getOrDefault(ent.getWorld().getName(), UCUtil.stripColor('&', ent.getWorld().getName()))));
                    }
                }

            }
        }
    }
}

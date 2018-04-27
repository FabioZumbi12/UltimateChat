package br.net.fabiozumbi12.UltimateChat.Sponge.Listeners;

import br.net.fabiozumbi12.UltimateChat.Sponge.UCUtil;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UCPixelmonListener {
    private UChat plugin;
    private List<String> legendaries =
            Arrays.asList("Moltres", "Articuno","Zapdos","Mewtwo","Mew","Raikou","Entei","Suicune","Lugia",
                    "Ho-Oh","Celebi","Regirock","Regice","Registeel","Latias","Latios","Kyogre","Groudon","Rayquaza","Jirachi","Deoxys",
                    "Uxie","Mesprit","Azelf","Dialga","Palkia","Heatran","Regigigas","Giratina","Cresselia","Manaphy","Darkrai","Shaymin","Arceus",
                    "Victini","Cobalion","Terrakion","Virizion","Tornadus","Thundurus","Reshiram","Zekrom","Landorus","Kyurem","Keldeo","Meloetta",
                    "Genesect","Xerneas","Yveltal","Zygarde","Diancie","Hoopa","Volcanion","Type: Null","Silvally","Tapu Koko","Tapu Lele","Tapu Bulu","Tapu Fini","Cosmog",
                    "Cosmoem","Solgaleo","Lunala","Nihilego","Buzzwole","Pheromosa","Xurkitree","Celesteela","Kartana","Guzzlord","Necrozma","Magearna",
                    "Marshadow","Poipole","Naganadel","Stakataka","Blacephalon");
    public UCPixelmonListener(UChat plugin){
        this.plugin = plugin;
    }

    @Listener
    public void onPixelmonSpawn(SpawnEntityEvent event){
        if (event.getEntities().size() > 0 && event.getEntities().get(0) instanceof Living){
            Entity ent = event.getEntities().get(0);
            if (ent.getType().getName().equalsIgnoreCase("pixelmon")){
                Matcher m = Pattern.compile("'(.*?)'").matcher(ent.toString());
                if (m.find()){
                    String name = m.group(1);
                    if (legendaries.contains(name)) {
                        plugin.getUCJDA().sendPixelmonLegendary(String.format(plugin.getConfig().root().discord.pixelmon.legendary_text,
                                name,
                                ent.getTransform().getLocation().getBiome().getName(),
                                plugin.getConfig().root().general.world_names.getOrDefault(ent.getWorld().getName(), UCUtil.stripColor(ent.getWorld().getName()))));
                    }
                }

            }
        }
    }
}

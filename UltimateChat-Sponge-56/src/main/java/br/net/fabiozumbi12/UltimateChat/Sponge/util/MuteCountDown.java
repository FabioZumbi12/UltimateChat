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

import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

public class MuteCountDown implements Consumer<Task> {
    final String p;
    int time;

    public MuteCountDown(String p, int t) {
        this.p = p;
        this.time = t * 60;
    }

    @Override
    public void accept(Task t) {
        if (UChat.get().timeMute.containsKey(p)) {
            time = UChat.get().timeMute.get(p) - 1;
        }
        if (UChat.get().mutes.contains(p)) {
            if (time > 0) {
                UChat.get().timeMute.put(p, time);
            } else {
                UChat.get().timeMute.remove(p);
                UChat.get().mutes.remove(p);
                UChat.get().unMuteInAllChannels(p);
                if (Sponge.getServer().getPlayer(p).isPresent()) {
                    UChat.get().getLang().sendMessage(Sponge.getServer().getPlayer(p).get(), UChat.get().getLang().get("channel.player.unmuted.all"));
                }
                t.cancel();
            }
        } else {
            t.cancel();
        }
    }
}

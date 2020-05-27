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

import br.net.fabiozumbi12.UltimateChat.Sponge.UCChannel;
import br.net.fabiozumbi12.UltimateChat.Sponge.UCMessages;
import br.net.fabiozumbi12.UltimateChat.Sponge.UChat;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;

public class UCUtil {

    public static Text toText(String str) {
        str = str.replace("§", "&");
        return TextSerializers.FORMATTING_CODE.deserialize(str);
    }

    public static String toColor(String str) {
        return str.replaceAll("(&([A-Fa-fK-Ok-oRr0-9]))", "§$2");
    }

    public static String stripColor(char symbol, String str) {
        return str.replaceAll("("+symbol+"([A-Fa-fK-Ok-oRr0-9]))", "");
    }

    public static boolean sendBroadcast(CommandSource src, String[] args, boolean silent) {
        StringBuilder message = new StringBuilder();
        StringBuilder hover = new StringBuilder();
        StringBuilder cmdline = new StringBuilder();
        StringBuilder url = new StringBuilder();
        boolean isHover = false;
        boolean isCmd = false;
        boolean isUrl = false;
        for (String arg : args) {
            arg = arg.replace("\\n", "\n");
            if (arg.contains(UChat.get().getConfig().root().broadcast.on_hover)) {
                hover.append(" ").append(arg.replace(UChat.get().getConfig().root().broadcast.on_hover, ""));
                isHover = true;
                isCmd = false;
                isUrl = false;
                continue;
            }
            if (arg.contains(UChat.get().getConfig().root().broadcast.on_click)) {
                cmdline.append(" ").append(arg.replace(UChat.get().getConfig().root().broadcast.on_click, ""));
                isCmd = true;
                isHover = false;
                isUrl = false;
                continue;
            }
            if (arg.contains(UChat.get().getConfig().root().broadcast.url)) {
                url.append(" ").append(arg.replace(UChat.get().getConfig().root().broadcast.url, ""));
                isCmd = false;
                isHover = false;
                isUrl = true;
                continue;
            }

            if (isCmd) {
                cmdline.append(" ").append(arg);
            } else if (isHover) {
                hover.append(" ").append(arg);
            } else if (isUrl) {
                url.append(" ").append(arg);
            } else {
                arg = arg.replace("\\n", "\n");
                message.append(" ").append(arg);
            }
        }

        if (message.toString().length() <= 1) {
            return false;
        }

        String finalMsg = UCMessages.formatTags("", message.toString().substring(1), src, src, message.toString().substring(1), new UCChannel("broadcast"));

        if (!silent) {
            Sponge.getServer().getConsole().sendMessage(UCUtil.toText("> Broadcast: &r" + finalMsg));
        }

        Builder fanci = Text.builder();
        fanci.append(UCUtil.toText(finalMsg));

        if (hover.toString().length() > 1) {
            fanci.onHover(TextActions.showText(UCUtil.toText(hover.toString().substring(1))));
            if (!silent) {
                Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> OnHover: &r" + hover.toString().substring(1)));
            }
        }

        if (cmdline.toString().length() > 1 && !silent) {
            Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> OnClick: &r" + cmdline.toString().substring(1)));
        }

        if (url.toString().length() > 1) {
            try {
                fanci.onClick(TextActions.openUrl(new URL(url.toString().substring(1))));
            } catch (MalformedURLException ignored) {
            }
            if (!silent) {
                Sponge.getServer().getConsole().sendMessage(UCUtil.toText("&8> Url: &r" + url.toString().substring(1)));
            }
        }

        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            if (cmdline.toString().length() > 1) {
                fanci.onClick(TextActions.runCommand("/" + cmdline.toString().substring(1).replace("{clicked}", p.getName())));
            }
            p.sendMessage(fanci.build());
            if (UChat.get().getJedis() != null) {
                UChat.get().getJedis().sendRawMessage(fanci.build());
            }
        }
        return true;
    }
}

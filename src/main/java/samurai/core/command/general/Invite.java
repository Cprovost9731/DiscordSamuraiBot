package samurai.core.command.general;

import samurai.core.command.Command;
import samurai.core.command.annotations.Key;
import samurai.core.entities.base.FixedMessage;
import samurai.core.entities.base.SamuraiMessage;

/**
 * @author TonTL
 * @version 4.2
 */
@Key("invite")
public class Invite extends Command {

    private static final String INVITE_URL = "https://discordapp.com/oauth2/authorize?client_id=270044218167132170&scope=bot&permissions=126016";

    @Override
    public SamuraiMessage buildMessage() {


        if (args.size() == 1 && (args.get(0).equalsIgnoreCase("plain") || args.get(0).equalsIgnoreCase("noperm"))) {
            return FixedMessage.build(INVITE_URL.substring(0, 78));
        }
        return FixedMessage.build(INVITE_URL);
    }
}

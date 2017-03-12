package samurai.core.command.manage;

import samurai.core.command.Command;
import samurai.core.command.annotations.Admin;
import samurai.core.command.annotations.Key;
import samurai.core.entities.base.FixedMessage;
import samurai.core.entities.base.SamuraiMessage;

/**
 * @author TonTL
 * @version 4.4 - 2/16/2017
 */
@Key("prefix")
@Admin
public class Prefix extends Command {

    @Override
    protected SamuraiMessage buildMessage() {
        if (args.size() != 1 || args.get(0).length() > 8)
            return FixedMessage.build("Invalid Argument. The prefix must be between 1-8 characters in length. Spaces are not allowed.");
        guild.setPrefix(args.get(0));
        return FixedMessage.build(String.format("Prefix successfully set to `%s`", args.get(0)));
    }
}

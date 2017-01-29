package samurai;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

/**
 * Main Class
 * Initializes Samurai bot
 */
public class Bot {

    private static final String BOT_TOKEN = "MjcwMDQ0MjE4MTY3MTMyMTcw.C1yJ0Q.oyQMo7ZGXdaq2K3P43NMwOO8diM";

    public static void main(String[] args) {

        try {
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
            //jdaBuilder.useSharding(0, 1);
            jdaBuilder.addListener(new BotListener()).setToken(BOT_TOKEN).buildBlocking();

        } catch (LoginException | RateLimitedException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
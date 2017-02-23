package samurai;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import samurai.action.admin.Groovy;

import javax.security.auth.login.LoginException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main Class
 * Initializes Samurai bot
 */
public class Bot {

    public static final String AVATAR = "https://cdn.discordapp.com/avatars/270044218167132170/c3b45c87f7b63e7634665a11475beedb.jpg";
    public static final long initializationTime = System.currentTimeMillis();
    public static final AtomicInteger CALLS;
    public static final AtomicInteger SENT;

    static final String SOURCE_GUILD = "233097800722808832";
    static final String BOT_ID = "270044218167132170";
    private static final String TOKEN = "MjcwMDQ0MjE4MTY3MTMyMTcw.C1yJ0Q.oyQMo7ZGXdaq2K3P43NMwOO8diM";


    @SuppressFBWarnings
    public static User self;
    private static TextChannel logChannel;

    static {
        CALLS = new AtomicInteger();
        SENT = new AtomicInteger();
    }

    private static void start() {

        try {
            JDABuilder jdaBuilder = new JDABuilder(AccountType.BOT);
            SamuraiListener listener = new SamuraiListener();
            JDA client = jdaBuilder
                    .addListener(listener)
                    .setToken(TOKEN)
                    .buildBlocking();
            listener.setJDA(client);
            self = client.getSelfUser();
            Groovy.addBinding("client", client);
            logChannel = client.getTextChannelById("281911114265001985");
        } catch (LoginException | RateLimitedException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Bot.start();
    }

    public static void logError(Throwable e) {
        logChannel.sendMessage(new MessageBuilder()
                .append("```\n")
                .append(ExceptionUtils.getMessage(e))
                .append("\n```")
                .build()).queue();
        e.printStackTrace();
    }

    public static void log(String s) {
        logChannel.sendMessage(s).queue();
    }
}

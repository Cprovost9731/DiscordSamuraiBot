package dreadmoirais.samurais.duel;

import dreadmoirais.samurais.BotData;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.List;

/**
 * Created by TonTL on 1/20/2017.
 * Games
 */
public abstract class Game {

    public static User samurai;

    public Message message;

    protected User A, B;
    protected User winner;
    protected User next;

    private BotData.UserData userDataA;
    private BotData.UserData userDataB;

    public Game(User Instigator, User Challenged) {
        A = Instigator;
        B = Challenged;
        winner = null;
    }

    public abstract List<String> getReactions();


    public boolean isPlayer(User player) {
        return player == A || player == B;
    }

    public abstract void perform(int move, User player);

    public MessageBuilder buildTitle() {
        MessageBuilder mb = new MessageBuilder();
        if (next == A) {
            mb.append("***")
                    .append(A.getAsMention())
                    .append("*** vs. ")
                    .append(B.getAsMention())
                    .append("\n");
        } else {
            mb.append(A.getAsMention())
                    .append(" vs. ***")
                    .append(B.getAsMention())
                    .append("***\n");
        }
        return mb;
    }

    public abstract Message buildBoard();

    public abstract boolean hasEnded();

    protected void setWinner(char w) {
        if (w == 'a') {
            winner = A;
            userDataA.incrementStat("Duels Won");
            userDataA.incrementStat("Duels Fought");
            userDataB.incrementStat("Duels Fought");
        } else if (w == 'b') {
            winner = B;
            userDataB.incrementStat("Duels Won");
            userDataB.incrementStat("Duels Fought");
            userDataA.incrementStat("Duels Fought");
        }
    }

    public void setData(HashMap<String, BotData.UserData> users) {
        userDataA = users.get(A.getId());
        userDataB = users.get(B.getId());
    }
}
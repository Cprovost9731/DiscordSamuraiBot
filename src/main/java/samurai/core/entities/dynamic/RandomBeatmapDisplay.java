package samurai.core.entities.dynamic;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import samurai.core.Bot;
import samurai.core.data.SamuraiDatabase;
import samurai.core.entities.base.DynamicMessage;
import samurai.core.events.ReactionEvent;
import samurai.core.events.listeners.ReactionListener;
import samurai.osu.Beatmap;
import samurai.osu.BeatmapSet;
import samurai.osu.Score;
import samurai.osu.enums.Mod;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author TonTL
 * @version 4.x - 2/25/2017
 */
public class RandomBeatmapDisplay extends DynamicMessage implements ReactionListener {

    private static final List<String> REACTIONS = Collections.unmodifiableList(Arrays.asList("🔙", "\uD83D\uDD12", "🛂", "🛃", "📈", "📉", "🔁"));
    private final ArrayList<String> hashArray;
    private final Stack<Integer> history = new Stack<>();
    private final Random r;
    private final int bound;
    private HashMap<String, LinkedList<Score>> scoreMap;
    private int currentIdx;
    private BeatmapSet currentSet;
    private boolean fullScore, fullMap;

    public RandomBeatmapDisplay(HashMap<String, LinkedList<Score>> scoreMap) {
        super();
        this.hashArray = new ArrayList<>(scoreMap.keySet());
        this.scoreMap = scoreMap;
        r = new Random();
        bound = hashArray.size();
        fullMap = false;
        fullScore = false;
        nextSet();
    }

    @Override
    protected void onReady() {
        submitNewMessage("Initializing...", newMenu(REACTIONS).andThen(message -> message.editMessage(getMessage()).queue()));
    }

    @Override
    public void onReaction(ReactionEvent event) {
        if (getMessageId() == 0) return;
        final String name = event.getName();
        if (REACTIONS.contains(name)) {
            execute(name);
            removeReaction(event);
            updateMessage(getMessage());
        }
    }


    public Message getMessage() {
        String hash = hashArray.get(currentIdx);
        Beatmap beatmap;
        if (currentSet.hasCurrent()) beatmap = currentSet.current();
        else beatmap = currentSet.getBeatmapByHash(hash);
        LinkedList<Score> scores = scoreMap.getOrDefault(beatmap.getHash(), new LinkedList<>());
        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setTitle(String.format("%s by %s", currentSet.getSong(), currentSet.getArtist()), null)
                .setColor(Color.PINK)
                .setAuthor("Osu!BeatmapInfo", String.format("https://osu.ppy.sh/b/%s&m=%d", beatmap.getMapID(), beatmap.getGameMode().value()), "http://w.ppy.sh/c/c9/Logo.png")
                .setFooter(String.valueOf(beatmap.getMapID()), "https://cdn.discordapp.com/avatars/270044218167132170/c3b45c87f7b63e7634665a11475beedb.jpg");
        double stars = beatmap.getDifficultyRating();
        StringBuilder diff = new StringBuilder().append(currentSet.getRankedStatus().getEmote());
        diff.append(String.format("[**%s**] ", beatmap.getDifficulty()));
        for (int i = 0; i < (int) stars; i++) diff.append("⭐");
        if (fullMap) {
            diff.append(String.format(" (%.4f) mapped by %s", stars, currentSet.getMapper()));
            embedBuilder.addField("Details", String.format("**AR**: %.2f    **CS**: %.2f    **HP**: %.2f    **OD**: %.2f", beatmap.getAr(), beatmap.getCs(), beatmap.getHp(), beatmap.getOd()), false);
            embedBuilder.addField("Length", String.format("%d:%02d (%d:%02d)", beatmap.getTotalTime() / 60, beatmap.getTotalTime() % 60, beatmap.getDrainTime() / 60, beatmap.getDrainTime() % 60), true);
        }
        embedBuilder.setDescription(diff.toString());
        StringBuilder scoreField = new StringBuilder();
        System.out.println("Scores Found: " + scores.size());
        for (Score score : scores) {
            scoreField.append(String.format("**%15s**  %s  %d   (%.2f%%)%n", score.getPlayer(), score.getGrade().getEmote(), score.getScore(), score.getAccuracy() * 100));
            if (fullScore) {
                scoreField
                        .append(String.format("<:hit_300:273365730047557632>`%d`      <:hit_100:273365765275779072>`%d`      <:hit_50:273365803452334080>`%d`      <:hit_miss:273365818211827714>`%d`%n", score.getCount300(), score.getCount100(), score.getCount50(), score.getCount0()))
                        .append(String.format("%dx/%dx **Mods:**", score.getMaxCombo(), beatmap.getMaxCombo()));
                for (Mod m : Mod.getMods(score.getModCombo())) {
                    scoreField.append(m.toString()).append(" ");
                }
            }
            scoreField.append("\n");
        }
        embedBuilder.addField("Scores", scoreField.toString(), false);
        return new MessageBuilder().setEmbed(embedBuilder.build()).build();
    }

    private void execute(String reactionName) {
        switch (reactionName) {
            case "🔁":
                nextSet();
                break;
            case "🔙":
                if (!history.isEmpty())
                    setCurrentSet(currentIdx = history.pop());
                break;
            case "\uD83D\uDD12":
                clearReactions();
                unregister();
                break;
            case "🛂":
                fullMap = !fullMap;
                break;
            case "🛃":
                fullScore = !fullScore;
                break;
            case "📈":
                currentSet.forward();
                break;
            case "📉":
                currentSet.back();
                break;
            default:
                Bot.log("Illegal BeatmapDisplay Access Error at " + getChannelId() + ":" + getMessageId());
        }
    }

    private void nextSet() {
        history.add(currentIdx);
        currentIdx = r.nextInt(bound);
        setCurrentSet(currentIdx);
    }

    private void setCurrentSet(Integer idx) {
        currentSet = SamuraiDatabase.getSet(hashArray.get(idx));
    }

}

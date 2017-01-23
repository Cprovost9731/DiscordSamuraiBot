package dreadmoirais.samurais;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Created by TonTL on 1/14/2017.
 * Handles events
 * API: http://home.dv8tion.net:8080/job/JDA/Promoted%20Build/javadoc/
 */
public class BotListener extends ListenerAdapter {

    //private static final String BOT_ID = "270044218167132170";//18

    private static User self; //bot user

    private static BotData data; //userData

    private static ArrayList<String> shadePhrases;

    private static Random rand;

    private static List<Game> games;

    private static List<Consumer<MessageReceivedEvent>> commands;
    private static List<String> keys;

    /**
     * constructor
     */
    public BotListener() {
        shadePhrases = new ArrayList<>(); //What shade will the tree provide when you can have Samurai[Bot]

        //Random object for rolls
        rand = new Random();

        games = new ArrayList<>();

        keys = new ArrayList<>();
        commands = new ArrayList<>();

        commands.add(BotListener::getStat);
        keys.add("!stat");
        commands.add(BotListener::getRoll);
        keys.add("!roll");
        commands.add(BotListener::startDuel);
        keys.add("!duel");
        commands.add(BotListener::exitProtocol);
        keys.add("!shutdown");
        commands.add(BotListener::getFlame);
        keys.add("!flame");
        commands.add(BotListener::getFile);
        keys.add("!upload");
    }

    @Override
    public void onReady(ReadyEvent event) {
        //fired when the connection is established

        loadKeywords(); //loads phrases from keywords.txt

        self = event.getJDA().getSelfUser();
        Game.samurai = self;
        data = new BotData(event.getJDA().getGuilds().get(0).getMembers());
    }


    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        System.out.println(event.getReaction().getEmote().getName());
        if (games.size() != 0) {
            updateGames(event);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //fires when a message is sent in the channel
        if (event.isFromType(ChannelType.TEXT)) {
            getCommand(event);
        }
    }

    private void getCommand(MessageReceivedEvent event) {
        String message = event.getMessage().getRawContent().toLowerCase();
        for (int i = 0; i < keys.size(); i++) {
            if (message.contains(keys.get(i)))
                commands.get(i).accept(event);
        }
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        super.onDisconnect(event);
        data.saveDataFull();
    }

    //Basic Commands
    private static void getStat(MessageReceivedEvent event) {
        if (event.getMessage().getMentionedUsers().size() == 0) {
            event.getChannel().sendMessage(data.users.get(event.getAuthor().getId()).buildEmbed()).queue();
        } else {
            for (User u : event.getMessage().getMentionedUsers()) {
                event.getChannel().sendMessage(data.users.get(u.getId()).buildEmbed()).queue();
            }
        }
    }

    private static void getRoll(MessageReceivedEvent event) {
        String message = event.getMessage().getRawContent().toLowerCase();

        int x = 1;
        if (message.length() < 7) {
            x += rand.nextInt(100);
        } else {
            try {
                x += rand.nextInt(Integer.parseInt(message.trim().substring(6)));
            } catch (NumberFormatException e) {
                x += rand.nextInt(100);
                event.getMessage().addReaction("\uD83D\uDE15").queue();
            }
        }
        event.getChannel().sendMessage(
                new MessageBuilder()
                        .append(event.getAuthor().getAsMention())
                        .append(" rolled ")
                        .append(x)
                        .build())
                .queue();

    }

    private static void exitProtocol(MessageReceivedEvent event) {
        event.getMessage().addReaction("\uD83D\uDC4B").queue();
        data.saveDataFull();
        event.getJDA().shutdown();
    }

    //MentionCommands

    private static void getFlame(MessageReceivedEvent event) {
        List<User> victims = new ArrayList<>(event.getMessage().getMentionedUsers());

        if (event.getMessage().mentionsEveryone())
            for (Member m : event.getGuild().getMembers())
                victims.add(m.getUser());
        else
            for (Role r : event.getMessage().getMentionedRoles())
                for (Member m : event.getGuild().getMembers())
                    if (m.getRoles().contains(r))
                        victims.add(m.getUser());


        if (victims.isEmpty()) {
            event.getMessage().addReaction("\uD83D\uDE15").queue();
        } else {
            for (User victim : victims) {
                if (!victim.equals(self)) {
                    MessageBuilder messageBuilder = new MessageBuilder();
                    int x = rand.nextInt(shadePhrases.size());
                    messageBuilder.append(shadePhrases.get(x))
                            .replaceAll("[victim]", victim.getAsMention());
                    if (x == 0) {
                        messageBuilder.append(victim.getName().substring(victim.getName().length() / 2));
                        event.getChannel().sendMessage(messageBuilder.build()).queue();
                    } else if (x == 1) {
                        messageBuilder.setTTS(true);
                        event.getChannel().sendMessage(messageBuilder.build()).queue(message -> message.editMessage(victim.getAsMention()).queue());
                    } else {
                        event.getChannel().sendMessage(messageBuilder.build()).queue();
                    }
                    data.incrementStat(victim.getId(), "Times Flamed");
                }
            }


        }
    }


    /**
     * INCOMPLETE
     */
    private static void getFile(MessageReceivedEvent event) {
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.size() > 0) {
            System.out.println("\nFound Attachment.");
            for (Message.Attachment a : attachments) {
                event.getMessage().addReaction("\u2705");
            }
        } else {
            event.getMessage().addReaction("\uD83D\uDE12");
        }
    }


    private static void startDuel(MessageReceivedEvent event) {
        if (event.getMessage().getMentionedUsers().size() == 1) {
            Game game = new Game(event.getAuthor(), event.getMessage().getMentionedUsers().get(0), rand.nextBoolean());
            game.setData(data.users);
            game.message = event.getChannel().sendMessage(game.buildTitle().build()).complete();
            for (String reaction : Game.connect4Reactions) {
                if (reaction.equals("8\u20e3")) {
                    game.message.addReaction(reaction).queue(success -> game.message.editMessage(game.buildBoard()).queue());
                } else {
                    game.message.addReaction(reaction).queue();
                }
            }
            games.add(game);
        }
    }

    private void updateGames(MessageReactionAddEvent event) {
        int x = Game.connect4Reactions.indexOf(event.getReaction().getEmote().getName());
        if (x != -1) {
            for (Game g : games) {
                if (g.message.getId().equals(event.getMessageId()) && g.isPlayer(event.getUser())) {
                    g.dropTile(x, event.getUser());
                    if (g.hasEnded()) {
                        games.remove(g);
                        for (MessageReaction messageReaction : event.getChannel().getMessageById(event.getMessageId()).complete().getReactions()) {
                            if (Game.connect4Reactions.contains(messageReaction.getEmote().getName())) {
                                messageReaction.removeReaction().queue();
                            }
                        }
                    }
                    g.message.editMessage(g.buildBoard()).queue();
                    event.getReaction().removeReaction(event.getUser()).queue();
                    break;
                }
            }
        }
    }


    //miscMethods
    private void loadKeywords() {
        try (BufferedReader br = new BufferedReader(new FileReader("src\\dreadmoirais\\data\\keywords.txt"))) {
            String line = br.readLine();
            System.out.println("Reading " + line);
            while ((line = br.readLine()) != null) {
                shadePhrases.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

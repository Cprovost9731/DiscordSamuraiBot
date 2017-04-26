package samurai.messages.impl;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import samurai.messages.MessageManager;
import samurai.messages.base.SamuraiMessage;

import java.util.function.Consumer;

/**
 * A messages object that has no further options
 * Created by TonTL on 2/13/2017.
 */
public class FixedMessage extends SamuraiMessage {

    private Message message;
    private Consumer<Message> consumer;


    public static FixedMessage build(String s) {
        if (s == null) return null;
        return new FixedMessage().setMessage(new MessageBuilder().append(s).build());
    }

    public static FixedMessage build(MessageEmbed e) {
        if (e == null) return null;
        return new FixedMessage().setMessage(new MessageBuilder().setEmbed(e).build());
    }

    public Message getMessage() {
        return message;
    }

    public FixedMessage setMessage(Message message) {
        this.message = message;
        return this;
    }

    public Consumer<Message> getConsumer() {
        return consumer;
    }

    public FixedMessage setConsumer(Consumer<Message> consumer) {
        this.consumer = consumer;
        return this;
    }

    @Override
    public void send(MessageManager messageManager) {
        messageManager.getClient().getTextChannelById(String.valueOf(getChannelId())).sendMessage(message).queue(consumer, null);
    }

    @Override
    protected Message initialize() {
        return message;
    }

    @Override
    protected void onReady(Message message) {

    }
}
package samurai.core.events.listeners;

import samurai.core.events.ReactionEvent;

/**
 * Listens for reactions on the message denoted by messageId in the dynamic message
 *
 * @author TonTL
 * @version 4.x - 3/10/2017
 * @see ReactionEvent
 */
public interface ReactionListener {
    void onReaction(ReactionEvent event);
}

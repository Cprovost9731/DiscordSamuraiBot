/*    Copyright 2017 Ton Ly
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package samurai.command.osu;

import net.dv8tion.jda.core.entities.Member;
import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.annotations.Key;
import samurai.database.objects.SamuraiGuild;
import samurai.database.objects.Player;
import samurai.messages.impl.FixedMessage;
import samurai.messages.base.SamuraiMessage;
import samurai.messages.impl.util.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

@Key("rank")
public class Rank extends Command {

    @Override
    public SamuraiMessage execute(CommandContext context) {
        final SamuraiGuild guild = context.getSamuraiGuild();
        final List<Member> mentions = context.getMentionedMembers();
        final List<Player> players = guild.getPlayers();
        if (players.size() == 0) return FixedMessage.build("No users found.");
        Optional<Player> playerOptional;
        if (mentions.size() == 0) {
            playerOptional = guild.getPlayer(context.getAuthorId());
            if (!playerOptional.isPresent())
                return FixedMessage.build("You have not linked an osu account to yourself yet.");
        } else if (mentions.size() == 1) {
            playerOptional = guild.getPlayer(mentions.get(0).getUser().getIdLong());
            if (!playerOptional.isPresent())
                return FixedMessage.build(String.format("**%s** does not have an osu account linked.", mentions.get(0).getEffectiveName()));
        } else {
            return null;
        }
        int listSize = players.size();
        final Player targetPlayer = playerOptional.get();
        int target = guild.getRankLocal(targetPlayer);
        List<String> nameList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            final Player player = players.get(i);
            final String name = context.getGuild().getMemberById(player.getDiscordId()).getEffectiveName();
            final String osuName = player.getOsuName();
            if (i != target) {
                nameList.add(String.format("%d. %s : %s (#%d)%n", i, name, osuName, player.getGlobalRank()));
            } else {
                nameList.add(String.format("#%d %s : %s (#%d)%n", i, name, osuName, player.getGlobalRank()));
            }
        }

        ListIterator<String> itr = nameList.listIterator();
        int pageLen = listSize % 10 >= 5 ? listSize / 10 + 1 : listSize / 10;
        ArrayList<String> book = new ArrayList<>(pageLen);
        for (int i = 0; i < pageLen - 1; i++) {
            StringBuilder sb = new StringBuilder(52 * listSize).append("```md\n");
            int j = 0;
            while (j++ < 10) {
                sb.append(itr.next());
            }
            sb.append("```");
            book.add(sb.toString());
        }
        StringBuilder sb = new StringBuilder(52 * listSize).append("```md\n");
        itr.forEachRemaining(sb::append);
        sb.append("```");
        book.add(sb.toString());


        return new Book(target / 10, book);
    }

}


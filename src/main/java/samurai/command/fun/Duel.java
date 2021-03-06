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
package samurai.command.fun;

import net.dv8tion.jda.core.Permission;
import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.CommandModule;
import samurai.command.annotations.Key;
import samurai.messages.base.SamuraiMessage;
import samurai.messages.impl.PermissionFailureMessage;
import samurai.messages.impl.duel.ConnectFour;

@Key("duel")
public class Duel extends Command {

    private static final Permission[] PERMISSIONS = {Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_MANAGE};

    @Override
    protected SamuraiMessage execute(CommandContext context) {
        if (!context.getSelfMember().hasPermission(context.getChannel(), PERMISSIONS)) {
            return new PermissionFailureMessage(context.getSelfMember(), context.getChannel(), PERMISSIONS);
        }
        if (context.getMentionedMembers().size() != 1)
            return new ConnectFour(context.getAuthor().getUser(), CommandModule.points.isEnabled(context.getSamuraiGuild().getModules()) ? context.getPointTracker() : null);
        else
            return new ConnectFour(context.getAuthor().getUser(), context.getMentionedMembers().get(0).getUser(), CommandModule.points.isEnabled(context.getSamuraiGuild().getModules()) ? context.getPointTracker() : null);
    }
}

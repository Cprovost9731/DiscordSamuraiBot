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
package samurai.command.debug;

import samurai.Bot;
import samurai.command.Command;
import samurai.command.CommandContext;
import samurai.command.annotations.Key;
import samurai.messages.impl.FixedMessage;
import samurai.messages.base.SamuraiMessage;

@Key("uptime")
public class Uptime extends Command {

    @Override
    protected SamuraiMessage execute(CommandContext context) {
        long timeDifference = System.currentTimeMillis() - Bot.START_TIME;
        int seconds = (int) ((timeDifference / 1000) % 60);
        int minutes = (int) ((timeDifference / 60000) % 60);
        int hours = (int) ((timeDifference / 3600000) % 24);
        int days = (int) (timeDifference / 86400000);
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(String.format("%d days, ", days));
        if (hours > 0) sb.append(String.format("%d hours, ", hours));
        if (minutes > 0) sb.append(String.format("%d minutes, ", minutes));
        sb.append(String.format("%d seconds.", seconds));
        return FixedMessage.build(sb.toString());
    }
}


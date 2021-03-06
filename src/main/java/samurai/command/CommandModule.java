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
package samurai.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TonTL
 * @version 5.x - 3/18/2017
 */
public enum CommandModule {
    basic(0L),
    manage(1L),
    general(2L),
    osu(4L),
    music(8L),
    fun(16L),
    points(32L),
    restricted(64L),
    debug(128L);


    private final long value;


    CommandModule(long value) {
        this.value = value;
    }

    public static long getEnabled(CommandModule... enabled) {
        long byteCombo = 0L;
        for (CommandModule cd : enabled) {
            byteCombo |= cd.value;
        }
        return byteCombo;
    }

    public static long getEnabledAll() {
        return getEnabled(CommandModule.values());
    }

    public static List<CommandModule> getVisible() {
        return Arrays.stream(CommandModule.values()).filter(commandModule -> commandModule != restricted).collect(Collectors.toList());
    }

    public static long getDefault() {
        return 0L;
    }

    public long getValue() {
        return value;
    }

    public boolean isEnabled(long byteCombo) {
        return true;
//        return (byteCombo & this.value) == this.value;
    }


}

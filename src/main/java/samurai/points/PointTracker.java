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
package samurai.points;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.user.UserOnlineStatusUpdateEvent;
import samurai.command.CommandModule;
import samurai.database.Database;
import samurai.database.dao.GuildDao;
import samurai.database.objects.SamuraiGuild;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class PointTracker {

    public static final int MESSAGE_POINT = 6;
    public static final int MINUTE_POINT = 1;
    public static final float DUEL_POINT_RATIO = .18f;

    public static final ScheduledExecutorService pool;

    static {
        pool = Executors.newSingleThreadScheduledExecutor();
    }

    private ConcurrentHashMap<Long, ConcurrentHashMap<Long, PointSession>> guildPointMap;

    public void load(ReadyEvent event) {
        final List<Guild> guilds = event.getJDA().getGuilds();
        guildPointMap = new ConcurrentHashMap<>(guilds.size());
        for (Guild guild : guilds) {
            final long guildId = guild.getIdLong();
            Function<GuildDao, SamuraiGuild> guildGet = guildDao -> guildDao.getGuild(guildId);
            final SamuraiGuild samuraiGuild = Database.get().openDao(GuildDao.class, guildGet);
            if (CommandModule.points.isEnabled(samuraiGuild.getModules())) {
                final ConcurrentHashMap<Long, PointSession> sessions = new ConcurrentHashMap<>(guild.getMembers().size());
                guildPointMap.put(guildId, sessions);
                for (Member member : guild.getMembers()) {
                    if (member.getUser().isBot() || member.getUser().isFake()) continue;
                    final OnlineStatus onlineStatus = member.getOnlineStatus();
                    if (onlineStatus != OnlineStatus.UNKNOWN && onlineStatus != OnlineStatus.OFFLINE) {
                        final long memberId = member.getUser().getIdLong();
                        sessions.put(memberId, Database.get().getPointSession(guildId, memberId).setStatus(onlineStatus));
                    }
                }
            }
        }

        pool.scheduleAtFixedRate(this::addPointsToAll, 2, 1, TimeUnit.MINUTES);
    }

    public void onUserOnlineStatusUpdate(UserOnlineStatusUpdateEvent event) {
        if (event.getUser().isBot() || event.getUser().isFake()) return;
        final JDA jda = event.getJDA();
        final User user = event.getUser();
        long userId = user.getIdLong();
        final OnlineStatus onlineStatus = event.getGuild().getMember(user).getOnlineStatus();
        final OnlineStatus previousOnlineStatus = event.getPreviousOnlineStatus();
        long guildId = event.getGuild().getIdLong();
        switch (onlineStatus) {
            case OFFLINE:
            case UNKNOWN: {
                ConcurrentHashMap<Long, PointSession> guildSession = guildPointMap.get(guildId);
                if (guildSession != null) {
                    PointSession pointSession = guildSession.remove(userId);
                    if (pointSession != null) {
                        pointSession.commit();
                    }
                }
            }
            break;
            default: {
                ConcurrentHashMap<Long, PointSession> guildSession = guildPointMap.get(guildId);
                if (guildSession != null) {
                    PointSession pointSession = guildSession.get(userId);
                    if (pointSession != null) {
                        pointSession.setStatus(onlineStatus);
                    } else {
                        guildSession.put(userId, Database.get().getPointSession(guildId, userId));
                    }
                }
            }
        }
    }

    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        ConcurrentHashMap<Long, PointSession> guildSessions = guildPointMap.get(event.getGuild().getIdLong());
        if (guildSessions != null) {
            PointSession pointSession = guildSessions.get(event.getAuthor().getIdLong());
            if (pointSession != null) {
                pointSession.offsetPoints(MESSAGE_POINT);
            }
        }
    }

    private void addPointsToAll() {
        guildPointMap.forEachValue(100L, guildSessions -> guildSessions.forEachValue(100L, pointSession -> pointSession.offsetPoints(MINUTE_POINT)));
    }

    public PointSession getPoints(long guildId, long userId) {
        ConcurrentHashMap<Long, PointSession> guildSession = guildPointMap.get(guildId);
        if (guildSession != null) {
            PointSession pointSession = guildSession.get(userId);
            if (pointSession != null) {
                return pointSession;
            }
        }
        return Database.get().getPointSession(guildId, userId);
    }

    public void offsetPoints(long guildId, long userId, long pointValue) {
        ConcurrentHashMap<Long, PointSession> guildSession = guildPointMap.get(guildId);
        if (guildSession != null) {
            PointSession pointSession = guildSession.get(userId);
            if (pointSession != null) {
                pointSession.offsetPoints(pointValue);
            } else {
                Database.get().getPointSession(guildId, userId).offsetPoints(pointValue).commit();
            }
        }
    }

    public static void close() {
        pool.shutdown();
    }

    public long transferPoints(long guildId, Long fromUserId, Long toUserId, double ratio) {
        long transfer = (long) (getPoints(guildId, fromUserId).points * ratio);
        offsetPoints(guildId, fromUserId, -1 * transfer);
        offsetPoints(guildId, toUserId, transfer);
        return transfer;
    }

    public void shutdown() {
        guildPointMap.forEachValue(100L, guildPoints -> guildPoints.forEachValue(100L, PointSession::commit));
    }
}

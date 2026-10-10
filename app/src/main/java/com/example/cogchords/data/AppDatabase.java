package com.example.cogchords.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cogchords.data.dao.LineupDao;
import com.example.cogchords.data.dao.SongDao;
import com.example.cogchords.data.entity.Lineup;
import com.example.cogchords.data.entity.LineupSong;
import com.example.cogchords.data.entity.Song;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Song.class, Lineup.class, LineupSong.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract SongDao songDao();
    public abstract LineupDao lineupDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "cog_chords_database"
                    )
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                if (INSTANCE != null) {
                                    SongDao dao = INSTANCE.songDao();
                                    dao.insert(new Song(
                                            "Amazing Grace",
                                            "John Newton",
                                            "G",
                                            "Hymn",
                                            "[Verse 1]\nG             C         G\nAmazing grace how sweet the sound\n               D\nThat saved a wretch like me\n   G          C          G\nI once was lost, but now I'm found\n     Em       D      G\nWas blind, but now I see\n\n[Verse 2]\n       G            C        G\n'Twas grace that taught my heart to fear\n               D\nAnd grace my fears relieved\n     G            C       G\nHow precious did that grace appear\n     Em      D       G\nThe hour I first believed"
                                    ));
                                    dao.insert(new Song(
                                            "10,000 Reasons",
                                            "Matt Redman",
                                            "G",
                                            "Worship",
                                            "[Chorus]\n         C           G\nBless the Lord, O my soul\n D/F#     Em\nO my soul\nC            G      Dsus4  D\nWorship His holy name\n          C        Em\nSing like never before\nC   D   Em\nO my soul\n     C             D       G\nI'll worship Your holy name\n\n[Verse 1]\n    C         G           D        Em\nThe sun comes up, it's a new day dawning\nC            G          D     Em\nIt's time to sing Your song again\n     C          G             D             Em\nWhatever may pass and whatever lies before me\nC2           G             Dsus4  D    G\nLet me be singing when the evening comes"
                                    ));
                                }
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}

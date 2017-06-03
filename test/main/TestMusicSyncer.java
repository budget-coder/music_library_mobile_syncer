package main;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import data.DoubleWrapper;

public class TestMusicSyncer {
    private MusicSyncer musicSync;
    private static final String MUSIC_ORI = "D:\\Users\\Aram\\MEGA\\Music\\Music (tags missing or not sorted)\\TEST_ORI";
    private static final String MUSIC_MOD = "D:\\Users\\Aram\\MEGA\\Music\\Music (tags missing or not sorted)\\TEST_MOD";
    
    @Before
    public void setup() {
        musicSync = new MusicSyncer(MUSIC_ORI, MUSIC_MOD);
    }
    
    @Test
    public void shouldCreateSessionFileIfNoneExist() {
        // Get workspace dir where the application is launched.
        File previousSession = new File(System.getProperty("user.dir") + "\\MLMS_LastSession.txt");
        final long currentNanoTime = System.nanoTime();
        // Make a copy of the file, adding the nanotime at the end of the name but before the extension.
        File previousSessionCopy = new File(
                new StringBuilder(previousSession.getName())
                        .insert(previousSession.getName().length() - 4,
                                currentNanoTime)
                        .toString());
        boolean didIBackupSessionFile = false;
        if (previousSession.exists()) {
            // Found existing copy. Make backup!
            assertThat(previousSession.renameTo(previousSessionCopy)).isTrue();
            didIBackupSessionFile = true;
        }
        try {
            musicSync.tryToLoadPreviousSession();
            assertThat(previousSession.exists()).isTrue();
            if (didIBackupSessionFile) {
                // Restore backup by deleting the empty file that was created.
                assertThat(previousSession.delete()).isTrue();
                assertThat(previousSessionCopy.getAbsoluteFile().renameTo(new File(previousSession.getName()))).isTrue();
            }
        } catch (InterruptedException ignore) {}
    }
    
    @Test
    public void shouldLoadPreviousSessionFile() {
        List<DoubleWrapper<String, Long>> previousSession = new ArrayList<>();
        File previousSessionFile = new File(System.getProperty("user.dir") + "\\MLMS_LastSession.txt");
        if (previousSessionFile.exists()) {
            try {
                previousSession = musicSync.tryToLoadPreviousSession();
                if (previousSessionFile.length() > 0) {
                    assertThat(previousSession.isEmpty()).isFalse();
                } else {
                    assertThat(previousSession.isEmpty()).isTrue();
                }
            } catch (InterruptedException ignore) {}
        }
    }
}

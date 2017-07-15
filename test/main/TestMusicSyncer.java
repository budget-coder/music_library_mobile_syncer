package main;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import data.DoubleWrapper;

// How to use the junit listener class (alternative to @BeforeClass and @AfterClass):
// http://memorynotfound.com/add-junit-listener-example/
public class TestMusicSyncer {
    private MusicSyncer musicSync;
    private static final String MUSIC_ORI = "D:\\Users\\Aram\\MEGA\\Music\\Music (tags missing or not sorted)\\TEST_ORI";
    private static final String MUSIC_MOD = "D:\\Users\\Aram\\MEGA\\Music\\Music (tags missing or not sorted)\\TEST_MOD";
    private static final File PREVIOUS_SESSION = new File(System.getProperty("user.dir") + "\\MLMS_LastSession.txt");
    private static File previousSessionCopy;
    private static boolean didIBackupSessionFile = false;

    @BeforeClass
    public static void initialization() {
        final long currentNanoTime = System.nanoTime();
        // Make a copy of the file, adding the nanotime at the end of the name but before the extension .txt which is 4 characters.
        previousSessionCopy = new File(
                new StringBuilder(PREVIOUS_SESSION.getName())
                        .insert(PREVIOUS_SESSION.getName().length() - 4,
                                currentNanoTime)
                        .toString());
        if (PREVIOUS_SESSION.exists()) {
            // Found existing copy. Make backup!
            assertThat(PREVIOUS_SESSION.renameTo(previousSessionCopy)).isTrue();
            didIBackupSessionFile = true;
        }
    }

     @AfterClass
     public static void cleanup() {
         if (didIBackupSessionFile) {
             // Restore backup by deleting the empty file that was created.
             assertThat(PREVIOUS_SESSION.delete()).isTrue();
             assertThat(previousSessionCopy.getAbsoluteFile().renameTo(new File(PREVIOUS_SESSION.getName()))).isTrue();
         }
     }
    
    @Before
    public void setup() {
        musicSync = new MusicSyncer(MUSIC_ORI, MUSIC_MOD);
    }
    
    @Test
    public void shouldCreateSessionFileIfNoneExist() {
        // Get workspace dir where the application is launched.
        File previousSession = new File(System.getProperty("user.dir") + "\\MLMS_LastSession.txt");
        try {
            musicSync.tryToLoadPreviousSession();
            assertThat(previousSession.exists()).isTrue();
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
    
    @Ignore
    @Test
    public void shouldIgnoreNonMusic() {
        //musicSync.updateMetaData(currentSrcFolder, sortedListOfSrc, listOfNewMusic);
    }
}

package main;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final Path ROOT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final String MUSIC_ORI = ROOT_DIR.resolve("test\\music_samples\\source").toString();
    private static final String MUSIC_MOD = ROOT_DIR.resolve("test\\music_samples\\destination").toString();
    private static final File PREVIOUS_SESSION = ROOT_DIR.resolve("MLMS_LastSession.txt").toFile();
    private static File previousSessionCopy;
    private static boolean didIBackupSessionFile = false;

    @BeforeClass
    public static void initialization() {
        if (PREVIOUS_SESSION.exists()) {
            // Found existing copy. Make backup by adding the nanotime at the
            // end of the name but before the extension .txt which is 4
            // characters.
            final long currentNanoTime = System.nanoTime();
            previousSessionCopy = new File(
                    new StringBuilder(PREVIOUS_SESSION.getName())
                            .insert(PREVIOUS_SESSION.getName().length() - 4,
                                    currentNanoTime)
                            .toString());
            try {
                Files.copy(PREVIOUS_SESSION.toPath(), previousSessionCopy.toPath());
            } catch (IOException e) {
                System.err.println("FATAL: Could not backup the previous session file by making a copy"
                        + "with the current nanotime appended to it. Exitting...");
            }
            // assertThat(PREVIOUS_SESSION.renameTo(previousSessionCopy)).isTrue();
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
        if (PREVIOUS_SESSION.exists()) {
            assertThat(PREVIOUS_SESSION.delete()).isTrue();
        }
        try {
            musicSync.tryToLoadPreviousSession();
            assertThat(PREVIOUS_SESSION.exists()).isTrue();
        } catch (InterruptedException ignore) {}
    }
    
    @Test
    public void shouldLoadPreviousSessionFile() {
        List<DoubleWrapper<String, Long>> previousSessionList = new ArrayList<>();
        if (PREVIOUS_SESSION.exists()) {
            try {
                previousSessionList = musicSync.tryToLoadPreviousSession();
                if (PREVIOUS_SESSION.length() > 0) {
                    assertThat(previousSessionList.isEmpty()).isFalse();
                } else {
                    assertThat(previousSessionList.isEmpty()).isTrue();
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

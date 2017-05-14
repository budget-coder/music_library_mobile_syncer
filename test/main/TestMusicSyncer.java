package main;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestMusicSyncer {
    private MusicSyncer musicSync;
    private final String MUSIC_ORI = "D:\\Users\\Aram\\MEGA\\Music\\Music (tags missing or not sorted)\\TEST_ORI";
    private final String MUSIC_MOD = "D:\\Users\\Aram\\MEGA\\Music\\Music (tags missing or not sorted)\\TEST_MOD";
    
    @Before
    public void setup() {
        musicSync = new MusicSyncer(MUSIC_ORI, MUSIC_MOD);
    }
    
    @Test
    public void shouldCreateSessionFileIfNoneExist() {
        File previousSession = new File(System.getProperty("user.dir") + "\\MLMS_LastSession.txt");
        assertThat(!previousSession.exists()).isTrue();
        try {
            musicSync.tryToLoadPreviousSession();
            assertThat(previousSession.exists()).isTrue();
        } catch (InterruptedException ignore) {}
    }
    
    @Ignore
    @Test
    public void shouldLoadPreviousSessionFile() {
//        List<DoubleWrapper<String,Long>> previousSession = musicSync.tryToLoadPreviousSession();
//        assertThat(previousSession.size()).isEqualTo(3);
    }
}

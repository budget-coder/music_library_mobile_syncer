package metadata;
import java.io.File;
import java.util.Random;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.mp4.Mp4Tag;

public class RandomMetadataUpdater {
    public static void main(String args[]) {
        File fileFolderSrc = new File(args[0].replace('\\', '/'));
        if (fileFolderSrc.isDirectory()) {
            Random random = new Random();
            for (final File fileEntry : fileFolderSrc.listFiles()) {
                int randomInt = random.nextInt(5);
                if (randomInt != 2) {
                    continue;
                }
                // Get file name and extension, if any.
                final String strFile = fileEntry.getName();
                final int fileExtIndex = strFile.lastIndexOf("."); // If there is no extension, this will default to -1.
                final String strExt = strFile.substring(fileExtIndex + 1);
                try {
                    switch (strExt.toUpperCase()) {
                    // Exploit the concept of fallthrough.
                    case "MP3":
                        MP3File mp3FileDst = (MP3File) AudioFileIO.read(fileEntry);
                        AbstractID3v2Tag v2TagDst = mp3FileDst.getID3v2Tag();
                        v2TagDst.setField(FieldKey.ALBUM, "MODIFIED");
                        mp3FileDst.setTag(v2TagDst);
                        mp3FileDst.commit();
                        break;
                    case "M4A":
                        AudioFile mp4AudioFileDst = AudioFileIO.read(fileEntry);
                        Mp4Tag mp4FileDst = (Mp4Tag) mp4AudioFileDst.getTag();
                        mp4FileDst.setField(FieldKey.ALBUM, "MODIFIED");
                        mp4AudioFileDst.commit();
                        break;
                    default:
                        continue; // "These are not the files you are looking for."
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

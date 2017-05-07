import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.mp4.Mp4Tag;

public class MusicSyncer {    
    private String dstFolder;
    private String srcFolder;
    private boolean optionAddNewMusic;
    private boolean optionDeleteOrphanedMusic;
    private boolean optionSearchInSubdirectories;
    private SimpleAttributeSet attr;
    /**
     * We want to make a list of keys to avoid duplication and reduce the
     * likelihood of the programmer forgetting to check for a key. This list
     * will NOT be able to be modified after creation as this will add bugs.
     */
    private final List<String> listOfMP3Keys;
    /**
     * {@link #listOfMP3Keys}
     */
    private final List<FieldKey> listOfMP4Keys;
    
    
    public MusicSyncer(String srcFolder, String dstFolder) {
        this.srcFolder = srcFolder;
        this.dstFolder = dstFolder;
        // Options are false by default.
        optionAddNewMusic = false;
        optionDeleteOrphanedMusic = false;
        optionSearchInSubdirectories = false;
        
        attr = new SimpleAttributeSet();
        /*
         * TODO The documentation is shite; unless there is a method for
         * determining which ID3 tag is used, then use the ID3vxxFrames class
         * that you have the, ugh, most of. For instance, most of my music is
         * ID3v23.
         */
        listOfMP3Keys = Collections.unmodifiableList(Arrays.asList(
                ID3v23Frames.FRAME_ID_V3_TITLE, ID3v24Frames.FRAME_ID_ARTIST,
                ID3v23Frames.FRAME_ID_V3_ACCOMPANIMENT,
                ID3v23Frames.FRAME_ID_V3_ALBUM, ID3v23Frames.FRAME_ID_V3_TYER,
                ID3v23Frames.FRAME_ID_V3_TRACK, ID3v23Frames.FRAME_ID_V3_SET,
                ID3v23Frames.FRAME_ID_V3_GENRE, ID3v23Frames.FRAME_ID_V3_COMPOSER));
        listOfMP4Keys = Collections.unmodifiableList(Arrays.asList(
                FieldKey.TITLE, FieldKey.ARTIST,
                FieldKey.ALBUM_ARTIST,
                FieldKey.ALBUM, FieldKey.YEAR,
                FieldKey.TRACK, FieldKey.DISC_NO,
                FieldKey.GENRE, FieldKey.COMPOSER));
    }
    
    public void initiate() throws InterruptedException {
        File fileFolderSrc = new File(srcFolder.replace('\\', '/'));
        File fileFolderDst = new File(dstFolder.replace("\\", "/"));
        final boolean doesSrcAndDstExist = (fileFolderSrc.exists()
                && fileFolderSrc.isDirectory())
                || (fileFolderDst.exists() && fileFolderDst.isDirectory());
        if (!doesSrcAndDstExist) {
            StyleConstants.setForeground(attr, Color.RED);
            UI.writeStatusMessage("ERROR: The source/target folder is not a folder or does not exist.", attr);
        } else {
            // Reuse the variable
            listFilesOfFolder(fileFolderSrc, fileFolderDst);
        }
    }
    
    public void setAddNewMusicOption(boolean option) {
        optionAddNewMusic = option;
    }
    
    public void setDeleteOrphanedMusic(boolean option) {
        optionDeleteOrphanedMusic = option;
    }
    
    public void setSearchInSubdirectories(boolean option) {
        optionSearchInSubdirectories = option;
    }

    private boolean listFilesOfFolder(final File folderSrc, final File folderDst) {
        // Note that we want to locally scope variables as much as possible:
        // http://stackoverflow.com/questions/8803674/declaring-variables-inside-or-outside-of-a-loop/8878071#8878071
        boolean didAllGoWell = true;
        File[] listOfSrc = folderSrc.listFiles();
        File[] listOfDst = folderDst.listFiles();
        List<File> sortedListOfSrc = new ArrayList<>();
        String currentSession = "";
        List<DoubleWrapper<String, Long>> lastSession = tryToLoadPreviousSession();
        StyleConstants.setForeground(attr, Color.BLACK);
        UI.writeStatusMessage("List of src and dst folders completed. Checking for differences...", attr);

        // The file-search algorithm
        // TODO Explain the extra index
        int lastSessionIndex = 0;
        for (int i = 0; i < listOfSrc.length; i++) {
            final File fileEntry = listOfSrc[i];
            boolean isCurrentSessionUpdated = false;

            if (optionSearchInSubdirectories && fileEntry.isDirectory()) {
                // If a folder was found, and the user wants it, then search it.
                // TODO Will not be tested with the current directory
                System.out.println("Recursing through folder; THIS SHOULD NOT HAPPEN (FOR NOW)");
                listFilesOfFolder(fileEntry, folderDst);
            } else {
                // Get file name and extension, if any.
                final String strFile = fileEntry.getName();
                final int fileExtIndex = strFile.lastIndexOf("."); // If there is no extension, this will default to -1.
                final String strExt = strFile.substring(fileExtIndex + 1);
                final long fileLastMod = fileEntry.lastModified();
                // Try to locate the file in the previous session instead of checking all the metadata.
                if (lastSession.size() >= lastSessionIndex) {
                    final boolean isSameFile = lastSession.get(lastSessionIndex).getArg1().equals(strFile);
                    final boolean hasBeenModified = lastSession.get(lastSessionIndex).getArg2() != fileLastMod;
                    // We assume that, for the most part, most music is unchanged from last session.
                    if (!isSameFile) {
                        // Although it is not the same file, we have to make
                        // absolutely sure that it is not further down the last
                        // session file.
                        boolean wasFileLocated = tryToLocateFileInPreviouSession(lastSession, lastSessionIndex, strFile);
                        if (!wasFileLocated || !hasBeenModified) {
                            continue;
                        }
                    } else {
                        currentSession += strFile + "\n";
                        currentSession += fileLastMod + "\n";
                        if (!hasBeenModified) {
                            continue;
                        }
                        isCurrentSessionUpdated = true;
                    }
                }
                // To make it case-insensitive (and avoid problems with
                // Turkish), convert to uppercase.
                switch (strExt.toUpperCase()) {
                // Exploit the concept of fallthrough.
                case "MP3":
                case "M4A":
                    if (!isCurrentSessionUpdated) {
                        currentSession += strFile + "\n";
                        currentSession += fileLastMod + "\n";
                    }
                    sortedListOfSrc.add(fileEntry);
                    lastSessionIndex++;
                    break;
                default:
                    continue; // "These are not the files you are looking for."
                }
                // Copy new music to dst if the option was checked.
                // TODO This should be the last operation in the whole program because it adds unnecessary comparisons (at minimum n-checks!).
                if (optionAddNewMusic) {
                    checkAndAddNewMusicToDst(fileEntry, folderSrc, folderDst);
                }
                continue;
            }
        }
        // When all is finished and done, save the list of music to a .txt file (will overwrite existing).
        try {
            Files.write(Paths.get("MLMS_LastSession.txt"), Arrays.asList(currentSession), Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("ERROR: Could not save a list of the music to a .txt file!");
        }
        
        StyleConstants.setForeground(attr, Color.BLACK);
        UI.writeStatusMessage("Done searching through the src folder. Checking for differences...", attr);
        
        for (final File fileEntryDst : listOfDst) {
            final String strFile = fileEntryDst.getName();
            final int index = strFile.lastIndexOf("."); // If there is no extension, this will default to -1.
            final String strExt = strFile.substring(index + 1);
            boolean isTheMusicOrphaned = true;
            boolean isItMusic = true;
            
            try {
                switch (strExt.toUpperCase()) {
                case "MP3":
                    // Only start comparing metadata if both dir have the file.
                    // TODO This is ugly and slow as fuck
                    for (final File fileEntrySrc : listOfSrc) {
                        if (!(fileEntrySrc.getName().equals(fileEntryDst.getName()))) { // Do nothing because file was not found.
                            continue;
                        }
                        isTheMusicOrphaned = false;
                        // TODO DELETE THIS CHECK WHEN THE LASTMODIFIED TXT WORKS.
                        if (fileEntrySrc.lastModified() >= fileEntryDst.lastModified()) {
                            // Only update metadata if the user updated the src file.
                            updateMP3MetaData(fileEntrySrc, fileEntryDst);
                        }
                        break;
                    }
                    break;
                case "M4A":
                    // M4A are structurally the same as MP4 files.
                    for (final File fileEntrySrc : listOfSrc) {
                        if (!(fileEntrySrc.getName().equals(fileEntryDst.getName()))) { // Do nothing because file was not found.
                            continue;
                        }
                        isTheMusicOrphaned = false;
                        if (fileEntrySrc.lastModified() >= fileEntryDst.lastModified()) {
                            updateM4AMetaData(fileEntrySrc, fileEntryDst);
                        }
                        break;
                    }
                    break;
                default:
                    isItMusic = false;
                    break;
                }
                // Delete orphaned music in dst if the option was checked.
                if (optionDeleteOrphanedMusic && isItMusic && isTheMusicOrphaned) {
                    if (tryToDeleteOrphanedMusicInDst(fileEntryDst)) {
                        StyleConstants.setForeground(attr, Color.ORANGE);
                        UI.writeStatusMessage("Deleted " + fileEntryDst.getName(), attr);
                    } else {
                        StyleConstants.setForeground(attr, Color.RED);
                        UI.writeStatusMessage("Could not delete " + fileEntryDst.getName() + ".", attr);
                    }
                }
            } catch (InvalidAudioFrameException | CannotReadException
                    | IOException | TagException
                    | ReadOnlyFileException | CannotWriteException e) {
                // TODO Use a logger just like in hotciv/cave
                System.err.println("FATAL: " + e.toString());
            }
        }
        
        return didAllGoWell;
    }

    private boolean tryToLocateFileInPreviouSession(
            List<DoubleWrapper<String, Long>> lastSession, int lastSessionIndex,
            String strFile) {
        // TODO Ya better add a description of this fuckery. :D
        boolean isOutOfBound = lastSession.size() < lastSessionIndex || lastSessionIndex < 0;
        boolean isPreceedingCurrentFile = false;
        boolean isFollowingCurrentFile = false;
        boolean wasFileLocated = false;
        do {
            int currentFileComparedToPreviousVersion = lastSession.get(lastSessionIndex).getArg1().compareTo(strFile); 
            if (currentFileComparedToPreviousVersion < 0) {
                // We are too low behind in our session list.
                if (isFollowingCurrentFile) {
                    // The previous session did not contain the
                    // current file; in the previous iteration, the
                    // index was decremented.
                    break;
                }
                isPreceedingCurrentFile = true;
                lastSessionIndex++;
            } else if (currentFileComparedToPreviousVersion > 0) {
                // We are too far ahead in our session list.
                if (isPreceedingCurrentFile) {
                    // Same conclusion for the opposite reason; previously, the index was incremented. 
                    break;
                }
                isFollowingCurrentFile = true;
                lastSessionIndex--;
            } else {
                // Hoozah! We found the file.
                wasFileLocated = true;
            }
            isOutOfBound = lastSession.size() < lastSessionIndex || lastSessionIndex < 0;
        } while (!isOutOfBound && !wasFileLocated);
        return wasFileLocated;
    }

    /**
     * For optimization, this method should only be called when the MP3 file has
     * been modified!
     * 
     * @param fileSrc
     * @param fileDst
     * @throws CannotReadException
     * @throws IOException
     * @throws TagException
     * @throws ReadOnlyFileException
     * @throws InvalidAudioFrameException
     * @throws CannotWriteException
     */
    private void updateMP3MetaData(File fileSrc, File fileDst)
            throws CannotReadException, IOException, TagException,
            ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
        // Here we need to make a wrapper class because we need the tag field
        // when updating music.
        List<DoubleWrapper<String, String>> listOfTagsSrc = new ArrayList<>();
        List<DoubleWrapper<FieldKey, String>> listOfTagsDst = new ArrayList<>();
        // Read metadata from the files.
        AbstractID3v2Tag v2TagSrc = ((MP3File) AudioFileIO.read(fileSrc)).getID3v2Tag();
        MP3File mp3FileDst = (MP3File) AudioFileIO.read(fileDst);
        AbstractID3v2Tag v2TagDst = mp3FileDst.getID3v2Tag();
        
        // Get each relevant tag from src and dst version of the file and save
        // them in lists for later comparisons.
        for (int i = 0; i < listOfMP3Keys.size(); i++) {
            String key = listOfMP3Keys.get(i);
            FieldKey fieldKey = listOfMP4Keys.get(i); // TODO This is dangerous
                                                      // as it assumes that both
                                                      // lists are in the same
                                                      // order. Okay, this is
                                                      // really dangerous.
            listOfTagsSrc.add(new DoubleWrapper<>(key, v2TagSrc.getFirst(key)));
            //System.out.println("THE KEY IS " + v2TagDst.getValue(fieldKey, 0));
            // TODO For some reason, it fucks up on the genre (for instance, (24) = soundtrack...)
            // TODO It does not seem to be able to handle YEARs with less than 4 digits well.
            listOfTagsDst.add(new DoubleWrapper<>(fieldKey, v2TagDst.getFirst(key)));
        }
        // Now for the comparisons.
        DoubleWrapper<FieldKey, String> keyTagFile;
        for (int i = 0; i < listOfTagsSrc.size(); i++) {
            keyTagFile = listOfTagsDst.get(i);
            if (!listOfTagsSrc.get(i).getArg2().equals(keyTagFile.getArg2())) {
                // Update metadata of the target music file.
                System.out.println("Replacing tag " + listOfTagsDst.get(i).getArg2() + " with " + listOfTagsSrc.get(i).getArg2() + " with field " + keyTagFile.getArg1());
                v2TagDst.setField(keyTagFile.getArg1(), listOfTagsSrc.get(i).getArg2());
                mp3FileDst.setTag(v2TagDst);
                mp3FileDst.commit();
            }
        }
    }
    
    // TODO This feels a lot like duplicate code but how should it be improved?
    private void updateM4AMetaData(File fileSrc, File fileDst)
            throws CannotReadException, IOException, TagException,
            ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
        List<DoubleWrapper<FieldKey, String>> listOfTagsSrc = new ArrayList<>();
        List<DoubleWrapper<FieldKey, String>> listOfTagsDst = new ArrayList<>();
        // Read metadata from the files.
        Mp4Tag mp4FileSrc = (Mp4Tag) AudioFileIO.read(fileSrc).getTag();
        AudioFile mp4AudioFileDst = AudioFileIO.read(fileDst);
        Mp4Tag mp4FileDst = (Mp4Tag) mp4AudioFileDst.getTag();
        
        // Get each relevant tag from src and dst version of the file and save
        // them in lists for later comparisons.
        for (FieldKey key : listOfMP4Keys) {
            listOfTagsSrc.add(new DoubleWrapper<>(key, mp4FileSrc.getFirst(key)));
            listOfTagsDst.add(new DoubleWrapper<>(key, mp4FileDst.getFirst(key)));
        }
        // Now for the comparisons.
        DoubleWrapper<FieldKey, String> keyTagFile;
        for (int i = 0; i < listOfTagsSrc.size(); i++) {
            keyTagFile = listOfTagsDst.get(i);
            if (!listOfTagsSrc.get(i).getArg2().equals(keyTagFile.getArg2())) {
                // Update metadata of the target music file.
                mp4FileDst.setField(keyTagFile.getArg1(), listOfTagsSrc.get(i).getArg2());
                mp4AudioFileDst.commit();
            }
        }
    }
    
    private void checkAndAddNewMusicToDst(final File fileEntry, final File folderSrc,
            final File folderDst) {
        // Filter for searching for a specific music file.
        // TODO This runs n^2 times.......
        final String strFile = fileEntry.getName();
        final FilenameFilter musicFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.equals(strFile);
            }
        };
        if (folderDst.list(musicFilter).length <= 0) {
            try {
                // Convert dst folder's path correctly.
                Path targetPath = folderDst.toPath()
                        .resolve(folderSrc.toPath()
                                .relativize(fileEntry.toPath()));
                Files.copy(fileEntry.toPath(), targetPath);
                StyleConstants.setForeground(attr, Color.GREEN);
                UI.writeStatusMessage("Added " + fileEntry.getName() + ".", attr);
            } catch (IOException e) {
                StyleConstants.setForeground(attr, Color.RED);
                UI.writeStatusMessage("FATAL: Could not copy " + fileEntry.getName() + " to destination.", attr);
            }
        }
    }
    
    private boolean tryToDeleteOrphanedMusicInDst(File fileEntryDst) {
        // TODO Per the documentation, this can throw an IOException if the operation was unsuccessful. 
        return fileEntryDst.delete();
    }
    
    /**
     * ...
     * The music contained in the .txt file should be sorted in an
     * alphabetic order. Use this fact to search through this list and the
     * current list of music (i.e. the src folder) at the same time. TODO Do
     * this in parallel with the actual tagging. Something along the lines of a
     * queue which amasses a list of music to be modified.
     * 
     * @return 
     */
    private List<DoubleWrapper<String, Long>> tryToLoadPreviousSession() {
        File lastSession = new File("MLMS_LastSession.txt");
        // If the file does not exist, then create it for the future syncing.
        if (!lastSession.exists()) {
            try {
                lastSession.createNewFile();
            } catch (IOException e) {
                System.out.println("FATAL: Could not create a file to store the current list of music in!");
            }
        }
        List<DoubleWrapper<String, Long>> lastSessionList = new ArrayList<>();  
        // Try-with-ressources to ensure that the stream is closed.
        try (BufferedReader br = new BufferedReader(new FileReader(lastSession))) {
            // Syntax: name of file on the first line and its last modified date on the next line.
            String line = br.readLine();
            while (line != null) {
                String name = line;
                long lastMod = Long.parseLong(br.readLine());
                lastSessionList.add(new DoubleWrapper<>(name, lastMod));
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, Color.BLUE);
            UI.writeStatusMessage("No last sync session was found.", attr);
        } catch (IOException e) {
            System.err.println("Error when loading last sync session: " + e.getMessage());
        }
        return lastSessionList;
    }
}

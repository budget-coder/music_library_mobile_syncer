import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.jaudiotagger.tag.reference.GenreTypes;

public class MusicSyncer {    
    private long TEMP_LAST_MODIFIED = Long.MIN_VALUE; // TODO Get this from the previous list, if any.
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
                FieldKey.TITLE, FieldKey.ARTIST, FieldKey.ALBUM_ARTIST,
                FieldKey.ALBUM, FieldKey.YEAR, FieldKey.TRACK, FieldKey.DISC_NO,
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
        List<File> listOfSrc = new ArrayList<>();
        File[] listOfDst = folderDst.listFiles();
        StyleConstants.setForeground(attr, Color.BLACK);
        UI.writeStatusMessage("List of src and dst folders completed. Checking for differences...", attr);

        // The file-search algorithm
        for (final File fileEntry : folderSrc.listFiles()) {
            if (optionSearchInSubdirectories && fileEntry.isDirectory()) {
                // If a folder was found, and the user wants it, then search it.
                // TODO Will not be tested with the current directory
                System.out.println("Recursing through folder; THIS SHOULD NOT HAPPEN (FOR NOW)");
                listFilesOfFolder(fileEntry, folderDst);
            } else {
                // Get file name and extension, if any.
                final String strFile = fileEntry.getName();
                final int index = strFile.lastIndexOf("."); // If there is no extension, this will default to -1.
                final String strExt = strFile.substring(index + 1);
                /* TODO For optimization when a previous .txt library file is used.
                if (!shouldFileBeAddedToList(fileEntry.lastModified(), index, strExt)) {
                    // This file has not been modified, has no filetype, or has an incompatible extension.
                    continue;
                }
                */
                // To make it case-insensitive (and avoid problems with
                // Turkish), convert to uppercase.
                switch (strExt.toUpperCase()) {
                // Exploit the concept of fallthrough.
                case "MP3":
                case "M4A":
                    listOfSrc.add(fileEntry);
                    break;
                default:
                    // "These are not the files you are looking for."
                    continue;
                }
                // Copy new music to dst if the option was checked.
                
                if (optionAddNewMusic) {
                    addNewMusicToDst(fileEntry, folderSrc, folderDst);
                }
            }
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
                System.err.println("FATAL: " + e.toString());
            }
        }
        
        // TODO If everything worked out, i.e. ask user, save a .txt file
        // Path pathTolibraryTxt = Paths.get(".").toAbsolutePath().normalize(); // Get dir of application.
        
        /*
        // When all is finished and done, save the list of music to a .txt file (will overwrite existing).
        try {
            Files.write(Paths.get("MLMS.txt"), listStrings, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("ERROR: Could not save a list of the music to a .txt file!");
        }
        */
        return didAllGoWell;
    }

    /* TODO Use this method, specifically the part with the lastModified date,
     * when comparing with an existing .txt file. 
    private static boolean shouldFileBeAddedToList(long lastModified, int index, String strExt) {
        boolean returnCode;
        if (lastModified <= TEMP_LAST_MODIFIED || index < 0) {
            returnCode = false;
        } else {
            returnCode = true;
        }
        return returnCode;
    }
    */

    /**
     * For optimization, this method should only be called when the MP3 file has been modified!
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
        List<KeyTagDouble<String>> listOfTagsSrc = new ArrayList<>();
        List<KeyTagDouble<FieldKey>> listOfTagsDst = new ArrayList<>();
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
            listOfTagsSrc.add(new KeyTagDouble<String>(key, v2TagSrc.getFirst(key)));
            //System.out.println("THE KEY IS " + v2TagDst.getValue(fieldKey, 0));
            // TODO For some reason, it fucks up on the genre (for instance, (24) = soundtrack...)
            // TODO It does not seem to be able to handle YEARs with less than 4 digits well.
            listOfTagsDst.add(new KeyTagDouble<FieldKey>(fieldKey, v2TagDst.getFirst(key)));
        }
        // Now for the comparisons.
        KeyTagDouble<FieldKey> keyTagFile;
        for (int i = 0; i < listOfTagsSrc.size(); i++) {
            keyTagFile = listOfTagsDst.get(i);
            if (!listOfTagsSrc.get(i).getTag().equals(keyTagFile.getTag())) {
                // Update metadata of the target music file.
                System.out.println("Replacing tag " + listOfTagsDst.get(i).getTag() + " with " + listOfTagsSrc.get(i).getTag() + " with field " + keyTagFile.getKey());
                v2TagDst.setField(keyTagFile.getKey(), listOfTagsSrc.get(i).getTag());
                mp3FileDst.setTag(v2TagDst);
                mp3FileDst.commit();
            }
        }
    }
    
    // TODO This feels a lot like duplicate code but how should it be improved?
    private void updateM4AMetaData(File fileSrc, File fileDst)
            throws CannotReadException, IOException, TagException,
            ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
        List<KeyTagDouble<FieldKey>> listOfTagsSrc = new ArrayList<>();
        List<KeyTagDouble<FieldKey>> listOfTagsDst = new ArrayList<>();
        // Read metadata from the files.
        Mp4Tag mp4FileSrc = (Mp4Tag) AudioFileIO.read(fileSrc).getTag();
        AudioFile mp4AudioFileDst = AudioFileIO.read(fileDst);
        Mp4Tag mp4FileDst = (Mp4Tag) mp4AudioFileDst.getTag();
        
        // Get each relevant tag from src and dst version of the file and save
        // them in lists for later comparisons.
        for (FieldKey key : listOfMP4Keys) {
            listOfTagsSrc.add(new KeyTagDouble<FieldKey>(key, mp4FileSrc.getFirst(key)));
            listOfTagsDst.add(new KeyTagDouble<FieldKey>(key, mp4FileDst.getFirst(key)));
        }
        // Now for the comparisons.
        KeyTagDouble<FieldKey> keyTagFile;
        for (int i = 0; i < listOfTagsSrc.size(); i++) {
            keyTagFile = listOfTagsDst.get(i);
            if (!listOfTagsSrc.get(i).getTag().equals(keyTagFile.getTag())) {
                // Update metadata of the target music file.
                mp4FileDst.setField(keyTagFile.getKey(), listOfTagsSrc.get(i).getTag());
                mp4AudioFileDst.commit();
            }
        }
    }
    
    private void addNewMusicToDst(final File fileEntry, final File folderSrc,
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
}

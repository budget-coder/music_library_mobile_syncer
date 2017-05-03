import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;

public class MusicSyncer {    
    private long TEMP_LAST_MODIFIED = Long.MIN_VALUE; // TODO Get this from the previous list, if any.
    private String dstFolder;
    private String srcFolder;
    private boolean optionAddNewMusic;
    private boolean optionDeleteOrphanedMusic;
    private boolean optionRENAMEME;
    
    private SimpleAttributeSet attr;
    
    public MusicSyncer(String srcFolder, String dstFolder) {
        this.srcFolder = srcFolder;
        this.dstFolder = dstFolder;
        // Options are false by default.
        optionAddNewMusic = false;
        optionDeleteOrphanedMusic = false;
        optionRENAMEME = false;
        
        attr = new SimpleAttributeSet();
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
    
    public void setRENAMEME(boolean option) {
        optionRENAMEME = option;
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
            if (fileEntry.isDirectory()) {
                // If a folder was found, search it.
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
                        if (fileEntrySrc.getName().equals(fileEntryDst.getName())) {
                            updateMP3MetaData(fileEntrySrc, fileEntryDst);
                            isTheMusicOrphaned = false;
                            break;
                        }
                    }
                    break;
                case "M4A":
                    // M4A are structurally the same as MP4 files.
                    System.out.println("Found m4a file");
                    for (final File fileEntrySrc : listOfSrc) {
                        if (fileEntrySrc.getName().equals(fileEntryDst.getName())) {
                            updateM4AMetaData(fileEntrySrc, fileEntryDst);
                            isTheMusicOrphaned = false;
                            break;
                        } 
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
        MP3File mp3FileSrc = (MP3File) AudioFileIO.read(fileSrc);
        AbstractID3v2Tag v2TagSrc = mp3FileSrc.getID3v2Tag();
        List<String> listOfTagsSrc = new ArrayList<>();
        // NOTE: Both lists must contain each tag in the same order!
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_TITLE));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_ARTIST));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_ACCOMPANIMENT));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_ALBUM));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_YEAR));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_TRACK));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_SET));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_GENRE));
        listOfTagsSrc.add(v2TagSrc.getFirst(ID3v24Frames.FRAME_ID_COMPOSER));
        
        MP3File mp3FileDst = (MP3File) AudioFileIO.read(fileDst);
        AbstractID3v2Tag v2TagDst = mp3FileDst.getID3v2Tag();
        // Here we need to make a wrapper class because we need the tag field
        // when updating music.
        List<KeyTagDouble> listOfTagsDst = new ArrayList<>();
        listOfTagsDst.add(new KeyTagDouble(FieldKey.TITLE, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_TITLE)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.ARTIST, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_ARTIST)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.ALBUM_ARTIST, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_ACCOMPANIMENT)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.ALBUM, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_ALBUM)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.YEAR, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_YEAR)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.TRACK, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_TRACK)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.DISC_NO, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_SET)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.GENRE, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_GENRE)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.COMPOSER, v2TagDst.getFirst(ID3v24Frames.FRAME_ID_COMPOSER)));
        // Now for the comparisons.
        KeyTagDouble keyTagFile;
        for (int i = 0; i < listOfTagsSrc.size(); i++) {
            keyTagFile = listOfTagsDst.get(i);
            if (!listOfTagsSrc.get(i).equals(keyTagFile.getTag())) {
                // Update metadata of the target music file.
                v2TagDst.setField(keyTagFile.getKey(), listOfTagsSrc.get(i));
                mp3FileDst.commit();
            }
        }
    }
    
    // TODO This feels a lot like duplicate code but how should it be improved?
    private void updateM4AMetaData(File fileSrc, File fileDst)
            throws CannotReadException, IOException, TagException,
            ReadOnlyFileException, InvalidAudioFrameException, CannotWriteException {
        System.out.println("Updating M4A file");
        Mp4Tag mp4FileSrc = (Mp4Tag) AudioFileIO.read(fileSrc).getTag();
        List<String> listOfTagsSrc = new ArrayList<>();
        // TODO How can you do this better? This is error prone because both
        // lists have to have the tags in the same order, and no tags must be
        // forgotten...
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.TITLE));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.ARTIST));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.ALBUM_ARTIST));
        System.out.println(mp4FileSrc.getFirst(Mp4FieldKey.ALBUM_ARTIST));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.ALBUM));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.MM_ORIGINAL_YEAR));
        System.out.println(mp4FileSrc.getFirst(Mp4FieldKey.MM_ORIGINAL_YEAR));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.TRACK));
        System.out.println(mp4FileSrc.getFirst(Mp4FieldKey.TRACK));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.DISCNUMBER));
        System.out.println(mp4FileSrc.getFirst(Mp4FieldKey.DISCNUMBER));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.GENRE));
        listOfTagsSrc.add(mp4FileSrc.getFirst(Mp4FieldKey.COMPOSER));
        
        AudioFile mp4AudioFileDst = AudioFileIO.read(fileDst);
        Mp4Tag mp4FileDst = (Mp4Tag) mp4AudioFileDst.getTag();
        // Like in the method "updateMP3MetaData(File, File)", we need a wrapper
        // class when updating music.
        List<KeyTagDouble> listOfTagsDst = new ArrayList<>();
        listOfTagsDst.add(new KeyTagDouble(FieldKey.TITLE, mp4FileDst.getFirst(Mp4FieldKey.TITLE)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.ARTIST, mp4FileDst.getFirst(Mp4FieldKey.ARTIST)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.ALBUM_ARTIST, mp4FileDst.getFirst(Mp4FieldKey.ALBUM_ARTIST)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.ALBUM, mp4FileDst.getFirst(Mp4FieldKey.ALBUM)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.YEAR, mp4FileDst.getFirst(Mp4FieldKey.MM_ORIGINAL_YEAR)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.TRACK, mp4FileDst.getFirst(Mp4FieldKey.TRACK)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.DISC_NO, mp4FileDst.getFirst(Mp4FieldKey.DISCNUMBER)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.GENRE, mp4FileDst.getFirst(Mp4FieldKey.GENRE)));
        listOfTagsDst.add(new KeyTagDouble(FieldKey.COMPOSER, mp4FileDst.getFirst(Mp4FieldKey.COMPOSER)));
        // Now for the comparisons.
        KeyTagDouble keyTagFile;
        for (int i = 0; i < listOfTagsSrc.size(); i++) {
            keyTagFile = listOfTagsDst.get(i);
            if (!listOfTagsSrc.get(i).equals(keyTagFile.getTag())) {
                // Update metadata of the target music file.
                mp4FileDst.setField(keyTagFile.getKey(), listOfTagsSrc.get(i));
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
                System.out.println("Comparing " + name + " to " + strFile);
                return name.equals(strFile);
            }
        };
        if (folderDst.list(musicFilter).length <= 0) {
            try {
                System.out.println("Copying file to dst...");
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

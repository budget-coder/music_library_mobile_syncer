package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

import data.DataClass;
import data.DoubleWrapper;
import filesystem.MTPDeviceStrategy;
import filesystem.PCDeviceStrategy;
import framework.DeviceStrategy;
import framework.FileWrapper;
import framework.StateDeviceStrategy;

public class MusicSyncer {
    private final FileWrapper srcFolder;
    private final FileWrapper dstFolder;
    private boolean optionAddNewMusic;
    private boolean optionDeleteOrphanedMusic;
    private boolean optionSearchInSubdirectories;
    private final SimpleAttributeSet attr;
    private final DeviceStrategy srcStrategy;
    private final DeviceStrategy dstStrategy;
    private final StateDeviceStrategy stateDeviceStrategy;
    /**
     * We want to make a list of keys to avoid duplication and reduce the
     * likelihood of the programmer forgetting to check for a key. This list
     * will NOT be able to be modified after creation as this will add bugs.
     */
    // TODO DISC_NO disabled until it can be looked up on an MTP device
    private final List<FieldKey> listOfFieldKeys = Collections.unmodifiableList(Arrays.asList(
            FieldKey.TITLE, FieldKey.ARTIST,
            FieldKey.ALBUM_ARTIST,
            FieldKey.ALBUM, FieldKey.YEAR,
            FieldKey.TRACK, //FieldKey.DISC_NO,
            FieldKey.GENRE, FieldKey.COMPOSER));
	
    // TODO Temporary measure for getting artwork on PC. On mtp, this is used to
	// ignore artwork because I do not know how to get artwork from an mtp device
	// (unless completely assumed to be android...)
	private boolean isSrcDevice;
	private boolean isDstDevice;
    
    public MusicSyncer(String srcFolderStr, String dstFolderStr, boolean isSrcDevice, boolean isDstDevice) {
    	srcFolderStr = srcFolderStr.replace('\\', '/');
    	dstFolderStr = dstFolderStr.replace('\\', '/');
    	this.isSrcDevice = isSrcDevice;
    	this.isDstDevice = isDstDevice;
    	srcStrategy = (isSrcDevice ? new MTPDeviceStrategy(srcFolderStr) : new PCDeviceStrategy(srcFolderStr));
    	dstStrategy = (isDstDevice ? new MTPDeviceStrategy(dstFolderStr) : new PCDeviceStrategy(dstFolderStr));
    	// TODO Statedevicestrat is ONLY used to determine whether to use mtp strat. or pc strat when copying...
		stateDeviceStrategy = new SwitchBetweenDevicesStrategy(srcStrategy, dstStrategy, isSrcDevice);
    	//deviceStrategy.setToPCOrDevice(false); // Default value.
    	srcFolder = srcStrategy.getFolder();
    	dstFolder = dstStrategy.getFolder();
        // Options are false by default.
        optionAddNewMusic = false;
        optionDeleteOrphanedMusic = false;
        optionSearchInSubdirectories = false;
        optionSearchInSubdirectories = false;
        attr = new SimpleAttributeSet();
    }
    
    /**
     * This method is used to start the whole syncing process. It consists of
     * three parts: building a list of modified music, determining what metadata
     * was changed, and making the appropriate updates.
     * 
     * @throws InterruptedException
     */
    public void initiate() throws InterruptedException {
        if (srcFolder.isDirectory() && dstFolder.isDirectory()) {
			DoubleWrapper<List<FileWrapper>, List<FileWrapper>> tuppleModifiedNewMusic = buildMusicListToSync(srcFolder);
            updateMetaData(tuppleModifiedNewMusic.getArg1(), tuppleModifiedNewMusic.getArg2());
            addNewMusicList(tuppleModifiedNewMusic.getArg2());
        } else {
            StyleConstants.setForeground(attr, DataClass.ERROR_COLOR);
            UI.writeStatusMsg("ERROR: The source/target folder is not a folder or does not exist.", attr);
        }
    }

    /**
	 * This method builds a list of music which have been modified since last sync
	 * session. Note that this means if the program cannot find a previous session
	 * file, <b>ALL</b> music will be marked as modified until the metadata is
	 * closely examined.
	 * 
	 * @param currentSrcFolder
	 *            - the current source folder. Used to handle nested folders.
	 * @return a tuple of lists containing 1) a list of modified music and 2) a list
	 *         of new music.
	 * @throws InterruptedException
	 */
    public DoubleWrapper<List<FileWrapper>,List<FileWrapper>> buildMusicListToSync(FileWrapper currentSrcFolder)
            throws InterruptedException {
    	final FileWrapper[] listOfSrc = srcFolder.listFiles();
    	final FileWrapper[] listOfDst = dstFolder.listFiles();
        // TODO This call here is useless when it comes to nested folders...
        UI.setMaximumLimitOnProgressBar((listOfSrc.length + listOfDst.length));
        final List<FileWrapper> sortedListOfSrc = new ArrayList<>(); // A list with only the modified music.
        final List<FileWrapper> listOfNewMusic = new ArrayList<>(); // A list with only the music to be added.
        final StringBuilder currentSession = new StringBuilder();
        final List<DoubleWrapper<String, Integer>> lastSession = tryToLoadPreviousSession();
        
        StyleConstants.setForeground(attr, DataClass.INFO_COLOR);
        UI.writeStatusMsg("List of src and dst folders completed.", attr);
        // Before we begin, we might want to check if there is any orphaned
        // music which we can get rid of to avoid extra comparisons.
        if (optionDeleteOrphanedMusic) {
            UI.writeStatusMsg("Locating orphaned music...", attr);
            final String folderSrcPath = currentSrcFolder.getAbsolutePath();
            lookForAndDeleteOrphanedMusicInDst(listOfDst, folderSrcPath);
        }
        StyleConstants.setForeground(attr, DataClass.INFO_COLOR);
        UI.writeStatusMsg("Finding files in src which have been updated since last session...", attr);
        /*
         * We use a separate index for the last session file because we cannot
         * guarantee that the amount of music in the source folder equals the
         * amount described in the file.
         */
        int lastSessionIndex = 0;
        for (int i = 0; i < listOfSrc.length; i++) { // The algorithm
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            final FileWrapper fileEntrySrc = listOfSrc[i];
            if (optionSearchInSubdirectories && fileEntrySrc.isDirectory()) {
                // If a folder was found, and the user wants it, then search it.
                // TODO Will not be tested with the current directory
                System.err.println("Recursing through folder; THIS SHOULD NOT HAPPEN (FOR NOW)");
                throw new UnsupportedOperationException(getClass().getName() + ": Recursing subdirectories in music folder not implemented yet");
                /*
                FileWrapper currentSrcFolderCopy = currentSrcFolder; // Take a backup of the current location
                currentSrcFolder = fileEntrySrc;
                buildMusicListToSync(currentSrcFolder); // Recurse the subdirectory
                currentSrcFolder = currentSrcFolderCopy; // Restore the original folder.
                continue;
                */
            }
            // Get file name and extension, if any.
            final String strFile = fileEntrySrc.getName();
            final int fileExtIndex = strFile.lastIndexOf("."); // If no extension, this will default to -1.
            final String strExt = strFile.substring(fileExtIndex + 1); 
            // We use MurmurHash3 on the file itself to get a unique hash to compare with.
            /* TODO Somehow find a way to create a unique value for files on PC AND MOTHERFUCKING MTP. Fuck jmtp...
            byte[] fileEntrySrcBytes = {0};
            try {
                fileEntrySrcBytes = Files.readAllBytes(fileEntrySrc.toPath());
            } catch (IOException e) { // Will occur if the synchronization is stopped abruptly. 
                System.err.println("FATAL: Could not read bytes of " + strFile + " for hashing.");
                e.printStackTrace();
            }
            final int fileHash = MurmurHash3.murmurhash3_x86_32(fileEntrySrcBytes, 0, strFile.length(), 14);
            */
            final int fileHash = 0;
            //final long fileLastMod = fileEntrySrc.lastModified();
            // To make it case-insensitive (and avoid problems with Turkish), convert to uppercase.
            switch (strExt.toUpperCase()) {
            // Exploit the concept of fallthrough.
            case "MP3":
            case "M4A":
                // Try to locate the file in the previous session instead of checking all the metadata.
                updateCurrentSession(currentSession, strFile, fileHash);
                boolean hasBeenModified = true;
                boolean wasFileLocated = false;
                if (lastSessionIndex < lastSession.size()) {
                    final boolean isSameFile = lastSession.get(lastSessionIndex).getArg1().equals(strFile);
                    // We assume that, for the most part, most music is unchanged from last session.
                    if (!isSameFile) {
						// Although it is not the same file, we have to make absolutely sure that it is
						// not further down (or up) the last session file.
                        DoubleWrapper<Boolean, Integer> locateFileWrapper =
                                tryToLocateFileInPreviouSession(lastSession, lastSessionIndex, strFile);
                        wasFileLocated = locateFileWrapper.getArg1();
                        hasBeenModified = locateFileWrapper.getArg2() != fileHash;
                    } else {
                    	hasBeenModified = lastSession.get(lastSessionIndex).getArg2() != fileHash;
                        wasFileLocated = true;
                    }
                    if (wasFileLocated) {
						// If and only if the file was located, then we can move on to the next file
						// entry. This is to avoid skipping a file and prematurely finish the last
						// session.
                        lastSessionIndex++;
                    }
                }
                // Check if the music exists in dst.
                
                
                
                ///////////////////////////////////////////////////////////////////////////////
				// TODO until hashing or something similar can be implemented for mtp files
				// (hashing requires a byte array representing the MTPFile...), hasBeenModified
				// will always be true.
                ///////////////////////////////////////////////////////////////////////////////
                hasBeenModified = true;
                
                
                
                FileWrapper fileInDst = dstStrategy.getFileInstance(dstFolder.getAbsolutePath() + "\\" + strFile);
                if (wasFileLocated && !hasBeenModified) {
                    UI.updateProgressBar(2);
                    continue;
                } else if (optionAddNewMusic && !fileInDst.doesFileExist()) {
					// If the option was checked, "mark" new music by adding them to a list whose
					// contents will be added later. This should be the last operation in the whole
					// program because it adds unnecessary comparisons (at minimum n-checks!).
                    listOfNewMusic.add(fileEntrySrc);
                } else if (fileInDst.doesFileExist()){
                    sortedListOfSrc.add(fileEntrySrc);
                } else {
                	UI.updateProgressBar(2); // File was not found on dst and the user does not want to add it.
                }
                break;
            default:
            	UI.updateProgressBar(1);
                break; // "These are not the files you are looking for."
            }
        }
        // When all is finished and done, save the list of music to a .txt file (will overwrite existing).
        try {
            Paths.get("MLMS_LastSession.txt").toFile().setWritable(true);
            Files.write(Paths.get("MLMS_LastSession.txt"),
                        Arrays.asList(currentSession.toString()), Charset.forName("UTF-8"));
            Paths.get("MLMS_LastSession.txt").toFile().setWritable(false);
        } catch (IOException e) {
            System.err.println("FATAL: Could not save a list of the music to a .txt file!");
        }
        return new DoubleWrapper<List<FileWrapper>, List<FileWrapper>>(sortedListOfSrc, listOfNewMusic);
    }
    
    /**
     * This method inspects the list of music given and examines the metadata,
     * updating it a change is detected.
     * @param sortedListOfSrc
     *            - a list of modified music to be examined.
     * @param listOfNewMusic
     *            - a list of new music to be directly copied from src to dst.
     * 
     * @throws InterruptedException
     */
    public void updateMetaData(List<FileWrapper> sortedListOfSrc, List<FileWrapper> listOfNewMusic)
            throws InterruptedException {
        StyleConstants.setForeground(attr, DataClass.INFO_COLOR);
        UI.writeStatusMsg("Updating metadata...", attr);
        
        for (final FileWrapper fileSrc : sortedListOfSrc) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            // Create a FileWrapper of the file at destination.
            FileWrapper fileDst = dstStrategy.getFileInstance(dstFolder.getAbsolutePath() + "\\" + fileSrc.getName());
            
			// Before getting every relevant metadata, we check the length of both music
			// files. If the mod. version is not the same, then the music data has been
			// modified. The only fix is to replace and return.
            if (!fileSrc.getDuration().equals(fileDst.getDuration())) {
            	listOfNewMusic.add(fileSrc);
            }
            else {
            	// Get each relevant tag from src and dst version of the file and save
                // them in lists for later comparisons.
            	for (FieldKey fieldKey : listOfFieldKeys) {
            		final String tagValueSrc = fileSrc.getTagData(fieldKey);
                	final String tagValueDst = fileDst.getTagData(fieldKey);
					if (!tagValueSrc.equals(DataClass.ERROR_STRING) && !tagValueDst.equals(DataClass.ERROR_STRING)
							&& !tagValueSrc.equals(tagValueDst)) {
                		fileDst.changeTag(fieldKey, tagValueSrc);
                	}
            	}
				// Artworks, however, are a special case. Notice that we are only interested in
				// the first artwork as the others are assumed to be mistakes since they are not
				// shown when the music is played.
            	
            	// TODO This check is very, very necessary as I can only
            	// acquire artwork if the music is on the pc. If it's on an MTP
            	// device, then I have yet to find a method on how to get it.
            	// jmtp is not documented at all...
            	if (!isSrcDevice && !isDstDevice) {
	                final Artwork srcArtwork = fileSrc.getAlbumArt();
	                final Artwork dstArtwork = fileDst.getAlbumArt();
	                if (srcArtwork != null && dstArtwork == null) {
	                	fileDst.changeAlbumArt(srcArtwork);
	                } else if (srcArtwork == null && dstArtwork != null) {
	                	fileDst.changeAlbumArt(null); // Delete artwork
	                } else if (srcArtwork != null && dstArtwork != null) {
	                    // Only get the first artwork.
	                    byte[] srcArtworkArr = srcArtwork.getBinaryData();
	                    byte[] dstArtworkArr = dstArtwork.getBinaryData();
	                    if (!Arrays.equals(srcArtworkArr, dstArtworkArr)) {
	                    	fileDst.changeAlbumArt(null); // Delete dst artwork first
	                    	fileDst.changeAlbumArt(srcArtwork); // Add new artwork from src.
	                    }
	                }
            	}
            	fileDst.applyTagChanges();
            }
            UI.updateProgressBar(2);
        }
    }

    /**
	 * Locate the current file in the previous session file.
	 * 
	 * @param lastSession
	 *            - our last session file where each entry contains a name and a
	 *            last modified.
	 * @param lastSessionIndex
	 *            - the current index describing our location in src.
	 * @param strFile
	 *            - the name of the current file.
	 * @return a tuple containing a boolean for whether the file was found and its
	 *         last modified date. If it was not found, then the date is 0.
	 */
    public DoubleWrapper<Boolean, Integer> tryToLocateFileInPreviouSession(
            List<DoubleWrapper<String, Integer>> lastSession, int lastSessionIndex,
            String strFile) {
        boolean isOutOfBound = lastSession.size() < lastSessionIndex || lastSessionIndex < 0;
        boolean isPreceedingCurrentFile = false;
        boolean isFollowingCurrentFile = false;
        boolean wasFileLocated = false;
        do {
			int currentFileComparedToPreviousVersion = lastSession.get(lastSessionIndex).getArg1().compareTo(strFile);
            if (currentFileComparedToPreviousVersion < 0) {
                // We are too low behind in our session list.
                if (isFollowingCurrentFile) {
					// The previous session did not contain the current file; in the previous
					// iteration, the index was decremented.
                    break;
                }
                isPreceedingCurrentFile = true;
                lastSessionIndex++;
            } else if (currentFileComparedToPreviousVersion > 0) {
                // We are too far ahead in our session list.
                if (isPreceedingCurrentFile) {
                    break; // Same conclusion for the opposite reason; before, the index was incremented.
                }
                isFollowingCurrentFile = true;
                lastSessionIndex--;
            } else {
                wasFileLocated = true; // Hoozah! We found the file.
            }
            isOutOfBound = lastSessionIndex >= lastSession.size() || lastSessionIndex < 0;
        } while (!isOutOfBound && !wasFileLocated);
        if (!isOutOfBound) {
            return new DoubleWrapper<Boolean, Integer>(wasFileLocated, lastSession.get(lastSessionIndex).getArg2());
        }
        return new DoubleWrapper<Boolean, Integer>(wasFileLocated, 0);
    }
    
	/**
	 * This method copies new music to {@link #dstFolder}.
	 * 
	 * @param listOfNewMusic
	 *            - a list of new music to be added directly.
	 * @throws InterruptedException
	 */
    public void addNewMusicList(List<FileWrapper> listOfNewMusic) throws InterruptedException {
        StyleConstants.setForeground(attr, DataClass.NEW_MUSIC_COLOR);
        for (final FileWrapper newMusic : listOfNewMusic) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            String strFile = newMusic.getName();
            try {
                stateDeviceStrategy.copyMusicToDst(newMusic);
                UI.writeStatusMsg("Added " + strFile + ".", attr);
            } catch (IOException e) {
                StyleConstants.setForeground(attr, DataClass.ERROR_COLOR);
                UI.writeStatusMsg("FATAL: Could not copy " + strFile + " to destination.", attr);
                e.printStackTrace();
            }
            UI.updateProgressBar(2);
        }
    }
    
    /**
     * Delete orphaned music in the specified folder if the option was checked.
     * This should be the first operation of the program. Otherwise, it adds
     * more comparisons and bugs out the sorted list.
     * 
     * @param listOfFolder
     *            - a list of files in the specified folder
     * @param pathToFolder
     *            - the path to the folder.
     * @throws InterruptedException 
     */
    public void lookForAndDeleteOrphanedMusicInDst(FileWrapper[] listOfFolder, String pathToFolder) throws InterruptedException {
        for (final FileWrapper fileEntryDst : listOfFolder) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            // Check if it's music first. If it is not, then skip it.
            final String strFile = fileEntryDst.getName();
            final int fileExtIndex = strFile.lastIndexOf("."); // If no extension, this will default to -1.
            final String strExt = strFile.substring(fileExtIndex + 1); 
            switch (strExt.toUpperCase()) {
            // Exploit the concept of fallthrough.
            case "MP3":
            case "M4A":
				FileWrapper fileOnSrc = srcStrategy.getFileInstance(pathToFolder + "\\" + fileEntryDst.getName());
	            if (fileOnSrc.doesFileExist()) {
	                continue;
	            }
	            if (fileEntryDst.deleteFile()) {
	                StyleConstants.setForeground(attr, DataClass.DEL_MUSIC_COLOR);
	                UI.writeStatusMsg("Deleted " + fileEntryDst.getName(), attr);
	                UI.updateProgressBar(1);
	            } else {
	                StyleConstants.setForeground(attr, DataClass.ERROR_COLOR);
	                UI.writeStatusMsg("Could not delete " + fileEntryDst.getName() + ".", attr);
	            }
            default:
            	UI.updateProgressBar(1); // This was not a music file. Just skip it.
            	break;
            }
        }
    }
    
    /**
     * Load and return the contents of the previous session file, if available.
     * The music contained in the .txt file should be sorted in an alphabetic
     * order. Use this fact to search through this list and the current list of
     * music (i.e. the src folder) at the same time.
     * 
     * @return a list containing the previous session. Otherwise, it is empty.
     * @throws InterruptedException
     */
    public List<DoubleWrapper<String, Integer>> tryToLoadPreviousSession() throws InterruptedException {
        File lastSession = new File("MLMS_LastSession.txt");
        // If the file does not exist, then create it for the future syncing.
        if (!lastSession.exists()) {
            try {
                lastSession.createNewFile();
            } catch (IOException e) {
                StyleConstants.setForeground(attr, DataClass.ERROR_COLOR);
                UI.writeStatusMsg("FATAL: Could not create a file to store the current list of music in!", attr);
            }
        }
        List<DoubleWrapper<String, Integer>> lastSessionList = new ArrayList<>();
        // Try-with-ressources to ensure that the stream is closed. Notice that
        // we do not just make a new instance of FileReader because it uses
        // Java's platform default encoding, and that is not always correct!
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(lastSession), Charset.forName("UTF-8")))) {
            // Syntax: name of file on the first line and its last modified date on the next line.
            String line = br.readLine();
            while (line != null) {
                if (Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                String name = line;
                line = br.readLine();
                // To avoid NumberFormatException on empty lines.
                if (line != null) {
                    int lastMod = Integer.parseInt(line);
                    lastSessionList.add(new DoubleWrapper<>(name, lastMod));
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, DataClass.INFO_COLOR);
            UI.writeStatusMsg("No last sync session was found.", attr);
        } catch (IOException e) {
            System.err.println("Error when loading last sync session: " + e.getMessage());
        }
        return lastSessionList;
    }
    
    /**
     * Update the current session file with new entries. We use a StringBuilder
     * to pass the reference to the string by value and thus update the session
     * file correctly.
     * 
     * @param currentSession
     *            - A StringBuilder representation of the current session file.
     * @param strFile
     *            - the current file.
     * @param fileLastMod
     *            - the last modified date of the file.
     */
    private void updateCurrentSession(StringBuilder currentSession, String strFile, long fileLastMod) {
        currentSession.append(strFile + "\n");
        currentSession.append(fileLastMod + "\n");
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
}

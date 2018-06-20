package filesystem;

import java.io.File;
import java.io.IOException;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Frames;
import org.jaudiotagger.tag.images.Artwork;

import data.DataClass;
import framework.FileWrapper;

public class PCFile implements FileWrapper {
	private final File file;
	private final String strExt;
	private AudioFile audioFile;
	private Tag musicTag;
	private boolean isAudioDataInitialized = false;
	private ID3v23Frames id3v23Frame;
	
	public PCFile(String pathToFile) {
		file = new File(pathToFile);
		final int index = pathToFile.lastIndexOf("."); // If there is no extension, this will default to -1.
		strExt = pathToFile.substring(index + 1);		
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public String getName() {
		return file.getName();
	}
	
	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public boolean deleteFile() {
		return file.delete();
	}

//	@Override
//	public int getUniqueHash() throws IOException {
//		byte[] fileEntrySrcBytes = Files.readAllBytes(file.toPath());
//        // We use MurmurHash3 on the file itself to get a unique hash to compare with.
//        return MurmurHash3.murmurhash3_x86_32(fileEntrySrcBytes, 0, file.getName().length(), 14); 
//	}

	@Override
	public boolean doesFileExist() {
		return file.exists();
	}

	@Override
	public FileWrapper[] listFiles() {
		File[] tempFileList = file.listFiles();
		FileWrapper[] fileList = new FileWrapper[tempFileList.length];
		for (int i = 0; i < tempFileList.length; ++i) {
			fileList[i] = new PCFile(tempFileList[i].getAbsolutePath());
		}
		return fileList;
	}
	
	/**
	 * Initializes the tag data of the file, assuming it is an audio file, and if it
	 * hasn't been done before (i.e. the method is called on this file for the first
	 * time). If it has been done before, then it returns (true) immediately.
	 * 
	 * @return true if everything went well; false otherwise.
	 * @throws InterruptedException 
	 */
	private boolean initializeMusicTagIfNecessary() throws InterruptedException {
		if (!isAudioDataInitialized) {
			try {
				audioFile = AudioFileIO.read(file);
				if (strExt.equals("MP3")) {
					musicTag = ((MP3File) audioFile).getID3v2Tag();
				} else {
					musicTag = AudioFileIO.read(file).getTag();
				}
				id3v23Frame = ID3v23Frames.getInstanceOf();
			} catch (CannotReadException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				// TODO Use a logger just like in hotciv/cave
                System.err.println("FATAL: " + e.getMessage() + System.lineSeparator() + "Type of exception: " + e.getClass().getName());
                return false;
			} catch (IOException e) {
				if (audioFile == null) {
					// Something seriously went wrong. It is likely because the user interrupted the
					// read() operation.
					throw new InterruptedException();
				}
			}
			isAudioDataInitialized = true;
		}
		return true;
	}
	
	@Override
	public String getDuration() throws InterruptedException {
		if (initializeMusicTagIfNecessary()) {
			return musicTag.getFirst(ID3v23Frames.FRAME_ID_V3_LENGTH);
		}
		return DataClass.ERROR_STRING;
	}
	
	@Override
	public Artwork getAlbumArt() throws InterruptedException {
		if (initializeMusicTagIfNecessary()) {
			return musicTag.getFirstArtwork();
		}
		return null; // TODO Replace with Null Pattern 
	}
	
	@Override
	public void changeAlbumArt(Artwork newArt) {
		if (newArt == null) {
			musicTag.deleteArtworkField();
		} else {
			try {
				musicTag.setField(newArt);
			} catch (FieldDataInvalidException e) {
				System.err.println("FATAL: " + e.getMessage());
			}
		}
	}

	@Override
	public String getTagData(FieldKey fieldKey) throws InterruptedException {
		if (!initializeMusicTagIfNecessary()) {
			return DataClass.ERROR_STRING;
		}
		if (strExt.equals("MP3")) {
			/* TODO bug report to Paul Taylor, author of jaudiotagger
             * getFirst(FieldKey key) does NOT give the right year; it
             * should be "TYER" and not "TDRC". We get "TYER" by getting it
             * from the frame ID3v23Frames.FRAME_ID_V3_TYER and this is done
             * as follows.
             */
			return musicTag.getFirst(id3v23Frame.getId3KeyFromGenericKey(fieldKey).getFieldName());
		}
		return musicTag.getFirst(fieldKey);
	}

	@Override
	public void changeTag(FieldKey fieldKey, String tagValueSrc) {
		try {
			musicTag.setField(fieldKey, tagValueSrc);
		} catch (KeyNotFoundException | FieldDataInvalidException e) {
			System.err.println("FATAL: " + e.getMessage());
		}
	}

	@Override
	public void applyTagChanges() {
		audioFile.setTag(musicTag);
		try {
			audioFile.commit();
		} catch (CannotWriteException e) {
			System.err.println("FATAL: Cannot write tag changes to " + getName()
					+ System.lineSeparator() + "Original message: " + e.getMessage());
		}
	}

}

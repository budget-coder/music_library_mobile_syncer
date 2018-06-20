package filesystem;

import java.time.LocalDate;
import java.time.ZoneId;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

import data.DataClass;
import framework.FileWrapper;
import jmtp.PortableDeviceAudioObject;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;

public class MTPFile implements FileWrapper {
	private final PortableDeviceObject file;
	private final PortableDeviceAudioObject audioFile;
	
	/**
	 * Constructs a valid MTPFile for a valid storage and file path.
	 * 
	 * @param storage
	 *            - the storage object. If no storage is known at the time, then
	 *            passing "null" will construct an invalid MTPFile. This means that
	 *            all methods will do nothing or return default values (such as the
	 *            empty string).
	 * @param pathToFile
	 *            - the path to the folder or file.
	 * 
	 */
	public MTPFile(PortableDeviceStorageObject storage, String pathToFile) {
		// TODO Improve with Null Pattern for storage (!!!that and folderObject in MTPUtil!!!)
		if (storage != null) {
			file = MTPUtil.getChildFileByNameRecursively(storage, pathToFile);
		} else {
			file = new NullPortableDeviceObject();
		}
		if (file instanceof PortableDeviceAudioObject) {
			audioFile = (PortableDeviceAudioObject) file;
		} else {
			// TODO improve with null pattern here as well
			audioFile = null;
		}
	}
	
	/**
	 * Constructs a valid MTPFile for a valid folder and file path. Behaves
	 * similarly to the referred constructor.
	 * 
	 * @see #MTPFile(PortableDeviceStorageObject, String)
	 */
	public MTPFile(PortableDeviceFolderObject parentFolder, String pathToFile) {
		// TODO Improve with Null Pattern for storage (!!!that and folderObject in MTPUtil!!!)
		if (parentFolder == null || parentFolder instanceof NullPortableDeviceFolderObject) {
			file = new NullPortableDeviceObject();
		} else {
			file = MTPUtil.getChildFileByNameRecursively(parentFolder, pathToFile);
		}
		if (file instanceof PortableDeviceAudioObject) {
			audioFile = (PortableDeviceAudioObject) file;
		} else {
			audioFile = null;
		}
	}

	@Override
	public boolean isDirectory() {
		if (file instanceof PortableDeviceFolderObject) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public boolean deleteFile() {
		if (file.canDelete()) {
			file.delete();
			return true;
		}
		return false;
	}

//	@Override
//	public int getUniqueHash() throws IOException {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public boolean doesFileExist() {
		return file != null ? true : false;
	}

	@Override
	public String getAbsolutePath() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(getClass().getName() + " : Not implemented yet.");
	}

	@Override
	public FileWrapper[] listFiles() {
		// TODO Auto-generated method stub. RETURN NULL JUST LIKE java.io.File if path is not directory.
		throw new UnsupportedOperationException(getClass().getName() + " : Not implemented yet.");
		/*
		PortableDeviceObject[] deviceObjList = ((PortableDeviceFolderObject) file).getChildObjects();
		FileWrapper[] fileList = new FileWrapper[deviceObjList.length];
		for (int i = 0; i < deviceObjList.length; ++i) {
			// TODO Implement ths correctly using the code from TestEnvironment, line 61
			//fileList[i] = new MTPFile(deviceObjList[i].)
		}
		return fileList;
		*/
	}

	@Override
	public String getDuration() {
		return audioFile.getDuration().toString();
	}

	@Override
	public String getTagData(FieldKey fieldKey) {
		switch (fieldKey) {
		case TITLE:
			return audioFile.getTitle();
		case ARTIST:
			return audioFile.getArtist();
		case ALBUM_ARTIST:
			return audioFile.getAlbumArtist();
		case ALBUM:
			return audioFile.getAlbum();
		case YEAR:
			LocalDate localDate = audioFile.getReleaseDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			return localDate.getYear() - 1900 + "";
		case COMPOSER:
			return audioFile.getComposer();
		case DISC_NO:
			System.err.println("FATAL: Attempting to acquire disc_no from mtp when NOT IMPLEMENTED YET");
			return DataClass.ERROR_STRING;
		case GENRE:
			return audioFile.getGenre();
		case TRACK:
			return audioFile.getTrackNumber() + "";
		default:
			System.err.println("FATAL: Unknown tag " + fieldKey + " from mtp is requested");
			return DataClass.ERROR_STRING;
		}
	}

	@Override
	public void changeTag(FieldKey fieldKey, String tagValueSrc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void applyTagChanges() { // Do nothing when using the jmtp library.
	}

	@Override
	public Artwork getAlbumArt() {
		throw new UnsupportedOperationException(getClass().getName() + ": Not implemented yet");
	}

	@Override
	public void changeAlbumArt(Artwork newArt) {
		throw new UnsupportedOperationException(getClass().getName() + ": Not implemented yet");
	}

}

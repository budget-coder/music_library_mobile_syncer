package filesystem;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;

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
	private final String pathToFile; 
	
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
		this.pathToFile = pathToFile;
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
		this.pathToFile = pathToFile;
		// TODO Improve with Null Pattern for storage (!!!that and folderObject in MTPUtil!!!)
		if (parentFolder == null || parentFolder instanceof NullPortableDeviceFolderObject) {
			file = new NullPortableDeviceObject();
			System.out.println("MTP: path + " + pathToFile + " does not exist!");
		} else {
			file = MTPUtil.getChildFileByNameRecursively(parentFolder, pathToFile);
			System.out.println("MTP: Constructed file of path " + pathToFile + " Path is folder? " + (file instanceof PortableDeviceFolderObject));
		}
		if (file instanceof PortableDeviceAudioObject) {
			audioFile = (PortableDeviceAudioObject) file;
		} else {
			audioFile = null;
		}
	}
	
	/**
	 * Private constructor for directly creating <b>known</b> MTP files.
	 * 
	 * @see #MTPFile(PortableDeviceStorageObject, String)
	 */
	private MTPFile(PortableDeviceObject file, String pathToFile) {
		this.file = file;
		this.pathToFile = pathToFile;
		if (file instanceof PortableDeviceAudioObject) {
			audioFile = (PortableDeviceAudioObject) file;
		} else {
			// TODO improve with null pattern here as well
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
		return pathToFile;
	}

	@Override
	public FileWrapper[] listFiles() {
		if (!isDirectory()) {
			return null;
		}
		PortableDeviceObject[] deviceObjList = ((PortableDeviceFolderObject) file).getChildObjects();
		FileWrapper[] fileList = new FileWrapper[deviceObjList.length];
		for (int i = 0; i < deviceObjList.length; ++i) {
			fileList[i] = new MTPFile(deviceObjList[i],
					pathToFile + File.separator + deviceObjList[i].getOriginalFileName());
		}
		return fileList;
	}

	@Override
	public String getDuration() {
		String duration = audioFile.getDuration().toString();
		// TODO jaudiotagger solution for duration did not work, so it has been changed
		// to getting it from AudioHeader, which only supports duration down to seconds.
		// Therefore, we cut off the milliseconds here with "-3".
		return duration.substring(0, duration.length()-3);
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
		System.out.println("Changing tag " + fieldKey + " to \"" + tagValueSrc + "\"");
		switch (fieldKey) {
		case TITLE:
			audioFile.setTitle(tagValueSrc);
			break;
		case ARTIST:
			audioFile.setArtist(tagValueSrc);
			break;
		case ALBUM_ARTIST:
			audioFile.setAlbumArtist(tagValueSrc);
			break;
		case ALBUM:
			audioFile.setAlbum(tagValueSrc);
			break;
		case YEAR:
			// This is a very roundabout way of changing the year because Date's methods are
			// more or less deprecated.
			Calendar cal = Calendar.getInstance();
			cal.setTime(audioFile.getReleaseDate());
			cal.set(Calendar.YEAR, Integer.parseInt(tagValueSrc));
			audioFile.setReleaseDate(cal.getTime());
			break;
		case COMPOSER:
			audioFile.setComposer(tagValueSrc);
			break;
		case DISC_NO:
			throw new UnsupportedOperationException(getClass().getName() + ": DISC_NO not implemented yet (because jmtp is incomplete)");
			// REMEMBER BREAK
		case GENRE:
			audioFile.setGenre(tagValueSrc);
			break;
		case TRACK:
			audioFile.setTrackNumber(Integer.parseInt(tagValueSrc));
			break;
		default:
			System.err.println("FATAL: Unknown tag " + fieldKey + " from mtp is requested");
			break;
		}
	}

	@Override
	public void applyTagChanges() { // Do nothing when using the jmtp library.
	}

	@Override
	public Artwork getAlbumArt() {
		throw new UnsupportedOperationException(getClass().getName() + ": getAlbumArt() not implemented yet");
	}

	@Override
	public void changeAlbumArt(Artwork newArt) {
		throw new UnsupportedOperationException(getClass().getName() + ": changeAlbumArt() not implemented yet");
	}
}

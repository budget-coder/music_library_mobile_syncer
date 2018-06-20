package filesystem;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

import data.DataClass;
import framework.FileWrapper;

public class NullFileWrapper implements FileWrapper {
	private static final NullFileWrapper[] NULL_FILE_WRAPPER_ARR = {new NullFileWrapper()};

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String getName() {
		return DataClass.ERROR_STRING;
	}

	@Override
	public String getAbsolutePath() {
		return DataClass.ERROR_STRING;
	}

	@Override
	public FileWrapper[] listFiles() {
		return NULL_FILE_WRAPPER_ARR;
	}

	@Override
	public boolean deleteFile() {
		return false;
	}

	@Override
	public boolean doesFileExist() {
		return false;
	}

	@Override
	public String getDuration() {
		return DataClass.ERROR_STRING;
	}

	@Override
	public String getTagData(FieldKey fieldKey) {
		return DataClass.ERROR_STRING;
	}

	@Override
	public void changeTag(FieldKey fieldKey, String tagValueSrc) { // Do nothing
	}

	@Override
	public void applyTagChanges() { // Do nothing
	}

	@Override
	public Artwork getAlbumArt() {
		return null; // TODO Use null pattern for Artwork
	}

	@Override
	public void changeAlbumArt(Artwork newArt) { // Do nothing
	}
}

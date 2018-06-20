package framework;

import java.io.IOException;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.images.Artwork;

public interface FileWrapper {
	public boolean isDirectory();
	public String getName();
	public String getAbsolutePath();
	public FileWrapper[] listFiles();
	
	public boolean deleteFile();
	
	/**
	 * 
	 * @return A unique value corresponding to the file's current state. If it could
	 *         not be computed, then 0 is returned.
	 * @throws IOException
	 *             if any operation on the file fails/is abruptly stopped.
	 */
	//public int getUniqueHash() throws IOException;

	public boolean doesFileExist();
	public String getDuration() throws InterruptedException;
	public String getTagData(FieldKey fieldKey) throws InterruptedException;
	public void changeTag(FieldKey fieldKey, String tagValueSrc);
	public void applyTagChanges();
	//////////////////////
	// TODO ONLY WORKS FOR NON-MTP DEVICES (FOR NOW)
	public Artwork getAlbumArt() throws InterruptedException;
	public void changeAlbumArt(Artwork newArt);
	//////////////////////
	
}

package filesystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import be.derycke.pieter.com.Guid;
import jmtp.PortableDeviceAudioObject;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDevicePlaylistObject;

public class NullPortableDeviceFolderObject implements PortableDeviceFolderObject {
	private static final Date NULL_DATE = new Date();
	private static final NullPortableDeviceObject NULL_PORT_DEV_OBJ = new NullPortableDeviceObject();
	private static final short[] NULL_SHORT_ARR = {0};
	private static final NullPortableDeviceFolderObject NULL_PORT_DEV_FOLDER_OBJ = new NullPortableDeviceFolderObject();
	private static final NullPortableDeviceObject[] NULL_PORT_DEV_OBJ_ARR = {NULL_PORT_DEV_OBJ};

	@Override
	public String getID() {
		return "";
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getOriginalFileName() {
		return "";
	}

	@Override
	public boolean canDelete() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public boolean isSystemObject() {
		return false;
	}

	@Override
	public boolean isDrmProtected() {
		return false;
	}

	@Override
	public Date getDateModified() {
		return NULL_DATE;
	}

	@Override
	public Date getDateCreated() {
		return NULL_DATE;
	}

	@Override
	public Date getDateAuthored() {
		return NULL_DATE;
	}

	@Override
	public PortableDeviceObject getParent() {
		return NULL_PORT_DEV_OBJ;
	}

	@Override
	public BigInteger getSize() {
		return BigInteger.ZERO;
	}

	@Override
	public String getPersistentUniqueIdentifier() {
		return "";
	}

	@Override
	public String getSyncID() {
		return "";
	}

	@Override
	public Guid getFormat() {
		return new Guid(0, 0, 0, NULL_SHORT_ARR);
	}

	@Override
	public void setSyncID(String value) { // Do nothing
	}

	@Override
	public void delete() { // Do nothing
	}

	@Override
	public PortableDeviceObject[] getChildObjects() {
		return NULL_PORT_DEV_OBJ_ARR;
	}

	@Override
	public PortableDeviceAudioObject addAudioObject(File bestand, String artist, String title, BigInteger duration)
			throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortableDeviceAudioObject addAudioObject(File file, String artist, String title, BigInteger duration,
			String genre, String album, Date releaseDate, int track) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortableDevicePlaylistObject createPlaylistObject(String name, PortableDeviceObject[] references) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortableDeviceFolderObject createFolderObject(String name) {
		return NULL_PORT_DEV_FOLDER_OBJ;
	}

	@Override
	public void delete(boolean recursive) { // Do nothing
	}
}

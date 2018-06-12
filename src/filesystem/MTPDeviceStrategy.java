package filesystem;

import java.io.IOException;

import framework.DeviceStrategy;
import framework.FileWrapper;
import jmtp.PortableDevice;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;

public class MTPDeviceStrategy implements DeviceStrategy {
	private PortableDevice device;
	private PortableDeviceStorageObject storage;
	private final MTPFile dstFolderMTP;
	private static final MTPFile NULL_MTPFILE = new MTPFile(new NullPortableDeviceFolderObject(), "");

	/**
	 * Constructs an MTP device strategy from the given folder path.
	 * 
	 * @param dstFolder
	 *            - The destination folder. If it is not on form
	 *            "&lt;device&gt;/&lt;storage&gt;/&lt;path-to-folder&gt;", then the
	 *            resulting MTP folder is invalid. Example: G3/SD-card/Music
	 */
	public MTPDeviceStrategy(String dstFolder) {
		// Verify that the folder path is legal
		final String strDevice;
		final String strNoDevice;
		final String strStorage;
		if (dstFolder.indexOf('/') > 0) {
			strDevice = dstFolder.substring(0, dstFolder.indexOf('/'));
			strNoDevice = dstFolder.substring(strDevice.length()+1); // +1 skips '/'
			if (strNoDevice.indexOf('/') > 0) {
				strStorage = strNoDevice.substring(0, strNoDevice.indexOf('/'));
			} else {
				dstFolderMTP = NULL_MTPFILE; // Invalid path. Create invalid MTPFile.
				return;
			}
		} else {
			dstFolderMTP = NULL_MTPFILE;
			return;
		}
		//System.out.println("showDialog: MTP device is " + strDevice + " and storage is " + strStorage);
		// Locate device and storage from path.
		for (PortableDevice device : MTPUtil.getDevices()) {
			if (device.getFriendlyName().equals(strDevice)) {
				this.device = device;
				for (PortableDeviceStorageObject storage : MTPUtil.getDeviceStorages(device)) {
					if (storage.getName().equals(strStorage)) {
						this.storage = storage;
						break;
					} // if-storage-found end
				} // for-each-storage
				break;
			} // if-device-found end
		} // for-each-device
		if (device != null && storage != null) {
			// Success scenario: both device and storage are valid. Pass the pointed-to folder/file along.
			final String strOnlyFolders = strNoDevice.substring(strStorage.length()+1); // +1 skips '/'
			dstFolderMTP = new MTPFile(storage, strOnlyFolders);
		} else {
			dstFolderMTP = NULL_MTPFILE;
		}
	}

	@Override
	public boolean isDstADirectory() {
		return dstFolderMTP.isDirectory();
	}

	@Override
	public FileWrapper[] listDstFiles() {
		final FileWrapper[] dstFolderList = dstFolderMTP.listFiles();
		FileWrapper[] returnList = new FileWrapper[dstFolderList.length];
		for (int i = 0; i < dstFolderList.length; ++i) {
			returnList[i] = new MTPFile(storage, dstFolderList[i].getAbsolutePath());
		}
		return returnList;
	}

	@Override
	public FileWrapper getDstFolder() {
		return dstFolderMTP;
	}

	@Override
	public FileWrapper getFileInstance(String path) {
		// TODO Make smarter by using the other constructor with a folder instead of always recursing.
		return new MTPFile(storage, path);
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(getClass() + ": copyMusicToDst not implemeneted yet");

	}

}

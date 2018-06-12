package filesystem;

import framework.FileWrapper;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;

public class MTPFile implements FileWrapper {
	private final PortableDeviceObject file;
	
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

}

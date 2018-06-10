package filesystem;

import framework.FileWrapper;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;

public class MTPFile implements FileWrapper {
	private PortableDeviceObject file;
	
	public MTPFile(PortableDeviceStorageObject storage, String pathToFile) {
		file = MTPUtil.getChildByName(storage, pathToFile);
	}

	@Override
	public boolean isDirectory() {
		if ((PortableDeviceFolderObject) file != null) {
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
		// TODO Auto-generated method stub
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

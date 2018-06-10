package filesystem;

import java.io.IOException;
import java.util.ArrayList;

import framework.DeviceStrategy;
import framework.FileWrapper;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;

public class MTPDeviceStrategy implements DeviceStrategy {
	private final PortableDeviceStorageObject storage;
	private final MTPFile dstFolderMTP;

	public MTPDeviceStrategy(PortableDeviceStorageObject storage, String dstFolder) {
		this.storage = storage;
		dstFolderMTP = new MTPFile(storage, dstFolder);
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
		return new MTPFile(storage, path);
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException(getClass() + ": copyMusicToDst not implemeneted yet");

	}

}

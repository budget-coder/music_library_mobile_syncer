package filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import framework.DeviceStrategy;
import framework.FileWrapper;

public class PCDeviceStrategy implements DeviceStrategy {
	private final File dstFolder;
	private final PCFile dstFolderPC;

	public PCDeviceStrategy(File dstFolder) {
		this.dstFolder = dstFolder;
		dstFolderPC = new PCFile(dstFolder);
	}
	
	@Override
	public boolean isDstADirectory() {
		return dstFolder.isDirectory();
	}
	
	@Override
	public FileWrapper[] listDstFiles() {
		final File[] dstFolderList = dstFolder.listFiles();
		FileWrapper[] returnList = new FileWrapper[dstFolderList.length];
		for (int i = 0; i < dstFolderList.length; i++) {
			returnList[i] = new PCFile(dstFolderList[i]);
		}
		return returnList;
	}
	
	@Override
	public FileWrapper getDstFolder() {
		return dstFolderPC;
	}
	
	@Override
	public FileWrapper getFileInstance(String path) {
		return new PCFile(path);
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		Path targetPath = Paths.get(dstFolder.getAbsolutePath()).resolve(newMusic.getName());
		Files.copy(Paths.get(newMusic.getAbsolutePath()), targetPath,
					StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
	}
}

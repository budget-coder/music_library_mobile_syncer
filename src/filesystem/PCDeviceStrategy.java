package filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import framework.DeviceStrategy;
import framework.FileWrapper;

public class PCDeviceStrategy implements DeviceStrategy {
	private final PCFile dstFolderPC;

	public PCDeviceStrategy(String dstFolder) {
		dstFolderPC = new PCFile(dstFolder);
	}
	
	@Override
	public boolean isADirectory() {
		return dstFolderPC.isDirectory();
	}
	
	@Override
	public FileWrapper[] listFiles() {
		final FileWrapper[] dstFolderList = dstFolderPC.listFiles();
		FileWrapper[] returnList = new FileWrapper[dstFolderList.length];
		for (int i = 0; i < dstFolderList.length; ++i) {
			returnList[i] = new PCFile(dstFolderList[i].getAbsolutePath());
		}
		return returnList;
	}
	
	@Override
	public FileWrapper getFolder() {
		return dstFolderPC;
	}
	
	@Override
	public FileWrapper getFileInstance(String path) {
		return new PCFile(path);
	}

	@Override
	public void copyMusicToCurrentFolder(FileWrapper newMusic) throws IOException {
		copyMusicToSpecificFolder(newMusic, dstFolderPC.getAbsolutePath());
	}

	@Override
	public void copyMusicToSpecificFolder(FileWrapper newMusic, String pathToFolder) throws IOException {
		Path targetPath = Paths.get(pathToFolder).resolve(newMusic.getName());
		Files.copy(Paths.get(newMusic.getAbsolutePath()), targetPath,
					StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
	}
}

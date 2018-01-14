package filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import framework.DeviceStrategy;
import framework.FileWrapper;

public class PCDeviceStrategy implements DeviceStrategy {
	private final File srcFolder, dstFolder;

	public PCDeviceStrategy(File srcFolder, File dstFolder) {
		this.srcFolder = srcFolder;
		this.dstFolder = dstFolder;
	}
	
	@Override
	public List<FileWrapper> listSrcOrDstFiles(boolean isSrc) {
		List<FileWrapper> returnList = new ArrayList<>();
		if (isSrc) {
			for (File file : srcFolder.listFiles()) {
				returnList.add(new PCFile(file));
			}
		} else {
			for (File file : dstFolder.listFiles()) {
				returnList.add(new PCFile(file));
			}
		}
		return returnList;
	}

	@Override
	public int getSizeOfSrcOrDstFolder(boolean isSrc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void copyMusic(String sourcePath, String targetPath) throws IOException {
		Files.copy(Paths.get(sourcePath), Paths.get(targetPath),
					StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
	}
}

package framework;

import java.io.IOException;
import java.util.List;

public interface DeviceStrategy {
	// fileEntrySrc
	
	
	// fileEntryDst
	
	
	// listOfSrc
	//public DeviceStrategy(String srcFolderStr, String dstFolderStr);
	public List<FileWrapper> listSrcOrDstFiles(boolean isSrc);
	public int getSizeOfSrcOrDstFolder(boolean isSrc);
	
	public void copyMusic(String sourcePath, String targetPath) throws IOException;
	
	// srcFolder
	// new File
	
	//public int getNoOfFiles();
}

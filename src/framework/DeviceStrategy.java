package framework;

import java.io.IOException;

public interface DeviceStrategy {
	public boolean isADirectory();

	public FileWrapper[] listFiles();

	public FileWrapper getFolder();

	public FileWrapper getFileInstance(String path);
	
	public void copyMusicToCurrentFolder(FileWrapper newMusic) throws IOException;
	
	public void copyMusicToSpecificFolder(FileWrapper newMusic, String pathToFolder) throws IOException;
}

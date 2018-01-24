package framework;

import java.io.IOException;

public interface DeviceStrategy {
	public boolean isDstADirectory();

	public FileWrapper[] listDstFiles();

	public FileWrapper getDstFolder();

	public FileWrapper getFileInstance(String path);

	public void copyMusicToDst(FileWrapper newMusic) throws IOException;
}

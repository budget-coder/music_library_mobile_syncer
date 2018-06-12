package framework;

import java.io.IOException;

public interface StateDeviceStrategy {
	public void copyMusicToDst(FileWrapper newMusic) throws IOException;
	
	public void setSrcAsMTPDevice(boolean option);
	
	public void setDstAsMTPDevice(boolean option);

	public FileWrapper getSrcFolder();
	
	public FileWrapper getDstFolder();
}

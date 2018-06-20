package framework;

import java.io.IOException;

public interface StateDeviceStrategy {
	public void copyMusicToDst(FileWrapper newMusic) throws IOException;
}

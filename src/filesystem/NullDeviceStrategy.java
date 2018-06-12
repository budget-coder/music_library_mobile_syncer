package filesystem;

import java.io.IOException;

import framework.DeviceStrategy;
import framework.FileWrapper;

public class NullDeviceStrategy implements DeviceStrategy {
	private static final NullFileWrapper[] NULL_FILE_WRAPPER_ARR = {new NullFileWrapper()};

	@Override
	public boolean isADirectory() {
		return false;
	}

	@Override
	public FileWrapper[] listFiles() {
		return NULL_FILE_WRAPPER_ARR;
	}

	@Override
	public FileWrapper getFolder() {
		return new NullFileWrapper();
	}

	@Override
	public FileWrapper getFileInstance(String path) {
		return new NullFileWrapper();
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException { // Do nothing
	}
}

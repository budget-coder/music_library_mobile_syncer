package filesystem;

import framework.FileWrapper;

public class NullFileWrapper implements FileWrapper {
	private static final NullFileWrapper[] NULL_FILE_WRAPPER_ARR = {new NullFileWrapper()};

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public String getAbsolutePath() {
		return "";
	}

	@Override
	public FileWrapper[] listFiles() {
		return NULL_FILE_WRAPPER_ARR;
	}

	@Override
	public boolean deleteFile() {
		return false;
	}

	@Override
	public boolean doesFileExist() {
		return false;
	}
}

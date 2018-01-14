package filesystem;

import java.io.IOException;

import framework.FileWrapper;

public class MTPFile implements FileWrapper {

	@Override
	public boolean isDirectory() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteFile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getUniqueHash() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean doesFileExist() {
		// TODO Auto-generated method stub
		return false;
	}

}

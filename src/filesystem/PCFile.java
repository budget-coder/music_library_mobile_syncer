package filesystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import framework.FileWrapper;
import util.MurmurHash3;

public class PCFile implements FileWrapper {
	private final File file; 
	
	public PCFile(File file) {
		this.file = file;
	}
	
	public PCFile(String pathToFile) {
		file = new File(pathToFile);
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public String getName() {
		return file.getName();
	}
	
	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

	@Override
	public boolean deleteFile() {
		return file.delete();
	}

//	@Override
//	public int getUniqueHash() throws IOException {
//		byte[] fileEntrySrcBytes = Files.readAllBytes(file.toPath());
//        // We use MurmurHash3 on the file itself to get a unique hash to compare with.
//        return MurmurHash3.murmurhash3_x86_32(fileEntrySrcBytes, 0, file.getName().length(), 14); 
//	}

	@Override
	public boolean doesFileExist() {
		return file.exists();
	}

}

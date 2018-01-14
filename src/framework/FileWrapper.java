package framework;

import java.io.IOException;

public interface FileWrapper {
	public boolean isDirectory();
	public String getName();
	
	public boolean deleteFile();
	
	/**
	 * 
	 * @return A unique value corresponding to the file's current state. If it could
	 *         not be computed, then -1 is returned.
	 * @throws IOException TODO
	 */
	public int getUniqueHash() throws IOException;

	public boolean doesFileExist();
}

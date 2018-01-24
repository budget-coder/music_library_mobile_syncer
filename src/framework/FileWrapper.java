package framework;

import java.io.IOException;

public interface FileWrapper {
	public boolean isDirectory();
	public String getName();
	public String getAbsolutePath();
	
	public boolean deleteFile();
	
	/**
	 * 
	 * @return A unique value corresponding to the file's current state. If it could
	 *         not be computed, then 0 is returned.
	 * @throws IOException
	 *             if any operation on the file fails/is abruptly stopped.
	 */
	//public int getUniqueHash() throws IOException;

	public boolean doesFileExist();
}

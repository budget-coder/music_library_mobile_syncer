package main;

import java.io.File;
import java.io.IOException;

import filesystem.PCFile;
import framework.DeviceStrategy;
import framework.FileWrapper;
import framework.StateDeviceStrategy;

public class SwitchBetweenDevicesStrategy implements StateDeviceStrategy {
	private final DeviceStrategy srcStrategy;
	private final DeviceStrategy dstStrategy;
	private final boolean isSrcDevice;
	private final boolean isDstDevice;
	
	public SwitchBetweenDevicesStrategy(final DeviceStrategy srcStrategy, final DeviceStrategy dstStrategy,
			final boolean isSrcDevice, final boolean isDstDevice) {
		this.srcStrategy = srcStrategy;
		this.dstStrategy = dstStrategy;
		this.isSrcDevice = isSrcDevice;
		this.isDstDevice = isDstDevice;
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		if (!isSrcDevice && !isDstDevice) {
			dstStrategy.copyMusicToCurrentFolder(newMusic);
		} else if (!isSrcDevice && isDstDevice) {
			dstStrategy.copyMusicToCurrentFolder(newMusic);
		} else if (isSrcDevice && !isDstDevice) {
			srcStrategy.copyMusicToSpecificFolder(newMusic, dstStrategy.getFolder().getAbsolutePath());
		} else {
			// Both the src. and dst. are MTP devices. Step 1: copy the music temporarily to the workspace 
			srcStrategy.copyMusicToSpecificFolder(newMusic, System.getProperty("user.dir"));
			// Step 2: Get an instance of the new file in our workspace. 
			FileWrapper tempMusic = new PCFile(System.getProperty("user.dir") + File.separator + newMusic.getName());
			// Step 3: Copy the new workspace file to the dst.
			dstStrategy.copyMusicToCurrentFolder(tempMusic);
			// Step 4: Cleanup. Delete the new file from the workspace.
			tempMusic.deleteFile();
		}
	}
}

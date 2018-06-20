package main;

import java.io.IOException;

import framework.DeviceStrategy;
import framework.FileWrapper;
import framework.StateDeviceStrategy;

// TODO why am I extending NullDeviceStrategy here...?
public class SwitchBetweenDevicesStrategy implements StateDeviceStrategy {
	private final DeviceStrategy srcStrategy;
	private final DeviceStrategy dstStrategy;
	private final boolean isSrcDevice;
	
	public SwitchBetweenDevicesStrategy(final DeviceStrategy srcStrategy, final DeviceStrategy dstStrategy,
			final boolean isSrcDevice) {
		this.srcStrategy = srcStrategy;
		this.dstStrategy = dstStrategy;
		this.isSrcDevice = isSrcDevice;
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		if (isSrcDevice) {
			srcStrategy.copyMusicToDst(newMusic);
		} else {
			dstStrategy.copyMusicToDst(newMusic);
		}
	}
}

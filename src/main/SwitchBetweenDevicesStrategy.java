package main;

import java.io.IOException;

import filesystem.NullDeviceStrategy;
import framework.DeviceStrategy;
import framework.FileWrapper;
import framework.StateDeviceStrategy;

public class SwitchBetweenDevicesStrategy extends NullDeviceStrategy implements StateDeviceStrategy {
	private final DeviceStrategy mtpStrategy;
	private final DeviceStrategy pcStrategy;
	private DeviceStrategy currentSrcState;
	private DeviceStrategy currentDstState;
	
	public SwitchBetweenDevicesStrategy(final DeviceStrategy mtpStrategy,
			final DeviceStrategy pcStrategy) {
		this.mtpStrategy = mtpStrategy;
		this.pcStrategy = pcStrategy;
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		// Iff. source and destination are on the PC, then use pcStrategy. Otherwise, use mtpStrategy.
		if (currentSrcState.equals(currentDstState) && currentSrcState.equals(pcStrategy)) {
			pcStrategy.copyMusicToDst(newMusic);
		} else {
			mtpStrategy.copyMusicToDst(newMusic);
		}
	}
	
	@Override
	public FileWrapper getSrcFolder() {
		return currentSrcState.getFolder();
	}
	
	@Override
	public FileWrapper getDstFolder() {
		return currentDstState.getFolder();
	}

	@Override
	public void setSrcAsMTPDevice(boolean isDevice) {
		currentSrcState = (isDevice ? mtpStrategy : pcStrategy);
	}

	@Override
	public void setDstAsMTPDevice(boolean isDevice) {
		currentDstState = (isDevice ? mtpStrategy : pcStrategy);
	}
}

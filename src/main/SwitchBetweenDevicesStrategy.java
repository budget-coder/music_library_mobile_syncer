package main;

import java.io.IOException;

import framework.DeviceStrategy;
import framework.FileWrapper;
import framework.StateDeviceStrategy;

public class SwitchBetweenDevicesStrategy implements StateDeviceStrategy {
	private DeviceStrategy mtpStrategy;
	private DeviceStrategy pcStrategy;
	private DeviceStrategy currentState;
	
	public SwitchBetweenDevicesStrategy(DeviceStrategy mtpStrategy,
			DeviceStrategy pcStrategy) {
		this.mtpStrategy = mtpStrategy;
		this.pcStrategy = pcStrategy;
	}
	
	@Override
	public void setToPCOrDevice(boolean isDevice) {
		currentState = (isDevice ? mtpStrategy : pcStrategy); // Same as if-then-else
	}

	@Override
	public FileWrapper[] listDstFiles() {
		return currentState.listDstFiles();
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		currentState.copyMusicToDst(newMusic);
	}
}

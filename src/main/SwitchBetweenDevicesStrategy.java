package main;

import java.io.IOException;
import java.util.List;

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
	public List<FileWrapper> listSrcOrDstFiles(boolean isSrc) {
		return currentState.listSrcOrDstFiles(isSrc);
	}

	@Override
	public int getSizeOfSrcOrDstFolder(boolean isSrc) {
		return currentState.getSizeOfSrcOrDstFolder(isSrc);
	}

	@Override
	public void copyMusic(String sourcePath, String targetPath) throws IOException {
		currentState.copyMusic(sourcePath, targetPath);
	}
}

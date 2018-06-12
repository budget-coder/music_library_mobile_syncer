package main;

import java.io.IOException;

import framework.DeviceStrategy;
import framework.FileWrapper;
import framework.StateDeviceStrategy;

public class SwitchBetweenDevicesStrategy implements StateDeviceStrategy {
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
	public void setSrcAsMTPDevice(boolean isDevice) {
		currentSrcState = (isDevice ? mtpStrategy : pcStrategy);
	}

	@Override
	public void setDstAsMTPDevice(boolean isDevice) {
		currentDstState = (isDevice ? mtpStrategy : pcStrategy);
	}
	
	/*
	@Override
	public void setToPCOrDevice(boolean isDevice) {
		currentState = (isDevice ? mtpStrategy : pcStrategy); // Same as if-then-else
	}
	*/

	@Override
	public FileWrapper[] listDstFiles() {
		return currentDstState.listDstFiles();
	}

	@Override
	public void copyMusicToDst(FileWrapper newMusic) throws IOException {
		// Iff. source and destination are on the PC, then use pcStrategy. Otherwise, use mtpStrategy.
		if (currentSrcState.equals(currentDstState) && currentSrcState.equals(pcStrategy)) {
			pcStrategy.copyMusicToDst(newMusic);
		}
	}

	@Override
	public boolean isDstADirectory() {
		return currentDstState.isDstADirectory();
	}

	@Override
	public FileWrapper getDstFolder() {
		return currentDstState.getDstFolder();
	}

	@Override
	public FileWrapper getSrcFileInstance(String path) {
		return currentSrcState.getFileInstance(path);
	}
	
	@Override
	public FileWrapper getDstFileInstance(String path) {
		return currentDstState.getFileInstance(path);
	}

	@Override
	public FileWrapper getFileInstance(String path) {
		// TODO State pattern seems hard to make use of here other than when
		// using the copy function.
		throw new UnsupportedOperationException(getClass().getName() + ": getFileInstance not implemented");
	}
}

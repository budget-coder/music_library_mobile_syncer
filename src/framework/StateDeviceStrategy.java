package framework;

public interface StateDeviceStrategy extends DeviceStrategy {
	//public void setToPCOrDevice(boolean isDevice);

	public void setSrcAsMTPDevice(boolean option);

	public void setDstAsMTPDevice(boolean option);

	FileWrapper getSrcFileInstance(String path);

	FileWrapper getDstFileInstance(String path);
}

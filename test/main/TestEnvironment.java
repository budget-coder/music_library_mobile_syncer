package main;

import jmtp.PortableDevice;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import util.MTPUtil;

public class TestEnvironment {
    public static void main(final String[] args) {
        System.out.println("Printing devices...");
        //PortableDevice[] devices = MTPUtil.getDevices();
        MTPUtil.printAll();
        System.out.println("Done.");

        System.out.println("Getting device manually");
        PortableDevice[] devices = MTPUtil.getDevices();
        if (devices.length == 0) {
            return;
        }
        devices[0].open();
        for (PortableDeviceObject object : devices[0].getRootObjects()) {
            PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
            System.out.println("Device name: " + storage.getChildObjects()[0].getOriginalFileName());
        }
    }
}
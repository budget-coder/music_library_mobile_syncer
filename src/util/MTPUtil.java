package util;

import java.util.ArrayList;

//import de.aok.no.kopra.agnes.desktop.util.LogUtil;

import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceManager;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;

/**
 * @author Philipp Ebert
 * 
 *         Various util methods for MTP device communication
 */
public class MTPUtil {
    private static PortableDeviceFolderObject folderNew;
    private static PortableDeviceManager manager = new PortableDeviceManager();

    public static PortableDeviceFolderObject createFolder(String path,
            PortableDeviceStorageObject storage,
            PortableDeviceFolderObject folder, String lastDir) {
    	path = path.substring((path.indexOf("\\") + 1), path.length());
        if (path.indexOf("\\") != -1) {
            String fileName = path.substring(0, path.indexOf("\\"));
            if (folder == null) {
                folderNew = (PortableDeviceFolderObject) getChildByName(storage, fileName);
                if (folderNew == null) {
                    folderNew = storage.createFolderObject(fileName);
                    // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,MTPUtil.class.getSimpleName(),
                    // "Created Root Directory " + z);
                }
                return createFolder(path, storage, folderNew, lastDir);
            } else {
                folderNew = (PortableDeviceFolderObject) getChildByName(folder, fileName);
                if (folderNew == null) {
                    folderNew = folder.createFolderObject(fileName);
                    // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,
                    // MTPUtil.class.getSimpleName(), "Created Directory " + z);
                }
                return createFolder(path, storage, folderNew, lastDir);
            }
        } else { // we have reached the top, try to create the last folder
            if (folder == null) {
                // no need to do anything
                // storage.createFolderObject(lastDir);
                folderNew = (PortableDeviceFolderObject) getChildByName(storage,
                        lastDir);
            } else {
                folderNew = getChildByName(folder, lastDir);
                if (folderNew == null) {
                    // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,
                    // MTPUtil.class.getSimpleName(), "Created Last Directory "
                    // + lastDir);
                    folderNew = folder.createFolderObject(lastDir);
                }
            }
            return folderNew;
        }
    }

    //private static PortableDeviceFolderObject getChildByName(
    public static PortableDeviceFolderObject getChildByName(
            PortableDeviceFolderObject folder, String childName) {
        for (PortableDeviceObject object : folder.getChildObjects()) {
            if (object.getOriginalFileName().equals(childName)) {
                // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,MTPUtil.class.getSimpleName(),
                // "Found directory " + z);
                return (PortableDeviceFolderObject) object;
            }
        }
        return null;
    }

    //private static PortableDeviceObject getChildByName(
    public static PortableDeviceObject getChildByName(
            PortableDeviceStorageObject storage, String childName) {
        for (PortableDeviceObject object : storage.getChildObjects()) {
            if (object.getOriginalFileName().equals(childName)) {
                // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,
                // MTPUtil.class.getSimpleName(), "Found directory " + z);
                return object;
            }
        }
        return null;
    }

    public static PortableDevice[] getDevices() {
        manager.refreshDeviceList();
        return manager.getDevices();
    }
    
    public static ArrayList<PortableDeviceStorageObject> getDeviceStorages(PortableDevice device) {
        manager.refreshDeviceList();
        ArrayList<PortableDeviceStorageObject> storageList = new ArrayList<>();
        for (PortableDeviceObject object : device.getRootObjects()) {
            if (!(object instanceof PortableDeviceStorageObject)) {
                continue;
            }
            storageList.add((PortableDeviceStorageObject) object);
        }
        return storageList;
    }

    public static void printDevices() {
        manager.refreshDeviceList();
        for (PortableDevice device : manager.getDevices()) {
            device.open();
            System.out.println("MTPUtil - Devices - " + device.getModel());
            device.close();
        }
    }

    public static void printAll() {
        manager.refreshDeviceList();
        for (PortableDevice device : manager.getDevices()) {
            device.open();
            System.out.println("MTPUtil Device - " + device.getModel());
            if (device.getRootObjects().length == 0) {
                System.out.println("Could not access " + device.getModel()
                        + "'s storage. Make sure it has been unlocked once since connecting it.");
            }
            for (PortableDeviceObject object : device.getRootObjects()) {
                if (!(object instanceof PortableDeviceStorageObject)) {
                    continue;
                }
                
                
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
                System.out.println("New storage! Traversing...");
                System.out.println("Name: " + storage.getName());

                for (PortableDeviceObject child : ((PortableDeviceStorageObject) storage)
                        .getChildObjects()) {
                    System.out.println("MTPUtil Dir - " + child.getName());
                }
            }
        }
    }

    /**
     * Requires all devices to be closed
     * 
     * @return Array of device descriptions
     */
    public static ArrayList<MTPDeviceInfo> getDeviceModels() {
        ArrayList<MTPDeviceInfo> models = new ArrayList<MTPDeviceInfo>();
        manager.refreshDeviceList();
        for (PortableDevice device : manager.getDevices()) {
            device.open();
            models.add(new MTPDeviceInfo(device.getModel(),
                    device.getSerialNumber()));
            device.close();
        }
        return models;
    }
}
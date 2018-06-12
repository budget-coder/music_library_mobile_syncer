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
                folderNew = (PortableDeviceFolderObject) getChildFileByName(storage, fileName);
                if (folderNew == null) {
                    folderNew = storage.createFolderObject(fileName);
                    // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,MTPUtil.class.getSimpleName(),
                    // "Created Root Directory " + z);
                }
                return createFolder(path, storage, folderNew, lastDir);
            } else {
                folderNew = (PortableDeviceFolderObject) getChildFolderByName(folder, fileName);
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
                folderNew = (PortableDeviceFolderObject) getChildFileByName(storage,
                        lastDir);
            } else {
                folderNew = getChildFolderByName(folder, lastDir);
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
    
    public static PortableDeviceObject getChildFileByName(
    		PortableDeviceFolderObject folder, String childName) {
    	for (PortableDeviceObject object : folder.getChildObjects()) {
            if (object.getOriginalFileName().equals(childName)) {
                // LogUtil.debugPrint(LogUtil.LOG_LEVEL_FULL,MTPUtil.class.getSimpleName(),
                // "Found directory " + z);
                return object;
            }
        }
        return null;
    }

    //private static PortableDeviceFolderObject getChildByName(
    public static PortableDeviceFolderObject getChildFolderByName(
            PortableDeviceFolderObject folder, String childName) {
        return (PortableDeviceFolderObject) getChildFileByName(folder, childName);
    }

    //private static PortableDeviceObject getChildByName(
    public static PortableDeviceObject getChildFileByName(
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
    
    public static PortableDeviceObject getChildFileByNameRecursively(
    		PortableDeviceFolderObject folder, String pathToChild) {
    	PortableDeviceFolderObject nextFolderObj = folder; // Take a copy first
    	PortableDeviceObject nextFileObj;
    	String currFolder = "";
    	do {
    		final int indexOfSeperator = pathToChild.indexOf('/');
    		// If and only if there are more separators, then use the index of the next one.
    		if (indexOfSeperator >= 0) {
    			currFolder = pathToChild.substring(0, pathToChild.indexOf('/'));
    			pathToChild = pathToChild.substring(currFolder.length()+1); // Again, +1 for skipping separator '/'
    		} else {
    			// No separator found i.e. we must have gotten to the last folder.
    			currFolder = pathToChild;
    			pathToChild = pathToChild.substring(currFolder.length());
    		}
    		nextFileObj = MTPUtil.getChildFileByName(nextFolderObj, currFolder);
    		//System.out.println("nextFileObj is null? " + (nextFileObj == null) + ". Path is " + pathToChild);
    		if (nextFileObj instanceof PortableDeviceFolderObject) {
    			//System.out.println(nextFileObj.getOriginalFileName() + " is an instance of folder");
    			nextFolderObj = (PortableDeviceFolderObject) nextFileObj;
    		} else if (!(nextFileObj == null)) { // TODO Replace with null pattern
    			break; // We found a file instead of a folder. Break out.
    		}
    	} while (!pathToChild.isEmpty() && nextFileObj != null);
    	return nextFileObj;
    }
    
    public static PortableDeviceObject getChildFileByNameRecursively(
    		PortableDeviceStorageObject storage, String pathToChild) {
    	String currFolder = "";
    	// If no '/' is found, then the path only consists of one folder
    	if (pathToChild.indexOf('/') <= 0) {
    		currFolder = pathToChild;
    		return (PortableDeviceFolderObject) MTPUtil.getChildFileByName(storage, currFolder);
    	}
    	// If we got here, then the path consists of multiple folders which means we can recurse!
    	currFolder = pathToChild.substring(0, pathToChild.indexOf('/'));
    	PortableDeviceFolderObject nextFolderObj = (PortableDeviceFolderObject) MTPUtil.getChildFileByName(storage, currFolder);
    	// Iterate through the rest of the folders, if any
    	String nextFolders = pathToChild.substring(currFolder.length()+1); // Skip the first folder. +1 skips '/'
    	// If the path is "", then the first folder was just post-fixed with '/'. Return resultant folder.
    	if (nextFolders.isEmpty()) {
    		return nextFolderObj;
    	}
    	// Now we recurse through the rest of the folders!
    	return getChildFileByNameRecursively(nextFolderObj, nextFolders);
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
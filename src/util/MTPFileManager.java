package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;

import be.derycke.pieter.com.COMException;
import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceManager;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import jmtp.PortableDeviceToHostImpl32;

/**
 * @author Philipp Ebert
 * 
 *         Handles MTP device operation logic
 */
public class MTPFileManager {
    //boolean debug = false;
    private PortableDevice device;
    
    public MTPFileManager(PortableDevice portableDevice) {
    	device = portableDevice;
    }
    
    //public synchronized void openDevice(PortableDevice portableDevice) {
    public void openDevice() {
        device.open();
    }

    public void closeDevice() {
        if (device != null) {
            device.close();
            device = null;
        }
    }

    //public synchronized boolean isOpen() {
    public boolean isOpen() {
        if (device != null) {
            return true;
        }
        return false;
    }
    
    public PortableDevice getDevice() {
        return device;
    }

    public void addFile(File file, String mtpPath)
            throws FileNotFoundException, IOException, COMException {
        deleteFile(file.getName(), mtpPath);
        String lastpartofpath = mtpPath
                .substring(mtpPath.lastIndexOf("\\") + 1);
        PortableDeviceStorageObject storage = getStorage();
        PortableDeviceFolderObject folder = MTPUtil.createFolder(mtpPath,
                storage, null, lastpartofpath);

        // PortableDeviceAudioObject object = folder.addAudioObject(file, "--",
        // "--", new BigInteger("0"));
        folder.addAudioObject(file, "--", "--", new BigInteger("0"));
        // LogUtil.debugPrint(LogUtil.LOG_LEVEL_LESS,
        // this.getClass().getSimpleName(), "Copied " + file.getAbsolutePath() +
        // " to " + mtpPath
        // + " on " + getDevice().getModel());
    }

    public ArrayList<PortableDeviceObject> getFiles(String path)
            throws COMException {
        String lastpartofpath = path.substring(path.lastIndexOf("\\") + 1);
        PortableDeviceStorageObject storage = getStorage();
        PortableDeviceFolderObject folder = MTPUtil.createFolder(path, storage,
                null, lastpartofpath);

        ArrayList<PortableDeviceObject> newFiles = new ArrayList<PortableDeviceObject>();
        for (PortableDeviceObject object : folder.getChildObjects()) {
            newFiles.add(object);
        }
        return newFiles;

    }

    public ArrayList<PortableDeviceObject> getNewFiles(Date lastChecked,
            String path) throws COMException {
        String lastpartofpath = path.substring(path.lastIndexOf("\\") + 1);
        PortableDeviceStorageObject storage = getStorage();
        PortableDeviceFolderObject folder = MTPUtil.createFolder(path, storage,
                null, lastpartofpath);

        ArrayList<PortableDeviceObject> newFiles = new ArrayList<PortableDeviceObject>();
        for (PortableDeviceObject object : folder.getChildObjects()) {
            if (object.getDateModified() != null
                    && object.getDateModified().after(lastChecked)) {
                newFiles.add(object);
            }
        }
        return newFiles;

    }

    public PortableDeviceObject findFile(String name, String path)
            throws COMException {
        PortableDeviceStorageObject storage = getStorage();
        if (storage == null) {
            return null; // this is not a storage device
        }
        PortableDeviceFolderObject folder;
        String lastpartofpath = path.substring(path.lastIndexOf("\\") + 1);
        if (!lastpartofpath.equals("")) {
            folder = MTPUtil.createFolder(path, storage, null, lastpartofpath);

            for (PortableDeviceObject object : folder.getChildObjects()) {
                if (object.getOriginalFileName().equals(name)) {
                    return object;
                }
            }
        } else {
            for (PortableDeviceObject object : storage.getChildObjects()) {
                if (object.getOriginalFileName().equals(name)) {
                    return object;
                }
            }
        }
        return null;
    }

    public void getFile(String objectId, String destPath) throws COMException {
        new PortableDeviceToHostImpl32().copyFromPortableDeviceToHost(objectId,
                destPath, device);
    }

    public boolean deleteFile(String name, String path) throws COMException {
        PortableDeviceObject fileObject = findFile(name, path);
        if (fileObject != null && fileObject.canDelete()) {
            fileObject.delete();
            return true;
        }
        return false;
    }

    public void deleteAllFiles(String path) throws COMException {
        String lastpartofpath = path.substring(path.lastIndexOf("\\") + 1);
        PortableDeviceStorageObject storage = getStorage();

        PortableDeviceFolderObject folder = MTPUtil.createFolder(path, storage,
                null, lastpartofpath);
        for (PortableDeviceObject fileObject : folder.getChildObjects()) {
            if (fileObject != null && fileObject.canDelete()) {
                fileObject.delete();
            }
        }
    }

    public void createFolder(String path) throws COMException {
        String lastpartofpath = path.substring(path.lastIndexOf("\\") + 1);
        PortableDeviceStorageObject storage = getStorage();
        MTPUtil.createFolder(path, storage, null, lastpartofpath);
    }

    public ArrayList<String> getAllFilesByName(String path) {
        ArrayList<String> fileNames = new ArrayList<String>();
        String lastpartofpath = path.substring(path.lastIndexOf("\\") + 1);
        PortableDeviceStorageObject storage = getStorage();

        PortableDeviceFolderObject folder = MTPUtil.createFolder(path, storage,
                null, lastpartofpath);
        for (PortableDeviceObject fileObject : folder.getChildObjects()) {
            if (fileObject != null) {
                fileNames.add(fileObject.getOriginalFileName());
            }
        }
        return fileNames;
    }

    public static PortableDevice[] getDevices() {
        PortableDeviceManager manager = new PortableDeviceManager();
        manager.refreshDeviceList();
        return manager.getDevices();
    }

    private PortableDeviceStorageObject getStorage() {
        if (device.getRootObjects() != null) {
            for (PortableDeviceObject object : device.getRootObjects()) {
                if (object instanceof PortableDeviceStorageObject) {
                    PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
                    return storage;
                }
            }
        }
        return null;
    }
}

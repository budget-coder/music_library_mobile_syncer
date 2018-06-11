package main;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import be.derycke.pieter.com.COMException;
import jmtp.PortableDevice;
import jmtp.PortableDeviceAudioObject;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import util.MTPFileManager;
import util.MTPUtil;

public class TestEnvironment {
    public static void main(final String[] args) {
        System.out.println("Getting device manually");
        PortableDevice[] devices = MTPUtil.getDevices();
        if (devices.length == 0) {
            return;
        }
        PortableDevice mtpDevice = devices[0];
        MTPFileManager mtpFileManager = new MTPFileManager(mtpDevice);
        mtpFileManager.openDevice();
        //devices[0].open(); // TODO Show list to user...
        ArrayList<PortableDeviceStorageObject> deviceStorages = MTPUtil.getDeviceStorages(mtpDevice);
        System.out.println("Now iterating the list of storages...");
        for (PortableDeviceStorageObject storage : deviceStorages) {
            if (!storage.getName().equals("SD-kort")) {
                continue;
            }
            PortableDeviceFolderObject dstFolderDevice =
                    (PortableDeviceFolderObject) MTPUtil.getChildByName(storage, "MusicTEST");
            dstFolderDevice = MTPUtil.getChildByName(dstFolderDevice, "DEEPER");
            if (dstFolderDevice != null) {
                //PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
                //PortableDeviceObject mtpDeviceObject = (PortableDeviceObject) mtpDevice;
                //System.out.println(mtpDeviceObject.getID());
                for (PortableDeviceObject deviceObj : dstFolderDevice.getChildObjects()) {
					System.out.println("File in folder: " + deviceObj.getName());
					if (deviceObj instanceof PortableDeviceAudioObject) {
						PortableDeviceAudioObject audioFile = (PortableDeviceAudioObject) deviceObj;
						LocalDate localDate = audioFile.getReleaseDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
						final int year = localDate.getYear() - 1900;
						System.out.println(
								"Title: " + audioFile.getTitle() + ", Artist: " + audioFile.getArtist() + "\n"
								+ "Album artist: " + audioFile.getAlbumArtist() + ", album: " + audioFile.getAlbum() + "\n"
								+ "Year: " + year + ", Track no.: " + audioFile.getTrackNumber() + "\n"
								+ "Disc_no: ???" + ", Genre: " + audioFile.getGenre() + "\n"
								+ "Composer: " + audioFile.getComposer() + ", Artwork: ???" + "\n"
								+ "Length: " + audioFile.getDuration() + ", TEST: " + audioFile.getUseCount());
						System.out.println("Attempting to acquire album artwork...");
						// Seems to store -1. Possible that biginteger does not work with artwork
						byte[] artwork = audioFile.getArtwork(); // Keeps throwing Win32WPDDefines.ERROR_NOT_FOUND
						System.out.println("Acquired the artwork: " + artwork.toString());
						return;
					} else {
						System.out.println("The file is NOT an audio instance...");
					}
				}
            }
            else {
            	System.out.println("Could not find folder \"MusicTEST\\DEEPER\"");
            }
            break;
        }
        
    }
    
    private void testCopy(PortableDeviceFolderObject dstFolderDevice, MTPFileManager mtpFileManager) {
    	String tempPath = "";
		PortableDeviceFolderObject tempFolder = dstFolderDevice;
		while (tempFolder != null) {
			tempPath = tempFolder.getOriginalFileName() + "\\" + tempPath;
			if (tempFolder.getParent() instanceof PortableDeviceFolderObject) {
				tempFolder = (PortableDeviceFolderObject) tempFolder.getParent();
			} else {
				break;
			}
		}
		tempPath = "Computer\\" + tempPath.substring(0, tempPath.length()-1);
		System.out.println("Copying file to mtp device at path " + tempPath);
		File newMusic = new File("C:/Users/Aram/Overførsler/ACE+ - Into the World of Zanza.mp3");
		if (newMusic.exists() && !newMusic.isDirectory()) {
			try {
				mtpFileManager.addFile(newMusic, tempPath);
			} catch (IOException | COMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Copied.");
		}
    }
    
    private void testRobot() throws InterruptedException {
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        long timeSinceLastAction = System.currentTimeMillis();
        final long seventy_seconds_in_ms = 70000;
        robot.setAutoDelay(0);
        while (true) {
        	if (System.currentTimeMillis() - timeSinceLastAction >= seventy_seconds_in_ms) {
        		//System.out.println("Meow x,y: " + MouseInfo.getPointerInfo().getLocation().x + ", " + MouseInfo.getPointerInfo().getLocation().y);
        		robot.mouseMove(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        		timeSinceLastAction = System.currentTimeMillis();
        	} else {
        		System.out.println("Not meow");
        		Thread.sleep(1000);
        		//robot.mouseMove(2, 2); // This call blocks!
        	}
        }
    }
}
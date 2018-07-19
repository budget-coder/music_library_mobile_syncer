package main;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.ID3v23Frames;

import be.derycke.pieter.com.COMException;
import filesystem.MTPFile;
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
        testLocateAndPrintMusicTags(mtpDevice);
        //testRecursiveFolderLookup(mtpDevice);
        //testCreateFolder(mtpDevice);
        //testGetPCTag();
    }
    
	private static void testGetPCTag() {
		File filemp3 = new File("C:\\Users\\Aram\\Overførsler\\[TEST TEST TEST TEST\\temp-musik\\kalimba-thumbnail.mp3");
    	File filem4a = new File("C:\\Users\\Aram\\Overførsler\\[TEST TEST TEST TEST\\temp-musik\\Hurts - Mercy.m4a");
    	if (filem4a.exists()) {
    		System.out.println("File " + filem4a.getName() + " exists");
    		Tag musicTag;
    		try {
    			MP3File mp3File = (MP3File) AudioFileIO.read(filemp3);
				musicTag = mp3File.getID3v2Tag();
				String duration = musicTag.getFirst(ID3v23Frames.FRAME_ID_V3_LENGTH);
				System.out.println("duration of file is " + duration + " seconds");
				
				
				AudioHeader audioHeaderAll = AudioFileIO.read(filem4a).getAudioHeader();
				
				System.out.println("Or wait! Maybe it is " + mp3File.getAudioHeader().getTrackLength());
				System.out.println("or or, maybe it is " + audioHeaderAll.getTrackLength());
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	} else {
    		System.err.println("File " + filem4a.getName() + " does NOT exist");
    	}
    }
    
    private static void testCreateFolder(PortableDevice mtpDevice) {
    	for (PortableDeviceStorageObject storage : MTPUtil.getDeviceStorages(mtpDevice)) {
    		if (!storage.getName().equals("SD-kort")) {
    			continue;
    		}
    		String path1 = "MusicTEST\\DEEPER";
    		String path2 = "MusicTEST\\DEEPER\\EVEN DEEPER";
    		//System.out.println("lastindexof is " + path1.lastIndexOf(File.separatorChar) + 1);
    		String lastpartofpath = path2.substring(path2.lastIndexOf(File.separatorChar) + 1);
    		if (!lastpartofpath.equals("")) {
    			System.out.println("Lastpartofpath is " + lastpartofpath);
	    		PortableDeviceFolderObject folder = MTPUtil.createFolder(path2, storage, null, lastpartofpath);
	    		
	    		if (folder != null) {
	    			System.out.println("Folder IS NOT null");
	    		} else {
	    			System.out.println("Folder IS null");
	    		}
    		} else {
    			System.err.println("lastpartofpath was empty");
    		}
    	}
    }
    
    private static void testRecursiveFolderLookup(PortableDevice mtpDevice) {
    	ArrayList<PortableDeviceStorageObject> deviceStorages = MTPUtil.getDeviceStorages(mtpDevice);
    	for (PortableDeviceStorageObject storage : deviceStorages) {
            if (!storage.getName().equals("SD-kort")) {
                continue;
            }
            String musicPath = "MusicTEST/DEEPER/EVEN DEEPER/ACE+ - Into the World of Zanza.mp3";
            String folderPath = "MusicTEST/DEEPER/EVEN DEEPER";
            PortableDeviceObject dstFolderDevice = MTPUtil.getChildFileByNameRecursively(storage, folderPath);
            PortableDeviceObject dstFileDevice = MTPUtil.getChildFileByNameRecursively(storage, musicPath);
            if (dstFolderDevice != null) {
            	System.out.println("Path " + folderPath + " EXISTED! Got file " + dstFolderDevice.getOriginalFileName());
            	MTPFile test = new MTPFile(storage, folderPath);
            	System.out.println("test is directory? " + test.isDirectory() );
            } else {
            	System.err.println("Path " + folderPath + " did NOT exist!");
            }
            // Next file
            if (dstFileDevice != null) {
            	System.out.println("Path " + musicPath + " EXISTED! Got file " + dstFileDevice.getOriginalFileName());
            	MTPFile test = new MTPFile(storage, musicPath);
            	System.out.println("test is directory? " + test.isDirectory() );
            } else {
            	System.err.println("Path " + musicPath + " did NOT exist!");
            }
    	}
    }
    
    private static void testLocateAndPrintMusicTags(PortableDevice mtpDevice) {
    	ArrayList<PortableDeviceStorageObject> deviceStorages = MTPUtil.getDeviceStorages(mtpDevice);
        System.out.println("Now iterating the list of storages...");
        for (PortableDeviceStorageObject storage : deviceStorages) {
            if (!storage.getName().equals("SD-kort")) {
                continue;
            }
            PortableDeviceFolderObject dstFolderDevice =
                    (PortableDeviceFolderObject) MTPUtil.getChildFileByName(storage, "MusicTEST");
            dstFolderDevice = MTPUtil.getChildFolderByName(dstFolderDevice, "DEEPER");
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
						/*
						System.out.println("Attempting to acquire album artwork...");
						// Seems to store -1. Possible that biginteger does not work with artwork
						byte[] artwork = audioFile.getArtwork(); // Keeps throwing Win32WPDDefines.ERROR_NOT_FOUND
						System.out.println("Acquired the artwork: " + artwork.toString());
						*/
						System.out.println();
						System.out.println();
						((PortableDeviceAudioObject) deviceObj).setArtist("bitch");
						//audioFile.setArtist("get nop'ed");
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
    
    private static void testCopy(PortableDeviceFolderObject dstFolderDevice, MTPFileManager mtpFileManager) {
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
    
    private static void testRobot() throws InterruptedException {
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
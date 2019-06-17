package lejos.nxt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import lejos.nxt.remote.FileInfo;
import lejos.nxt.remote.NXTCommand;
import lejos.pc.comm.NXTCommandConnector;

/**
 * Support for remote file operations
 * 
 * @author Brian Bagnall
 *
 */
public class FileSystem {
	
	//TODO this class is somewhat similar to lejos.nxt.remote.RemoteNXT, merge
	
	private static final NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();
		
	// Make sure no one tries to instantiate this.
	private FileSystem() {}
	
	// Consider using String instead of File?
	public static byte upload(File localSource) {
		// FIRST get data from file
		byte [] data;
		byte success;
		try {
			FileInputStream in = new FileInputStream(localSource);
			//TODO don't load file into memory
			data = new byte[(int) localSource.length()];
			//TODO respect InputStream semantics or use DataInputStream.readFully()
			in.read(data);
			in.close();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
		
		// Now send the data to the NXT
		try {
			byte handle = nxtCommand.openWrite(localSource.getName(), data.length);
			success = nxtCommand.writeFile(handle, data, 0, data.length);
			nxtCommand.closeFile(handle);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
				
		return success;
	}
	
	/**
	 * 
	 * @param fileName The name of the file on the NXT, including filename extension.
	 * @return The file data, as an array of bytes. If there is a problem, the array will
	 * contain one byte with the error code.
	 */
	public static byte [] download(String fileName) {
		byte [] data;
		
		try {
			FileInfo finfo = nxtCommand.openRead(fileName);
			data = new byte[finfo.fileSize];
			nxtCommand.readFile(finfo.fileHandle, data, 0, finfo.fileSize);
			nxtCommand.closeFile(finfo.fileHandle);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return null;
		}
		return data;
	}
	
	/**
	 * Download a file from the NXT and save it to a file.
	 * @param fileName
	 * @param destination Where the file will be saved. Can be directory or
	 * full path and filename.
	 * @return Error code.
	 */
	public static byte download(String fileName, File destination) {
		byte [] data = download(fileName);
		File fullFile;
		if(destination.isDirectory())
			fullFile = new File(destination.toString() + File.separator + fileName); 
		else
			fullFile = destination;
		try {
			
			if(fullFile.createNewFile()) {
				FileOutputStream out = new FileOutputStream(fullFile);
				out.write(data);
				out.close();
			}
		} catch (IOException e) {
			System.out.println("File write failed");
			return -1;
		}
		return 0;
	}
	
	/**
	 * Download a file from the NXT and save it to a local directory.
	 * @param fileName
	 * @param destination Where the file will be saved. Can be directory or
	 * full path and filename. e.g. "c:/Documents/Lego Sounds/Sir.rso"
	 * @return Error code.
	 */
	public static byte download(String fileName, String destination) {
		File file = new File(destination);
		return download(fileName, file);
	}
	
	/**
	 * Delete a file from the NXT.
	 * @param fileName
	 * @return 0 = success
	 */
	public static byte delete(String fileName) {
		try {
			return nxtCommand.delete(fileName);
		} catch (IOException ioe) {
			return -1;
		}	
	}
	
	/**
	 * Returns a list of all files on NXT brick.
	 * @return An array on file names, or NULL if no files found.
	 */
	public static String [] getFileNames() {
		return getFileNames("*.*");
	}
	
	/**
	 * Returns a list of files on NXT brick.
	 * @param searchCriteria "*.*" or [FileName].* or or *.[Extension] or [FileName].[Extension]
	 * @return An array on file names, or NULL if nothing found.
	 */
	// This method could provide file sizes by returning FileInfo objects
	// instead. It's simpler for users to return fileNames.
	public static String [] getFileNames(String searchCriteria) {
		try {
			ArrayList<String> names = new ArrayList<String>(1);
			FileInfo f = nxtCommand.findFirst(searchCriteria);
			if(f == null)
				return null;
			do {
				names.add(f.fileName);
				if(f != null)
					// TODO this close is executed after every findFirst/Next which is likely to be wrong!
					nxtCommand.closeFile(f.fileHandle); // According to protocol, must be closed when done with it.
				f = nxtCommand.findNext(f.fileHandle);
			} while (f != null);
			
			String [] returnArray = new String [1];
			return (String [])names.toArray(returnArray);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return null;
		}
	}
	
	/**
	 * Retrieves the file name of the Lego executable currently running on the NXT.
	 * @return the status
	 */
	public static String getCurrentProgramName() {
		try {
			return nxtCommand.getCurrentProgramName();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return null;
		}
	}
	
	/**
	 * Starts a Lego executable file on the NXT.
	 * @param fileName
	 * @return the status
	 */
	public static byte startProgram(String fileName) {
		try {
			return nxtCommand.startProgram(fileName);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
	/**
	 * Stops the currently running Lego executable on the NXT.
	 * @return the status
	 */
	public static byte stopProgram() {
		try {
			return nxtCommand.stopProgram();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
}

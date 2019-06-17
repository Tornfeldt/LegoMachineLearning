package lejos.nxt.remote;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Structure that gives information about a leJOS NXJ file.
 *
 */
public class FileInfo {
	
	/**
	 * The name of the file - up to 20 characters.
	 */
	public String fileName;
	
	/**
	 * The handle for accessing the file.
	 */
	public byte fileHandle;
	
	/**
	 * The size of the file in bytes.
	 */
	public int fileSize;
	
	public FileInfo(String fileName) {
		this.fileName = fileName;
	}	
}

package lejos.pc.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * This class replaces System.out etc. with reasonable alternatives.
 * 
 * @author skoehler
 * @deprecated will be replaced
 */
@Deprecated
public class SystemContext
{
	public static final PrintWriter err;
	public static final PrintWriter out;
	public static final BufferedReader in;
	private static String nxjHome;
	private static Writer delErr;
	private static Writer delOut;
	private static Reader delIn;
	
	static {
		nxjHome = System.getProperty("nxj.home");
		delErr = new OutputStreamWriter(System.err);
		delOut = new OutputStreamWriter(System.out);
		delIn = new InputStreamReader(System.in);

		err = new PrintWriter(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				synchronized (lock)	{
					delErr.write(cbuf, off, len);
				}
			}
			
			@Override
			public void write(int c) throws IOException {
				synchronized (lock) {
					delErr.write(c);
				}
			}
			
			@Override
			public void flush() throws IOException {
				synchronized (lock)	{
					delErr.flush();
				}
			}
			
			@Override
			public void close() throws IOException {
				// do nothing
			}
		}, true);
		
		out = new PrintWriter(new Writer() {
			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				synchronized (lock)	{
					delOut.write(cbuf, off, len);
				}
			}
			
			@Override
			public void write(int c) throws IOException {
				synchronized (lock) {
					delOut.write(c);
				}
			}
			
			@Override
			public void flush() throws IOException {
				synchronized (lock)	{
					delOut.flush();
				}
			}
			
			@Override
			public void close() throws IOException {
				// do nothing
			}
		}, true);
		
		in = new BufferedReader(new Reader() {			
			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				synchronized (lock) {
					return delIn.read(cbuf, off, len);
				}
			}
			
			@Override
			public int read() throws IOException {
				synchronized (lock) {
					return delIn.read();
				}
			}
			
			@Override
			public void close() throws IOException {
				// do nothing
			}
		});
	}

	public static void setIn(Reader r)
	{
		delIn = r;
	}
	
	public static void setOut(Writer w)
	{
		delOut = w;
	}
	
	public static void setErr(Writer w)
	{
		delErr = w;
	}
	
	public static void setNxjHome(String path)
	{
		nxjHome = path;
	}

	public static String getNxjHome()
	{
		return nxjHome;
	}
	
	
}

/* $Id: OSInfo.java,v 1.2 2006/04/08 18:03:12 sven Exp $
 * Created on 02.10.2004
 */
package lejos.internal.jni;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Performs normalization of os.name and os.arch system properties.
 */
public class OSInfo
{
	public static final String ARCH_X86 = "x86";
	public static final String ARCH_X86_64 = "x86_64";
	public static final String ARCH_PPC = "ppc";
	public static final String ARCH_PPC64 = "ppc64";
	public static final String ARCH_SPARC = "sparc";
	public static final String ARCH_PA_RISK = "pa_risk";
	public static final String ARCH_ARM = "arm";

	public static final String OS_WINDOWS_CE = "windows_ce";
	public static final String OS_WINDOWS = "windows";
	public static final String OS_LINUX = "linux";
	public static final String OS_MACOS = "macos";
	public static final String OS_MACOSX = "macosx";
	public static final String OS_SOLARIS = "solaris";
	
	public static final String UNKNOWN = "unknown";

	private static final String PREFIX_OS = "os.";
	private static final String PREFIX_ARCH = "arch.";
	
	private static Map<?, ?> loadProperties() throws IOException
	{
		StringReader s = new StringReader("# For more info: http://lopica.sourceforge.net/os.html\n" +
				"#\n" +
				"# All values are regular expressions, case insensitive. \n" +
				"# Regular expressions must not overlap!\n" +
				"\n" +
				"arch.x86 = i\\\\d86\n" +
				"arch.x86_64 = amd64\n" +
				"arch.ppc = powerpc\n" +
				"arch.arm = armv41\n" +
				"arch.pa_risk = pa-risk\n" +
				"\n" +
				"os.windows_ce = windows ce\n" +
				"os.windows = windows (?!ce).*\n" +
				"os.linux = \n" +
				"os.macos = mac os\n" +
				"os.macosx = mac os x\n" +
				"os.solaris = sunos\n");

		try
		{
			Properties r = new Properties();

			r.load(s);
			return r;
		}
		finally
		{
			s.close();
		}
	}
	
	private static String probe(Map<?, ?> p, String prefix, String value)
	{
		value = value.toLowerCase();
		for (Map.Entry<?, ?> e : p.entrySet())
		{
			String key = e.getKey().toString();
			String pattern = e.getValue().toString();
			if (key.startsWith(prefix) && value.matches(pattern))
			{
				return key.substring(prefix.length());
			}
		}
		return value;
	}

	private static String getDefaultOS()
	{
		return System.getProperty("os.name", UNKNOWN);
	}

	private static String getDefaultArch()
	{
		return System.getProperty("os.arch", UNKNOWN);
	}

	private static int getDefaultModel()
	{
		try
		{
			return Integer.parseInt(System.getProperty("sun.arch.data.model", "0"));
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

	private final String os;
	private final String arch;

	/**
	 * Creates OSInfo-Object with Infos about the current System according to the
	 * System-properties.
	 * @throws IOException 
	 */
	public OSInfo() throws IOException
	{
		this(getDefaultOS(), getDefaultArch(), getDefaultModel());
	}

	/**
	 * @param os
	 * @param arch
	 * @param datamodel
	 * @throws IOException 
	 */
	public OSInfo(String os, String arch, int datamodel) throws IOException
	{
		Map<?, ?> alias = loadProperties();
		this.os = probe(alias, PREFIX_OS, os);
		this.arch = probe(alias, PREFIX_ARCH, arch);
	}

	/**
	 * Get the name of the OS.
	 * @return the name of the OS
	 */
	public String getOS()
	{
		return this.os;
	}

	/**
	 * Get the name of the architecture.
	 * @return the name of the architecture
	 */
	public String getArch()
	{
		return this.arch;
	}

	public boolean isOS(String name)
	{
		return this.os.equalsIgnoreCase(name);
	}

	public boolean isArch(String name)
	{
		return this.arch.equalsIgnoreCase(name);
	}
	
	
	/**
	 * @throws IOException 
	 * @deprecated don't call it, only used for debugging/user assistance
	 */
	@Deprecated
	public static void main(String[] args) throws IOException
	{
		/*
		 * This method is invoked from inside the build.xml that we provide
		 * our users with for building the native libs on Linux.
		 */
		OSInfo os = new OSInfo();
		System.out.println(os.getOS()+"/"+os.getArch());
	}
}
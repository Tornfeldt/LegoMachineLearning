package lejos.pc.comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class FlashWrite
{
	public static byte[] loadCode() throws IOException
	{
		Class<?> c = FlashWrite.class;		
		URL u = c.getResource(c.getSimpleName()+".bin");
		
		InputStream is = u.openStream();
		try
		{
			byte[] buf = new byte[4096];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			while (true)
			{
				int len = is.read(buf, 0, buf.length);
				if (len < 0)
					break;
				
				os.write(buf, 0, len);
			}
			return os.toByteArray();
		}
		finally
		{
			is.close();
		}
	}
}

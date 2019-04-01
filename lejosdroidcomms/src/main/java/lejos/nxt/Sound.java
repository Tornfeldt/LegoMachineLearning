package lejos.nxt;

import lejos.pc.comm.*;
import lejos.util.Delay;

import java.io.*;
import lejos.nxt.remote.*;

/**
 * Sound class.
 * Usage: SoundSensor.playTone(500, 1000);
 * 
 * This version of the Sound class supports remote execution.
 * 
 * @author <a href="mailto:bbagnall@mts.net">Brian Bagnall</a>
 */
public class Sound {	
	private static final NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();
	
	// Make sure no one tries to instantiate this.
	private Sound() {}
	
    public static int C2 = 523;
	
    /**
     * Play a system sound.
     * <TABLE BORDER=1>
     * <TR><TH>aCode</TH><TH>Resulting Sound</TH></TR>
     * <TR><TD>0</TD><TD>short beep</TD></TR>
     * <TR><TD>1</TD><TD>double beep</TD></TR>
     * <TR><TD>2</TD><TD>descending arpeggio</TD></TR>
     * <TR><TD>3</TD><TD>ascending  arpeggio</TD></TR>
     * <TR><TD>4</TD><TD>long, low buzz</TD></TR>
     * </TABLE>
     */
    public static void systemSound(boolean aQueued, int aCode)
    {
        if (aCode == 0)
            playTone(600, 200);
        else if (aCode == 1)
        {
            playTone(600, 150);
            pause(200);
            playTone(600, 150);
            pause(150);
        }
        else if (aCode == 2)// C major arpeggio
            for (int i = 4; i < 8; i++)
            {
                playTone(C2 * i / 4, 100);
                pause(100);
            }
        else if (aCode == 3)
            for (int i = 7; i > 3; i--)
            {
                playTone(C2 * i / 4, 100);
                pause(100);
            }
        else if (aCode == 4)
        {
            playTone(100, 500);
            pause(500);
        }
    }
    

    /**
     * Beeps once.
     */
    public static void beep()
    {
        systemSound(true, 0);
    }

    /**
     * Beeps twice.
     */
    public static void twoBeeps()
    {
        systemSound(true, 1);
    }

    /**
     * Downward tones.
     */
    public static void beepSequence()
    {
        systemSound(true, 3);
    }

    /**
     * Upward tones.
     */
    public static void beepSequenceUp()
    {
        systemSound(true, 2);
    }

    /**
     * Low buzz 
     */
    public static void buzz()
    {
        systemSound(true, 4);
    }
	
	public static void playTone(int frequency, int duration) {
		try {
			nxtCommand.playTone(frequency, duration);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
	
	/**
	 * Plays a sound file from the NXT. SoundSensor files use the 
	 * .rso extension. The filename is not case sensitive.
	 * Filenames on the NXT Bricks display do now show the filename extension.
	 * @param fileName e.g. "Woops.rso"
	 * @param repeat true = repeat, false = play once.
	 * @return If you receive a non-zero number, the filename is probably wrong
	 * or the file is not uploaded to the NXT brick.
	 */
	public static byte playSoundFile(String fileName, boolean repeat) {
		try {
			return nxtCommand.playSoundFile(fileName, repeat);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
	/**
	 * Plays a sound file once from the NXT. SoundSensor files use the 
	 * .rso extension. The filename is not case sensitive.
	 * Filenames on the NXT Bricks display do now show the filename extension.
	 * @param fileName e.g. "Woops.rso"
	 * @return If you receive a non-zero number, the filename is probably wrong
	 * or the file is not uploaded to the NXT brick.
	 */
	public static byte playSoundFile(String fileName) {
		return Sound.playSoundFile(fileName, false);
	}
		
	/**
	 * Stops a sound file that has been playing/repeating.
	 * @return Error code.
	 */
	public static int stopSoundPlayback() {
		try {
			return nxtCommand.stopSoundPlayback();
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
    public static void pause(int t)
    {
        Delay.msDelay(t);
    }  
}

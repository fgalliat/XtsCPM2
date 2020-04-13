package j80.spectrum;

/*
 * Audio support for Jasper
 * Author: Jan Bobrowski <jb@wizard.ae.krakow.pl>
 * License: GPL
 */

import com.softsynth.javasonics.*; 
import com.softsynth.javasonics.core.*;

class JavaSonicsAudio extends Audio {

	private AudioOutputStream line;

	public JavaSonicsAudio() throws Exception {
		if(!SonicSystem.isNativeSupported())
			throw new Exception();
		try {
			AudioDevice device = SonicSystem.getDevice();
//			line = device.getOutputStream( frameRate, numChannels, bitsPerSample );
			line = device.getOutputStream(8000, 1, 8);
			line.open(); 
			line.start();
		} catch (DeviceUnavailableException e) {
			System.err.println(e.toString());
			throw new Exception();
		}
	}

	public void play(byte[] buf, int len) {
		short[] t = new short[len];
		for(int i=0; i<len; i++) t[i] = (short)(256*buf[i]); // we must recode :-(
		line.write(t, 0, len);
	}

	public void setVolume(double vol) {
		super.setVolume(vol);
		xlat = new byte[div+1];
		vol *= 255;
		for(int i=0; i<=div; i++)
			xlat[i] = (byte)(vol*i/div - 128);
	}
}

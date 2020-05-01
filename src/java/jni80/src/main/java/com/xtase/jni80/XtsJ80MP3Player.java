/**
 * Code modified by XTase - fgalliat @ May 2020
 * 
 * part of JNI80 project to Emulate DFPlayer
 * 
 */

/***************************************************************************
 *  JLayerME is a JAVA library that decodes/plays/converts MPEG 1/2 Layer 3.
 *  Project Homepage: http://www.javazoom.net/javalayer/javalayerme.html.
 *  Copyright (C) JavaZOOM 1999-2005.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *---------------------------------------------------------------------------
 */
package com.xtase.jni80;

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;
import javazoom.jlme.decoder.Decoder;
import javazoom.jlme.decoder.Header;
import javazoom.jlme.decoder.SampleBuffer;
import javazoom.jlme.decoder.BitStream;

public class XtsJ80MP3Player {
    private static Decoder decoder;
    private static SourceDataLine line;
    private BitStream bitstream;

    // private boolean playable = true;
    private boolean playable = false;
    private boolean inPause = false;
    // protected boolean endOfPlay = false;
    protected boolean endOfPlay = true;

    protected int curTrkNum = 1;

    public XtsJ80MP3Player() {
    }

    protected static void startOutput(AudioFormat playFormat) throws LineUnavailableException {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, playFormat);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("sorry, the sound format cannot be played");
        }
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(playFormat);
        line.start();
    }

    protected static void stopOutput() {
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
            line = null;
        }
    }

    public static void main(String args[]) throws Exception {
        if (args.length > 0) {
            String file = args[0];
            new XtsJ80MP3Player().playTrack(Integer.parseInt(file));
        } else {
            throw new IllegalArgumentException("waiting a trackNum [1..999]");
        }
    }

    public void playTrack(int trckNum) throws Exception {
        if (trckNum < 1 || trckNum > 999) {
            trckNum = 1;
        }

        File mp3Root = new File("/vm_mnt/MP3");
        if (!mp3Root.exists()) {
            throw new IllegalArgumentException("Missing the MP3 root (" + mp3Root.getPath() + ")");
        }

        if (trckNum > mp3Root.list().length) {
            trckNum = 1;
        }

        try {
            String numTrack = null;
            String mp3file = null;
            try {
                numTrack = "" + trckNum;
                if (numTrack.length() == 1) {
                    numTrack = "0" + numTrack;
                }
                if (numTrack.length() == 2) {
                    numTrack = "0" + numTrack;
                }

                File[] files = mp3Root.listFiles();
                File found = null;
                for (File f : files) {
                    if (f.isFile() && f.getName().startsWith(numTrack)) {
                        found = f;
                        break;
                    }
                }
                mp3file = found.getAbsolutePath();

                curTrkNum = trckNum;

            } catch (Exception ex) {
                throw ex;
            }

            // System.out.println(numTrack);
            // System.out.println(mp3file);

            bitstream = new BitStream(new BufferedInputStream(new FileInputStream(mp3file), 2048));

            System.out.println("starting (" + mp3file + ")");
            
            new Thread() {
                public void run() {
                    try {
                        play();
                    } catch(Exception ex) {
                        System.out.println("(!!) "+ex);
                    }
                }
            }.start();
            
            System.out.println("ending");

        } catch (Exception e) {
            System.err.println("couldn't locate the mp3 file");
        }
    }

    public void play() throws Exception {
        endOfPlay = false;

        inPause = false;
        playable = true;
        boolean first = true;
        int length;
        Header header = bitstream.readFrame();
        decoder = new Decoder(header, bitstream);
        while (playable) {

            if (inPause) {
                delay(200);
                continue;
            }

            try {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame();
                length = output.size();
                if (length == 0)
                    break;
                // {
                if (first) {
                    first = false;
                    System.out.println("frequency: " + decoder.getOutputFrequency() + ", channels: "
                            + decoder.getOutputChannels());
                    startOutput(new AudioFormat(decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true,
                            false));
                }
                line.write(output.getBuffer(), 0, length);
                bitstream.closeFrame();
                header = bitstream.readFrame();
                // System.out.println("Mem:"+(rt.totalMemory() -
                // rt.freeMemory())+"/"+rt.totalMemory());
                // }
            } catch (Exception e) {
                // e.printStackTrace();
                break;
            }
        }
        playable = false;
        stopOutput();
        bitstream.close();

        endOfPlay = true;
    }

    public void stop() {
        playable = false;
        while (!endOfPlay) {
            delay(50);
        }
    }

    public void pause() {
        inPause = !inPause;
    }

    public void next() throws Exception {
        curTrkNum++;
        stop();
        playTrack(curTrkNum);
    }

    public void prev() throws Exception {
        curTrkNum--;
        stop();
        playTrack(curTrkNum);
    }

    public boolean isPlaying() {
        return playable && !inPause;
    }

    protected void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
        }
    }

}
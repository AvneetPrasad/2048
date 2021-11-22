package gameLogic;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * This class contain audio handling that enables audio to be used during the program 
 * create a loop of audio that would play during the whole game
 * 
 * @author kayle and avneet
 */
public class AudioHandler {

	private static AudioHandler handler;   
	private HashMap<String, Clip> sounds;  // creating a hashmap called sounds

	private AudioHandler() {
		sounds = new HashMap<String, Clip>();  
	}

	public static AudioHandler getInstance() {
		if (handler == null) {          
			handler = new AudioHandler();
		}
		return handler;
	}

        /**
         *  load the game file once the program has been closed 
         * @param resourcePath
         * @param name 
         */
	public void load(String resourcePath, String name) {
		// Establish path to file
		URL resource = AudioHandler.class.getClassLoader().getResource(resourcePath);

		// Get the audio input from the file and base format
		AudioInputStream input = null;
		try {   //try to aquire the audio file 
			input = AudioSystem.getAudioInputStream(resource);
                        // catch if audio couldn't get aquired or print 
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		AudioFormat baseFormat = input.getFormat();
                
		// if encoding the audio file 
		if(baseFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED){
			try{    // try attach audio system to the hashmap
				Clip c = AudioSystem.getClip();
				c.open(input);
				sounds.put(name, c);
				return;
			
                        }
			catch(Exception e){  //print exception 
				e.printStackTrace();
			}
		}
		
		// Decoding audio format
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

		// Create new input stream for new format
		AudioInputStream decodedIn = AudioSystem.getAudioInputStream(decodedFormat, input);

		Clip c = null;
		try {
		    c = AudioSystem.getClip();
			c.open(decodedIn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		sounds.put(name, c);
	}
        /**
         * once the game is playing loop audio
         * @param name
         * @param loopCount 
         */
	public void play(String name, int loopCount) {
		if(sounds.get(name).isRunning()){
			sounds.get(name).stop();
		}
		sounds.get(name).setFramePosition(0);
		sounds.get(name).loop(loopCount);
	}
	/**
         * adjusting the volume of the audio file
         * @param name
         * @param value 
         */
	public void adjustVolume(String name, int value){
		FloatControl control = (FloatControl)sounds.get(name).getControl(FloatControl.Type.MASTER_GAIN);
		control.setValue(value);
	}
}
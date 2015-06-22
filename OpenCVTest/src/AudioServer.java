
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JFrame;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;

public class AudioServer extends Thread {
	private ServerSocket mServer;
	CanvasFrame canvas = new CanvasFrame("Web Cam");
	public static TargetDataLine microphone;
	public static TargetDataLine line;
    public static SourceDataLine speakers;
    
	// path of the wav file
    File wavFile = new File("/Users/eyzhou/Desktop/" + "audio.wav");

	public AudioServer() {
		canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		System.out.println("server's waiting");
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		Socket socket = null;
		ByteArrayOutputStream byteArray = null;
		try {
			mServer = new ServerSocket(8080);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(SocketServer.server_is_running) {
		try {
			if (byteArray != null)
				byteArray.reset();
			else
				byteArray = new ByteArrayOutputStream();

			socket = mServer.accept();
			System.out.println("new audio socket");

			inputStream = new BufferedInputStream(socket.getInputStream());
			outputStream = new BufferedOutputStream(socket.getOutputStream());

		
			AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
		    
		    try {
		        microphone = AudioSystem.getTargetDataLine(format);

		        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
		        microphone = (TargetDataLine) AudioSystem.getLine(info);
		        microphone.open(format);

		        ByteArrayOutputStream out = new ByteArrayOutputStream();
		        int numBytesRead;
		        int CHUNK_SIZE = 1024;
		        byte[] data = new byte[microphone.getBufferSize() / 5];
		        microphone.start();
		        
		        line = (TargetDataLine) AudioSystem.getLine(info);
		        line.open(format);
		        line.start();
		        
		        Thread recording_thread = new Thread(new Runnable() {
		            public void run() {
		            	// Write data to saved wav file
						AudioInputStream ais = new AudioInputStream(line);
						try {
							AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
		       });  
		       recording_thread.start();
		        
		        int bytesRead = 0;
		        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
		        speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
		        speakers.open(format);
		        speakers.start();
		        
		    
		        
		        while (SocketServer.server_is_running) { 
		            numBytesRead = microphone.read(data, 0, CHUNK_SIZE);
		            bytesRead += numBytesRead;
		            
		            // send audio byte[] data
		            outputStream.write(intToBytes(numBytesRead));
		            outputStream.write(data);
					outputStream.flush();
					
					if (Thread.currentThread().isInterrupted())
					{
						System.out.println("thread interrupted");
						break;
					}

				     
		            // Write data to speakers stream for immediate play-back
		            speakers.write(data, 0, numBytesRead);
		        }
		        
		     
		        
		    } catch (LineUnavailableException e) {
		        e.printStackTrace();
		    } 

			outputStream.close();
			inputStream.close();


		} catch (IOException e) {
//			System.out.println("closing mic");
//	        speakers.drain();
//	        speakers.close();
//	        microphone.close();
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} finally {
//			try {
//				if (outputStream != null) {
//					System.out.println("closed");
//					outputStream.close();
//					outputStream = null;
//				}
//
//				if (inputStream != null) {
//					inputStream.close();
//					inputStream = null;
//				}
//
//				if (socket != null) {
//					socket.close();
//					socket = null;
//				}
//
//				if (byteArray != null) {
//					byteArray.close();
//				}
//
//			} catch (IOException e) {
//
//			}

		}
	}
		
		System.out.println("closing mic");
        speakers.drain();
        speakers.close();
        microphone.stop();
        microphone.close();
        
        System.out.println("Audio thread ended");
		return;

	}
	
	public static byte[] intToBytes(int yourInt) throws IOException {
		return ByteBuffer.allocate(4).putInt(yourInt).array();
	}
	


}

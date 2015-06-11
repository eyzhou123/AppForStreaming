
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
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
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class AudioServer extends Thread {
	private ServerSocket mServer;
	CanvasFrame canvas = new CanvasFrame("Web Cam");


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
			while (true) {
				if (byteArray != null)
					byteArray.reset();
				else
					byteArray = new ByteArrayOutputStream();

				socket = mServer.accept();
				System.out.println("new socket");

				inputStream = new BufferedInputStream(socket.getInputStream());
				outputStream = new BufferedOutputStream(socket.getOutputStream());

//////////////				
				AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
			    TargetDataLine microphone;
			    SourceDataLine speakers;
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

			        int bytesRead = 0;
			        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
			        speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
			        speakers.open(format);
			        speakers.start();
			        while (true) { // temporary test
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
			            //out.write(data, 0, numBytesRead); 
						
			            // Write data to speakers stream for immediate play-back
			            speakers.write(data, 0, numBytesRead);
			        }
			        speakers.drain();
			        speakers.close();
			        microphone.close();
			    } catch (LineUnavailableException e) {
			        e.printStackTrace();
			    } 
/////////////////
//
//				JsonObject jsonObj = new JsonObject();
//				jsonObj.addProperty("type", "data");
//				jsonObj.addProperty("length", frame_length);
//				jsonObj.addProperty("width", grabber.getImageWidth());
//				jsonObj.addProperty("height", grabber.getImageHeight());
//
//				// send jsonObj first
//				outputStream.write(jsonObj.toString().getBytes());
//				outputStream.flush();
//
//				byte[] buff = new byte[256];
//				int len = 0;
//				String msg = null;
//
//				while ((len = inputStream.read(buff)) != -1) {
//					msg = new String(buff, 0, len);
//
//					// JSON analysis
//					JsonParser parser = new JsonParser();
//					boolean isJSON = true;
//					JsonElement element = null;
//					try {
//						element =  parser.parse(msg);
//					}
//					catch (JsonParseException e) {
//						//Log.e(TAG, "exception: " + e);
//						isJSON = false;
//					}
//					if (isJSON && element != null) {
//						JsonObject obj = element.getAsJsonObject();
//						element = obj.get("state");
//						if (element != null && element.getAsString().equals("ok")) {
//
//
//							IplImage img = null;
//							BufferedImage buff_img = null;
//
//							// send data
//							// use compressed JPG format for speed
//							// need to send the byte size first (changes every time)
//							while (true) {
//								try {
//									img = grabber.grab();
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//								if (img != null) {
//									canvas.showImage(img);
//									buff_img = img.getBufferedImage();
//
//									ByteArrayOutputStream baos = new ByteArrayOutputStream();
//									ImageIO.write(buff_img, "jpg", baos);
//									byte[] bytes = baos.toByteArray();
////									
//									//System.out.println("bytes length: " + bytes.length);
//									
//									outputStream.write(intToBytes(bytes.length));
//									outputStream.write(bytes); 
//									outputStream.flush();
//
//									if (Thread.currentThread().isInterrupted())
//									{
//										System.out.println("??");
//										break;
//									}
//								}
//								else {
//									System.out.println(":(");
//								}
//							}
//
//							break;
//						}
//					}
//					else {
//						break;
//					}
//				}

				outputStream.close();
				inputStream.close();

			}

		} catch (IOException e) {
			System.out.println("exception");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					System.out.println("closed");
					outputStream.close();
					outputStream = null;
				}

				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}

				if (socket != null) {
					socket.close();
					socket = null;
				}

				if (byteArray != null) {
					byteArray.close();
				}

			} catch (IOException e) {

			}

		}

	}
	
	public static byte[] intToBytes(int yourInt) throws IOException {
		return ByteBuffer.allocate(4).putInt(yourInt).array();
	}
	


}

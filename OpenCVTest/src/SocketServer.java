
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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameRecorder;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_AAC;
import static org.bytedeco.javacpp.avcodec.AV_CODEC_ID_H264;

public class SocketServer extends Thread {
	private ServerSocket mServer;
	CanvasFrame canvas = new CanvasFrame("Web Cam");
	Date date;
	private boolean recording = false;
	//private FrameRecorder recorder = null;
	public static FFmpegFrameRecorder recorder = null;
	
	String path = "/Users/eyzhou/Desktop/";
	
	public SocketServer() {
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
			mServer = new ServerSocket(8888);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
//		Loader.load(opencv_objdetect.class);
		
		date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
		String timestamp = sdf.format(date);
		
	
//		try {
//			recorder = FrameRecorder.createDefault(path + timestamp + ".mpg", 
//					320, 700);
//		} catch (org.bytedeco.javacv.FrameRecorder.Exception e3) {
//			// TODO Auto-generated catch block
//			e3.printStackTrace();
//		}
		FrameGrabber grabber = new OpenCVFrameGrabber(0);
		recorder = new FFmpegFrameRecorder(path+timestamp+".mp4", 
				700, 600);
		
//		recorder.setCodecID(CODEC_ID_MPEG1VIDEO);
//		recorder.setVideoCodec(CV_FOURCC((byte)'I',(byte)'Y',(byte) 'U',(byte) 'V'));
//		recorder.setVideoCodec(CV_FOURCC((byte)'M',(byte)'J',(byte)'P',(byte)'G'));
//		recorder.setBitrate(10 * 1024 * 1024);
//		recorder.setVideoCodec(CV_FOURCC_DEFAULT);
		
		recorder.setVideoCodec(AV_CODEC_ID_H264);
		recorder.setFrameRate(15);
		recorder.setFormat("mp4"); 
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        recorder.setSampleFormat(grabber.getSampleFormat());
        recorder.setSampleRate(grabber.getSampleRate()); 
        recorder.setAudioCodec(AV_CODEC_ID_AAC);
		
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//        recorder.setFormat("mp4");
//        recorder.setFrameRate(grabber.getFrameRate());
//        recorder.setSampleFormat(grabber.getSampleFormat());
//        recorder.setSampleRate(grabber.getSampleRate()); 
		
		
		while(true) {
		try {
			while (true) {
				if (byteArray != null)
					byteArray.reset();
				else
					byteArray = new ByteArrayOutputStream();

				socket = mServer.accept();
				System.out.println("new socket");

				if (!recording) {
			        try {
						recorder.start();
						recording = true;
						System.out.println("Recording video");
					} catch (org.bytedeco.javacv.FrameRecorder.Exception e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
				
				inputStream = new BufferedInputStream(socket.getInputStream());
				outputStream = new BufferedOutputStream(socket.getOutputStream());

				canvas.setCanvasSize(600, 480);
				
				//FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(path+timestamp+"");
				// minimum: 640x480 ?
				grabber.setImageWidth(700);
				grabber.setImageHeight(600);
				
				
				OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
				Java2DFrameConverter javaconverter = new Java2DFrameConverter(); 
				
				int i=0;
				int frame_length = 0;
				try {
					grabber.start();
				} catch (Exception e) {
					System.out.println("exception from starting grabber");
					
				}
				IplImage initial_frame = null;
				Frame initial_f = null;
				while(frame_length == 0) {
					try {

						//initial_frame = grabber.grab();
						initial_f = grabber.grab();
						initial_frame = converter.convert(initial_f);

						if(initial_frame != null) {
							BufferedImage initialBufferImage = javaconverter.convert(initial_f);

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(initialBufferImage, "bmp", baos);
							byte[] bytes = baos.toByteArray();

							frame_length = bytes.length;
						}

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
					}
				}


				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("type", "data");
				jsonObj.addProperty("length", frame_length);
				jsonObj.addProperty("width", grabber.getImageWidth());
				jsonObj.addProperty("height", grabber.getImageHeight());

				// send jsonObj first
				outputStream.write(jsonObj.toString().getBytes());
				outputStream.flush();

				byte[] buff = new byte[256];
				int len = 0;
				String msg = null;

				while ((len = inputStream.read(buff)) != -1) {
					msg = new String(buff, 0, len);

					// JSON analysis
					JsonParser parser = new JsonParser();
					boolean isJSON = true;
					JsonElement element = null;
					try {
						element =  parser.parse(msg);
					}
					catch (JsonParseException e) {
						//Log.e(TAG, "exception: " + e);
						isJSON = false;
					}
					if (isJSON && element != null) {
						JsonObject obj = element.getAsJsonObject();
						element = obj.get("state");
						if (element != null && element.getAsString().equals("ok")) {


							IplImage img = null;
							Frame frame = null;
							BufferedImage buff_img = null;

							// send data
							// use compressed JPG format for speed
							// need to send the byte size first (changes every time)
							while (true) {
								try {
//									img = grabber.grab();
									frame = grabber.grabFrame();
									img = converter.convert(frame);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
								}
								if (frame != null) {
//									canvas.showImage(img);
									canvas.showImage(frame);
									try {
//										recorder.record(img);
										recorder.record(frame);
									} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									
//									buff_img = img.getBufferedImage();
									buff_img = javaconverter.convert(frame);
									

									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									ImageIO.write(buff_img, "jpg", baos);
									byte[] bytes = baos.toByteArray();

									//System.out.println("bytes length: " + bytes.length);
									
									outputStream.write(intToBytes(bytes.length));
									outputStream.write(bytes); 
									outputStream.flush();

									if (Thread.currentThread().isInterrupted())
									{
										System.out.println("??");
										break;
									}
								}
								else {
									System.out.println(":(");
									break;
								}
							}

							break;
						}
					}
					else {
						break;
					}
				}
				
				

				outputStream.close();
				inputStream.close();
				if (recording) {
					try {
						recorder.stop();
						recorder.release();
						System.out.println("Stopped recording video");
					} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					grabber.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		} catch (IOException e) {
			if (recording) {
				try {
					recorder.stop();
					System.out.println("Stopped recording video");
				} catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			System.out.println("exception");
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

	}
	
	public static byte[] intToBytes(int yourInt) throws IOException {
		return ByteBuffer.allocate(4).putInt(yourInt).array();
	}


}

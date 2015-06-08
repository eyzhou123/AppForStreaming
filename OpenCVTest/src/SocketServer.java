

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
import java.util.Arrays;

import javax.imageio.ImageIO;
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

public class SocketServer extends Thread {
	private ServerSocket mServer;
	CanvasFrame canvas = new CanvasFrame("Web Cam");


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
			while (true) {
				if (byteArray != null)
					byteArray.reset();
				else
					byteArray = new ByteArrayOutputStream();

				socket = mServer.accept();
				System.out.println("new socket");

				inputStream = new BufferedInputStream(socket.getInputStream());
				outputStream = new BufferedOutputStream(socket.getOutputStream());
//640 x 480
// greyscale?
				canvas.setCanvasSize(600, 480);
				FrameGrabber grabber = new OpenCVFrameGrabber(0);
				grabber.setImageWidth(160);
				grabber.setImageHeight(500);
				int i=0;
				int frame_length = 0;
				try {
					grabber.start();
				} catch (Exception e) {
					System.out.println("EXCEEEPTTIOOON");
				}
				IplImage initial_frame = null;
				while(frame_length == 0) {
					try {

						initial_frame = grabber.grab();

						if(initial_frame != null) {
							BufferedImage initialBufferImage = initial_frame.getBufferedImage();

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(initialBufferImage, "bmp", baos);
							byte[] bytes = baos.toByteArray();

							frame_length = bytes.length;
						}

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}


				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("type", "data");
				jsonObj.addProperty("length", frame_length);
				jsonObj.addProperty("width", grabber.getImageWidth());
				jsonObj.addProperty("height", grabber.getImageHeight());

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
							BufferedImage buff_img = null;

							// send data

							while (true) {
								try {
									img = grabber.grab();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if (img != null) {
									canvas.showImage(img);
									buff_img = img.getBufferedImage();

									ByteArrayOutputStream baos = new ByteArrayOutputStream();
									ImageIO.write(buff_img, "bmp", baos);
									byte[] bytes = baos.toByteArray();
//									
//									byte[] bytes_with_length = Arrays.copyOf(bytes, bytes.length + 1); //create new array from old array and allocate one more element
//								    bytes_with_length[bytes_with_length.length - 1] = intToBytes(bytes.length);
									
									System.out.println("bytes length: " + bytes.length);

									//byte[] bytes = ((DataBufferByte) img.getBufferedImage().getData().getDataBuffer()).getData();


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

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
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
	
	public byte[] intToBytes(int my_int) throws IOException {
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    ObjectOutput out = new ObjectOutputStream(bos);
	    out.writeInt(my_int);
	    out.close();
	    byte[] int_bytes = bos.toByteArray();
	    bos.close();
	    return int_bytes;
	}
	public int bytesToInt(byte[] int_bytes) throws IOException {
	    ByteArrayInputStream bis = new ByteArrayInputStream(int_bytes);
	    ObjectInputStream ois = new ObjectInputStream(bis);
	    int my_int = ois.readInt();
	    ois.close();
	    return my_int;
	}


}

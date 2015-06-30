

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class SocketServerAndroid extends Thread {
	private ServerSocket mServer;
	private DataListener mDataListener;
	private BufferManager mBufferManager;
	private int width;
	private int height;
	public static boolean android_is_streaming = true;

	public SocketServerAndroid() {
	    
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		System.out.println("android server's waiting");
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		Socket socket = null;
		ByteArrayOutputStream byteArray = null;
		
		
		try {
			mServer = new ServerSocket(8880);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (SocketServer.server_is_running) {
			try {	
				if (byteArray != null)
					byteArray.reset();
				else
					byteArray = new ByteArrayOutputStream();

				socket = mServer.accept();
				System.out.println("new android socket");
				
				inputStream = new BufferedInputStream(socket.getInputStream());
				outputStream = new BufferedOutputStream(socket.getOutputStream());
			
				int just_read = 0;
				byte[] buff = new byte[256];
				byte[] imageBuff = null;
				byte[] length_buff = new byte[4];
				int len = 0;
				String msg = null;
				// read msg
				while ((len = inputStream.read(buff)) != -1) {
					System.out.println("reading initial message");
					msg = new String(buff, 0, len);
					// JSON analysis
	                JsonParser parser = new JsonParser();
	                boolean isJSON = true;
	                JsonElement element = null;
	                try {
	                    element =  parser.parse(msg);
	                }
	                catch (JsonParseException e) {
	                    System.out.println("exception: " + e);
	                    isJSON = false;
	                }
	                if (isJSON && element != null) {
	                    JsonObject obj = element.getAsJsonObject();
	                    element = obj.get("type");
	                    if (element != null && element.getAsString().equals("data")) {
	                        element = obj.get("length");
	                        int length = element.getAsInt();
	                        element = obj.get("width");
	                        width = element.getAsInt();
	                        element = obj.get("height");
	                        height = element.getAsInt();
	                        
	                        imageBuff = new byte[length];
                            mBufferManager = new BufferManager(length, width, height);
                            mBufferManager.setOnDataListener(mDataListener);
                            break;
	                    }
	                }
	                else {
	                    byteArray.write(buff, 0, len);
	                    break;
	                }
				}
				if (imageBuff != null) {
				    JsonObject jsonObj = new JsonObject();
		            jsonObj.addProperty("state", "ok");
		            outputStream.write(jsonObj.toString().getBytes());
		            outputStream.flush();
		            
//		            // read image data
//				    while ((len = inputStream.read(imageBuff)) != -1) {
//	                    mBufferManager.fillBuffer(imageBuff, len);
//	                }
		            
		            while(true) {
			            int length_bytes_read = 0;
						
						while (length_bytes_read < 4) {
							just_read = inputStream.read(length_buff, length_bytes_read, 4 - length_bytes_read);
							if (just_read < 0) {
								break;
							}
							length_bytes_read += just_read;
						}
						if (just_read < 0) {
							break;
						}
						int updated_length = bytesToInt(length_buff);
						//Log.d("ERRORCHECK", "read new length as: " + updated_length);
						imageBuff = new byte[updated_length];
						//mBufferManager = new BufferManager(updated_length, width, height);
						//mBufferManager.setOnDataListener(mDataListener);
						
						// read image
						int image_bytes_read = 0;
						while (image_bytes_read < updated_length) {
							just_read = inputStream.read(imageBuff, image_bytes_read, updated_length - image_bytes_read);
							if (just_read < 0) {
								break;
							}
							image_bytes_read += just_read;
						}
						if (just_read < 0) {
							break;
						}
					    ByteArrayInputStream stream = new ByteArrayInputStream(imageBuff);
	    			    BufferedImage bufferedImage = null;
	    			    try {
							bufferedImage = ImageIO.read(stream);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    				
	    			    if (bufferedImage == null) {
	    			    	System.out.println("Buffered image is NULL");
	    			    }
	                    mDataListener.onDirty(bufferedImage);
	//					mBufferManager.fillBuffer(imageBuff, updated_length);
		            }
				}
				
//				if (mBufferManager != null) {
//					mBufferManager.close();
//				}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			android_is_streaming = false;
			
			e.printStackTrace();
//		} finally {
//			try {
//				if (outputStream != null) {
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
//	                socket = null;
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
		System.out.println("Exited android server");
		return;

	}

	public void setOnDataListener(DataListener listener) {
		mDataListener = listener;
	}
	
	public int bytesToInt(byte[] int_bytes) throws IOException {
		return ByteBuffer.wrap(int_bytes).getInt();
	}
}



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class SocketServerAndroid extends Thread {
	private ServerSocket mServer;
	private DataListener mDataListener;
	private BufferManager mBufferManager;

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
			while (!Thread.currentThread().isInterrupted()) {
				if (byteArray != null)
					byteArray.reset();
				else
					byteArray = new ByteArrayOutputStream();

				socket = mServer.accept();
				System.out.println("new android socket");
				
				inputStream = new BufferedInputStream(socket.getInputStream());
				outputStream = new BufferedOutputStream(socket.getOutputStream());
				
				byte[] buff = new byte[256];
				byte[] imageBuff = null;
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
	                        int width = element.getAsInt();
	                        element = obj.get("height");
	                        int height = element.getAsInt();
	                        
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
		            
		            // read image data
				    while ((len = inputStream.read(imageBuff)) != -1) {
				    	System.out.println("READING IMAGE DATA");
	                    mBufferManager.fillBuffer(imageBuff, len);
	                    System.out.println("IMAGEBUFF IS SIZE: " + imageBuff.length);
	                }
				}
				
				if (mBufferManager != null) {
					mBufferManager.close();
				}
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

	public void setOnDataListener(DataListener listener) {
		mDataListener = listener;
	}
}

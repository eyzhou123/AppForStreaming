
public class MainProgram {
	public static void main (String [] args) {
		//GrabberShow grabber = new GrabberShow();
		SocketServer server = new SocketServer();
		AudioServer audio_socket = new AudioServer();
		
		server.start();
		audio_socket.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
		        System.out.println("Server was closed");
			        try {
						SocketServer.recorder.stop();
						SocketServer.recorder.release();
						SocketServer.recording = false;
						System.out.println("Stopped recording video");
					} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    }
		});
	}
}

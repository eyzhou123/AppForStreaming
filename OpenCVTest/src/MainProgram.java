import java.io.IOException;

import javax.swing.JFrame;

public class MainProgram {
	public static SocketServer server = new SocketServer();
	public static AudioServer audio_socket = new AudioServer();
	public static boolean exit = false;
	
	public static void main (String [] args) {
		
		GUI gui = new GUI();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(200, 100);
		gui.setVisible(true);
		
		//GrabberShow grabber = new GrabberShow();
		
		
		server.start();
		audio_socket.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
		        System.out.println("Server exited.");
		    }
		});
		
		
		
		
//		// merge video and audio to one final video
//		Runtime rt = Runtime.getRuntime();
//		try {
//			Process pr = rt.exec("ffmpeg -i audio.wav -i video.mp4 -acodec copy -vcodec copy output.mp4");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
}

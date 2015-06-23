import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;

public class MainProgram {
	public static SocketServer server = new SocketServer();
	public static AudioServer audio_socket = new AudioServer();
	public static boolean exit = false;
	public static String path_to_ffmpeg = "/usr/local/bin/ffmpeg";
	
	public static void main (String [] args) {
		
		GUI gui = new GUI();
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.setSize(200, 200);
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
		
		try {
			MainProgram.server.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			MainProgram.audio_socket.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (AudioServer.line.isRunning()) {
			AudioServer.line.stop();
	        AudioServer.line.drain();
	        AudioServer.line.close();
		}
		
		// merge video and audio to one final video
		Runtime rt = Runtime.getRuntime();
		try {
			System.out.println("Merging audio and video files");
			
			Date date = new Date();
    		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
    		String timestamp = sdf.format(date);
			

			Process pr2 = rt.exec(path_to_ffmpeg + " -i " + SocketServer.path 
					+ "audio.wav -i " + SocketServer.path + "video.mp4 -c:v copy -c:a aac -strict experimental " 
					+ SocketServer.path + timestamp + ".mp4");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
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

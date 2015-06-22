import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI extends JFrame {
	private JButton button;
	
	public GUI() {
		super("Server");
		setLayout(new FlowLayout());
		
		button = new JButton("Stop server");
		add(button);
		
		HandlerClass handler = new HandlerClass();
		button.addActionListener(handler);
		
	}
	
	private class HandlerClass implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JOptionPane.showMessageDialog(null, "Closed server, saving video.");
			SocketServer.server_is_running = false;
			SocketServer.canvas.removeAll();
			AudioServer.line.stop();
	        AudioServer.line.drain();
	        AudioServer.line.close();
			
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
			
//			try {
//				Process p = Runtime.getRuntime().exec(new String[]{"bash","-c","chmod 777 /usr/local/bin/ffmpeg"});
//				p.waitFor();
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			// merge video and audio to one final video
			Runtime rt = Runtime.getRuntime();
			try {
				String path_to_ffmpeg = "/usr/local/bin/ffmpeg";
				System.out.println("Merging audio and video files");
				
				Date date = new Date();
	    		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
	    		String timestamp = sdf.format(date);
				
//				Process pr2 = rt.exec("/usr/local/bin/ffmpeg -i /Users/eyzhou/Desktop/audio.wav -i /Users/eyzhou/Desktop/video.mp4 -acodec copy -vcodec copy /Users/eyzhou/Desktop/output.mp4");
				Process pr2 = rt.exec("/usr/local/bin/ffmpeg -i /Users/eyzhou/Desktop/audio.wav -i /Users/eyzhou/Desktop/video.mp4 -c:v copy -c:a aac -strict experimental /Users/eyzhou/Desktop/" + timestamp + ".mp4");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.exit(0);
			
		}
	}
}
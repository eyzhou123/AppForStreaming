import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI extends JFrame {
	public static JButton button;
	
	public GUI() {
		super("Server");
		setLayout(new FlowLayout());
		
		button = new JButton("Stop server");
		button.setBackground(new Color(0, 229, 255));
		button.setForeground(Color.BLACK);
		button.setBorderPainted(false);
		button.setOpaque(true);
		button.setFont(button.getFont().deriveFont(Font.BOLD));
		add(button);
		button.setFont(new Font("Arial", Font.PLAIN, 20));
		HandlerClass handler = new HandlerClass();
		button.addActionListener(handler);
		
	}
	
	public static void make_video_a() {
		System.out.println("closing mic");
        AudioServer.speakers.drain();
        AudioServer.speakers.close();
        AudioServer.microphone.stop();
        AudioServer.microphone.close();
        
		AudioServer.line.stop();
        AudioServer.line.drain();
        AudioServer.line.close();
	}
	
	public static void make_video_v() {
//		if (AudioServer.microphone.isOpen()) {
//			System.out.println("...");
//		}
		
		if (SocketServer.recording) {
			try {
				SocketServer.recorder.stop();
				SocketServer.recorder.release();
				SocketServer.recording = false;
				System.out.println("Stopped recording video");
			} catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
        
		
        try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
	    // merge video and audio to one final video
		Runtime rt = Runtime.getRuntime();
		try {
			System.out.println("Merging audio and video files");
			
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
			String timestamp = sdf.format(date);
			
			Process pr2 = rt.exec(MainProgram.path_to_ffmpeg + " -i " + SocketServer.path 
					+ "audio.wav -i " + SocketServer.path + "video.mp4 -c:v copy -c:a aac -strict experimental " 
					+ SocketServer.path + timestamp + ".mp4");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			
			// merge video and audio to one final video
			Runtime rt = Runtime.getRuntime();
			try {
				System.out.println("Merging audio and video files");
				
				Date date = new Date();
	    		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
	    		String timestamp = sdf.format(date);
				
	    		Process pr2 = rt.exec(MainProgram.path_to_ffmpeg + " -i " + SocketServer.path 
						+ "audio.wav -i " + SocketServer.path + "video.mp4 -c:v copy -c:a aac -strict experimental " 
						+ SocketServer.path + timestamp + ".mp4");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.exit(0);
			
		}
	}

	
}
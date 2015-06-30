import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class GUI extends JFrame {
	public static JButton button;
	public static JPanel panel1;
	public static JPanel panel2;
	
	public GUI() {
		super("Server");
		setLayout(new FlowLayout());
		
//		setBackground(Color.gray);
//		setSize(1000, 400);
//
//	    JPanel topPanel = new JPanel();
//	    topPanel.setLayout( new BorderLayout() );
//	    getContentPane().add( topPanel );
//		
//	    panel1 = new JPanel();
//	    panel1.setSize(500, 400);
//	    panel1.setLayout(new FlowLayout());
    	// This button should be used when closing the server, because it 
 		// ensures that a video is saved first. 
 		button = new JButton("Stop server");
 		button.setBackground(new Color(0, 229, 255));
 		button.setForeground(Color.BLACK);
 		button.setBorderPainted(false);
 		button.setOpaque(true);
 		button.setFont(button.getFont().deriveFont(Font.BOLD));
 		button.setFont(new Font("Arial", Font.PLAIN, 20));
 		HandlerClass handler = new HandlerClass();
 		button.addActionListener(handler);
	    add(button);
	    
//	    panel2 = new JPanel();
//	    panel2.setLayout(new FlowLayout());
//	    panel2.add(new JButton( "Button 1" ));
	    
	    
//	    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT );
//	    topPanel.add(splitPane, BorderLayout.CENTER );
//	    splitPane.setLeftComponent(panel1 );
//	    splitPane.setRightComponent(panel2 );
	    
		
		
		
	}
	
	public static void make_video_a() {
		// Take care of closing speakers/microphone 
        AudioServer.speakers.drain();
        AudioServer.speakers.close();
        AudioServer.microphone.stop();
        AudioServer.microphone.close();
        
        // This will stop recording audio and write the file
		AudioServer.line.stop();
        AudioServer.line.drain();
        AudioServer.line.close();
	}
	
	// The following two methods are for when the client side closes. They deal with
	// making sure both audio and video are written before trying to merge. They also
	// will not exit the program.
	
	public static void make_video_v() {
		// Closes the recorder and writes the video file
		
		// Wait for audio to be written
		while (AudioServer.line.isOpen()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        
		Thread finish_recording_thread = new Thread(new Runnable() {
            public void run() {
            	if (SocketServer.recording) {
        			try {
        				SocketServer.recorder.stop();
        				SocketServer.recorder.release();
        				SocketServer.recording = false;
        				//System.out.println("Stopped recording video");
        			} catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
        				// TODO Auto-generated catch block
        				e1.printStackTrace();
        			}
        		}
            }
       });  
		finish_recording_thread.start();
		
	    // merge video and audio to one final video
		Runtime rt = Runtime.getRuntime();
		try {
			System.out.println("Merging audio and video files");
			
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_h.mm.ssa");
			String timestamp = sdf.format(date);
			
////		the following can speed up the video if needed when merging (but only works directly from terminal)
	//		Process pr2 = rt.exec(MainProgram.path_to_ffmpeg + " -i " + SocketServer.path 
	//				+ "audio.wav -i " + SocketServer.path + "video.mp4 -strict experimental -vf 'setpts=0.7*PTS'" 
	//				+ SocketServer.path + timestamp + ".mp4");
			
			Process pr2 = rt.exec(MainProgram.path_to_ffmpeg + " -i " + SocketServer.path 
					+ "audio.wav -i " + SocketServer.path + "video.mp4 -strict experimental " 
					+ SocketServer.path + timestamp + ".mp4");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	// The following will write the files, wait for the two threads to end, and then merge them. 
	// Then, the program will exit.
	
	private class HandlerClass implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (!SocketServer.client_closed) {
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
			}
			System.exit(0);
			
		}
	}

	
}

public class MainProgram {
	public static void main (String [] args) {
		//GrabberShow grabber = new GrabberShow();
		SocketServer server = new SocketServer();
		AudioServer audio_socket = new AudioServer();
		
		server.start();
		audio_socket.start();
	}
}

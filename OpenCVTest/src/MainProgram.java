
public class MainProgram {
	public static void main (String [] args) {
		//GrabberShow grabber = new GrabberShow();
		SocketServer server = new SocketServer();
		
		server.start();
	}
}

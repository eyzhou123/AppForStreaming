import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;



public class GrabberShow implements Runnable {
    //final int INTERVAL=1000;///you may use interval
    IplImage image;
    CanvasFrame canvas = new CanvasFrame("Web Cam");
    public GrabberShow() {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }
    @Override
    public void run() {
        FrameGrabber grabber = new OpenCVFrameGrabber(0);
        
        
        int i=0;

            try {
				grabber.start();
				
			} catch (Exception e) {
			}
            IplImage img = null;
            while (true) {
                try {
					img = grabber.grab();
				} catch (Exception e) {
				}
                if (img != null) {
                	System.out.println(grabber.getImageWidth());
                    System.out.println(grabber.getImageHeight());
                    // show image on window
                    canvas.showImage(img);
                    img.getByteBuffer();
                }
                
                 //Thread.sleep(INTERVAL);
            }

    }
}
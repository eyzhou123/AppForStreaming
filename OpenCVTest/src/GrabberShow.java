import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_highgui.*;



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
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter javaconverter = new Java2DFrameConverter(); 
        
        int i=0;

            try {
				grabber.start();
				
			} catch (Exception e) {
			}
            IplImage img = null;
            Frame frame = null;
            while (true) {
                try {
                	frame = grabber.grab();
					img = converter.convert(frame);
				} catch (Exception e) {
				}
                if (img != null) {
                	System.out.println(grabber.getImageWidth());
                    System.out.println(grabber.getImageHeight());
                    // show image on window
                    canvas.showImage(frame);
                    img.getByteBuffer();
                }
                
                 //Thread.sleep(INTERVAL);
            }

    }
}
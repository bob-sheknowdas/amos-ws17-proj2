package de.tuberlin.amos.ws17.swit.landscape_tracking;

import de.tuberlin.amos.ws17.swit.image_analysis.ImageUtils;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

//Diese Klasse stellt auf Basis des LandscapeTracker-Interfaces
//eine Mock-Klasse zum Testen bzw. Demonstrieren dar.
//Sie hat Zugriff auf den MediaPlayer auf der Oberfläche (MVVM goodbye!)
//um beim Aufruf der Methode getImage einen Snapshot des abgespielten Videos
// als Bild zur Verfügung zu stellen.
//Dafür muss in der app.properties die Einstellung camera=2 gesetzt sein.
public class DemoVideoLandscapeTracker implements LandscapeTracker {
    private static final Logger LOGGER = Logger.getLogger(DemoVideoLandscapeTracker.class.getName());
    private MediaView mediaView;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private FFmpegFrameGrabber grabber;

    private BufferedImage getFrame(double second) throws FrameGrabber.Exception {
        synchronized (grabber) {
            double frameRate = grabber.getFrameRate();
            double lengthInSeconds = grabber.getLengthInTime() / 1000000f;
            if (second >= lengthInSeconds) {
                LOGGER.log(Level.INFO, "Frame outside of video");
                return null;
            }
            int frameNumber = (int) (frameRate * second);
            grabber.setFrameNumber(frameNumber);
            Frame frame = grabber.grab();
            BufferedImage image = converter.convert(frame);
            if (image == null) {
                LOGGER.log(Level.WARNING, "Failed to grab image at " + second + "s");
            }
            return image;
        }
    }

    public DemoVideoLandscapeTracker(MediaView mediaView, String videoName) {
        this.mediaView = mediaView;
        mediaView.getMediaPlayer().setCycleCount(1000);
        try {
            grabber = new FFmpegFrameGrabber(ImageUtils.getTestVideoPath(videoName));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BufferedImage getImage() {
        double currentSecond = mediaView.getMediaPlayer().getCurrentTime().toSeconds();
        BufferedImage image = null;
        try {
            image = getFrame(currentSecond);
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void startModule() {
        mediaView.setVisible(true);
        mediaView.getMediaPlayer().play();
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean stopModule() {
        mediaView.setVisible(false);
        mediaView.getMediaPlayer().stop();
        try {
            grabber.stop();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public BufferedImage getModuleImage() {
        String path = "";
        try {
            this.getClass().getResource("");
            path = this.getClass().getClassLoader().getResource("module_images/landscape_tracking.jpg").getPath();
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println(path);
        }
        return null;
    }

    @Override
    public String getModuleName() {
        return "Landscape Tracker";
    }
}

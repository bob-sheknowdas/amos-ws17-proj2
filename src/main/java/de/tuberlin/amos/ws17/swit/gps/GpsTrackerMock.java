package de.tuberlin.amos.ws17.swit.gps;

import de.tuberlin.amos.ws17.swit.common.KinematicProperties;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;

import de.tuberlin.amos.ws17.swit.common.exceptions.ModuleNotWorkingException;
import org.joda.time.DateTime;

import javax.imageio.ImageIO;

public class GpsTrackerMock extends GpsTrackerImplementation {

    private LinkedList<KinematicProperties> returnList;
    private LinkedList<KinematicProperties> travelledList;

    private int index = -1;
    private int skipCount = 1;

    // constructor
    public GpsTrackerMock() {
        // DateTime timeStamp, double course, double velocity, double acceleration
        returnList = new LinkedList<KinematicProperties>();
        KinematicProperties one = new KinematicProperties(null, 180, 10, 0);
        one.setLatitude(52.5219184);
        one.setLongitude(13.411026);
        KinematicProperties two = new KinematicProperties(null, 42, 20, 1);
        two.setLatitude(52.539462);
        two.setLongitude(13.424657);
        KinematicProperties three = new KinematicProperties(null, 0, 10, 2);
        three.setLatitude(52.552202);
        three.setLongitude(13.430470);
        KinematicProperties four = new KinematicProperties(null, 90, 100, -50);
        four.setLatitude(52.543122);
        four.setLongitude(13.450364);
        KinematicProperties five = new KinematicProperties(null, 270, 1, 99);
        five.setLatitude(52.536744);
        five.setLongitude(13.4462239);
        returnList.add(one);
        returnList.add(two);
        returnList.add(three);
        returnList.add(four);
        returnList.add(five);

        travelledList = new LinkedList<KinematicProperties>();
    }

    // returns latest gps position from either the file reader or the port reader
    public GpsPosition getGpsPosition(){
        return null;
    }

    public String getModuleName(){ return "GpsModule"; }

    public BufferedImage getModuleImage() {
        String path = "";
        try {
            path = this.getClass().getClassLoader().getResource("module_images/gps_tracker.png").getPath();
            return ImageIO.read(new File(path));
        } catch (IOException|NullPointerException e) {
            e.printStackTrace();
            System.out.println(path);
        }
        return null;
    }
    // returns an object filled with the fake values (fot testing without hardware)
    // for every second new request, a new coordinate is sent
    public KinematicProperties fillDumpObject(KinematicProperties kinProp) throws ModuleNotWorkingException{
        DateTime now = new DateTime();
        skipCount--;
        if (skipCount == 0){
            skipCount = 1;
            index++;
            while (index >= returnList.size())
                index -= returnList.size();
        }
        returnList.get(index).setTimeStamp(now);
        travelledList.push(returnList.get(index));
        return returnList.get(index);
    }

    public void startModule() throws ModuleNotWorkingException{
        // not a real module, does not need starting
    }

    public boolean stopModule(){
        // not a real module, does not need stopping
        return true;
    }


    @Override
    public LinkedList<KinematicProperties> getGpsTrack(int count) {

        if (travelledList.size() <= count) {
            return travelledList;
        } else {
            int start = travelledList.size() - count;
            return new LinkedList<>(travelledList.subList(start, travelledList.size() - 1));
        }
    }
}

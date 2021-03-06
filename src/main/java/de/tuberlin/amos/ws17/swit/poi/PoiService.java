package de.tuberlin.amos.ws17.swit.poi;

import de.tuberlin.amos.ws17.swit.common.GpsPosition;
import de.tuberlin.amos.ws17.swit.common.PointOfInterest;
import de.tuberlin.amos.ws17.swit.common.exceptions.ModuleViolationException;

import java.util.Collection;
import java.util.List;

/**
 * A Service for retrieving {@link PointOfInterest}s
 */
public interface PoiService<T extends PointOfInterest> {

    /**
     * Recive all POIs for a circle
     * @param center as the centerpoint
     * @param radius as the radius in meters
     * @return a List of the found POIs
     * @throws ModuleViolationException for an invalid requests
     */
    List<T> loadPlaceForCircle(GpsPosition center, int radius) throws ModuleViolationException;

    /**
     * Recive all POIs for a circle
     * @param center as the centerpoint
     * @param radius as the radius in meters
     * @param types  as the types to retrieve
     * @return a List of the found POIs
     * @throws ModuleViolationException for an invalid requests
     */
    List<T> loadPlaceForCircleAndPoiType(GpsPosition center, int radius, PoiType... types) throws ModuleViolationException;

    /**
     * Loding photos expensive so it's done separately
     * @param poisToAddPhotosTo as the POIs where a try of adding a photo is made
     */
    void addImages(Collection<T> poisToAddPhotosTo);

}

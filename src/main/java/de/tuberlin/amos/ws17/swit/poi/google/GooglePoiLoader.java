package de.tuberlin.amos.ws17.swit.poi.google;

import de.tuberlin.amos.ws17.swit.common.ApiConfig;
import de.tuberlin.amos.ws17.swit.common.GpsPosition;
import de.tuberlin.amos.ws17.swit.common.PointOfInterest;
import de.tuberlin.amos.ws17.swit.poi.PoiType;
import se.walkercrou.places.*;
import se.walkercrou.places.exception.GooglePlacesException;
import se.walkercrou.places.exception.InvalidRequestException;

import java.awt.image.BufferedImage;
import java.util.*;

public class GooglePoiLoader {

	private GooglePlaces client;
	private RequestHandler rh= new FixedRequestHandler();

	private GoogleTypeMap typeMap= new GoogleTypeMap();

	private GooglePoiFactory poiFactory = new GooglePoiFactory();

	private int xResolution, yResolution;

	public GooglePoiLoader(int xResolution, int yResolution) {
		this(false, xResolution, yResolution);
	}

	public GooglePoiLoader(boolean enableLogging, int xResolution, int yResolution) {
		client = new GooglePlaces(ApiConfig.getProperty("GooglePlaces"), rh);
		client.setDebugModeEnabled(enableLogging);
		this.xResolution=xResolution;
		this.yResolution=yResolution;
	}

	SearchGeometryFactory geometryFactory=new SearchGeometryFactory();

    public Set<GooglePoi> loadPlaceForMultiCircleSearchGeometry(MultiCircleSearchGeometry multiCircle){

        Set<GooglePoi> pois=new HashSet<>();
        for(CircleSearchGeometry circle: multiCircle){
            pois.addAll(loadPlaceForCircleSearchGeometry(circle));
        }
        return pois;
    }

    public Set<GooglePoi> loadPlaceForCircleSearchGeometry(CircleSearchGeometry circle){
        Set<GooglePoi> pois=new HashSet<>();

        //if nothing definded load all
        if(circle.getPoiTypes()==null
				&&circle.getGoogletypes()==null){
			pois.addAll(
					loadPlaceForCircle(circle.getCenter(), circle.getRadiusInMeters()));
			return pois;
		}

		if(circle.getPoiTypes()!=null){

			PoiType[] types=circle.getPoiTypes().toArray(new PoiType[circle.getPoiTypes().size()]);
			pois.addAll(
					loadPlaceForCircleAndPoiType(circle.getCenter(), circle.getRadiusInMeters(), types));
		}
		if(circle.getGoogletypes()!=null){

			GoogleType[] types=circle.getGoogletypes().toArray(new GoogleType[circle.getGoogletypes().size()]);
			pois.addAll(
					loadPlaceForCircleAndType(circle.getCenter(), circle.getRadiusInMeters(), types));
		}

		return pois;
    }

	public List<? extends PointOfInterest> loadPointOfInterestForCircle(GpsPosition center, int radius) throws InvalidRequestException{

		return loadPlaceForCircle(center, radius, new Param[0]);
	}
	public List<GooglePoi> loadPlaceForCircle(GpsPosition center, int radius) throws InvalidRequestException{

		return loadPlaceForCircle(center, radius, new Param[0]);
	}

	public List<GooglePoi> loadPlaceForCircleAndType(GpsPosition center, int radius, GoogleType... types) throws InvalidRequestException{
		//TODO koennte fehlerhaft sein
		Param[] params=new Param[types.length];
		for(int i=0; i<types.length; i++){
			params[i]=new Param("type").value(types[i]);
		}

		return loadPlaceForCircle(center, radius, params);
	}

	public List<GooglePoi> loadPlaceForCircleAndPoiType(GpsPosition center, int radius, PoiType... types) throws InvalidRequestException{

		Set<GoogleType> gTypes=new HashSet<>();
		for(PoiType type: types){
			gTypes.addAll(typeMap.getKeysByValue(type));
		}

		Param[] params=new Param[gTypes.size()];

		int i=0;
		for(GoogleType gType: gTypes){
			params[i]=new Param("type").value(gType);
			i++;
		}

		return loadPlaceForCircle(center, radius, params);
	}

	private List<GooglePoi> loadPlaceForCircle(GpsPosition center, int radius, Param[] params) throws InvalidRequestException{

		try {
			List<Place> places = client.getNearbyPlaces(center.getLatitude(), center.getLongitude(), radius, GooglePlaces.MAXIMUM_RESULTS, params);
			places = getPlacesDetails(places);
			return poiFactory.createPOIsfromPlace(places);

		}catch(GooglePlacesException e){
			e.printStackTrace();
			System.err.println(e.getErrorMessage());
			return null;
		}
	}
	private static List<Place> getPlacesDetails(List<Place> places){

    	if (places == null) {
    		return new ArrayList<Place>();
		}

		List<Place> detailedPlaces=new ArrayList<>();
		
		for(Place p: places){
			String gson="{ \"result\" : "+p.getJson().toString()+"\n}";
			detailedPlaces.add(Place.parseDetails(p.getClient(), gson));
		}
		return detailedPlaces;
	}

	public void downloadImages(Collection<GooglePoi> poisToAddPhotosTo){
    	if (poisToAddPhotosTo == null) {
    		return;
		}

		for(GooglePoi poi:poisToAddPhotosTo){
			downloadImage(poi);
		}
	}

	private void downloadImage(GooglePoi poi) {
    	if (poi == null) {
    		return;
		}

		if(poi.getPhotoreference()!=null) {
			Photo photo = poi.getPhotoreference();
			BufferedImage image = photo.download(xResolution, yResolution).getImage();
			poi.setImage(image);
		}
	}

}

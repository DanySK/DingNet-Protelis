package util;

import org.jxmapviewer.viewer.GeoPosition;

public class MapHelper {

    private static MapHelper instance = new MapHelper();

    private MapHelper() {
    }

    public static MapHelper getInstance() {
        return instance;
    }

    /**
     * The coordinates of the point [0,0] on the map.
     */
    private GeoPosition mapOrigin;

    public void setMapOrigin(GeoPosition mapOrigin) {
        this.mapOrigin = mapOrigin;
        new MapHelper();
    }

    /**
     * Returns the coordinates of the point [0,0] on the map.
     * @return The coordinates of the point [0,0] on the map.
     */
    public GeoPosition getMapOrigin() {
        if (mapOrigin == null) {
            throw new IllegalStateException("Map utils not initialized");
        }
        return mapOrigin;
    }

    /**
     * Converts a GeoPostion to an x-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The x-coordinate on the map of the GeoPosition.
     */
    public int toMapXCoordinate(GeoPosition geoPosition){
        return toMapXCoordinate(geoPosition, getMapOrigin());
    }

    /**
     * Converts a GeoPostion to an x-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @param mapOrigin the coordinates of the point [0,0] on the map.
     * @return The x-coordinate on the map of the GeoPosition.
     */
    public int toMapXCoordinate(GeoPosition geoPosition, GeoPosition mapOrigin){
        return (int)Math.round(1000*distance(mapOrigin, new GeoPosition(mapOrigin.getLatitude(), geoPosition.getLongitude())));
}

    /**
     * Converts a GeoPostion to an y-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The y-coordinate on the map of the GeoPosition.
     */
    public int toMapYCoordinate(GeoPosition geoPosition){
        return toMapYCoordinate(geoPosition, getMapOrigin());
    }

    /**
     * Converts a GeoPostion to an y-coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @param mapOrigin the coordinates of the point [0,0] on the map.
     * @return The y-coordinate on the map of the GeoPosition.
     */
    public int toMapYCoordinate(GeoPosition geoPosition, GeoPosition mapOrigin){
        return (int)Math.round(1000*distance(mapOrigin, new GeoPosition(geoPosition.getLatitude(), mapOrigin.getLongitude())));
    }

    /**
     * Converts a GeoPostion to an coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @return The coordinate on the map of the GeoPosition.
     */

    public Pair<Integer,Integer> toMapCoordinate(GeoPosition geoPosition){
        return toMapCoordinate(geoPosition, getMapOrigin());
    }

    /**
     * Converts a GeoPostion to an coordinate on the map.
     * @param geoPosition the GeoPosition to convert.
     * @param mapOrigin the coordinates of the point [0,0] on the map.
     * @return The coordinate on the map of the GeoPosition.
     */
    public Pair<Integer,Integer> toMapCoordinate(GeoPosition geoPosition, GeoPosition mapOrigin){
        return new Pair<>(toMapXCoordinate(geoPosition, mapOrigin), toMapYCoordinate(geoPosition, mapOrigin));
    }

    /**
     * A function to calculate the longitude from a given x-coordinate on the map.
     * @param xPos  The x-coordinate of the entity.
     * @return The longitude of the given x-coordinate
     */
    public double toLongitude(int xPos){
        return toLongitude(xPos, getMapOrigin());
    }

    /**
     * A function to calculate the longitude from a given x-coordinate on the map.
     * @param xPos  The x-coordinate of the entity.
     * @param mapOrigin the coordinates of the point [0,0] on the map.
     * @return The longitude of the given x-coordinate
     */
    public double toLongitude(int xPos, GeoPosition mapOrigin){
        double longitude;
        if(xPos> 0) {
            longitude = xPos;
            longitude = longitude / 1000;
            longitude = longitude / 1.609344;
            longitude = longitude / (60 * 1.1515);
            longitude = Math.toRadians(longitude);
            longitude = Math.cos(longitude);
            longitude = longitude - Math.sin(Math.toRadians(mapOrigin.getLatitude())) * Math.sin(Math.toRadians(mapOrigin.getLatitude()));
            longitude = longitude / (Math.cos(Math.toRadians(mapOrigin.getLatitude())) * Math.cos(Math.toRadians(mapOrigin.getLatitude())));
            longitude = Math.acos(longitude);
            longitude = Math.toDegrees(longitude);
            longitude = longitude + mapOrigin.getLongitude();
        }
        else{
            longitude = mapOrigin.getLongitude();
        }
        return longitude;
    }

    /**
     * A function to calculate the latitude from a given y-coordinate on the map.
     * @param yPos  The y-coordinate of the entity.
     * @return The latitude of the given y-coordinate.
     */
    public double toLatitude(int yPos){
        return toLatitude(yPos, getMapOrigin());
    }

    /**
     * A function to calculate the latitude from a given y-coordinate on the map.
     * @param yPos  The y-coordinate of the entity.
     * @param mapOrigin the coordinates of the point [0,0] on the map.
     * @return The latitude of the given y-coordinate.
     */
    public double toLatitude(int yPos, GeoPosition mapOrigin){
        double latitude = yPos;
        latitude = latitude /1000 ;
        latitude = latitude/ 1.609344;
        latitude = latitude / (60 * 1.1515);
        latitude = latitude + mapOrigin.getLatitude();
        return latitude;

    }

    static public double distance(GeoPosition pos1, GeoPosition pos2) {
        double lat1 = pos1.getLatitude(), lon1 = pos1.getLongitude();
        double lat2 = pos2.getLatitude(), lon2 = pos2.getLongitude();
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            return dist;
        }
    }
}
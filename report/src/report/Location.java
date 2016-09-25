package report;

public class Location {

	 private double latitude;

	 private double longitude;

	 private String distance;
	 
	 public Location() {

	 }

	 public Location(Double latitude, Double longitude, String distance) {
	  this.latitude = latitude;
	  this.longitude = longitude;
	  this.distance = distance;
	 }
	 
	 /**
	  * @return the latitude
	  */
	 public double getLatitude() {
	  return latitude;
	 }

	 /**
	  * @return the longitude
	  */
	 public double getLongitude() {
	  return longitude;
	 }
	 
	 /**
	  * @return the longitude
	  */
	 public String getDistance() {
	  return distance;
	 }
}

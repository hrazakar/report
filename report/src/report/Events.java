package report;

import java.sql.Timestamp;
import java.util.*;

public class Events {

	  protected String AccountID;
	  protected String DeviceID ;
	  protected double Latitude;
	  protected double Longitude;
	  protected double Speed;
	  protected Timestamp DateEvt;
	  protected double OdometerKM;
	  protected double OdometerOffsetKM;
	  protected int StatusCode;
	  protected int SpeedMax;


	  public Events(String AccountID, String DeviceID, double Latitude, double Longitude, double Speed,
			  Timestamp DateEvt, double OdometerKM, double OdometerOffsetKM, int StatusCode, int SpeedMax) {

	     this.AccountID = AccountID;
	     this.DeviceID = DeviceID;
	     this.Latitude = Latitude;
	     this.Longitude = Longitude;
	     this.Speed = Speed;
	     this.DateEvt = DateEvt;
	     this.OdometerKM = OdometerKM;
	     this.OdometerOffsetKM = OdometerOffsetKM;
	     this.StatusCode = StatusCode;	     
	     this.SpeedMax = SpeedMax;	     
	  }

	  /**
	   Returns the id of the account.

	   @return  the id of the account.
	   */
	  public String getAccountID() {
	    return AccountID;
	  }

	  /**
	   Returns the id of the Device.

	   @return  the id of the Device.
	   */
	  public String getDeviceID() {
	    return DeviceID;
	  }

	  /**
	   Returns the latitude of Longitude.

	   @return  the latitude of Longitude.
	   */
	  public double getLongitude() {
	    return Longitude;
	  }

	  /**
	   Returns the latitude of events.

	   @return  the latitude of events.
	   */
	  public double getLatitude() {
	    return Latitude;
	  }

	  /**
	   Returns the Speed of events.

	   @return  the Speed events.
	   */
	  public double getSpeed() {
	    return Speed;
	  }

	  /**
	   Returns the Speed of events.

	   @return  the Speed events.
	   */
	  public double getSpeedMax() {
	    return SpeedMax;
	  }

	  public Timestamp getDateEvt() {
	    return DateEvt;
	  }
	  /**
	   Returns the Originator of the ticket.

	   @return  the originator of the ticket.
	   */
	  public double getOdometerKM() {
	    return OdometerKM;
	  }

	  public double getOdometerOffsetKM() {
	    return OdometerOffsetKM;
	  }

	  public double getStatusCode() {
		    return StatusCode;
		  }
	  /**
	   Set the type of the ticket.

	   @param	type	the type of the ticket.
	   */
	  public void setAccountID(String AccountID) {
	    this.AccountID = AccountID;
	  }

	  /**
	   Set the id of the ticket.

	   @param	id	the id of the ticket.
	   */
	  public void setLatitude(double Latitude) {
	    this.Latitude = Latitude;
	  }

	  public void setLongitude(double Longitude) {
		    this.Longitude = Longitude;
		  }
	  public void setSpeed(double Speed) {
		    this.Speed = Speed;
		  }
	  public void setDeviceID(String DeviceID) {
		    this.DeviceID = DeviceID;
		  }
	  public void setDateEvt(Timestamp DateEvt) {
		  	this.DateEvt = DateEvt;
		  }
	  public void setOdometerOffsetKM(double OdometerOffsetKM) {
		    this.OdometerOffsetKM = OdometerOffsetKM;
		  }
	  public void setOdometerKM(double OdometerKM) {
		    this.OdometerKM = OdometerKM;
		  }
	  public void setStatusCode(int StatusCode) {
		    this.StatusCode = StatusCode;
		  }
	  public void setSpeedMax(int SpeedMax) {
		    this.SpeedMax = SpeedMax;
		  }

}

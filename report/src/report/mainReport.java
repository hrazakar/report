package report;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;



public class mainReport {
	private static final Logger LOGGER = Logger.getLogger(
	Thread.currentThread().getStackTrace()[0].getClassName() );
	private static final DecimalFormat dec = new DecimalFormat("#00.000000");
	private static String[] deviceID;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
	    Connection con = null;
	    Statement st = null;
	    ResultSet rs = null;
	    String sql = null;
	    boolean flag = false;

	    String url = "jdbc:mysql://77.72.92.130/gts";
	    String user = "gts";
	    String password = "heryrate93";

	    String accountID = "airtel";
	    
	    String pathInfos = "";
	    String stopsInfos = "";
	    String eventsInfos = "";

	    try {
	        Class.forName("com.mysql.jdbc.Driver");
	        con = DriverManager.getConnection(url, user, password);
	        st = con.createStatement();
	        createTemporaryTable(con, st, accountID); //"FROM_UNIXTIME(dateEvt,'%d%m%Y%H%i%s') CREATE TEMPORARY TABLE datatmpEvt( accountID varchar(45), deviceID varchar(45), dateEvt Timestamp, lat double, lon double, speed int);";
	        //calculTemp(con, st);
	        System.out.println("Nombre de device: " + deviceID.length);
	        for (int i=0;i<deviceID.length;i++){
	        	pathInfos = createpathInfos(con, st, deviceID[i]);
		        stopsInfos = createstopsInfos(con, st, deviceID[i]);
		        if (stopsInfos == null) stopsInfos = createstopsInfosnull(con, st, accountID, deviceID[i]);
		        eventsInfos = createeventsInfos(con, st, deviceID[i]);
	        }

	    } catch (SQLException ex) {
	        LOGGER.severe(ex.getMessage());
	    } finally {
	        try {
	            if (rs != null) {
	                rs.close();
	            }
	            if (st != null) {
	                st.close();
	            }
	            if (con != null) {
	                con.close();
	            }

	        } catch (SQLException ex) {
	            LOGGER.warning(ex.getMessage());
	        }
	    }
		File htmlTemplateFile = new File("d:\\Template.html");
		String htmlString;
		htmlString = FileUtils.readFileToString(htmlTemplateFile);
		String title = "New Page";
		String body = "This is Body";
		htmlString = htmlString.replace("$title", title);
		File newHtmlFile = new File("d:\\new.html");
		FileUtils.writeStringToFile(newHtmlFile, htmlString);

	}

	public static String createstopsInfos(Connection c, Statement s, String dvId){
		boolean flag;
		boolean first;
		ResultSet rs = null;
		String stopsInfos = "Array(";
	    String accountID = "";
	    String deviceID = "";
	    String deviceIDlast = "";
	    Double latitude = 0.0;
	    Double longitude = 0.0;
	    Timestamp dateEV;
	    String temps ="";
	    String temps2 ="";
	    String datem;
	    int vitesse = 0;
	    int tmps, nbstop = 0;
	    int nb =0;
		
        String sql = "SELECT accountID, dateEvt as dateEV, deviceID, lat as latitude, lon as longitude, speed as speedKPH, tmps, first FROM datatmpEvt where deviceID='"+dvId+"' order by deviceID,dateEV;";
        try {
	        rs = s.executeQuery(sql);
	        for(flag = rs.next(); flag; flag = rs.next())
	        {
	        	accountID = rs.getString("accountID");
	            if(accountID == null)
	            	accountID = "";
	            deviceID = rs.getString("deviceID");
	            if(deviceID == null)
	            	deviceID = "";
	            dateEV = rs.getTimestamp("dateEV");
	            if(dateEV == null)
	            	dateEV = null;
	            latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
	            longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
	            vitesse = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
	            tmps = rs.getObject("tmps") != null ? rs.getInt("tmps") : null;
	            first = rs.getObject("first") != null ? rs.getBoolean("first") : null;
	            if (nb==0) deviceIDlast = deviceID;
	            if (first){
	            	temps2 = null;
	            	temps = "\"\"";
	            }else{
	            	temps = "\"00-00-00 "+getDurationString(tmps)+"\"";
	            	temps2 = Integer.toString(tmps);
	            }
		        if ( vitesse < 1){
		            //Array(Array(-18.94375, 47.50367, 20151208070953, 289, 0), Array(-18.94341, 47.50208, 71013, 309, 32))
		            if (deviceIDlast.equals(deviceID)){
		            	if (nb>0) stopsInfos = stopsInfos + ", ";
		            	if (nb==0) datem = new SimpleDateFormat("yyyyMMddHHmmss").format(dateEV);
		            	else datem = new SimpleDateFormat("HHmmss").format(dateEV);
		            	stopsInfos = stopsInfos + "Array("+latitude+", " + longitude +", " + datem +", "+temps+", " + temps2 + ", "+ vitesse+")";
		            	nbstop++;
		            }
		            else{
		            	stopsInfos = stopsInfos + ")";
		                //System.out.println(deviceIDlast);
		                //System.out.println(stopsInfos);
		                stopsInfos = "Array(";
		                nb = -1;
		            }
		            nb++;

	            }
	            //System.out.println("accountID:" + accountID + " deviceID:" + deviceID + " date:" + dateEV + " latitude:" + latitude + " longitude:"+longitude + " vitesse:" +vitesse );
	            deviceIDlast = deviceID;
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (nbstop > 0){
	        stopsInfos = stopsInfos + ")";
            sql = "SELECT accountID, deviceID, vehicleMake, vehicleModel, licensePlate FROM Device where accountID='"+accountID+"' and deviceID='"+deviceID+"';";
            try {
            	 rs = s.executeQuery(sql);
                 if(rs.next())
                 {
                	 String vehicleMake = rs.getObject("vehicleMake") != null ? rs.getString("vehicleMake") : null;
                	 String vehicleModel = rs.getObject("vehicleModel") != null ? rs.getString("vehicleModel") : null;
                	 String licensePlate = rs.getObject("licensePlate") != null ? rs.getString("licensePlate") : null;
                	 stopsInfos = "Array(" + stopsInfos + ", \""+licensePlate+" "+vehicleModel+"\")";
                	 
                 }
                 rs.close();
            } catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
	        System.out.println(deviceIDlast +": stopsInfos");
	        System.out.println(stopsInfos);
        }else{
	        stopsInfos = null;
	        System.out.println(deviceIDlast +": AUCUN stopsInfos");
        }
		return stopsInfos;
	}

	public static String createstopsInfosnull(Connection c, Statement s, String acId, String dvId){
		ResultSet rs = null;
		String stopsInfos = "Array(";

        String sql = "SELECT accountID, FROM_UNIXTIME(timestamp) as timestamp, deviceID, latitude, longitude, speedKPH FROM EventData WHERE accountID='"+acId+"' AND deviceID='"+dvId+"' ORDER BY timestamp DESC LIMIT 1;";
        try {
	        rs = s.executeQuery(sql);
            if (rs.next()) {
            	int speedKPH = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
                Timestamp dateEV = rs.getTimestamp("timestamp");
                double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
                double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
            	stopsInfos = stopsInfos + "Array("+latitude+", " + longitude +", " + new SimpleDateFormat("yyyyMMddHHmmss").format(dateEV) +", \"\", null, 0))";
                sql = "SELECT accountID, deviceID, vehicleMake, vehicleModel, licensePlate FROM Device where accountID='"+acId+"' and deviceID='"+dvId+"';";
                ResultSet rs1 = s.executeQuery(sql);
	             if(rs1.next())
	             {
	            	 String vehicleMake = rs1.getObject("vehicleMake") != null ? rs1.getString("vehicleMake") : null;
	            	 String vehicleModel = rs1.getObject("vehicleModel") != null ? rs1.getString("vehicleModel") : null;
	            	 String licensePlate = rs1.getObject("licensePlate") != null ? rs1.getString("licensePlate") : null;
	            	 stopsInfos = "Array(" + stopsInfos + ", \""+licensePlate+" "+vehicleModel+"\")";	            	 
	             }
	             rs1.close();
           }
           rs.close();
	       System.out.println(dvId +": stopsInfosnull");
	       System.out.println(stopsInfos);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return stopsInfos;
	}

	public static String createpathInfos(Connection c, Statement s, String dvId){
		boolean flag, first;
		ResultSet rs = null;
	    String accountID = "";
	    String deviceID = "";
	    String deviceIDlast = "";
	    Double latitude = 0.0;
	    Double longitude = 0.0;
	    Timestamp dateEV = null;
	    String dateEVbegin = "";
	    String dateEVend = "";
	    String datem;
	    int vitesse = 0;
	    int tmps, nbpath=0;
	    int nb =0;
	    String pathinfos = "Array(";
		
        String sql = "SELECT accountID, dateEvt as dateEV, deviceID, lat as latitude, lon as longitude, speed as speedKPH, tmps, first FROM datatmpEvt where deviceID='"+dvId+"' order by dateEV;";
        try {
	        rs = s.executeQuery(sql);
	        for(flag = rs.next(); flag; flag = rs.next())
	        {
	        	accountID = rs.getString("accountID");
	            if(accountID == null)
	            	accountID = "";
	            deviceID = rs.getString("deviceID");
	            if(deviceID == null)
	            	deviceID = "";
	            dateEV = rs.getTimestamp("dateEV");
	            if(dateEV == null)
	            	dateEV = null;
	            latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
	            longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
	            vitesse = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
	            tmps = rs.getObject("tmps") != null ? rs.getInt("tmps") : null;
	            //heading = rs.getObject("heading") != null ? rs.getInt("heading") : null;
	            if (nb==0) deviceIDlast = deviceID;
	            if (nbpath==0) dateEVbegin = new SimpleDateFormat("yyyMMddHHmmss").format(dateEV);
	            //Array(Array(-18.94375, 47.50367, 20151208070953, 289, 0), Array(-18.94341, 47.50208, 71013, 309, 32))
	            if (deviceIDlast.equals(deviceID)){
	            	if (nb>0) pathinfos = pathinfos + ", ";
	            	if (nb==0) datem = new SimpleDateFormat("yyyyMMddHHmmss").format(dateEV);
	            	else datem = new SimpleDateFormat("HHmmss").format(dateEV);
	            	pathinfos = pathinfos + "Array("+latitude+", " + longitude +", " + datem +", " + tmps + ", "+ vitesse+")";
	            	nbpath++;
	            }
	            else{
	                pathinfos = pathinfos + ")";
	                System.out.println(deviceIDlast);
	                System.out.println(pathinfos);
	                pathinfos = "Array(";
	                nb = -1;
	            }
	            //System.out.println("accountID:" + accountID + " deviceID:" + deviceID + " date:" + dateEV + " latitude:" + latitude + " longitude:"+longitude + " vitesse:" +vitesse );
	            deviceIDlast = deviceID;
	            nb++;
	        }
	        if (nbpath > 0) dateEVend = new SimpleDateFormat("yyyMMddHHmmss").format(dateEV);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (nbpath > 0){
	        pathinfos = pathinfos + ")";
            sql = "SELECT accountID, deviceID, vehicleMake, vehicleModel, licensePlate FROM Device where accountID='"+accountID+"' and deviceID='"+deviceID+"';";
            try {
            	 rs = s.executeQuery(sql);
                 if(rs.next())
                 {
                	 String vehicleMake = rs.getObject("vehicleMake") != null ? rs.getString("vehicleMake") : null;
                	 String vehicleModel = rs.getObject("vehicleModel") != null ? rs.getString("vehicleModel") : null;
                	 String licensePlate = rs.getObject("licensePlate") != null ? rs.getString("licensePlate") : null;
                	 pathinfos = "Array(" + pathinfos + ", \""+dateEVbegin+"\", \""+dateEVend+"\", 1)";
                	 
                 }
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
	        System.out.println(deviceIDlast +": pathInfos");
	        System.out.println(pathinfos);
        }else{
	        pathinfos = "";
	        System.out.println(deviceIDlast +": AUCUN pathInfos");       	
        }
		return pathinfos;
	}
	
	public static String createeventsInfos(Connection c, Statement s, String dvId){
		boolean flag;
		ResultSet rs=null;
		String eventsInfos = "Array(";
	    String accountID = "";
	    String deviceID = "";
	    String deviceIDlast = "";
	    Double latitude = 0.0;
	    Double longitude = 0.0;
	    float vitesse = 0;
	    String accountIDlast = "";
	    Double latitudelast = 0.0;
	    Double longitudelast = 0.0;
	    Timestamp dateEVlast = null;
	    int speed,speedmax = 0,nbevents=0;
	    //String sql = "SELECT accountID, FROM_UNIXTIME(timestamp) as dateEvt, deviceID, latitude, longitude, max(speedKPH) as speedKPH FROM EventData WHERE accountID='airtel' and DATE_FORMAT(FROM_UNIXTIME(timestamp),'%y-%m-%d') = DATE_FORMAT(NOW(),'%y-%m-%d');";
        String sql = "SELECT accountID, dateEvt, deviceID, lat as latitude, lon as longitude, speed as speedKPH, tmps FROM datatmpEvt where deviceID='"+dvId+"';";
        try {
	        rs = s.executeQuery(sql);
	        for(flag = rs.next(); flag; flag = rs.next())
	        {
	        	accountID = rs.getString("accountID");
	            if(accountID == null)
	            	accountID = "";
	            deviceID = rs.getString("deviceID");
	            if(deviceID == null)
	            	deviceID = "";
	            Timestamp dateEV = rs.getTimestamp("dateEvt");
	            //System.out.println(accountID + " " + deviceID + " " + new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(dateEV));
	            latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
	            longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
	            vitesse = rs.getObject("speedKPH") != null ? rs.getFloat("speedKPH") : null;
	            speed = Math.round(vitesse);
	            if (speed > speedmax) {
	            	deviceIDlast = deviceID;
	            	dateEVlast = dateEV;
	            	accountIDlast = accountID;
	            	latitudelast = latitude;
	            	longitudelast = longitude;
	            	speedmax = speed;
	            }
	            //tmps = rs.getObject("tmps") != null ? rs.getInt("tmps") : null;
	            //System.out.println("accountID:" + accountID + " deviceID:" + deviceID + " date:" + dateEV + " latitude:" + latitude + " longitude:"+longitude + " vitesse:" +vitesse );
	        }
	        if ( speedmax > 0){
	            //Array(Array(-18.94375, 47.50367, 20151208070953, 289, 0), Array(-18.94341, 47.50208, 71013, 309, 32))
	            eventsInfos = eventsInfos + "Array("+latitudelast+", " + longitudelast +", " + new SimpleDateFormat("yyyyMMddHHmmss").format(dateEVlast) +", " + new SimpleDateFormat("HHmmss").format(dateEVlast) +",\"\", \"Vitesse maximale\", " + speedmax +", -2"+")";
	            nbevents++;
	        }

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (nbevents > 0){
        	eventsInfos = eventsInfos + ")";
    		
            sql = "SELECT accountID, deviceID, vehicleMake, vehicleModel, licensePlate FROM Device where accountID='"+accountID+"' and deviceID='"+deviceID+"';";
            try {
            	 rs = s.executeQuery(sql);
                 if(rs.next())
                 {
                	 String vehicleMake = rs.getObject("vehicleMake") != null ? rs.getString("vehicleMake") : null;
                	 String vehicleModel = rs.getObject("vehicleModel") != null ? rs.getString("vehicleModel") : null;
                	 String licensePlate = rs.getObject("licensePlate") != null ? rs.getString("licensePlate") : null;
                	 eventsInfos = "Array(" + eventsInfos + ", \""+licensePlate+" "+vehicleModel+"\")";
                	 
                 }
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
	        System.out.println(deviceIDlast +": eventsInfos");
	        System.out.println(eventsInfos);
        }else {
        	eventsInfos="";
        	System.out.println(deviceIDlast +": AUCUN eventsInfos");
        }
		return eventsInfos;
	}

	private static String getDurationString(int seconds) {

	    int hours = seconds / 3600;
	    int minutes = (seconds % 3600) / 60;
	    seconds = seconds % 60;

	    return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	}
	
	private static String twoDigitString(int number) {

	    if (number == 0) {
	        return "00";
	    }

	    if (number / 10 == 0) {
	        return "0" + number;
	    }

	    return String.valueOf(number);
	}
	
	public static void calculTemp(Connection c, Statement s){
		int tmp = 0;
		boolean flag;
		long diff = 0L;
		int tmpidlast = 0;
		String deviceIDlast = null;
		Timestamp dateEVlast = null;
		
        String sql = "SELECT tmpid, accountID, deviceID, dateEvt FROM datatmpEvt order by deviceID, dateEvt;";
        try {
	        ResultSet rs = s.executeQuery(sql);
	        for(flag = rs.next(); flag; flag = rs.next())
	        {
                int tmpid = rs.getObject("tmpid") != null ? rs.getInt("tmpid") : null;
				String deviceID = rs.getObject("deviceID") != null ? rs.getString("deviceID") : null;
				Timestamp dateEV = rs.getTimestamp("dateEvt");
				if (deviceIDlast != null){
					if (deviceIDlast.equalsIgnoreCase(deviceID)){
						diff = dateEV.getTime() - dateEVlast.getTime() ;
					}else{
						diff = 0;
					}
					tmp = (int) diff / 1000;
					//System.out.println("Temps: "+ tmp);
					String sentence = "UPDATE datatmpEvt SET tmps = ?,dateEvt = ? WHERE tmpid = ?";
	                PreparedStatement pstmt = c.prepareStatement(sentence);
	                pstmt.setInt(1, tmp);
	                pstmt.setTimestamp(2, dateEVlast);
	                pstmt.setInt(3, tmpidlast);
	                pstmt.executeUpdate();
				} 
                tmpidlast = tmpid;
                deviceIDlast = deviceID;
                dateEVlast = dateEV;
			}
	        if (deviceIDlast != null){
	        	long unixTimestamp = Instant.now().getEpochSecond();
	        	diff = unixTimestamp - dateEVlast.getTime();
	        	tmp = (int) diff / 1000;
	        	//System.out.println("Temps: "+ tmp);
	        	String sentence = "UPDATE datatmpEvt SET tmps = ?,dateEvt = ? WHERE tmpid = ?";
                PreparedStatement pstmt = c.prepareStatement(sentence);
                pstmt.setInt(1, tmp);
                pstmt.setTimestamp(2, dateEVlast);
                pstmt.setInt(3, tmpidlast);
                pstmt.executeUpdate();
	        }	
	        rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void createTemporaryTable(Connection c, Statement s, String account) {
		ResultSet rs = null;
		boolean flag;
		boolean first = true;
        String devIDlast = "";
    	String acIDlast = "";
        Timestamp dateEVlast = null;
        double latitudelast = 0;
        double longitudelast = 0;
        int vitesselast = 0;
        int nbevt=0;
        int duree = 0;
        int i = 0;
			
        String sentence = "CREATE TEMPORARY TABLE datatmpEvt( tmpid int NOT NULL AUTO_INCREMENT, accountID varchar(45), deviceID varchar(45), dateEvt Timestamp, lat double, lon double, speed int, tmps int, first boolean, PRIMARY KEY(tmpid), INDEX(tmpid));";
        try {
    		Statement st = c.createStatement();
            s.executeUpdate(sentence);
            String sql = "SELECT count(*) as nbrDevice from Device WHERE accountID='"+account+"';";
            ResultSet rs1 = st.executeQuery(sql);
            if (rs1.next()) {
            	int nbrDevice = rs1.getObject("nbrDevice") != null ? rs1.getInt("nbrDevice") : null;
            	deviceID = new String[nbrDevice];
            	sql = "SELECT accountID, deviceID from Device WHERE accountID='"+account+"';";
            	ResultSet rs2 = st.executeQuery(sql);
            	for(flag = rs2.next(); flag; flag = rs2.next())
	            {
	            	deviceID[i] = rs2.getString("deviceID");
	            	System.out.println(rs2.getString("accountID") + " " + deviceID[i]);
	            	i++;
	            }
 	            //String sql = "SELECT accountID, FROM_UNIXTIME(timestamp) as timestamp, deviceID, latitude, longitude, speedKPH FROM EventData WHERE accountID='telo' and deviceID='telo1' and DATE_FORMAT(FROM_UNIXTIME(timestamp),'%y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 day),'%y-%m-%d') order by deviceID,timestamp;";
	            sql = "SELECT accountID, FROM_UNIXTIME(timestamp) as timestamp, deviceID, latitude, longitude, speedKPH FROM EventData WHERE accountID='"+account+"' and DATE_FORMAT(FROM_UNIXTIME(timestamp),'%y-%m-%d') = DATE_FORMAT(NOW(),'%y-%m-%d') order by deviceID,timestamp;";
		        rs = st.executeQuery(sql);
		        for(flag = rs.next(); flag; flag = rs.next())
	            {
	            	String acID = rs.getString("accountID");
	                if(acID == null)
	                	acID = "";
	                String devID = rs.getString("deviceID");
	                if(devID == null)
	                	devID = "";
	                Timestamp dateEV = rs.getTimestamp("timestamp");
	                double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
	                double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
	                int vitesse = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
	                if (devIDlast.equalsIgnoreCase(devID)) {
	                	first = false;
	                }
	                else {
	                	first = true;
	                }
	                if (first) {
	                	duree = 0;
	                	if (vitesselast > 0){
	    	                sentence = "INSERT INTO datatmpEvt(accountID, deviceID, dateEvt, lat, lon, speed, tmps, first) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	    	                PreparedStatement pstmt = c.prepareStatement(sentence);
	    	                pstmt.setString(1, acIDlast);
	    	                pstmt.setString(2, devIDlast);
	    	                pstmt.setTimestamp(3, dateEVlast);
	    	                pstmt.setDouble(4, latitudelast);
	    	                pstmt.setDouble(5, longitudelast);
	    	                pstmt.setInt(6, 0);
	    	                pstmt.setInt(7, duree);
	    	                pstmt.setBoolean(8, true);
	    	                pstmt.executeUpdate();
	    	                nbevt++;
	                	}else{
	    	                sentence = "INSERT INTO datatmpEvt(accountID, deviceID, dateEvt, lat, lon, speed, tmps, first) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	    	                PreparedStatement pstmt = c.prepareStatement(sentence);
	    	                pstmt.setString(1, acID);
	    	                pstmt.setString(2, devID);
	    	                pstmt.setTimestamp(3, dateEV);
	    	                pstmt.setDouble(4, latitude);
	    	                pstmt.setDouble(5, longitude);
	    	                pstmt.setInt(6, 0);
	    	                pstmt.setInt(7, duree);
	    	                pstmt.setBoolean(8, first);
	    	                pstmt.executeUpdate();
	    	                nbevt++;                		
	                	}
	                	devIDlast = devID;
	                }else{
	                    if (devIDlast.equalsIgnoreCase(devID) && acIDlast.equalsIgnoreCase(acID) && vitesselast < 1 && vitesse < 1){
	                    	
	                    }else{
	                    	duree = (int) (dateEV.getTime() - dateEVlast.getTime())/1000 ;
			                sentence = "INSERT INTO datatmpEvt(accountID, deviceID, dateEvt, lat, lon, speed, tmps, first) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
			                PreparedStatement pstmt = c.prepareStatement(sentence);
			                pstmt.setString(1, acID);
			                pstmt.setString(2, devID);
			                pstmt.setTimestamp(3, dateEV);
			                pstmt.setDouble(4, latitude);
			                pstmt.setDouble(5, longitude);
			                pstmt.setInt(6, vitesse);
	    	                pstmt.setInt(7, duree);
	    	                pstmt.setBoolean(8, first);
			                pstmt.executeUpdate();
			                nbevt++;
	                    }
	                }
	                //System.out.println(acID + " " + devID + " " + new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(dateEV) + " " + latitude + " "+ longitude + " " + vitesse);
	                devIDlast = devID;
	            	acIDlast = acID;
	                dateEVlast = dateEV;
	                latitudelast = latitude;
	                longitudelast = longitude;
	                vitesselast = vitesse;
	            }
		        rs2.close();
	            sentence = "INSERT INTO datatmpEvt(accountID, deviceID, dateEvt, lat, lon, speed, tmps, first) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
	            PreparedStatement pstmt = c.prepareStatement(sentence);
	            pstmt.setString(1, acIDlast);
	            pstmt.setString(2, devIDlast);
	            pstmt.setTimestamp(3, dateEVlast);
	            pstmt.setDouble(4, latitudelast);
	            pstmt.setDouble(5, longitudelast);
	            pstmt.setInt(6, 0);
	            pstmt.setInt(7, 0);
	            pstmt.setBoolean(8, true);
	            pstmt.executeUpdate();
	            nbevt++;
            }
            rs1.close();
            rs.close();

            sql = "SELECT tmpid, accountID, deviceID, dateEvt, lat, lon, speed, tmps, first FROM datatmpEvt order by deviceID, dateEvt;";
    	    rs = s.executeQuery(sql);
    	    for(flag = rs.next(); flag; flag = rs.next())
    	        {
                    int tmpid = rs.getObject("tmpid") != null ? rs.getInt("tmpid") : null;
    				String accountID = rs.getObject("accountID") != null ? rs.getString("accountID") : null;
    				String deviceID = rs.getObject("deviceID") != null ? rs.getString("deviceID") : null;
    				Timestamp dateEV = rs.getTimestamp("dateEvt");
    	            double latitude = rs.getObject("lat") != null ? rs.getDouble("lat") : null;
    	            double longitude = rs.getObject("lon") != null ? rs.getDouble("lon") : null;
    	            int vitesse = rs.getObject("speed") != null ? rs.getInt("speed") : null;
                    int tmps = rs.getObject("tmps") != null ? rs.getInt("tmps") : null;
                    first = rs.getObject("first") != null ? rs.getBoolean("first") : null;
                    System.out.println(accountID + " " + deviceID + " " + new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(dateEV) + " " + dec.format(latitude) + " "+ dec.format(longitude) + " " + vitesse + " " + tmps + " " + first);
    	        }
        } catch (SQLException e) {
            e.printStackTrace();
        } /*finally {
            try {
                s.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }*/
        System.out.println("Chargement de la table terminé. Nb="+nbevt);
    }	
}

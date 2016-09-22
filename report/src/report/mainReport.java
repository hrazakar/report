package report;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;



public class mainReport {
	private static final Logger LOGGER = Logger.getLogger(
	Thread.currentThread().getStackTrace()[0].getClassName() );
	private static final DecimalFormat dec = new DecimalFormat("#00.000000");
	private static String[] deviceID;
	private static String date_trt;
    public  static final char    CSV_SEPARATOR_CHAR             = '|';

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

	    String accountID = "telo";
	    
	    String pathInfos = "";
	    String stopsInfos = "";
	    String eventsInfos = "";
		//	<script type="text/javascript">
		//		stopsInfos[0] = Array(Array(Array(-18.14335, 49.38557, 20151208184519, "", null, 0)), "1273 TAM -WULLING");
		//	</script>
	    String Infos = "";
		//new Array(new Array(null, stops[0], null), new Array(path[1], stops[1], events[1]), new Array(path[2], stops[2], events[2]), new Array(path[3], stops[3], events[3]), new Array(null, stops[4], null), new Array(path[5], stops[5], events[5]), new Array(null, stops[6], null))
	    String gMapElts = "new Array(";
		String infoTab = "";
		//Food & Beverage - Compte-rendu du 08/12/2015
		String titre_date = "";
	    int nbrdevice = 0;

	    LinkedList<Events> pointsList;
		File htmlTemplateFile = new File("../report/src/report/template.html");
		//File htmlTemplateFile = new File("d:\\template-V0.html");
		String htmlString;
		htmlString = FileUtils.readFileToString(htmlTemplateFile);
	    try {
	        Class.forName("com.mysql.jdbc.Driver");
	        con = DriverManager.getConnection(url, user, password);
	        st = con.createStatement();
	        pointsList = createTemporaryTable(con, st, accountID); //"FROM_UNIXTIME(dateEvt,'%d%m%Y%H%i%s') CREATE TEMPORARY TABLE datatmpEvt( accountID varchar(45), deviceID varchar(45), dateEvt Timestamp, lat double, lon double, speed int);";
	        //calculTemp(con, st);
	        nbrdevice = deviceID.length;
	        System.out.println("Nombre de device: " + nbrdevice);
	        for (int i=0;i<deviceID.length;i++){
	        	pathInfos = createpathInfos(con, st, deviceID[i]);
	        	if (pathInfos != null){
	        		pathInfos = "<script type=\"text/javascript\"> pathInfos["+i+"] = " + pathInfos + ";</script>";
	        		Infos = Infos + pathInfos;
		        	System.out.println(pathInfos);
	        	}
		        stopsInfos = createstopsInfos(con, st, deviceID[i]);
		        if (stopsInfos == null) stopsInfos = createstopsInfosnull(con, st, accountID, deviceID[i]);
	        	if (stopsInfos != null){
	        		stopsInfos = "<script type=\"text/javascript\"> stopsInfos["+i+"] = " + stopsInfos + ";</script>";
	        		Infos = Infos + stopsInfos; 
		        	System.out.println(stopsInfos);
	        	}
		        eventsInfos = createeventsInfos(con, st, deviceID[i]);
	        	if (eventsInfos != null){
	        		eventsInfos = "<script type=\"text/javascript\"> eventsInfos["+i+"] = " + eventsInfos + ";</script>";
	        		Infos = Infos + eventsInfos; 
		        	System.out.println(eventsInfos);
	        	}
	        	String pathElts = "path["+i+"]";
	        	if(pathInfos == null ) pathElts = "null"; 
	        	String stopElts = "stops["+i+"]";
	        	if(stopsInfos == null ) stopElts = "null"; 
	        	String eventsElts = "events["+i+"]";
	        	if(eventsInfos == null ) eventsElts = "null"; 
	        	if (i > 0) gMapElts = gMapElts + ", ";
	        	gMapElts = gMapElts + "new Array("+ pathElts +", " + stopElts + ", " + eventsElts +")";
	        	//System.out.println(Infos);
	        	infoTab = infoTab + createinfoTab(con, st, accountID, deviceID[i], i, pointsList);
	        }
        	gMapElts = gMapElts + ")";
        	System.out.println(gMapElts);

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
		String title = "Araka";
		String filename = "";
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date dateBefore1Days = cal.getTime();
		System.out.println(new SimpleDateFormat("dd/MM/yyyy").format(dateBefore1Days));
		date_trt = new SimpleDateFormat("dd/MM/yyyy").format(dateBefore1Days);
		titre_date = accountID + " - Compte-rendu du " + date_trt ;
		htmlString = htmlString.replace("$title", title);
		htmlString = htmlString.replace("$GAT_Infos", Infos);
		htmlString = htmlString.replace("$GAT_gMapElts", gMapElts);
		htmlString = htmlString.replace("$GAT_infoTab", infoTab);
		htmlString = htmlString.replace("$GAT_nbrdevice", Integer.toString(nbrdevice));
		htmlString = htmlString.replace("$GAT_Titre_date", titre_date);
		htmlString = htmlString.replace("$GAT_dateBegin", date_trt + " 00:00:00");
		htmlString = htmlString.replace("$GAT_dateEnd", date_trt + " 23:59:59");
		String datefile = new SimpleDateFormat("dd-MM-yyyy").format(dateBefore1Days);
		filename = accountID + "_COMPTE-RENDU_" + datefile + ".html";
		File newHtmlFile = new File("d:\\"+filename);
		FileUtils.writeStringToFile(newHtmlFile, htmlString);

	}

	public static String createinfoTab(Connection c, Statement s, String acId, String dvId, int irang, LinkedList pList){
	    //<tr>
	    //	<td>3</td> Rang
	    //	<td>113</td> Identif
	    //	<td>9114 TAN -JAC 3.5T</td> Info véhicule
	    //	<td>2015-12-08 06:59:07</td> date début
	    //	<td>2015-12-08 13:37:40</td> date fin
	    //	<td>00-00-00 02:15:34</td> roulage
	    //	<td>00-00-00 04:22:59</td> Arrêt
	    //	<td>51</td> distance total
	    //	<td>22</td> vitesse moyenne
	    //	<td>79</td> Vitesse max
	    //	<td><input type="checkbox" style="cursor:pointer;" id="cb3" title="Cliquez ici pour afficher le trajet de ce véhicule" onclick="if (this.checked) {if (isReplayRunning) pausePathReplay(true, true);if (curCheckedRowId > -1) {if (sb = document.getElementById('sliderbg' + curCheckedRowId)) sb.style.display = 'none';if (cb = document.getElementById('cb' + curCheckedRowId)) cb.title='Cliquez ici pour afficher le trajet de ce véhicule';};gMapDisplayData(3, true, true, false);secuDisplayData(3,true);curCheckedRowId=3;this.title='Cliquez ici pour masquer le trajet de ce véhicule';}else {pausePathReplay(true, true);if (sb = document.getElementById('sliderbg' + 3)) sb.style.display = 'none';pathFilterDiv.style.display = 'none';gMapHideData(3, false);secuDisplayData(3,false);curCheckedRowId = -1;this.title='Cliquez ici pour afficher le trajet de ce véhicule';};"></td>
	    //	<input type="checkbox" style="cursor:pointer;" id="cb0" title="Cliquez ici pour afficher le trajet de ce véhicule" onclick="if (this.checked) {if (isReplayRunning) pausePathReplay(true, true);if (curCheckedRowId > -1) {if (sb = document.getElementById('sliderbg' + curCheckedRowId)) sb.style.display = 'none';if (cb = document.getElementById('cb' + curCheckedRowId)) cb.title='Cliquez ici pour afficher le trajet de ce véhicule';};gMapDisplayData(0, true, true, false);secuDisplayData(0,true);curCheckedRowId=0;this.title='Cliquez ici pour masquer le trajet de ce véhicule';}else {gMapHideData(0, false);secuDisplayData(0,false);curCheckedRowId = -1;this.title='Cliquez ici pour afficher le trajet de ce véhicule';};">
	    //	<td>-18.93935 47.56441</td> coordonnée evenement
	    //</tr>
		boolean flag;
		ResultSet rs = null;
	    int nb =0;
		String infoTab = "";
		double odometreDeb = 0;
		double odometreFin = 0;
        double latitudemax = 0;
        double longitudemax = 0;
        double odometerKMlast = 0;
        double odometerOffsetKMlast = 0;
        double distance = 0;
        Timestamp dateEVlast= null, dateEVdeb=null;
		int speedmax = 0;
		int dureeroulage = 0;
		int dureearret = 0;
		
		infoTab = "<tr><td>"+irang+"</td><td>"+dvId+"</td>";
        String sql = "select accountID, deviceID, vehicleMake, vehicleModel, licensePlate FROM Device where accountID='"+acId+"' and deviceID='"+dvId+"';";
        try {
	        rs = s.executeQuery(sql);
		
            if(rs.next())
            {
           	 String vehicleMake = rs.getObject("vehicleMake") != null ? rs.getString("vehicleMake") : null;
           	 String vehicleModel = rs.getObject("vehicleModel") != null ? rs.getString("vehicleModel") : null;
           	 String licensePlate = rs.getObject("licensePlate") != null ? rs.getString("licensePlate") : null;
           	 infoTab = infoTab + "<td>"+licensePlate+" "+vehicleModel+"</td>";
           	 sql = "SELECT accountID, dateEvt as dateEV, deviceID, lat as latitude, lon as longitude, speed as speedKPH, tmps, first, odometerKM, odometerOffsetKM FROM datatmpEvt where deviceID='"+dvId+"' order by dateEV;";
           	 rs = s.executeQuery(sql);
           	 for(flag = rs.next(); flag; flag = rs.next())
           	 {
		        String acID = rs.getObject("accountID") != null ? rs.getString("accountID") : null;
		        String devID = rs.getObject("deviceID") != null ? rs.getString("deviceID") : null;
	            Timestamp dateEV = rs.getTimestamp("dateEV");
	            double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
	            double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
	            int vitesse = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
	            int duree = rs.getObject("tmps") != null ? rs.getInt("tmps") : null;
                double odometerKM = rs.getObject("odometerKM") != null ? rs.getDouble("odometerKM") : null;
                double odometerOffsetKM = rs.getObject("odometerOffsetKM") != null ? rs.getDouble("odometerOffsetKM") : null;
                if (nb == 0){
                	odometreDeb = odometerKM + odometerOffsetKM;  
                	dateEVdeb = dateEV;
                }
 	            int speed = Math.round(vitesse);
 	            if (speed > speedmax) {
 	            	speedmax = speed;
 	            	latitudemax = latitude;
 	            	longitudemax = longitude;
 	            }
 	            if (speed > 0){ // Calcul temps de roulage
 	            	dureeroulage = dureeroulage + duree;
 	            }else{ // Calcul temps d'arrêt
 	            	dureearret = dureearret + duree;
 	            } 	            	
                dateEVlast = dateEV;
                odometerKMlast = odometerKM;
                odometerOffsetKMlast = odometerOffsetKM;

 	            nb++;
           	 }
           	 if (nb > 1){
           		 distance = 0.0D;
           		 for (int i = 0; i < pList.size();i++){
           			Events evt = (Events) pList.get(i);
           			if (evt.getDeviceID().equals(dvId)){
           			  String request = "";
           			  int k = i + 20;
           			  if (k >= pList.size()) k = pList.size();
           			  for (int j = i; j < k; j++) {
                 		Events evtnxt = (Events) pList.get(j);
               			if (evtnxt.getDeviceID().equals(dvId)){           				  
               				request = request + "loc=" + evtnxt.getLatitude() + "," + evtnxt.getLongitude();         			
             			  }
           			  }
           			  request = "http://77.72.92.132:5000/viaroute?" + request+"&instructions=false&alt=false";
           			  String rep = sendGet(request, "viaroute");
           			  distance = distance + Double.parseDouble(rep)/1000.0;
           			  request = "";
           			  i = k - 1;
           			}
           		 }
      			 System.out.println("Distance OSRM: " + distance);  
           		 //distance = (odometerKMlast + odometerOffsetKMlast) - odometreDeb;
	             System.out.println("Distance: " + (int)distance + "kms");
	             System.out.println("Vitesse max: " + speedmax + "kms/h");
	             System.out.println("Temps roulage: " + (int) dureeroulage + "s");
	             double vitessemoy =  distance/(dureeroulage/3600.0);
	             System.out.println("Vitesse moy: " + (int)vitessemoy + " kms/h");
	             int numberOfDays;
	             int numberOfHours;
	             int numberOfMinutes;
	             int numberOfSeconds;

	             //numberOfDays = dureeroulage / 86400;
	             numberOfHours = ((int)dureeroulage % 86400 ) / 3600 ;
	             numberOfMinutes = (((int)dureeroulage % 86400 ) % 3600 ) / 60; 
	             numberOfSeconds = (((int)dureeroulage % 86400 ) % 3600 ) % 60  ;
	             String roulage = numberOfHours + ":" + numberOfMinutes + ":" + numberOfSeconds;
	             numberOfHours = ((int)dureearret % 86400 ) / 3600 ;
	             numberOfMinutes = (((int)dureearret % 86400 ) % 3600 ) / 60; 
	             numberOfSeconds = (((int)dureearret % 86400 ) % 3600 ) % 60  ;
	             String arret = numberOfHours + ":" + numberOfMinutes + ":" + numberOfSeconds;
	             System.out.println("Temps arrêt: " + (int) dureearret + "s" + " "+ arret);
	             long diff = dateEVlast.getTime() - dateEVdeb.getTime() ;
	             int dureeTH = (int) diff / 1000;
	             //if (vitessemoy > 150) vitessemoy = 0;
	             System.out.println("Temps Total: " + dureeTH + "s");
	             infoTab = infoTab + "<td>"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateEVdeb)+"</td>";
	             infoTab = infoTab + "<td>"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dateEVlast)+"</td>";
	             infoTab = infoTab + "<td>00-00-00 "+roulage+"</td>";
	             infoTab = infoTab + "<td>00-00-00 "+arret+"</td>";
	             infoTab = infoTab + "<td>"+(int)distance+"</td>";
	             infoTab = infoTab + "<td>"+(int)vitessemoy+"</td>";
	             infoTab = infoTab + "<td>"+speedmax+"</td>";
	             infoTab = infoTab + "<td><input type=\"checkbox\" style=\"cursor:pointer;\" id=\"cb"+irang+"\" title=\"Cliquez ici pour afficher le trajet de ce véhicule\" onclick=\"if (this.checked) {if (isReplayRunning) pausePathReplay(true, true);if (curCheckedRowId > -1) {if (sb = document.getElementById('sliderbg' + curCheckedRowId)) sb.style.display = 'none';if (cb = document.getElementById('cb' + curCheckedRowId)) cb.title='Cliquez ici pour afficher le trajet de ce véhicule';};gMapDisplayData("+irang+", true, true, false);secuDisplayData("+irang+",true);curCheckedRowId="+irang+";this.title='Cliquez ici pour masquer le trajet de ce véhicule';}else {pausePathReplay(true, true);if (sb = document.getElementById('sliderbg' + "+irang+")) sb.style.display = 'none';pathFilterDiv.style.display = 'none';gMapHideData("+irang+", false);secuDisplayData("+irang+",false);curCheckedRowId = -1;this.title='Cliquez ici pour afficher le trajet de ce véhicule';};\"></td>";
	             infoTab = infoTab + "<td>"+dec.format(latitudemax) + " "+ dec.format(longitudemax)+"</td></tr>";
           	 }else {
           		 infoTab = infoTab + "<td></td><td></td><td></td><td></td><td>0</td><td>0</td><td>0</td>";
           		 infoTab = infoTab + "<td><input type=\"checkbox\" style=\"cursor:pointer;\" id=\"cb"+irang+"\" title=\"Cliquez ici pour afficher le trajet de ce véhicule\" onclick=\"if (this.checked) {if (isReplayRunning) pausePathReplay(true, true);if (curCheckedRowId > -1) {if (sb = document.getElementById('sliderbg' + curCheckedRowId)) sb.style.display = 'none';if (cb = document.getElementById('cb' + curCheckedRowId)) cb.title='Cliquez ici pour afficher le trajet de ce véhicule';};gMapDisplayData("+irang+", true, true, false);secuDisplayData("+irang+",true);curCheckedRowId="+irang+";this.title='Cliquez ici pour masquer le trajet de ce véhicule';}else {gMapHideData("+irang+", false);secuDisplayData("+irang+",false);curCheckedRowId = -1;this.title='Cliquez ici pour afficher le trajet de ce véhicule';};\"></td>";
	             infoTab = infoTab + "<td></td></tr>";
           	 }
            }
            rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("infoTab: "+ infoTab);
		return infoTab;
	}
	
	public static String createstopsInfos(Connection c, Statement s, String dvId){
		boolean flag;
		boolean first;
		ResultSet rs = null;
		String stopsInfos ="";
	    String accountID = "";
	    String deviceID = "";
	    String deviceIDlast = "";
	    Double latitude = 0.0;
	    Double longitude = 0.0;
	    Timestamp dateEV;
	    Timestamp dateEVfirst, timestamp = null;
	    String temps ="";
	    String temps2 ="";
	    String datem;
	    int vitesse = 0;
	    int tmps, nbstop = 0;
	    int nb =0, nblecture = 0;
		
        String sql = "SELECT count(*) as nbinfo FROM datatmpEvt where deviceID='"+dvId+"';";
        try {
	        rs = s.executeQuery(sql);
	        if (rs.next()) {
	        	int nbinfo = rs.getObject("nbinfo") != null ? rs.getInt("nbinfo") : null;
	        	if (nbinfo > 1){
	                sql = "SELECT accountID, dateEvt as dateEV, deviceID, lat as latitude, lon as longitude, speed as speedKPH, tmps, first FROM datatmpEvt where deviceID='"+dvId+"' order by deviceID,dateEV;";
	    	        rs = s.executeQuery(sql);
	    	        for(flag = rs.next(); flag; flag = rs.next())
	    	        {
	    	        	nblecture++;
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
	    	            if (nb==0){ 
	    	            	stopsInfos = "Array(";
	    	            	deviceIDlast = deviceID;
	    	            }
	    	            if (first || nblecture==nbinfo){
	    	            	dateEVfirst = dateEV;
	    	            	temps2 = null;
	    	            	temps = "\"\"";
	    	            }else{
	    	            	temps = "\"00-00-00 "+getDurationString(tmps)+"\"";
	    	            	//temps2 = Integer.toString(tmps);
	    	            	Calendar cal = Calendar.getInstance();
	    	                cal.setTimeInMillis(dateEV.getTime());
	    	                cal.add(Calendar.SECOND, tmps);
	    	                timestamp = new Timestamp(cal.getTime().getTime());
	    	                temps2 = new SimpleDateFormat("HHmmss").format(timestamp); 
	    	            }
	    		        if ( vitesse < 1){
	    		            //Array(Array(-18.94375, 47.50367, 20151208070953, 289, 0), Array(-18.94341, 47.50208, 71013, 309, 32))
	    		            if (deviceIDlast.equals(deviceID)){
	    		            	if (nb>0) stopsInfos = stopsInfos + ", ";
	    		            	if (nb==0 || nblecture==nbinfo) datem = new SimpleDateFormat("yyyyMMddHHmmss").format(dateEV);
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
	        	}
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
	        //System.out.println(deviceIDlast +": stopsInfos");
	        //System.out.println(stopsInfos);
        }else{
	        stopsInfos = null;
	        System.out.println(deviceIDlast +": AUCUN stopsInfos");
        }
		return stopsInfos;
	}

	public static String createstopsInfosnull(Connection c, Statement s, String acId, String dvId){
		ResultSet rs = null;
		String stopsInfos = "";

        String sql = "SELECT accountID, FROM_UNIXTIME(timestamp) as timestamp, deviceID, latitude, longitude, speedKPH FROM EventData WHERE accountID='"+acId+"' AND deviceID='"+dvId+"' ORDER BY timestamp DESC LIMIT 1;";
        try {
	        rs = s.executeQuery(sql);
            if (rs.next()) {
            	stopsInfos = "Array(";
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
	       //System.out.println(dvId +": stopsInfosnull");
	       //System.out.println(stopsInfos);
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
	    String pathinfos = "";
		
        String sql = "SELECT count(*) as nbinfo FROM datatmpEvt where deviceID='"+dvId+"';";
        try {
	        rs = s.executeQuery(sql);
	        if (rs.next()) {
	        	pathinfos = "Array(";
	        	int nbinfo = rs.getObject("nbinfo") != null ? rs.getInt("nbinfo") : null;
	        	if (nbinfo > 1){
	                sql = "SELECT accountID, dateEvt as dateEV, deviceID, lat as latitude, lon as longitude, speed as speedKPH, tmps, first FROM datatmpEvt where deviceID='"+dvId+"' order by dateEV;";
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
	        	}
	        }
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
	       // System.out.println(deviceIDlast +": pathInfos");
	        //System.out.println(pathinfos);
        }else{
	        pathinfos = null;
	        System.out.println(deviceIDlast +": AUCUN pathInfos");       	
        }
		return pathinfos;
	}
	
	public static String createeventsInfos(Connection c, Statement s, String dvId){
		boolean flag;
		ResultSet rs=null;
		String eventsInfos = "";
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
	            if (nbevents == 0) {
	            	eventsInfos = "Array(";
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
	        //System.out.println(deviceIDlast +": eventsInfos");
	        //System.out.println(eventsInfos);
        }else {
        	eventsInfos=null;
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

	public static boolean calculProx(double latitude, double longitude, double latitudelast, double longitudelast){
		boolean proxim = false;
		
		if (Math.abs(latitudelast - latitude) < 0.00009 && Math.abs(longitudelast - longitude) < 0.00009) proxim = true;
		else proxim = false;
		
		return proxim;	
	}
	
	public static LinkedList createTemporaryTable(Connection c, Statement s, String account) {
		ResultSet rs = null;
		boolean flag, proximite;
		boolean first = true;
		boolean topwrite = true;
		boolean statuslast = true;
        String devIDlast = "";
    	String acIDlast = "";
        Timestamp dateEVlast = null;
        double latitudelast = 0;
        double longitudelast = 0;
        double odometerKMlast = 0;
        double odometerOffsetKMlast = 0;
        double vitesselast = 0;
        int nbevt=0;
        int duree = 0;
        int i = 0;
        
        LinkedList<Events> eventsList = new LinkedList<Events>();	
        
        StringBuffer sb = new StringBuffer();
        String sentence = "CREATE TEMPORARY TABLE datatmpEvt( tmpid int NOT NULL AUTO_INCREMENT, accountID varchar(45), deviceID varchar(45), dateEvt Timestamp, lat double, lon double, speed int, tmps int, first boolean, odometerKM double, odometerOffsetKM double, PRIMARY KEY(tmpid), INDEX(tmpid));";
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
            	//DATE_FORMAT(NOW(),'%y-%m-%d')
            	sql = "SELECT accountID, FROM_UNIXTIME(timestamp) as timestamp, deviceID, latitude, longitude, speedKPH, odometerKM, odometerOffsetKM FROM EventData WHERE accountID='"+account+"' and longitude <> 0.0 and DATE_FORMAT(FROM_UNIXTIME(timestamp),'%y-%m-%d') = DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 day),'%y-%m-%d') order by deviceID,timestamp;";
		        rs = st.executeQuery(sql);
		        // lecture premier enregistrement
	            if (rs.next()) {
	            	acIDlast = rs.getObject("accountID") != null ? rs.getString("accountID") : null;
	                devIDlast = rs.getObject("accountID") != null ? rs.getString("deviceID") : null;
	                dateEVlast = rs.getTimestamp("timestamp");
	                latitudelast = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
	                longitudelast = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
	                //vitesselast = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
	                vitesselast = 0.;
	                odometerKMlast = rs.getObject("odometerKM") != null ? rs.getDouble("odometerKM") : null;
	                odometerOffsetKMlast = rs.getObject("odometerOffsetKM") != null ? rs.getDouble("odometerOffsetKM") : null;
	                statuslast = true;
	            	
			        for(flag = rs.next(); flag; flag = rs.next())
		            {
			        	String acID = rs.getObject("accountID") != null ? rs.getString("accountID") : null;
			        	String devID = rs.getObject("accountID") != null ? rs.getString("deviceID") : null;
		                Timestamp dateEV = rs.getTimestamp("timestamp");
		                double latitude = rs.getObject("latitude") != null ? rs.getDouble("latitude") : null;
		                double longitude = rs.getObject("longitude") != null ? rs.getDouble("longitude") : null;
		                double vitesse = rs.getObject("speedKPH") != null ? rs.getInt("speedKPH") : null;
		                double odometerKM = rs.getObject("odometerKM") != null ? rs.getDouble("odometerKM") : null;
		                double odometerOffsetKM = rs.getObject("odometerOffsetKM") != null ? rs.getDouble("odometerOffsetKM") : null;
		                //System.out.println(acID + " " + devID + " " + new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(dateEV) + " " + latitude + " "+ longitude + " " + vitesse);
		                //proximite = calculProx(latitude, longitude, latitudelast, longitudelast);
		                if (devIDlast.equalsIgnoreCase(devID) && vitesse < 1 && vitesselast < 1) {
		                	topwrite = false;
		                }
		                else {	
			                long diff = dateEV.getTime() - dateEVlast.getTime() ;
			                duree = (int) diff / 1000;
			                if (devIDlast.equalsIgnoreCase(devID)) {
			                	if (statuslast) { 
			                		first = true;
			                		duree = 0;
			                	}
			                	statuslast = false;
			                }
			                else {
			                	statuslast = true;
			                	duree = 0;
			                }
			                	if (first) { 
			                		duree = 0;
			                		vitesselast = 0;
			                	}
					            sentence = "INSERT INTO datatmpEvt(accountID, deviceID, dateEvt, lat, lon, speed, tmps, first, odometerKM, odometerOffsetKM) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					            PreparedStatement pstmt = c.prepareStatement(sentence);
		    	                pstmt.setString(1, acIDlast);
		    	                pstmt.setString(2, devIDlast);
		    	                pstmt.setTimestamp(3, dateEVlast);
		    	                pstmt.setDouble(4, latitudelast);
		    	                pstmt.setDouble(5, longitudelast);
		    	                pstmt.setDouble(6, vitesselast);
		    	                pstmt.setInt(7, duree);
		    	                pstmt.setBoolean(8, first);
		    	                pstmt.setDouble(9, odometerKMlast);
		    	                pstmt.setDouble(10, odometerOffsetKMlast);
				                pstmt.executeUpdate();
					            nbevt++;
	
					            devIDlast = devID;
				            	acIDlast = acID;
				                dateEVlast = dateEV;
				                latitudelast = latitude;
				                longitudelast = longitude;
				                vitesselast = vitesse;
				                odometerKMlast = odometerKM;
				                odometerOffsetKMlast = odometerOffsetKM;
				                first = false;
		                }
		            }
	                if (topwrite){
				            sentence = "INSERT INTO datatmpEvt(accountID, deviceID, dateEvt, lat, lon, speed, tmps, first, odometerKM, odometerOffsetKM) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
				            PreparedStatement pstmt = c.prepareStatement(sentence);
			                pstmt.setString(1, acIDlast);
			                pstmt.setString(2, devIDlast);
			                pstmt.setTimestamp(3, dateEVlast);
			                pstmt.setDouble(4, latitudelast);
			                pstmt.setDouble(5, longitudelast);
			                pstmt.setDouble(6, 0.0D);
			                pstmt.setInt(7, 0);
			                pstmt.setBoolean(8, first);
			                pstmt.setDouble(9, odometerKMlast);
			                pstmt.setDouble(10, odometerOffsetKMlast);
			                pstmt.executeUpdate();
				            nbevt++;
	                }
	            }
		        rs2.close();
            }
            rs1.close();
            rs.close();

            sql = "SELECT tmpid, accountID, deviceID, dateEvt, lat, lon, speed, tmps, first, odometerKM, odometerOffsetKM FROM datatmpEvt order by deviceID, dateEvt;";
    	    rs = s.executeQuery(sql);
    	    for(flag = rs.next(); flag; flag = rs.next())
    	        {
                    int tmpid = rs.getObject("tmpid") != null ? rs.getInt("tmpid") : null;
    				String accountID = rs.getObject("accountID") != null ? rs.getString("accountID") : null;
    				String deviceID = rs.getObject("deviceID") != null ? rs.getString("deviceID") : null;
    				Timestamp dateEV = rs.getTimestamp("dateEvt");
    	            double latitude = rs.getObject("lat") != null ? rs.getDouble("lat") : null;
    	            double longitude = rs.getObject("lon") != null ? rs.getDouble("lon") : null;
    	            double vitesse = rs.getObject("speed") != null ? rs.getInt("speed") : null;
                    int tmps = rs.getObject("tmps") != null ? rs.getInt("tmps") : null;
                    first = rs.getObject("first") != null ? rs.getBoolean("first") : null;
	                double odometerKM = rs.getObject("odometerKM") != null ? rs.getDouble("odometerKM") : null;
	                double odometerOffsetKM = rs.getObject("odometerOffsetKM") != null ? rs.getDouble("odometerOffsetKM") : null;
                  
	                eventsList.add(new Events(accountID, deviceID, latitude, longitude, vitesse, dateEV, odometerKM, odometerOffsetKM));
	                
	                System.out.println(accountID + " " + deviceID + " " + new SimpleDateFormat("yyy-MM-dd HH:mm:ss").format(dateEV) + " " + dec.format(latitude) + " "+ dec.format(longitude) + " " + vitesse + " " + tmps + " " + first + " "+odometerKM + " "+odometerOffsetKM);
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
        return eventsList;
    }	
	
	// HTTP GET request
	@SuppressWarnings("deprecation")
	private static String sendGet(String url, String type) {
		String genreJson;
		String distance = "0";
		
		try {
			JSONArray genreArray = null;
			if (type.equalsIgnoreCase("match")){
				//url = "http://77.72.92.132:5000/match?loc="+lat1+","+lon1+"&t="+tms1+"loc="+lat2+","+lon2+"&t="+tms2+"&compression=true";
				genreJson = IOUtils.toString(new URL(url));
				JSONObject genreJsonObject = (JSONObject) JSONValue.parseWithException(genreJson);

				genreArray = (JSONArray) genreJsonObject.get("matchings");
				//System.out.println(genreArray);
				if (genreArray == null) {
					System.out.println("JMapData not found in JSON response");
		            distance = "0";
		        }
				else {
			        // get the first genre
			        JSONObject firstGenre = (JSONObject) genreArray.get(0);
			        JSONObject routesummary = (JSONObject) firstGenre.get("route_summary");
			        distance = routesummary.get("total_distance").toString();
				}
			}
			else {
				//url = "http://77.72.92.132:5000/viaroute?loc="+lat1+","+lon1+"&loc="+lat2+","+lon2+"&instructions=false&alt=false";
				genreJson = IOUtils.toString(new URL(url));
				JSONObject genreJsonObject = (JSONObject) JSONValue.parseWithException(genreJson);
				
				JSONObject firstGenre = (JSONObject) genreJsonObject.get("route_summary");
				//System.out.println(firstGenre.get("total_distance").toString());
				distance = firstGenre.get("total_distance").toString();
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return distance;    
	}		

}

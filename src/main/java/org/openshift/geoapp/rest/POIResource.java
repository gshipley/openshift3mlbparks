package org.openshift.geoapp.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.openshift.geoapp.domain.POI;
import org.openshift.geoapp.mongo.DBConnection;
import org.openshift.geoapp.util.Config;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@RequestScoped
@Path("/poi")
public class POIResource {

	@Inject
	private DBConnection dbConnection;
	
	@Inject
	private Config config;
	
	private DBCollection getPOICollection() {
		DB db = dbConnection.getDB();
		DBCollection poiListCollection = db.getCollection(DBConnection.POI_COLLECTION);

		return poiListCollection;
	}

	private POI populatePoiInformation(DBObject dataValue) {
		POI poi = new POI();

		poi.setId(dataValue.get("_id"));
		poi.setPosition(getPosition(dataValue));
		
		Map<String, Object> params = new HashMap<>();
		for (String field : dataValue.keySet()) {
			params.put(field, dataValue.get(field));
		}
		StrSubstitutor popupTemplate = new StrSubstitutor(params);
		poi.setInfo(popupTemplate.replace(config.getPopupTemplate()));
		
		return poi;
	}
	
	private Object getPosition(DBObject dataValue) {
		if (dataValue.containsField("coordinates")) {
			return dataValue.get("coordinates");
		} else if (dataValue.containsField("position")) {
			return dataValue.get("position");
		} else if (dataValue.containsField("pos")) {
			return dataValue.get("pos");
		} else if (dataValue.containsField("location")) {
			return dataValue.get("location");
		}
		
		return null;
	}

	// get all the POIs
	@GET()
	@Produces("application/json")
	public List<POI> getAllPOI() {
		ArrayList<POI> allPOIList = new ArrayList<>();

		DBCollection poiCollection = this.getPOICollection();
		DBCursor cursor = poiCollection.find();
		try {
			while (cursor.hasNext()) {
				allPOIList.add(this.populatePoiInformation(cursor.next()));
			}
		} finally {
			cursor.close();
		}

		return allPOIList;
	}

	@GET
	@Produces("application/json")
	@Path("within")
	public List<POI> findPOIWithin(@QueryParam("lat1") float lat1,
			@QueryParam("lon1") float lon1, @QueryParam("lat2") float lat2,
			@QueryParam("lon2") float lon2) {

		ArrayList<POI> allPOIList = new ArrayList<>();
		DBCollection poiList = this.getPOICollection();

		// make the query object
		BasicDBObject spatialQuery = new BasicDBObject();

		ArrayList<double[]> boxList = new ArrayList<double[]>();
		boxList.add(new double[] { new Float(lon2), new Float(lat2) });
		boxList.add(new double[] { new Float(lon1), new Float(lat1) });

		BasicDBObject boxQuery = new BasicDBObject();
		boxQuery.put("$box", boxList);

		spatialQuery.put("coordinates", new BasicDBObject("$within", boxQuery));
		System.out.println("Using spatial query: " + spatialQuery.toString());

		DBCursor cursor = poiList.find(spatialQuery);
		try {
			while (cursor.hasNext()) {
				allPOIList.add(this.populatePoiInformation(cursor.next()));
			}
		} finally {
			cursor.close();
		}

		return allPOIList;
	}
}

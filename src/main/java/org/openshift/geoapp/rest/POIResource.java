package org.openshift.geoapp.rest;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.openshift.geoapp.domain.POI;
import org.openshift.geoapp.mongo.DBConnection;
import org.openshift.geoapp.util.Config;
import org.stringtemplate.v4.ST;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

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
		
		ST info = new ST(config.getPopupTemplate(), '{', '}');
		for (String field : config.getDataFields()) {
			info.add(field, dataValue.get(field));
		}
		poi.setId(info.render());
		
		return poi;
	}

	private Object getPosition(DBObject dataValue) {
		if (dataValue.containsField("position")) {
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

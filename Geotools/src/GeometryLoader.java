import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.ibm.util.CoordinateConversion;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/* This class uses the data files from http://www.gadm.org/
 * 
 * 
 * 
 *
 */
public class GeometryLoader {
	private TreeMap<String, String> attributeIdentifierMapping = new TreeMap<String, String>();
	private static final String GEOMETRY_STRING_PARAM = "geometryString";
	private static final String COUNTRY_CODE_PARAM = "countryCode";
	private static final String NAME_PARAM = "name";
	private static final String UN_CODE_PARAM = "unCode";
	private static final String CENTROID_LAT_PARAM = "centroidLat";
	private static final String CENTROID_LON_PARAM = "centroldLon";
	private static final String POP_2005_PARAM = "pop2005";
	private static final String SUBDIVISION_1_PARAM = "sub1";
	private static final String SUBDIVISION_2_PARAM = "sub2";
	private static final String SUBDIVISION_3_PARAM = "sub3";
	private static final String SUBDIVISION_4_PARAM = "sub4";
	private static final String SUBDIVISION_5_PARAM = "sub5";
	private static final String SUBDIVISION_6_PARAM = "sub6";
	private static final String DIVISION_TYPE_PARAM = "divType";
	private static final String DIVISION_TYPE_ENG_PARAM = "divTypeEng";
	
	

	public enum CoordinateType {
		LATLONG, UTM
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GeometryLoader gl = new GeometryLoader();
		String baseUrl = args[0];

		for (int i = 1; i < args.length; i++) {
			// args[i],
			// CoordinateType.UTM, 29, 0.0
			String[] commandParts = args[i].split("\\|");
			String fileName = commandParts[0];
			CoordinateType ct = CoordinateType.valueOf(commandParts[1]);
			Integer utmZone = null;
			Double centralMeridian = null;
			String countryCodeOverride = null;
			if (ct.equals(CoordinateType.UTM)) {
				utmZone = Integer.parseInt(commandParts[2]);
				centralMeridian = Double.parseDouble(commandParts[3]);
			}
			countryCodeOverride = commandParts[4];
			String featureType = commandParts[5];
			try {
				gl.formURL(baseUrl, fileName, ct, utmZone, centralMeridian,
						countryCodeOverride, featureType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public GeometryLoader() {
		loadCountryMap();
		configureFileIdentifier();
	}

	private static final String actionParam = "importOgrFeature";
	private TreeMap<String, String> countryMap = new TreeMap<String, String>();

	private void loadCountryMap() {
		countryMap.put("Liberia", "LR");
		countryMap.put("Malawi", "MW");
		countryMap.put("Rwanda", "RW");
		countryMap.put("Guatemala", "GT");
		countryMap.put("Honduras", "HN");
		countryMap.put("Nicaragua", "NI");
		countryMap.put("El Salvador", "SV");
	}

	TreeMap<String, String> attributeURLMapping = new TreeMap<String, String>();

	public void configureFileIdentifier() {
		attributeIdentifierMapping.put("ISO2", COUNTRY_CODE_PARAM);
		attributeIdentifierMapping.put("POP2005", POP_2005_PARAM);
		attributeIdentifierMapping.put("LAT", CENTROID_LAT_PARAM);
		attributeIdentifierMapping.put("LON", CENTROID_LON_PARAM);
		attributeIdentifierMapping.put("the_geom", GEOMETRY_STRING_PARAM);
		attributeIdentifierMapping.put("NAME", NAME_PARAM);
		attributeIdentifierMapping.put("NAME_0", NAME_PARAM);
		attributeIdentifierMapping.put("ID", SUBDIVISION_1_PARAM);
		attributeIdentifierMapping.put("ADM0", COUNTRY_CODE_PARAM);
		attributeIdentifierMapping.put("ADM1", SUBDIVISION_1_PARAM);
		attributeIdentifierMapping.put("ADM2", SUBDIVISION_2_PARAM);
		attributeIdentifierMapping.put("ADM3", SUBDIVISION_3_PARAM);
		attributeIdentifierMapping.put("ADM4", SUBDIVISION_4_PARAM);
		attributeIdentifierMapping.put("ADM5", SUBDIVISION_5_PARAM);
		attributeIdentifierMapping.put("NAME_1", SUBDIVISION_1_PARAM);
		attributeIdentifierMapping.put("NAME_2", SUBDIVISION_2_PARAM);
		attributeIdentifierMapping.put("NAME_3", SUBDIVISION_3_PARAM);
		attributeIdentifierMapping.put("NAME_4", SUBDIVISION_4_PARAM);
		attributeIdentifierMapping.put("CLNAME", SUBDIVISION_1_PARAM);
		attributeIdentifierMapping.put("FIRST_CCNA", SUBDIVISION_2_PARAM);
		attributeIdentifierMapping.put("FIRST_DNAM", SUBDIVISION_3_PARAM);
		attributeIdentifierMapping.put("LBL", NAME_PARAM);
		attributeIdentifierMapping.put("DIVISION",NAME_PARAM);
		attributeIdentifierMapping.put("TYPE_2",DIVISION_TYPE_PARAM);
		attributeIdentifierMapping.put("ENGTYPE_2",DIVISION_TYPE_ENG_PARAM);
		
		attributeURLMapping.put(COUNTRY_CODE_PARAM, "STRING|%s");
		attributeURLMapping.put(CENTROID_LON_PARAM, "DOUBLE|%14.12f");
		attributeURLMapping.put(CENTROID_LAT_PARAM, "DOUBLE|%14.12f");
		attributeURLMapping.put(POP_2005_PARAM, "INTEGER|%d");
		attributeURLMapping.put(UN_CODE_PARAM, "STRING|%s");
		attributeURLMapping.put(GEOMETRY_STRING_PARAM, "STRING|%s");
		attributeURLMapping.put(NAME_PARAM, "STRING|%s");
		attributeURLMapping.put(SUBDIVISION_1_PARAM, "STRING|%s");
		attributeURLMapping.put(SUBDIVISION_2_PARAM, "STRING|%s");
		attributeURLMapping.put(SUBDIVISION_3_PARAM, "STRING|%s");
		attributeURLMapping.put(SUBDIVISION_4_PARAM, "STRING|%s");
		attributeURLMapping.put(SUBDIVISION_5_PARAM, "STRING|%s");
		attributeURLMapping.put(SUBDIVISION_6_PARAM, "STRING|%s");
		attributeURLMapping.put(DIVISION_TYPE_PARAM, "STRING|%s");
		attributeURLMapping.put(DIVISION_TYPE_ENG_PARAM, "STRING|%s");
	}

	TreeMap<String, String> valuesMap = new TreeMap<String, String>();

	private void formURL(String baseUrl, String fileName,
			CoordinateType coordType, Integer utmZoneNumber,
			Double centralMeridian, String countryCodeOverride,
			String featureType) throws IOException {
		String serviceUrl = "action=%s&"
				+ "projectCoordinateSystemIdentifier=%s&"
				+ "geoCoordinateSystemIdentifier=%s&" + "datumIdentifier=%s&"
				+ "spheroid=%s&" + "ogrFeatureType=%s&"
				+ "x1=%14.12f&y1=%14.12f&x2=%14.12f&y2=%14.12f&";

		String prjcrs = "World_Mercator";
		String geoCoordSystemIdent = "GCS_WGS_1984";
		String dataIdent = "WGS_1984";
		String spheroid = "6378137";
		String ogrFeatureType = "COUNTRY";
		StringBuilder sb = new StringBuilder();
		sb.append("name=");
		final File f = new File(fileName);
		if(!f.exists()) {
			throw new FileNotFoundException("File not found: " + fileName);
		}
		FileDataStore store = FileDataStoreFinder.getDataStore(f);
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store.getFeatureSource();
		ReferencedEnvelope re = featureSource.getBounds();
		Double maxX = null;
		Double maxY = null;
		Double minX = null;
		Double minY = null;
		if (coordType.equals(CoordinateType.LATLONG)) {
			maxX = re.getMaxX();
			maxY = re.getMaxY();
			minX = re.getMinX();
			minY = re.getMinY();
		} else if (coordType.equals(CoordinateType.UTM)) {
			Double maxUTMX = re.getMaxX();
			Double maxUTMY = re.getMaxY();
			Double minUTMX = re.getMinX();
			Double minUTMY = re.getMinY();

			CoordinateConversion cc = new CoordinateConversion();
			double[] maxLatLon = cc.utm2LatLon(utmZoneNumber + " "
					+ centralMeridian + " " + maxUTMX + " " + maxUTMY);
			double[] minLatLon = cc.utm2LatLon(utmZoneNumber + " "
					+ centralMeridian + " " + minUTMX + " " + minUTMY);
			maxX = maxLatLon[1];
			maxY = maxLatLon[0];
			minX = minLatLon[1];
			minY = minLatLon[0];
		}

		FeatureReader<org.opengis.feature.simple.SimpleFeatureType, org.opengis.feature.simple.SimpleFeature> fr = store
				.getFeatureReader();
		int i = 0;
		while (fr.hasNext()) {
			i++;
			System.out.println("Feature " + i);
			org.opengis.feature.simple.SimpleFeature sf = fr.next();
			org.opengis.feature.simple.SimpleFeatureType sft = fr
					.getFeatureType();
			System.out.println("   FeatureType: " + sft.getName().toString());
			if (featureType.equals("SUB_COUNTRY_OTHER"))
				ogrFeatureType = "SUB_COUNTRY_OTHER";
			else
				ogrFeatureType = "COUNTRY";
			for (Property prop : sf.getProperties()) {
				//System.out.println("    prop name: " + prop.getName() + " ; " + prop.getType()+ " ; " + prop.getValue());
				for (Map.Entry<String, String> entry : attributeIdentifierMapping
						.entrySet()) {
					// if (!prop.getName().toString().equals("the_geom")){
						// System.out.println("Entry to compareto: " +
					  // entry.getKey() + " Property :" + prop.getName() + ": " +
					  // prop.getValue());
					// }
					if (entry.getKey().equalsIgnoreCase(
							prop.getName().toString())) {
						if (entry.getKey().equals("ADM0")
								|| prop.getName().toString().equals("CLNAME")) {
							valuesMap.put(entry.getValue(),
									countryMap.get(prop.getValue().toString()));

						} else if (prop.getName().toString()
								.equalsIgnoreCase("the_geom")
								&& coordType.equals(CoordinateType.UTM)) {
							CoordinateConversion cc = new CoordinateConversion();
							GeometryFactory geometryFactory = JTSFactoryFinder
									.getGeometryFactory(null);
							WKTReader reader = new WKTReader(geometryFactory);
							com.vividsolutions.jts.geom.Geometry shape = null;
							try {

								shape = (MultiPolygon) reader.read(prop
										.getValue().toString());
								int numGeo = shape.getNumGeometries();
								Polygon[] polygons = new Polygon[numGeo];

								for (i = 0; i < numGeo; i++) {
									Polygon poly0 = (Polygon) shape
											.getGeometryN(0);
									ArrayList<Coordinate> cNewList = new ArrayList<Coordinate>();
									for (Coordinate c : poly0.getCoordinates()) {
										double[] latlon = cc
												.utm2LatLon(utmZoneNumber + " "
														+ centralMeridian + " "
														+ c.x + " " + c.y);
										System.out.println("lat/lon: "
												+ latlon[0] + " " + latlon[1]);
										Coordinate cNew = new Coordinate();
										cNew.x = latlon[1];
										cNew.y = latlon[0];
										cNewList.add(cNew);
									}
									Coordinate[] cArr = new Coordinate[cNewList
											.size()];
									int j = 0;
									for (Coordinate cArrItem : cNewList) {
										cArr[j] = cArrItem;
										j++;
									}
									LinearRing lr = geometryFactory
											.createLinearRing(cArr);
									Polygon newPoly = geometryFactory
											.createPolygon(lr, null);
									polygons[0] = newPoly;

								}
								MultiPolygon mp = geometryFactory
										.createMultiPolygon(polygons);
								System.out.println(mp);
								valuesMap.put(entry.getValue(), mp.toString());
							} catch (Exception ex) {
								System.out.println(ex);
							}

						} else {
							valuesMap.put(entry.getValue(), prop.getValue()
									.toString());
						}
					}
				}
			}
			if (countryCodeOverride != null && !countryCodeOverride.toLowerCase().equals("null")) {
				valuesMap.put(COUNTRY_CODE_PARAM, countryCodeOverride);
			}
			String urlRequest = null;
			urlRequest = String.format(serviceUrl, actionParam, prjcrs,
					geoCoordSystemIdent, dataIdent, spheroid, ogrFeatureType,
					minX, minY, maxX, maxY);
			int j = 0;
			for (Map.Entry<String, String> attribute : attributeURLMapping
					.entrySet()) {
				Object param = null;
				if (valuesMap.containsKey(attribute.getKey())) {
					String[] valueParts = attributeURLMapping.get(
							attribute.getKey()).split("\\|");
					if (valueParts[0].equals("DOUBLE")) {
						param = Double.parseDouble(valuesMap.get(attribute
								.getKey()));
					} else if (valueParts[0].equals("INTEGER")) {
						param = Integer.parseInt(valuesMap.get(attribute
								.getKey()));
					} else {
						param = valuesMap.get(attribute.getKey());
					}
					String paramName = attribute.getKey();
					String format = valueParts[1];
					String part = paramName + "=" + format;
					part = String.format(part, param);
					urlRequest += part;
					j++;
					if (j < attributeURLMapping.size() - 1)
						urlRequest += "&";
				}
			}
			try {
				System.out.println(urlRequest);
				sendRequest(baseUrl, urlRequest);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	private void sendRequest(String serverBase, String urlString)
			throws IOException {
		URL url = new URL(serverBase + "externalgisdatarestapi?");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(urlString);
		wr.flush();
		System.out.println("      Sent: " + urlString);
		// Get the response
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			System.out.println("    Got: " + line);
		}
		wr.close();
		rd.close();
	}
}

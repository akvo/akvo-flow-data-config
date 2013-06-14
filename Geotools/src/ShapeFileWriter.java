import java.io.BufferedReader;
import java.io.IOException;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class ShapeFileWriter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}
	final org.opengis.feature.simple.SimpleFeatureType TYPE = null;
	
	@SuppressWarnings("unused")
	private void readData(String serverBase, String url) throws IOException{
		 FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();

	        /*
	         * GeometryFactory will be used to create the geometry attribute of each feature (a Point
	         * object for the location)
	         */
	        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

	        //BufferedReader reader = new BufferedReader(new FileReader(file));
	        BufferedReader reader = new BufferedReader(null);
	        try {
	            /* First line of the data file is the header */
	            String line = reader.readLine();
	            System.out.println("Header: " + line);

	            for (line = reader.readLine(); line != null; line = reader.readLine()) {
	                if (line.trim().length() > 0) { // skip blank lines
	                    String tokens[] = line.split("\\,");

	                    double longitude = Double.parseDouble(tokens[0]);
	                    double latitude = Double.parseDouble(tokens[1]);
	                    String name = tokens[2].trim();

	                    /* Longitude (= x coord) first ! */
	                    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

	                    featureBuilder.add(point);
	                    featureBuilder.add(name);
	                    SimpleFeature feature = featureBuilder.buildFeature(null);
	                    collection.add(feature);
	                }
	            }

	        } catch (IOException e) {
				e.printStackTrace();
			} finally {
	            reader.close();
	        }
	}

}

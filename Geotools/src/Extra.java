import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class Extra {
	private Coordinate convertToRect(Double lat, Double lon, Double alt)
			throws TransformException, NoSuchAuthorityCodeException,
			FactoryException {

		MathTransform ecefTransform = CRS.findMathTransform(
				DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);

		GeometryFactory geometryFactory = JTSFactoryFinder
				.getGeometryFactory(null);

		Point p = geometryFactory.createPoint(new Coordinate(lat, lon, alt));

		Geometry tp = JTS.transform(p, ecefTransform);

		System.out.println(tp.toString());
		return tp.getCoordinate();

	}

	private Double[] convert(Double lat, Double lon) {
		Double e2 = 6.69437999014E-3;
		Double littlex = Math.sqrt(1 - (e2 * Math.pow(
				Math.sin(Math.toRadians(lat)), 2)));
		Double a = 6378137.0;

		Double normal = (a / littlex);
		Double x = (normal) * Math.cos(Math.toRadians(lat))
				* Math.cos(Math.toRadians(lon));
		Double y = ((normal)) * Math.cos(Math.toRadians(lat))
				* Math.sin(Math.toRadians(lon));
		System.out.println("coordinate " + x + " " + y);
		return new Double[] { x, y };
	}

	/***
	 * Converts a {@link DefaultGeographicCRS#WGS84} longitude latitude position
	 * into an ENU position whose origin is provided
	 * 
	 * @param origin
	 *            the origin of your ENU plane. This is a {@link DirectPosition}
	 *            object whose {@link CoordinateReferenceSystem} is
	 *            {@link DefaultGeographicCRS#WGS84}
	 * @param latLong
	 *            this is the point whose ENU position you want to compute. This
	 *            should be a {@link DirectPosition} object whose
	 *            {@link CoordinateReferenceSystem} is
	 *            {@link DefaultGeographicCRS#WGS84}
	 * 
	 * @return a {@link DirectPosition} whose {@link CoordinateReferenceSystem}
	 *         is null (since I can't find a good ENU CRS)
	 * @throws FactoryException
	 *             This should not happen, but is warranted by
	 *             {@link CRS#findMathTransform(CoordinateReferenceSystem, CoordinateReferenceSystem)}
	 * @throws TransformException
	 *             This should not happen but is warranted by
	 *             {@link MathTransform#transform(DirectPosition, DirectPosition)}
	 *             . Note, the reason it should not happen is because there is
	 *             already a predefined path between WGS84 and ECEF
	 * @throws MismatchedDimensionException
	 *             This should not happen but is warranted by
	 *             {@link MathTransform#transform(DirectPosition, DirectPosition)}
	 *             . Note: It should not happen because if your parameters are
	 *             actually WGS84 then the dimensions will all be proper.
	 */
	public DirectPosition toENU(DirectPosition origin, DirectPosition latLong)
			throws FactoryException, MismatchedDimensionException,
			TransformException {
		// This has the potential to throw an exception, but never should.
		// By default there is a predefined transform path between WGS84 and
		// ECEF
		MathTransform ecefTransform = CRS.findMathTransform(
				DefaultGeographicCRS.WGS84, DefaultGeocentricCRS.CARTESIAN);

		DirectPosition ecefOrigin = ecefTransform.transform(origin, null);
		DirectPosition ecefLatLong = ecefTransform.transform(latLong, null);

		double deltaX = ecefLatLong.getOrdinate(0) - ecefOrigin.getOrdinate(0);
		double deltaY = ecefLatLong.getOrdinate(1) - ecefOrigin.getOrdinate(1);
		double deltaZ = ecefLatLong.getOrdinate(2) - ecefOrigin.getOrdinate(2);

		// Get the sin/cos information for the origin
		double sinLat = Math.sin(origin.getOrdinate(1)); // sin(latitude)
		double cosLat = Math.cos(origin.getOrdinate(1)); // cos(latitude)

		double sinLong = Math.sin(origin.getOrdinate(0)); // sin(longitude)
		double cosLong = Math.cos(origin.getOrdinate(0)); // cos(longitude)

		// Transform the ECEF
		double x = (deltaY * cosLong) - (deltaX * sinLong);
		double y = (deltaZ * cosLat) - (deltaX * sinLat * cosLong)
				- (deltaY * sinLat * sinLong);
		double z = (deltaX * cosLat * cosLong) + (deltaY * cosLat * sinLong)
				+ (deltaZ * sinLat);

		// Return a result
		return new GeneralDirectPosition(x, y, z);
	}

	@SuppressWarnings("unused")
	private void readShapFile(String fileName) throws IOException,
			NoSuchAuthorityCodeException, TransformException, FactoryException {
		// File file = JFileDataStoreChooser.showOpenFile("shp", null);
		// if (file == null) {
		// return;
		// }
		FileDataStore store = FileDataStoreFinder.getDataStore(new File(
				fileName));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = store.getFeatureSource();
		String typeNames = store.getTypeNames()[0];
		FeatureReader<org.opengis.feature.simple.SimpleFeatureType, org.opengis.feature.simple.SimpleFeature> fr = store
				.getFeatureReader();
		int i = 0;
		while (fr.hasNext()) {
			i++;
			org.opengis.feature.simple.SimpleFeature sf = fr.next();
			List<AttributeDescriptor> arD = fr.getFeatureType()
					.getAttributeDescriptors();
			org.opengis.feature.simple.SimpleFeatureType sft = fr
					.getFeatureType();
			GeometryDescriptor gd = sft.getGeometryDescriptor();
			for (Coordinate c : loadCoordinates()) {
				System.out.println("Searching for: " + c.toString());
				Geometry geometry = (Geometry) sf.getDefaultGeometry();
				Geometry g2 = new GeometryFactory().createPoint(c);
				Boolean found = geometry.contains(g2);
				System.out.println(i + " Contains: " + found);
				if (found)
					System.out.println("GOOOOOOPALLLL");
			}
		}

		// MapContext map = new DefaultMapContext();
		// map.setTitle("Quickstart");
		// map.addLayer(featureSource, null);
		//
		// JMapFrame.showMap(map);
	}

	private ArrayList<Coordinate> loadCoordinates() {
		ArrayList<Coordinate> arrCoord = new ArrayList<Coordinate>();
		Double[] coordArr = convert(4.214943141390651, -8.2177734375);
		arrCoord.add(new Coordinate(coordArr[0], coordArr[1]));
		Double[] coordArr2 = convert(6.300774, -10.79716);
		arrCoord.add(new Coordinate(coordArr2[0], coordArr2[1]));
		try {
			arrCoord.add(convertToRect(4.214943141390651, -8.2177734375, 0D));
			arrCoord.add(convertToRect(6.300774, -10.79716, 0D));
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
		return arrCoord;

	}
}

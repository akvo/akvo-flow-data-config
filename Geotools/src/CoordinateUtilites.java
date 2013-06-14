import java.util.Collections;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;

public class CoordinateUtilites {

	@SuppressWarnings("deprecation")
	public static void convertLatLonToUTM(Integer zoneNumber, Double latitude, Double longitude,Double utmZoneCenterLongitude) throws FactoryException, TransformException{
		
		

		MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
		ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

		GeographicCRS geoCRS = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
		CartesianCS cartCS = org.geotools.referencing.cs.DefaultCartesianCS.GENERIC_2D;

		ParameterValueGroup parameters = mtFactory.getDefaultParameters("Transverse_Mercator");
		parameters.parameter("central_meridian").setValue(0);
		parameters.parameter("latitude_of_origin").setValue(0.0);
		parameters.parameter("scale_factor").setValue(0.9996);
		parameters.parameter("false_easting").setValue(0.0);
		parameters.parameter("false_northing").setValue(0.0);

		Map<String, String> properties = Collections.singletonMap("name", "WGS 84 / UTM Zone " + zoneNumber);
		ProjectedCRS projCRS = factories.createProjectedCRS(properties, geoCRS, null, parameters, cartCS);

		MathTransform transform = CRS.findMathTransform(geoCRS, projCRS);

		double[] dest = new double[2];
		transform.transform(new double[] {longitude, latitude}, 0, dest, 0, 1);

		int easting = (int)Math.round(dest[0]);
		int northing = (int)Math.round(dest[1]);
		System.out.println("easting/northing: " +easting + "/" + northing);
	}
	
	public static void main(String[] args){
		Double lat = Double.parseDouble(args[0]);
		Double lon = Double.parseDouble(args[1]);
		Double utmZoneCenterLongitude = Double.parseDouble(args[2]);
		Integer utmZone = Integer.parseInt(args[3]);
		try {
			convertLatLonToUTM(utmZone,lat,lon,utmZoneCenterLongitude);
		} catch (FactoryException e) {
			e.printStackTrace();
		} catch (TransformException e) {
			e.printStackTrace();
		}
	}
}

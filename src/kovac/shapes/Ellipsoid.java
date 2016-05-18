package kovac.shapes;

/**
 * This class is here to represent an ellipsoid
 * 
 * @author bastien.kovac
 * 
 */
public class Ellipsoid {

	/**
	 * The three semi-length of the ellipsoid (Usually noted as a, b and c)
	 */
	private double xSemiAxis, ySemiAxis, zSemiAxis;

	/**
	 * The name of the ellipsoid
	 */
	private String name;

	public Ellipsoid(double xSemiAxis, double ySemiAxis, double zSemiAxis) {

		this.xSemiAxis = xSemiAxis;
		this.ySemiAxis = ySemiAxis;
		this.zSemiAxis = zSemiAxis;
		
	}

	/**
	 * @return The volume of the ellipsoid
	 */
	public double getVolume() {
		return ((Math.PI * 4) / 3) * xSemiAxis * ySemiAxis * zSemiAxis;
	}

	/**
	 * This method uses Knud Thomsen's formula, which has a relative error of at
	 * most 1.061%
	 * 
	 * @return The surface of the ellipsoid
	 */
	public double getSurface() {
		double p = 1.6075;
		double tmp = (Math.pow(xSemiAxis, p) * Math.pow(ySemiAxis, p) + Math.pow(xSemiAxis, p) * Math.pow(zSemiAxis, p)
				+ Math.pow(ySemiAxis, p) * Math.pow(zSemiAxis, p)) / 3;
		return (4 * Math.PI) * Math.pow(tmp, 1 / p);
	}
	
	

	/**
	 * Sets the name of the ellipsoid
	 * 
	 * @param name
	 *            The name to give to the ellipsoid
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return The name of the ellipsoid
	 */
	public String getName() {
		return this.name;
	}

}
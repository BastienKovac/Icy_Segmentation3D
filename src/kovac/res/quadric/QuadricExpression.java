package kovac.res.quadric;

import java.util.UUID;

import Jama.Matrix;

/**
 * This class stores a quadratic expression as a 10x1 Matrix, representing the
 * 10 factors of this expression.
 * 
 * @author bastien.kovac
 *
 */
public class QuadricExpression {

	private String uniqueID;

	private Matrix coefficients;

	public QuadricExpression(String uniqueID) {
		this.uniqueID = uniqueID;
	}
	
	public QuadricExpression() {
		uniqueID = UUID.randomUUID().toString();
	}

	public QuadricExpression(Matrix coefficients) {
		uniqueID = UUID.randomUUID().toString();
		this.coefficients = coefficients;
	}

	public QuadricExpression(double[] coefficients) {
		uniqueID = UUID.randomUUID().toString();
		if (coefficients.length != 10)
			throw new IllegalArgumentException("Needs 10 factors to build a QuadricExpression");
		double[][] matArrayCoeffs = new double[10][1];
		for (int i = 0; i < 10; i++) {
			matArrayCoeffs[i] = new double[] { coefficients[i] };
		}
		this.coefficients = new Matrix(matArrayCoeffs);
	}
	
	public void setCoefficients(double[] coeffs) {
		if (coeffs.length != 10)
			throw new RuntimeException("Needs 10 factors to build a QuadricExpression");
		double[][] matArrayCoeffs = new double[10][1];
		for (int i = 0; i < 10; i++) {
			matArrayCoeffs[i] = new double[] { coeffs[i] };
		}
		this.coefficients = new Matrix(matArrayCoeffs);
	}

	public Matrix getCoefficients() {
		return this.coefficients;
	}
	 
	public String getID() {
		return uniqueID;
	}
	
	public double[] getSimpleArray() {
		return this.coefficients.getColumnPackedCopy();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uniqueID == null) ? 0 : uniqueID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QuadricExpression other = (QuadricExpression) obj;
		if (uniqueID == null) {
			if (other.uniqueID != null)
				return false;
		} else if (!uniqueID.equals(other.uniqueID))
			return false;
		return true;
	}
	
}

package kovac.maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import Jama.SingularValueDecomposition;
import icy.type.point.Point3D;
import kovac.res.quadric.QuadricExpression;
import kovac.res.util.MathUtils;
import kovac.res.util.ViewerUtil;
import kovac.shapes.EllipsoidOverlay;
import vtk.vtkPoints;

/**
 * This class handles the generation of the ellipsoid fitting a set of given
 * points. It uses Singular Value Decomposition to generate the characteristic
 * Matrix of the ellipsoid, and center of mass to calculate its center.
 * 
 * 
 * @author bastien.kovac
 *
 */
public class EllipsoidAlgorithm {

	// Note that the notations for the variables have been chosen accordingly to
	// the ones used in the original Matlab code, to improve clarity during
	// debug phase

	/**
	 * Parameter for Douglas-Rachford in ]0,+infty[
	 */
	private static double gamma = 0.01;
	/**
	 * Number of iterations of the algorithm
	 */
	private static int nbIterations = 100;
	/**
	 * The matrix representing the base points. It is a nbPoints x 3 matrix,
	 * with each column representing a coordinate
	 */
	private Matrix basePoints;
	/**
	 * The quadratic equation of the ellipsoid
	 */
	private QuadricExpression quadricExpression;
	/**
	 * The characteristic matrix of the ellipsoid
	 */
	private Matrix matSR;
	/**
	 * The vtk object representing the real center of the ellipsoid
	 */
	private vtkPoints realCenter;
	private static double[] c;

	/**
	 * Builds a new EllipsoidAlgorithm from a given list of three dimensional
	 * points
	 * 
	 * @param basePoints
	 *            The base points
	 */
	public EllipsoidAlgorithm(List<Point3D> basePoints) {
		this.basePoints = new Matrix(3, basePoints.size());
		for (int i = 0; i < basePoints.size(); i++) {
			this.basePoints.set(0, i, basePoints.get(i).getX());
			this.basePoints.set(1, i, basePoints.get(i).getY());
			this.basePoints.set(2, i, basePoints.get(i).getZ());
		}
		// We shift origin to the center of the image
		double[] sizes;
		if (ViewerUtil.areSet())
			sizes = ViewerUtil.getSizes();
		else
			sizes = new double[] { 0, 0, 0 };
		double[][] shifting = new double[3][basePoints.size()];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < basePoints.size(); j++) {
				shifting[i][j] = (sizes[i] / 2);
			}
		}
		Matrix shiftMat = new Matrix(shifting);
		this.basePoints = this.basePoints.minus(shiftMat);
		this.basePoints.print(3, 4);
	}

	public static Matrix getQ0(Matrix baseMatrix) {
		double[] baseCenter = MathUtils.getCenterOfMass(baseMatrix);
		double[] matVariance = new double[baseMatrix.getRowDimension()];
		for (int i = 0; i < baseMatrix.getRowDimension(); i++)
			matVariance[i] = MathUtils.getVariance(baseMatrix.getArray()[i]);
		double avgRadius = MathUtils.sum(matVariance);
		double[][] baseSphereArray = new double[][] { { 1.0 / 3.0 }, { 1.0 / 3.0 }, { 1.0 / 3.0 }, { 0.0 }, { 0.0 },
				{ 0.0 }, { -2 * baseCenter[0] / 3.0 }, { -2 * baseCenter[1] / 3.0 }, { -2 * baseCenter[2] / 3.0 },
				{ (baseCenter[0] * baseCenter[0] + baseCenter[1] * baseCenter[1] + baseCenter[2] * baseCenter[2]
						- avgRadius) / 3.0 } };
		return new Matrix(baseSphereArray);
	}

	/**
	 * Implements the Douglas-Rachford algorithm
	 */
	private void douglasRachford() {
		c = MathUtils.getCenterOfMass(basePoints);
		// We recenter to center of mass
		double[][] shifting = new double[3][basePoints.getColumnDimension()];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < basePoints.getColumnDimension(); j++) {
				shifting[i][j] = c[i];
			}
		}
		basePoints = basePoints.minus(new Matrix(shifting));
		Matrix K = getK(basePoints);
		Matrix M = getM(K);
		Matrix p = getQ0(basePoints);
		Matrix q = null;
		for (int i = 0; i < nbIterations; i++) {
			q = proxf2(p);
			p = p.plus(proxf1(M, q.times(2).minus(p))).minus(q);
		}
		q = proxf2(q);
		// We decenter
		for (int i = 0 ; i < c.length ; i++) {
			c[i] = -c[i];
		}
		
		double[][] decenter = new double[][] { { 0 }, { 0 }, { 0 }, { 0 }, { 0 }, { 0 },
				{ 2 * q.get(0, 0) * c[0] + q.get(3, 0) * c[1] + q.get(4, 0) * c[2] },
				{ 2 * q.get(1, 0) * c[1] + q.get(3, 0) * c[0] + q.get(5, 0) * c[2] },
				{ 2 * q.get(2, 0) * c[2] + q.get(4, 0) * c[0] + q.get(5, 0) * c[1] },
				{ q.get(0, 0) * (c[0] * c[0]) + q.get(1, 0) * (c[1] * c[1]) + q.get(2, 0) * (c[2] * c[2])
						+ q.get(3, 0) * c[0] * c[1] + q.get(4, 0) * c[0] * c[2] + q.get(5, 0) * c[1] * c[2]
						+ q.get(6, 0) * c[0] + q.get(7, 0) * c[1] + q.get(8, 0) * c[2] } };
		q = q.plus(new Matrix(decenter));
		quadricExpression = new QuadricExpression(q);
	}

	/**
	 * Returns the mlDivide result for the baseSphereMatrix and the input matrix
	 * 
	 * @param q
	 *            The input matrix
	 * @param m
	 *            The other input matrix
	 * @return A result matrix A as A = m\q
	 */
	public static Matrix proxf1(Matrix M, Matrix q) {
		return M.solve(q);
	}

	/**
	 * Function Project_On_B from matlab
	 * 
	 * @param q
	 *            Input Matrix
	 * @return a vector column
	 */
	public static Matrix proxf2(Matrix q0) {
		Matrix Q0 = new Matrix(3, 3);
		double[] arrayCopy = q0.getColumnPackedCopy();

		// Initialize q0
		Q0.set(0, 0, arrayCopy[0]);
		Q0.set(0, 1, arrayCopy[3] / 2);
		Q0.set(0, 2, arrayCopy[4] / 2);

		Q0.set(1, 0, arrayCopy[3] / 2);
		Q0.set(1, 1, arrayCopy[1]);
		Q0.set(1, 2, arrayCopy[5] / 2);

		Q0.set(2, 0, arrayCopy[4] / 2);
		Q0.set(2, 1, arrayCopy[5] / 2);
		Q0.set(2, 2, arrayCopy[2]);

		EigenvalueDecomposition eig = new EigenvalueDecomposition(Q0);

		Matrix U = eig.getV();
		Matrix S0 = eig.getD();

		// Diagonalize s0
		Matrix s0 = diag(S0);
		// Use projsplx on diagS0
		List<Double> asList = new ArrayList<Double>(Arrays.asList(ArrayUtils.toObject(s0.getColumnPackedCopy())));
		List<Double> result = projsplx(asList);
		Matrix s = new Matrix(result.size(), 1);
		for (int i = 0; i < result.size(); i++)
			s.set(i, 0, result.get(i));

		// Diag s
		Matrix S = diag(s);
		Matrix Q = U.times(S.times(U.transpose()));

		// Initialize final result
		Matrix q = new Matrix(q0.getRowDimension(), 1);

		q.set(0, 0, Q.get(0, 0));
		q.set(1, 0, Q.get(1, 1));
		q.set(2, 0, Q.get(2, 2));
		q.set(3, 0, 2 * Q.get(1, 0));
		q.set(4, 0, 2 * Q.get(2, 0));
		q.set(5, 0, 2 * Q.get(2, 1));

		for (int i = 6; i < q0.getRowDimension(); i++) {
			q.set(i, 0, q0.get(i, 0));
		}

		return q;

	}

	/**
	 * Return a vector column (nx1 Matrix) corresponding to the diagonal of the
	 * input Matrix. If the input matrix is a vector column, it returns a square
	 * Matrix with these diagonal values instead
	 * 
	 * @param q
	 *            A square input matrix of n x n dimension
	 * @return The diagonal values
	 */
	public static Matrix diag(Matrix q) {
		Matrix res = null;
		if (q.getColumnDimension() == q.getRowDimension()) {
			res = new Matrix(q.getColumnDimension(), 1);
			for (int i = 0; i < q.getRowDimension(); i++) {
				res.set(i, 0, q.get(i, i));
			}
		} else {
			if (q.getColumnDimension() == 1) {
				res = new Matrix(q.getRowDimension(), q.getRowDimension());
				for (int i = 0; i < q.getRowDimension(); i++) {
					res.set(i, i, q.get(i, 0));
				}
			}

		}
		return res;
	}

	/**
	 * @return The K matrix corresponding to the points given to the algorithm
	 *         as parameters
	 */
	public static Matrix getK(Matrix basePoints) {
		// Initialize Matrix
		Matrix D = new Matrix(10, basePoints.getColumnDimension());
		for (int i = 0; i < basePoints.getColumnDimension(); i++) {
			// X^2, Y^2 and Z^2 coefficients
			D.set(0, i, basePoints.get(0, i) * basePoints.get(0, i));
			D.set(1, i, basePoints.get(1, i) * basePoints.get(1, i));
			D.set(2, i, basePoints.get(2, i) * basePoints.get(2, i));

			// XY, XZ and YZ coefficients
			D.set(3, i, basePoints.get(0, i) * basePoints.get(1, i));
			D.set(4, i, basePoints.get(0, i) * basePoints.get(2, i));
			D.set(5, i, basePoints.get(1, i) * basePoints.get(2, i));

			// X, Y and Z coefficients
			D.set(6, i, basePoints.get(0, i));
			D.set(7, i, basePoints.get(1, i));
			D.set(8, i, basePoints.get(2, i));

			// Gamma coefficient
			D.set(9, i, 1);
		}
		// Return K
		return D.times(D.transpose());
	}

	/**
	 * Original Matlab/C code from Xiaojing Ye Algorithm explained here :
	 * http://arxiv.org/abs/1101.6081
	 * 
	 * @param y
	 *            An input n-dimension vector
	 * @return ?
	 */
	public static List<Double> projsplx(List<Double> y) {
		boolean bget = false;
		int m = y.size();

		List<Double> sortedList = new ArrayList<Double>(y);
		Collections.sort(sortedList, Collections.reverseOrder());

		double tmpSum = 0;
		double tMax = 0;

		for (int i = 0; i < m - 1; i++) {
			tmpSum += sortedList.get(i);
			tMax = (tmpSum - 1) / (i + 1);
			if (tMax >= sortedList.get(i + 1)) {
				bget = true;
				break;
			}
		}

		if (!bget) {
			tMax = (tmpSum + sortedList.get(m - 1) - 1) / m;
		}

		for (int i = 0; i < y.size(); i++) {
			y.set(i, Math.max(y.get(i) - tMax, 0));
		}

		return y;
	}

	public static Matrix getM(Matrix K) {
		Matrix identity = new Matrix(K.getRowDimension(), K.getColumnDimension());
		for (int i = 0; i < identity.getRowDimension(); i++) {
			identity.set(i, i, 1);
		}
		Matrix M = K.times(gamma).plus(identity);
		return M;
	}

	/**
	 * Quadratic coefficients to parameters
	 */
	@SuppressWarnings("unused")
	private void getRealParameters() {
		this.realCenter = new vtkPoints();
		this.matSR = new Matrix(3, 3);
		double[] arrayQ = quadricExpression.getSimpleArray();

		// Initialize matrix
		matSR.set(0, 0, arrayQ[0]);
		matSR.set(0, 1, arrayQ[3] / 2);
		matSR.set(0, 2, arrayQ[4] / 2);

		matSR.set(1, 0, arrayQ[3] / 2);
		matSR.set(1, 1, arrayQ[1]);
		matSR.set(1, 2, arrayQ[5] / 2);

		matSR.set(2, 0, arrayQ[4] / 2);
		matSR.set(2, 1, arrayQ[5] / 2);
		matSR.set(2, 2, arrayQ[2]);

		// create S and R then SR (SU here)
		Matrix tmp = new Matrix(matSR.getArray());
		SingularValueDecomposition svd = tmp.svd();
		Matrix S = svd.getS();
		Matrix U = svd.getU();

		double[][] test = U.getArray(), test2 = new double[3][3];
		for (int iT = 0; iT < test.length; iT++) {
			for (int jT = 0; jT < test[0].length; jT++) {
				test2[(2 * iT + 2) % 3][jT] = test[iT][jT];
			}
		}
		U = new Matrix(test2);

		double[][] st = new double[3][3];

		// normalize S
		st[0][0] = Math.sqrt(S.get(2, 2) / S.get(0, 0)) * 20;
		st[1][1] = Math.sqrt(S.get(2, 2) / S.get(1, 1)) * 20;
		st[2][2] = Math.sqrt(S.get(2, 2) / S.get(2, 2)) * 20;
		S = new Matrix(st);

		matSR = S.times(U.transpose());

		Matrix lastCoeffs = new Matrix(3, 1);
		lastCoeffs.set(0, 0, arrayQ[6] / 2);
		lastCoeffs.set(1, 0, arrayQ[7] / 2);
		lastCoeffs.set(2, 0, arrayQ[8] / 2);

		Matrix centerMat = proxf1(matSR, lastCoeffs);
		realCenter = new vtkPoints();
		realCenter.InsertNextPoint(centerMat.getColumnPackedCopy());
	}

	public QuadricExpression getFinalQuadric() {
		if (quadricExpression == null)
			douglasRachford();
		return quadricExpression;
	}

	/**
	 * Builds and return the ellipsoid calculated by the algorithm as an Overlay
	 * 
	 * @return An overlay displaying the ellipsoid
	 */
	public EllipsoidOverlay generateEllipsoid() {
		douglasRachford();
		// getRealParameters();
		return new EllipsoidOverlay(quadricExpression);
	}

}
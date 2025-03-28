package com.PDR.PDRposition.Utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Created by Pumpkin on 2018/12/9 0009.
 */

public class MatrixOperation {
    public MatrixOperation() {

    }

    public static double[][] multiply(double[][] m1, double[][] m2) {
        int m1rows = m1.length;
        int m1cols = m1[0].length;
        int m2rows = m2.length;
        int m2cols = m2[0].length;
        if (m1cols != m2rows)
            throw new IllegalArgumentException("matrices don't match: " + m1cols + " != " + m2rows);
        double[][] result = new double[m1rows][m2cols];
        // multiply
        for (int i = 0; i < m1rows; i++)
            for (int j = 0; j < m2cols; j++)
                for (int k = 0; k < m1cols; k++)
                    result[i][j] += m1[i][k] * m2[k][j];

        return result;
    }

    public static double[] multiply(double[][] m1, double[] m2) {
        int m1rows = m1.length;
        int m1cols = m1[0].length;
        int m2rows = m2.length;
        if (m1cols != m2rows)
            throw new IllegalArgumentException("matrices don't match: " + m1cols + " != " + m2rows);
        double[] result = new double[m1rows];
        // multiply
        for (int i = 0; i < m1rows; i++)
            for (int k = 0; k < m1cols; k++)
                result[i] += m1[i][k] * m2[k];

        return result;
    }


    public static double[][] add(double[][] a, double[][] b) {
        int rows = a.length;
        int columns = a[0].length;
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;

    }

    public static double[] add(double[] a, double[] b) {
        int rows = a.length;
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    public static double[][] subtract(double[][] a, double[][] b) {
        int rows = a.length;
        int columns = a[0].length;
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }

    public static double[] subtract(double[] a, double[] b) {
        int rows = a.length;
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    }

    public double[][] scale(double[][] a, double b) {
        int rows = a.length;
        int columns = a[0].length;
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = a[i][j] * b;
            }
        }
        return result;
    }

    public static double[][] transposeMatrix(double[][] m) {
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];

        return temp;
    }


    public double[] crossProduct(double[] v1, double[] v2) {
        double[] vR = new double[v1.length];
        vR[0] = ((v1[1] * v2[2]) - (v1[2] * v2[1]));
        vR[1] = -((v1[0] * v2[2]) - (v1[2] * v2[0]));
        vR[2] = ((v1[0] * v2[1]) - (v1[1] * v2[0]));
        return vR;
    }

    public double dotProduct(double[] a, double[] b) {
        double n = 0.0;
        int lim = Math.min(a.length, b.length);
        for (int i = 0; i < lim; i++) {
            n += a[i] * b[i];
        }
        return n;
    }

    public float[] reOrthogonalization(double[][] dcm) {
        // TODO Auto-generated method stub
        double[][] R1 = {{dcm[0][0], dcm[0][1], dcm[0][2]}};
        double[][] R2 = {{dcm[1][0], dcm[1][1], dcm[1][2]}};
        double[][] error = multiply(R1, transposeMatrix(R2));
        double[][] R1_orth = subtract(R1, scale(R2, error[0][0] / 2));
        double[][] R2_orth = subtract(R2, scale(R1, error[0][0] / 2));
        //R3 is the cross product of R1 and R2
        double[][] R3_orth = new double[1][3];
        R3_orth[0] = crossProduct(R1_orth[0], R2_orth[0]);
        //make sure that each row has a magnitude equal to one
        R1_orth = scale(R1_orth, (3 - dotProduct(R1_orth[0], R1_orth[0])) / 2);
        R2_orth = scale(R2_orth, (3 - dotProduct(R2_orth[0], R2_orth[0])) / 2);
        R3_orth = scale(R3_orth, (3 - dotProduct(R3_orth[0], R3_orth[0])) / 2);
        //gyro-based orientation
        float[] temp = {(float) R1_orth[0][0], (float) R1_orth[0][1], (float) R1_orth[0][2],
                (float) R2_orth[0][0], (float) R2_orth[0][1], (float) R2_orth[0][2],
                (float) R3_orth[0][0], (float) R3_orth[0][1], (float) R3_orth[0][2]};
        return temp;
    }

    public float[] reOrthogonalization(float[] mDcm_g) {
        // TODO Auto-generated method stub
        double[][] temp1 = {{(double) mDcm_g[0], (double) mDcm_g[1], (double) mDcm_g[2]},
                {(double) mDcm_g[3], (double) mDcm_g[4], (double) mDcm_g[5]},
                {(double) mDcm_g[6], (double) mDcm_g[7], (double) mDcm_g[8]}};
        float[] temp2 = new float[9];
        temp2 = reOrthogonalization(temp1);
        return temp2;
    }

    public float[] getRotationMatrixFromOrientation(float[] orientation) {
        // TODO Auto-generated method stub
        float Cr = (float) Math.cos(orientation[2]);
        float Sr = (float) Math.sin(orientation[2]);
        float Cy = (float) Math.cos(orientation[0]);
        float Sy = (float) Math.sin(orientation[0]);
        float Cp = (float) Math.cos(orientation[1]);
        float Sp = (float) Math.sin(orientation[1]);
        float[] temp = {Cr * Cy - Sr * Sp * Sy, Cp * Sy, Sr * Cy + Cr * Sp * Sy,
                -Cr * Sy - Sr * Sp * Cy, Cp * Cy, -Sr * Sy + Cr * Sp * Cy,
                -Sr * Cp, -Sp, Cr * Cp};
        return temp;
    }

    public double[] getMax(double[][] mAccBuff) {
        double[] MaxVal = new double[2];
        MaxVal[0] = mAccBuff[0][0];
        MaxVal[1] = 0;

        for (int x = 1; x < mAccBuff.length; x++) {
            if (mAccBuff[x][0] > MaxVal[0]) {
                MaxVal[0] = mAccBuff[x][0];
                MaxVal[1] = x;
            }
        }
        return MaxVal;
    }

    public static double[][] eyeMatrix(int n) {
        double[][] array = new double[n][n];

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (row == col) {
                    array[row][col] = 1;
                } else {
                    array[row][col] = 0;
                }
            }
        }
        return array;
    }

    public static double[][] zeroMatrix(int m, int n) {
        double[][] array = new double[m][n];
        for (int row = 0; row < m; row++) {
            for (int col = 0; col < n; col++) {
                array[row][col] = 0;
            }
        }
        return array;
    }

    public static double[] zeroMatrixSingle(int m) {
        double[] array = new double[m];
        for (int row = 0; row < m; row++) {
            array[row] = 0;
        }
        return array;
    }

    public static String[] nullStringArrySingle(int m) {
        String[] array = new String[m];
        for (int row = 0; row < m; row++) {
            array[row] = "null";
        }
        return array;
    }

    public static double[][] copy(double[][] c) {
        int DIM1 = c.length;
        int DIM2 = c[0].length;
        double[][] aa = new double[DIM1][DIM2];
        for (int j = 0; j < DIM1; j++) {
            for (int k = 0; k < DIM2; k++) {
                aa[j][k] = c[j][k];
            }
        }
        return aa;
    }


    public static double[] copySingle(double[] c) {
        int DIM = c.length;
        double[] aa = new double[DIM];
        for (int j = 0; j < DIM; j++) {
            aa[j] = c[j];
        }
        return aa;
    }

    public static double[][] invert(double A[][]) {

        double[][] a = copy(A);
        int n = a.length;
        double x[][] = new double[n][n];
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i = 0; i < n; ++i) b[i][i] = 1;

        // Transform the matrix into an upper triangle
        gaussian(a, index);

        // Update the matrix b[i][j] with the ratios stored
        for (int i = 0; i < n - 1; ++i)
            for (int j = i + 1; j < n; ++j)
                for (int k = 0; k < n; ++k)
                    b[index[j]][k]
                            -= a[index[j]][i] * b[index[i]][k];

        // Perform backward substitutions
        for (int i = 0; i < n; ++i) {
            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];
            for (int j = n - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < n; ++k) {
                    x[j][i] -= a[index[j]][k] * x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }

    // Method to carry out the partial-pivoting Gaussian
    // elimination.  Here index[] stores pivoting order.
    public static void gaussian(double a[][], int index[]) {
        int n = index.length;
        double c[] = new double[n];

        // Initialize the index
        for (int i = 0; i < n; ++i) index[i] = i;

        // Find the rescaling factors, one from each row
        for (int i = 0; i < n; ++i) {
            double c1 = 0;
            for (int j = 0; j < n; ++j) {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }

        // Search the pivoting element from each column
        int k = 0;
        for (int j = 0; j < n - 1; ++j) {
            double pi1 = 0;
            for (int i = j; i < n; ++i) {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) {
                    pi1 = pi0;
                    k = i;
                }
            }
            // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < n; ++i) {
                double pj = a[index[i]][j] / a[index[j]][j];

                // Record pivoting ratios below the diagonal
                a[index[i]][j] = pj;

                // Modify other elements accordingly
                for (int l = j + 1; l < n; ++l)
                    a[index[i]][l] -= pj * a[index[j]][l];
            }
        }
    }

    // is symmetric
    public static boolean isSymmetric(double[][] A) throws Exception {
        double EPSILON = 1e-5;
        int N = A.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
                if (A[i][j] == Double.NaN || A[j][i] == Double.NaN) {
                    throw new Exception("Matrix contains NaN");
                }
                if (A[i][j] - A[j][i] > EPSILON) {

                    A[i][j] = A[j][i];

                    return true;  //updated so that rounding errors doesn't make it fail
                } else {
                    A[i][j] = A[j][i];
                }
            }
        }
        return true;
    }

    public static double[][] AveNoDiagonalEle(double[][] Matrix) {
        //		result = MatrixOperation.copy(Matrix);
        if (!isSquare(Matrix))
            return Matrix;


        double[][] result = MatrixOperation.zeroMatrix(Matrix.length, Matrix.length);
        for (int i = 0; i < Matrix.length; i++) {
            for (int j = 0; j < Matrix.length; j++) {
                result[i][j] = (Matrix[i][j] + Matrix[j][i]) / 2;
            }
        }

        //		double aveEle;
        //		for(int i = 0 ; i < Matrix.length ; i ++) {
        //			for (int j = i+1 ; j < Matrix.length ; j ++ ) {
        ////				if( i != j ) {
        //					aveEle = ( Matrix[i][j] + Matrix[j][i] ) / 2 ;
        //					Matrix[i][j] = aveEle;
        //					Matrix[j][i] = aveEle;
        ////				}
        //			}
        //		}
        //

        return result;
    }

    // is symmetric
    public static boolean isSquare(double[][] A) {
        int N = A.length;
        for (int i = 0; i < N; i++) {
            if (A[i].length != N) return false;
        }
        return true;
    }


    // return Cholesky factor L of psd matrix A = L L^T
    public static double[][] cholesky(double[][] A) throws Exception {
        debug("Entering Cholesky");

        int DEBUG = 0; //0 = no debug, 1 = debug on PC, 2 = debug on brick
        isSquare(A);
        if (!isSquare(A)) {
            throw new Exception("Matrix is not square");
        }
        if (!isSymmetric(A)) {
            throw new Exception("Matrix is not symmetric");
        }

        int N = A.length;
        double[][] L = new double[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                for (int k = 0; k < j; k++) {
                    sum += L[i][k] * L[j][k];
                }
                if (i == j) L[i][i] = Math.sqrt(A[i][i] - sum);
                else L[i][j] = 1.0 / L[j][j] * (A[i][j] - sum);
            }
            if (L[i][i] <= 0) {
                throw new Exception("Matrix not positive definite");
            }
        }
        checkForNaN(L);
        debug("Leaving Cholesky");
        return L;
    }

    private static void checkForNaN(double[][] A) throws Exception {
        int N = A.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
                if (Double.isNaN(A[i][j])) {
                    throw new Exception("Matrix contains NaN");
                }
            }
        }
    }

    private static void debug(String s) {
        if (s.isEmpty())
            return;
        else {
            System.out.println(s);
        }
    }


    public static double[] List2Arry_1(List<Double> a) {

        double[] arry = new double[a.size()];
        for (int i = 0; i < a.size(); i++) {
            arry[i] = a.get(i);
        }
        return arry;
    }

    public static double[][] List2Arry_2(List<Double> a) {

        double[][] arry = eyeMatrix(a.size());
        for (int i = 0; i < a.size(); i++) {
            arry[i][i] = a.get(i);
        }
        return arry;
    }

    public static double[][] List2Arry_3(List<double[]> a) {

        double[][] arry = new double[a.size()][a.get(0).length];
        for (int i = 0; i < a.size(); i++) {
            for (int j = 0; j < a.get(0).length; j++) {
                arry[i][j] = a.get(i)[j];
            }
        }
        return arry;
    }


    public static boolean is_Zero(double a) {

        if (Math.abs(a) < 0.000001) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean is_Equal(double a, double b) {
        if (Math.abs(a - b) < 0.0000000001) {
            return true;
        } else {
            return false;
        }
    }

    public static int[] ArryFromListInt(List<int[]> data, int num, int postion) {
        int[] Arry = new int[num];
        for (int i = 0; i < num; i++) {
            Arry[i] = data.get(data.size() - 1 - i)[postion];
        }
        return Arry;
    }


    public static double[] ArryFromList(List<double[]> data, int position) {
        int size = data.size();
        double[] Arry = new double[size];
        for (int i = 0; i < size; i++) {
            Arry[i] = data.get(i)[position];
        }
        return Arry;
    }

    public static double[] AbsArry(double[] data) {
        int size = data.length;
        double[] Arry = new double[size];
        for (int i = 0; i < size; i++) {
            Arry[i] = Math.abs(data[i]);
        }
        return Arry;
    }

    public static double[] ArryFromList(List<double[]> data, int position, int num) {

        double[] Arry = new double[num];
        for (int i = 0; i < num; i++) {
            Arry[i] = data.get(data.size() - 1 - i)[position];
        }
        return Arry;
    }


    public static double getAverage(double[] data) {

        double sum = 0;
        int size = data.length;
        for (int i = 0; i < size; i++) {
            sum += data[i];
        }
        return sum / size;
    }

    public static double getAverage(List<double[]> data) {

        double sum = 0;
        int size = data.size();
        for (int i = 0; i < size; i++) {
            sum += data.get(i)[3];
        }
        return sum / size;
    }


    public static double getStandardDevition(List<double[]> data) {
        double STD = 0;
        double Ave = getAverage(data);
        int size = data.size();
        for (int i = 0; i < size; i++) {
            STD += Math.pow((data.get(i)[3] - Ave), 2);
        }
        return Math.sqrt(STD / size);
    }

    public static double[] ArryFromList(List<Integer> Data) {
        int size = Data.size();
        double[] FromData = new double[size];
        for (int i = 0; i < size; i++) {
            FromData[i] = Data.get(i);
        }
        return FromData;
    }

    public static int getMaxValue(double[] data) {
        int size = data.length;
        double a = data[0];
        int index = 0;
        for (int i = 1; i < size; i++) {
            if (data[i] > a) {
                a = data[i];
                index = i;
            }
        }
        return index;
    }


    public static int getMaxIndex(int[] data) {
        int size = data.length;
        int a = data[0];
        int index = 0;
        for (int i = 1; i < size; i++) {
            if (data[i] >= a) {
                a = data[i];
                index = i;
            }
        }
        return index;
    }


    public static int getSum(int[] data) {
        int sum = 0;
        for (int i : data) {
            sum += i;
        }
        return sum;
    }

    public static double ModelValueVector(double VecX, double VecY) {
        double ModelValue;
        ModelValue = Math.sqrt(Math.pow(VecX, 2) + Math.pow(VecY, 2));
        return ModelValue;
    }

    public static double InterAngleVec(double Vec1X, double Vec1Y, double Vec2X, double Vec2Y) {
        double InterAngle;
        InterAngle = Math.acos((Vec1X * Vec2X + Vec1Y * Vec2Y) / (Math.sqrt(Vec1X * Vec1X + Vec1Y * Vec1Y) * Math.sqrt(Vec1X * Vec2X + Vec1Y * Vec2Y)));
        return InterAngle;
    }


    public static <T> List<T> deepCopyForList(List<T> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<T> dest = (List<T>) in.readObject();
        return dest;
    }

    public static int getMaxIndexForPdist(double[][] pdistMaxCluster) {
        int size = pdistMaxCluster[0].length;
        double a = pdistMaxCluster[0][0];
        int index = 0;
        for (int i = 1; i < size; i++) {
            if (pdistMaxCluster[0][i] >= a) {
                a = pdistMaxCluster[0][i];
                index = i;
            }
        }
        return index;
    }

    //    public static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
//        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//        ObjectOutputStream out = new ObjectOutputStream(byteOut);
//        out.writeObject(src);
//
//        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
//        ObjectInputStream in = new ObjectInputStream(byteIn);
//        @SuppressWarnings("unchecked")
//        List<T> dest = (List<T>) in.readObject();
//        return dest;
//    }
    public static double[] CalValForArry(double[] arry) {
        // TODO Auto-generated method stub
        double[] Arry = arry.clone();

        double Var = 0;
        double ave = 0;
        double sumArry = 0;
        for (int i = 0; i < Arry.length; i++) {
            sumArry += Arry[i];
        }
        ave = sumArry / Arry.length;

        double sumVar = 0;
        for(int i = 0;i<Arry.length;i++) {
            sumVar += (Arry[i] - ave)  * (Arry[i] - ave);
        }

        Var = sumVar / Arry.length;
        double[] Ave_Var = new double[2];
        Ave_Var[0] = ave;
        Ave_Var[1] = Var;
        return Ave_Var;
    }


    public static boolean  IsEqualOfTwoArry(Object[] a , Object[] b) {
        int lengthA = a.length;
        int lengthB = b.length;
        if(lengthA != lengthB) {
            return false;
        }
        for(int i = 0 ; i < lengthA ; i ++) {
            if ( a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
import static java.lang.Math.*;

import java.util.Arrays;

public class Mat3 {
    static final Mat3 I = new Mat3(new double[][] {
            {1, 0, 0}, {0, 1, 0}, {0, 0, 1}
    });
    
    final double[][] data;
    
    Mat3(double[][] data) {
        assert data.length == 3;
        for (int r = 0; r < 3; ++r)
            assert data[r].length == 3;
        this.data = data;
    }
    
    Mat3(double v00, double v01, double v02,
            double v10, double v11, double v12,
            double v20, double v21, double v22) {
        this(new double[][] {
                {v00, v01, v02},
                {v10, v11, v12},
                {v20, v21, v22},
        });
    }
    
    static Mat3 rotationMatrixX(double a) {
        return new Mat3(1, 0, 0, 0, cos(a), -sin(a), 0, sin(a), cos(a));
    }
    
    static Mat3 rotationMatrixY(double a) {
        return new Mat3(cos(a), 0, sin(a), 0, 1, 0, -sin(a), 0, cos(a));
    }
    
    static Mat3 rotationMatrixZ(double a) {
        return new Mat3(cos(a), -sin(a), 0, sin(a), cos(a), 0, 0, 0, 1);
    }
    
    static Mat3 rotationMatrix(double a, Vect3 u) {
        double c = cos(a), s = sin(a), omc = 1 - c;
        return new Mat3(
                c + u.x * u.x * omc, u.x * u.y * omc - u.z * s, u.x * u.z * omc + u.y * s,
                u.y * u.z * omc + u.z * s, c + u.y * u.y * omc, u.y * u.z * omc - u.x * s,
                u.z * u.x * omc - u.y * s, u.z * u.y * omc + u.x * s, c + u.z * u.z * omc);
        /*return I.scale(cos(a))
                .plus(u.crossProductMatrix().scale(sin(a)))
                .plus(u.tensorProductWithSelf().scale(1 - cos(a)));*/
    }
    
    Mat3 scale(double k) {
        double[][] result = new double[3][3];
        for (int r = 0; r < 3; ++r)
            for (int c = 0; c < 3; ++c)
                result[r][c] = k * data[r][c];
        return new Mat3(result);
    }
    
    Mat3 plus(Mat3 that) {
        double[][] result = new double[3][3];
        for (int r = 0; r < 3; ++r)
            for (int c = 0; c < 3; ++c)
                result[r][c] = data[r][c] + that.data[r][c];
        return new Mat3(result);
    }
    
    Mat3 minus(Mat3 that) {
        return plus(that.scale(-1));
    }
    
    Mat3 times(Mat3 that) {
        double[][] result = new double[3][3];
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                for (int k = 0; k < 3; ++k)
                    result[i][j] += data[i][k] * that.data[k][j];
        return new Mat3(result);
    }
    
    Vect3 apply(Vect3 v) {
        double[] result = new double[3];
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                result[i] += data[i][j] * v.get(j);
        return new Vect3(result);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof Mat3)
            return Arrays.deepEquals(data, ((Mat3) o).data);
        return false;
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 3; ++r) {
            if (r != 0)
                sb.append(' ');
            sb.append('[');
            for (int c = 0; c < 3; ++c) {
                if (c != 0)
                    sb.append(' ');
                sb.append(String.format("%+.2f", data[r][c]));
            }
            sb.append(']');
        }
        return sb.toString();
    }
    
    static { tests: {
        boolean ea = false;
        assert ea = true;
        if (!ea) break tests;
        
        assert I.scale(2).equals(I.plus(I));
        assert I.times(I).equals(I);
        
        assert I.apply(Vect3.UNIT_X).equals(Vect3.UNIT_X) : I.apply(Vect3.UNIT_X);
        assert I.apply(Vect3.UNIT_Y).equals(Vect3.UNIT_Y);
        assert I.apply(Vect3.UNIT_Z).equals(Vect3.UNIT_Z);
        assert I.apply(new Vect3(1, 1, 1)).equals(new Vect3(1, 1, 1));
        
        assert new Mat3(6, -7, 10, 0, 3, -1, 0, 5, -7).apply(new Vect3(2, 3, 4)).equals(new Vect3(31, 5, -13)) :
            new Mat3(6, -7, 10, 0, 3, -1, 0, 5, -7).apply(new Vect3(2, 3, 4));
        
        //Mat3 rotX = rotationMatrixX(Math.PI / 2);
        //Mat3 rotXArb = rotationMatrix(Math.PI / 2, Vect3.UNIT_X);
        //Mat3 rotXExpected = new Mat3(1, 0, 0, 0, 0, -1, 0, 1, 0);
        //assert rotX.equals(rotXExpected) : rotX + " != " + rotXExpected;
        //assert rotXArb.equals(rotXExpected) : rotXArb + " != " + rotXExpected;
        
        System.out.println("All Mat3 tests passed.");
    }}
}

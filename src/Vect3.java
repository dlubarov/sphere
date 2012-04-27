import static org.lwjgl.opengl.GL11.*;
import static java.lang.Math.*;

import java.util.Arrays;

public class Vect3 {
    static final Vect3
            UNIT_X = new Vect3(1, 0, 0),
            UNIT_Y = new Vect3(0, 1, 0),
            UNIT_Z = new Vect3(0, 0, 1);
    
    final double x, y, z;
    
    Vect3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    Vect3(double[] data) {
        assert data.length == 3;
        x = data[0];
        y = data[1];
        z = data[2];
    }
    
    double get(int i) {
        switch (i) {
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default: throw new IllegalArgumentException();
        }
    }
    
    Vect3 neg() {
        return scale(-1);
    }
    
    Vect3 plus(Vect3 that) {
        return new Vect3(x + that.x, y + that.y, z + that.z);
    }
    
    Vect3 minus(Vect3 that) {
        return plus(that.neg());
    }
    
    Vect3 scale(double k) {
        return new Vect3(x * k, y * k, z * k);
    }
    
    double distanceTo(Vect3 that) {
        return minus(that).norm();
    }
    
    double dot(Vect3 that) {
        return x * that.x + y * that.y + z * that.z;
    }
    
    Vect3 cross(Vect3 that) {
        return new Vect3(
                y * that.z - z * that.y,
                z * that.x - x * that.z,
                x * that.y - y * that.x);
    }
    
    Vect3 somethingOrtho() {
        return cross(Vect3.UNIT_Y);
        //return cross(new Vect3(3, 7, 11)).normalized();
    }
    
    double norm2() {
        return x * x + y * y + z * z;
    }
    
    double norm() {
        return sqrt(norm2());
    }
    
    Vect3 normalized() {
        return scale(1 / norm());
    }
    
    Mat3 crossProductMatrix() {
        return new Mat3(0, -z, y, z, 0, -x, -y, x, 0);
    }
    
    Mat3 tensorProductWithSelf() {
        return new Mat3(x*x, x*y, x*z, x*y, y*y, y*z, x*z, y*z, z*z);
    }
    
    Vect3 rotateX(double a) {
        return Mat3.rotationMatrixX(a).apply(this);
    }
    
    Vect3 rotateY(double a) {
        return Mat3.rotationMatrixY(a).apply(this);
    }
    
    Vect3 rotateZ(double a) {
        return Mat3.rotationMatrixZ(a).apply(this);
    }
    
    Vect3 rotate(double a, Vect3 u) {
        return Mat3.rotationMatrix(a, u).apply(this);
    }
    
    void glVertex() {
        glVertex3d(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Vect3) {
            Vect3 that = (Vect3) o;
            return x == that.x && y == that.y && z == that.z;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new double[] {x, y, z});
    }
    
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f, %.2f)", x, y, z);
    }
}

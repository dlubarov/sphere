import static org.lwjgl.opengl.GL11.*;

public class Ring {
    final int n;
    final double phase;
    final boolean back;
    
    Ring(double phase, boolean back) {
        n = 16;
        this.phase = phase;
        this.back = back;
    }
    
    Vect3 getNormal(double m, double b, double p) {
        double rotPhase = m + phase;
        if (back)
            rotPhase = -rotPhase;
        return Vect3.UNIT_X.rotateY(rotPhase * 2 * Math.PI);
    }
    
    Vect3 getPoint(double m, double b, double p, double rad, double ptPhase) {
        Vect3 n = getNormal(m, b, p);
        return n.somethingOrtho().rotate(ptPhase * 2 * Math.PI, n).scale(rad);
    }
    
    void render(double m, double b, double p, double rad) {
        for (int i = 0; i < n; ++i) {
            Vect3 c = getPoint(m, b, p, rad, i / (double) (n - 1));
            glBegin(GL_TRIANGLES);
            for (int j = 0; j < 3; ++j) {
                new Color((phase + j/3.0/5.0) % 1, 1, 0.8).bind();
                Vect3 triPoint = c.normalized().somethingOrtho()
                        .rotate(2 * Math.PI * (j / 3.0 + b), c.normalized())
                        .scale(5).plus(c);
                triPoint.glVertex();
            }
            glEnd();
        }
    }
}

import static org.lwjgl.opengl.GL11.*;

public class Color {
    final double h, s, v;
    
    Color(double h, double s, double v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }
    
    void bind() {
        java.awt.Color c = java.awt.Color.getHSBColor((float) h, (float) s, (float) v);
        float[] rgb = c.getRGBColorComponents(null);
        glColor3f(rgb[0], rgb[1], rgb[2]);
    }
    
    void bindClear() {
        java.awt.Color c = java.awt.Color.getHSBColor((float) h, (float) s, (float) v);
        float[] rgb = c.getRGBColorComponents(null);
        glClearColor(rgb[0], rgb[1], rgb[2], 1);
    }
}

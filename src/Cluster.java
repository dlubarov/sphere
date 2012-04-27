import java.util.*;

import static org.lwjgl.opengl.GL11.*;

public class Cluster {
    final static Cluster singleton = new Cluster();
    
    Collection<Layer> layers;
    
    Cluster() {
        setLayers(1);
    }
    
    void setLayers(int n) {
        layers = new HashSet<Layer>();
        for (int i = 0; i < n; ++i)
            layers.add(new Layer(i / (double) n));
    }
    
    void render(double m, double b, double p) {
        if (m == 4)
            setLayers(2);
        if (m == 8)
            setLayers(3);
        if (m == 12)
            setLayers(4);
        
        glPushMatrix();
        double s = 1;
        double mRem = Timing.numMeasures - m;
        if (mRem < 2)
            s *= mRem / 2;
        glScaled(s, s, s);
        for (Layer l : layers)
            l.render(m, b, p);
        glPopMatrix();
    }
}

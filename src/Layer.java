
public class Layer {
    final double phase;
    Ring[] rings;
    
    Layer(double phase) {
        this.phase = phase;
        setRings(2, false);
    }
    
    void setRings(int n, boolean someReverse) {
        rings = new Ring[n];
        if (someReverse)
            for (int i = 0; i < n; i += 2) {
                rings[i] = new Ring(i / (double) n, false);
                rings[i + 1] = new Ring(i / (double) n, true);
            }
        else
            for (int i = 0; i < n; ++i)
                rings[i] = new Ring(i / (double) n, false);
    }
    
    double rad(double m) {
        double r = Math.sin((phase + m % 1) * Math.PI) * 80;
        return r;
    }
    
    void render(double m, double b, double p) {
        if (m == 4)
            setRings(4, false);
        if (m == 8)
            setRings(8, false);
        if (m == 12)
            setRings(16, true);
        if (m == 16)
            setRings(8, false);
        for (Ring r : rings)
            r.render(m, b, p, rad(m));
    }
}

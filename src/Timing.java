
public class Timing {
    static int beatsPerMeasure = 4,
            pulsesPerBeat = 4,
            ticksPerPulse = 8,
            numMeasures = 20;
    
    static double beatDur = 0.75,
            pulseDur = beatDur / pulsesPerBeat,
            tickDur = pulseDur / ticksPerPulse,
            measureDur = beatDur * beatsPerMeasure;
    
    static double t0;
    
    static void mainLoop() throws Exception {
        System.out.printf("Running at %.2f FPS.\n", 1 / tickDur);
        t0 = System.nanoTime() * 1e-9;
        for (int tick = 0;; ++tick) {
            double p = tick / (double) ticksPerPulse,
                   b = p / pulsesPerBeat,
                   m = b / beatsPerMeasure;
            if (m > numMeasures)
                break;
            
            if (Math.random() < -0.05) {
                double timeUsed = time() - tick * tickDur;
                System.out.printf("Used %.3fs of %.3fs.\n", timeUsed, tickDur);
            }
            
            Animation.render(m, b, p);
            sleepUntil((tick + 1) * tickDur);
        }
        Thread.sleep(2000);
        System.exit(0);
    }
    
    static double time() {
        return System.nanoTime() * 1e-9 - t0;
    }
    
    static void sleepUntil(double t) {
        while (time() < t)
            ;
        /*for (;;) {
            double dt = t - time();
            if (dt <= 0)
                return;
            if (dt > .01) {
                long ns = (long) ((dt - .01) * 1e9);
                java.util.concurrent.locks.LockSupport.parkNanos(ns);
            }
        }*/
    }
}

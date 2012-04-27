import static java.lang.Math.*;

import java.util.Random;

import javax.sound.sampled.*;

public class Audio extends Thread {
    static final int SAMPLE_RATE = 16 * 1024; // KHz
    static final int BYTES = 2;
    static final AudioFormat af;
    static final SourceDataLine line;
    static final double DURATION = Timing.measureDur * (Timing.numMeasures + 1);
    static final double[] amp;
    static final boolean clamp = true;
    
    static double[] majorIntervals = {2, 2, 1, 2, 2, 2, 1};
    
    static {
        af = new AudioFormat(SAMPLE_RATE, 8*BYTES, 1, true, false);
        try {
            line = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        amp = new double[(int) (DURATION * SAMPLE_RATE)];
        generate();
        getData();
    }
    
    static boolean consonant(double a, double b) {
        for (int i = 1; i <= 4; ++i)
            for (int j = 1; j <= 4; ++j) {
                double aa = i * a, bb = j * b;
                double ratio = aa / bb;
                if (ratio < 1)
                    ratio = 1 / ratio;
                if (ratio < 1.05)
                    return true;
            }
        return false;
    }
    
    static double note(int notesFromBase) {
        double base = 200;
        int pos = 0;
        int halfSteps = 0;
        for (int i = 0; i < notesFromBase; ++i)
            halfSteps += majorIntervals[pos++ % majorIntervals.length];
        return base * Math.pow(2, halfSteps/12.0);
    }
    
    static void generate() {
        Random r = new Random();
        int lastBaseNote = -1;
        for (int m = 0; m < Timing.numMeasures; ++m) {
            System.out.println("Generating measure " + (m + 1) + "/" + Timing.numMeasures);
            int baseNote;
            do {
                baseNote = r.nextInt(4);
            } while (baseNote == lastBaseNote);
            addDecaying(note(baseNote + 3), 0.4, Timing.measureDur * m, Timing.beatDur * 3);
            lastBaseNote = baseNote;
            
            boolean second = m >= 4 && m < 16;
            for (int b = 0; b < Timing.beatsPerMeasure; ++b) {
                int beatNote = baseNote + 3;
                if (m % 2 != 0 || m >= 4)
                    beatNote += r.nextInt(3);
                while (!consonant(note(baseNote), note(beatNote)))
                    ++beatNote;
                for (int p = 0; p < Timing.pulsesPerBeat; ++p) {
                    double t = Timing.measureDur * m + Timing.beatDur * b + Timing.pulseDur * p;
                    int pulseNote = beatNote;
                    if (p == 1) ++pulseNote;
                    if (p == 2) pulseNote += 3;
                    addDecaying(note(pulseNote), 0.3, t, Timing.pulseDur);
                    if (second) {
                        int secondPulseNote = pulseNote + 4;
                        if (r.nextInt(3) == 0)
                            secondPulseNote = beatNote + r.nextInt(6);
                        while (!consonant(note(pulseNote), note(secondPulseNote))) {
                            if (secondPulseNote > 0 && r.nextBoolean())
                                ++secondPulseNote;
                            else
                                --secondPulseNote;
                        }
                        addDecaying(note(secondPulseNote), 0.3, t, Timing.pulseDur);
                    }
                }
            }
        }
        addDecaying(note(3), 0.6, Timing.measureDur * Timing.numMeasures, Timing.measureDur);
    }
    
    static byte[] data;
    static void getData() {
        byte[] result = new byte[amp.length * BYTES];
        for (int i = 0; i < amp.length; ++i) {
            if (clamp) {
                if (amp[i] < -1) amp[i] = -1;
                if (amp[i] > 1) amp[i] = 1;
            }
            if (amp[i] > 1 || amp[i] < -1)
                throw new RuntimeException("amplitude out of bounds");
            int a = (int) (amp[i] * ((1 << 8 * BYTES - 1) - 1));
            for (int j = 0; j < BYTES; ++j)
                result[i*BYTES + j] = (byte) (a >> (8*j));
        }
        data = result;
    }

    @Override
    public void run() {
        try {
            line.open(af, SAMPLE_RATE);
            line.start();
            int chunkSize = 512;
            for (int p = 0; p < data.length; p += chunkSize)
                line.write(data, p, Math.min(data.length - p, chunkSize));
            line.drain();
            line.close();
            System.exit(0);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    static void addSin(double f, double mag, double t1, double dur) {
        double mul = 2 * PI * f / SAMPLE_RATE;
        double limit = (t1 + dur) * SAMPLE_RATE;
        for (int i = (int) (t1 * SAMPLE_RATE); i < limit; ++i)
            amp[i] += mag * sin(mul * i);
    }
    
    static void addDecaying(double f, double mag, double t1, double dur) {
        double mul = 2 * PI * f / SAMPLE_RATE;
        for (int i = (int) (t1 * SAMPLE_RATE);; ++i) {
            double dt = i / (double) SAMPLE_RATE - t1;
            double decay = 1 - dt / dur;
            if (decay <= 0)
                return;
            amp[i] += mag * decay * sin(mul * i);
        }
    }
}
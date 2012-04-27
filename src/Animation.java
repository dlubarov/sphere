import java.awt.image.*;
import java.nio.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import com.xuggle.mediatool.*;
import com.xuggle.xuggler.*;
import com.xuggle.xuggler.video.*;

import static org.lwjgl.opengl.GL11.*;

public class Animation {
    static final String title = "Sphere";
    static final int W = 800, H = 600;

    static boolean write = false;
    static IContainer outContainer;
    static IMediaWriter writer;
    static IStreamCoder outStreamCoder;

    public static BufferedImage convert(BufferedImage sourceImage) {
        int targetType = BufferedImage.TYPE_3BYTE_BGR;
        if (sourceImage.getType() == targetType)
            return sourceImage;
        BufferedImage image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), targetType);
        //image.getGraphics().drawImage(sourceImage, 0, 0, null);
        image.getGraphics().drawImage(sourceImage,
                0, 0, W, H,
                0, H, W, 0,
                null);
        return image;
    }

    public static void main(String[] args) throws Exception {
        Display.setDisplayMode(new DisplayMode(W, H));
        Display.setTitle("Animation");
        Display.create();

        glEnable(GL_DEPTH_TEST);
        glClearDepth(1);

        if (write) {
            outContainer = IContainer.make();
            int retval = outContainer.open("output.mp4", IContainer.Type.WRITE, null);
            if (retval < 0)
                throw new RuntimeException("could not open output file");
            IStream outStream = outContainer.addNewStream(0);
            outStreamCoder = outStream.getStreamCoder();
            //ICodec codec = ICodec.guessEncodingCodec(null, null, "output.mp4",
            //        null, ICodec.Type.CODEC_TYPE_VIDEO);
            ICodec codec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_H264);
            
            outStreamCoder.setNumPicturesInGroupOfPictures(5);
            outStreamCoder.setCodec(codec);
            outStreamCoder.setBitRate(200000);
            outStreamCoder.setBitRateTolerance(50000);
            outStreamCoder.setPixelType(IPixelFormat.Type.YUV420P);
            outStreamCoder.setHeight(H); outStreamCoder.setWidth(W);
            outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
            outStreamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, true);
            outStreamCoder.setGlobalQuality(0);
            IRational frameRate = IRational.make((int) (1 / Timing.tickDur), 1);
            outStreamCoder.setFrameRate(frameRate);
            outStreamCoder.setTimeBase(IRational.make(frameRate.getDenominator(), frameRate.getNumerator()));
            outStreamCoder.open();
            outContainer.writeHeader();
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override public void run() {
                    System.out.println("writing trailer");
                    outContainer.writeTrailer();
                }
            });
        }

        Audio audio = new Audio();
        System.out.println("Ready to start...");
        /*Keyboard.create();
        while (!Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
            Display.processMessages();
            Keyboard.poll();
        }*/
        audio.start();
        Timing.mainLoop();
    }

    static void render(double m, double b, double p) {
        if (Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
            System.exit(0);
        
        if (b % 1 == 0)
            Display.setTitle(String.format("%d:%d", (int) m, (int) b % 4));
        
        new Color(m, 0.25, 0.1).bindClear();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(60f, W / (float) H, .1f, 1000f);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        GLU.gluLookAt(40, 160, 40, 0, 0, 0, 0, 1, 0);

        // Axes
//        glBegin(GL_LINES);
//            // x axis in red
//            glColor3f(1, 0, 0);
//            glVertex3i(0, 0, 0);
//            glVertex3i(100, 0, 0);
//            // y axis in green
//            glColor3f(0, 1, 0);
//            glVertex3i(0, 0, 0);
//            glVertex3i(0, 100, 0);
//            // z axis in blue
//            glColor3f(0, 0, 1);
//            glVertex3i(0, 0, 0);
//            glVertex3i(0, 0, 100);
//        glEnd();
        
        Cluster.singleton.render(m, b, p);
        Display.update();
        
        if (write) {
            ByteBuffer bbuf = BufferUtils.createByteBuffer(W*H*4);
            glReadBuffer(GL_FRONT);
            glReadPixels(0, 0, W, H, GL_RGBA, GL_UNSIGNED_BYTE, bbuf);
            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
            //int[] dst = ((DataBufferInt) img.getData().getDataBuffer()).getData();
            for (int y = 0; y < H; ++y)
                for (int x = 0; x < W; ++x) {
                    int i = (y * W + x) * 4;
                    int rr = bbuf.get(i) & 0xFF,
                        gg = bbuf.get(i + 1) & 0xFF,
                        bb = bbuf.get(i + 2) & 0xFF;
                    int rgb = 0xff << 24 | rr << 16 | gg << 8 | bb;
                    img.setRGB(x, y, rgb);
                }
            img = convert(img);
            
            IPacket packet = IPacket.make();
            IConverter converter = ConverterFactory.createConverter(img, IPixelFormat.Type.YUV420P);
            long microseconds = (long) (b * Timing.beatDur * 1e6);
            IVideoPicture outFrame = converter.toPicture(img, microseconds); 
            if (m == 0)
                outFrame.setKeyFrame(true);
            outFrame.setQuality(0); 
            outStreamCoder.encodeVideo(packet, outFrame, 0); 
            outFrame.delete();
            if (packet.isComplete())
                outContainer.writePacket(packet);
            else
                System.out.println("INCOMPLETE");
        }
    }
}

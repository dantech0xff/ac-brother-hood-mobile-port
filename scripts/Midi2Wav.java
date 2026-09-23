import javax.sound.midi.*;
import javax.sound.sampled.*;
import com.sun.media.sound.AudioSynthesizer;
import com.sun.media.sound.SoftSynthesizer;
import java.io.File;
import java.util.*;

public class Midi2Wav {
    public static void main(String[] a) throws Exception {
        Sequence seq = MidiSystem.getSequence(new File(a[0]));
        AudioSynthesizer synth = new SoftSynthesizer();
        AudioFormat fmt = new AudioFormat(44100, 16, 2, true, false);
        AudioInputStream ais = synth.openStream(fmt, new HashMap<String, Object>());

        long seqBytes = (long) ((seq.getMicrosecondLength() / 1e6) * fmt.getFrameRate() * fmt.getFrameSize())
                        + 200_000;                       // ~55ms reverb tail + slack

        Sequencer seqr = MidiSystem.getSequencer(false);
        seqr.getTransmitter().setReceiver(synth.getReceiver());
        seqr.open();
        seqr.setSequence(seq);
        seqr.start();

        File out = new File(a[1]);
        byte[] buf = new byte[8192];
        long total = 0; long peak = 0;
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(out)) {
            while (total < seqBytes) {
                int n = ais.read(buf, 0, buf.length);
                if (n <= 0) { if (!seqr.isRunning()) break; Thread.sleep(5); continue; }
                fos.write(buf, 0, n);
                for (int i = 0; i + 3 < n; i += 4) {
                    int s = Math.abs((buf[i + 1] << 8) | (buf[i] & 0xFF));
                    if (s > peak) peak = s;
                }
                total += n;
            }
        }
        seqr.stop(); seqr.close(); ais.close();
        long frames = total / fmt.getFrameSize();
        System.out.println("bytes=" + total + " peak=" + peak + " ~sec=" + (frames / fmt.getFrameRate()));
        // rewrite as proper WAV (PCM stream → WAV header + data)
        File wav = new File(a[2]);
        AudioInputStream in = new AudioInputStream(new java.io.FileInputStream(out), fmt, frames);
        AudioSystem.write(in, AudioFileFormat.Type.WAVE, wav);
        System.out.println("wrote " + wav + " " + wav.length());
    }
}

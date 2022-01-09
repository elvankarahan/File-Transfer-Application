import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class TransferUtils
{
    public static final int PACKET_SIZE = 1024;
    public static final int SENDER_PORT = 26000;
    public static final int RECEIVER_PORT = 26100;
    public static final int TIMEOUT = 5000;
    public static final String MESSAGE_ACCEPT = "x_ACCEPT_x";
    public static final String MESSAGE_REFUSE = "x_REFUSE_x";
    public static final int ACCEPTED = 0;
    public static final int ERROR_REFUSED = 1;
    public static final int ERROR_ABORTED = 2;
    public static final int ERROR_TIMEOUT = 3;
    
    public static byte[] fileToBytes(final File f) throws IOException {
        final InputStream fis = new FileInputStream(f);
        if (f.length() > 2147483647L) {
            throw new IOException("File size is too big.");
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream((int)f.length());
        final byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        fis.close();
        return baos.toByteArray();
    }
    
    public static void bytesToFile(final byte[] bytes, final File file) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            bos.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
            try {
                if (bos != null) {
                    bos.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
    
    public static byte[] intToBytes(final int number) {
        return new byte[] { (byte)(number >>> 24 & 0xFF), (byte)(number >>> 16 & 0xFF), (byte)(number >>> 8 & 0xFF), (byte)(number & 0xFF) };
    }
    
    public static int bytesToInt(final byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
    
    public static byte[] createPacket(final byte[]... bytes) {
        int offset = 0;
        int totalLength = 0;
        for (int i = 0; i < bytes.length; ++i) {
            totalLength += bytes[i].length;
        }
        final byte[] packet = new byte[totalLength];
        for (int j = 0; j < bytes.length; ++j) {
            if (bytes[j].length > 0) {
                System.arraycopy(bytes[j], 0, packet, offset, bytes[j].length);
                offset += bytes[j].length;
            }
        }
        return packet;
    }
}

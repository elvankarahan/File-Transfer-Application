import java.net.InetAddress;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.DatagramSocket;
import java.io.File;

public class FileReceiver implements Runnable
{
    private String senderIP;
    private File file;
    private byte[] fileData;
    private DatagramSocket socket;
    private int seqNo;
    private String fileName;
    private int numOfPackets;
    private boolean receiving;
    private FileReceiverListener listener;
    
    public FileReceiver() throws SocketException {
        this.socket = new DatagramSocket(26100);
    }
    
    public void listen() {
        new Thread(this).start();
    }
    
    public boolean isReceiving() {
        return this.receiving;
    }
    
    public void setFile(final File file) {
        this.file = file;
    }
    
    public void abort() {
        this.seqNo = -1;
        this.sendAck();
        this.close();
    }
    
    public void close() {
        this.receiving = false;
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
    }
    
    @Override
    public void run() {
        if (!this.waitForInit()) {
            return;
        }
        this.seqNo = 0;
        this.receiving = true;
        this.listener.fileReceived(0);
        do {
            this.waitForPacket();
            this.sendAck();
        } while (this.seqNo < this.numOfPackets - 1 && this.receiving);
        if (this.receiving) {
            TransferUtils.bytesToFile(this.fileData, this.file);
        }
        this.close();
    }
    
    private boolean waitForInit() {
        final byte[] buffer = new byte[128];
        try {
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
            }
            catch (SocketException se) {
                return false;
            }
            final String msg = new String(packet.getData()).trim();
            this.fileName = msg.substring(0, msg.indexOf(124));
            final int fileSize = Integer.parseInt(msg.substring(msg.indexOf(124) + 1));
            this.senderIP = packet.getAddress().getHostAddress();
            this.numOfPackets = (int)Math.ceil(fileSize / 1024.0);
            this.fileData = new byte[fileSize];
            final boolean choice = this.listener.fileInfoReceived(this.fileName, fileSize);
            if (choice) {
                this.responseInit("x_ACCEPT_x");
                return true;
            }
            this.responseInit("x_REFUSE_x");
            this.close();
            return false;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void responseInit(final String message) {
        final byte[] data = message.getBytes();
        try {
            final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.senderIP), 26000);
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void waitForPacket() {
        final byte[] buffer = new byte[1032];
        try {
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            System.out.println("Waiting for packet...");
            try {
                this.socket.receive(packet);
            }
            catch (SocketException se) {
                return;
            }
            final byte[] seqBytes = new byte[4];
            System.arraycopy(packet.getData(), 0, seqBytes, 0, seqBytes.length);
            final byte[] lengthBytes = new byte[4];
            System.arraycopy(packet.getData(), 4, lengthBytes, 0, lengthBytes.length);
            this.seqNo = TransferUtils.bytesToInt(seqBytes);
            final int bytesLength = TransferUtils.bytesToInt(lengthBytes);
            final byte[] bytes = new byte[bytesLength];
            System.arraycopy(packet.getData(), 8, bytes, 0, bytes.length);
            if (this.seqNo == -1 && bytesLength == 0) {
                this.receiving = false;
                this.listener.errorOccurred(2);
                return;
            }
            System.arraycopy(bytes, 0, this.fileData, this.seqNo * 1024, bytesLength);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private synchronized void sendAck() {
        final byte[] seqBytes = TransferUtils.intToBytes(this.seqNo);
        try {
            final DatagramPacket packet = new DatagramPacket(seqBytes, seqBytes.length, InetAddress.getByName(this.senderIP), 26000);
            System.out.println("Sending Ack...");
            try {
                this.socket.send(packet);
            }
            catch (SocketException ex) {}
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.listener.fileReceived((int)(100.0 * ((this.seqNo + 1) / (double)this.numOfPackets)));
    }
    
    public void setFileReceiverListener(final FileReceiverListener l) {
        this.listener = l;
    }
    
    public interface FileReceiverListener
    {
        boolean fileInfoReceived(final String p0, final int p1);
        
        void fileReceived(final int p0);
        
        void errorOccurred(final int p0);
    }
}

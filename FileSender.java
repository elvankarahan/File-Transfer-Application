import java.net.SocketTimeoutException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.IOException;
import java.io.File;
import java.net.DatagramSocket;

public class FileSender implements Runnable
{
    private String receiverIP;
    private DatagramSocket socket;
    private File file;
    private byte[] fileData;
    private int seqNo;
    private int numOfPackets;
    private FileSenderListener listener;
    private boolean sending;
    
    public FileSender(final String receiverIP, final File file) throws IOException {
        this.receiverIP = receiverIP;
        this.file = file;
        this.fileData = TransferUtils.fileToBytes(file);
        this.seqNo = 0;
        this.numOfPackets = (int)Math.ceil(this.fileData.length / 1024.0);
        this.sending = false;
        this.socket = new DatagramSocket(26000);
    }
    
    public void send() {
        new Thread(this).start();
    }
    
    public boolean isSending() {
        return this.sending;
    }
    
    public void abort() {
        if (!this.sending) {
            return;
        }
        this.seqNo = -1;
        this.sendPacket(new byte[0]);
        this.sending = false;
    }
    
    public void close() {
        this.sending = false;
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
    }
    
    @Override
    public void run() {
        this.sendInit();
        final int response = this.waitForInit();
        if (response == 1) {
            return;
        }
        this.seqNo = 0;
        this.sending = true;
        this.listener.fileSent(0);
        try {
            this.socket.setSoTimeout(5000);
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] data = new byte[1024];
        while (this.seqNo < this.numOfPackets && this.sending) {
            int length = data.length;
            if (this.seqNo == this.numOfPackets - 1) {
                length = this.fileData.length - this.seqNo * 1024;
                data = new byte[length];
            }
            System.arraycopy(this.fileData, this.seqNo * 1024, data, 0, length);
            this.sendPacket(data);
            this.waitForAck();
        }
        this.close();
    }
    
    private void sendInit() {
        final String initMsg = this.file.getName() + "|" + this.fileData.length;
        final byte[] data = initMsg.getBytes();
        try {
            final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.receiverIP), 26100);
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private int waitForInit() {
        final byte[] buffer = new byte[64];
        try {
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                this.socket.receive(packet);
            }
            catch (SocketTimeoutException ste) {
                this.listener.errorOccurred(3);
                return 3;
            }
            catch (SocketException se) {
                return -1;
            }
            final String msg = new String(packet.getData()).trim();
            if (msg.equals("x_ACCEPT_x")) {
                return 0;
            }
            if (msg.equals("x_REFUSE_x")) {
                this.listener.errorOccurred(1);
                return 1;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    private synchronized void sendPacket(final byte[] bytes) {
        final byte[] seqBytes = TransferUtils.intToBytes(this.seqNo);
        final byte[] lengthBytes = TransferUtils.intToBytes(bytes.length);
        final byte[] data = TransferUtils.createPacket(new byte[][] { seqBytes, lengthBytes, bytes });
        try {
            final DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(this.receiverIP), 26100);
            System.out.println("Sending packet...");
            this.socket.send(packet);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private boolean waitForAck() {
        try {
            while (true) {
                final byte[] seqBytes = new byte[4];
                final DatagramPacket packet = new DatagramPacket(seqBytes, seqBytes.length);
                try {
                    System.out.println("Waiting for Ack...");
                    this.socket.receive(packet);
                }
                catch (SocketTimeoutException ste) {
                    return false;
                }
                catch (SocketException se) {
                    return false;
                }
                final int receivedSeqNo = TransferUtils.bytesToInt(packet.getData());
                if (receivedSeqNo == this.seqNo) {
                    ++this.seqNo;
                    this.listener.fileSent((int)(100.0 * (this.seqNo / (double)this.numOfPackets)));
                    return true;
                }
                if (receivedSeqNo == -1) {
                    this.sending = false;
                    this.listener.errorOccurred(2);
                    return false;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void setFileSenderListener(final FileSenderListener l) {
        this.listener = l;
    }
    
    public interface FileSenderListener
    {
        void fileSent(final int p0);
        
        void errorOccurred(final int p0);
    }
}

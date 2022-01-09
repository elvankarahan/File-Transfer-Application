import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.net.SocketException;
import java.io.File;
import javax.swing.JFileChooser;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class ReceiveScreen extends Screen
{
    private JProgressBar progressBar;
    private JLabel nameLabel;
    private JLabel sizeLabel;
    private JLabel progressLabel;
    private JButton backButton;
    private JButton abortButton;
    private FileReceiver fileReceiver;
    
    public ReceiveScreen(final TransferFrame frame) {
        super(frame);
        try {
            (this.fileReceiver = new FileReceiver()).setFileReceiverListener(new FileReceiver.FileReceiverListener() {
                @Override
                public boolean fileInfoReceived(final String fileName, final int fileSize) {
                    ReceiveScreen.this.nameLabel.setText(fileName);
                    double size = fileSize / 1024.0;
                    ReceiveScreen.this.sizeLabel.setText(String.format("%.1f KB", size));
                    if (size > 1000.0) {
                        size /= 1024.0;
                        ReceiveScreen.this.sizeLabel.setText(String.format("%.1f MB", size));
                    }
                    int response = JOptionPane.showConfirmDialog(ReceiveScreen.this.frame(), "Do you confirm the file: " + fileName, "Confirm File", 0);
                    if (response != 0) {
                        ReceiveScreen.this.changeScreen(new MainScreen(ReceiveScreen.this.frame()));
                        return false;
                    }
                    final JFileChooser jfc = new JFileChooser();
                    jfc.setSelectedFile(new File(fileName));
                    response = jfc.showSaveDialog(ReceiveScreen.this.frame());
                    if (response == 0) {
                        ReceiveScreen.this.fileReceiver.setFile(jfc.getSelectedFile());
                        ReceiveScreen.this.nameLabel.setText(jfc.getSelectedFile().getName());
                        System.out.println(jfc.getSelectedFile());
                        ReceiveScreen.this.backButton.setEnabled(false);
                        ReceiveScreen.this.abortButton.setEnabled(true);
                        return true;
                    }
                    ReceiveScreen.this.changeScreen(new MainScreen(ReceiveScreen.this.frame()));
                    return false;
                }
                
                @Override
                public void fileReceived(final int percent) {
                    ReceiveScreen.this.progressLabel.setText(percent + " %");
                    ReceiveScreen.this.progressBar.setValue(percent);
                    if (percent == 100) {
                        ReceiveScreen.this.progressLabel.setText("Completed");
                        ReceiveScreen.this.abortButton.setEnabled(false);
                        ReceiveScreen.this.backButton.setEnabled(true);
                        JOptionPane.showMessageDialog(ReceiveScreen.this.frame(), "File transfer is completed!");
                    }
                }
                
                @Override
                public void errorOccurred(final int errorCode) {
                    if (errorCode == 2) {
                        ReceiveScreen.this.fileReceiver.close();
                        ReceiveScreen.this.changeScreen(new MainScreen(ReceiveScreen.this.frame()));
                        JOptionPane.showMessageDialog(ReceiveScreen.this.frame(), "File transfer is aborted by the sender.", "Aborted", 0);
                    }
                }
            });
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = 17;
        c.insets = new Insets(2, 2, 8, 2);
        mainPanel.add(new JLabel("File Name:"), c);
        this.nameLabel = new JLabel("Waiting for the file...");
        c.gridx = 1;
        c.gridy = 0;
        mainPanel.add(this.nameLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        mainPanel.add(new JLabel("File Size:"), c);
        this.sizeLabel = new JLabel("-");
        c.gridx = 1;
        c.gridy = 1;
        mainPanel.add(this.sizeLabel, c);
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(10, 2, 2, 2);
        mainPanel.add(new JLabel("Progress:"), c);
        (this.progressBar = new JProgressBar(0, 100)).setPreferredSize(new Dimension(180, 24));
        this.progressBar.setMinimumSize(this.progressBar.getPreferredSize());
        c.gridx = 1;
        c.gridy = 2;
        mainPanel.add(this.progressBar, c);
        c.gridx = 2;
        c.gridy = 2;
        mainPanel.add(this.progressLabel = new JLabel(""), c);
        final JPanel bottomPanel = new JPanel(new FlowLayout(1));
        (this.backButton = new JButton("Back")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ReceiveScreen.this.fileReceiver.close();
                ReceiveScreen.this.changeScreen(new MainScreen(ReceiveScreen.this.frame()));
            }
        });
        (this.abortButton = new JButton("Abort")).setEnabled(false);
        this.abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ReceiveScreen.this.fileReceiver.abort();
                ReceiveScreen.this.backButton.setEnabled(true);
                ReceiveScreen.this.abortButton.setEnabled(false);
                ReceiveScreen.this.changeScreen(new MainScreen(ReceiveScreen.this.frame()));
            }
        });
        bottomPanel.add(this.backButton);
        bottomPanel.add(this.abortButton);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.fill = 2;
        mainPanel.add(bottomPanel, c);
        this.add(mainPanel);
        this.fileReceiver.listen();
    }
}

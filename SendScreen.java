import java.awt.FlowLayout;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

public class SendScreen extends Screen
{
    private JTextField ipText;
    private JTextField fileText;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JButton chooseButton;
    private JButton backButton;
    private JButton sendButton;
    private File selectedFile;
    private FileSender fileSender;
    
    public SendScreen(final TransferFrame frame) {
        super(frame);
        final JPanel mainPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        (this.fileText = new JTextField(14)).setEditable(false);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = 17;
        c.fill = 0;
        c.insets = new Insets(2, 2, 2, 2);
        final JLabel fileLabel = new JLabel("File Path:");
        mainPanel.add(fileLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.fill = 2;
        mainPanel.add(this.fileText, c);
        (this.chooseButton = new JButton("Choose File")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser jfc = new JFileChooser();
                final int response = jfc.showOpenDialog(SendScreen.this.frame());
                if (response == 0) {
                    SendScreen.this.selectedFile = jfc.getSelectedFile();
                    SendScreen.this.fileText.setText(SendScreen.this.selectedFile.getAbsolutePath());
                    SendScreen.this.sendButton.setEnabled(!SendScreen.this.fileText.getText().isEmpty() && !SendScreen.this.ipText.getText().trim().isEmpty());
                }
            }
        });
        c.gridx = 2;
        c.gridy = 0;
        c.fill = 0;
        mainPanel.add(this.chooseButton, c);
        (this.ipText = new JTextField(14)).addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                if (SendScreen.this.ipText.getText().trim().isEmpty() || SendScreen.this.fileText.getText().trim().isEmpty()) {
                    SendScreen.this.sendButton.setEnabled(false);
                }
                else {
                    SendScreen.this.sendButton.setEnabled(true);
                }
            }
            
            @Override
            public void keyTyped(final KeyEvent e) {
                if ((!Character.isDigit(e.getKeyChar()) && e.getKeyChar() != '.') || SendScreen.this.ipText.getText().length() == 15) {
                    e.consume();
                }
            }
        });
        c.gridx = 0;
        c.gridy = 1;
        final JLabel ipLabel = new JLabel("Receiver's IP:");
        mainPanel.add(ipLabel, c);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = 2;
        mainPanel.add(this.ipText, c);
        (this.sendButton = new JButton("Send File")).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    if (SendScreen.this.fileSender != null && SendScreen.this.fileSender.isSending()) {
                        SendScreen.this.sendButton.setEnabled(false);
                        SendScreen.this.fileSender.abort();
                        SendScreen.this.changeScreen(new SendScreen(SendScreen.this.frame()));
                    }
                    else {
                        SendScreen.this.fileSender = new FileSender(SendScreen.this.ipText.getText(), SendScreen.this.selectedFile);
                        SendScreen.this.fileSender.setFileSenderListener(new FileSender.FileSenderListener() {
                            @Override
                            public void fileSent(final int percent) {
                                SendScreen.this.progressLabel.setText(percent + " %");
                                SendScreen.this.progressBar.setValue(percent);
                                SendScreen.this.sendButton.setText("Abort");
                                SendScreen.this.sendButton.setEnabled(true);
                                if (percent == 100) {
                                    SendScreen.this.progressLabel.setText("Completed");
                                    SendScreen.this.sendButton.setText("Send File");
                                    SendScreen.this.sendButton.setEnabled(false);
                                    SendScreen.this.backButton.setEnabled(true);
                                    JOptionPane.showMessageDialog(SendScreen.this.frame(), "File transfer is completed!");
                                }
                            }
                            
                            @Override
                            public void errorOccurred(final int errorCode) {
                                if (errorCode == 2) {
                                    SendScreen.this.fileSender.close();
                                    JOptionPane.showMessageDialog(SendScreen.this.frame(), "File transfer is aborted by the receiver.", "Aborted", 0);
                                    SendScreen.this.changeScreen(new SendScreen(SendScreen.this.frame()));
                                }
                                else if (errorCode == 1) {
                                    SendScreen.this.fileSender.close();
                                    JOptionPane.showMessageDialog(SendScreen.this.frame(), "File transfer is refused by the receiver.", "Refused", 0);
                                    SendScreen.this.changeScreen(new SendScreen(SendScreen.this.frame()));
                                }
                            }
                        });
                        SendScreen.this.fileSender.send();
                        SendScreen.this.sendButton.setEnabled(false);
                        SendScreen.this.backButton.setEnabled(false);
                        SendScreen.this.progressLabel.setText("Waiting...");
                    }
                }
                catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        });
        this.sendButton.setEnabled(false);
        c.gridx = 2;
        c.gridy = 1;
        c.fill = 0;
        mainPanel.add(this.sendButton, c);
        c.insets = new Insets(10, 2, 2, 2);
        (this.progressBar = new JProgressBar(0, 100)).setPreferredSize(new Dimension(180, 24));
        c.gridx = 0;
        c.gridy = 2;
        final JLabel barLabel = new JLabel("Progress:");
        barLabel.setHorizontalAlignment(4);
        mainPanel.add(barLabel, c);
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
                if (SendScreen.this.fileSender != null) {
                    SendScreen.this.fileSender.close();
                }
                SendScreen.this.changeScreen(new MainScreen(SendScreen.this.frame()));
            }
        });
        bottomPanel.add(this.backButton);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.fill = 2;
        mainPanel.add(bottomPanel, c);
        this.add(mainPanel);
    }
}

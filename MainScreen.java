import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JButton;

public class MainScreen extends Screen
{
    private JButton sendButton;
    private JButton receiveButton;
    
    public MainScreen(final TransferFrame frame) {
        super(frame);
        final JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(2, 1));
        (this.sendButton = new JButton("Send File")).setFont(MainScreen.DEFAULT_FONT);
        this.sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainScreen.this.changeScreen(new SendScreen(MainScreen.this.frame()));
            }
        });
        mainPanel.add(this.sendButton);
        (this.receiveButton = new JButton("Receive File")).setFont(MainScreen.DEFAULT_FONT);
        this.receiveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                MainScreen.this.changeScreen(new ReceiveScreen(MainScreen.this.frame()));
            }
        });
        mainPanel.add(this.receiveButton);
        mainPanel.setPreferredSize(new Dimension(180, 100));
        this.add(mainPanel);
    }
}

import javax.swing.UIManager;
import java.awt.Component;
import javax.swing.JFrame;

public class TransferFrame extends JFrame
{
    private Screen screen;
    
    public TransferFrame() {
        super("File Transfer Application");
        this.changeScreen(new MainScreen(this));
        this.setDefaultCloseOperation(3);
        this.setResizable(false);
        this.setSize(Screen.SCREEN_SIZE);
        this.setLocationRelativeTo(null);
    }
    
    public void changeScreen(final Screen s) {
        if (this.screen != null) {
            this.remove(this.screen.panel());
        }
        this.screen = s;
        this.getContentPane().add(this.screen.panel(), "Center");
        this.revalidate();
        this.repaint();
    }
    
    public static void main(final String[] args) {
        for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equals("Nimbus")) {
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                }
                catch (Exception e1) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                    catch (Exception ex) {}
                }
            }
        }
        new TransferFrame().setVisible(true);
    }
}

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.Font;
import java.awt.Dimension;

public class Screen
{
    public static final Dimension SCREEN_SIZE;
    public static final Font DEFAULT_FONT;
    public static final Font DEFAULT_BIG_FONT;
    private TransferFrame frame;
    private JPanel panel;
    
    public Screen(final TransferFrame frame) {
        this.frame = frame;
        (this.panel = new JPanel(new GridBagLayout())).setPreferredSize(Screen.SCREEN_SIZE);
    }
    
    public Screen() {
        this(null);
    }
    
    public void add(final Component component) {
        this.panel.add(component);
    }
    
    protected JPanel panel() {
        return this.panel;
    }
    
    protected TransferFrame frame() {
        return this.frame;
    }
    
    protected void changeScreen(final Screen screen) {
        this.frame.changeScreen(screen);
        screen.frame = this.frame;
    }
    
    static {
        SCREEN_SIZE = new Dimension(480, 240);
        DEFAULT_FONT = new Font("SansSerif", 1, 14);
        DEFAULT_BIG_FONT = new Font("SansSerif", 1, 18);
    }
}

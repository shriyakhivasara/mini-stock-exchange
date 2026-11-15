package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * Colours, fonts and small formatting helpers shared by every screen,
 * kept in one place so the whole application looks consistent.
 */
public class Theme {

    public static final Color BUY_GREEN = new Color(0, 135, 75);
    public static final Color SELL_RED = new Color(200, 45, 45);
    public static final Color HEADER_BLUE = new Color(28, 48, 80);
    public static final Color MUTED_TEXT = new Color(105, 110, 120);

    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 20);
    public static final Font HEADING_FONT = new Font("SansSerif", Font.BOLD, 14);

    /** Empty space around the inside of a panel. */
    public static Border padding() {
        return BorderFactory.createEmptyBorder(10, 10, 10, 10);
    }

    /** A bold label used as the title of each panel. */
    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        return label;
    }

    /** 1234.5 becomes "1,234.50". */
    public static String money(double amount) {
        return String.format("%,.2f", amount);
    }
}

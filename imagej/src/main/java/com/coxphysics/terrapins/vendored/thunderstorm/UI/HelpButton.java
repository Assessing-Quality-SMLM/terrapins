package com.coxphysics.terrapins.vendored.thunderstorm.UI;

import com.coxphysics.terrapins.vendored.thunderstorm.JarFirstClassLoader;
import ij.IJ;
import ij.plugin.BrowserLauncher;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.EditorKit;

public class HelpButton extends JButton {

    private static JDialog window = constructFrame();
    private static JEditorPane htmlBrowser;
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 400;
    URL url;

    public HelpButton(URL helpUrl) {
        //icon licence: CC 3.0, attribution required: http://p.yusukekamiyamane.com/icons/attribution/
        super(new ImageIcon(IJ.getClassLoader().getResource("resources/help/images/question-button-icon.png")));
        setBorder(BorderFactory.createEmptyBorder());
        setBorderPainted(false);
        setIconTextGap(0);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.url = helpUrl;
        addActionListener(new HelpButtonActionListener());

    }

    private static JDialog constructFrame() {
        final JDialog frame = new JDialog();
        try {
            frame.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent e) {
                    frame.setVisible(false);
                }
            });
            if(IJ.isJava17()) {
                frame.setType(Window.Type.UTILITY);
            }
            frame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE); //for use within modal dialog
            htmlBrowser = createEditorUsingOurClassLoader();
            htmlBrowser.setBorder(BorderFactory.createEmptyBorder());
            htmlBrowser.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        try {
                            if("jar".equals(e.getURL().getProtocol())) {
                                htmlBrowser.setPage(e.getURL());
                            } else {
                                BrowserLauncher.openURL(e.getURL().toString());
                            }
                        } catch(Exception ex) {
                            IJ.handleException(ex);
                        }
                    } else if(e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                        htmlBrowser.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else if(e.getEventType() == HyperlinkEvent.EventType.EXITED) {
                        htmlBrowser.setCursor(Cursor.getDefaultCursor());
                    }
                }
            });
            JScrollPane scrollPane = new JScrollPane(htmlBrowser);
            scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            frame.getContentPane().add(scrollPane);
        } catch(Throwable e) {
            // Throwable, not Exception: the embedded HTML browser (swingbox, a third-party
            // dependency last released around 2012) can fail with an Error rather than an
            // Exception on modern JDKs -- e.g. NoClassDefFoundError from legacy JSSE classes
            // (com.sun.net.ssl.internal.ssl.Provider) it still references internally, which were
            // removed after Java 8. That must not propagate out of a static initializer: it would
            // permanently poison the HelpButton class (ExceptionInInitializerError on every
            // subsequent use) and take the whole dialog that requested a help button down with it.
            // The help viewer degrading to "unavailable" is a fair trade for that.
            htmlBrowser = null;
            IJ.handleException(e instanceof Exception ? (Exception) e : new RuntimeException(e));
        }
        return frame;
    }

    /**
     * Create a BrowserPane object using a custom classloader that prefers
     * classes in our jar
     */
    public static JEditorPane createEditorUsingOurClassLoader() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassLoader our = JarFirstClassLoader.getInstance();
        Class c = Class.forName("org.fit.cssbox.swingbox.BrowserPane", true, our);
        JEditorPane editor = (JEditorPane) c.newInstance();

        Class c2 = Class.forName("org.fit.cssbox.swingbox.SwingBoxEditorKit", true, our);
        editor.setEditorKit((EditorKit) c2.newInstance());
        return editor;
    }

    /**
     * shows the url in the static window, sizes and positions the window
     * accordingly
     */
    private void showInTextWindow() throws IOException {
        if(htmlBrowser == null) {
            // the embedded HTML browser failed to initialize (see constructFrame); nothing to show
            IJ.log("Help viewer is unavailable.");
            return;
        }
        window.setVisible(false);
        // same height as parent window of the button, positioned next to it on left or right side
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        window.setPreferredSize(new Dimension(WINDOW_WIDTH, Math.max(ancestor.getHeight(), WINDOW_HEIGHT)));
        int screenEnd = ancestor.getGraphicsConfiguration().getBounds().width + ancestor.getGraphicsConfiguration().getBounds().x;
        Point ancestorLocation = ancestor.getLocationOnScreen();
        if(ancestorLocation.x + ancestor.getWidth() + window.getPreferredSize().width < screenEnd) {
            window.setLocation(ancestorLocation.x + ancestor.getWidth(), ancestorLocation.y);
        } else {
            window.setLocation(ancestorLocation.x - window.getPreferredSize().width, ancestorLocation.y);
        }

        //set page shown in browser
        if(url != null && !url.equals(htmlBrowser.getPage())) {
            try {
                htmlBrowser.setPage(url);
            } catch(Exception e) {
                htmlBrowser.setText("Could not load help file");
            }
        }

        window.pack();
        window.setVisible(true);
    }

    class HelpButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                showInTextWindow();
            } catch(IOException ex) {
                IJ.handleException(ex);
            }
        }
    }
}

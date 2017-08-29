package org.vmol.app.visualization;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import javafx.stage.Stage;

public class JmolVisualization {
	
	private static final Logger logger = Logger.getLogger(JmolVisualization.class);
	private final Stage primaryStage;
	private JFrame jmolWindow;
	private JFrame multiJmolWindow;
	private JmolViewer jmolViewer;
	private File currentOpenFile;
	int currFile = 0;
	
	public JmolVisualization(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}
	
	public void show(File xyzFile) {
        if (jmolWindow == null) {
            logger.info("Creating Jmol window.");
            jmolWindow = new JFrame("Visualization of " + xyzFile.getName());
            jmolWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    logger.info("Jmol window closing.");
                    System.out.println("Jmol window closing");
                    jmolWindow = null;
                    jmolViewer = null;
                    setCurrentOpenFile(null);
                }
            });
            jmolWindow.setSize(600, 600);

            Container contentPane = jmolWindow.getContentPane();
            JmolPanel jmolPanel = new JmolPanel();

            // main panel -- Jmol panel on top
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.add(jmolPanel);

            // main panel -- console panel on bottom
    	    JPanel panel2 = new JPanel();
    	    panel2.setLayout(new BorderLayout());
    	    panel2.setPreferredSize(new Dimension(800, 300));
    	    AppConsole console = new AppConsole(jmolPanel.viewer, panel2,
    	        "History State Clear");
    	    
    	    // Can use a different JmolStatusListener or JmolCallbackListener interface
    	    // if you want to, but AppConsole itself should take care of any console-related callbacks
    	    jmolPanel.viewer.setJmolCallbackListener(console);
    	    panel.add("South", panel2);
            
            contentPane.add(panel);
            jmolViewer = jmolPanel.viewer;

            alignWindowPosition(jmolWindow);
            jmolWindow.setVisible(true);
            
        } else {
            logger.debug("Bringing existing Jmol window to front.");
        }
        openFile(xyzFile.getAbsoluteFile());
        jmolWindow.toFront();
    }
	
	public void showMultipleFiles(List<File> files) {
		
		if (multiJmolWindow == null) {
			multiJmolWindow = new JFrame();
	        
	        JPanel control = new JPanel();
	        control.add(new JButton(new AbstractAction("\u22b2Prev") {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	                currFile--;
	                if (currFile < 0) currFile = files.size() - 1;
	                openCurrentFile(files);
	            }
	        }));
	        control.add(new JButton(new AbstractAction("Next\u22b3") {

	            @Override
	            public void actionPerformed(ActionEvent e) {
	                currFile = (currFile+1)%files.size();
	                openCurrentFile(files);
	            }
	        }));
	        multiJmolWindow.add(control, BorderLayout.SOUTH);
	        multiJmolWindow.pack();
	        multiJmolWindow.setLocationRelativeTo(null);
	        
            multiJmolWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Jmol window closing");
                    multiJmolWindow = null;
                    jmolViewer = null;
                }
            });
            multiJmolWindow.setSize(600, 600);

            Container contentPane = multiJmolWindow.getContentPane();
            JmolPanel jmolPanel = new JmolPanel();

            // main panel -- Jmol panel on top
            JPanel panel = new JPanel();
            panel.setLayout(new CardLayout());
            panel.add(jmolPanel);

            // main panel -- console panel on bottom -- Not necessary for multiJmolWindow
//    	    JPanel panel2 = new JPanel();
//    	    panel2.setLayout(new BorderLayout());
//    	    panel2.setPreferredSize(new Dimension(800, 300));
//    	    AppConsole console = new AppConsole(jmolPanel.viewer, panel2,
//    	        "History State Clear");
    	    
    	    // Can use a different JmolStatusListener or JmolCallbackListener interface
    	    // if you want to, but AppConsole itself should take care of any console-related callbacks
//    	    jmolPanel.viewer.setJmolCallbackListener(console);
//    	    panel.add("South", panel2);
            
            contentPane.add(panel);
            jmolViewer = jmolPanel.viewer;

            alignWindowPosition(multiJmolWindow);
            multiJmolWindow.setVisible(true);
            
        } else {
            logger.debug("Bringing existing Jmol window to front.");
        }
        openCurrentFile(files);
        multiJmolWindow.toFront();
	}

    private void openCurrentFile(List<File> files) {
    	String strError = jmolViewer.openFile(files.get(currFile).getAbsolutePath());
        if (strError == null) {
            jmolViewer.scriptWait("select clear");
            jmolViewer.clearSelection();
        } else {
            System.out.println("Error while loading XYZ file. " + strError);
        }
	}

	private void alignWindowPosition(JFrame window) {
    	System.out.println(primaryStage.getX());
    	System.out.println(primaryStage.getY());
    	
        double x = primaryStage.getX() - 100;
        double y = primaryStage.getY() - 30;

        logger.info(String.format("Positioning jmol window at position x=%f y=%f", x, y));

        window.setLocation((int) x, (int) y);
    }

    public void openFile(File file) {
        if (file != null) {
            boolean isDifferentFile = getCurrentOpenFile() == null
                    || !FilenameUtils.equalsNormalized(getCurrentOpenFile().getAbsolutePath(), file.getAbsolutePath());
            if (jmolViewer != null && isDifferentFile) {
                String strError = jmolViewer.openFile(file.getAbsolutePath());
                if (strError == null) {
                    jmolViewer.scriptWait("select clear");
                    jmolViewer.clearSelection();
                    //jmolViewer.evalString(strScript);
                    setCurrentOpenFile(file);
                } else {
                    logger.error("Error while loading XYZ file. " + strError);
                }
            }
        }
    }
    
    public File getCurrentOpenFile() {
		return currentOpenFile;
	}

	public void setCurrentOpenFile(File currentOpenFile) {
		this.currentOpenFile = currentOpenFile;
	}

	static class JmolPanel extends JPanel {

        /**
		 * 
		 */
		private static final long serialVersionUID = 5769167456781069647L;

		JmolViewer viewer;

        private final Dimension currentSize = new Dimension();

        JmolPanel() {
            viewer = JmolViewer.allocateViewer(this, new SmarterJmolAdapter(),
                    null, null, null, null, null);
        }

        @Override
        public void paint(Graphics g) {
            getSize(currentSize);
            viewer.renderScreenImage(g, currentSize.width, currentSize.height);
        }
    }
}

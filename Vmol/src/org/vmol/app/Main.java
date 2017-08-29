package org.vmol.app;
	
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jmol.adapter.readers.molxyz.XyzReader;
import org.jmol.adapter.readers.quantum.QchemReader;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolViewer;
import org.jmol.util.Logger;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;
import org.vmol.app.visualization.JmolVisualization;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class Main extends Application {
	
	private static Stage primaryStage;
	private static BorderPane mainLayout;

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws IOException {
		Main.primaryStage = primaryStage;
		Main.primaryStage.setTitle("Vmol");
		showMainView();
		//showJmolViewer();
	}

	private void showMainView() throws IOException {
		mainLayout = FXMLLoader.load(getClass().getResource("MainView.fxml"));
		primaryStage.setScene(new Scene(mainLayout));
		mainLayout.setScaleShape(true);
		//primaryStage.initModality(Modality.WINDOW_MODAL);
		primaryStage.setAlwaysOnTop(false);
		//primaryStage.setHeight(300);
		//	primaryStage.setWidth(300);
		primaryStage.show();
	}
	
	private void showJmolViewer() {
		final SwingNode swingNode = new SwingNode();
		createAndSetSwingContent(swingNode, null);
		//mainLayout.getChildren().add(swingNode);
		Pane pane = new Pane();
		pane.setPrefSize(510, 700);
		pane.getChildren().add(swingNode);
//		Group group = new Group();
//        group.getChildren().add(swingNode);
//        group.setAutoSizeChildren(false);
		mainLayout.setCenter(swingNode);
		//Stage temp = new Stage();
		//temp.initModality(Modality.APPLICATION_MODAL);
		//temp.setScene(new Scene(pane));
		//temp.show();
		//mainLayout.setCenter(pane);
	}
	
    private static void createAndSetSwingContent(final SwingNode swingNode, String fileName) {
    	Platform.runLater(new Runnable() {
            @Override
            public void run() {
        	    JmolPanel jmolPanel = new JmolPanel();
        	    jmolPanel.setPreferredSize(new Dimension(800, 400));
        	    // main panel -- Jmol panel on top

        	    JPanel panel = new JPanel();
        	    panel.setLayout(new BorderLayout());
        	    panel.add("North", jmolPanel);
        	    
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
        	    
        	    mainLayout.setVisible(true);
        	    //frame.setVisible(true);
        	    String strError = null;
        	    if (fileName != null && !fileName.isEmpty())
        	    strError = jmolPanel.viewer.openFile(fileName);
        	    //jmolPanel.viewer.openStringInline(strXyzHOH);
//        	    jmolPanel.viewer.openFileAsync("benzene.xyz");
        	    if (strError != null)
        	      Logger.error(strError);

        	    panel.setFocusable(true);
                swingNode.setContent(panel);
            }
        });
    }

	  static class JmolPanel extends JPanel {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
 
//	public static void showOpenFileDialog() {
//		FileChooser fileChooser = new FileChooser();
//		fileChooser.setTitle("Open Molecule");
//        fileChooser.setInitialDirectory(
//                new File(System.getProperty("user.home"))
//            );
//		fileChooser.getExtensionFilters().addAll(
//                new FileChooser.ExtensionFilter("All Files", "*.*"),
//                new FileChooser.ExtensionFilter("XYZ", "*.xyz"),
//                new FileChooser.ExtensionFilter("PDB", "*.pdb")
//            );
//		File file = fileChooser.showOpenDialog(primaryStage);
//		if (file != null) {
////			SwingNode node = new SwingNode();
////			System.out.println("File Path : " + file.getAbsolutePath());
////			createAndSetSwingContent(node, file.getAbsolutePath());
////			mainLayout.setCenter(node);
////			JmolVisualization JmolVisualization = new JmolVisualization(primaryStage);
//			// TODO: validate an xyz file if it is in correct format
//			getJmolVisualization().show(file);
//		}
//	}
//
//	public static void showQChemInputWindow() throws IOException {
//		Parent qChemInput = FXMLLoader.load(Main.class.getResource("qchem/QChemInput.fxml"));
//		Stage stage = new Stage();
//		stage.setTitle("Q-Chem Input");
//		stage.setScene(new Scene(qChemInput));
//		stage.show();
//	}

}

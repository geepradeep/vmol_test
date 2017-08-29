package org.vmol.app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.vmol.app.fileparser.FileParserController;
import org.vmol.app.server.view.config.ServerEditConfigViewController;
import org.vmol.app.visualization.JmolVisualization;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.RadioButton;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainViewController {
	
	@FXML
	private Parent root;
	
	private static JmolVisualization jmolVisualization;
	
	@FXML
	public void openFile() throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Molecule");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
            );
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("XYZ", "*.xyz"),
                new FileChooser.ExtensionFilter("PDB", "*.pdb")
            );
		Stage currStage = (Stage) root.getScene().getWindow();
		File file = fileChooser.showOpenDialog(currStage);
		if (file != null) {
			// Check if it's an xyz or pdb file
			String fileName = file.getName();
			System.out.println(fileName);
			boolean isXyzorPDB = fileName.contains("xyz") || fileName.contains("pdb");
			if (isXyzorPDB) {
				// TODO: validate an xyz file if it is in correct format
				setJmolVisualization(new JmolVisualization(currStage));
				getJmolVisualization().show(file);	
			} else {
				openFileParserWindow(file);
			}
		}
	}
	
	private void openFileParserWindow(File file) throws IOException {
		
		FXMLLoader loader = new FXMLLoader();
    	loader.setLocation(getClass().getResource("fileparser/FileParser.fxml"));
		
		Parent fileParser = loader.load();
		
		Stage stage = new Stage();
		stage.setTitle(file.getName());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner((Stage)root.getScene().getWindow());
		stage.setScene(new Scene(fileParser));

		// Set the file into the controller
		FileParserController controller = loader.getController();
		controller.setFile(file);
		
		stage.showAndWait();
		
//		Parent fileParser = FXMLLoader.load(getClass().getResource("fileparser/FileParser.fxml"));
//		Stage stage = new Stage();
//		stage.setTitle(file.getName());
//		stage.setScene(new Scene(fileParser));
//		stage.show();
	}

	@FXML
	public void openLibEFPWindow() throws IOException {
		Parent qChemInput = FXMLLoader.load(getClass().getResource("qchem/QChemInput.fxml"));
		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Q-Chem Input");
		stage.setScene(new Scene(qChemInput));
		stage.show();
	}
	
	@FXML
	public void openServersListWindow() throws IOException {
		Parent serversList = FXMLLoader.load(getClass().getResource("server/ServersList.fxml"));
		Stage stage = new Stage();
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Servers list");
		stage.setScene(new Scene(serversList));
		stage.show();
	}

	public void testButtonClicked() {
		System.out.println("Button clicked!");
	}

	public static JmolVisualization getJmolVisualization() {
		return jmolVisualization;
	}

	public static void setJmolVisualization(JmolVisualization jmolVisualization) {
		MainViewController.jmolVisualization = jmolVisualization;
	}
	
}

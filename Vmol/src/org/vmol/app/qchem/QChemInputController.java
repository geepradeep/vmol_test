package org.vmol.app.qchem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.prefs.BackingStoreException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import org.vmol.app.MainViewController;
import org.vmol.app.server.ServerConfigController;
import org.vmol.app.server.ServerDetails;

public class QChemInputController implements Initializable{
	
	@FXML
	private Parent root;
	
    @FXML
    private TextField title;
    
    @FXML
    private ComboBox<String> calculation;
    
    private Map<String, String> calculationMap = new HashMap<String, String>() {{
    	put("Single Point Energy", "SP");
    	put("Geometry Optimization", "Opt");
    	put("Frequencies", "Freq");
    }};
    
    @FXML
    private ComboBox<String> theory;
    
    @FXML
    private ComboBox<String> basis;
    
    @FXML
    private TextField charge;
    
    @FXML
    private TextField multiplicity;
    
    @FXML
    private ComboBox<String> format;

    @FXML
    private TextArea qChemInputTextArea;
    
    @FXML
    private ComboBox<String> serversList;
    
    List<ServerDetails> serverDetailsList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Adding listener to title
		title.textProperty().addListener((observable, oldValue, newValue) -> {
		    try {
				updateQChemInputText();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// Initializing calculation ComboBox
		calculation.setItems(FXCollections.observableList(new ArrayList<>(calculationMap.keySet())));
		calculation.setValue("Geometry Optimization");
		
		// Initializing theory ComboBox
		List<String> theoryTypes = new ArrayList<String>();
		theoryTypes.add("HF");
		theoryTypes.add("MP2");
		theoryTypes.add("B3LYP");
		theoryTypes.add("B3LYP5");
		theoryTypes.add("EDF1");
		theoryTypes.add("M06-2X");
		theoryTypes.add("CCSD");
		
		theory.setItems(FXCollections.observableList(theoryTypes));
		theory.setValue("B3LYP");
		
		// Initializing basis ComboBox
		List<String> basisTypes = new ArrayList<String>();
		basisTypes.add("STO-3G");
		basisTypes.add("3-21G");
		basisTypes.add("6-31G(d)");
		basisTypes.add("6-31G(d,p)");
		basisTypes.add("6-31+G(d)");
		basisTypes.add("6-311G(d)");
		basisTypes.add("cc-pVDZ");
		basisTypes.add("LANL2DZ");
		basisTypes.add("LACVP");
		
		basis.setItems(FXCollections.observableList(basisTypes));
		basis.setValue("6-31G(d)");
		
		// TODO : Make both charge and multiplicity fields accept only Numbers 
		// Initializing Charge textField
		charge.setText("0");
		charge.textProperty().addListener((observable, oldValue, newValue) -> {
			// force the field to be numeric only
            if (!newValue.matches("-?[0-9]+")) {
                charge.setText(newValue.replaceAll("[^\\d]", ""));
            }
		    try {
				updateQChemInputText();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// Initializing Multiplicity textField
		multiplicity.setText("1");
		multiplicity.textProperty().addListener((observable, oldValue, newValue) -> {
			// force the field to be numeric only
            if (!newValue.matches("^[1-9]\\d*$")) {
                multiplicity.setText("");
            }
		    try {
				updateQChemInputText();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// Initializing Format ComboBox
		List<String> formatTypes = new ArrayList<String>();
		
		formatTypes.add("Cartesian");
//		formatTypes.add("Z-matrix");
//		formatTypes.add("Z-matrix (compact)");
		
		format.setItems(FXCollections.observableList(formatTypes));
		if (formatTypes.size() > 0) format.setValue(formatTypes.get(0));
		
		// Initializing qChemInputTextArea
		try {
			qChemInputTextArea.setText(getQChemInputText());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Initializing serversList
		serverDetailsList = new ArrayList<>();
		try {
			serverDetailsList = ServerConfigController.getServerDetailsList();
		} catch (ClassNotFoundException | BackingStoreException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> serverNames = new ArrayList<>();
		for (ServerDetails server : serverDetailsList) {
			serverNames.add(server.getServerName());
		}
		serversList.setItems(FXCollections.observableList(serverNames));
		if (serverNames.size() > 0) serversList.setValue(serverNames.get(0));
	}

	private String getQChemInputText() throws FileNotFoundException {
		StringBuilder sb = new StringBuilder();
		// Appending the rem part
		sb.append("$rem\n");
		sb.append("   JOBTYPE " + calculationMap.get(calculation.getValue()) + "\n");
		sb.append("   THEORY " + theory.getValue() + "\n");
		sb.append("   BASIS " + basis.getValue() + "\n");
		sb.append("   GUI=2\n");
		sb.append("$end\n");
		sb.append("\n");
		
		// Appending the comment part
		sb.append("$comment\n");
		sb.append(title.getText() + "\n");
		sb.append("$end\n");
		sb.append("\n");
		
		// Appending the molecule part
		sb.append("$molecule\n");
		sb.append("   " + charge.getText() + " " + multiplicity.getText() + "\n");
		// Check if any existing xyzFile is present
		
		if (MainViewController.getJmolVisualization() != null && 
				MainViewController.getJmolVisualization().getCurrentOpenFile() != null) {
			File xyzFile = MainViewController.getJmolVisualization().getCurrentOpenFile();
			Scanner scanner = new Scanner(xyzFile);
			int skip = 2; // skip first two lines of xyz file
			while (scanner.hasNextLine()) {
				if (skip == 0) {
					sb.append("   " + scanner.nextLine() + "\n");
				} else {
					scanner.nextLine();
					skip--;
				}
			}
			scanner.close();
		}
		
		sb.append("$end\n");
		return sb.toString();
	}
	
	// This method will be called on update of any of the input fields
	public void updateQChemInputText() throws FileNotFoundException {
		qChemInputTextArea.setText(getQChemInputText());
	}

	// Generate Q-Chem Input file
	public void generateQChemInputFile() {
		String qChemText = qChemInputTextArea.getText();
		FileChooser fileChooser = new FileChooser();
		
		//Set extension filter
        ExtensionFilter extFilter = new ExtensionFilter("QChem Input Deck (*.qcin)", "*.qcin");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File currentOpenFile = null ;
        
        if (MainViewController.getJmolVisualization() != null) 
        	currentOpenFile = MainViewController.getJmolVisualization().getCurrentOpenFile(); 
        
        if (currentOpenFile != null) {
        	String fileName = currentOpenFile.getName();
        	int dotIndex = fileName.indexOf('.');
        	if (dotIndex != -1) fileName = fileName.substring(0, dotIndex);
        	fileChooser.setInitialFileName(fileName);
        }
        
        //Show save file dialog
        Stage currStage = (Stage) root.getScene().getWindow();
        File file = fileChooser.showSaveDialog(currStage);
        
        if(file != null){
            saveFile(qChemText, file);
        }
	}

	private void saveFile(String content, File file) {
        try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			// Handle the exception appropriately!
			e.printStackTrace();
		}
	}
	
	// Method to handle the submit action to selected server
	public void handleSubmit() throws IOException, InterruptedException {
		ServerDetails selectedServer = serverDetailsList.get(serversList.getSelectionModel().getSelectedIndex());
		if (selectedServer.getServerType().equalsIgnoreCase("local"))
			submitJobToLocalServer(selectedServer);
		// Handle SSH case later
	}

	private void submitJobToLocalServer(ServerDetails selectedServer) throws IOException, InterruptedException {
		// Printing the complete absolute path from where the application was initialized
		System.out.println("Your working Directory is " + System.getProperty("user.dir"));
	    String path = System.getProperty("user.dir");
	    String executablePath = selectedServer.getWorkingDirectory();
	    System.out.println("Executable path is: " + executablePath);
	    String inputFileName = path + "/vmolAppJob_" + title.getText();
	    String outputFileName = path += "/vmolAppJob_" + title.getText() + "_output";

	    // First create the input File at that location with the content in qchemInputTextArea
	    boolean inputFileCreated = createInputFile(inputFileName);
	    if (!inputFileCreated) return; // Can probably return some error here
	    List<String> command = new ArrayList<String>();
	    command.add(executablePath);
	    command.add(inputFileName);
	    ProcessBuilder builder = new ProcessBuilder(command);
	    builder.redirectOutput(new File(outputFileName));

	    final Process process = builder.start();
	    
	    int errorCode = process.waitFor(); // 0 means everything went well!
	    System.out.println("Process execution completed with errorCode : " + String.valueOf(errorCode)); 
	    
	    if (outputFileName.length() != 0) {
	    	System.out.println("Printing output File contents: ");
	    	Scanner s = null;
		    try {
		    	s = new Scanner(new File(outputFileName));
		    	while (s.hasNextLine()) {
		    		System.out.println(s.nextLine());
		    	}
		    } catch (Exception e){
		    	e.printStackTrace(System.out);
		    } finally {
		    	if (s != null) s.close();
		    }
	    }
	}

	// Creates an input file at this location 
	private boolean createInputFile(String inputFileName) {
		BufferedWriter output = null;
        try {
            File file = new File(inputFileName);
            output = new BufferedWriter(new FileWriter(file));
            output.write(qChemInputTextArea.getText());
            output.close();
        } catch ( IOException e ) {
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	
}

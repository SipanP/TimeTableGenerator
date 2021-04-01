package timetablegenerator;

import javafx.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLDocumentController implements Initializable {

    @FXML
    public Label generatingPrompt;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        examHallTable.setPlaceholder(new Label("No Halls Have Been Added"));
    }

    @FXML
    public void fileChooser(ActionEvent event) {
        FileChooser fc = new FileChooser();
    }

    @FXML
    public TextField examHallName;

    @FXML
    public ListView examHallTable;

    @FXML
    public void addExamHall(ActionEvent event) {
        Boolean hallExists = false;
        List currentExamHalls = examHallTable.getItems();
        for (Object hall : currentExamHalls) {
            if (hall.toString().equals(examHallName.getText().toUpperCase())) {
                hallExists = true;
                break;
            }
        }
        if (hallExists || examHallName.getText().trim().isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Exam Hall Name");
            alert.setContentText("Exam hall already exists or input was invalid!");
            alert.showAndWait();
        } else {
            examHallTable.getItems().add(examHallName.getText().toUpperCase());
            examHallName.clear();
        }
    }

    @FXML
    public void removeExamHall(ActionEvent event) {
        final int selectedIdx = examHallTable.getSelectionModel().getSelectedIndex();
        if (selectedIdx != -1) {
            final int newSelectedIdx = (selectedIdx == examHallTable.getItems().size() - 1) ? selectedIdx - 1 : selectedIdx;

            examHallTable.getItems().remove(selectedIdx);
            examHallTable.getSelectionModel().select(newSelectedIdx);
        }
    }

    public String[] getExamHalls() {
        Object[] objects = examHallTable.getItems().toArray();
        String[] examHalls = new String[objects.length];
        System.arraycopy(objects, 0, examHalls, 0, objects.length);
        return examHalls;
    }

    @FXML
    public void selectSpreadsheet(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Spreadsheet File (*.csv)", "*.csv"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            filePath.setText(selectedFile.getPath());
        }
    }

    @FXML
    public TextField filePath;
    @FXML
    public TextField numberOfWeeks;
    @FXML
    public TextField capacity;
    @FXML
    public Slider sessions;

    @FXML
    public void generateTimetable(ActionEvent event) {
        boolean fileFound = importSpreadsheet(filePath.getText());
        boolean fieldsAreValid = areFieldsValid();
        if (fieldsAreValid && fileFound) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Timetable Generation Warning");
            alert.setHeaderText("Timetable generation may take some time!");
            alert.setContentText("Press OK to begin...");
            generatingPrompt.setText("Generating timetable... Please be patient!");
            alert.showAndWait();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("TimetableViewer.fxml"));

            Parent frontPageParent = null;
            try {
                frontPageParent = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            Scene timetableViewerScene = new Scene(frontPageParent);

            TimetableViewerController controller = loader.getController();
            
            try{
                controller.initData(
                        studentSpreadsheet,
                        Integer.parseInt(numberOfWeeks.getText()),
                        (int) sessions.getValue(),
                        getExamHalls(),
                        Integer.parseInt(capacity.getText())
                );
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(timetableViewerScene);
                window.show();
            } catch (Exception e){
                generatingPrompt.setText("");
                Alert importAlert = new Alert(AlertType.ERROR);
                importAlert.setTitle("Error");
                importAlert.setHeaderText("Invalid Input");
                importAlert.setContentText("Please ensure the student spreadsheet is valid");
                importAlert.showAndWait();
            }

            
        } else if (!fieldsAreValid) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Input");
            alert.setContentText("Please ensure the fields are valid");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid File");
            alert.setContentText("File not found!");
            alert.showAndWait();
        }
    }

    private boolean areFieldsValid() {
        boolean fieldsAreValid = true;
        if (filePath.getText().trim().isEmpty() || numberOfWeeks.getText().trim().isEmpty()
                || capacity.getText().trim().isEmpty() || examHallTable.getItems().isEmpty()
                || !numberOfWeeks.getText().matches("^0*[1-9]\\d*$") || !capacity.getText().matches("^0*[1-9]\\d*$")) {
            fieldsAreValid = false;
        }
        return fieldsAreValid;
    }

    private List<List<String>> studentSpreadsheet;

    private boolean importSpreadsheet(String filePath) {
        List<List<String>> lines = new ArrayList<>();
        Scanner inputStream;
        try {
            inputStream = new Scanner(new File(filePath));
            //skip first row
            String line = inputStream.nextLine();
            while (inputStream.hasNextLine()) {
                line = inputStream.nextLine();
                String[] values = line.split(",");
                ArrayList<String> studentRow = new ArrayList<String>(Arrays.asList(values));
                for (int i = values.length - 1; i >= 0; i--) {
                    if (studentRow.get(i).equals("")) {
                        studentRow.remove(i);
                    }
                }
                // this adds the currently parsed line to the 2-dimensional string array
                lines.add(studentRow);
            }

            inputStream.close();
            studentSpreadsheet = lines;
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    @FXML
    public void resetFields(ActionEvent event) {
        filePath.clear();
        numberOfWeeks.clear();
        capacity.clear();
        sessions.adjustValue(0);
        examHallName.clear();
        examHallTable.getItems().clear();
        System.out.println("Fields have been reset");
    }

    @FXML
    MenuBar menuBar;

    @FXML
    public void loadTimetable(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("TimetableViewer.fxml"));
        
        Parent frontPageParent = null;
        try {
            frontPageParent = loader.load();
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scene timetableViewerScene = new Scene(frontPageParent);

        TimetableViewerController controller = loader.getController();
        try {
            controller.loadTimetable();
            Stage window = (Stage) menuBar.getScene().getWindow();
            window.setScene(timetableViewerScene);
            window.show();
        } catch (Exception e) {
            System.out.println("No file selected");
        }

        

    }

    @FXML
    public javafx.scene.control.MenuItem closeButton;

    @FXML
    public void closeAction() {
        Platform.exit();
    }

}

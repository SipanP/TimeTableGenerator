package timetablegenerator;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class TimeTableGenerator extends Application {
    
    @Override
    public void start(Stage window) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
        
        Scene scene = new Scene(root);
        window.setTitle("Exam Timetable Generator");
        window.setScene(scene);
        window.show();
        
    }

    
    public static void main(String[] args) {
        launch(args);
        
    }
    
    
    
}

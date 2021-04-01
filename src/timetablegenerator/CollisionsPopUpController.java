package timetablegenerator;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CollisionsPopUpController implements Initializable {

    @FXML
    private Label totalCollisions;

    @FXML
    private TableView<CollisionInsightTableHelper> collisionRankingTable;

    @FXML
    private TableColumn<CollisionInsightTableHelper, String> studentIDColumn;

    @FXML
    private TableColumn<CollisionInsightTableHelper, Integer> collisionsColumn;

    @FXML
    public void setUp(int numberOfTotalCollisions, HashMap<String, Integer> studentNumberOfCollisions) {
        totalCollisions.setText(String.valueOf(numberOfTotalCollisions));
        //Set up collisions table
        studentIDColumn.setCellValueFactory(new PropertyValueFactory<CollisionInsightTableHelper, String>("studentID"));
        collisionsColumn.setCellValueFactory(new PropertyValueFactory<CollisionInsightTableHelper, Integer>("noOfCollisions"));
        ObservableList<CollisionInsightTableHelper> studentCollisionsList = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> c : studentNumberOfCollisions.entrySet()) {
            studentCollisionsList.add(new CollisionInsightTableHelper(c.getKey(), c.getValue()));
        }
        collisionRankingTable.setItems(studentCollisionsList);
        collisionRankingTable.getSortOrder().add(collisionsColumn);
    }
    
    @FXML
    private MenuBar menuBar;
    
    @FXML
    public void closeAction(ActionEvent event) {
        menuBar.getScene().getWindow().hide();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

}

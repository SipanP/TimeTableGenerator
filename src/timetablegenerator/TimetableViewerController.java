package timetablegenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class TimetableViewerController implements Initializable {

    private boolean isTimetableLoaded = false;
    private List<Student> loadedStudents;
    List<List<String>> studentSpreadsheet;
    int numberOfWeeks;
    public int sessions;
    String[] examHalls;
    int capacity;
    Chromosome timetable;
    TimetableGeneratorAlgorithm timetableAlgorithm;

    final double CROSSOVER_PROBABILITY = 0.1;
    final double MUTATION_PROBABILITY = 0.1;
    final int NUMBER_OF_EXAMS_PER_GROUP = 2;

    @FXML
    public ScrollPane scrollPane;

    @FXML
    public ComboBox<String> selectStudent;

    @FXML
    public TreeView collisionsTable;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    public void initData(List<List<String>> studentSpreadsheet, int numberOfWeeks, int sessions, String[] examHalls, int capacity) {
        this.studentSpreadsheet = studentSpreadsheet;
        this.numberOfWeeks = numberOfWeeks;
        this.sessions = sessions;
        this.examHalls = examHalls;
        this.capacity = capacity;
        for (int i = 0; i < studentSpreadsheet.size(); i++) {
            selectStudent.getItems().add("Student " + i);
        }
        selectStudent.getSelectionModel().selectFirst();

        this.timetableAlgorithm = new TimetableGeneratorAlgorithm(
                getNumberOfStudentGroups(), numberOfWeeks,
                examHalls, CROSSOVER_PROBABILITY,
                MUTATION_PROBABILITY, NUMBER_OF_EXAMS_PER_GROUP,
                getUniqueStudentGroups(), sessions,
                studentSpreadsheet, capacity);
        this.timetable = timetableAlgorithm.getOptimalTimetable();

        VBox vBox = new VBox();
        for (int w = 0; w < numberOfWeeks; w++) {

            // create a grid with some sample data.
            GridPane grid = new GridPane();
            Label day0 = new Label("");
            Label day1 = new Label("Monday");

            Label day2 = new Label("Tuesday");

            Label day3 = new Label("Wednesday");

            Label day4 = new Label("Thursday");

            Label day5 = new Label("Friday");
            Label[] days = {day0, day1, day2, day3, day4, day5};
            for (Label day : days) {
                day.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
            }
            grid.addRow(0, days);
            for (Label day : days) {
                GridPane.setFillWidth(day, true);
                GridPane.setFillHeight(day, true);
                day.setMaxWidth(Double.MAX_VALUE);
                day.setMaxHeight(Double.MAX_VALUE);
            }
            for (int i = 0; i < sessions; i++) {
                Label session = new Label("Session " + (i + 1));
                session.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                GridPane.setFillWidth(session, true);
                GridPane.setFillHeight(session, true);
                session.setMaxWidth(Double.MAX_VALUE);
                session.setMaxHeight(Double.MAX_VALUE);
                grid.addRow((i + 1), session);
                GridPane.setHgrow(session, Priority.ALWAYS);
                GridPane.setVgrow(session, Priority.ALWAYS);
            }

            ArrayList<Exam> examCell = new ArrayList<>();
            for (int j = 0; j < timetableAlgorithm.getOptimalTimetable().getExams().length; j++) {
                int examDay = timetableAlgorithm.getOptimalTimetable().getExams()[j].getDay() % 5;
                int week = (int) timetableAlgorithm.getOptimalTimetable().getExams()[j].getDay() / 5;
                if (week == w) {
                    examCell.add(timetableAlgorithm.getOptimalTimetable().getExams()[j]);
                    //grid.add(new Label(timetable.getOptimalTimetable().getExams()[j].getStudentGroup().getStudentGroupID()), examDay, timetable.getOptimalTimetable().getExams()[j].getSession());
                }
            }
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < this.sessions; j++) {
                    String examNames = "";
                    for (Exam ex : examCell) {
                        if (ex.getDay() % 5 == i && ex.getSession() == j) {
                            examNames += ex.getStudentGroup().getStudentGroupID() + "    " + examHalls[ex.getExamHallIndex()] + "\n";
                        }
                    }
                    Label examNamesLabel = new Label(examNames);
                    grid.add(examNamesLabel, i + 1, j + 1);
                    GridPane.setFillWidth(examNamesLabel, true);
                    GridPane.setFillHeight(examNamesLabel, true);
                    examNamesLabel.setMaxWidth(Double.MAX_VALUE);
                    examNamesLabel.setMaxHeight(Double.MAX_VALUE);
                }
            }
            // make all of the Controls and Panes inside the grid fill their grid cell, 
            // align them in the center and give them a filled background.
            // you could also place each of them in their own centered StackPane with 
            // a styled background to achieve the same effect.
            for (Node n : grid.getChildren()) {
                if (n instanceof Control) {
                    Control control = (Control) n;
                    control.setStyle("-fx-background-color: white; "
                            + "-fx-alignment: center; -fx-padding: 10 10 10 10;");
                }
                if (n instanceof Pane) {
                    Pane pane = (Pane) n;
                    pane.setStyle("-fx-background-color: white; "
                            + "-fx-alignment: center; -fx-padding: 10 10 10 10;");
                } else {
                    n.setStyle("-fx-background-color: white; "
                            + "-fx-alignment: center; -fx-padding: 10 10 10 10;");
                }
            }

            // style the grid so that it has a background and gaps around the grid and between the 
            // grid cells so that the background will show through as grid lines.
            grid.setStyle("-fx-background-color: black; "
                    + "-fx-padding: 2; "
                    + "-fx-hgap: 2; "
                    + "-fx-vgap: 2;");
            // turn layout pixel snapping off on the grid so that grid lines will be an even width.
            grid.setSnapToPixel(false);

            // set some constraints so that the grid will fill the available area.
            for (int i = 0; i < grid.getColumnConstraints().size(); i++) {
                ColumnConstraints part = new ColumnConstraints(100, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
                //ColumnConstraints column1 = new ColumnConstraints(100,100,Double.MAX_VALUE);
                //part.setHgrow(Priority.ALWAYS);
                part.setPercentWidth(100 / 6);
                part.setHalignment(HPos.CENTER);
                grid.getColumnConstraints().add(part);
            }
            for (int i = 0; i < grid.getRowConstraints().size(); i++) {
                RowConstraints rowPart = new RowConstraints();
                rowPart.setPercentHeight(100 / (sessions + 1));
                rowPart.setValignment(VPos.CENTER);
                grid.getRowConstraints().add(rowPart);
            }

            // layout the scene in a stackpane with some padding so that the grid is centered 
            // and it is easy to see the outer grid lines.
            StackPane layout = new StackPane();
            layout.setStyle("-fx-background-color: whitesmoke; -fx-padding: 10;");
            layout.getChildren().addAll(grid);

            vBox.getChildren().add(layout);
        }

        scrollPane.setContent(vBox);
        fillCollisions(this.timetable, timetableAlgorithm);
        selectStudent.setOnAction(e -> fillCollisions(this.timetable, timetableAlgorithm));

    }

    @FXML
    public void fillCollisions(Chromosome timetable, TimetableGeneratorAlgorithm timetableGenerator) {
        HashMap<String, HashMap<String, Exam[]>> collisions = timetable.getCollisions(timetableGenerator.extractStudents(studentSpreadsheet), NUMBER_OF_EXAMS_PER_GROUP);
        String studentName = selectStudent.getSelectionModel().getSelectedItem();
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        /*for(int i = 0; i < timetableGenerator.NUMBER_OF_WEEKS; i++){
            makeBranch("Week " + (i+1), root);
        }*/

        Set<Map.Entry<String, Exam[]>> daySession = collisions.get(studentName).entrySet();
        String[] daysInAWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (Map.Entry<String, Exam[]> entry : daySession) {
            String key = entry.getKey();
            int day = Integer.parseInt(key.split(" ")[0]);
            String dayName = daysInAWeek[day % 5];
            int week = day / 5;
            int session = Integer.parseInt(key.split(" ")[1]) + 1;
            String title = "Week: " + (week + 1) + "\nDay: " + dayName + "\nSession: " + session;
            TreeItem<String> titleBranch = makeBranch(title, root);
            Exam[] clashingExams = entry.getValue();
            for (Exam clashingExam : clashingExams) {
                makeBranch(clashingExam.getStudentGroup().getStudentGroupID(), titleBranch);
            }

        }
        collisionsTable.setRoot(root);
        collisionsTable.setShowRoot(false);
    }

    @FXML
    public void fillCollisionsFromLoaded(Chromosome timetable, List<Student> studentList) {
        HashMap<String, HashMap<String, Exam[]>> collisions = timetable.getCollisions(studentList, NUMBER_OF_EXAMS_PER_GROUP);
        String studentName = selectStudent.getSelectionModel().getSelectedItem();
        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);
        /*for(int i = 0; i < timetableGenerator.NUMBER_OF_WEEKS; i++){
            makeBranch("Week " + (i+1), root);
        }*/

        Set<Map.Entry<String, Exam[]>> daySession = collisions.get(studentName).entrySet();
        String[] daysInAWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (Map.Entry<String, Exam[]> entry : daySession) {
            String key = entry.getKey();
            int day = Integer.parseInt(key.split(" ")[0]);
            String dayName = daysInAWeek[day % 5];
            int week = day / 5;
            int session = Integer.parseInt(key.split(" ")[1]) + 1;
            String title = "Week: " + (week + 1) + "\nDay: " + dayName + "\nSession: " + session;
            TreeItem<String> titleBranch = makeBranch(title, root);
            Exam[] clashingExams = entry.getValue();
            for (Exam clashingExam : clashingExams) {
                makeBranch(clashingExam.getStudentGroup().getStudentGroupID(), titleBranch);
            }

        }
        collisionsTable.setRoot(root);
        collisionsTable.setShowRoot(false);
    }

    @FXML
    public void fillStudentCollisionSelector(List<Student> students) {
        for (int i = 0; i < students.size(); i++) {
            selectStudent.getItems().add("Student " + i);
        }
        selectStudent.getSelectionModel().selectFirst();
    }

    public TreeItem<String> makeBranch(String title, TreeItem<String> parent) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(true);
        parent.getChildren().add(item);
        return item;
    }

    private int getTotalCollisions() {
        int totalCollisions = 0;
        HashMap<String, HashMap<String, Exam[]>> collisions;
        if (isTimetableLoaded) {
            collisions = timetable.getCollisions(loadedStudents, NUMBER_OF_EXAMS_PER_GROUP);
        } else {
            collisions = timetable.getCollisions(timetableAlgorithm.extractStudents(studentSpreadsheet), NUMBER_OF_EXAMS_PER_GROUP);
        }
        for (HashMap<String, Exam[]> c : collisions.values()) {
            totalCollisions += c.size();
        }
        return totalCollisions;
    }

    @FXML
    public void collisionInsights(ActionEvent event) {
        System.out.println("Showing collision insights...");
        int totalCollisions = getTotalCollisions();
        HashMap<String, Integer> studentNumberOfCollisions = getStudentNumberOfCollisions();
        try {

            FXMLLoader collisionLoader = new FXMLLoader(getClass().getResource("collisionsPopUp.fxml"));
            Parent root = collisionLoader.load();
            Stage collisionsPopUpWindow = new Stage();
            collisionsPopUpWindow.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            collisionsPopUpWindow.setTitle("Collision Insights");
            CollisionsPopUpController controller = collisionLoader.getController();
            controller.setUp(totalCollisions, studentNumberOfCollisions);
            collisionsPopUpWindow.setScene(scene);
            collisionsPopUpWindow.show();
        } catch (IOException ex) {
            Logger.getLogger(TimetableViewerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int getNumberOfStudentGroups() {
        return getUniqueStudentGroups().size();
    }

    private ArrayList<String> getUniqueStudentGroups() {
        ArrayList<String> uniqueStudentGroups = new ArrayList<>();
        studentSpreadsheet.forEach((student) -> {
            student.forEach((subject) -> {
                if (!uniqueStudentGroups.contains(subject)) {
                    uniqueStudentGroups.add(subject);
                }
            });
        });
        return uniqueStudentGroups;
    }

    @FXML
    public void saveTimetable(ActionEvent event) {
        try {
            List<Student> nullChecker = timetableAlgorithm.studentSpreadsheet;
            System.out.println("Saving timetable...");
            FileChooser fileChooser = new FileChooser();
            //Set extension filter for db files
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB (*.sqlite)", "*.sqlite"));
            //Show save file dialog
            File file = fileChooser.showSaveDialog(null);

            String sql1 = "CREATE TABLE students (\n"
                    + "    id   TEXT         PRIMARY KEY,\n"
                    + "    name VARCHAR (50) \n"
                    + ");";
            String sql2 = "CREATE TABLE studentGroups (\n"
                    + "    id   TEXT         PRIMARY KEY,\n"
                    + "    name VARCHAR (50) \n"
                    + ");";
            String sql3 = "CREATE TABLE examHalls (\n"
                    + "    id   TEXT         PRIMARY KEY,\n"
                    + "    name VARCHAR (50) \n"
                    + ");";
            String sql4 = "CREATE TABLE studentGroupLink (\n"
                    + "    studentID       REFERENCES students (id),\n"
                    + "    studentGroupID  REFERENCES studentGroups (id) \n"
                    + ");";
            String sql5 = "CREATE TABLE exams (\n"
                    + "    studentGroupID         REFERENCES studentGroups (id),\n"
                    + "    examHallID             REFERENCES examHalls (id),\n"
                    + "    day            INTEGER,\n"
                    + "    session        INTEGER\n"
                    + ");";
            Connection conn = null;
            Statement cstmt = null;
            try {
                conn = this.connect(file.getAbsolutePath());
                cstmt = conn.createStatement();
                cstmt.execute(sql1);
                cstmt.execute(sql2);
                cstmt.execute(sql3);
                cstmt.execute(sql4);
                cstmt.execute(sql5);
                for (Student student : timetableAlgorithm.studentSpreadsheet) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO students(id, name) VALUES(?,?)");
                    pstmt.setString(1, student.getStudentID());
                    pstmt.setString(2, student.getStudentName());
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                for (StudentGroup sG : timetableAlgorithm.studentGroups) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO studentGroups(id, name) VALUES(?,?)");
                    pstmt.setString(1, sG.getStudentGroupID());
                    pstmt.setString(2, sG.getStudentGroupName());
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                for (int i = 0; i < timetableAlgorithm.examHalls.length; i++) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO examHalls(id, name) VALUES(?,?)");
                    pstmt.setString(1, Integer.toString(i));
                    pstmt.setString(2, timetableAlgorithm.examHalls[i]);
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                for (Student student : timetableAlgorithm.studentSpreadsheet) {
                    for (StudentGroup sG : student.getStudentGroups()) {
                        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO studentGroupLink(studentID, studentGroupID) VALUES(?,?)");
                        pstmt.setString(1, student.getStudentID());
                        pstmt.setString(2, sG.getStudentGroupID());
                        pstmt.executeUpdate();
                        pstmt.close();
                    }
                }
                for (Exam exam : timetable.getExams()) {
                    PreparedStatement pstmt = conn.prepareStatement("INSERT INTO exams(studentGroupID, examHallID, day, session) VALUES(?,?,?,?)");
                    pstmt.setString(1, exam.getStudentGroup().getStudentGroupID());
                    pstmt.setString(2, Integer.toString(exam.getExamHallIndex()));
                    pstmt.setString(3, Integer.toString(exam.getDay()));
                    pstmt.setString(4, Integer.toString(exam.getSession()));
                    pstmt.executeUpdate();
                    pstmt.close();
                }
                fileSavedAlert(file.getPath());
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            } finally {
                if (cstmt != null) {
                    try {
                        cstmt.close();
                    } catch (SQLException e) {
                        e.getMessage();
                    }
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.getMessage();
                }
            }
        } catch (NullPointerException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Saving Error");
            alert.setHeaderText("Database already exists");
            alert.setContentText("The databse you are trying to create already exists");
            alert.showAndWait();
        }
    }

    private Connection connect(String timetablePath) {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:" + timetablePath;
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite DB has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    @FXML
    public void generateNewTimetable(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(TimetableViewerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Scene newTimetableScene = new Scene(loader.getRoot());
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(newTimetableScene);
        window.show();
    }

    @FXML
    public void loadTimetableOption(){
        try{
            loadTimetable();
        } catch(Exception e){
            System.out.println("No file selected");
        }
    }
    
    
    @FXML
    public void loadTimetable() throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB (*.sqlite)", "*.sqlite"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Connection conn = this.connect(selectedFile.getAbsolutePath());
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id, name FROM students");
                // loop through the result set
                List<Student> students = new ArrayList<>();
                while (rs.next()) {
                    students.add(new Student(rs.getString("id"), rs.getString("name")));
                }
                ResultSet rs1 = stmt.executeQuery("SELECT id, name FROM studentGroups");
                // loop through the result set
                List<StudentGroup> studentGroupsList = new ArrayList<>();
                while (rs1.next()) {
                    studentGroupsList.add(new StudentGroup(rs1.getString("id"), rs1.getString("name")));
                }

                String studentGroupQuery = "SELECT studentGroupID FROM studentGroupLink WHERE studentID = ?";
                PreparedStatement pstmt = conn.prepareStatement(studentGroupQuery);
                for (Student student : students) {
                    pstmt.setString(1, student.getStudentID());
                    ResultSet rs2 = pstmt.executeQuery();
                    while (rs2.next()) {
                        String studentGroupID = rs2.getString("studentGroupID");
                        for (StudentGroup sG : studentGroupsList) {
                            if (sG.getStudentGroupID().equals(studentGroupID)) {
                                student.addStudentGroup(sG);
                            }
                        }
                    }
                }
                String studentQuery = "SELECT studentID FROM studentGroupLink WHERE studentGroupID = ?";
                PreparedStatement pstmt1 = conn.prepareStatement(studentQuery);
                for (StudentGroup studentGroup : studentGroupsList) {
                    pstmt1.setString(1, studentGroup.getStudentGroupID());
                    ResultSet rs3 = pstmt1.executeQuery();
                    while (rs3.next()) {
                        String studentID = rs3.getString("studentID");
                        studentGroup.addStudent(studentID);
                    }
                }
                List<String> examHallsList = new ArrayList<>();
                ResultSet rs4 = stmt.executeQuery("SELECT name FROM examHalls");
                while (rs4.next()) {
                    examHallsList.add(rs4.getString("name"));
                }
                String[] examHalls = new String[examHallsList.size()];
                for (int i = 0; i < examHallsList.size(); i++) {
                    examHalls[i] = examHallsList.get(i).toString();
                }
                this.examHalls = examHalls;
                ResultSet rs5 = stmt.executeQuery("SELECT studentGroupID, examHallID, day, session FROM exams");
                List<Exam> exams = new ArrayList<>();
                while (rs5.next()) {
                    for (StudentGroup studentGroup : studentGroupsList) {
                        String studentGroupID = rs5.getString("studentGroupID");
                        if (studentGroupID.equals(studentGroup.getStudentGroupID())) {
                            exams.add(new Exam(studentGroup, rs5.getInt("examHallID"), rs5.getInt("day"), rs5.getInt("session")));
                            break;
                        }
                    }
                }
                Exam[] examArray = new Exam[exams.size()];
                for (int i = 0; i < exams.size(); i++) {
                    examArray[i] = exams.get(i);
                }
                Chromosome loadedTimetable = new Chromosome(examArray);
                this.timetable = loadedTimetable;
                conn.close();

                VBox vBox = new VBox();
                int maxDay = 0;
                for (Exam exam : exams) {
                    if (exam.getDay() > maxDay) {
                        maxDay = exam.getDay();
                    }
                }

                int loadedNumberOfWeeks = (int) Math.ceil((double) maxDay / 5);
                this.numberOfWeeks = loadedNumberOfWeeks;
                int maxSession = 0;
                for (Exam exam : exams) {
                    if (exam.getSession() > maxSession) {
                        maxSession = exam.getSession();
                    }
                }
                maxSession++;
                this.sessions = maxSession;
                for (int w = 0; w < loadedNumberOfWeeks; w++) {

                    // create a grid with some sample data.
                    GridPane grid = new GridPane();
                    Label day0 = new Label("");
                    Label day1 = new Label("Monday");

                    Label day2 = new Label("Tuesday");

                    Label day3 = new Label("Wednesday");

                    Label day4 = new Label("Thursday");

                    Label day5 = new Label("Friday");
                    Label[] days = {day0, day1, day2, day3, day4, day5};
                    for (Label day : days) {
                        day.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                    }
                    grid.addRow(0, days);
                    for (Label day : days) {
                        GridPane.setFillWidth(day, true);
                        GridPane.setFillHeight(day, true);
                        day.setMaxWidth(Double.MAX_VALUE);
                        day.setMaxHeight(Double.MAX_VALUE);
                    }
                    for (int i = 0; i < maxSession; i++) {
                        Label session = new Label("Session " + (i + 1));
                        session.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
                        GridPane.setFillWidth(session, true);
                        GridPane.setFillHeight(session, true);
                        session.setMaxWidth(Double.MAX_VALUE);
                        session.setMaxHeight(Double.MAX_VALUE);
                        grid.addRow((i + 1), session);
                        GridPane.setHgrow(session, Priority.ALWAYS);
                        GridPane.setVgrow(session, Priority.ALWAYS);
                    }

                    ArrayList<Exam> examCell = new ArrayList<>();
                    for (int j = 0; j < loadedTimetable.getExams().length; j++) {
                        int examDay = loadedTimetable.getExams()[j].getDay() % 5;
                        int week = (int) loadedTimetable.getExams()[j].getDay() / 5;
                        if (week == w) {
                            examCell.add(loadedTimetable.getExams()[j]);
                        }
                    }
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < maxSession; j++) {
                            String examNames = "";
                            for (Exam ex : examCell) {
                                if (ex.getDay() % 5 == i && ex.getSession() == j) {
                                    examNames += ex.getStudentGroup().getStudentGroupID() + "    " + examHalls[ex.getExamHallIndex()] + "\n";
                                }
                            }
                            Label examNamesLabel = new Label(examNames);
                            grid.add(examNamesLabel, i + 1, j + 1);
                            GridPane.setFillWidth(examNamesLabel, true);
                            GridPane.setFillHeight(examNamesLabel, true);
                            examNamesLabel.setMaxWidth(Double.MAX_VALUE);
                            examNamesLabel.setMaxHeight(Double.MAX_VALUE);
                        }
                    }
                    for (Node n : grid.getChildren()) {
                        if (n instanceof Control) {
                            Control control = (Control) n;
                            control.setStyle("-fx-background-color: white; "
                                    + "-fx-alignment: center; -fx-padding: 10 10 10 10;");
                        }
                        if (n instanceof Pane) {
                            Pane pane = (Pane) n;
                            pane.setStyle("-fx-background-color: white; "
                                    + "-fx-alignment: center; -fx-padding: 10 10 10 10;");
                        } else {
                            n.setStyle("-fx-background-color: white; "
                                    + "-fx-alignment: center; -fx-padding: 10 10 10 10;");
                        }
                    }
                    grid.setStyle("-fx-background-color: black; "
                            + "-fx-padding: 2; "
                            + "-fx-hgap: 2; "
                            + "-fx-vgap: 2;");
                    grid.setSnapToPixel(false);

                    for (int i = 0; i < grid.getColumnConstraints().size(); i++) {
                        ColumnConstraints part = new ColumnConstraints(100, Control.USE_COMPUTED_SIZE, Double.MAX_VALUE);
                        part.setPercentWidth(100 / 6);
                        part.setHalignment(HPos.CENTER);
                        grid.getColumnConstraints().add(part);
                    }
                    for (int i = 0; i < grid.getRowConstraints().size(); i++) {
                        RowConstraints rowPart = new RowConstraints();
                        rowPart.setPercentHeight(100 / (maxSession + 1));
                        rowPart.setValignment(VPos.CENTER);
                        grid.getRowConstraints().add(rowPart);
                    }
                    StackPane layout = new StackPane();
                    layout.setStyle("-fx-background-color: whitesmoke; -fx-padding: 10;");
                    layout.getChildren().addAll(grid);

                    vBox.getChildren().add(layout);
                }
                isTimetableLoaded = true;
                scrollPane.setContent(vBox);
                fillStudentCollisionSelector(students);
                fillCollisionsFromLoaded(loadedTimetable, students);
                this.loadedStudents = students;
                this.timetable = loadedTimetable;
                selectStudent.setOnAction(e -> fillCollisionsFromLoaded(loadedTimetable, students));

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

        } else {
            throw new FileNotFoundException();
        }
    }

    @FXML
    public void exportToDocument() throws FileNotFoundException, IOException {
        try {
            FileChooser fileChooser = new FileChooser();
            //Set extension filter for db files
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document (*.docx)", "*.docx"));
            //Show save file dialog
            File file = fileChooser.showSaveDialog(null);
            //Blank Document
            XWPFDocument document = new XWPFDocument();

            //Write the Document in file system
            FileOutputStream out = new FileOutputStream(file);

            XWPFParagraph paragraph = document.createParagraph();
            String schoolLogo = "src/timetablegenerator/Twyford Logo.png";
            FileInputStream schoolLogoImage = new FileInputStream(schoolLogo);
            //XWPFParagraph schoolLogoParagraph = document.createParagraph();
            XWPFRun schoolLogoRun = paragraph.createRun();
            schoolLogoRun.addPicture(schoolLogoImage, XWPFDocument.PICTURE_TYPE_PNG, schoolLogo, Units.toEMU(30), Units.toEMU(30)); // 200x200 pixels
            schoolLogoImage.close();
            //create Paragraph

            XWPFRun paragraphOneRunOne = paragraph.createRun();
            paragraphOneRunOne.setBold(true);
            paragraphOneRunOne.setFontSize(25);
            paragraphOneRunOne.setText("Year XX");

            XWPFRun paragraphOneRunTwo = paragraph.createRun();
            paragraphOneRunTwo.setBold(true);
            paragraphOneRunTwo.addBreak();
            paragraphOneRunTwo.setText("Quarter X Exam Week timetable");
            paragraphOneRunTwo.addBreak();

            XWPFRun paragraphOneRunThree = paragraph.createRun();
            paragraphOneRunThree.setBold(true);
            paragraphOneRunThree.setText("Week beginning DD/MM/YYYY");
            paragraphOneRunThree.addBreak();
            paragraphOneRunThree.addBreak();
            paragraphOneRunThree.addBreak();

            for (int i = 0; i < numberOfWeeks; i++) {
                XWPFTable table = document.createTable();
                table.getCTTbl().addNewTblPr().addNewTblW().setW(BigInteger.valueOf(10000));
                //create first row
                XWPFTableRow tableRowOne = table.getRow(0);
                tableRowOne.getCell(0).setText("");
                ArrayList<String> daysInAWeek = new ArrayList<String>();
                daysInAWeek.add("Monday");
                daysInAWeek.add("Tuesday");
                daysInAWeek.add("Wednesday");
                daysInAWeek.add("Thursday");
                daysInAWeek.add("Friday");
                for (int d = 0; d < daysInAWeek.size(); d++) {
                    tableRowOne.addNewTableCell();
                    XWPFParagraph para = table.getRow(0).getCell(d + 1).getParagraphs().get(0);
                    XWPFRun run = para.createRun();
                    run.setBold(true);
                    run.setFontSize(13);
                    run.setText(daysInAWeek.get(d));

                }

                for (int j = 0; j < sessions; j++) {
                    XWPFTableRow sessionRow = table.createRow();
                    XWPFParagraph para = sessionRow.getCell(0).getParagraphs().get(0);
                    XWPFRun run = para.createRun();
                    run.setBold(true);
                    run.setFontSize(13);
                    run.setText("Session " + (j + 1));

                }
                for (Exam exam : timetable.getExams()) {
                    if (exam.getDay() / 5 == i) {
                        ArrayList<String> cellContent = new ArrayList<String>();
                        cellContent.add(exam.getStudentGroup().getStudentGroupID());
                        cellContent.add(" - ");
                        cellContent.add(examHalls[exam.getExamHallIndex()]);
                        XWPFParagraph para = table.getRow(exam.getSession() + 1).getCell((exam.getDay() % 5) + 1).getParagraphs().get(0);
                        for (String text : cellContent) {
                            XWPFRun run = para.createRun();
                            run.setText(text);
                            if (cellContent.get(cellContent.size() - 1) == text) {
                                run.addBreak();
                            }
                        }
                    }
                }
                XWPFRun breakAfterTable = document.createParagraph().createRun();
                breakAfterTable.addBreak();
            }
            document.write(out);
            out.close();
            fileSavedAlert(file.getPath());
        } catch (InvalidFormatException ex) {
            Logger.getLogger(TimetableViewerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void fileSavedAlert(String filePath) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("File has been saved");
        alert.setHeaderText(null);
        alert.setContentText("The file has been saved at path\n" + filePath);
        alert.showAndWait();
    }

    @FXML
    public void closeAction() {
        Platform.exit();
    }

    private HashMap<String, Integer> getStudentNumberOfCollisions() {
        HashMap<String, Integer> studentNumberOfCollisions = new HashMap<String, Integer>();
        HashMap<String, HashMap<String, Exam[]>> collisions;

        if (isTimetableLoaded) {
            collisions = timetable.getCollisions(loadedStudents, NUMBER_OF_EXAMS_PER_GROUP);
        } else {
            collisions = timetable.getCollisions(timetableAlgorithm.extractStudents(studentSpreadsheet), NUMBER_OF_EXAMS_PER_GROUP);
        }
        for (Map.Entry<String, HashMap<String, Exam[]>> c : collisions.entrySet()) {
            studentNumberOfCollisions.put(c.getKey(), c.getValue().size());
        }
        return studentNumberOfCollisions;
    }
}

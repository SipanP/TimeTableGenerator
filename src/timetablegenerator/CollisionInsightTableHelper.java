package timetablegenerator;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CollisionInsightTableHelper {

    private SimpleStringProperty studentID;
    private SimpleIntegerProperty noOfCollisions;

    public CollisionInsightTableHelper(String studentID, Integer noOfCollisions) {
        this.studentID = new SimpleStringProperty(studentID);
        this.noOfCollisions = new SimpleIntegerProperty(noOfCollisions);
    }

    public String getStudentID() {
        return studentID.get();
    }

    public void setStudentID(SimpleStringProperty studentID) {
        this.studentID = studentID;
    }

    public int getNoOfCollisions() {
        return noOfCollisions.get();
    }

    public void setNoOfCollisions(SimpleIntegerProperty noOfCollisions) {
        this.noOfCollisions = noOfCollisions;
    }

}

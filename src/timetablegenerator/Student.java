package timetablegenerator;

import java.util.ArrayList;
import java.util.List;

public class Student {

    private String studentID;
    private String studentName;
    private List<StudentGroup> studentGroups;

    public Student(String studentID, String studentName, List<StudentGroup> studentGroups) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.studentGroups = studentGroups;
    }

    public Student(String studentID, List<StudentGroup> studentGroups) {
        this.studentID = studentID;
        this.studentGroups = studentGroups;
    }

    public Student(String studentID) {
        this.studentID = studentID;
        this.studentGroups = new ArrayList<>();
    }

    public Student(String studentID, String studentName) {
        this.studentID = studentID;
        this.studentName = studentName;
        this.studentGroups = new ArrayList<>();
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public List<StudentGroup> getStudentGroups() {
        return studentGroups;
    }

    public void setStudentGroups(List<StudentGroup> studentGroups) {
        this.studentGroups = studentGroups;
    }

    public void addStudentGroup(StudentGroup studentGroup) {
        this.studentGroups.add(studentGroup);
    }

}

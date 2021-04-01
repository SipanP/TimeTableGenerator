package timetablegenerator;

import java.util.ArrayList;

public class StudentGroup {

    private String studentGroupID;
    private String studentGroupName;
    private ArrayList<String> studentsID = new ArrayList<>();

    ;

    public StudentGroup(String studentGroupID, String studentGroupName) {
        this.studentGroupID = studentGroupID;
        this.studentGroupName = studentGroupName;
    }

    public StudentGroup(String studentGroupID) {
        this.studentGroupID = studentGroupID;
    }

    public ArrayList<String> getStudents() {
        return studentsID;
    }

    public void setStudents(ArrayList<Student> students) {
        //System.out.println(students.get(0).getStudentID());
        for (Student s : students) {
            this.studentsID.add(s.getStudentID());
        }
    }

    public void addStudent(String studentID) {
        this.studentsID.add(studentID);
    }

    public String getStudentGroupID() {
        return studentGroupID;
    }

    public String getStudentGroupName() {
        return studentGroupName;
    }

}

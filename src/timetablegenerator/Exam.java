package timetablegenerator;

public class Exam {

    private StudentGroup studentGroup;
    private int examHallIndex;
    private int day;
    private int session;

    public Exam(StudentGroup studentGroup) {
        this.studentGroup = studentGroup;
    }

    public Exam(StudentGroup studentGroup, int examHallIndex, int day, int session) {
        this.studentGroup = studentGroup;
        this.examHallIndex = examHallIndex;
        this.day = day;
        this.session = session;
    }

    public StudentGroup getStudentGroup() {
        return studentGroup;
    }
    //Shouldn't be able to change the student group of the exam once it has been instantiated

    public int getExamHallIndex() {
        return examHallIndex;
    }

    public void setExamHallIndex(int examHallIndex) {
        this.examHallIndex = examHallIndex;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getSession() {
        return session;
    }

    public void setSession(int session) {
        this.session = session;
    }

}

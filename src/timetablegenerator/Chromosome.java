package timetablegenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class Chromosome {

    private Exam[] exams;
    private double fitness;
    private double capacity;
    private HashMap<String, HashMap<String, Exam[]>> collisions = new HashMap<String, HashMap<String, Exam[]>>();

    public Chromosome(Exam[] exams) {
        this.exams = exams;
    }

    public Exam[] getExams() {
        return exams;
    }

    public void setExams(Exam[] exams) {
        this.exams = exams;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getFitness(List<Student> students, int NUMBER_OF_WEEKS, String[] examHalls, int sessions) {
        calculatefitness(students, NUMBER_OF_WEEKS, examHalls, sessions);
        return this.fitness;
    }

    public HashMap<String, HashMap<String, Exam[]>> getCollisions(List<Student> students, int NUMBER_OF_EXAMS_PER_GROUP) {
        //A nested hashmap:
        //The key in the first layer is the student ID
        //The key in the nested hashmap (second layer) is the day and session
        //The value in the nested hashmap is the exams that have collided
        HashMap<String, HashMap<String, Exam[]>> collisions = new HashMap<>();
        ArrayList<Integer[]> times = new ArrayList<>();
        for (Student student : students) {
            ArrayList<String> sgID = new ArrayList<>();
            for (StudentGroup sG : student.getStudentGroups()) {
                sgID.add(sG.getStudentGroupID());
            }
            ArrayList<Exam> specificExams = new ArrayList<>();
            for (int i = 0; i < (student.getStudentGroups().size() * NUMBER_OF_EXAMS_PER_GROUP); i++) {
                for (Exam exam : exams) {
                    if (sgID.contains(exam.getStudentGroup().getStudentGroupID())) {
                        specificExams.add(exam);
                    }
                }
            }
            HashMap<String, Exam[]> clashes = new HashMap<String, Exam[]>();
            for (Exam specificExam : specificExams) {
                int day = specificExam.getDay();
                int session = specificExam.getSession();
                String specificTime = day + " " + session;

                for (int i = 0; i < specificExams.size(); i++) {
                    if (specificExam.getDay() == specificExams.get(i).getDay() && specificExam.getSession() == specificExams.get(i).getSession() && specificExam != specificExams.get(i)) {
                        Exam[] clashedExams = {specificExam, specificExams.get(i)};
                        clashes.put(specificTime, clashedExams);
                    }
                }
            }
            collisions.put(student.getStudentID(), clashes);
        }
        this.collisions = collisions;
        return collisions;
    }

    public void calculatefitness(List<Student> students, int numberOfWeeks, String[] examHalls, int sessions) {
        fitness = 0;
        int[][][] examHallStudentAmount = new int[numberOfWeeks*5][sessions][examHalls.length];
        //Add up all the number of students in each exam hall by iterating through each exam
        for (int i = 0; i < this.exams.length; i++) {
            examHallStudentAmount[this.exams[i].getDay()][this.exams[i].getSession()][this.exams[i].getExamHallIndex()] += this.exams[i].getStudentGroup().getStudents().size();
        }
        //For each exam hall, at a specific session, if the number of students in the exam hall is greater than the capacity
        //increase the fitness by 100 times the difference in the number of students and the capacity of the hall
        for (int[][] studentAmount : examHallStudentAmount) {
            for (int[] specificHallAmountDay : studentAmount) {
                for (int specificHallAmountSession : specificHallAmountDay) {
                    if (specificHallAmountSession > capacity) {
                        fitness += (specificHallAmountSession - capacity) * 100;
                    }
                }
            }
        }
        //Iterate through each student
        for (Student student : students) {
            ArrayList<ArrayList<Integer>> examDates = new ArrayList<ArrayList<Integer>>();
            List<StudentGroup> studentGroups = student.getStudentGroups();
            for (StudentGroup sG : studentGroups) {
                for (Exam exam : exams) {
                    if (exam.getStudentGroup().getStudentGroupID().equals(sG.getStudentGroupID())) {
                        examDates.add(new ArrayList<Integer>(Arrays.asList(exam.getDay(), exam.getSession())));
                    }
                }
            }
            //Sort all the exam dates increasing by day
            Collections.sort(examDates, new Comparator<ArrayList<Integer>>() {
                @Override
                public int compare(ArrayList<Integer> o1, ArrayList<Integer> o2) {
                    return o1.get(0).compareTo(o2.get(0));
                }
            });
            for (int n = 0; n < examDates.size() - 1; n++) {
                //If two adjacent exams are on the same day
                if (examDates.get(n).get(0) == examDates.get(n + 1).get(0)) {
                    fitness += 50;
                    //If two adjacent exams are on the same day and session
                    if (examDates.get(n).get(1) == examDates.get(n + 1).get(1)) {
                        fitness += 50;
                    }
                } else {
                    //If adjacent exams are not on the same day,
                    //find the number of days between the two adjacent exams and divide the total number of days
                    //by the number of days between the adjacent exams
                    //add this to the fitness
                    fitness += ((numberOfWeeks * 5) / (examDates.get(n + 1).get(0) - examDates.get(n).get(0)));
                }
            }
        }
        this.fitness = normaliseFitness(fitness);
    }

    private double normaliseFitness(double fitness) {
        return 1 / fitness;
    }

}

package timetablegenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TimetableGeneratorAlgorithm {
    Chromosome[] population;
    int[] fitnessValues;
    String[] examHalls;
    Chromosome optimalTimetable;
    final int POPULATION_SIZE = 120;
    final double CROSSOVER_PROBABILITY;
    final double MUTATION_PROBABILITY;
    final int MAX_NUMBER_OF_GENERATIONS = 100;
    final int NUMBER_OF_EXAMS_PER_GROUP;
    final int NUMBER_OF_WEEKS;
    final int NUMBER_OF_STUDENT_GROUPS;
    final int SESSIONS;
    StudentGroup[] studentGroups;
    double[][] populationFitness;
    final int capacity;
    List<Student> studentSpreadsheet;
    
    Random rand = new Random();

    public TimetableGeneratorAlgorithm(int numberOfStudentGroups, int numberOfWeeks, 
            String[] examHalls, double CROSSOVER_PROBABILITY, 
            double MUTATION_PROBABILITY, int NUMBER_OF_EXAMS_PER_GROUP, 
            ArrayList<String> uniqueStudentGroups, int sessions,
            List<List<String>> studentSpreadsheet, int capacity) {
        this.NUMBER_OF_EXAMS_PER_GROUP = NUMBER_OF_EXAMS_PER_GROUP;
        population = new Chromosome[POPULATION_SIZE];
        this.NUMBER_OF_WEEKS = numberOfWeeks;
        this.examHalls = examHalls;
        this.CROSSOVER_PROBABILITY = CROSSOVER_PROBABILITY;
        this.MUTATION_PROBABILITY = MUTATION_PROBABILITY;
        this.NUMBER_OF_STUDENT_GROUPS = numberOfStudentGroups;
        this.SESSIONS = sessions;
        this.capacity = capacity;
        
        studentGroups = new StudentGroup[NUMBER_OF_STUDENT_GROUPS];
        String[] uniqueStudentGroupsArray = Arrays.copyOf(uniqueStudentGroups.toArray(), uniqueStudentGroups.toArray().length, String[].class);
        for(int i = 0; i < NUMBER_OF_STUDENT_GROUPS; i++){
            this.studentGroups[i] = new StudentGroup(uniqueStudentGroupsArray[i]);
        }
        this.studentSpreadsheet = extractStudents(studentSpreadsheet);
        setStudentsToStudentGroups();
        int generation = 1;
        population = initialisePopulation();
        populationFitness = getPopulationFitness();
        sortPopulation(populationFitness, population);
        setOptimalTimetable(population);
        while(/*optimalTimetable.getFitness(this.studentSpreadsheet, NUMBER_OF_WEEKS, examHalls, this.SESSIONS) < 0.0005 &&*/ MAX_NUMBER_OF_GENERATIONS >= generation){
            generation++;
            Chromosome[] newPopulation = new Chromosome[POPULATION_SIZE];
            int count = 0;
            for(int i = 0; i < POPULATION_SIZE; i++){
                if(Math.random() < CROSSOVER_PROBABILITY){
                    newPopulation[count] = crossover(selectParent(), selectParent());
                    count++;
                }
            }
            populationFitness = getPopulationFitness();
            population = sortPopulation(populationFitness, population);
            for(int i = count; i < POPULATION_SIZE; i++){
                newPopulation[i] = population[(int)populationFitness[i-count][0]];
            }
            
            population = newPopulation;
            populationFitness = getPopulationFitness();
            population = sortPopulation(populationFitness, population);
            
            setOptimalTimetable(population);
            System.out.println("Generation: " + generation);
            System.out.println("Fitness: " + optimalTimetable.getFitness(this.studentSpreadsheet, NUMBER_OF_WEEKS, this.examHalls, this.SESSIONS));
        }
    }

    public void setOptimalTimetable(Chromosome[] Population) {
        this.optimalTimetable = Population[0];
    }

    public Chromosome getOptimalTimetable() {
        return optimalTimetable;
    }
    
    
    public List<Student> extractStudents(List<List<String>> studentSpreadsheet){
        
        List<Student> students = new ArrayList<>();
        for(int j = 0; j < studentSpreadsheet.size(); j++){
            Student student = new Student("Student " + j);
            for(int k = 0; k < studentSpreadsheet.get(j).size(); k++){
                for(StudentGroup sG : studentGroups){
                    if(sG.getStudentGroupID().equals(studentSpreadsheet.get(j).get(k))){
                        student.addStudentGroup(sG);
                    }
                }
            }
            students.add(student);
        }
        return students;
    }
    
    
    public void setStudentsToStudentGroups(){
        for(int i=0; i < NUMBER_OF_STUDENT_GROUPS; i++){
            ArrayList<Student> students = new ArrayList<>();
            for(int j =0; j < studentSpreadsheet.size(); j++){
                if(studentSpreadsheet.get(j).getStudentGroups().contains(studentGroups[i])){
                    students.add(studentSpreadsheet.get(j));
                }
            }
            studentGroups[i].setStudents(students);
        }
    }
    
    public Chromosome[] initialisePopulation(){
        Chromosome[] generation0 = new Chromosome[POPULATION_SIZE];

        for (int i = 0; i < generation0.length; i++) {

            Exam[] exams = new Exam[NUMBER_OF_STUDENT_GROUPS * NUMBER_OF_EXAMS_PER_GROUP];
            int count = 0;
            while (count < NUMBER_OF_EXAMS_PER_GROUP) {
                for (int n = count * NUMBER_OF_STUDENT_GROUPS; n < (count + 1) * NUMBER_OF_STUDENT_GROUPS; n++) {
                    exams[n] = new Exam(studentGroups[n % (NUMBER_OF_STUDENT_GROUPS)]);
                }
                count++;
            }

            for (int j = 0; j < exams.length; j++) {
                exams[j].setDay(rand.nextInt(NUMBER_OF_WEEKS * 5));
                if (examHalls.length == 1) {
                    exams[j].setExamHallIndex(0);
                } else {
                    exams[j].setExamHallIndex(rand.nextInt(examHalls.length));
                }
                if(this.SESSIONS == 1){
                    exams[j].setSession(0);
                } else{
                    exams[j].setSession(rand.nextInt(this.SESSIONS));
                }
            }
            generation0[i] = new Chromosome(exams);
        }
        return generation0;
    }
    
    public double[][] getPopulationFitness(){
        double[][] popFitness = new double[POPULATION_SIZE][2];
        for(int i = 0; i < POPULATION_SIZE; i++){
            popFitness[i][0] = i;
            population[i].setCapacity(capacity);
            //System.out.println(population[i].getExams()[5].getDay());
            popFitness[i][1] = population[i].getFitness(studentSpreadsheet, NUMBER_OF_WEEKS, this.examHalls, this.SESSIONS);
        }
        return popFitness;
    }
    
    public Chromosome[] sortPopulation(double[][] populationFitness, Chromosome[] population){
        Chromosome[] sortedPop = new Chromosome[POPULATION_SIZE];
        MergeSortBySecond sortFitness = new MergeSortBySecond();
        sortFitness.sort(populationFitness, 0, populationFitness.length - 1);
        for(int i = 0; i < POPULATION_SIZE; i++){
            sortedPop[i] = population[(int) populationFitness[i][0]];
        }
        return sortedPop;
    }
    
    
   
    
    public Chromosome crossover(Chromosome parent1, Chromosome parent2){
        Chromosome p1 = parent1;
        Chromosome p2 = parent2;
        /*if(rand.nextDouble() < MUTATION_PROBABILITY){
            p1 = mutate(new Chromosome(parent1.getExams()));
        }
        if(rand.nextDouble() < MUTATION_PROBABILITY){
            p2 = mutate(new Chromosome(parent2.getExams()));
        }*/
        int cutIndex = rand.nextInt(p1.getExams().length);
        Exam[] childExams = new Exam[p1.getExams().length];
        for(int i = 0; i < cutIndex; i++){
            childExams[i] = p1.getExams()[i];
        }
        for(int i = cutIndex; i < childExams.length; i++){
            childExams[i] = p2.getExams()[i];
        }
        return new Chromosome(childExams);
        
    }
    
    private Chromosome mutate(Chromosome parent){
        Chromosome mutatedParent = parent;
        for(int i = 0; i < 3; i++){
            int indexToChange = rand.nextInt(mutatedParent.getExams().length);
            mutatedParent.getExams()[indexToChange].setDay(rand.nextInt(NUMBER_OF_WEEKS*5));
            mutatedParent.getExams()[indexToChange].setSession(rand.nextInt(SESSIONS));
            mutatedParent.getExams()[indexToChange].setExamHallIndex(rand.nextInt(examHalls.length));
            
        }
        return mutatedParent;
    }
    
    
    public Chromosome selectParent(){
        double overallFitness = 0;
        for (double fitness[]: populationFitness) {
            overallFitness += fitness[1];
        }
        double n = overallFitness * rand.nextDouble();
        double runningTotal = 0;
        int chromosome = 0;
        for(int i = 0; i < populationFitness.length; i++){
            runningTotal += populationFitness[i][1];
            if(runningTotal > n){
                chromosome = i;
                break;
            }
        }
        return population[chromosome];
    }
    
    /* public Chromosome generateTimetable(){
        return population;
    }*/
    
}

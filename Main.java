import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Advanced Java CLI Student Grade Calculator.
 * Parses input datasets via CSV, runs academic grading algorithms,
 * handles bad row data tokens defensively, and outputs clean analytics files.
 * 
 * @author Victor Amshima
 */
public class Main {
    public static void main(String[] args) {
        String inputPath = "students.csv";
        String outputPath = "student_report.txt";

        System.out.println("🔄 Initializing Grade Processing Engine...");

        try {
            // 1. Read files data
            List<Student> students = FileManager.readStudentsFromCsv(inputPath);
            
            if (students.isEmpty()) {
                System.out.println("⚠️ Process halted: The input CSV file contains no valid student records.");
                return;
            }

            // 2. Compute grades sequentially
            for (Student student : students) {
                GradeCalculator.processStudentGrades(student);
            }

            // 3. Output results to the terminal console
            System.out.println("\n==========================================");
            System.out.println("        STUDENT GRADE REPORT (CONSOLE)");
            System.out.println("==========================================");
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                System.out.printf("Name: %s\n", s.getName());
                System.out.printf("Average: %.2f\n", s.getAverage());
                System.out.printf("Grade: %c\n", s.getLetterGrade());
                System.out.printf("Status: %s\n", s.getStatus());
                if (i < students.size() - 1) {
                    System.out.println("----------------------------\n");
                }
            }

            // 4. Save report file outputs natively
            FileManager.writeGradeReport(outputPath, students);
            System.out.println("\n✅ Success: Consolidated report printed and saved to '" + outputPath + "'");

        } catch (FileNotFoundException e) {
            System.out.println("\n❌ File Error: The required source file '" + inputPath + "' was not found.");
            System.out.println("Please create a 'students.csv' file in the same folder as this script.");
        } catch (IOException e) {
            System.out.println("\n❌ System Error reading/writing data: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n❌ Critical Runtime Failure: " + e.getMessage());
        }
    }
}

// ============================================================================
// AUXILIARY CLASSES (Package-Private: Declared without 'public' keyword)
// ============================================================================

class Student {
    private final String name;
    private final List<Integer> scores;
    private double average;
    private char letterGrade;
    private String status;

    public Student(String name, List<Integer> scores) {
        this.name = name;
        this.scores = scores;
    }

    public String getName() { return name; }
    public List<Integer> getScores() { return scores; }
    public double getAverage() { return average; }
    public void setAverage(double average) { this.average = average; }
    public char getLetterGrade() { return letterGrade; }
    public void setLetterGrade(char letterGrade) { this.letterGrade = letterGrade; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

class GradeCalculator {
    public static void processStudentGrades(Student student) {
        List<Integer> scores = student.getScores();
        if (scores == null || scores.isEmpty()) {
            student.setAverage(0.0);
            student.setLetterGrade('F');
            student.setStatus("FAIL");
            return;
        }

        double total = 0;
        for (int score : scores) {
            total += score;
        }
        double average = total / scores.size();
        student.setAverage(average);

        char grade;
        if (average >= 70) grade = 'A';
        else if (average >= 60) grade = 'B';
        else if (average >= 50) grade = 'C';
        else if (average >= 45) grade = 'D';
        else grade = 'F';
        
        student.setLetterGrade(grade);
        student.setStatus(grade == 'F' ? "FAIL" : "PASS");
    }
}

class FileManager {
    public static List<Student> readStudentsFromCsv(String filePath) throws IOException {
        List<Student> students = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] tokens = line.split(",");
                if (tokens.length < 2) continue;

                String name = tokens[0].trim();
                
                // Improvement 1: Prevent blank names from rendering silently
                if (name.isEmpty()) {
                    name = "Unknown Student";
                }

                List<Integer> scores = new ArrayList<>();

                for (int i = 1; i < tokens.length; i++) {
                    try {
                        scores.add(Integer.parseInt(tokens[i].trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ Warning: Skipping invalid score token '" + tokens[i] + "' for student: " + name);
                    }
                }

                // Improvement 2: Defensively skip rows missing numerical metrics entirely
                if (!scores.isEmpty()) {
                    students.add(new Student(name, scores));
                } else {
                    System.out.println("⚠️ Alert: Skipping record for '" + name + "' due to complete lack of valid scores.");
                }
            }
        }
        return students;
    }

    public static void writeGradeReport(String filePath, List<Student> students) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            String reportHeader = "==========================================\n" +
                                  "        STUDENT GRADE REPORT\n" +
                                  "==========================================\n\n";
            
            bw.write(reportHeader);

            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                bw.write(String.format("Name: %s\n", s.getName()));
                bw.write(String.format("Average: %.2f\n", s.getAverage()));
                bw.write(String.format("Grade: %c\n", s.getLetterGrade()));
                bw.write(String.format("Status: %s\n", s.getStatus()));

                if (i < students.size() - 1) {
                    bw.write("----------------------------\n\n");
                }
            }
        }
    }
}

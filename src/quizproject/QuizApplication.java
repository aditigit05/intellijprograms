package quizproject;
import java.sql.*;
import java.util.Scanner;

// User Class to handle user details
class User {
    private final int userId;
    private final String username;
    private final String email;

    public User(int userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    // You can remove these if not used, but keeping them here for future use
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}

// Question Class to handle question details and fetch questions by difficulty
class Question {
    private final int questionId;
    private final String questionText;
    private final String optionA;
    private final String optionB;
    private final String optionC;
    private final String optionD;
    private final String correctOption;

    public Question(int questionId, String questionText, String optionA, String optionB, String optionC, String optionD, String correctOption) {
        this.questionId = questionId; // Keeping this field for future use
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    public static Question fetchQuestionByDifficulty(Connection conn, String difficultyLevel) throws SQLException {
        String questionQuery = "SELECT * FROM que WHERE difficulty_level = ? ORDER BY RANDOM() LIMIT 1";
        PreparedStatement pstmt = conn.prepareStatement(questionQuery);
        pstmt.setString(1, difficultyLevel);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return new Question(
                    rs.getInt("question_id"),
                    rs.getString("question_text"),
                    rs.getString("option_a"),
                    rs.getString("option_b"),
                    rs.getString("option_c"),
                    rs.getString("option_d"),
                    rs.getString("correct_option")
            );
        } else {
            return null;
        }
    }

    public String displayQuestion() {
        return "\n" + questionText + "\nA) " + optionA + "\nB) " + optionB + "\nC) " + optionC + "\nD) " + optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }
}

// Quiz Class to handle quiz logic
class Quiz {
    private final User user;
    private int score;

    public Quiz(User user) {
        this.user = user;
        this.score = 0;
    }

    public void startQuiz(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Select difficulty level: easy, moderate, tough");
        String difficultyLevel = scanner.nextLine().toLowerCase();
        boolean continueQuiz = true;

        while (continueQuiz) {
            Question question = Question.fetchQuestionByDifficulty(conn, difficultyLevel);

            if (question != null) {
                System.out.println(question.displayQuestion());
                System.out.print("Enter your answer (A, B, C, D) or type 'exit' to leave: ");
                String userAnswer = scanner.nextLine().toUpperCase();

                if (userAnswer.equals("EXIT")) {
                    System.out.println("You have chosen to exit the quiz.");
                    continueQuiz = false;
                } else {
                    if (userAnswer.equals(question.getCorrectOption())) {
                        score += 1;
                        System.out.println("Correct answer! Your score: " + score);
                    } else {
                        System.out.println("Wrong answer! The correct answer was: " + question.getCorrectOption());
                    }
                }

                saveUserResult(conn);
            } else {
                System.out.println("No questions available for this difficulty level.");
                continueQuiz = false;
            }
        }

        System.out.println("Quiz ended. Final score: " + score);
    }

    private void saveUserResult(Connection conn) throws SQLException {
        String insertResult = "INSERT INTO res (user_id, score) VALUES (?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(insertResult);
        pstmt.setInt(1, user.getUserId());
        pstmt.setInt(2, score);
        pstmt.executeUpdate();
    }
}

// Main application class
public class QuizApplication {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection(); Scanner scanner = new Scanner(System.in)) {
            // Step 1: Log in or register user
            User user = loginUser(conn, scanner);

            // Step 2: If login is successful, start the quiz
            if (user != null) {
                Quiz quiz = new Quiz(user);
                quiz.startQuiz(conn, scanner);
            } else {
                System.out.println("Error: Unable to log in or create user.");
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    // User login or registration
    public static User loginUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        String query = "SELECT * FROM users WHERE username = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            // User exists, fetch details
            int userId = rs.getInt("user_id");
            String email = rs.getString("email");
            System.out.println("Login successful. Welcome, " + username + "!");
            return new User(userId, username, email); // Assuming User constructor
        } else {
            // User doesn't exist, create new user
            System.out.println("User not found. Creating new user...");
            System.out.print("Enter your email: ");
            String email = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            String insertUser = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement insertPstmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            insertPstmt.setString(1, username);
            insertPstmt.setString(2, email);
            insertPstmt.setString(3, password);
            insertPstmt.executeUpdate();

            // Retrieve generated user_id
            ResultSet generatedKeys = insertPstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int newUserId = generatedKeys.getInt(1);
                System.out.println("New user created with username: " + username);
                return new User(newUserId, username, email); // Assuming User constructor
            } else {
                System.out.println("Error creating user.");
                return null;
            }
        }
    }
}

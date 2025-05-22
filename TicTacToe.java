import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;

// Player class (using record for simplicity and immutability)
record Player(String name, char mark) {
}

class GameBoard {
    private final char[][] board;
    private char winner; // '\0' if no winner yet, 'D' for draw

    // Represents "row,col"
    private static final List<List<String>> WINNING_COMBINATIONS = Arrays.asList(
            Arrays.asList("0,0", "0,1", "0,2"),
            Arrays.asList("1,0", "1,1", "1,2"),
            Arrays.asList("2,0", "2,1", "2,2"),
            Arrays.asList("0,0", "1,0", "2,0"),
            Arrays.asList("0,1", "1,1", "2,1"),
            Arrays.asList("0,2", "1,2", "2,2"),
            Arrays.asList("0,0", "1,1", "2,2"),
            Arrays.asList("0,2", "1,1", "2,0")
    );

    public GameBoard() {
        board = new char[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' '; // Use space for empty cell for display
            }
        }
        winner = '\0'; // No winner initially
    }

    public void displayBoard() {
        System.out.println("    "); // Initial spacing
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                System.out.println("    " + "---+---+---");
            }
            System.out.print("    ");
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    System.out.print(i + "," + j); // Show coordinates for empty cells
                } else {
                    System.out.print(" " + board[i][j] + " ");
                }
                if (j < 2) {
                    System.out.print("|");
                }
            }
            System.out.println();
        }
        System.out.println("    "); // Trailing spacing
    }
    
    // More accurate display method similar to Python version
    public void displayBoardPythonStyle() {
        System.out.println("    "); // Initial spacing
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                System.out.println("    " + "---------------"); // Adjusted for 3 chars + 2 pipes
            }
            System.out.print("    ");
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                     // To match "0,0" | "0,1" | "0,2" format, we need 3 chars per cell
                    System.out.print(i + "," + j); 
                } else {
                    System.out.print(" " + board[i][j] + " ");
                }
                if (j < 2) {
                    System.out.print(" | ");
                }
            }
            System.out.println();
        }
        System.out.println("    "); // Trailing spacing
    }


    public String checkStatus() {
        // Check rows, columns, and diagonals for a win
        for (List<String> combination : WINNING_COMBINATIONS) {
            String[] cell1_coords = combination.get(0).split(",");
            String[] cell2_coords = combination.get(1).split(",");
            String[] cell3_coords = combination.get(2).split(",");

            char mark1 = board[Integer.parseInt(cell1_coords[0])][Integer.parseInt(cell1_coords[1])];
            char mark2 = board[Integer.parseInt(cell2_coords[0])][Integer.parseInt(cell2_coords[1])];
            char mark3 = board[Integer.parseInt(cell3_coords[0])][Integer.parseInt(cell3_coords[1])];

            if (mark1 != ' ' && mark1 == mark2 && mark2 == mark3) {
                this.winner = mark1;
                return "Complete";
            }
        }

        // Check for draw (no empty cells left)
        boolean freeCellExists = false;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    freeCellExists = true;
                    break;
                }
            }
            if (freeCellExists) break;
        }

        if (!freeCellExists) {
            this.winner = 'D'; // 'D' for Draw
            return "Complete";
        }

        return "Pending";
    }

    public boolean isCellEmpty(int r, int c) {
        if (r < 0 || r > 2 || c < 0 || c > 2) { // Should be caught before this
            return false;
        }
        return board[r][c] == ' ';
    }

    public boolean placeMark(int r, int c, char mark) {
        if (r >= 0 && r < 3 && c >= 0 && c < 3 && board[r][c] == ' ') {
            board[r][c] = mark;
            return true;
        }
        return false; // Should not happen if isCellEmpty and bounds are checked first
    }

    public char getWinner() {
        return winner;
    }
}

public class TicTacToe {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to Tic-Tac-Toe!");

        // Player Setup
        System.out.print("Please enter a name for Player 1: ");
        String p1Name = scanner.nextLine();
        char p1Mark;
        while (true) {
            System.out.print("Please choose a mark between X and O for " + p1Name + ": ");
            String markInput = scanner.nextLine().toUpperCase();
            if (markInput.length() == 1 && (markInput.charAt(0) == 'X' || markInput.charAt(0) == 'O')) {
                p1Mark = markInput.charAt(0);
                break;
            } else {
                System.out.println("Invalid mark. Please choose X or O.");
            }
        }

        System.out.print("Please enter a name for Player 2: ");
        String p2Name = scanner.nextLine();
        char p2Mark = (p1Mark == 'X') ? 'O' : 'X';

        Player player1 = new Player(p1Name, p1Mark);
        Player player2 = new Player(p2Name, p2Mark);

        System.out.println(player1.name() + " uses " + player1.mark());
        System.out.println(player2.name() + " uses " + player2.mark());

        System.out.println("\nInitializing Game Board....");
        GameBoard board = new GameBoard();
        System.out.println("\nGame Started.\n=============\n");
        board.displayBoardPythonStyle(); // Use the Python-like display

        System.out.println("\nChoose a position from available positions on the board (e.g., 0,1).\n");

        Player currentPlayer = player1;
        String gameStatus = board.checkStatus();

        while (gameStatus.equals("Pending")) {
            System.out.println(currentPlayer.name() + "'s turn (" + currentPlayer.mark() + "):");
            handlePlayerTurn(scanner, currentPlayer, board);
            
            System.out.println("\n");
            board.displayBoardPythonStyle();
            System.out.println("\n");

            gameStatus = board.checkStatus();
            if (gameStatus.equals("Complete")) {
                break;
            }

            // Switch player
            currentPlayer = (currentPlayer == player1) ? player2 : player1;
        }

        // End Game
        char winnerMark = board.getWinner();
        if (winnerMark != '\0' && winnerMark != 'D') {
            String winnerName = (winnerMark == player1.mark()) ? player1.name() : player2.name();
            System.out.println("Congratulations! " + winnerName + " is the winner.");
        } else if (winnerMark == 'D') {
            System.out.println("Game ended in a draw.");
        } else {
            // Should not happen if loop broke correctly
            System.out.println("Game ended unexpectedly.");
        }
        scanner.close();
    }

    private static void handlePlayerTurn(Scanner sc, Player currentPlayer, GameBoard board) {
        while (true) {
            System.out.print(currentPlayer.name() + ": ");
            String input = sc.nextLine();
            try {
                String[] parts = input.split(",");
                if (parts.length != 2) {
                    System.out.println("Invalid format. Please use row,col (e.g., 0,1).");
                    continue;
                }
                int row = Integer.parseInt(parts[0].trim());
                int col = Integer.parseInt(parts[1].trim());

                if (!(row >= 0 && row <= 2 && col >= 0 && col <= 2)) {
                    System.out.println("Invalid position. Row and column must be between 0 and 2.");
                    continue;
                }

                if (!board.isCellEmpty(row, col)) {
                    System.out.println("Cell already occupied. Choose an empty cell.");
                    continue;
                }

                board.placeMark(row, col, currentPlayer.mark());
                break; // Valid input, exit loop

            } catch (NumberFormatException e) {
                System.out.println("Invalid format. Please enter numbers for row and column (e.g., 0,1).");
            } catch (Exception e) { // Catch any other unexpected issues with input
                System.out.println("An unexpected error occurred with your input. Please try again. Format: row,col");
            }
        }
    }
}
```

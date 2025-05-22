import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class TicTacToeTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    // Helper to normalize line endings for cross-platform compatibility
    private String getNormalizedOutput() {
        return outContent.toString().replace("\r\n", "\n");
    }

    // --- GameBoard Tests ---
    @Test
    @DisplayName("GameBoard: Should place mark and update cell state")
    void testGameBoard_PlaceMarkAndIsCellEmpty() {
        GameBoard board = new GameBoard();
        assertTrue(board.isCellEmpty(0, 0), "Cell (0,0) should be empty initially.");
        assertTrue(board.placeMark(0, 0, 'X'), "placeMark should return true for successful placement.");
        assertFalse(board.isCellEmpty(0, 0), "Cell (0,0) should not be empty after placing mark.");
    }

    @Test
    @DisplayName("GameBoard: Should handle out-of-bounds and occupied cell placements")
    void testGameBoard_PlaceMark_OutOfBoundsOrOccupied() {
        GameBoard board = new GameBoard();
        // GameBoard.placeMark has bounds checking
        assertFalse(board.placeMark(-1, 0, 'X'), "Should return false for out of bounds (negative index).");
        assertFalse(board.placeMark(3, 0, 'X'), "Should return false for out of bounds (positive index).");

        assertTrue(board.placeMark(0, 0, 'X'), "Should place mark in empty cell successfully the first time.");
        assertFalse(board.isCellEmpty(0,0), "Cell should be occupied after first mark.");
        assertFalse(board.placeMark(0, 0, 'O'), "Should return false when trying to place mark in occupied cell.");
        // To verify the mark wasn't changed, we can check the winner if this was a winning move
        // or rely on isCellEmpty still being false and assuming the original mark 'X' is there.
        // For example, if we make X win:
        board.placeMark(0,1,'X');
        board.placeMark(0,2,'X');
        assertEquals('X', board.getWinner(), "Winner should still be X, implying O did not overwrite.");
    }
    
    @Test
    @DisplayName("GameBoard: Should detect X winning in a row")
    void testGameBoard_CheckStatus_XWinsRow() {
        GameBoard board = new GameBoard();
        board.placeMark(0, 0, 'X');
        board.placeMark(0, 1, 'X');
        board.placeMark(0, 2, 'X');
        assertEquals("Complete", board.checkStatus(), "Game should be complete.");
        assertEquals('X', board.getWinner(), "Winner should be X.");
    }

    @Test
    @DisplayName("GameBoard: Should detect O winning in a column")
    void testGameBoard_CheckStatus_OWinsColumn() {
        GameBoard board = new GameBoard();
        board.placeMark(0, 1, 'O');
        board.placeMark(1, 1, 'O');
        board.placeMark(2, 1, 'O');
        assertEquals("Complete", board.checkStatus(), "Game should be complete.");
        assertEquals('O', board.getWinner(), "Winner should be O.");
    }

    @Test
    @DisplayName("GameBoard: Should detect X winning on main diagonal")
    void testGameBoard_CheckStatus_XWinsDiagonal1() {
        GameBoard board = new GameBoard();
        board.placeMark(0, 0, 'X');
        board.placeMark(1, 1, 'X');
        board.placeMark(2, 2, 'X');
        assertEquals("Complete", board.checkStatus(), "Game should be complete.");
        assertEquals('X', board.getWinner(), "Winner should be X.");
    }

    @Test
    @DisplayName("GameBoard: Should detect O winning on anti-diagonal")
    void testGameBoard_CheckStatus_OWinsDiagonal2() {
        GameBoard board = new GameBoard();
        board.placeMark(0, 2, 'O');
        board.placeMark(1, 1, 'O');
        board.placeMark(2, 0, 'O');
        assertEquals("Complete", board.checkStatus(), "Game should be complete.");
        assertEquals('O', board.getWinner(), "Winner should be O.");
    }

    @Test
    @DisplayName("GameBoard: Should detect a draw")
    void testGameBoard_CheckStatus_Draw() {
        GameBoard board = new GameBoard();
        board.placeMark(0, 0, 'X'); board.placeMark(0, 1, 'O'); board.placeMark(0, 2, 'X');
        board.placeMark(1, 0, 'X'); board.placeMark(1, 1, 'X'); board.placeMark(1, 2, 'O');
        board.placeMark(2, 0, 'O'); board.placeMark(2, 1, 'X'); board.placeMark(2, 2, 'O');
        assertEquals("Complete", board.checkStatus(), "Game should be complete for a draw.");
        assertEquals('D', board.getWinner(), "Game should be a draw ('D').");
    }

    @Test
    @DisplayName("GameBoard: Should detect game as pending")
    void testGameBoard_CheckStatus_Pending() {
        GameBoard board = new GameBoard();
        board.placeMark(0, 0, 'X');
        board.placeMark(0, 1, 'O');
        assertEquals("Pending", board.checkStatus(), "Game should be pending.");
        assertEquals('\0', board.getWinner(), "There should be no winner yet (null char).");
    }
    
    // --- TicTacToe.handlePlayerTurn Tests ---
    // IMPORTANT: These tests assume TicTacToe.handlePlayerTurn is changed to public static.

    @Test
    @DisplayName("handlePlayerTurn: Should accept valid move")
    void testHandlePlayerTurn_ValidMove() {
        GameBoard board = new GameBoard();
        Player player = new Player("TestPlayer", 'X');
        String simulatedInput = "0,0\n"; // Valid move
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Scanner sc = new Scanner(System.in); 
        
        TicTacToe.handlePlayerTurn(sc, player, board); // Assumes public static

        assertFalse(board.isCellEmpty(0, 0), "Cell (0,0) should be occupied after valid move.");
        // To confirm the *correct* mark 'X' was placed, we can make this move part of a win for X
        board.placeMark(0,1,'X');
        board.placeMark(0,2,'X');
        assertEquals('X', board.getWinner(), "Winner should be X, confirming X's mark was placed by handlePlayerTurn.");
        assertTrue(getNormalizedOutput().isEmpty(), "No error messages should be printed for valid input.");
        sc.close();
    }

    @Test
    @DisplayName("handlePlayerTurn: Should reject invalid formats then accept valid move")
    void testHandlePlayerTurn_InvalidFormat_ThenValid() {
        GameBoard board = new GameBoard();
        Player player = new Player("TestPlayer", 'O');
        String simulatedInput = "1\n"          // Invalid format (single number)
                             + "a,b\n"        // Invalid format (non-numeric)
                             + "1,1\n";       // Valid
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Scanner sc = new Scanner(System.in);
        TicTacToe.handlePlayerTurn(sc, player, board);

        assertFalse(board.isCellEmpty(1, 1), "Cell (1,1) should be occupied by 'O'.");
        String output = getNormalizedOutput();
        assertTrue(output.contains("Invalid format. Please use row,col (e.g., 0,1)."), "Error for '1' not found.");
        assertTrue(output.contains("Invalid format. Please enter numbers for row and column (e.g., 0,1)."), "Error for 'a,b' not found.");
        sc.close();
    }

    @Test
    @DisplayName("handlePlayerTurn: Should reject out-of-bounds then accept valid move")
    void testHandlePlayerTurn_OutOfBounds_ThenValid() {
        GameBoard board = new GameBoard();
        Player player = new Player("TestPlayer", 'X');
        String simulatedInput = "-1,0\n"        // Out of bounds
                             + "3,3\n"         // Out of bounds
                             + "2,2\n";       // Valid
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Scanner sc = new Scanner(System.in);
        TicTacToe.handlePlayerTurn(sc, player, board);

        assertFalse(board.isCellEmpty(2, 2), "Cell (2,2) should be occupied by 'X'.");
        String output = getNormalizedOutput();
        assertTrue(output.contains("Invalid position. Row and column must be between 0 and 2."), "Error for out of bounds not found.");
        sc.close();
    }

    @Test
    @DisplayName("handlePlayerTurn: Should reject occupied cell then accept valid move")
    void testHandlePlayerTurn_OccupiedCell_ThenValid() {
        GameBoard board = new GameBoard();
        Player playerX = new Player("PlayerX", 'X');
        Player playerO = new Player("PlayerO", 'O');
        board.placeMark(0, 0, playerX.mark()); // Pre-occupy cell

        String simulatedInput = "0,0\n"        // Occupied
                             + "1,2\n";       // Valid for PlayerO
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        Scanner sc = new Scanner(System.in);
        TicTacToe.handlePlayerTurn(sc, playerO, board); // Player O's turn

        assertFalse(board.isCellEmpty(1, 2), "Cell (1,2) should be occupied by 'O'.");
        assertFalse(board.isCellEmpty(0, 0), "Cell (0,0) should still be occupied by 'X'.");
        String output = getNormalizedOutput();
        assertTrue(output.contains("Cell already occupied. Choose an empty cell."), "Error for occupied cell not found.");
        sc.close();
    }

    // --- Mark Selection Tests (testing parts of TicTacToe.main) ---
    @Test
    @DisplayName("MarkSelection: Player 1 valid 'X', Player 2 should be 'O'")
    void testMarkSelection_ValidX_P2isO() {
        String p1Name = "P1";
        String p2Name = "P2";
        String simulatedInput = p1Name + "\n" + 
                                "X\n" + 
                                p2Name + "\n" +
                                "0,0\n" + // P1 (X)
                                "1,0\n" + // P2 (O)
                                "0,1\n" + // P1
                                "1,1\n" + // P2
                                "0,2\n";  // P1 wins, game ends
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        TicTacToe.main(new String[]{}); 

        String output = getNormalizedOutput();
        assertTrue(output.contains(p1Name + " uses X"), "Output should confirm P1 uses X.");
        assertTrue(output.contains(p2Name + " uses O"), "Output should confirm P2 uses O.");
    }

    @Test
    @DisplayName("MarkSelection: Player 1 valid 'o' (lowercase), stored as 'O', Player 2 'X'")
    void testMarkSelection_Valid_o_lowercase_P2isX() {
        String p1Name = "PlayerO";
        String p2Name = "PlayerX";
         String simulatedInput = p1Name + "\n" + 
                                "o\n" + 
                                p2Name + "\n" +
                                "0,0\n" + 
                                "1,0\n" + 
                                "0,1\n" + 
                                "1,1\n" + 
                                "0,2\n";  
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        TicTacToe.main(new String[]{});

        String output = getNormalizedOutput();
        assertTrue(output.contains(p1Name + " uses O"), "Output should confirm P1 uses O (uppercase).");
        assertTrue(output.contains(p2Name + " uses X"), "Output should confirm P2 uses X.");
    }
    
    @Test
    @DisplayName("MarkSelection: Player 1 invalid marks then valid 'O', Player 2 'X'")
    void testMarkSelection_InvalidMark_ThenValidO_P2isX() {
        String p1Name = "Tester";
        String p2Name = "Opponent";
        String simulatedInput = p1Name + "\n" + 
                                "A\n" +       
                                "XO\n" +      
                                "O\n" +       
                                p2Name + "\n" +
                                "0,0\n" + 
                                "1,0\n" + 
                                "0,1\n" + 
                                "1,1\n" + 
                                "0,2\n";  

        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        TicTacToe.main(new String[]{});

        String output = getNormalizedOutput();
        assertTrue(output.contains("Invalid mark. Please choose X or O."), "Should show error for invalid marks.");
        assertTrue(output.contains(p1Name + " uses O"), "Output should confirm P1 uses O.");
        assertTrue(output.contains(p2Name + " uses X"), "Output should confirm P2 uses X.");
        
        long count = output.lines().filter(line -> line.contains("Invalid mark. Please choose X or O.")).count();
        assertEquals(2, count, "Expected exactly two invalid mark prompts for 'A' and 'XO'.");
    }
}
```

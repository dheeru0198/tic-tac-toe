package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
	"unicode"
)

// Player struct holds the player's name and their mark ('X' or 'O').
type Player struct {
	Name string
	Mark rune
}

// GameBoard struct represents the Tic-Tac-Toe board and game state.
type GameBoard struct {
	board  [3][3]rune
	winner rune // 'X', 'O', 'D' (Draw), or ' ' (Pending)
}

// winningCombinations defines all possible winning lines on the board.
// Each inner slice represents a cell as {row, col}.
var winningCombinations = [][][]int{
	// Rows
	{{0, 0}, {0, 1}, {0, 2}},
	{{1, 0}, {1, 1}, {1, 2}},
	{{2, 0}, {2, 1}, {2, 2}},
	// Columns
	{{0, 0}, {1, 0}, {2, 0}},
	{{0, 1}, {1, 1}, {2, 1}},
	{{0, 2}, {1, 2}, {2, 2}},
	// Diagonals
	{{0, 0}, {1, 1}, {2, 2}},
	{{0, 2}, {1, 1}, {2, 0}},
}

// NewGameBoard creates and returns a new GameBoard initialized for the start of a game.
func NewGameBoard() *GameBoard {
	gb := &GameBoard{winner: ' '} // Game is initially pending
	for i := 0; i < 3; i++ {
		for j := 0; j < 3; j++ {
			gb.board[i][j] = ' ' // Initialize with empty spaces
		}
	}
	return gb
}

// DisplayBoard prints the current state of the game board to the console.
// Empty cells are shown with their "row,col" coordinates.
func (gb *GameBoard) DisplayBoard() {
	fmt.Println("\n    ") // Initial spacing
	for i := 0; i < 3; i++ {
		if i > 0 {
			fmt.Println("    " + "---------------") // Separator line
		}
		rowStr := "    "
		for j := 0; j < 3; j++ {
			if gb.board[i][j] == ' ' {
				rowStr += fmt.Sprintf("%d,%d", i, j)
			} else {
				rowStr += fmt.Sprintf(" %c ", gb.board[i][j])
			}
			if j < 2 {
				rowStr += " | "
			}
		}
		fmt.Println(rowStr)
	}
	fmt.Println("    \n") // Trailing spacing
}

// IsCellEmpty checks if the cell at the given row and column is empty.
func (gb *GameBoard) IsCellEmpty(row, col int) bool {
	if row < 0 || row > 2 || col < 0 || col > 2 {
		return false // Out of bounds is not considered "empty" in a playable sense
	}
	return gb.board[row][col] == ' '
}

// PlaceMark attempts to place the given mark at the specified row and column.
// It returns true if the mark was placed successfully (cell was empty and in bounds),
// and false otherwise.
func (gb *GameBoard) PlaceMark(row, col int, mark rune) bool {
	if row >= 0 && row < 3 && col >= 0 && col < 3 && gb.board[row][col] == ' ' {
		gb.board[row][col] = mark
		return true
	}
	return false
}

// CheckStatus evaluates the board for a win, draw, or if the game is still pending.
// It updates gb.winner and returns the status ('X', 'O', 'D' for Draw, ' ' for Pending).
func (gb *GameBoard) CheckStatus() rune {
	// Check for a win
	for _, combination := range winningCombinations {
		cell1 := gb.board[combination[0][0]][combination[0][1]]
		cell2 := gb.board[combination[1][0]][combination[1][1]]
		cell3 := gb.board[combination[2][0]][combination[2][1]]

		if cell1 != ' ' && cell1 == cell2 && cell2 == cell3 {
			gb.winner = cell1 // Winner found
			return gb.winner
		}
	}

	// Check for a draw (no empty cells left and no winner yet)
	hasEmptyCell := false
	for i := 0; i < 3; i++ {
		for j := 0; j < 3; j++ {
			if gb.board[i][j] == ' ' {
				hasEmptyCell = true
				break
			}
		}
		if hasEmptyCell {
			break
		}
	}

	if !hasEmptyCell {
		gb.winner = 'D' // Draw
		return gb.winner
	}

	gb.winner = ' ' // Pending
	return gb.winner
}

// GetWinner returns the current winner of the game ('X', 'O', 'D', or ' ').
func (gb *GameBoard) GetWinner() rune {
	return gb.winner
}

// getInput reads a line of text from the console after printing a prompt.
func getInput(prompt string, reader *bufio.Reader) string {
	fmt.Print(prompt)
	input, _ := reader.ReadString('\n')
	return strings.TrimSpace(input)
}

// handlePlayerTurn manages a single player's turn, including input and validation.
func handlePlayerTurn(reader *bufio.Reader, currentPlayer Player, gb *GameBoard) {
	fmt.Printf("\n%s's turn (%c).\n", currentPlayer.Name, currentPlayer.Mark)
	// Board is displayed by the main loop before calling this

	for {
		moveInput := getInput(fmt.Sprintf("%s: ", currentPlayer.Name), reader)
		parts := strings.Split(moveInput, ",")

		if len(parts) != 2 {
			fmt.Println("Invalid format. Please use row,col (e.g., 0,1).")
			continue
		}

		rowStr := strings.TrimSpace(parts[0])
		colStr := strings.TrimSpace(parts[1])

		row, errRow := strconv.Atoi(rowStr)
		col, errCol := strconv.Atoi(colStr)

		if errRow != nil || errCol != nil {
			fmt.Println("Invalid format. Please enter numbers for row and column (e.g., 0,1).")
			continue
		}

		if !(row >= 0 && row <= 2 && col >= 0 && col <= 2) {
			fmt.Println("Invalid position. Row and column must be between 0 and 2.")
			continue
		}

		if !gb.IsCellEmpty(row, col) {
			fmt.Println("Cell already occupied. Choose an empty cell.")
			continue
		}

		if gb.PlaceMark(row, col, currentPlayer.Mark) {
			break // Valid move placed, exit loop
		}
		// Should not be reached if IsCellEmpty and bounds check are correct,
		// but as a fallback:
		fmt.Println("Failed to place mark. Please try again.")
	}
}

func main() {
	reader := bufio.NewReader(os.Stdin)
	fmt.Println("Welcome to Tic-Tac-Toe (Go Version)!")

	// Player Setup
	p1Name := getInput("Please enter a name for Player 1: ", reader)
	var p1Mark rune
	for {
		markInputStr := getInput(fmt.Sprintf("Please choose a mark (X or O) for %s: ", p1Name), reader)
		if len(markInputStr) == 1 {
			mark := unicode.ToUpper(rune(markInputStr[0]))
			if mark == 'X' || mark == 'O' {
				p1Mark = mark
				break
			}
		}
		fmt.Println("Invalid mark. Please choose X or O.")
	}

	p2Name := getInput("Please enter a name for Player 2: ", reader)
	var p2Mark rune
	if p1Mark == 'X' {
		p2Mark = 'O'
	} else {
		p2Mark = 'X'
	}

	player1 := Player{Name: p1Name, Mark: p1Mark}
	player2 := Player{Name: p2Name, Mark: p2Mark}

	fmt.Printf("\n%s uses %c\n", player1.Name, player1.Mark)
	fmt.Printf("%s uses %c\n", player2.Name, player2.Mark)

	// Game Initialization
	board := NewGameBoard()
	fmt.Println("\nInitializing Game Board....")
	fmt.Println("Game Started.\n=============\n")
	
	currentPlayer := player1
	gameStatus := board.CheckStatus() // Should be ' ' initially

	for gameStatus == ' ' {
		board.DisplayBoard()
		fmt.Printf("\nChoose a position from available positions on the board (e.g., 0,1).\n")
		handlePlayerTurn(reader, currentPlayer, board)
		
		gameStatus = board.CheckStatus()
		if gameStatus != ' ' {
			board.DisplayBoard() // Display final board state
			break
		}

		if currentPlayer.Name == player1.Name {
			currentPlayer = player2
		} else {
			currentPlayer = player1
		}
	}

	// End Game
	fmt.Println("\nGame Over.")
	winner := board.GetWinner()
	if winner == 'D' {
		fmt.Println("Game ended in a draw.")
	} else if winner != ' ' {
		winnerName := ""
		if winner == player1.Mark {
			winnerName = player1.Name
		} else {
			winnerName = player2.Name
		}
		fmt.Printf("Congratulations! %s (%c) is the winner!\n", winnerName, winner)
	} else {
		// Should not happen if loop logic is correct
		fmt.Println("Game ended unexpectedly.")
	}
}
```

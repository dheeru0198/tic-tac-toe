import * as readlineSync from 'readline-sync';

type Mark = 'X' | 'O';
type CellValue = Mark | null;

interface Player {
    name: string;
    mark: Mark;
}

class GameBoard {
    private board: CellValue[][];
    private gameWinnerMark: Mark | null = null; // Stores the mark of the winner
    private isDraw: boolean = false;

    private static readonly WINNING_COMBINATIONS: number[][][] = [
        // Rows
        [[0, 0], [0, 1], [0, 2]],
        [[1, 0], [1, 1], [1, 2]],
        [[2, 0], [2, 1], [2, 2]],
        // Columns
        [[0, 0], [1, 0], [2, 0]],
        [[0, 1], [1, 1], [2, 1]],
        [[0, 2], [1, 2], [2, 2]],
        // Diagonals
        [[0, 0], [1, 1], [2, 2]],
        [[0, 2], [1, 1], [2, 0]],
    ];

    constructor() {
        this.board = [
            [null, null, null],
            [null, null, null],
            [null, null, null],
        ];
    }

    displayBoard(): void {
        console.log("\n    "); // Initial spacing
        for (let i = 0; i < 3; i++) {
            if (i > 0) {
                console.log("    " + "---------------"); // Adjusted for 3 chars + 2 pipes
            }
            let rowStr = "    ";
            for (let j = 0; j < 3; j++) {
                if (this.board[i][j] === null) {
                    rowStr += `${i},${j}`;
                } else {
                    rowStr += ` ${this.board[i][j]} `;
                }
                if (j < 2) {
                    rowStr += " | ";
                }
            }
            console.log(rowStr);
        }
        console.log("    \n"); // Trailing spacing
    }

    isCellEmpty(row: number, col: number): boolean {
        return this.board[row][col] === null;
    }

    placeMark(row: number, col: number, mark: Mark): boolean {
        if (row >= 0 && row < 3 && col >= 0 && col < 3 && this.isCellEmpty(row, col)) {
            this.board[row][col] = mark;
            return true;
        }
        return false;
    }

    checkStatus(): 'Win' | 'Draw' | 'Pending' {
        // Check for a win
        for (const combination of GameBoard.WINNING_COMBINATIONS) {
            const [a, b, c] = combination;
            const cellA = this.board[a[0]][a[1]];
            const cellB = this.board[b[0]][b[1]];
            const cellC = this.board[c[0]][c[1]];

            if (cellA && cellA === cellB && cellA === cellC) {
                this.gameWinnerMark = cellA; // cellA is 'X' or 'O'
                return 'Win';
            }
        }

        // Check for a draw (no null cells left and no winner)
        let hasEmptyCell = false;
        for (let i = 0; i < 3; i++) {
            for (let j = 0; j < 3; j++) {
                if (this.board[i][j] === null) {
                    hasEmptyCell = true;
                    break;
                }
            }
            if (hasEmptyCell) break;
        }

        if (!hasEmptyCell) {
            this.isDraw = true;
            return 'Draw';
        }

        return 'Pending';
    }

    getWinnerMark(): Mark | null {
        return this.gameWinnerMark;
    }

    getIsDraw(): boolean {
        return this.isDraw;
    }
}

function handlePlayerTurn(player: Player, board: GameBoard): void {
    console.log(`\n${player.name}'s turn (${player.mark}).`);
    // board.displayBoard(); // Board is displayed before calling this in the main loop

    while (true) {
        const moveInput = readlineSync.question(`${player.name}: `);
        const parts = moveInput.split(',');

        if (parts.length !== 2) {
            console.log("Invalid format. Please use row,col (e.g., 0,1).");
            continue;
        }

        const row = parseInt(parts[0].trim(), 10);
        const col = parseInt(parts[1].trim(), 10);

        if (isNaN(row) || isNaN(col)) {
            console.log("Invalid format. Please enter numbers for row and column (e.g., 0,1).");
            continue;
        }

        if (!(row >= 0 && row <= 2 && col >= 0 && col <= 2)) {
            console.log("Invalid position. Row and column must be between 0 and 2.");
            continue;
        }

        if (!board.isCellEmpty(row, col)) {
            console.log("Cell already occupied. Choose an empty cell.");
            continue;
        }

        board.placeMark(row, col, player.mark);
        break; // Valid input, exit loop
    }
}

function startGame(): void {
    console.log("Welcome to Tic-Tac-Toe (TypeScript Version)!");

    // Player Setup
    const p1Name = readlineSync.question("Please enter a name for Player 1: ");
    let p1Mark: Mark;
    while (true) {
        const markInput = readlineSync.question(`Please choose a mark (X or O) for ${p1Name}: `).toUpperCase();
        if (markInput === 'X' || markInput === 'O') {
            p1Mark = markInput as Mark;
            break;
        } else {
            console.log("Invalid mark. Please choose X or O.");
        }
    }

    const p2Name = readlineSync.question("Please enter a name for Player 2: ");
    const p2Mark: Mark = (p1Mark === 'X') ? 'O' : 'X';

    const player1: Player = { name: p1Name, mark: p1Mark };
    const player2: Player = { name: p2Name, mark: p2Mark };

    console.log(`\n${player1.name} uses ${player1.mark}`);
    console.log(`${player2.name} uses ${player2.mark}`);

    const board = new GameBoard();
    console.log("\nInitializing Game Board....");
    console.log("Game Started.\n=============\n");
    
    let currentPlayer = player1;
    let gameStatus = board.checkStatus();

    while (gameStatus === 'Pending') {
        board.displayBoard(); // Display board at the start of each turn
        console.log(`\nChoose a position from available positions on the board (e.g., 0,1).`);
        handlePlayerTurn(currentPlayer, board);
        
        gameStatus = board.checkStatus();
        if (gameStatus !== 'Pending') {
            board.displayBoard(); // Display final board state
            break;
        }

        currentPlayer = (currentPlayer === player1) ? player2 : player1;
    }

    // End Game
    if (gameStatus === 'Win') {
        const winnerMark = board.getWinnerMark();
        const winnerName = (winnerMark === player1.mark) ? player1.name : player2.name;
        console.log(`Congratulations! ${winnerName} (${winnerMark}) is the winner!`);
    } else if (gameStatus === 'Draw') {
        console.log("Game ended in a draw.");
    } else {
        // Should not happen if loop broke correctly
        console.log("Game ended unexpectedly.");
    }
}

// Create a simple package.json if it doesn't exist, or tell user to run npm init
// For now, assuming user will handle package.json or run with ts-node directly
// after `npm install readline-sync @types/readline-sync`

startGame();
```

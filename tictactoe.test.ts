import { Player, GameBoard, handlePlayerTurn, startGame, Mark } from './tictactoe'; // Adjust path if needed
import * as readlineSync from 'readline-sync';

jest.mock('readline-sync');
const mockedReadlineSync = readlineSync as jest.Mocked<typeof readlineSync>;

describe('GameBoard', () => {
    let board: GameBoard;

    beforeEach(() => {
        board = new GameBoard();
    });

    it('should initialize an empty 3x3 board', () => {
        for (let i = 0; i < 3; i++) {
            for (let j = 0; j < 3; j++) {
                expect(board.isCellEmpty(i, j)).toBe(true);
            }
        }
    });

    it('should place a mark correctly on an empty cell', () => {
        expect(board.placeMark(0, 0, 'X')).toBe(true);
        expect(board.isCellEmpty(0, 0)).toBe(false);
        // To check the actual mark, we'd ideally have a getMarkAt(row, col)
        // Or, we can infer it by checking game status after more moves
        board.placeMark(0,1,'X');
        board.placeMark(0,2,'X');
        expect(board.checkStatus()).toBe('Win');
        expect(board.getWinnerMark()).toBe('X');
    });

    it('should not place a mark on an occupied cell', () => {
        board.placeMark(0, 0, 'X');
        expect(board.placeMark(0, 0, 'O')).toBe(false);
        expect(board.isCellEmpty(0, 0)).toBe(false); // Still occupied
        // Check that 'X' is still there (indirectly)
        board.placeMark(1,1,'O');
        board.placeMark(0,1,'X');
        board.placeMark(2,2,'O');
        board.placeMark(0,2,'X'); // X wins
        expect(board.checkStatus()).toBe('Win');
        expect(board.getWinnerMark()).toBe('X');
    });

    it('should not place a mark out of bounds', () => {
        expect(board.placeMark(3, 0, 'X')).toBe(false);
        expect(board.placeMark(0, -1, 'O')).toBe(false);
    });

    // Test winning conditions
    const marks: Mark[] = ['X', 'O'];
    marks.forEach(mark => {
        // Rows
        for (let i = 0; i < 3; i++) {
            it(`should detect ${mark} win in row ${i}`, () => {
                board.placeMark(i, 0, mark);
                board.placeMark(i, 1, mark);
                board.placeMark(i, 2, mark);
                expect(board.checkStatus()).toBe('Win');
                expect(board.getWinnerMark()).toBe(mark);
            });
        }
        // Columns
        for (let j = 0; j < 3; j++) {
            it(`should detect ${mark} win in column ${j}`, () => {
                board.placeMark(0, j, mark);
                board.placeMark(1, j, mark);
                board.placeMark(2, j, mark);
                expect(board.checkStatus()).toBe('Win');
                expect(board.getWinnerMark()).toBe(mark);
            });
        }
        // Diagonals
        it(`should detect ${mark} win on main diagonal`, () => {
            board.placeMark(0, 0, mark);
            board.placeMark(1, 1, mark);
            board.placeMark(2, 2, mark);
            expect(board.checkStatus()).toBe('Win');
            expect(board.getWinnerMark()).toBe(mark);
        });
        it(`should detect ${mark} win on anti-diagonal`, () => {
            board.placeMark(0, 2, mark);
            board.placeMark(1, 1, mark);
            board.placeMark(2, 0, mark);
            expect(board.checkStatus()).toBe('Win');
            expect(board.getWinnerMark()).toBe(mark);
        });
    });

    it('should detect a draw condition', () => {
        // X O X
        // X X O
        // O X O
        board.placeMark(0, 0, 'X'); board.placeMark(0, 1, 'O'); board.placeMark(0, 2, 'X');
        board.placeMark(1, 0, 'X'); board.placeMark(1, 1, 'X'); board.placeMark(1, 2, 'O');
        board.placeMark(2, 0, 'O'); board.placeMark(2, 1, 'X'); board.placeMark(2, 2, 'O');
        expect(board.checkStatus()).toBe('Draw');
        expect(board.getIsDraw()).toBe(true);
        expect(board.getWinnerMark()).toBe(null);
    });

    it('should detect a pending game condition', () => {
        board.placeMark(0, 0, 'X');
        board.placeMark(1, 1, 'O');
        expect(board.checkStatus()).toBe('Pending');
        expect(board.getWinnerMark()).toBe(null);
        expect(board.getIsDraw()).toBe(false);
    });

    it('displayBoard should run without errors (visual check or more advanced spy)', () => {
        const consoleSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
        board.displayBoard();
        expect(consoleSpy).toHaveBeenCalled(); // Basic check that it tried to print something
        consoleSpy.mockRestore();
    });
});

describe('handlePlayerTurn input validation', () => {
    let board: GameBoard;
    let player: Player;
    let consoleSpy: jest.SpyInstance;

    beforeEach(() => {
        board = new GameBoard();
        player = { name: 'TestPlayer', mark: 'X' };
        consoleSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
        mockedReadlineSync.question.mockReset(); // Reset mock for each test
    });

    afterEach(() => {
        consoleSpy.mockRestore();
    });

    it('should accept a valid move and update the board', () => {
        mockedReadlineSync.question.mockReturnValueOnce('0,0');
        handlePlayerTurn(player, board); // Assumes handlePlayerTurn is exported and uses readlineSync
        expect(mockedReadlineSync.question).toHaveBeenCalledTimes(1);
        expect(board.isCellEmpty(0,0)).toBe(false);
        expect(consoleSpy).not.toHaveBeenCalledWith(expect.stringContaining("Invalid"));
        expect(consoleSpy).not.toHaveBeenCalledWith(expect.stringContaining("occupied"));
    });

    it('should re-prompt for invalid format (single number) then accept valid move', () => {
        mockedReadlineSync.question
            .mockReturnValueOnce('1')        // Invalid
            .mockReturnValueOnce('0,1');     // Valid
        handlePlayerTurn(player, board);
        expect(mockedReadlineSync.question).toHaveBeenCalledTimes(2);
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Invalid format. Please use row,col"));
        expect(board.isCellEmpty(0,1)).toBe(false);
    });

    it('should re-prompt for invalid format (non-numeric) then accept valid move', () => {
        mockedReadlineSync.question
            .mockReturnValueOnce('a,b')      // Invalid
            .mockReturnValueOnce('0,2');     // Valid
        handlePlayerTurn(player, board);
        expect(mockedReadlineSync.question).toHaveBeenCalledTimes(2);
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Invalid format. Please enter numbers"));
        expect(board.isCellEmpty(0,2)).toBe(false);
    });
    
    it('should re-prompt for out-of-bounds move then accept valid move', () => {
        mockedReadlineSync.question
            .mockReturnValueOnce('-1,0')     // Invalid (out of bounds)
            .mockReturnValueOnce('3,3')      // Invalid (out of bounds)
            .mockReturnValueOnce('1,1');     // Valid
        handlePlayerTurn(player, board);
        expect(mockedReadlineSync.question).toHaveBeenCalledTimes(3);
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Invalid position."));
        expect(board.isCellEmpty(1,1)).toBe(false);
    });

    it('should re-prompt for occupied cell then accept valid move', () => {
        board.placeMark(0,0, 'O'); // Pre-occupy cell
        mockedReadlineSync.question
            .mockReturnValueOnce('0,0')      // Invalid (occupied)
            .mockReturnValueOnce('1,2');     // Valid
        handlePlayerTurn(player, board);
        expect(mockedReadlineSync.question).toHaveBeenCalledTimes(2);
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Cell already occupied."));
        expect(board.isCellEmpty(1,2)).toBe(false);
    });
});

describe('Mark Selection in startGame', () => {
    let consoleSpy: jest.SpyInstance;

    beforeEach(() => {
        consoleSpy = jest.spyOn(console, 'log').mockImplementation(() => {});
        mockedReadlineSync.question.mockReset();
    });

    afterEach(() => {
        consoleSpy.mockRestore();
    });

    it('should correctly set up players with valid mark "X"', () => {
        mockedReadlineSync.question
            .mockReturnValueOnce('Player1') // P1 Name
            .mockReturnValueOnce('X')       // P1 Mark
            .mockReturnValueOnce('Player2') // P2 Name
            // Quick game inputs to end startGame
            .mockReturnValueOnce('0,0') // P1
            .mockReturnValueOnce('1,0') // P2
            .mockReturnValueOnce('0,1') // P1
            .mockReturnValueOnce('1,1') // P2
            .mockReturnValueOnce('0,2'); // P1 wins

        startGame(); // This will run the setup and the game loop

        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Player1 uses X"));
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Player2 uses O"));
    });

    it('should correctly set up players with valid mark "o" (lowercase)', () => {
        mockedReadlineSync.question
            .mockReturnValueOnce('PlayerO') // P1 Name
            .mockReturnValueOnce('o')       // P1 Mark (lowercase)
            .mockReturnValueOnce('PlayerX') // P2 Name
            .mockReturnValueOnce('0,0') 
            .mockReturnValueOnce('1,0') 
            .mockReturnValueOnce('0,1') 
            .mockReturnValueOnce('1,1') 
            .mockReturnValueOnce('0,2');

        startGame();
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("PlayerO uses O")); // Should be uppercase
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("PlayerX uses X"));
    });
    
    it('should re-prompt for invalid mark then accept valid mark', () => {
        mockedReadlineSync.question
            .mockReturnValueOnce('P1Test')  // P1 Name
            .mockReturnValueOnce('A')       // Invalid mark
            .mockReturnValueOnce('XO')      // Invalid mark
            .mockReturnValueOnce('X')       // Valid mark
            .mockReturnValueOnce('P2Test')  // P2 Name
            .mockReturnValueOnce('0,0')
            .mockReturnValueOnce('1,0')
            .mockReturnValueOnce('0,1')
            .mockReturnValueOnce('1,1')
            .mockReturnValueOnce('0,2');

        startGame();

        // Check that the "Invalid mark" message was logged
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("Invalid mark. Please choose X or O."));
        // Check that it was called for each invalid attempt (A and XO)
        // This is a bit tricky as other console.logs happen. A more robust way would be to count specific calls.
        // For simplicity, we check it was called at least once. A more precise check:
        const logCalls = consoleSpy.mock.calls;
        let invalidMarkLogCount = 0;
        for (const call of logCalls) {
            if (typeof call[0] === 'string' && call[0].includes("Invalid mark. Please choose X or O.")) {
                invalidMarkLogCount++;
            }
        }
        expect(invalidMarkLogCount).toBe(2); // For 'A' and 'XO'

        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("P1Test uses X"));
        expect(consoleSpy).toHaveBeenCalledWith(expect.stringContaining("P2Test uses O"));
    });
});

// Example of how to check if tictactoe.ts exports necessary items
// This is more of a meta-check and not a typical unit test.
// import * as TicTacToeModule from './tictactoe';
// it('should export necessary components', () => {
//     expect(TicTacToeModule.GameBoard).toBeDefined();
//     expect(TicTacToeModule.handlePlayerTurn).toBeDefined();
//     expect(TicTacToeModule.startGame).toBeDefined();
// });

```

**A note on `tictactoe.ts` exports:**
For these tests to run, `tictactoe.ts` must export `Player`, `GameBoard`, `handlePlayerTurn`, and `startGame`. For example:
```typescript
// At the end of tictactoe.ts, or inline for classes/functions
export { Player, GameBoard, handlePlayerTurn, startGame, Mark };
// Or: export class GameBoard { ... }
// export function handlePlayerTurn(...) { ... }
// etc.
```
I have written the tests assuming these exports exist.
Also, I've added a `Mark` type export as it's used in the `GameBoard` tests.

Finally, I will update `package.json` to include a test script.

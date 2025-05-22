class Player:
    def __init__(self, name: str, mark: str) -> None:
        self.name = name
        self.mark = mark


class GameBoard(list):
    winning_combinations =[
        ['0,0', '0,1', '0,2'],
        ['1,0', '1,1', '1,2'],
        ['2,0', '2,1', '2,2'],
        ['0,0', '1,0', '2,0'],
        ['0,1', '1,1', '2,1'],
        ['0,2', '1,2', '2,2'],
        ['0,0', '1,1', '2,2'],
        ['0,2', '1,1', '2,0']
    ]
    winner = None
    def __init__(self):
        initial_matrix = [
            [None, None, None],
            [None, None, None],
            [None, None, None]
        ]
        super().__init__(initial_matrix)
    
    def __repr__(self) -> str:
        pretty_board = "    "
        for i in range(3):
            if i in [1,2]:
                pretty_board = pretty_board + "\n    " + "-"*16 + "\n    "
            for j in range(3):
                if self[i][j] is None:
                    pretty_board = pretty_board + f"{i},{j}"
                else:
                    pretty_board = pretty_board + " " + self[i][j] + " "
                if j in [0,1]:
                    pretty_board = pretty_board + " | "
        return pretty_board
    
    @property
    def status(self) -> str:
        value_O = []
        value_X = []
        free_cell_exists = False
        for i in range(3):
            for j in range(3):
                if self[i][j] == "X":
                    value_X.append(f"{i},{j}")
                elif self[i][j] == "O":
                    value_O.append(f"{i},{j}")
                if self[i][j] is None:
                    free_cell_exists = True
        for i in self.winning_combinations:
            if set(i).issubset(set(value_O)):
                self.winner = "O"
                return "Complete"
            elif set(i).issubset(set(value_X)):
                self.winner = "X"
                return "Complete"
        if not free_cell_exists:
            return "Complete"
        else:
            return "Pending"


def handle_player_turn(player: Player, board: GameBoard):
    """Handles a single player's turn, including input and validation."""
    while True:
        player_input = input(f"{player.name}: ")
        try:
            pos_i_str, pos_j_str = player_input.split(",")
            pos_i = int(pos_i_str)
            pos_j = int(pos_j_str)

            if not (0 <= pos_i <= 2 and 0 <= pos_j <= 2):
                print("Invalid position. Row and column must be between 0 and 2.")
                continue
            if board[pos_i][pos_j] is not None:
                print("Cell already occupied. Choose an empty cell.")
                continue
            
            board[pos_i][pos_j] = player.mark
            break  # Valid input, exit inner loop
        except ValueError:
            print("Invalid format. Please use row,col (e.g., 0,1).")


def setup():
    player1_name = input("Please enter a name for Player 1: ")
    player1_mark = input(f"Please choose a mark between X and O for {player1_name}: ")
    while True:
        player1_mark = player1_mark.upper()
        if player1_mark == 'X' or player1_mark == 'O':
            break
        else:
            print("Invalid mark. Please choose X or O.")
            player1_mark = input(f"Please choose a mark between X and O for {player1_name}: ")
    player2_name = input("Please enter a name for Player 2: ")
    player2_mark = "X" if player1_mark=="O" else "O"
    print(f"{player1_name} uses {player1_mark}")
    print(f"{player2_name} uses {player2_mark}")
    player1 = Player(player1_name, player1_mark)
    player2 = Player(player2_name, player2_mark)

    print("Initializing Game Board....")
    # Game board initialization
    board = GameBoard()
    print("\nGame Started.\n=============\n")
    print(board)

    print("\nChoose a position from available positions on the board.\n")

    current_player = player1
    while board.status == "Pending":
        handle_player_turn(current_player, board)
        print("\n")
        print(board)
        print("\n")

        if board.status == "Complete":
            break
        
        # Switch player
        if current_player == player1:
            current_player = player2
        else:
            current_player = player1
            
    if board.winner:
        winner = player1.name if player1.mark == board.winner else player2.name
        print(f"Congratulations! {winner} is the winner.")
    else:
        print("Game ended in a draw.")

setup()

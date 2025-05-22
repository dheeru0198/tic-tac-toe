import unittest
from unittest.mock import patch, call
import io # For capturing print statements if necessary

# Assuming game.py is in the same directory or accessible via PYTHONPATH
from game import Player, GameBoard, handle_player_turn, setup

class TestGameInputValidation(unittest.TestCase):

    def setUp(self):
        self.board = GameBoard()
        self.player1 = Player("TestPlayer1", "X")
        self.player2 = Player("TestPlayer2", "O")

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO) # To capture print statements
    def test_valid_move(self, mock_stdout, mock_input):
        mock_input.return_value = "0,0"
        handle_player_turn(self.player1, self.board)
        self.assertEqual(self.board[0][0], "X")
        self.assertEqual(mock_input.call_count, 1)

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_invalid_move_format_then_valid(self, mock_stdout, mock_input):
        mock_input.side_effect = ["1", "a,b", "0,1"] # Invalid, Invalid, Valid
        handle_player_turn(self.player1, self.board)
        self.assertEqual(self.board[0][1], "X")
        self.assertEqual(mock_input.call_count, 3)
        # Check if error messages were printed
        output = mock_stdout.getvalue()
        self.assertIn("Invalid format. Please use row,col (e.g., 0,1).", output)

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_out_of_bounds_move_then_valid(self, mock_stdout, mock_input):
        mock_input.side_effect = ["-1,0", "3,3", "0,2"] # Out of bounds, Out of bounds, Valid
        handle_player_turn(self.player1, self.board)
        self.assertEqual(self.board[0][2], "X")
        self.assertEqual(mock_input.call_count, 3)
        output = mock_stdout.getvalue()
        self.assertIn("Invalid position. Row and column must be between 0 and 2.", output)

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_occupied_cell_move_then_valid(self, mock_stdout, mock_input):
        self.board[0][0] = "O" # Pre-occupy a cell
        mock_input.side_effect = ["0,0", "1,1"] # Occupied, Valid
        handle_player_turn(self.player1, self.board)
        self.assertEqual(self.board[1][1], "X")
        self.assertEqual(mock_input.call_count, 2)
        output = mock_stdout.getvalue()
        self.assertIn("Cell already occupied. Choose an empty cell.", output)


class TestMarkSelection(unittest.TestCase):

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO) # To capture print statements during setup
    def test_valid_mark_selection_X(self, mock_stdout, mock_input):
        # Order of inputs for setup(): P1_name, P1_mark, P2_name, P1_move (if game starts)
        # We only need to test up to mark selection for this part of setup
        mock_input.side_effect = ["PlayerOne", "X", "PlayerTwo"]
        
        # To test mark selection, we need to call setup() but prevent it from running the full game loop.
        # We can patch 'handle_player_turn' to do nothing during this test.
        with patch('game.handle_player_turn'): 
            # Also need to patch GameBoard's print for cleaner test output if setup prints board
            with patch.object(GameBoard, '__repr__', return_value="Mocked Board"):
                 # Patch board status to prevent game loop from starting
                with patch.object(GameBoard, 'status', new_callable=unittest.mock.PropertyMock) as mock_status:
                    mock_status.return_value = "Complete" # Prevent game loop
                    # Call setup and then inspect the players it should have created and stored
                    # This requires setup to store players in a way we can access, or we modify setup to return them for testing
                    # For now, let's assume we can't easily get them back from setup directly without modifying setup.
                    # A better approach would be to refactor setup to return players, or test effects (like print statements)
                    
                    # Re-thinking: setup() creates players internally. To check their marks,
                    # we need to either have setup return them, or we check the print output
                    # that confirms their marks.
                    
                    # Let's check the print output for now.
                    setup_output_player1_mark = ""
                    setup_output_player2_mark = ""
                    
                    # To capture the players, we'll need to modify how `Player` instances are created or stored,
                    # or rely on print statements.
                    # Let's patch `Player.__init__` to capture the created players.
                    created_players = {}
                    original_player_init = Player.__init__
                    def mocked_player_init(self, name, mark):
                        original_player_init(self, name, mark)
                        created_players[name] = self
                    
                    with patch('game.Player.__init__', side_effect=mocked_player_init, autospec=True):
                        setup() # Call the setup function

                    self.assertIn("PlayerOne", created_players)
                    self.assertIn("PlayerTwo", created_players)
                    self.assertEqual(created_players["PlayerOne"].mark, "X")
                    self.assertEqual(created_players["PlayerTwo"].mark, "O")
                    
                    # Verify print statements
                    output = mock_stdout.getvalue()
                    self.assertIn("PlayerOne uses X", output)
                    self.assertIn("PlayerTwo uses O", output)

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_valid_mark_selection_o_lowercase(self, mock_stdout, mock_input):
        mock_input.side_effect = ["PlayerOne", "o", "PlayerTwo"]
        created_players = {}
        original_player_init = Player.__init__
        def mocked_player_init(self, name, mark):
            original_player_init(self, name, mark)
            created_players[name] = self
        
        with patch('game.Player.__init__', side_effect=mocked_player_init, autospec=True):
            with patch('game.handle_player_turn'): # Stop game loop
                 with patch.object(GameBoard, '__repr__', return_value="Mocked Board"):
                    with patch.object(GameBoard, 'status', new_callable=unittest.mock.PropertyMock) as mock_status:
                        mock_status.return_value = "Complete"
                        setup()

        self.assertEqual(created_players["PlayerOne"].mark, "O")
        self.assertEqual(created_players["PlayerTwo"].mark, "X")
        output = mock_stdout.getvalue()
        self.assertIn("PlayerOne uses O", output)
        self.assertIn("PlayerTwo uses X", output)

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_invalid_mark_then_valid_mark(self, mock_stdout, mock_input):
        # P1_name, invalid_mark, invalid_mark, valid_mark, P2_name
        mock_input.side_effect = ["PlayerOne", "A", "1", "O", "PlayerTwo"]
        
        created_players = {}
        original_player_init = Player.__init__
        def mocked_player_init(self, name, mark):
            original_player_init(self, name, mark)
            created_players[name] = self

        with patch('game.Player.__init__', side_effect=mocked_player_init, autospec=True):
            with patch('game.handle_player_turn'): # Stop game loop
                 with patch.object(GameBoard, '__repr__', return_value="Mocked Board"):
                    with patch.object(GameBoard, 'status', new_callable=unittest.mock.PropertyMock) as mock_status:
                        mock_status.return_value = "Complete"
                        setup()
        
        self.assertEqual(created_players["PlayerOne"].mark, "O")
        self.assertEqual(created_players["PlayerTwo"].mark, "X")
        
        # Check that input for P1 mark was called 3 times (1 original + 2 re-prompts)
        # The calls are: P1_name, P1_mark (1st try), P1_mark (2nd try), P1_mark (3rd try), P2_name
        # So, input() is called 5 times in total for this sequence.
        # The specific calls for mark selection are the 2nd, 3rd, and 4th calls to input().
        self.assertEqual(mock_input.call_count, 5) 
        
        # Check prompts for mark selection specifically
        # Expected calls:
        # input("Please enter a name for Player 1: ")
        # input("Please choose a mark between X and O for PlayerOne: ") # Call 1 for mark
        # input("Please choose a mark between X and O for PlayerOne: ") # Call 2 for mark
        # input("Please choose a mark between X and O for PlayerOne: ") # Call 3 for mark
        # input("Please enter a name for Player 2: ")
        mark_prompt = "Please choose a mark between X and O for PlayerOne: "
        self.assertEqual(mock_input.mock_calls[1], call(mark_prompt)) # First attempt (A)
        self.assertEqual(mock_input.mock_calls[2], call(mark_prompt)) # Second attempt (1)
        self.assertEqual(mock_input.mock_calls[3], call(mark_prompt)) # Third attempt (O)

        output = mock_stdout.getvalue()
        self.assertIn("Invalid mark. Please choose X or O.", output) # Check error message
        self.assertIn("PlayerOne uses O", output)
        self.assertIn("PlayerTwo uses X", output)

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_player2_mark_assignment_P1_is_O(self, mock_stdout, mock_input):
        mock_input.side_effect = ["P1", "O", "P2"]
        created_players = {}
        original_player_init = Player.__init__
        def mocked_player_init(self, name, mark):
            original_player_init(self, name, mark)
            created_players[name] = self

        with patch('game.Player.__init__', side_effect=mocked_player_init, autospec=True):
            with patch('game.handle_player_turn'):
                with patch.object(GameBoard, '__repr__', return_value="Mocked Board"):
                    with patch.object(GameBoard, 'status', new_callable=unittest.mock.PropertyMock) as mock_status:
                        mock_status.return_value = "Complete"
                        setup()
        self.assertEqual(created_players["P1"].mark, "O")
        self.assertEqual(created_players["P2"].mark, "X")

    @patch('builtins.input')
    @patch('sys.stdout', new_callable=io.StringIO)
    def test_player2_mark_assignment_P1_is_X(self, mock_stdout, mock_input):
        mock_input.side_effect = ["P1", "X", "P2"]
        created_players = {}
        original_player_init = Player.__init__
        def mocked_player_init(self, name, mark):
            original_player_init(self, name, mark)
            created_players[name] = self

        with patch('game.Player.__init__', side_effect=mocked_player_init, autospec=True):
            with patch('game.handle_player_turn'):
                with patch.object(GameBoard, '__repr__', return_value="Mocked Board"):
                    with patch.object(GameBoard, 'status', new_callable=unittest.mock.PropertyMock) as mock_status:
                        mock_status.return_value = "Complete"
                        setup()
        self.assertEqual(created_players["P1"].mark, "X")
        self.assertEqual(created_players["P2"].mark, "O")


if __name__ == '__main__':
    unittest.main()

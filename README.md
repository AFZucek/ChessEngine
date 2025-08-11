# ChessEngine
Chess Project I made in HS 2023. Created Engine + GUI in Java. Made AI w/ MinMax Algorithm

Goals:
  * Show inheritance between a ‘Piece’ class and each specific type of piece like Pawn
  * To create a working chess engine with graphics
  * Check to ensure user is inputting legal move
  * Create AI for user to play against

What was acheived:
  * Successfully created a chess engine 
  * Made a working AI using Minimax
  * Implemented the ‘Castle’ move in chess
  * Made an animation after one Piece takes another

Design User View:
  * Inside of the IDE the user is prompted to enter the column then row for piece they want to move
  * Then the user is prompted to enter the column and row for where they want to move the piece
  * Then the AI moves and the program prints what piece moved and where

UML Diagram:

MinMax Algorithm:
  * Recursive algorithm that evaluates all possible moves for an AI and chooses the best one
    * Performs depth-first search on game tree of possible moves
    * Maximizing vs minimizing player
    * Value maximized or minimized based on player turn at current depth
    * Position only evaluated at final depth level
  * Is Minimax efficient?
    * ~35 average branching factor for chess
      * Changes with board position
    * Time complexity is O(b^m)
      * b represents branching factor
      * m represents maximum depth

Problems encountered:
  * When creating MiniMax needed way to look at future moves without moving Pieces on displayed board
  * Accidently ordered rows wrong
  * Implementing castle was difficult
  * Rook on left side randomly moving
  * Bad at endgame, especially with low depth

What was learned?
  * Using ACM graphics to add images, display game pieces, and show animations
  * Creating a fully functioning chess game with user input
  * Properly implementing complex chess logic
  * Implementing minimax and analyzing its efficiency

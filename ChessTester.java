/**
 * @author Addisons
 * Import acm for graphics and Scanner
 */

import acm.program.GraphicsProgram;
import acm.graphics.GLabel;
import acm.graphics.GImage;
import acm.graphics.GRect;
import java.awt.Color;
import java.util.Scanner;


/**
 * ChessTester class handles creation of board, user interface, and other universally used methods
 * ex: checkmate method and mimiMax are in this class
 * Keeps track of variables like number of pieces killed
 */
public class ChessTester extends GraphicsProgram{


    private static final int NROWS = 8;
    private static final int NCOLUMNS = 8;
    final int LEFT_DES_GAP= 25; // gap between left side, and description
    final int DES_BOARD_GAP = 15; // gap between description and board
    private boolean whiteTurn = true;

    private static final Piece[][] board = new Piece[8][8];
    private static double sqSize;
    private int checkPieceCol;
    private int checkPieceRow;
    private static int numKilled = 0;
    private String turnString;
    private int depth = 3;
    private int numPieces = 0;

    public static void main(String[] args){
        new ChessTester().start(args);
    }

    /**
     * Method that runs when program runs
     * handles UI and calls all methods needed to display program
     */
    public void run() {

        Player p1 = new Player(true);
        AI p2 = new AI(false);

        displayBoard();
        displayPieces(p1, p2);
        boolean play = true;
        String key = "abcdefgh";
        Scanner scan = new Scanner(System.in);
        boolean notEntered = true;
        String chosenDif = "";

        System.out.println("In this chess game you will play an AI. \nYou will enter your moves below" +
                           " in the IDE and the board will display the moves");

        while(notEntered){
            System.out.println("Enter 1 for Easy/Fast mode, Enter 2 for Hard/Slow mode");
            chosenDif = scan.nextLine();
            if (chosenDif.equals("1") || chosenDif.equals("2")) notEntered = false;
            if (notEntered) System.out.println("Incorrect Entry");
        }
        changeDepth(Integer.valueOf(chosenDif) + 1);


        while (play) {

            if (!whiteTurn){
                turnString = "AI";
                GLabel turnLabel = new GLabel(turnString + "'s turn");
                add(turnLabel, sqSize * 10, sqSize * 2);

                int bestPieceCol = 0;
                int bestPieceRow = 0;
                int bestNewCol = 0;
                int bestNewRow = 0;
                int bestMove = -Integer.MAX_VALUE;
                for (int c = 0; c < NCOLUMNS; c++){
                    for (int r = 0; r < NCOLUMNS; r++){
                        if (board[c][r] != null ) numPieces++;
                        if (board[c][r] != null && !board[c][r].getIsWhite()){
                            for (int cnew = 0; cnew < NCOLUMNS; cnew++) {
                                for (int rnew = 0; rnew < NROWS; rnew++) {
                                    if (board[c][r].canMove(cnew, rnew, board)){
                                        Piece[][] newB = copyBoard(board);
                                        newB[c][r].testMove(cnew, rnew, newB);
                                        int miniMaxVal = miniMax(newB, depth, false);
                                        if (miniMaxVal > bestMove){
                                            bestPieceCol = c;
                                            bestPieceRow = r;
                                            bestNewCol = cnew;
                                            bestNewRow = rnew;
                                            bestMove = miniMaxVal;
                                        }
                                        newB[cnew][rnew].testMove(c, r, newB); //move back
                                    }
                                }
                            }
                        }

                    }
                }
                if (numPieces < 10) depth = 3;
                numPieces = 0;

                char movedTo = key.charAt(bestNewCol);
                System.out.println("Piece " + ChessTester.getBoard()[bestPieceCol][bestPieceRow].getClass().getName() +
                                    " has moved to " + movedTo + " " + (NROWS-bestNewRow)); //-8 for conversion
                ChessTester.getBoard()[bestPieceCol][bestPieceRow].move(bestNewCol, bestNewRow);

                if (checkMate((King) p1.getPieces()[12], board)){
                    play = false;
                    System.out.println("Checkmate! AI Won!!");
                }
                whiteTurn = true;
                remove(turnLabel);

            }
            turnString = "white";
            GLabel turnLabel = new GLabel(turnString + "'s turn");
            add(turnLabel, sqSize * 10, sqSize * 2);

            if (getWhiteKing(board) == null){
                System.out.println("You looooost :(");
                break;
            }

            System.out.println("Enter column and row for piece you want to move (ex: a7)");
            String chosenPiece = scan.nextLine();
            System.out.println("Enter column and row or where you want to move it (ex: a5)");
            String location = scan.nextLine();

            if (chosenPiece.length() < 2 || location.length() < 2){
                System.out.println("Please enter correct value");
            }

            else {

                int row = NROWS - Character.getNumericValue(chosenPiece.charAt(1)); //conversion
                int column = key.indexOf(chosenPiece.charAt(0));

                int newRow = NCOLUMNS - Character.getNumericValue(location.charAt(1)); // conversion
                int newColumn = key.indexOf(location.charAt(0));


                boolean caughtE = false;
                try {
                    board[column][row].getClass();
                } catch (Exception e) {
                    caughtE = true;
                    System.out.println("You did not input correctly, try again");
                    whiteTurn = !whiteTurn;
                }


                if (!caughtE && board[column][row] == null) {
                    System.out.println("Pick a square occupied by a Piece");
                    whiteTurn = !whiteTurn;
                }


                if (!caughtE && board[column][row] != null) {
                    if (!board[column][row].getIsWhite() && whiteTurn
                            || board[column][row].getIsWhite() && !whiteTurn) {
                        System.out.println("It is " + turnString + "'s turn, try again");
                        whiteTurn = !whiteTurn;
                    } else if (board[column][row].canMove(newColumn, newRow, board)) {
                        board[column][row].move(newColumn, newRow);
                    } else {
                        whiteTurn = !whiteTurn;
                        System.out.println("Please enter a legal move");
                    }
                }

                remove(turnLabel);
                whiteTurn = !whiteTurn;
                if (checkMate((King) p2.getPieces()[12], board)) {
                    play = false;
                    System.out.println("CheckMate!! you Won!! ");
                }

            }
        }
    }

    /**
     * Changes depth for minimax
     * @param newDepth
     */
    public void changeDepth(int newDepth){
        depth = newDepth;
    }

    /**
     * Evaluates board based on number of white + black pieces
     * more positive number is good for white
     * more negative number is good for black
     * @param boardNew takes a board as an input
     * @return alue of evaluated board
     */
    public int evaluateBoard(Piece[][] boardNew){
        int val = 0;
        int checkMateVal = 1000;
        int checkVal = 5;
        int positionVal = 6;
        int pawnVal = 4;
        int bishopval = 12;
        int knightval = 12;
        int rookVal = 24;
        int queenVal = 50;
        int kingVal = 1000000;
        int pawnMidRow = 3;
        int leftMidCol = 3;
        int rightMidCol = 4;

        if (getBlackKing(boardNew) != null){
            if (getBlackKing(boardNew).getRow() == 0) val+=positionVal;
            if (inCheck(getBlackKing(boardNew), getBlackKing(boardNew).getColumn(), getBlackKing(boardNew).getRow(), boardNew)){
                val -= checkVal;
                if (checkMate(getBlackKing(boardNew), boardNew)) val -= Integer.MAX_VALUE;
            }
        }
        if (getWhiteKing(boardNew) != null){
            if (inCheck(getWhiteKing(boardNew), getWhiteKing(boardNew).getColumn(), getWhiteKing(boardNew).getRow(), boardNew)){
                val += checkVal;
                if (checkMate(getWhiteKing(boardNew), boardNew)) val += checkMateVal;
            }
        }

        if (boardNew[leftMidCol][pawnMidRow] != null && !boardNew[leftMidCol][pawnMidRow].getIsWhite()) val += positionVal;
        if (boardNew[rightMidCol][pawnMidRow] != null && !boardNew[rightMidCol][pawnMidRow].getIsWhite()) val += positionVal;

        for (Piece[] x: boardNew) {
            for (Piece j : x) {
                if (j!= null && !j.getIsWhite()){
                    if (j.getClass().getName().equals("Pawn")) val += pawnVal;
                    else if (j.getClass().getName().equals("Bishop")) val += bishopval;
                    else if (j.getClass().getName().equals("Knight")) val += knightval;
                    else if (j.getClass().getName().equals("Rook")) val += rookVal;
                    else if (j.getClass().getName().equals("King")) val += Integer.MAX_VALUE;
                    else if (j.getClass().getName().equals("Queen")) val += queenVal;
                }
                else if (j!= null && j.getIsWhite()){
                    if (j.getClass().getName().equals("Pawn")) val -= pawnVal;
                    else if (j.getClass().getName().equals("Bishop")) val -= bishopval;
                    else if (j.getClass().getName().equals("Knight")) val -= knightval;
                    else if (j.getClass().getName().equals("Rook")) val -= rookVal;
                    else if (j.getClass().getName().equals("King")) val -= kingVal;
                    else if (j.getClass().getName().equals("Queen")) val -= queenVal;
                }
            }
        }
        return val;
    }

    /**
     * Used to determine if game is over
     * @param boardinput
     * @return boolean if whiteKing is on board
     */
    public King getWhiteKing(Piece[][] boardinput){
        for (Piece[] x : boardinput){
            for (Piece j: x){
                if (j != null && j.getClass().getName().equals("King") && j.getIsWhite()) return (King)j;
            }
        }
        return null;
    }

    /**
     * Used to determine if game is over
     * @param boardinput
     * @return boolean if blackKing is on board
     */
    public King getBlackKing(Piece[][] boardinput){
        for (Piece[] x : boardinput){
            for (Piece j: x){
                if (j != null && j.getClass().getName().equals("King") && !j.getIsWhite()) return (King)j;
            }
        }
        return null;
    }

    /**
     * Copies values of an array of Pieces
     * Used in minimax to map out boardValue after different moves
     * @param oldBoard
     * @return newBoard
     */
    public Piece[][] copyBoard(Piece[][] oldBoard){
        Piece[][] newBoard = new Piece[NCOLUMNS][NCOLUMNS];
        for (int c = 0; c < NCOLUMNS; c++){
            for (int r = 0; r < NROWS; r++){
                newBoard[c][r] = oldBoard[c][r];
            }
        }
        return newBoard;
    }

    /**
     * Recursively calls itself until depth is zero to evaluate value of possible moves in the future
     * Tests each possible move then finds the 'best' move and evaluates the other teams response
     * It returns the value of the board after the best possible string of moves
     * @param boardNode
     * @param depth
     * @param maximizingPlayer In this program black/ai is always maximizing player
     * @return boardValue after the best possible string of moves
     */
    public int miniMax( Piece[][] boardNode, int depth, boolean maximizingPlayer){
        int bestVal;
        if (depth == 0) return evaluateBoard(boardNode);
        if (maximizingPlayer){
            bestVal = Integer.MIN_VALUE;
            for (Piece[] x: boardNode){
                for (Piece j: x){
                    if (j != null && !j.getIsWhite()){
                        for (int c = 0; c < NCOLUMNS; c++){
                            for (int r = 0; r < NROWS; r++){
                                Piece[][] newBoard = copyBoard(boardNode);
                                if (j.canMove(c, r, newBoard) && !(c== j.getColumn() && r== j.getRow())){
                                    int savedJCol = j.getColumn();
                                    int savedJRow = j.getRow();
                                    newBoard[j.getColumn()][j.getRow()].testMove(c,r, newBoard);
                                    int val = miniMax(newBoard, depth - 1, false);
                                    if (val > bestVal) bestVal = val;
                                    //then need to return Piece back
                                    newBoard[j.getColumn()][j.getRow()].testMove(savedJCol,savedJRow, newBoard);
                                }
                            }
                        }
                    }
                }
            }
            return bestVal;
        }
        else {
            bestVal = Integer.MAX_VALUE;
            for (Piece[] x: boardNode){
                for (Piece j: x){
                    if (j != null && j.getIsWhite()){
                        for (int c = 0; c < NCOLUMNS; c++){
                            for (int r = 0; r < NROWS; r++){
                                Piece[][] newBoard = copyBoard(boardNode);
                                if (j.canMove(c, r, newBoard) && !(c== j.getColumn() && r== j.getRow())){
                                    int savedJCol = j.getColumn();
                                    int savedJRow = j.getRow();
                                    newBoard[j.getColumn()][j.getRow()].testMove(c,r, newBoard);
                                    int val = miniMax(newBoard, depth - 1, true);
                                    if (val < bestVal) bestVal = val;
                                    //to return piece back
                                    newBoard[j.getColumn()][j.getRow()].testMove(savedJCol,savedJRow, newBoard);
                                }
                            }
                        }
                    }
                }
            }
            return bestVal;
        }
    }


    /**
     * Displays tiles on board
     * Also labels rows and columns
     */
    public void displayBoard(){
        String COLUMN_DES = "abcdefgh";
        String ROW_DES = "87654321";
        int brownRedVal = 198;
        int brownGreenVal = 162;
        int brownBlueVal = 126;

        sqSize = (double) (getHeight()-LEFT_DES_GAP) / NROWS;

        for (int i = 0; i < NROWS; i++){
            GLabel columnLabel = new GLabel(COLUMN_DES.charAt(i) + "");
            add(columnLabel, (i*sqSize) + sqSize/2 + LEFT_DES_GAP, sqSize*NROWS + DES_BOARD_GAP);

            GLabel rowLabel = new GLabel(ROW_DES.charAt(i) + "");
            add(rowLabel, LEFT_DES_GAP - DES_BOARD_GAP, (i*sqSize) + sqSize/2);

        }

        for (int i = 0; i < NROWS; i++) {
            for (int j = 0; j < NCOLUMNS; j++) {
                double x = j * sqSize + LEFT_DES_GAP;
                double y = i * sqSize;
                GRect sq = new GRect(x, y, sqSize, sqSize);

                Color brown = new Color(brownRedVal, brownGreenVal, brownBlueVal);

                sq.setFillColor(brown);
                sq.setFilled((i + j) % 2 != 0);
                add(sq);
            }
        }
    }

    /**
     * Takes two players as input
     * Displays their pieces on the board by accessing their images
     * @param p1
     * @param p2
     */
    public void displayPieces(Player p1, Player p2){

        final int PX_SHIFT_VAL = 22;
        if (!p1.getPieces()[0].getIsWhite()){
            for (int i = 0; i < NCOLUMNS; i++){
                add(p1.getPieces()[i].getImg(), i*sqSize + PX_SHIFT_VAL, sqSize);
                add(p2.getPieces()[i].getImg(), i*sqSize + PX_SHIFT_VAL, sqSize*(NCOLUMNS-2));
                board[i][1] = p1.getPieces()[i];
                board[i][6] = p2.getPieces()[i];
            }
            for (int i = 0; i < NCOLUMNS; i++){
                add(p1.getPieces()[i + NCOLUMNS].getImg(), i*sqSize + PX_SHIFT_VAL, 0);
                add(p2.getPieces()[i + NCOLUMNS].getImg(), i*sqSize + PX_SHIFT_VAL, sqSize*(NCOLUMNS - 1));
                board[i][0] = p1.getPieces()[i + NCOLUMNS];
                board[i][7] = p2.getPieces()[i + NCOLUMNS];
            }
        }
        else {
            for (int i = 0; i < NCOLUMNS; i++){
                add(p2.getPieces()[i].getImg(), i*sqSize + PX_SHIFT_VAL, sqSize);
                add(p1.getPieces()[i].getImg(), i*sqSize + PX_SHIFT_VAL, sqSize*(NCOLUMNS-2));
                board[i][1] = p2.getPieces()[i];
                board[i][6] = p1.getPieces()[i];
            }
            for (int i = 0; i < NCOLUMNS; i++){
                add(p2.getPieces()[i + NCOLUMNS].getImg(), i*sqSize + PX_SHIFT_VAL, 0);
                add(p1.getPieces()[i + NCOLUMNS].getImg(), i*sqSize + PX_SHIFT_VAL, sqSize*(NCOLUMNS-1));
                board[i][0] = p2.getPieces()[i + NCOLUMNS];
                board[i][7] = p1.getPieces()[i + NCOLUMNS];
            }
        }
    }


    /**
     * Takes king and its column and row as input
     * checks if king on boarInput is inCheck
     * @param k
     * @param col
     * @param row
     * @param boardInput
     * @return boolean if king is in check or not
     */
    public boolean inCheck(King k, int col, int row, Piece[][] boardInput) {
        for (Piece[] x: boardInput){
            for (Piece j: x){
                if (j != null && j.getIsWhite() != k.getIsWhite() && !j.getClass().getName().equals("King")){
                    if (j.canMove(col, row, boardInput)){
                        checkPieceCol = j.getColumn();
                        checkPieceRow = j.getRow();

                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Takes king as input determines if it is in checkmate
     * Has a lot of loops to check if any pieces can block the checkmate
     * @param k
     * @param boardInput
     * @return boolean if king is in checkmate
     */
    public boolean checkMate(King k, Piece[][] boardInput){
        boolean inCheckMate = false;

        if (inCheck(k, k.getColumn(), k.getRow(), boardInput)){
            inCheckMate = true;
            for (int r = 0; r < NROWS; r++){
                for (int c = 0; c < NCOLUMNS; c++){
                    if (boardInput[c][r] == null ||
                            boardInput[c][r].getIsWhite() != k.getIsWhite()){
                        if (Math.abs(r-k.getRow()) < 2 && Math.abs(c-k.getColumn()) < 2){
                            if (k.canMove(c, r, boardInput)){
                                return false;
                            }
                        }
                    }
                }
            }
        }

        for (int r = 0; r < NROWS; r++) {
            for (int c = 0; c < NCOLUMNS; c++) {
                if (boardInput[c][r] != null &&
                        boardInput[c][r].getIsWhite() == k.getIsWhite()){
                    if (boardInput[c][r].canMove(checkPieceCol, checkPieceRow, boardInput)) inCheckMate = false;

                    if (boardInput[checkPieceCol][checkPieceRow] != null && //check for when I make piece null during king can move
                        !boardInput[checkPieceCol][checkPieceRow].getClass().getName().equals("Knight") &&
                        !boardInput[c][r].getClass().getName().equals("King")){
                        if (boardInput[checkPieceCol][checkPieceRow].getClass().getName().equals("Rook")){
                            if (checkPieceCol > k.getColumn()){
                                for (int coll = checkPieceCol; coll > k.getColumn(); coll--){
                                    if (boardInput[c][r].canMove(coll, checkPieceRow, boardInput)) inCheckMate = false;
                                }
                            }
                            if (checkPieceCol < k.getColumn()){
                                for (int coll = k.getColumn(); coll < k.getColumn(); coll++){
                                    if (boardInput[c][r].canMove(coll, checkPieceRow, boardInput)) inCheckMate = false;
                                }
                            }
                            if (checkPieceRow > k.getRow()){
                                for (int roww = checkPieceRow; roww > k.getRow(); roww--){
                                    if (boardInput[c][r].canMove(checkPieceCol, roww, boardInput)) inCheckMate = false;
                                }
                            }
                            if (checkPieceRow < k.getRow()){
                                for (int roww = k.getRow(); roww < k.getRow(); roww++){
                                    if (boardInput[c][r].canMove(checkPieceCol, roww, boardInput)) inCheckMate = false;
                                }
                            }
                        }

                        int coll;
                        if (checkPieceRow > k.getRow()){
                            if (checkPieceCol > k.getColumn()){
                                coll = checkPieceCol;
                                for (int roww = checkPieceRow; roww > k.getRow(); roww--){
                                    if (coll > -1 && coll < NCOLUMNS && roww > -1 && roww < NROWS && boardInput[c][r].canMove(coll, roww, boardInput)){ //lokey sketch checks
                                        inCheckMate = false;
                                    }
                                    coll--;
                                }
                            }
                            if (checkPieceCol < k.getColumn()){
                                coll = k.getColumn();
                                for (int roww = checkPieceRow; roww > k.getRow(); roww--){
                                    if (coll > -1 && coll < NCOLUMNS && roww > -1 && roww < NROWS && boardInput[c][r].canMove(coll, roww, boardInput)){
                                        inCheckMate = false;
                                    }
                                    coll++;
                                }
                            }
                        }

                        if (checkPieceRow < k.getRow()){
                            if (checkPieceCol > k.getColumn()){
                                coll = k.getColumn();
                                for (int roww = k.getRow(); roww > checkPieceRow; roww--){
                                    if (coll > -1 && coll < NCOLUMNS && roww > -1 && roww < NROWS && boardInput[c][r].canMove(coll, roww, boardInput)){
                                        inCheckMate = false;
                                    }
                                    coll++;
                                }
                            }
                            if (checkPieceCol < k.getColumn()){
                                coll = k.getColumn();
                                for (int roww = k.getRow(); roww > checkPieceRow && coll > 0; roww--){
                                    if (coll > -1 && coll < NCOLUMNS && roww > -1 && roww < NROWS && boardInput[c][r].canMove(coll, roww, boardInput)){
                                        inCheckMate = false;
                                    }
                                    coll--;
                                }
                            }
                        }
                    }

                }
            }
        }
        return inCheckMate;
    }

    /**
     * Simply changes a column and row on a board to specified piece
     * @param column
     * @param row
     * @param p
     * @param boardToChange
     */
    public static void changeBoard(int column, int row, Piece p, Piece[][] boardToChange){
        boardToChange[column][row] = p;
    }

    /**
     * @return displayed chess board
     */
    public static Piece[][] getBoard(){
        return board;
    }
    public static double getSqSize() {return sqSize;}
    public static int getKilled() { return numKilled;}
    public static void upKills() {numKilled++;}
    public static int getNumCols(){ return NCOLUMNS;}

}

/**
 * Player class holds pieces of the player
 */
class Player{
    private final boolean isWhite;
    Piece[] pieces = new Piece[16];

    public Player(boolean isWhite){
        this.isWhite = isWhite;
        if (isWhite){
            for (int i = 0; i < ChessTester.getNumCols(); i++) pieces[i] = new Pawn(isWhite, i, 6);
            pieces[8] = new Rook(isWhite, 0, 7);
            pieces[9] = new Knight(isWhite, 1, 7);
            pieces[10] = new Bishop(isWhite, 2, 7);
            pieces[11] = new Queen(isWhite, 3, 7);
            pieces[12] = new King(isWhite, 4, 7);
            pieces[13] = new Bishop(isWhite, 5, 7);
            pieces[14] = new Knight(isWhite, 6, 7);
            pieces[15] = new Rook(isWhite, 7, 7);
        }
        else {
            for (int i = 0; i < ChessTester.getNumCols(); i++) pieces[i] = new Pawn(isWhite, i, 1);
            pieces[8] = new Rook(isWhite, 0, 0);
            pieces[9] = new Knight(isWhite, 1, 0);
            pieces[10] = new Bishop(isWhite, 2, 0);
            pieces[11] = new Queen(isWhite, 3, 0);
            pieces[12] = new King(isWhite, 4, 0);
            pieces[13] = new Bishop(isWhite, 5, 0);
            pieces[14] = new Knight(isWhite, 6, 0);
            pieces[15] = new Rook(isWhite, 7, 0);
        }


    }

    /**
     * @return players pieces
     */
    public Piece[] getPieces(){
        return pieces;
    }
}

/**
 * Originally though there would be more difference
 * Just became a way to differentiate AI from Player
 */
class AI extends Player{
    public AI(boolean isWhite) {
        super(isWhite);
    }
}

/**
 * Class holds row and column of each piece
 * Also holds boolean if the piece is white or black
 * Has methods to move each piece both visually and on the 2D array
 */
abstract class Piece{
    final private boolean isWhite;
    private GImage image;
    private int row;
    private int column;
    private boolean moved;

    public Piece(boolean isWhite, int column, int row){
        this.isWhite = isWhite;
        this.column = column;
        this.row = row;
        moved = false;
    }
    public void setImg(GImage image){
        this.image = image;
    }

    public GImage getImg(){
        return image;
    }

    public boolean getIsWhite(){
        return isWhite;
    }
    public int getColumn() {return column;}
    public void changeCol(int c){ column = c;}
    public void changeRow(int r){ row = r;}
    public int getRow() {return row;}
    public boolean hasMoved() {return moved;}
    public abstract boolean canMove(int newColumn, int newRow, Piece[][] boardInput);

    /**
     * Tests a move on a new Board without moving it graphically
     * @param newColumn
     * @param newRow
     * @param boardInput
     */
    public void testMove(int newColumn, int newRow, Piece[][] boardInput){
        if (boardInput[newColumn][newRow]!=null){
            boardInput[newColumn][newRow].testKill(boardInput);
        }

        ChessTester.changeBoard(column, row, null, boardInput);
        ChessTester.changeBoard(newColumn, newRow, this, boardInput);
        row = newRow;
        column = newColumn;
    }

    /**
     * Moves Piece graphically
     * If it is a king it checks if it is moving to 'Castle'
     * @param newColumn
     * @param newRow
     */
    public void move( int newColumn, int newRow){

        if (this.getClass().getName().equals("King")){
            if (newColumn - column == 2) ChessTester.getBoard()[newColumn + 1][newRow].move(column+ 1, row);
            else if (column - newColumn == 2) ChessTester.getBoard()[newColumn - 2][newRow].move(column - 1, row);
        }
        moved = true;
        int speed = 3000000*((int) Math.sqrt(Math.abs(newColumn-column) + Math.abs(newRow-row)));

        for (int i = 0; i < speed; i++){
            image.move(((newColumn-column)*ChessTester.getSqSize())/speed,
                    ((newRow-row)*ChessTester.getSqSize())/speed);
        }

        if (ChessTester.getBoard()[newColumn][newRow]!=null){
            ChessTester.getBoard()[newColumn][newRow].kill();
        }

        ChessTester.changeBoard(column, row,null, ChessTester.getBoard());
        ChessTester.changeBoard(newColumn, newRow, this, ChessTester.getBoard());
        row = newRow;
        column = newColumn;
    }

    /**
     * Handles animation when one Piece takes another
     */
    public void kill(){
        int colConstant= 0;
        int speedCons = 10000000;
        int yCons = 150;
        int xConst1 = 10;
        int yConst2 = 5;
        int yConst3 = 40;

        if (isWhite) colConstant = 30;
        for (int i = 0; i < speedCons; i++){
            image.move((((xConst1-column)*ChessTester.getSqSize() + colConstant)/speedCons),
                    ((((yConst2-row)*ChessTester.getSqSize()) -yCons + (ChessTester.getKilled()*yConst3)))/speedCons);
        }
        ChessTester.changeBoard(column, row,null, ChessTester.getBoard());
        ChessTester.upKills();
    }

    /**
     * Is used to test a kill on different board without animation
     * @param boardInput
     */
    public void testKill(Piece[][] boardInput){
        ChessTester.changeBoard(column, row,null, boardInput);
    }
}

/**
 * Pawn class holds image of pawn
 */
class Pawn extends Piece{

    final private GImage image;
    private boolean isWhite;

    // need to make image part of piece class so that I can move it in piece classes

    public Pawn(boolean isWhite, int column, int row){
        super(isWhite, column, row);
        this.isWhite = isWhite;

        if (isWhite){
            image = new GImage("https://upload.wikimedia.org/wikipedia/commons/0/04/Chess_plt60.png");
        }
        else image = new GImage("https://upload.wikimedia.org/wikipedia/commons/c/cd/Chess_pdt60.png");

        super.setImg(image);
    }

    /**
     * specifically has to check if it can move 2 squares at start
     * also has to check if it can move diagonally when an opposing Piece is there
     * @param newColumn
     * @param newRow
     * @param boardInput
     * @return boolean if pawn can move to new tile
     */
    public boolean canMove( int newColumn, int newRow, Piece[][] boardInput ){
        int whiteStartingRow = 6;
        int blackStartingRow = 1;

        if (newColumn == getColumn() && newRow == getRow()) return false;
        if (newColumn == getColumn() && boardInput[newColumn][newRow] != null){
            return false;
        }
        if (isWhite){
            if (getRow() < newRow) return false;
            if (getRow() == whiteStartingRow && getRow() - newRow > 2) return false;
            if (getRow() != whiteStartingRow && getRow() - newRow > 1) return false;
            if (getRow()-newRow == 2){
                if (getRow() > 0 && boardInput[newColumn][getRow()-1] != null) {
                    return false;
                }
            }
        }

        else {
            if (getRow() > newRow) return false;
            if (getRow() == blackStartingRow && newRow - getRow() > 2) return false;
            if (getRow() != blackStartingRow && newRow - getRow() > 1) return false;
            if (newRow - getRow() == 2){
                if (getRow() > 0 && boardInput[newColumn][getRow()+1] != null) {
                    return false;
                }
            }
        }

        if (Math.abs(newColumn-getColumn()) == 1 && Math.abs(newRow-getRow()) == 1){
            if (boardInput[newColumn][newRow] != null){
                if (boardInput[newColumn][newRow].getIsWhite() != isWhite){
                    return true;
                }
            }
        }
        if (newColumn != getColumn()){
            return false;
        }
        return true;
    }

}

/**
 * Holds image for Knight
 * Also has separate move method because it looks cooler if knight does not move diagonally
 */
class Knight extends Piece{
    final private GImage image;
    private boolean isWhite;

    public Knight(boolean isWhite, int column, int row){
        super(isWhite, column, row);
        this.isWhite = isWhite;

        if (isWhite){
            image = new GImage("https://upload.wikimedia.org/wikipedia/commons/2/28/Chess_nlt60.png");
        }
        else image = new GImage("https://upload.wikimedia.org/wikipedia/commons/f/f1/Chess_ndt60.png");

        super.setImg(image);
    }

    public void move( int newColumn, int newRow){
        int speedConst = 3000000;

        for (int i = 0; i < speedConst; i++){
            image.move(0, ((newRow-getRow())*ChessTester.getSqSize())/speedConst);
        }
        for (int i = 0; i < speedConst; i++){
            image.move(((newColumn-getColumn())*ChessTester.getSqSize())/speedConst, 0);
        }

        if (ChessTester.getBoard()[newColumn][newRow]!=null){
            ChessTester.getBoard()[newColumn][newRow].kill();
        }

        ChessTester.changeBoard(getColumn(), getRow(), null, ChessTester.getBoard());
        ChessTester.changeBoard(newColumn, newRow, this, ChessTester.getBoard());

        changeRow(newRow);
        changeCol(newColumn);
    }

    /**
     * Does not have to check if pieces are in the way of it moving
     * Just checks to make sure that it is moving 2 units then 1 in any direction
     * @param newColumn
     * @param newRow
     * @param boardInput
     * @return
     */
    public boolean canMove( int newColumn, int newRow, Piece[][] boardInput ){
        if (newColumn == getColumn() && newRow == getRow()) return false;
        if (Math.abs(newColumn - getColumn()) != 1 &&
            Math.abs(newRow - getRow()) != 1) return false;

        if (Math.abs(newColumn - getColumn()) != 2 &&
                Math.abs(newRow - getRow()) != 2) return false;


        if (Math.abs(newColumn - getColumn()) == 1){
            if (Math.abs(newRow - getRow()) != 2) return false;
        }
        if (Math.abs(newColumn - getColumn()) == 2){
            if (Math.abs(newRow - getRow()) != 1) return false;
        }


        if (isWhite){
            if (boardInput[newColumn][newRow] != null) {
                if (boardInput[newColumn][newRow].getIsWhite()) {
                    return false;
                }
            }
        }
        else {
            if (boardInput[newColumn][newRow] != null) {
                if (!boardInput[newColumn][newRow].getIsWhite()) {
                    return false;
                }
            }
        }
        return true;
    }
}

/**
 * Holds image for Bishop
 */

class Bishop extends Piece{
    final private GImage image;
    private boolean isWhite;
    public Bishop(boolean isWhite, int column, int row){
        super(isWhite, column, row);
        this.isWhite = isWhite;;

        if (isWhite){
            image = new GImage("https://upload.wikimedia.org/wikipedia/commons/9/9b/Chess_blt60.png");
        }
        else image = new GImage("https://upload.wikimedia.org/wikipedia/commons/8/81/Chess_bdt60.png");

        super.setImg(image);

    }

    /**
     * Makes sure move is diagonal
     * Also checks if pieces are blocking move
     * @param newColumn
     * @param newRow
     * @param boardInput
     * @return boolean if can move
     */
    public boolean canMove( int newColumn, int newRow, Piece[][] boardInput ){
        if (newColumn == getColumn() && newRow == getRow()) return false;
        int rowC = 0;
        if (newRow > getRow()) rowC = 1;
        if (newRow < getRow()) rowC = -1;

        if ((Math.abs(newColumn-getColumn()) != Math.abs(newRow-getRow()))){
            return false;
        }

        if (newColumn > getColumn()){
            int j = getRow() + rowC;
            for (int i = getColumn() + 1; i < newColumn; i++){
                if (boardInput[i][j] != null){
                    return false;
                }
                j += rowC;
            }
        }
        else {
            int j = getRow() + rowC;
            for (int i = getColumn() - 1; i > newColumn; i--){
                if (boardInput[i][j] != null){
                    return false;
                }
                j += rowC;
            }
        }

        if (boardInput[newColumn][newRow] != null) {
            if (!boardInput[newColumn][newRow].getIsWhite() && !isWhite) {
                return false;
            }
            if (boardInput[newColumn][newRow].getIsWhite() && isWhite){
                return false;
            }
        }
        return true;
    }
}

/**
 * Holds image for Rook
 */
class Rook extends Piece{
    final private GImage image;
    private boolean isWhite;

    public Rook(boolean isWhite, int column, int row){
        super(isWhite, column, row);
        this.isWhite = isWhite;

        if (isWhite){
            image = new GImage("https://upload.wikimedia.org/wikipedia/commons/5/5c/Chess_rlt60.png");
        }
        else image = new GImage("https://upload.wikimedia.org/wikipedia/commons/a/a0/Chess_rdt60.png");

        super.setImg(image);
    }

    /**
     * Makes sure either col or row stayed the same
     * Checks if anything is blocking the move
     * @param newColumn
     * @param newRow
     * @param boardInput
     * @return boolean if can move
     */
    public boolean canMove( int newColumn, int newRow, Piece[][] boardInput ){
        if (newColumn == getColumn() && newRow == getRow()) return false;
        if (newColumn != getColumn() && newRow != getRow()) return false;

        if (boardInput[newColumn][newRow]!= null){
            if (boardInput[newColumn][newRow].getIsWhite() == isWhite){
                return false;
            }
        }

        if (newColumn > getColumn()){
            for (int i = getColumn() + 1; i < newColumn; i++){
                if (boardInput[i][getRow()]!= null) return false;
            }
        }
        else if (newColumn < getColumn()){
            for (int i = getColumn() - 1; i > newColumn; i--){
                if (boardInput[i][getRow()]!= null) return false;
            }
        }
        else if (newRow > getRow()){
            for (int i = getRow() + 1; i < newRow; i++){
                if (boardInput[getColumn()][i]!= null) return false;
            }
        }
        else if (newRow < getRow()){
            for (int i = getRow() - 1; i > newRow; i--){
                if (boardInput[getColumn()][i]!= null) return false;
            }
        }
        return true;
    }
}

/**
 * Holds image for queen
 */
class Queen extends Piece{
    final private GImage image;
    private boolean isWhite;

    public Queen(boolean isWhite, int column, int row){
        super(isWhite, column, row);
        this.isWhite = isWhite;

        if (isWhite){
            image = new GImage("https://upload.wikimedia.org/wikipedia/commons/4/49/Chess_qlt60.png");
        }
        else image = new GImage("https://upload.wikimedia.org/wikipedia/commons/a/af/Chess_qdt60.png");

        super.setImg(image);

    }

    /**
     * Almost a combination of bishop and Rook canMove methods
     * @param newColumn
     * @param newRow
     * @param boardInput
     * @return boolean if can move
     */
    public boolean canMove( int newColumn, int newRow, Piece[][] boardInput ){
        if (newColumn == getColumn() && newRow == getRow()) return false;
        if (Math.abs(newColumn-getColumn()) != Math.abs(newRow - getRow())){
            if (getColumn() != newColumn && getRow() != newRow) return false;
        }
        if (boardInput[newColumn][newRow]!= null){
            if (boardInput[newColumn][newRow].getIsWhite() == isWhite){
                return false;
            }
        }

        if (newColumn == getColumn() || newRow == getRow() ){
            if (newColumn > getColumn()){
                for (int i = getColumn() + 1; i < newColumn; i++){
                    if (boardInput[i][getRow()]!= null) return false;
                }
            }
            else if (newColumn < getColumn()){
                for (int i = getColumn() - 1; i > newColumn; i--){
                    if (boardInput[i][getRow()]!= null) return false;
                }
            }
            else if (newRow > getRow()){
                for (int i = getRow() + 1; i < newRow; i++){
                    if (boardInput[getColumn()][i]!= null) return false;
                }
            }
            else if (newRow < getRow()){
                for (int i = getRow() - 1; i > newRow; i--){
                    if (boardInput[getColumn()][i]!= null) return false;
                }
            }
        } else {

            int rowC = 0;
            if (newRow > getRow()) rowC = 1;
            if (newRow < getRow()) rowC = -1;

            if (newColumn > getColumn()){
                int j = getRow() + rowC;
                for (int i = getColumn() + 1; i < newColumn; i++){
                    if (boardInput[i][j] != null) return false;
                    j += rowC;
                }
            }
            else if (newColumn < getColumn()){
                int j = getRow()  + rowC;
                for (int i = getColumn() - 1; i > newColumn; i--){
                    if (boardInput[i][j] != null) return false;
                    j += rowC;
                }
            }
        }
        return true;
    }
}

/**
 * Holds image of king
 */
class King extends Piece{
    final private GImage image;
    private boolean isWhite;

    public King(boolean isWhite, int column, int row){
        super(isWhite, column, row);
        this.isWhite = isWhite;

        if (isWhite){
            image = new GImage("https://upload.wikimedia.org/wikipedia/commons/3/3b/Chess_klt60.png");
        }
        else image = new GImage("https://upload.wikimedia.org/wikipedia/commons/e/e3/Chess_kdt60.png");

        super.setImg(image);
    }

    /**
     * This method is really long because a lot of things are being checked
     * It checks if it is trying to castle, and if that castle is legal
     * Also checks if King is moving into a check, then returns false
     * @param newColumn
     * @param newRow
     * @param boardInput
     * @return method if can move
     */
    public boolean canMove( int newColumn, int newRow, Piece[][] boardInput ){
        if (newColumn == getColumn() && newRow == getRow()) return false;
        boolean moveTwo = (Math.abs(newColumn-getColumn()) > 1 ||  Math.abs(newRow-getRow()) > 1);
        boolean boo = true;

        if (moveTwo){
            if (newRow != getRow()) return false; // ensures it does not try to move across board
            for (Piece[] x: boardInput) {
                for (Piece j : x) {
                    if (j != null && j.getIsWhite() != isWhite && !j.getClass().getName().equals("King")) {
                        if ((j.canMove(getColumn(), getRow(), boardInput) || j.canMove(newColumn, newRow, boardInput))) {
                            return false;
                        }

                        if (newColumn == 6) {
                            if (boardInput[newColumn + 1][newRow] != null) {
                                if (boardInput[newColumn + 1][newRow].getClass().getName().equals("Rook")) {
                                    if (boardInput[getColumn()+ 1][newRow] != null || boardInput[getColumn()+ 2][newRow] != null ) return false;
                                    if (!hasMoved() && !boardInput[newColumn + 1][newRow].hasMoved()) {
                                        return true;
                                    }
                                }
                            }
                        }

                        else if (newColumn == 2) {
                            if (boardInput[newColumn - 2][newRow] != null) {
                                if (boardInput[newColumn - 2][newRow].getClass().getName().equals("Rook")) {
                                    if (boardInput[getColumn()- 1][newRow] != null || boardInput[getColumn()-2][newRow] != null ) return false;
                                    if (!hasMoved() && !boardInput[newColumn - 2][newRow].hasMoved()) {
                                        return true;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }


        if (boardInput[newColumn][newRow] != null){
            if (boardInput[newColumn][newRow].getIsWhite() == isWhite){
                return false;
            }
        }

        if (moveTwo) return false;

        for (Piece[] x: boardInput){
            for (Piece j: x){

                Piece kingPlaceHolder = this;
                boardInput[getColumn()][getRow()] = null;

                if (j != null && j.getIsWhite() != isWhite){

                    //Checks if piece can move to spot, even if occupied by piece of same color
                    if (boardInput[newColumn][newRow] != null){

                        Piece placeHolder = boardInput[newColumn][newRow];
                        boardInput[newColumn][newRow] = null;

                        //last check is to solve problem where piece thinks it can move to its own square
                        if (!j.getClass().getName().equals("Pawn") && j.canMove(newColumn, newRow, boardInput)
                           && !(j.getRow() == newRow  && j.getColumn() == newColumn)){
                            boardInput[newColumn][newRow] = placeHolder;
                            boardInput[getColumn()][getRow()] = kingPlaceHolder;
                            return false;
                        }
                        boardInput[newColumn][newRow] = placeHolder;

                    }

                    //checks if pawn can move diagonally onto spot
                    if (j.getClass().getName().equals("Pawn")){
                        if (Math.abs(newColumn-j.getColumn()) == 1){
                            if (j.getIsWhite()) {
                                if (newRow + 1 == j.getRow()){
                                    boardInput[getColumn()][getRow()] = kingPlaceHolder;
                                    return false;
                                }
                            }
                            else if (newRow - 1 == j.getRow()){
                                boardInput[getColumn()][getRow()] = kingPlaceHolder;
                                return false;
                            }
                        }
                    }

                    else if (j.canMove(newColumn, newRow, boardInput)){
                        boardInput[getColumn()][getRow()] = kingPlaceHolder;
                        return false;
                    }
                }
                boardInput[getColumn()][getRow()] = kingPlaceHolder;
            }

        }
        return boo;
    }
}


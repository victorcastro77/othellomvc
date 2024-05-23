package mvc.othello;

import com.mrjaffesclass.apcs.messenger.*;
import java.awt.Color;

/**
 * The model represents the data that the app uses.
 * @author Roger Jaffe
 * @version 1.0
 */
public class Model implements MessageHandler {

  // Messaging system for the MVC
  private final Messenger mvcMessaging;
  
  String[][] gameBoard = new String[8][8];
  boolean currentTurn = false;
  boolean noLastMove = false;
  boolean gameFinished = false;

  /**
   * Model constructor: Create the data representation of the program
   * @param messages Messaging class instantiated by the Controller for 
   *   local messages between Model, View, and controller
   */
  public Model(Messenger messages) {
    mvcMessaging = messages;
  }
  
  /**
   * Initialize the model here and subscribe to any required messages
   */
  public void init() {
    mvcMessaging.subscribe("moveMade", this);
    mvcMessaging.subscribe("newGame", this);
    newGame();
  }
  
  @Override
  public void messageHandler(String messageName, Object messagePayload) {
    switch (messageName) {
        case "moveMade": {
            int row = Character.getNumericValue(messagePayload.toString().charAt(0));
            int col = Character.getNumericValue(messagePayload.toString().charAt(1));
            if (gameBoard[row][col] == "_"){
                if (currentTurn == true){
                    gameBoard[row][col] = "1";
                } else{
                    gameBoard[row][col] = "0";
                }
                useRules(row, col);
            }
            showPossibleMoves();
            updateVisibleUI();
            break;
        }
        case "newGame": {
            newGame();
            break;
        }
    }
  }
  
  public void updateVisibleUI() {
    mvcMessaging.notify("gameUpdated", gameBoard);
    countSquares();
    
}
  
  public void countSquares(){
    int white = 0;
    int black = 0;
    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            if (gameBoard[i][j] == "1") {
                white += 1;
            } else if (gameBoard[i][j] == "0") {
                black += 1;
            }
        }
    }
    mvcMessaging.notify("squaresCountedWhite", white);
    mvcMessaging.notify("squaresCountedBlack", black);
    if (white + black == 64){
        mvcMessaging.notify("gameWon", checkWinner(black, white));
    }
  }
  
  public void newGame() {
    currentTurn = false;
    for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            gameBoard[i][j] = " ";
        }
    }
    gameBoard[3][3] = "1";
    gameBoard[3][4] = "0";
    gameBoard[4][3] = "0";
    gameBoard[4][4] = "1";
    showPossibleMoves();
    updateVisibleUI();
  }
  
  public void showPossibleMoves() {
      boolean canMove = false;
      for (int i = 0; i < 8; i++) {
        for (int j = 0; j < 8; j++) {
            if (gameBoard[i][j] == "_"){
                gameBoard[i][j] = " ";
            }
            if (checkLegalMove(new Coordinate(i, j))) {
                canMove = true;
                gameBoard[i][j] = "_";
            }
        }
      }
      if (canMove == false && !gameFinished){
          updateVisibleUI();
          if (noLastMove == true){
              mvcMessaging.notify("gameWon", " ");
              gameFinished = true;
          }
          currentTurn = !currentTurn;
          showPossibleMoves();
          updateVisibleUI();
          noLastMove = true;
      }
      else{
          noLastMove = false;
      }
  }
  
  public boolean checkLegalMove(Coordinate coordinate) {
    for (Coordinate direction : Constants.DIRECTIONS) {
        Coordinate coordinate1 = new Coordinate(coordinate.getRow(), coordinate.getCol());
        coordinate.add(direction);
        if (currentTurn == false && coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] == "1" && gameBoard[coordinate1.getRow()][coordinate1.getCol()] == " "){
            Coordinate newDirection = new Coordinate(coordinate.getRow()-coordinate1.getRow(), coordinate.getCol()-coordinate1.getCol());
            while (coordinate.isInsideBoard()){
                coordinate.add(newDirection);
                if (coordinate.isInsideBoard() && (gameBoard[coordinate.getRow()][coordinate.getCol()] == " " | gameBoard[coordinate.getRow()][coordinate.getCol()] == "_")){
                    break;
                }
                else if (coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] == "0"){
                    return true;
                }
            }
        }
        else if (currentTurn == true && coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] == "0" && gameBoard[coordinate1.getRow()][coordinate1.getCol()] == " "){
            Coordinate newDirection = new Coordinate(coordinate.getRow()-coordinate1.getRow(), coordinate.getCol()-coordinate1.getCol());
            while (coordinate.isInsideBoard()){
                coordinate.add(newDirection);
                if (coordinate.isInsideBoard() && (gameBoard[coordinate.getRow()][coordinate.getCol()] == " " | gameBoard[coordinate.getRow()][coordinate.getCol()] == "_")){
                    break;
                }
                else if (coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] == "1"){
                    return true;
                }
            }
        }
        coordinate = new Coordinate(coordinate1.getRow(), coordinate1.getCol());
    }
    return false;
  }
  
  public void useRules(int i, int j){
    if (currentTurn == true && gameBoard[i][j] == "1"){
        Coordinate coordinate = new Coordinate(i, j);
        for (Coordinate direction : Constants.DIRECTIONS) {
            coordinate = new Coordinate(i, j);
            while (coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] != " " && gameBoard[coordinate.getRow()][coordinate.getCol()] != "_"){
                mvcMessaging.notify("printOutput", coordinate);
                coordinate.add(direction);
                mvcMessaging.notify("printOutput", coordinate);
                if (coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] == "1"){
                    mvcMessaging.notify("printOutput", "ye");
                    mvcMessaging.notify("printOutput", direction);
                    coordinate = new Coordinate(i, j);
                    coordinate.add(direction);
                    mvcMessaging.notify("printOutput", gameBoard[coordinate.getRow()][coordinate.getCol()]);
                    while (gameBoard[coordinate.getRow()][coordinate.getCol()] != "1"){
                        mvcMessaging.notify("printOutput", coordinate);
                        gameBoard[coordinate.getRow()][coordinate.getCol()] = "1";
                        coordinate.add(direction);
                    }
                    break;
                }
            }
        }
    }
    else if (currentTurn == false && gameBoard[i][j] == "0"){
        Coordinate coordinate = new Coordinate(i, j);
        for (Coordinate direction : Constants.DIRECTIONS) {
            coordinate = new Coordinate(i, j);
            while (coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] != " " && gameBoard[coordinate.getRow()][coordinate.getCol()] != "_"){
                mvcMessaging.notify("printOutput", coordinate);
                coordinate.add(direction);
                mvcMessaging.notify("printOutput", coordinate);
                if (coordinate.isInsideBoard() && gameBoard[coordinate.getRow()][coordinate.getCol()] == "0"){
                    mvcMessaging.notify("printOutput", "ye");
                    mvcMessaging.notify("printOutput", direction);
                    coordinate = new Coordinate(i, j);
                    coordinate.add(direction);
                    mvcMessaging.notify("printOutput", gameBoard[coordinate.getRow()][coordinate.getCol()]);
                    while (gameBoard[coordinate.getRow()][coordinate.getCol()] != "0"){
                        mvcMessaging.notify("printOutput", coordinate);
                        gameBoard[coordinate.getRow()][coordinate.getCol()] = "0";
                        coordinate.add(direction);
                    }
                    break;
                }
            }
        }
    }
      currentTurn = !currentTurn;
  }
  
  public String checkWinner(int black, int white){
      if (black > white) return "0";
      else if (white > black) return "!";
      else return " ";
  }
}

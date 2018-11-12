package edu.utep.cs.cs4330.sudoku.model;

/** An abstraction of Sudoku puzzle. */
public class Board {

    /** Size of this board (number of columns/rows). */
    public final int size;
    public Square[][] board;
    public int squaresLeft;
    public int difficulty;
    private SudokuSolver strat;

    /** Create a new board of the given size. */
    public Board(int size, int diff) {
        this.size = size;
        difficulty = diff;
        board = new Square[size][size];
        strat = new SudokuSolver(this);
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++)
                board[i][j] = new Square(size, i, j);
        }
        squaresLeft = size*size;
        initializeBoard();
    }
    //Create an empty board.
    public Board(int s){
        size = s;
        board = new Square[size][size];
        strat = new SudokuSolver(this);
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++)
                board[i][j] = new Square(size, i, j);
        }
        squaresLeft = size*size;
    }
    //Create a copy of our board.
    public Board(Square[][] copyMe) {
        size = copyMe.length;
        board = new Square[size][size];
        squaresLeft = size*size;
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++)
                board[i][j] = new Square(size, i, j);
        }
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                if(copyMe[i][j].value != 0)
                    insert(copyMe[i][j].value, i, j);
            }
        }

    }
    private void initializeBoard() {
        int fill = 0;
        if(size == 4){
            if(difficulty == 1){fill = 6;}
            if(difficulty == 2){fill = 5;}
            if(difficulty == 3){fill = 4;}
        }
        if(size == 9){
            if(difficulty == 1){fill = 30 - (int)(Math.random()*5);}
            if(difficulty == 2){fill = 25 - (int)(Math.random()*4);}
            if(difficulty == 3){fill = 21 - (int)(Math.random()*4);}
        }
        for(int i = 0; i < fill; i++) {
            if(i <= 20)
                randomInsert();
            else carefulInsert();
        }
    }
    // This method is used to randomly insert values onto the board when initializing the Sudoku game.
    private void randomInsert(){
        boolean check = true;
        while(check){
            int randVar = (int)(Math.random()*size)+1;
            int randX = (int)(Math.random()*size);
            int randY = (int)(Math.random()*size);
            if(insert(randVar, randX, randY)){
                check = false;
                board[randX][randY].lock();
            }
        }
    }
    // This method is used to carefully insert values onto the board
    public void carefulInsert() {
        boolean check = true;
        while(check) {
            Square mostConstrained = strat.findMostConstrained();
            for (int i = 1; i <= size; i++) {
                if (insert(i, mostConstrained.x, mostConstrained.y))
                    check = false;
                    mostConstrained.lock();
            }
        }
    }
    /** Return the size of this board. */
    public int size() {
        return size;
    }
    // This method is used to insert a number on the Sudoku board.
    public boolean insert(int num, int x, int y){
        if(x < size && y < size && num <= size){
            if(num == 0){
                if(board[x][y].value != 0 && !board[x][y].locked) {
                    addOptions(x, y, board[x][y].value);
                    board[x][y].set(num);
                    squaresLeft++;
                }
                return true;
            }
            if(board[x][y].value != 0)
                return false;
            if(board[x][y].isOption(num)){
                board[x][y].set(num);
                removeOptions(x, y, num);
                squaresLeft--;
                return true;
            }
        }
        return false;
    }
    // This method is used to remove solution options to each related square on the board after inserting a number.
    public void removeOptions(int x, int y, int num){
        for(int i = 0; i < size; i++){
            board[x][i].removeRowOption(num);
            board[i][y].removeColOption(num);
        }
        int sr = (int) Math.sqrt(size);
        for(int i = 0; i < sr; i++) {
            for (int j = 0; j < sr; j++) {
                board[i+x/sr*sr][j+y/sr*sr].removeSqrOption(num);
            }
        }
    }
    // This method is used to add solution options to each related square on the board after deleting a number.
    public void addOptions(int x, int y, int num){
        for(int i = 0; i < size; i++){
            board[x][i].addRowOption(num);
            board[i][y].addColOption(num);
        }
        int sr = (int) Math.sqrt(size);
        for(int i = 0; i < sr; i++) {
            for (int j = 0; j < sr; j++)
                board[i+x/sr*sr][j+y/sr*sr].addSqrOption(num);
        }
    }
}

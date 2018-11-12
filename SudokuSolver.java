package edu.utep.cs.cs4330.sudoku.model;

/**
 * Created by rdgeh on 3/13/2018.
 */

public class SudokuSolver {
    private Board puzzle;

    public SudokuSolver(Board p){
        puzzle = p;
    }

    public Board solvePuzzle(){
        if(puzzle.squaresLeft == 0)
            return puzzle;
        Square mc = findMostConstrained();
        if(mc.optionsLeft() == 0)
            return null;
        for(int i = 1; i <= puzzle.size; i++){
            if(puzzle.board[mc.x][mc.y].isOption(i)){
                puzzle.insert(i, mc.x, mc.y);
                Board solution = solvePuzzle();
                if(solution != null)
                    return solution;
                else {
                    puzzle.insert(0, mc.x, mc.y);
                    mc.removeColOption(i);
                    mc.removeRowOption(i);
                    mc.removeSqrOption(i);
                }
            }
        }
        return null;
    }
    public Square findMostConstrained(){
        Square mc = new Square(10, -1, -1);
        for(int i = 0; i < puzzle.size; i++){
            for(int j = 0; j < puzzle.size; j++){
                if(puzzle.board[i][j].value == 0) {
                    if(mc.optionsLeft() > puzzle.board[i][j].optionsLeft()) {
                        mc = puzzle.board[i][j];
                    }
                }
            }
        }
        return mc;
    }
}

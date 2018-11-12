package edu.utep.cs.cs4330.sudoku.model;


public class Square {
    public int value;
    private boolean[] colOptions;
    private boolean[] rowOptions;
    private boolean[] sqrOptions;
    public boolean placedByPeer;
    public boolean isSelected;
    public int x;
    public int y;
    public boolean locked;

    public Square(int size, int x, int y){
        locked = false;
        placedByPeer = false;
        isSelected = false;
        value = 0;
        this.x = x;
        this.y = y;
        colOptions = new boolean[size+1];
        rowOptions = new boolean[size+1];
        sqrOptions = new boolean[size+1];
        for(int i = 0; i <= size; i++) {
            colOptions[i] = true;
            rowOptions[i] = true;
            sqrOptions[i] = true;
        }
    }
    public boolean isOption(int n){
        return (colOptions[n] && rowOptions[n] && sqrOptions[n]);
    }

    public boolean set(int n){
        if(isOption(n)){
            value = n;
            return true;
        }
        return false;
    }
    public int optionsLeft(){
        int total =0;
        for(int i = 1; i < sqrOptions.length; i++){
            if(isOption(i))
                total++;
        }
        return total;
    }
    public void removeColOption(int n){colOptions[n] = false;}
    public void removeRowOption(int n){rowOptions[n] = false;}
    public void removeSqrOption(int n){sqrOptions[n] = false;}
    public void addColOption(int n){colOptions[n] = true;}
    public void addRowOption(int n){rowOptions[n] = true;}
    public void addSqrOption(int n){sqrOptions[n] = true;}
    public void lock(){ locked = true; }
}

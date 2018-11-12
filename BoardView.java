package edu.utep.cs.cs4330.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

import edu.utep.cs.cs4330.sudoku.model.Board;
import edu.utep.cs.cs4330.sudoku.model.Square;

/**
 * A special view class to display a Sudoku board modeled by the
 * {@link edu.utep.cs.cs4330.sudoku.model.Board} class. You need to write code for
 * the <code>onDraw()</code> method.
 *
 * @see edu.utep.cs.cs4330.sudoku.model.Board
 * @author cheon
 */
public class BoardView extends View {

    /** To notify a square selection. */
    public interface SelectionListener {

        /** Called when a square of the board is selected by tapping.
         * @param x 0-based column index of the selected square.
         * @param y 0-based row index of the selected square. */
        void onSelection(int x, int y);
    }

    /** Listeners to be notified when a square is selected. */
    private final List<SelectionListener> listeners = new ArrayList<>();

    /** Number of squares in rows and columns.*/
    private int boardSize = 9;

    /** Board to be displayed by this view. */
    private Board board;

    /** Width and height of each square. This is automatically calculated
     * this view's dimension is changed. */
    private float squareSize;

    /** Translation of screen coordinates to display the grid at the center. */
    private float transX;

    /** Translation of screen coordinates to display the grid at the center. */
    private float transY;

    /** Paint to draw the background of the grid. */
    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        int boardColor = Color.rgb(201, 186, 145);
        boardPaint.setColor(boardColor);
        boardPaint.setAlpha(80); // semi transparent
    }

    /** Create a new board view to be run in the given context. */
    public BoardView(Context context) { //@cons
        this(context, null);
    }

    /** Create a new board view by inflating it from XML. */
    public BoardView(Context context, AttributeSet attrs) { //@cons
        this(context, attrs, 0);
    }

    /** Create a new instance by inflating it from XML and apply a class-specific base
     * style from a theme attribute. */
    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSaveEnabled(true);
        getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    /** Set the board to be displayed by this view. */
    public void setBoard(Board board) {
        this.board = board;
        boardSize = board.size;
    }

    /** Draw a 2-D graphics representation of the associated board. */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(transX, transY);
        if (board != null) {
            drawGrid(canvas);
            drawSquares(canvas);
        }
        canvas.translate(-transX, -transY);
    }

    /** Draw horizontal and vertical grid lines. */
    private void drawGrid(Canvas canvas) {
        final float maxCoord = maxCoord();
        canvas.drawRect(0, 0, maxCoord, maxCoord, boardPaint);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        if(boardSize == 9) {
            for (int i = -1; i <= 1; i++) {
                canvas.drawLine(maxCoord / 3 + i, 0, maxCoord / 3 + i, maxCoord, paint);
                canvas.drawLine(maxCoord * 2 / 3 + i, 0, maxCoord * 2 / 3 + i, maxCoord, paint);
                canvas.drawLine(0, maxCoord / 3 + i, maxCoord, maxCoord / 3 + i, paint);
                canvas.drawLine(0, maxCoord * 2 / 3 + i, maxCoord, maxCoord * 2 / 3 + i, paint);
            }
        }
        else{
            for (int i = -1; i <= 1; i++) {
                canvas.drawLine(maxCoord / 2 + i, 0, maxCoord / 2 + i, maxCoord, paint);
                canvas.drawLine(0, maxCoord / 2 + i, maxCoord, maxCoord / 2 + i, paint);
            }
        }
        for(int i = 1; i < boardSize; i++){
            canvas.drawLine(maxCoord/boardSize*i, 0, maxCoord/boardSize*i, maxCoord, paint);
            canvas.drawLine(0, maxCoord/boardSize*i, maxCoord, maxCoord/boardSize*i, paint);
        }
    }
    public boolean hintsEnabled;

    /** Draw all the squares (numbers) of the associated board. */
    private void drawSquares(Canvas canvas) {
        float max = maxCoord();
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setTextSize(max/boardSize - max/(boardSize*10));

        Paint peerPaint = new Paint();
        peerPaint.setColor(Color.GREEN);
        peerPaint.setTextSize(max/boardSize - max/(boardSize*10));

        Paint lockedSquare = new Paint();
        lockedSquare.setColor(Color.BLACK);
        lockedSquare.setTextSize(max/boardSize - max/(boardSize*10));
        Paint lockSqBg = new Paint();
        int lockedSquareColor = Color.rgb(200, 185, 144);
        lockSqBg.setColor(lockedSquareColor);

        Paint smallSquare = new Paint();
        smallSquare.setColor(Color.BLACK);
        smallSquare.setTextSize(max/(3*boardSize));

        Paint sqrSelpaint = new Paint();
        int sqrSelColor = Color.rgb(200, 215, 154);
        sqrSelpaint.setColor(sqrSelColor);

        Square[][] b = board.board;
        for(int i = 0; i < boardSize; i++) {
            for(int j = 0; j < boardSize; j++) {
                if(board.board[i][j].isSelected)
                    canvas.drawRect(i*max/boardSize + 5, j*max/boardSize + 5, (i+1)*max/boardSize - 5, (j+1)*max/boardSize - 5, sqrSelpaint);
                if(board.board[i][j].value != 0) {
                    String s = "" + board.board[i][j].value + "";
                    if(board.board[i][j].locked) {
                        canvas.drawRect(i*max/boardSize + 5, j*max/boardSize + 5, (i+1)*max/boardSize - 5, (j+1)*max/boardSize - 5, lockSqBg);
                        canvas.drawText(s, max / boardSize * (i) + (max / boardSize / 4), max / boardSize * (j + 1) - (max / boardSize / 6), lockedSquare);
                    }
                    else {
                        if(board.board[i][j].placedByPeer)
                            canvas.drawText(s, max / boardSize * (i) + (max / boardSize / 4), max / boardSize * (j + 1) - (max / boardSize / 6), peerPaint);
                        else
                            canvas.drawText(s, max / boardSize * (i) + (max / boardSize / 4), max / boardSize * (j + 1) - (max / boardSize / 6), paint);
                    }
                }
                else if(hintsEnabled){
                    String myString = "";
                    String myString2 = "";
                    for(int x = 1; x <= boardSize; x++){
                        if(board.board[i][j].isOption(x)){
                            if(x <= boardSize/2)
                                myString += "" + x;
                            else
                                myString2 += "" + x;
                        }
                            canvas.drawText(myString2, max / boardSize * (i) + (max / boardSize / 8), max / boardSize * (j + 1) - (max / boardSize / 6), smallSquare);
                            canvas.drawText(myString, max / boardSize * (i) + (max / boardSize / 8), max / boardSize * (j + 1) - (max / boardSize / 2), smallSquare);
                    }
                }
            }
        }
    }

    /** Overridden here to detect tapping on the board and
     * to notify the selected square if exists. */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                int xy = locateSquare(event.getX(), event.getY());
                if (xy >= 0) {
                    // xy encoded as: x * 100 + y
                    notifySelection(xy / 100, xy % 100);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    /**
     * Given screen coordinates, locate the corresponding square of the board, or
     * -1 if there is no corresponding square in the board.
     * The result is encoded as <code>x*100 + y</code>, where x and y are 0-based
     * column/row indexes of the corresponding square.
     */
    private int locateSquare(float x, float y) {
        x -= transX;
        y -= transY;
        if (x <= maxCoord() &&  y <= maxCoord()) {
            final float squareSize = lineGap();
            int ix = (int) (x / squareSize);
            int iy = (int) (y / squareSize);
            return ix * 100 + iy;
        }
        return -1;
    }

    /** To obtain the dimension of this view. */
    private final ViewTreeObserver.OnGlobalLayoutListener layoutListener
            =  new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            squareSize = lineGap();
            float width = Math.min(getMeasuredWidth(), getMeasuredHeight());
            transX = (getMeasuredWidth() - width) / 2f;
            transY = (getMeasuredHeight() - width) / 2f;
        }
    };

    /** Return the distance between two consecutive horizontal/vertical lines. */
    protected float lineGap() {
        return Math.min(getMeasuredWidth(), getMeasuredHeight()) / (float) boardSize;
    }

    /** Return the number of horizontal/vertical lines. */
    private int numOfLines() { //@helper
        return boardSize + 1;
    }

    /** Return the maximum screen coordinate. */
    protected float maxCoord() { //@helper
        return lineGap() * (numOfLines() - 1);
    }

    /** Register the given listener. */
    public void addSelectionListener(SelectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /** Unregister the given listener. */
    public void removeSelectionListener(SelectionListener listener) {
        listeners.remove(listener);
    }

    /** Notify a square selection to all registered listeners.
     *
     * @param x 0-based column index of the selected square
     * @param y 0-based row index of the selected square
     */
    private void notifySelection(int x, int y) {
        for (SelectionListener listener: listeners) {
            listener.onSelection(x, y);
        }
    }

}

// Russell Gehan
package edu.utep.cs.cs4330.sudoku;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import edu.utep.cs.cs4330.sudoku.model.Board;
import edu.utep.cs.cs4330.sudoku.model.Square;
import edu.utep.cs.cs4330.sudoku.model.SudokuSolver;

public class MainActivity extends AppCompatActivity {

    private Board board;
    private BoardView boardView;
    private int diff, size;
    private Square squareSelected;
    private SudokuSolver solver;
    private NetworkAdapter adapter;
    private AlertDialog.Builder builder;
    private Socket socket;
    private boolean connected;
    private Menu menu;

    /** All the number buttons. */
    private List<View> numberButtons;
    private static final int[] numberIds = new int[] {
            R.id.n0, R.id.n1, R.id.n2, R.id.n3, R.id.n4,
            R.id.n5, R.id.n6, R.id.n7, R.id.n8, R.id.n9
    };

    /** Width of number buttons automatically calculated from the screen size. */
    private static int buttonWidth;

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        socket = new Socket();
        builder = new AlertDialog.Builder(MainActivity.this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(SettingsActivity.NewGamePreferenceFragment.size == 0) size = 9;
        else if(SettingsActivity.NewGamePreferenceFragment.size == 1) size = 4;
        diff = SettingsActivity.NewGamePreferenceFragment.difficulty + 1;

        boolean boardHasSolution = false;
        while(!boardHasSolution) {
            board = new Board(size, diff);
            boardHasSolution = doesBoardHaveSolution(new Board(board.board));
        }
        boardView = findViewById(R.id.boardView);
        boardView.setBoard(board);
        boardView.addSelectionListener(this::squareSelected);
        boardView.hintsEnabled = false;
        numberButtons = new ArrayList<>(numberIds.length);
        for (int i = 0; i < numberIds.length; i++) {
            final int number = i; // 0 for delete button
            View button = findViewById(numberIds[i]);
            button.setOnClickListener(e -> numberClicked(number));
            numberButtons.add(button);
            setButtonWidth(button);
        }
        if(prefs.getBoolean(SettingsActivity.HelpPreferenceFragment.hintsEnabled, false))
            boardView.hintsEnabled = true;
        squareSelected = null;
        connected = false;
    }
    private boolean doesBoardHaveSolution(Board testBoard){
        solver = new SudokuSolver(testBoard);
        if (solver.solvePuzzle() != null)
            return true;
        else return false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id == R.id.share){
            if(!connected)
                startSharing();
            else
                stopSharing();
        }
        return super.onOptionsItemSelected(item);
    }
    /** Callback to be invoked when the new button is tapped. */
    public void newClicked(View view) {
        squareSelected = null;
        boolean boardHasSolution = false;
        while(!boardHasSolution) {
            board = new Board(size, diff);
            boardHasSolution = doesBoardHaveSolution(new Board(board.board));
        }
        if(connected){
            View waitingView = getLayoutInflater().inflate(R.layout.waiting_on_peer, null);
            waitingView.findViewById(R.id.cancelbutton).setOnClickListener(this::cancelDialogAndDisconnect);
            ArrayList<Square> squares = new ArrayList<>();
            for(int i =0; i < size; i++){
                for(int j = 0; j< size; j++){
                    if(board.board[i][j].value != 0)
                        squares.add(board.board[i][j]);
                }
            }
            int[] newBoard = new int[squares.size()*4];
            for(int i = 0; i < squares.size(); i++) {
                Square s = squares.get(i);
                newBoard[4*i] = s.x;
                newBoard[4*i+1] = s.y;
                newBoard[4*i+2] = s.value;
                newBoard[4*i+3] = 1;
            }
            adapter.writeNew(size, newBoard);
            startDialog(waitingView);
        }
        boardView.setBoard(board);
        boardView.invalidate();
    }
    /** Callback to be invoked when a number button is tapped.
     *
     * @param n Number represented by the tapped button
     *          or 0 for the delete button.
     */
    public void numberClicked(int n) {
        if(n <= size && squareSelected != null) {
            if (board.insert(n, squareSelected.x, squareSelected.y)) {
                if(socket != null && adapter != null)
                    adapter.writeFill(squareSelected.x, squareSelected.y, n);
                boardView.invalidate();
                if (board.squaresLeft == 0)
                    toast("Congratulations. You win!");
            }
            else toast(String.format("Sorry, you cant place a " + n + " at the square selected: (%d, %d)", squareSelected.x, squareSelected.y));
        }
        else toast(String.format("Sorry, you cant place a " + n + " at the square selected: (%d, %d)", squareSelected.x, squareSelected.y));
    }
    public void solvableClicked(View view){
        boolean isSolvable = doesBoardHaveSolution(new Board(board.board));
        toast("" + isSolvable);
    }
    public void solveClicked(View view){
        solver = new SudokuSolver(board);
        solver.solvePuzzle();
        boardView.invalidate();
    }
    /**
     * Callback to be invoked when a square is selected in the board view.
     *
     * @param x 0-based column index of the selected square.
     * @param x 0-based row index of the selected square.
     */

    private void squareSelected(int x, int y) {
        if(squareSelected != null)
            squareSelected.isSelected = false;
        squareSelected = board.board[x][y];
        squareSelected.isSelected = true;
        boardView.invalidate();
    }

    /** Show a toast message. */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /** Set the width of the given button calculated from the screen size. */
    private void setButtonWidth(View view) {
        if (buttonWidth == 0) {
            final int distance = 2;
            int screen = getResources().getDisplayMetrics().widthPixels;
            buttonWidth = (screen - ((9 + 1) * distance)) / 9; // 9 (1-9)  buttons in a row
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = buttonWidth;
        view.setLayoutParams(params);
    }
    /* Code added for Homework Assignment #3
       By: Russell Gehan
       CS 4330: Networking
       Last Modified: 4/9/2018
     */
    public void refreshSharingIcon(){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                if (connected)
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.sharingconnected));
                else
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.sharingicon));
            }
        });
    }

    public void startDialog(View view){
        if(dialog != null)
            if(dialog.isShowing()) dialog.cancel();
        builder = new AlertDialog.Builder(this);
        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }
    public void cancelDialog(View view){ dialog.cancel(); }
    public void cancelDialogAndDisconnect(View view){
        dialog.cancel();
        connected = false;
        refreshSharingIcon();
        try{ socket.close(); } catch (IOException e) {}
    }

    // Method called when user clicks on the share button while connected to a peer.
    public void stopSharing(){
        View stopView = getLayoutInflater().inflate(R.layout.disconnect, null);
        startDialog(stopView);
        stopView.findViewById(R.id.cancelbutton).setOnClickListener(this:: cancelDialog);
        stopView.findViewById(R.id.okbutton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                try{ socket.close(); } catch(IOException e){}
                connected = false;
                refreshSharingIcon();
            }
        });
    }
    // Method called when user clicks on the share button while not already connected to a peer.
    public void startSharing(){
        View sharingView = getLayoutInflater().inflate(R.layout.sharing_main, null);
        startDialog(sharingView);
        RadioButton wifi = sharingView.findViewById(R.id.wifi_button);
        if(wifi.isChecked())
            sharingView.findViewById(R.id.sharebutton).setOnClickListener(this:: sharingWithWifi);
        sharingView.findViewById(R.id.cancelbutton).setOnClickListener(this::cancelDialog);
    }
    public void sharingWithWifi(View view){
        View pairingView = getLayoutInflater().inflate(R.layout.pairing, null);
        startDialog(pairingView);
        pairingView.findViewById(R.id.cancelbutton).setOnClickListener(this:: cancelDialog);
        pairingView.findViewById(R.id.pairbutton).setOnClickListener(this:: connectClicked);
        peerText = pairingView.findViewById(R.id.peer_text);
        portText = pairingView.findViewById(R.id.port_text);
    }
    private EditText peerText, portText;

    public void connectClicked(View view){
        View waitingView = getLayoutInflater().inflate(R.layout.waiting_on_peer, null);
        String peerMsg = peerText.getText().toString();
        Log.d("Peer name ", peerMsg);
        int port = Integer.parseInt(portText.getText().toString());
        startDialog(waitingView);
        connectToPeer(peerMsg, port);
        waitingView.findViewById(R.id.cancelbutton).setOnClickListener(this::cancelDialogAndDisconnect);
    }
    // This method connects the user to a peer when given the server string and port number.
    public void connectToPeer(String server, int port){
        new Thread(() -> {
            socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(server, port));
                connected = true;
                refreshSharingIcon();
                adapter = new NetworkAdapter(socket);

            } catch(IOException e){}
            if (socket != null) {
                adapter.setMessageListener(new NetworkAdapter.MessageListener() {
                    @Override
                    public void messageReceived(NetworkAdapter.MessageType type, int x, int y, int z, int[] others) {
                        if(type.toString().equals("CLOSE")) {
                            connected = false;
                            refreshSharingIcon();
                            try { socket.close(); } catch (IOException e) {}
                            if(dialog != null)
                                if(dialog.isShowing())
                                    dialog.cancel();
                            Log.d("We got ", type.toString());
                        }
                        else if(type.toString().equals("NEW_ACK")){
                            if(dialog != null)
                                if(dialog.isShowing())
                                    dialog.cancel();
                        }

                        else if(type.toString().equals("JOIN_ACK")){
                            if(dialog != null)
                                if(dialog.isShowing())
                                    dialog.cancel();
                            receiveBoard(y, others);
                            refreshUI();
                        }
                        else if(type.toString().equals("NEW")){
                            handleNewGameRequest(x, others);
                        }
                        else if(type.toString().equals("FILL")) {
                            board.insert(z, x, y);
                            board.board[x][y].placedByPeer = true;
                            refreshUI();
                        }
                    }
                });
                adapter.receiveMessagesAsync();
                adapter.writeJoin();
                //handler.post(() -> Toast.makeText(this, socket != null ? "Connected." : "Failed to connect!",Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    public void receiveBoard(int size, int[] peer){
        this.size = size;
        board = new Board(size);
        for(int i = 0; i < peer.length; i+=4){
            int x = peer[i];
            int y = peer[i+1];
            int value = peer[i+2];
            board.insert(value, x, y);
            if(peer[i+3] == 1)
                board.board[x][y].lock();
            else
                board.board[x][y].placedByPeer = true;
        }
        boardView.setBoard(board);
    }
    // This method is called whenever by the networking thread to refresh the board whenever it is changed.
    public void refreshUI(){
        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                boardView.invalidate();
            }
        });
    }
    // This method is called by the networking thread to handle a new game request from the peer.
    public void handleNewGameRequest(final int x, final int[] others){
        runOnUiThread(new Runnable() {
            @Override
            public void run(){
                View newGame = getLayoutInflater().inflate(R.layout.new_request, null);
                startDialog(newGame);
                newGame.findViewById(R.id.nobutton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View view){
                        dialog.cancel();
                        if(connected) {
                            adapter.writeNewAck(false);
                            try{
                                socket.close();
                            }catch(IOException e){}
                            connected = false;
                            refreshSharingIcon();
                        }
                    }
                });
                newGame.findViewById(R.id.yesbutton).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View view){
                        dialog.cancel();
                        if(connected) {
                            adapter.writeNewAck(true);
                        }
                        receiveBoard(x, others);
                        refreshUI();
                    }
                });
            }
        });
    }
}

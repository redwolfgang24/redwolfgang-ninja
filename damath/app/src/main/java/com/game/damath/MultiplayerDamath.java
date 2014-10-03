package com.game.damath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.game.damath.DamathController.AttackListener;
import com.game.damath.DamathController.GameOverListener;

public class MultiplayerDamath extends Activity {
    // Debugging
    private static final String TAG = "Damath";
    private static final boolean D = true;
    
	// Message types sent from the BluetoothChatService Handler 
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_DISCOVERABLE = 3;
    
    // Intent result codes
    public static final int RESULT_ALLOW_DISCOVERABLE = 300;
    
    private final long ATTACK_INTERVAL = 15 * 1000; // 30 seconds

	// Key names received from the BluetoothChatService Handler 
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	
	public static final String EXTRA_MULTIPLAYER = "isMultiplayer";
	public static final String EXTRA_HOST = "isHost";
	
	public static final int PARSE_PLAYER_1_CHIPS = 0;
	public static final int PARSE_PLAYER_2_CHIPS = 1;
	public static final int UPDATE_BOARD = 3;
	public static final int DO_MOVE = 4;
	public static final int ANSWERING = 5;
	public static final int ANSWERING_DONE = 6;
	public static final int PLAYER1_NAME = 7;
	public static final int PLAYER2_NAME = 8;
	public static final String PLAYER1_SCORE = "s1";
	public static final String PLAYER2_SCORE = "s2";
	
	private static final int SIZE = 8;
	

	String addSignPos[] = { 	"71", "62", "55", "46", "31", "22", "15", "06" };
	String minusSignPos[] = { 	"73", "60", "57", "44", "33", "20", "17", "04" };
	String multiplySignPos[] = {"77", "64", "53", "40", "37", "24", "13", "00" };
	String divideSignPos[] = { 	"75", "66", "51", "42", "35", "26", "11", "02" };

	String p2ChipsPos[] = { "00:2", "02:5", "04:8", "06:11", "11:7", "13:10",
			"15:3", "17:0", "20:4", "22:1", "24:6", "26:9" };
	String p1ChipsPos[] = { "51:9", "53:6", "55:1", "57:4", "60:0", "62:3",
			"64:10", "66:7", "71:11", "73:8", "75:5", "77:2" };

//	 String p2ChipsPos[] = { "20:4", "22:1", "24:6", "26:9",
//			 		"60:7", "62:10", "64:3", "66:0" };
//	 String p1ChipsPos[] = { "15:9", "11:6"};
	BoardAdapter boardAdapter;
	DamathController damath;
	Player p1, p2;

	TextView player1ScoreView, player2ScoreView, player1Name, player2Name, attackerView, attackTimer;
	ImageView pausePlay;
	Bitmap pauseImg, playImg;
	boolean isPause, isMultiplayer, isHost, isAnswering, chipsInitialize, canSelectChip;
	
	GridView board;
	Map<String, OnClickListener> tileClickListeners = new HashMap<String, View.OnClickListener>();
	Dialog writeAnswer;
	
    private BluetoothAdapter btAdapter;
    private BluetoothService btService;
    
    SharedPreferences sharedPref;
    SharedPreferences.Editor prefEditor;
    
    AttackTimer timer;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.damath_board);
		sharedPref = getSharedPreferences(DamathSettings.DAMATH_SHARED_PREF, MODE_PRIVATE);
        prefEditor = sharedPref.edit();
        
		player1Name = (TextView) findViewById(R.id.player1_name);
		player2Name = (TextView) findViewById(R.id.player2_name);
		player1ScoreView = (TextView) findViewById(R.id.player1_score);
		player2ScoreView = (TextView) findViewById(R.id.player2_score);
		attackerView = (TextView) findViewById(R.id.attacking_player);
		attackTimer = (TextView) findViewById(R.id.attack_timer);

		// Hosting a game
		isHost = getIntent().getBooleanExtra(EXTRA_HOST, false);
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		// If BT is not on, request that it be enabled.
        if (!btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else{
        	setupBTService();
    		ensureDiscoverable();
        }
	}

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (btService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (btService.getState() == BluetoothService.STATE_NONE) {
              // Start the Bluetooth chat services
              btService.start();
            }
        }
    }

    private void setupBTService() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        btService = new BluetoothService(this, mHandler);
    }

	private void initDamath(Player p1, Player p2){
		attackerView.setText(p1.getName());

		damath = new DamathController(p1, p2);

		writeAnswer = new Dialog(this);
		writeAnswer.setContentView(R.layout.write_answer_dialog);
		writeAnswer.setTitle("Write Answer");
		damath.setAttackListener(new AttackListener() {
			@Override
			public void onAttack(final Player attacker, final Chip attackerChip, final Chip opponentChip, final int operation) {
				timer.cancel();
				((TextView)writeAnswer.findViewById(R.id.attackerChipVal)).setText(String.valueOf(attackerChip.getValue()));
				((TextView)writeAnswer.findViewById(R.id.opponentChipVal)).setText(String.valueOf(opponentChip.getValue()));
				
				String op="";
				switch (operation) {
				case Operator.ADDITION:
					op = "+";
					break;
				case Operator.DIFFERENCE:
					op = "-";
					break;
				case Operator.MULTIPLICATION:
					op = "x";
					break;
				case Operator.DIVISION:
					op = "/";
					break;
				}
				((TextView)writeAnswer.findViewById(R.id.operator)).setText(op);
				((EditText) writeAnswer.findViewById(R.id.answer)).setText("");
				
				isAnswering = true;
				writeAnswer.findViewById(R.id.answerBtn).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String ans = ((EditText) writeAnswer.findViewById(R.id.answer)).getText().toString();
						float score = Float.parseFloat(String.format("%.2f", damath.getAttackScore(attackerChip, opponentChip, operation)));
						if(!ans.equals("") && Float.parseFloat(ans) == score){
							attacker.setScore(attacker.getScore() + score);
							TextView scoreView = null;
							if(attacker == MultiplayerDamath.this.p1){
								scoreView = MultiplayerDamath.this.player1ScoreView;
								btService.write(String.valueOf(PLAYER1_SCORE + score).getBytes());
							}else{
								scoreView = MultiplayerDamath.this.player2ScoreView;
								btService.write(String.valueOf(PLAYER2_SCORE + score).getBytes());
							}
							scoreView.setText(String.valueOf(attacker.getScore()));
						} else{
							Toast.makeText(MultiplayerDamath.this, "Incorrect answer.", Toast.LENGTH_SHORT).show();
						}
						btService.write(String.valueOf(ANSWERING_DONE).getBytes());
						isAnswering = false;
						writeAnswer.dismiss();
					}
				});
				if((attacker == MultiplayerDamath.this.p1 && isHost) ||
					(attacker == MultiplayerDamath.this.p2 && !isHost)){
					writeAnswer.show();
					btService.write(String.valueOf(ANSWERING).getBytes());
				}
			}
		});
		
		damath.setGameOverListener(new GameOverListener() {
			@Override
			public void gameOver(Player win) {
				Toast.makeText(getApplicationContext(),"Winner: "+win.getName(), Toast.LENGTH_LONG).show();
				
				String highScores[] = {DamathSettings.HIGH_SCORE_1, DamathSettings.HIGH_SCORE_2, DamathSettings.HIGH_SCORE_3,
										DamathSettings.HIGH_SCORE_4, DamathSettings.HIGH_SCORE_5};
				String highScoreName[] = {DamathSettings.HIGH_SCORE_NAME_1, DamathSettings.HIGH_SCORE_NAME_2, DamathSettings.HIGH_SCORE_NAME_3,
										DamathSettings.HIGH_SCORE_NAME_4, DamathSettings.HIGH_SCORE_NAME_5};
				
		        int highScoreCount = highScores.length;
		        for(int i = 0; i< highScoreCount ; i++){
		        	if(sharedPref.getFloat(highScores[i], 100f) < win.getScore()){
		        		prefEditor.putFloat(highScores[i], win.getScore());
		        		prefEditor.putString(highScoreName[i], win.getName());
		        		prefEditor.commit();
		        		break;
		        	}
		        }
		        
			}
		});

		// Get the screen width of the device
		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

		// Get the gridview child size
		int tileSize = screenWidth / SIZE;

		boardAdapter = new BoardAdapter(this, damath.getBoard(), tileSize);

		board = (GridView) findViewById(R.id.board);
		board.getLayoutParams().width = screenWidth;
		board.getLayoutParams().height = screenWidth;
		board.setColumnWidth(tileSize);
		board.setAdapter(boardAdapter);

		pauseImg = BitmapFactory.decodeResource(getResources(), R.drawable.pause);
		playImg = BitmapFactory.decodeResource(getResources(), R.drawable.play);

		pausePlay = (ImageView) findViewById(R.id.pause_play);
		pausePlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isPause) {
					isPause = false;
					pausePlay.setImageBitmap(pauseImg);
					boardAdapter.notifyDataSetChanged();
				} else {
					isPause = true;
					pausePlay.setImageBitmap(playImg);
					boardAdapter.notifyDataSetChanged();
				}
			}
		});
		
		timer = new AttackTimer(ATTACK_INTERVAL, 1000, attackTimer, attackerView, p1, p2, damath, boardAdapter);
		timer.start();
	}
	
	private String[] shuffleChipValuePositions(String chipPos[]) {
		List<String> chipValue = new ArrayList<String>();
		List<String> chipPosition = new ArrayList<String>();
		List<String> shuffledChipValue = new ArrayList<String>();

		int chipCount = chipPos.length;
		for (int i = 0; i < chipCount; i++) {
			chipValue.add(String.valueOf(i));
			chipPosition.add(chipPos[i]);
		}

		while (chipValue.size() > 0) {
			int i = (int) (Math.random() * chipValue.size());
			int j = (int) (Math.random() * chipPosition.size());
			String pos = chipPosition.remove(j);
			String tmp = pos.substring(0, pos.lastIndexOf(":") + 1) + chipValue.remove(i);

			shuffledChipValue.add(tmp);
			Log.d("chips", tmp);
		}

		return shuffledChipValue.toArray(new String[shuffledChipValue.size()]);
	}

	// The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                	initGame();
                	if(isHost){
                		initPlayerName();
                	}
                    break;
                case BluetoothService.STATE_CONNECTING:
//                    setStatus(R.string.title_connecting);
                    break;
                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
//                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
//                byte[] writeBuf = (byte[]) msg.obj;
//                // construct a string from the buffer
//                String writeMessage = new String(writeBuf);
//                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                String tmp = new String(readBuf, 0, msg.arg1);
                Log.d("ranny", "message read " +tmp);
                // construct a string from the valid bytes in the buffer
                parseMsg(tmp);
                break;
            case MESSAGE_DEVICE_NAME:
//                // save the connected device's name
//                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                Toast.makeText(getApplicationContext(), "Connected to "
//                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

    private void initPlayerName(){
    	String playerName = sharedPref.getString(DamathSettings.PLAYER1_NAME, "RED");
    	if(isHost){
    		p1.setName(playerName);
			player1Name.setText(playerName);
			btService.write(String.valueOf(PLAYER1_NAME+playerName).getBytes());
    	}else{
    		p2.setName(playerName);
    		player2Name.setText(playerName);
    		btService.write(String.valueOf(PLAYER2_NAME+playerName).getBytes());
    	}
    }
    
    
    private void initGame(){
    	if(isHost && !chipsInitialize){
			p1ChipsPos = shuffleChipValuePositions(p1ChipsPos);
			p2ChipsPos = shuffleChipValuePositions(p2ChipsPos);
			
			p1 = new Player("RED");
			p1.initChips(p1ChipsPos);
			p1.setMoveDirectionUp(true);
			p1.setAttacking(true);
			
			p2 = new Player("BLUE");
			p2.initChips(p2ChipsPos);
			p2.setMoveDirectionUp(false);

			initDamath(p1, p2);
			
			String tmp = PARSE_PLAYER_1_CHIPS + reversePlayerChips(p1ChipsPos);
			Log.d("ranny", "sending player 1 chip " +tmp);
			btService.write(tmp.getBytes());
			pause(300);
			
			tmp = PARSE_PLAYER_2_CHIPS + reversePlayerChips(p2ChipsPos);
			Log.d("ranny", "sending player 2 chip " +tmp);
			btService.write(tmp.getBytes());
			pause(300);
			
			tmp = String.valueOf(UPDATE_BOARD);
			btService.write(tmp.getBytes());
			
			chipsInitialize = true;
    	}
    }
    
    private String reversePlayerChips(String chipPos[]){
		int selectedChipRow;
		int selectedChipCol;
		String pos, reversePos;
		int chipCount = chipPos.length;
		String reverseChipPos[] = new String[chipCount];
		
		for(int i = 0; i < chipCount; i++){
			pos = chipPos[i];
			selectedChipRow = Math.abs(Character.getNumericValue(pos.charAt(0)) - 7);
			selectedChipCol = Math.abs(Character.getNumericValue(pos.charAt(1)) - 7);
			reversePos = String.valueOf(selectedChipRow) + String.valueOf(selectedChipCol);
			
			pos = pos.replaceFirst(pos.substring(0, pos.lastIndexOf(":")), reversePos);
			reverseChipPos[i] = pos;
		}
		return Arrays.asList(reverseChipPos).toString().replace(" ", "").replace("[", "").replace("]", "");
    }
    
    private String reversePosition(String pos){
		int selectedChipRow;
		int selectedChipCol;
		
		selectedChipRow = Math.abs(Character.getNumericValue(pos.charAt(0)) - 7);
		selectedChipCol = Math.abs(Character.getNumericValue(pos.charAt(1)) - 7);
		
		return String.valueOf(selectedChipRow) + String.valueOf(selectedChipCol);
    }
    
    private void pause(long time){
    	try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
        }else if(!isHost){
			// Search for other players
            Intent serverIntent = new Intent(this, SearchHost.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    	}
    }
    
    private void parseMsg(String msg){
    	Log.d("ranny", msg);
    	if(!isHost){
	    	if(Character.getNumericValue(msg.charAt(0)) == PARSE_PLAYER_1_CHIPS){
	    		String chipPos = msg.substring(1, msg.length());
	    		p1 = new Player("RED");
	    		p1.initChips(chipPos.split(","));
	    		p1.setMoveDirectionUp(false);
				p1.setAttacking(true);
	    	} else if(Character.getNumericValue(msg.charAt(0)) == PARSE_PLAYER_2_CHIPS){
	    		String chipPos = msg.substring(1, msg.length());
	    		p2 = new Player("BLUE");
	    		p2.initChips(chipPos.split(","));
	    		p2.setMoveDirectionUp(true);
	    	} else if(Character.getNumericValue(msg.charAt(0)) == UPDATE_BOARD){
	    		Log.d("ranny", "udpating p2 board ");
//	    		damath.initBoard();
//				boardAdapter.notifyDataSetChanged();
	    		initDamath(p1, p2);
	    		initPlayerName();
	    	}
    	}
    	if(Character.getNumericValue(msg.charAt(0)) == DO_MOVE){
    		Log.d("ranny", "parsing move "+msg);
    		String tilePos = msg.substring(1, msg.length());
    		Log.d("ranny", "click "+tilePos);
    		tileClickListeners.get(tilePos).onClick(null);
    		boardAdapter.notifyDataSetChanged();
    	} else if(Character.getNumericValue(msg.charAt(0)) == ANSWERING_DONE){
    		isAnswering = false;
    	} else if(Character.getNumericValue(msg.charAt(0)) == PLAYER1_NAME){
    		String name = msg.substring(1, msg.length());
    		p1.setName(name);
    		player1Name.setText(name);
    	} else if(Character.getNumericValue(msg.charAt(0)) == PLAYER2_NAME){
    		String name = msg.substring(1, msg.length());
    		p2.setName(name);
    		player2Name.setText(name);
    	} else if(msg.length() > 1 && msg.substring(0, 2).equals(PLAYER1_SCORE)){
    		p1.setScore(p1.getScore() + Float.parseFloat(msg.substring(2, msg.length())));
    		player1ScoreView.setText(String.valueOf(p1.getScore()));
    	} else if(msg.length() > 1 && msg.substring(0, 2).equals(PLAYER2_SCORE)){
    		p2.setScore(p2.getScore() + Float.parseFloat(msg.substring(2, msg.length())));
    		player2ScoreView.setText(String.valueOf(p2.getScore()));
    	}
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras().getString(SearchHost.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                btService.connect(device);
                
            } else{
            	finish();
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
        		setupBTService();
        		ensureDiscoverable();
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Open bluetooth to start multiplayer game.", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        case REQUEST_DISCOVERABLE:
        	if (resultCode == 300) {
	        	if(!isHost){
	    			// Search for other players
	                Intent serverIntent = new Intent(this, SearchHost.class);
	                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	        	}
        	}
        	break;
        }
    }

	public boolean isPause() {
		return isPause;
	}

	@Override
	public void onBackPressed() {
		btService.stop();
		finish();
	}
	private class BoardAdapter extends BaseAdapter {
		private static final int SELECTED_CHIP_BG = 0xffe0b604;
		Activity activity;
		Map<String, Tile> board;
		int tileSize;
		Drawable selectedImgDrawable, blankTile;
		Map<Integer, Drawable> operatorDrawables = new HashMap<Integer, Drawable>();

		public BoardAdapter(Activity activity, Map<String, Tile> board,
				int tileSize) {
			this.activity = activity;
			this.board = board;
			this.tileSize = tileSize;
			selectedImgDrawable = getScaledDrawable(activity.getResources().getDrawable(R.drawable.selected_chip));
			blankTile = getScaledDrawable(activity.getResources().getDrawable(R.drawable.blank));

			operatorDrawables.put(Operator.ADDITION,
								getScaledDrawable(activity.getResources().getDrawable(R.drawable.add)));
			operatorDrawables.put(Operator.DIFFERENCE,
								getScaledDrawable(activity.getResources().getDrawable(R.drawable.minus)));
			operatorDrawables.put(Operator.MULTIPLICATION,
								getScaledDrawable(activity.getResources().getDrawable(R.drawable.multiply)));
			operatorDrawables.put(Operator.DIVISION,
								getScaledDrawable(activity.getResources().getDrawable(R.drawable.divide)));

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return SIZE * SIZE;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder view;
			if (convertView == null) {
				view = new ViewHolder();
				convertView = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.board_tile, null);
				view.parent = (ViewGroup) convertView.findViewById(R.id.tileView);
				view.tile = (ImageView) view.parent.findViewById(R.id.tile);
				view.chip = (TextView) view.parent.findViewById(R.id.chip);
				view.chip.getLayoutParams().height = tileSize;
				view.chip.getLayoutParams().width = tileSize;
				convertView.setTag(view);
			} else {
				view = (ViewHolder) convertView.getTag();
			}
			final int row = position / SIZE;
			final int col = position - row * SIZE;
			final String pos = String.valueOf(row) + String.valueOf(col);
			final Tile t = board.get(pos);
//			Log.d("ranny", "tap row "+row+"   col "+col);
			if (t == null) {
				view.tile.setBackgroundDrawable(blankTile);
			} else {
				view.tile.setBackgroundDrawable(operatorDrawables.get(t.getOperation()));

				if (damath.hasSelectedChip() && isTileHasSelectedChip(row, col)) {
					view.tile.setImageDrawable(selectedImgDrawable);
				} else {
					view.tile.setImageDrawable(null);
				}
				if (t.getChip() == null) {
					view.chip.setBackgroundResource(0);
					view.chip.setText("");
				} else {
					if (t.getChip().getOwner() == p1) {
						if(t.getChip().isDama()){
							view.chip.setBackgroundResource(R.drawable.dama_chip_red);
						} else{
							view.chip.setBackgroundResource(R.drawable.chip_red);
						}
						view.chip.setText(String.valueOf(p1.getChips().get(pos).getValue()));
					} else {
						if(t.getChip().isDama()){
							view.chip.setBackgroundResource(R.drawable.dama_chip_blue);
						} else{
							view.chip.setBackgroundResource(R.drawable.chip_blue);
						}
						view.chip.setText(String.valueOf(p2.getChips().get(pos).getValue()));
					}
				}
				boolean isCurrentAttackerValid = (p1.isAttacking() && isHost) || (p2.isAttacking() && !isHost);
				if(isCurrentAttackerValid){
					canSelectChip = true;
				}else if(isAnswering && isCurrentAttackerValid){
					canSelectChip = false;
				} else{
					canSelectChip = false;
				}
				
//				if((t.getChip().getOwner() == p1 && isHost) || (t.getChip().getOwner() == p2 && !isHost)){
					if (tileClickListeners.get(pos) == null) {
						tileClickListeners.put(pos, new OnClickListener() {
							@Override
							public void onClick(View v) {
								if(v != null){
									final String tmp = String.valueOf(DO_MOVE)+reversePosition(pos);
									btService.write(tmp.getBytes());
								}
								
								Log.d("ranny", "tap row "+row+"   col "+col);
								
								Player attacker = p1.isAttacking() ? p1 : p2;
								Player opponent = !p1.isAttacking() ? p1 : p2;
								if (damath.hasSelectedChip()) {
									if (isTileHasSelectedChip(row, col)) {
										if (!damath.canAttackAgain()) {
											damath.setSelectedChip(null);
											view.tile.setImageDrawable(null);
										}
										return;
									}

									view.tile.setBackgroundColor(SELECTED_CHIP_BG);
									if (damath.move(attacker, opponent, row, col)) {
										if (!damath.canAttackAgain()) {
//											attacker.setAttacking(false);
//											opponent.setAttacking(true);
//											attackerView.setText(opponent.getName());
											timer.changeAttacker();
											timer.start();
										}
										player1ScoreView.setText(String.valueOf(p1
												.getScore()));
										player2ScoreView.setText(String.valueOf(p2
												.getScore()));
										notifyDataSetChanged();
									} else {
										view.tile.setBackgroundDrawable(
												operatorDrawables.get(t.getOperation()));
									}
								} else {
									Chip c = t.getChip();
									if (c != null && c.getOwner() == attacker) {
										damath.setSelectedChip(t.getChip());
										view.tile.setImageDrawable(selectedImgDrawable);
										Log.d("ranny", "selected chip is set row: "
												+ row + " col: " + col);
									}
								}
							}

						});
					}

					view.tile.setEnabled(isPause() || !canSelectChip ? false : true);
					view.tile.setOnClickListener(isPause() || !canSelectChip ? null : tileClickListeners.get(pos));
//				}
			}

			return convertView;
		}

		private boolean isTileHasSelectedChip(int row, int col) {
			return Character.getNumericValue(damath.getSelectedChip().getPosition().charAt(0)) == row
					&& Character.getNumericValue(damath.getSelectedChip().getPosition().charAt(1)) == col;
		}

		private Drawable getScaledDrawable(Drawable d) {
			return new BitmapDrawable(getResources(),Bitmap.createScaledBitmap(((BitmapDrawable) d).getBitmap(),
							tileSize, tileSize, true));
		}

		private class ViewHolder {
			ViewGroup parent;
			ImageView tile;
			TextView chip;
		}
	}
}

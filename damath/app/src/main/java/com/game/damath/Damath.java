package com.game.damath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.game.damath.DamathController.AttackListener;
import com.game.damath.DamathController.GameOverListener;

public class Damath extends Activity {
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

    private final long ATTACK_INTERVAL = 15 * 1000; // 30 seconds

    // Key names received from the BluetoothChatService Handler 
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
		
	private static final int SIZE = 8;

	String p2ChipsPos[] = { "00:2", "02:5", "04:8", "06:11", "11:7", "13:10",
			"15:3", "17:0", "20:4", "22:1", "24:6", "26:9" };
	String p1ChipsPos[] = { "51:9", "53:6", "55:1", "57:4", "60:0", "62:3",
			"64:10", "66:7", "71:11", "73:8", "75:5", "77:2" };

//	 String p2ChipsPos[] = { "20:4", "22:1", "24:6", "26:9",
//			 		"60:7", "62:10", "64:3", "66:0" };
//	 String p1ChipsPos[] = { "15:9", "11:6"};

	DamathController damath;
	Player p1, p2;
	Dialog writeAnswer;
	BoardAdapter boardAdapter;
	
	TextView player1ScoreView, player2ScoreView, player1Name, player2Name, attackerView, attackTimer;
	ImageView pausePlay;
	Bitmap pauseImg, playImg;
	boolean isPause;
	
	AttackTimer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.damath_board);
		String p1Name, p2Name;
		
		SharedPreferences sharedPref = getSharedPreferences(DamathSettings.DAMATH_SHARED_PREF, MODE_PRIVATE);
		p1Name = sharedPref.getString(DamathSettings.PLAYER1_NAME, "RED");
		p2Name = sharedPref.getString(DamathSettings.PLAYER2_NAME, "BLUE");

		player1Name = (TextView) findViewById(R.id.player1_name);
		player2Name = (TextView) findViewById(R.id.player2_name);
		player1ScoreView = (TextView) findViewById(R.id.player1_score);
		player2ScoreView = (TextView) findViewById(R.id.player2_score);
		attackerView = (TextView) findViewById(R.id.attacking_player);
		attackTimer = (TextView) findViewById(R.id.attack_timer);
		
		p1ChipsPos = shuffleChipValuePositions(p1ChipsPos);
		p2ChipsPos = shuffleChipValuePositions(p2ChipsPos);

		p1 = new Player(p1Name, p1ChipsPos);
		p1.setMoveDirectionUp(true);
		p1.setAttacking(true);
		player1Name.setText(p1Name);

		p2 = new Player(p2Name, p2ChipsPos);
		p2.setMoveDirectionUp(false);
		player2Name.setText(p2Name);

		initDamath(p1, p2);
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
				
				String op = "";
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
				
				writeAnswer.findViewById(R.id.answerBtn).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						String ans = ((EditText) writeAnswer.findViewById(R.id.answer)).getText().toString();
						float score = Float.parseFloat(String.format("%.2f", damath.getAttackScore(attackerChip, opponentChip, operation)));
						if(!ans.equals("") && Float.parseFloat(ans) == score){
							attacker.setScore(attacker.getScore() + score);
							TextView scoreView = null;
							if(attacker == Damath.this.p1){
								scoreView = Damath.this.player1ScoreView;
							}else{
								scoreView = Damath.this.player2ScoreView;
							}
							scoreView.setText(String.valueOf(attacker.getScore()));
						} else{
							Toast.makeText(Damath.this, "Incorrect answer.", Toast.LENGTH_SHORT).show();
						}
						writeAnswer.dismiss();
					}
				});
				writeAnswer.show();
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
				
				SharedPreferences sharedPref = getSharedPreferences(DamathSettings.DAMATH_SHARED_PREF, MODE_PRIVATE);
		        SharedPreferences.Editor prefEditor = sharedPref.edit();
		        
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

		GridView board = (GridView) findViewById(R.id.board);
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


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.damath_board, menu);
		return true;
	}

	public boolean isPause() {
		return isPause;
	}
	
	class BoardAdapter extends BaseAdapter {
		private static final int SELECTED_CHIP_BG = 0xffe0b604;
		Activity activity;
		Map<String, Tile> board;
		int tileSize;
		Drawable selectedImgDrawable, blankTile;
		Map<Integer, Drawable> operatorDrawables = new HashMap<Integer, Drawable>();
		Map<String, OnClickListener> tileClickListeners = new HashMap<String, View.OnClickListener>();

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
			return SIZE * SIZE;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
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

				if (tileClickListeners.get(pos) == null) {
					tileClickListeners.put(pos, new OnClickListener() {
						@Override
						public void onClick(View v) {
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
//										attacker.setAttacking(false);
//										opponent.setAttacking(true);
//										attackerView.setText(opponent.getName());
										
										timer.changeAttacker();
										timer.start();
									}
									player1ScoreView.setText(String.valueOf(p1.getScore()));
									player2ScoreView.setText(String.valueOf(p2.getScore()));
									notifyDataSetChanged();
								} else {
									view.tile.setBackgroundDrawable(operatorDrawables.get(t.getOperation()));
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

				view.tile.setEnabled(isPause() ? false : true);
				view.tile.setOnClickListener(isPause() ? null : tileClickListeners.get(pos));

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

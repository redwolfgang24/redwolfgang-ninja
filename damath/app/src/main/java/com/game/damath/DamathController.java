package com.game.damath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class DamathController {
	String addSignPos[] = { 	"06", "15", "22", "31", "46", "55", "62", "71" };
	String minusSignPos[] = { 	"04", "17", "20", "33", "44", "57", "60", "73" };
	String multiplySignPos[] = {"00", "13", "24", "37", "40", "53", "64", "77" };
	String divideSignPos[] = { 	"02", "11", "26", "35", "42", "51", "66", "75" };
	
	/** Callback for processing the attack event*/
	public interface AttackListener{
		public void onAttack(Player attacker, Chip attackerChip, Chip opponentChip, int operation);
	}
	
	/** Callback for processing the attack event*/
	public interface GameOverListener{
		public void gameOver(Player win);
	}
	
	/** Exception for invalid moves done*/
	private class InvalidMove extends Exception{ }
	
	Map<String, Tile> board = new HashMap<String, Tile>();

	Player p1, p2;
	boolean isGameEnd;
	boolean canAttackAgain;
	Chip selectedChip, opponentChipAttacked;
	AttackListener attackListener;
	GameOverListener gameOverListener;
	
	public DamathController(Player player1, Player player2) {
		p1 = player1;
		p2 = player2;
		initBoard();
		attackListener = new AttackListener() {
			@Override
			public void onAttack(Player attacker, Chip attackerChip, Chip opponentChip, int operation) {
			}
		};
		
		gameOverListener = new GameOverListener() {
			@Override
			public void gameOver(Player win) {
			}
		};
	}
/*
	public DamathController(Player player1, Player player2, String addSignPos[],
							String minusSignPos[], String multiplySignPos[], String divideSignPos[]) {
		p1 = player1;
		p2 = player2;
		this.addSignPos = addSignPos;
		this.minusSignPos = minusSignPos;
		this.multiplySignPos = multiplySignPos;
		this.divideSignPos = divideSignPos;
		
		initBoard();
		attackListener = new AttackListener() {
			@Override
			public void onAttack(Player attacker, float score) {
			}
		};
		
		gameOverListener = new GameOverListener() {
			@Override
			public void gameOver(Player win) {
			}
		};
	}
*/
	public void initBoard() {
		int eachSignCount = addSignPos.length;
		for (int i = 0; i < eachSignCount; i++) {
			Chip c1 = p1.getChips().get(addSignPos[i]);
			if (c1 == null) {
				c1 = p2.getChips().get(addSignPos[i]);
			}
			board.put(addSignPos[i], new Tile(c1, Operator.ADDITION));

			Chip c2 = p1.getChips().get(minusSignPos[i]);
			if (c2 == null) {
				c2 = p2.getChips().get(minusSignPos[i]);
			}
			board.put(minusSignPos[i], new Tile(c2, Operator.DIFFERENCE));

			Chip c3 = p1.getChips().get(multiplySignPos[i]);
			if (c3 == null) {
				c3 = p2.getChips().get(multiplySignPos[i]);
			}
			board.put(multiplySignPos[i], new Tile(c3, Operator.MULTIPLICATION));

			Chip c4 = p1.getChips().get(divideSignPos[i]);
			if (c4 == null) {
				c4 = p2.getChips().get(divideSignPos[i]);
			}
			board.put(divideSignPos[i], new Tile(c4, Operator.DIVISION));
		}
	}

	public void setAttackListener(AttackListener attackListener){
		this.attackListener = attackListener;
	}

	public void setGameOverListener(GameOverListener gameOverListener) {
		this.gameOverListener = gameOverListener;
	}

	public Map<String, Tile> getBoard() {
		return board;
	}

	public void setBoard(Map<String, Tile> board) {
		this.board = board;
	}

	public boolean isGameEnd() {
		return isGameEnd;
	}

	public void setGameEnd(boolean isGameEnd) {
		this.isGameEnd = isGameEnd;
	}
	
	public boolean move(Player attacker, Player opponent, int row, int col) {
		String movePos = String.valueOf(row) + String.valueOf(col);
		int selectedChipRow = Character.getNumericValue(selectedChip.getPosition().charAt(0));
		int selectedChipCol = Character.getNumericValue(selectedChip.getPosition().charAt(1));
		
		opponentChipAttacked = null;
		if(selectedChip.isDama()){
			if(canDamaMoveAttackAt(opponent, selectedChipRow, selectedChipCol, row, col)){
				if(opponentChipAttacked == null){
					Log.d("ranny", "dama move");
					moveSelectedChipToMovePosition(attacker, movePos);
					selectedChip = null;
					return true;
				} else{
					canAttackAgain = false;
//					attacker.setScore(attacker.getScore() + getAttackScore(selectedChip, opponentChipAttacked,
//																			board.get(movePos).getOperation()));
					attackListener.onAttack(attacker, selectedChip, opponentChipAttacked,
													board.get(movePos).getOperation());
					Log.d("ranny", "playerScore "+attacker.getScore());
					
					removeOpponentChip(opponent, opponentChipAttacked.getPosition());
					moveSelectedChipToMovePosition(attacker, movePos);
					
					if(opponent.getChips().size() == 0){
						gameOverListener.gameOver(attacker);
					}
					
					selectedChipRow = Character.getNumericValue(selectedChip.getPosition().charAt(0));
					selectedChipCol = Character.getNumericValue(selectedChip.getPosition().charAt(1));
					
					Log.d("ranny", "dama chip pos row: "+selectedChipRow+"; col: "+selectedChipCol);
					if(getDamaPositionsToAttack(opponent, selectedChipRow, selectedChipCol).length > 0){
						canAttackAgain = true;
						Log.d("ranny", "can attack again");
					}
					
					if(!canAttackAgain){
						selectedChip = null;
					}
					
					return true;
				}
				
			// only 1 chip left and cannot move or attack
			} else if(attacker.getChips().size() == 1){
				gameOverListener.gameOver(opponent);
			}
		} else{
			if(!canAttackAgain && canMoveAt(attacker.isMoveDirectionUp(), selectedChipRow, selectedChipCol, row, col)){
				moveSelectedChipToMovePosition(attacker, movePos);
				if((attacker.isMoveDirectionUp() && row == 0) || (!attacker.isMoveDirectionUp() && row == 7)){
					attacker.getChips().get(selectedChip.getPosition()).setDama(true);
				}
				selectedChip = null;
				return true;
			} else if(canAttackAt(opponent, selectedChipRow, selectedChipCol, row, col)){
				canAttackAgain = false;
				Log.d("ranny", "can attack at row "+row+"    col "+col);
				
//				attacker.setScore(attacker.getScore() + getAttackScore(selectedChip, opponentChipAttacked,
//																		board.get(movePos).getOperation()));
				attackListener.onAttack(attacker, selectedChip, opponentChipAttacked,
												board.get(movePos).getOperation());
				Log.d("ranny", "playerScore "+attacker.getScore());
				
				removeOpponentChip(opponent, opponentChipAttacked.getPosition());
				moveSelectedChipToMovePosition(attacker, movePos);
				
				if(opponent.getChips().size() == 0){
					gameOverListener.gameOver(attacker);
				}
				
				selectedChipRow = Character.getNumericValue(selectedChip.getPosition().charAt(0));
				selectedChipCol = Character.getNumericValue(selectedChip.getPosition().charAt(1));
				
				if(getPositionsToAttack(opponent, selectedChipRow, selectedChipCol).length > 0){
					canAttackAgain = true;
				}
				
				if(!canAttackAgain){
					if((attacker.isMoveDirectionUp() && row == 0) || (!attacker.isMoveDirectionUp() && row == 7)){
						Log.d("ranny", "selected chip became a dama "+selectedChip.getPosition());
						attacker.getChips().get(selectedChip.getPosition()).setDama(true);
					}
					selectedChip = null;
				}
				
				return true;
				
			// only 1 chip left and cannot move or attack
			} else if(attacker.getChips().size() == 1){
				gameOverListener.gameOver(opponent);
			}
		}

		return false;
	}
	
	private void moveSelectedChipToMovePosition(Player attacker, String movePos){
		attacker.getChips().remove(selectedChip.getPosition());
		board.get(selectedChip.getPosition()).setChip(null);
		
		selectedChip.setPosition(movePos);
		
		attacker.getChips().put(movePos, selectedChip);
		board.get(movePos).setChip(selectedChip);
	}
	
	private void removeOpponentChip(Player opponent, String chipPosition){
		board.get(chipPosition).setChip(null);
		opponent.getChips().remove(chipPosition);
	}
	
	private boolean canDamaMoveAttackAt(Player opponent, int selectedChipRow,
											int selectedChipCol, int row, int col){
		float rise = selectedChipRow - row;
		float run = selectedChipCol - col;
		String move = String.valueOf(row) + String.valueOf(col);
		if(selectedChipRow == row || selectedChipCol == col || Math.abs(rise / run) != 1f ||
			(board.get(move) != null && board.get(move).getChip() != null)){
			return false;
		}
		
		boolean isMovePosUpper = selectedChipRow > row ? true : false;
		boolean isMovePosLeft = selectedChipCol > col ? true : false;

		boolean validMove = true;
		
		if(isMovePosUpper && isMovePosLeft){
			Log.d("ranny", "dama move upper left");
			for(int i = selectedChipRow - 1, j = selectedChipCol - 1; i >= row && j >= col; i--, j--){
				move = String.valueOf(i) + String.valueOf(j);
				Log.d("ranny", "dama move "+move);
				/*
				if(board.get(move) != null && board.get(move).getChip() != null){
					if(board.get(move).getChip().getOwner() == opponent){
						if(opponentChipAttacked == null){ // found a chip to attack
							Log.d("ranny", "found a chip to attack at "+move);
							opponentChipAttacked = board.get(move).getChip();
						} else{ // there is an opponent blocking chip
							opponentChipAttacked = null;
							validMove = false;
							break;
						}
					} else{ // there is a player chip blocking the move
						validMove = false;
						break;
					}
				}
				*/
				try {
					checkDamaMoveAt(move, opponent);
				} catch (InvalidMove e) {
					validMove = false;
					break;
				}
			}
		} else if(!isMovePosUpper && isMovePosLeft){
			Log.d("ranny", "dama move lower left");
			for(int i = selectedChipRow + 1, j = selectedChipCol - 1; i <= row && j >= col; i++, j--){
				move = String.valueOf(i) + String.valueOf(j);
				Log.d("ranny", "dama move "+move);
				try {
					checkDamaMoveAt(move, opponent);
				} catch (InvalidMove e) {
					validMove = false;
					break;
				}
			}
		} else if(isMovePosUpper && !isMovePosLeft){
			Log.d("ranny", "dama move upper right");
			for(int i = selectedChipRow - 1, j = selectedChipCol + 1; i >= row && j <= col; i--, j++){
				move = String.valueOf(i) + String.valueOf(j);
				Log.d("ranny", "dama move "+move);
				try {
					checkDamaMoveAt(move, opponent);
				} catch (InvalidMove e) {
					validMove = false;
					break;
				}
			}
		} else if(!isMovePosUpper && !isMovePosLeft){
			Log.d("ranny", "dama move lower right");
			for(int i = selectedChipRow + 1, j = selectedChipCol + 1; i <= row && j <= col; i++, j++){
				move = String.valueOf(i) + String.valueOf(j);
				Log.d("ranny", "dama move "+move);
				try {
					checkDamaMoveAt(move, opponent);
				} catch (InvalidMove e) {
					validMove = false;
					break;
				}
			}
		}
		
		return validMove;
	}
	
	private void checkDamaMoveAt(String move, Player opponent) throws InvalidMove{
		if(board.get(move) != null && board.get(move).getChip() != null){
			if(board.get(move).getChip().getOwner() == opponent){
				if(opponentChipAttacked == null){ // found a chip to attack
					Log.d("ranny", "found a chip to attack at "+move);
					opponentChipAttacked = board.get(move).getChip();
				} else{ // there is an opponent blocking chip
					opponentChipAttacked = null;
					throw new InvalidMove();
				}
			} else{ // there is a player chip blocking the move
				throw new InvalidMove();
			}
		}
	}
	
	private boolean canMoveAt(boolean isMoveDirectionUp, int selectedChipRow, int selectedChipCol, int row, int col){
		int allowedRow = isMoveDirectionUp ? selectedChipRow - 1 : selectedChipRow + 1;
		int allowedCol = selectedChipCol > col ? selectedChipCol - 1 : selectedChipCol + 1;
		String move = String.valueOf(row) + String.valueOf(col);
		
		return allowedRow == row && allowedCol == col && board.get(move) != null && board.get(move).getChip() == null;
	}
	
	private boolean canAttackAt(Player opponent, int selectedChipRow, int selectedChipCol, int row, int col){
		int allowedOpponentChipToAttackRow = selectedChipRow > row ? selectedChipRow - 1 : selectedChipRow + 1;
		int allowedOpponentChipToAttackCol = selectedChipCol > col ? selectedChipCol - 1 : selectedChipCol + 1;
		int allowedRow = selectedChipRow > row ? selectedChipRow - 2 : selectedChipRow + 2;
		int allowedCol = selectedChipCol > col ? selectedChipCol - 2 : selectedChipCol + 2;
		String opponentChipToAttackPosition = String.valueOf(allowedOpponentChipToAttackRow) + String.valueOf(allowedOpponentChipToAttackCol);
		String allowedAttackPosition = String.valueOf(allowedRow) + String.valueOf(allowedCol);
		
		boolean foundOpponentChip = false;
		if(board.get(opponentChipToAttackPosition) != null && board.get(opponentChipToAttackPosition).getChip() != null &&
			board.get(opponentChipToAttackPosition).getChip().getOwner() == opponent){
			foundOpponentChip = true;
			opponentChipAttacked = board.get(opponentChipToAttackPosition).getChip();
		}
		
		return allowedRow == row && allowedCol == col &&
				// Check if there is an opponent chip that can be attacked
				foundOpponentChip &&
				// Check if the position to move don't have a chip
				(board.get(allowedAttackPosition) != null &&
				board.get(allowedAttackPosition).getChip() == null);
	}
	
	private String[] getPositionsToAttack(Player opponent, int selectedChipRow, int selectedChipCol) {
		List<String> attackMoves = new ArrayList<String>();
		
		int leftColumn = selectedChipCol - 1 > 0 ? selectedChipCol - 1 : 0;
		int rightColumn = selectedChipCol + 1 < 7 ? selectedChipCol + 1 : 7;
		int upperRow = selectedChipRow - 1 > 0 ? selectedChipRow - 1 : 0;
		int lowerRow = selectedChipRow - 1 < 7 ? selectedChipRow + 1 : 0;
		String move = null;
		
		move = getAttackPosAt(opponent, upperRow, leftColumn, upperRow - 1, leftColumn - 1);
		if(move != null){
			attackMoves.add(move);
		}
		
		move = getAttackPosAt(opponent, upperRow, rightColumn, upperRow - 1, rightColumn + 1);
		if(move != null){
			attackMoves.add(move);
		}
		
		move = getAttackPosAt(opponent, lowerRow, leftColumn, lowerRow + 1, leftColumn - 1);
		if(move != null){
			attackMoves.add(move);
		}
		
		move = getAttackPosAt(opponent, lowerRow, rightColumn, lowerRow + 1, rightColumn + 1);
		if(move != null){
			attackMoves.add(move);
		}
		
		Log.d("ranny", "moves "+attackMoves.toString());
		
		return attackMoves.toArray(new String[attackMoves.size()]);
	}
	
	private String[] getDamaPositionsToAttack(Player opponent, int selectedChipRow, int selectedChipCol) {
		List<String> attackMoves = new ArrayList<String>();
		int moveRow, moveCol;
		
		String move;
		for(int i = 0; i <= 7; i++){
			opponentChipAttacked = null;
			moveRow = selectedChipRow - i;
			moveCol = selectedChipCol - i;
			if(isPositionInsideBoard(moveRow) && isPositionInsideBoard(moveCol)){
				move = canDamaMoveAttackAt(opponent, selectedChipRow, selectedChipCol, moveRow, moveCol) ?
						String.valueOf(moveRow) + String.valueOf(moveCol) : null;
				if(opponentChipAttacked != null && move != null){
					attackMoves.add(move);
				}
			}
			
			opponentChipAttacked = null;
			moveRow = selectedChipRow - i;
			moveCol = selectedChipCol + i;
			if(isPositionInsideBoard(moveRow) && isPositionInsideBoard(moveCol)){
				move = canDamaMoveAttackAt(opponent, selectedChipRow, selectedChipCol, moveRow, moveCol) ?
						String.valueOf(moveRow) + String.valueOf(moveCol) : null;
				if(opponentChipAttacked != null && move != null){
					attackMoves.add(move);
				}
			}
			
			opponentChipAttacked = null;
			moveRow = selectedChipRow + i;
			moveCol = selectedChipCol - i;
			if(isPositionInsideBoard(moveRow) && isPositionInsideBoard(moveCol)){
				move = canDamaMoveAttackAt(opponent, selectedChipRow, selectedChipCol, moveRow, moveCol) ?
						String.valueOf(moveRow) + String.valueOf(moveCol) : null;
				if(opponentChipAttacked != null && move != null){
					attackMoves.add(move);
				}
			}
			
			opponentChipAttacked = null;
			moveRow = selectedChipRow + i;
			moveCol = selectedChipCol + i;
			if(isPositionInsideBoard(moveRow) && isPositionInsideBoard(moveCol)){
				move = canDamaMoveAttackAt(opponent, selectedChipRow, selectedChipCol, moveRow, moveCol) ?
						String.valueOf(moveRow) + String.valueOf(moveCol) : null;
				if(opponentChipAttacked != null && move != null){
					attackMoves.add(move);
				}
			}
		}
		
		Log.d("ranny", "moves "+attackMoves.toString());
		
		return attackMoves.toArray(new String[attackMoves.size()]);
	}
	
	private String getAttackPosAt(Player opponent, int row, int col, int checkAtRow, int checkAtCol){
		String move = String.valueOf(row) + String.valueOf(col);
		Log.d("ranny", "checking opponent chip at "+move);
		if (board.get(move) != null && board.get(move).getChip() != null &&
				board.get(move).getChip().getOwner() == opponent){
				
				move = String.valueOf(checkAtRow) + String.valueOf(checkAtCol);
				Log.d("ranny", "checking attack move at "+move);
				if(board.get(move) != null && board.get(move).getChip() == null) {
					Log.d("ranny", "attack move found");
					return move;
				}
				
			}
		return null;
	}
	
	public float getAttackScore(Chip attacker, Chip opponent, int operation){
    	float score = 0f;
    	float attackerChipVal = attacker.getValue();
    	float opponentChipVal = opponent.getValue();
		switch (operation) {
		case Operator.ADDITION:
			score = attackerChipVal + opponentChipVal;
			break;
		case Operator.DIFFERENCE:
			score = attackerChipVal - opponentChipVal;
			break;
		case Operator.MULTIPLICATION:
			score = attackerChipVal * opponentChipVal;
			break;
		case Operator.DIVISION:
			if(opponentChipVal == 0){
				score = 0;
			} else{
				score = attackerChipVal / opponentChipVal;
			}
			break;
		}
		if(attacker.isDama()){
			score *= 2;
		}
		if(opponent.isDama()){
			score *= 2;
		}
		return score;
	}

	public Chip getSelectedChip() {
		return selectedChip;
	}

	public void setSelectedChip(Chip selectedChip) {
		this.selectedChip = selectedChip;
	}
	
	public boolean hasSelectedChip(){
		return selectedChip != null;
	}
	
	private boolean isPositionInsideBoard(int pos) {
		return pos >= 0 && pos <= 7;
	}
	
	public boolean canAttackAgain() {
		return canAttackAgain;
	}

	public static void main(String[] args) {
		// DamathController damath = new DamathController(new Player(), new
		// Player());
		//
		// damath.initBoard();

	}
}

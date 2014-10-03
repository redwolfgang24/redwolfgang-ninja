package com.game.damath;

import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AttackTimer extends CountDownTimer{
	TextView attackTimer, attackerName;
	Player attacker, opponent;
	DamathController damathController;
	BaseAdapter adapter;
	Handler handler;
	Runnable updateTimerToZero;
	
	public AttackTimer(long millisInFuture, long countDownInterval, TextView attackTimer, TextView attackerName,
						Player attacker, Player opponent, DamathController damathController, BaseAdapter adapter) {
		super(millisInFuture, countDownInterval);
		this.attackTimer = attackTimer;
		this.attackerName = attackerName;
		this.attacker = attacker;
		this.opponent = opponent;
		this.damathController = damathController;
		this.adapter = adapter;
		handler = new Handler();
		updateTimerToZero = new Runnable(){
			@Override
			public void run() {
				AttackTimer.this.attackTimer.setText("00:00");
			}
		};
		
	}

	@Override
	public void onFinish() {
		changeAttacker();
		damathController.setSelectedChip(null);
		adapter.notifyDataSetChanged();
		start();
	}

	@Override
	public void onTick(long millisUntilFinished) {
		attackTimer.setText("00:"+String.format("%02d", millisUntilFinished/1000));
		if(((int)(millisUntilFinished/1000)) == 1){
			// Display the 00:00 in the timer after 1 seconds
			handler.postDelayed(updateTimerToZero, 1000);
		}
		
	}
	
	public void changeAttacker(){
		attacker.setAttacking(false);
		opponent.setAttacking(true);
		attackerName.setText(opponent.getName());
		
		Player tmp = attacker;
		attacker = opponent;
		opponent = tmp;
	}
	
}

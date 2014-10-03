package com.game.damath;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class HighScore extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.high_scores);
		
		String highScores[] = {DamathSettings.HIGH_SCORE_1, DamathSettings.HIGH_SCORE_2, DamathSettings.HIGH_SCORE_3,
				DamathSettings.HIGH_SCORE_4, DamathSettings.HIGH_SCORE_5};
		String highScoreName[] = {DamathSettings.HIGH_SCORE_NAME_1, DamathSettings.HIGH_SCORE_NAME_2, DamathSettings.HIGH_SCORE_NAME_3,
						DamathSettings.HIGH_SCORE_NAME_4, DamathSettings.HIGH_SCORE_NAME_5};
		
		int nameView[] = {R.id.highScoreName1, R.id.highScoreName2, R.id.highScoreName3, R.id.highScoreName4, R.id.highScoreName5};
		int scoreView[] = {R.id.highScore1, R.id.highScore2, R.id.highScore3, R.id.highScore4, R.id.highScore5};
		
		SharedPreferences sharedPref = getSharedPreferences(DamathSettings.DAMATH_SHARED_PREF, MODE_PRIVATE);
		SharedPreferences.Editor prefEditor = sharedPref.edit();
		
		int highScoreCount = highScores.length;
		for(int i = 0; i< highScoreCount ; i++){
			((TextView)findViewById(nameView[i])).setText(sharedPref.getString(highScoreName[i], "Player"+i));
			((TextView)findViewById(scoreView[i])).setText(String.valueOf(sharedPref.getFloat(highScores[i], 100f)));
		}
		
		
	}
}

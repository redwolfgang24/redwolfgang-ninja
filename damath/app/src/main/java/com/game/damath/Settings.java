package com.game.damath;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class Settings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		final TextView player1Name = (TextView) findViewById(R.id.player1_name);
		final TextView player2Name = (TextView) findViewById(R.id.player2_name);

		final SharedPreferences sharedPref = getSharedPreferences(DamathSettings.DAMATH_SHARED_PREF, MODE_PRIVATE);
        final SharedPreferences.Editor prefEditor = sharedPref.edit();
        
        player1Name.setText(sharedPref.getString(DamathSettings.PLAYER1_NAME, "RED"));
        player2Name.setText(sharedPref.getString(DamathSettings.PLAYER2_NAME, "BLUE"));
		
		findViewById(R.id.save).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		prefEditor.putString(DamathSettings.PLAYER1_NAME, player1Name.getText().toString());
        		prefEditor.putString(DamathSettings.PLAYER2_NAME, player2Name.getText().toString());
        		prefEditor.commit();
        		finish();
			}
		});
	}
}

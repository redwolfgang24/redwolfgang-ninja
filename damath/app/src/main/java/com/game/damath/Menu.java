package com.game.damath;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class Menu extends Activity {
	Dialog connectionSelect;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
//		
//		// Get the screen width of the device
//		int boardImgSize = getWindowManager().getDefaultDisplay().getWidth() / 2;
//		ImageView boardImg = (ImageView) findViewById(R.id.boardImg);
//		LayoutParams params = new LayoutParams(boardImgSize, boardImgSize);
//		boardImg.setLayoutParams(params);
		
		connectionSelect = createDialog();
		
		findViewById(R.id.play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Menu.this, Damath.class));
			}
		});
		
		findViewById(R.id.highscore).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Menu.this, HighScore.class));
			}
		});
		
		findViewById(R.id.multiplayer).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent i = new Intent(Menu.this, Damath.class);
//				i.putExtra(Damath.EXTRA_MULTIPLAYER, true);
//				startActivity(i);
				connectionSelect.show();
			}
		});
		
		findViewById(R.id.settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Menu.this, Settings.class));
			}
		});
	}
	
	private Dialog createDialog(){
		// custom dialog
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.host_join_dialog);
		dialog.setTitle("Select Connection Type");
 
		Button host = (Button) dialog.findViewById(R.id.host);
		host.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(Menu.this, "Host", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(Menu.this, MultiplayerDamath.class);
				i.putExtra(MultiplayerDamath.EXTRA_HOST, true);
				startActivity(i);
				dialog.dismiss();
			}
		});
		
		Button join = (Button) dialog.findViewById(R.id.join);
		join.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(Menu.this, "Join", Toast.LENGTH_SHORT).show();
				Intent i = new Intent(Menu.this, MultiplayerDamath.class);
				i.putExtra(MultiplayerDamath.EXTRA_HOST, false);
				startActivity(i);
				dialog.dismiss();
			}
		});
		
		return dialog;
	}
}

package com.game.damath;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

public class DamathBoard extends GridView{

	private final int SIZE = 8;
	
	public DamathBoard(Context context) {
		this(context, null);		
	}
	

	public DamathBoard(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setNumColumns(SIZE);
		
		// Disable the scrolling of the gridview
		setOnTouchListener(new OnTouchListener(){
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        if(event.getAction() == MotionEvent.ACTION_MOVE){
		            return true;
		        }
		        return false;
		    }

		});
	}
}

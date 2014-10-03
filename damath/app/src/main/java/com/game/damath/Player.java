package com.game.damath;

import java.util.HashMap;
import java.util.Map;


public class Player {
    private float score;
    private boolean attacking;
    private boolean moveDirectionUp;
	private Map<String, Chip> getChips;
    private String name;


    public Player(String name){
    	this(name, null);
    }
    
	/**
     * Constructor that sets the chips of the player
     * 
     * @param chipsPositionAndValue
	 *		The values of this array should have a format "{row}{col}:{value}"<br>
	 *		Ex: "01:9" -> row: 0 col: 1 value: 9
     * 	
     */
    public Player(String name, String chipsPositionAndValue[]){
    	this.name = name;
    	getChips = new HashMap<String, Chip>();
    	if(chipsPositionAndValue != null){
    		initChips(chipsPositionAndValue);
    	}
    }
    
    
    public void initChips(String chipsPositionAndValue[]){
    	for(String posVal : chipsPositionAndValue){
    		int val = Integer.valueOf(posVal.substring(posVal.lastIndexOf(':')+1, posVal.length()));
    		String pos = posVal.substring(0, posVal.lastIndexOf(':'));
    		getChips.put(pos, new Chip(this, val, pos));
//    		Log.d("ranny", "pos "+pos+"    val "+val);
    	}
    }
    
    public float getScore() {
        return score;
    }
    public void setScore(float score) {
        this.score = score;
    }
    public boolean isAttacking() {
        return attacking;
    }
    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }
    
    public Map<String, Chip> getChips(){
    	return getChips;
    }

    public boolean isMoveDirectionUp() {
		return moveDirectionUp;
	}

	public void setMoveDirectionUp(boolean moveDirectionUp) {
		this.moveDirectionUp = moveDirectionUp;
	}
    public String getName() {
		return name;
	}
    public void setName(String name) {
		this.name = name;
	}
}

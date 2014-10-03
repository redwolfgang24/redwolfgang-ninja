package com.game.damath;


public class Chip {
    private int value;
    private String position;
    private Player owner;
    private boolean dama;
	
	public Chip(Player owner, int value, String position){
		this.owner = owner;
        this.value = value;
        this.position = position;
    }
    public int getValue() {
        return value;
    }
    public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public Player getOwner() {
		return owner;
	}
	public boolean isDama() {
		return dama;
	}
	public void setDama(boolean dama) {
		this.dama = dama;
	}
}

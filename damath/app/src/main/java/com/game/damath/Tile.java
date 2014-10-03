package com.game.damath;

public class Tile {
    
    Chip chip;
    int operation;
    
    public Tile(Chip chip, int operation){
        this.chip = chip;
        this.operation = operation;
    }
    
    public Chip getChip() {
        return chip;
    }
    public void setChip(Chip chip) {
        this.chip = chip;
    }
    public int getOperation() {
        return operation;
    }
    public void setOperation(int operation) {
        this.operation = operation;
    }
}

package com.digitalartsplayground.fantasycrypto.models;

public class LeaderBoardUnit {

    private String name;
    private float value;
    private long rank;

    public LeaderBoardUnit() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }
}

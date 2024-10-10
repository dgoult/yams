package com.limayrac.yams;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Parcelable {
    private String name;
    private boolean isIa;
    private String color;
    private String difficulty;

    public Player(String name, String color) {
        this.name = name;
        this.color = color;
        this.isIa = false; // Default to human player
        this.difficulty = null;
    }

    protected Player(Parcel in) {
        name = in.readString();
        isIa = in.readByte() != 0;
        color = in.readString();
        difficulty = in.readString();
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIa() {
        return isIa;
    }

    public void toggleIa() {
        this.isIa = !this.isIa;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDifficulty() {
        return difficulty;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeByte((byte) (isIa ? 1 : 0));
        parcel.writeString(color);
        parcel.writeString(difficulty);
    }
}
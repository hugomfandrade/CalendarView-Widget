package org.hugoandrade.calendarviewapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * Created by Hugo Andrade on 25/03/2018.
 */

public class Event implements Parcelable {

    private String mID;
    private String mTitle;
    private Calendar mDate;
    private int mColor;
    private boolean isCompleted;

    public Event(String id, String title, Calendar date, int color, boolean isCompleted) {
        mID = id;
        mTitle = title;
        mDate = date;
        mColor = color;
        this.isCompleted = isCompleted;
    }

    public String getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public Calendar getDate() {
        return mDate;
    }

    public int getColor() {
        return mColor;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    protected Event(Parcel in) {
        mID = in.readString();
        mTitle = in.readString();
        mColor = in.readInt();
        mDate = (Calendar) in.readSerializable();
        isCompleted = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mTitle);
        dest.writeInt(mColor);
        dest.writeSerializable(mDate);
        dest.writeByte((byte) (isCompleted ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}

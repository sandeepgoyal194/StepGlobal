package au.com.stepglobal.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import au.com.stepglobal.global.TripType;

/**
 * Created by hiten.bahri on 6/17/2017.
 */
public class TripObject implements Parcelable {

    String GUID;
    String userId;
    TripType tripType;
    String tripReason;
    long startTime;
    long stopTime;
    String status;

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public String getTripReason() {
        return tripReason;
    }

    public void setTripReason(String tripReason) {
        this.tripReason = tripReason;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(GUID);
        dest.writeString(userId);
        dest.writeParcelable(tripType, flags);
        dest.writeString(tripReason);
        dest.writeLong(startTime);
        dest.writeLong(stopTime);
        dest.writeString(status);
    }

    public TripObject() {
    }

    protected TripObject(Parcel in) {
        GUID = in.readString();
        userId = in.readString();
        tripType = in.readParcelable(TripType.class.getClassLoader());
        tripReason = in.readString();
        startTime = in.readLong();
        stopTime = in.readLong();
        status = in.readString();
    }

    public static final Creator<TripObject> CREATOR = new Creator<TripObject>() {
        @Override
        public TripObject createFromParcel(Parcel in) {
            return new TripObject(in);
        }

        @Override
        public TripObject[] newArray(int size) {
            return new TripObject[size];
        }
    };
}

package au.com.stepglobal.global;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hiten.bahri on 6/17/2017.
 */
public enum TripType implements Parcelable {
    PRIVATE("private"),
    BUSINESS("business");

    public final String display;

    TripType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display.toUpperCase();
    }

    public static TripType toTripType (String tripType) {
        try {
            return valueOf(tripType);
        } catch (Exception ex) {
            // For error cases
            return PRIVATE;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    public static final Parcelable.Creator<TripType> CREATOR = new Parcelable.Creator<TripType>() {

        public TripType createFromParcel(Parcel in) {
            return values()[in.readInt()];
        }

        public TripType[] newArray(int size) {
            return new TripType[size];
        }

    };

}

package au.com.stepglobal.global;

/**
 * Created by hiten.bahri on 16/06/2017.
 */

public enum BundleKey {
    USER_ID("kUserId"),
    TRIP_TYPE("kTripType"),
    TRIP_REASON("kTripReason"),
    TRIP_OBJECT("kTripObject");

    public final String key;

    BundleKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}

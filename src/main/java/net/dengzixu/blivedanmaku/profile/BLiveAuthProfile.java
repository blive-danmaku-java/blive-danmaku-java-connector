package net.dengzixu.blivedanmaku.profile;

public record BLiveAuthProfile(Long uid, String sessData) {

    public BLiveAuthProfile(long uid, String sessData) {
        this(Long.valueOf(uid), sessData);
    }

    public static BLiveAuthProfile getAnonymous() {
        return getDefault();
    }

    public static BLiveAuthProfile getDefault() {
        return new BLiveAuthProfile(0L, "");
    }
}

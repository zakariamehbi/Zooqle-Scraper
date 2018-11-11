package Zooqle;

public enum Qualities {
    _Low(1),
    _Med(2),
    _Std(3),
    _720p(4),
    _1080p(5),
    _Ultra(5),
    _3D(5);

    private int qualityId ;

    Qualities(int qualityId) {
        this.qualityId = qualityId;
    }

    public int getQualityId() {
        return qualityId;
    }
}

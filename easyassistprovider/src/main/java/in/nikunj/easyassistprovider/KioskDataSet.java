package in.nikunj.easyassistprovider;


class KioskDataSet {
    private String kioskName;
    private String kioskId;

    public KioskDataSet(String kioskName, String kioskId) {
        this.kioskName = kioskName;
        this.kioskId = kioskId;
    }

    public String getKioskName() {
        return kioskName;
    }

    public void setKioskName(String kioskName) {
        this.kioskName = kioskName;
    }

    public String getKioskId() {
        return kioskId;
    }

    public void setKioskId(String kioskId) {
        this.kioskId = kioskId;
    }
}

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KioskDataSet that = (KioskDataSet) o;

        if (kioskName != null ? !kioskName.equals(that.kioskName) : that.kioskName != null)
            return false;
        return kioskId != null ? kioskId.equals(that.kioskId) : that.kioskId == null;
    }

    @Override
    public int hashCode() {
        int result = kioskName != null ? kioskName.hashCode() : 0;
        result = 31 * result + (kioskId != null ? kioskId.hashCode() : 0);
        return result;
    }
}

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;

interface Tracker {
    void tambahData(int nilai, LocalDate tanggal);
    void resetData(LocalDate tanggal);
    int getTotal(LocalDate tanggal);
}

class RecordHari {
    private final LocalDate tanggal;
    private int volAir, jumlahPoo;
    private final ArrayList<String> detailAir, detailPoo;
    private boolean sudahAlert;

    public RecordHari(LocalDate tanggal) {
        this.tanggal = tanggal;
        this.detailAir = new ArrayList<>();
        this.detailPoo = new ArrayList<>();
    }

    public LocalDate getTanggal() { return tanggal; }
    public int getVolAir() { return volAir; }
    public void setVolAir(int v) { volAir = v; }
    public int getJumlahPoo() { return jumlahPoo; }
    public void setJumlahPoo(int j) { jumlahPoo = j; }
    public ArrayList<String> getDetailAir() { return detailAir; }
    public ArrayList<String> getDetailPoo() { return detailPoo; }
    public boolean isSudahAlert() { return sudahAlert; }
    public void setSudahAlert(boolean a) { sudahAlert = a; }
}

class SettingApp {
    private int mlPerGelas = 250;
    private int targetHarian = 2000;
    private int batasMax = 4000;
    private int batasMaxPoo = 5; // Batasan maksimal BAB per hari

    public int getMlPerGelas() { return mlPerGelas; }
    public void setMlPerGelas(int ml) { mlPerGelas = ml; }
    public int getTargetHarian() { return targetHarian; }
    public void setTargetHarian(int t) { targetHarian = t; }
    public int getBatasMax() { return batasMax; }
    public void setBatasMax(int b) { batasMax = b; }
    public int getBatasMaxPoo() { return batasMaxPoo; }
    public void setBatasMaxPoo(int b) { batasMaxPoo = b; }
}

class DataManager {
    private final ArrayList<RecordHari> riwayat;
    private final SettingApp setting;

    public DataManager() {
        riwayat = new ArrayList<>();
        setting = new SettingApp();
    }

    public RecordHari getRecord(LocalDate tanggal) {
        for (RecordHari r : riwayat) {
            if (r.getTanggal().equals(tanggal)) return r;
        }
        RecordHari baru = new RecordHari(tanggal);
        riwayat.add(baru);
        return baru;
    }

    public SettingApp getSetting() { return setting; }

    public ArrayList<RecordHari> getRiwayatBulan(YearMonth bulan) {
        ArrayList<RecordHari> hasil = new ArrayList<>();
        for (RecordHari r : riwayat) {
            if (YearMonth.from(r.getTanggal()).equals(bulan)) {
                hasil.add(r);
            }
        }
        return hasil;
    }
}

class WaterTracker implements Tracker {
    private final DataManager manager;

    public WaterTracker(DataManager dm) { this.manager = dm; }

    @Override
    public void tambahData(int ml, LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        int totalBaru = rec.getVolAir() + ml;

        if (totalBaru <= manager.getSetting().getBatasMax()) {
            rec.setVolAir(totalBaru);
            String waktu = LocalTime.now().toString().substring(0, 5);
            rec.getDetailAir().add(waktu + " - " + ml + " ml");
        }
    }

    @Override
    public void resetData(LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        rec.setVolAir(0);
        rec.getDetailAir().clear();
        rec.setSudahAlert(false);
    }

    @Override
    public int getTotal(LocalDate tanggal) {
        return manager.getRecord(tanggal).getVolAir();
    }

    public boolean perluAlert(LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        int total = rec.getVolAir();
        int target = manager.getSetting().getTargetHarian();

        if (total >= target && !rec.isSudahAlert()) {
            rec.setSudahAlert(true);
            return true;
        }
        return false;
    }

    public boolean sudahMax(LocalDate tanggal) {
        return getTotal(tanggal) >= manager.getSetting().getBatasMax();
    }
}

class PooTracker implements Tracker {
    private final DataManager manager;

    public PooTracker(DataManager dm) { this.manager = dm; }

    @Override
    public void tambahData(int nilai, LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        
        // Cek apakah sudah mencapai batas maksimal
        if (rec.getJumlahPoo() < manager.getSetting().getBatasMaxPoo()) {
            rec.setJumlahPoo(rec.getJumlahPoo() + 1);
            String waktu = LocalTime.now().toString().substring(0, 5);
            rec.getDetailPoo().add("BAB ke-" + rec.getJumlahPoo() + " - " + waktu);
        }
    }

    @Override
    public void resetData(LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        rec.setJumlahPoo(0);
        rec.getDetailPoo().clear();
    }

    @Override
    public int getTotal(LocalDate tanggal) {
        return manager.getRecord(tanggal).getJumlahPoo();
    }
    
    // Method untuk mengecek apakah sudah mencapai batas maksimal
    public boolean sudahMax(LocalDate tanggal) {
        return getTotal(tanggal) >= manager.getSetting().getBatasMaxPoo();
    }
}
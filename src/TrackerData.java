import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private int batasMaxPoo = 5;

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
    private static final String FILE_DATA = "data.txt";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DataManager() {
        riwayat = new ArrayList<>();
        setting = new SettingApp();
        muatData(); // Load dari file saat start
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

    public void simpanData() {
        try (PrintWriter w = new PrintWriter(new FileWriter(FILE_DATA))) {
            w.println("SETTINGS");
            w.println(setting.getMlPerGelas() + "," + setting.getTargetHarian() + "," + 
                     setting.getBatasMax() + "," + setting.getBatasMaxPoo());
            
            w.println("RECORDS");
            for (RecordHari r : riwayat) {
                w.println(r.getTanggal().format(FMT) + "|" + r.getVolAir() + "|" + 
                         r.getJumlahPoo() + "|" + r.isSudahAlert());
                w.print("AIR:");
                for (String d : r.getDetailAir()) w.print(d + ";");
                w.println();
                w.print("POO:");
                for (String d : r.getDetailPoo()) w.print(d + ";");
                w.println();
            }
        } catch (IOException e) {
            System.err.println("Error simpan: " + e.getMessage());
        }
    }

    public void simpanSettings() {
        simpanData();
    }

    private void muatData() {
        File f = new File(FILE_DATA);
        if (!f.exists()) return;
        
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line, mode = "";
            RecordHari curr = null;
            
            while ((line = r.readLine()) != null) {
                if (line.equals("SETTINGS")) { mode = "SET"; continue; }
                if (line.equals("RECORDS")) { mode = "REC"; continue; }
                
                if (mode.equals("SET")) {
                    String[] s = line.split(",");
                    setting.setMlPerGelas(Integer.parseInt(s[0]));
                    setting.setTargetHarian(Integer.parseInt(s[1]));
                    setting.setBatasMax(Integer.parseInt(s[2]));
                    setting.setBatasMaxPoo(Integer.parseInt(s[3]));
                } else if (mode.equals("REC")) {
                    if (line.startsWith("AIR:")) {
                        if (curr != null) {
                            for (String d : line.substring(4).split(";")) {
                                if (!d.isEmpty()) curr.getDetailAir().add(d);
                            }
                        }
                    } else if (line.startsWith("POO:")) {
                        if (curr != null) {
                            for (String d : line.substring(4).split(";")) {
                                if (!d.isEmpty()) curr.getDetailPoo().add(d);
                            }
                        }
                    } else {
                        String[] p = line.split("\\|");
                        if (p.length == 4) {
                            curr = new RecordHari(LocalDate.parse(p[0], FMT));
                            curr.setVolAir(Integer.parseInt(p[1]));
                            curr.setJumlahPoo(Integer.parseInt(p[2]));
                            curr.setSudahAlert(Boolean.parseBoolean(p[3]));
                            riwayat.add(curr);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error muat: " + e.getMessage());
        }
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
            manager.simpanData();
        }
    }

    @Override
    public void resetData(LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        rec.setVolAir(0);
        rec.getDetailAir().clear();
        rec.setSudahAlert(false);
        manager.simpanData();
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
            manager.simpanData();
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
        
        if (rec.getJumlahPoo() < manager.getSetting().getBatasMaxPoo()) {
            rec.setJumlahPoo(rec.getJumlahPoo() + 1);
            String waktu = LocalTime.now().toString().substring(0, 5);
            rec.getDetailPoo().add("BAB ke-" + rec.getJumlahPoo() + " - " + waktu);
            manager.simpanData();
        }
    }

    @Override
    public void resetData(LocalDate tanggal) {
        RecordHari rec = manager.getRecord(tanggal);
        rec.setJumlahPoo(0);
        rec.getDetailPoo().clear();
        manager.simpanData();
    }

    @Override
    public int getTotal(LocalDate tanggal) {
        return manager.getRecord(tanggal).getJumlahPoo();
    }
    
    public boolean sudahMax(LocalDate tanggal) {
        return getTotal(tanggal) >= manager.getSetting().getBatasMaxPoo();
    }
}
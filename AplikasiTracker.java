import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AplikasiTracker extends JFrame {
    private final DataManager dataManager;
    private final WaterTracker waterTracker;
    private final PooTracker pooTracker;
    private LocalDate tanggalIni;

    private JLabel labelAir, labelPoo, labelTanggal;
    private JProgressBar progressAir;
    private JTextArea areaAir, areaPoo;
    private JComboBox<String> comboBulan, comboTahun;

    // Template Warnai hidup
    private static final Color COLOR = new Color(41, 128, 185);
    private static final Color COLOR_WATER = new Color(52, 152, 219);
    private static final Color COLOR_POO = new Color(232, 60, 145);
    private static final Color COLOR_BG = new Color(236, 240, 241);
    private static final Color COLOR_LIGHT = new Color(248, 249, 250);
    private static final Color COLOR_TEXT = new Color(44, 62, 80);
    private static final Color COLOR_ACCENT = new Color(46, 204, 113);

    public AplikasiTracker() {
        dataManager = new DataManager();
        waterTracker = new WaterTracker(dataManager);
        pooTracker = new PooTracker(dataManager);
        tanggalIni = LocalDate.now();

        setTitle("Water & Poo Tracker");
        setSize(900, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setBackground(COLOR_BG);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.addTab("Dashboard", Dashboard());
        tabs.addTab("History", buatHistory());
        tabs.addTab("Settings", Settings());
        add(tabs);
    }

    private JPanel Dashboard() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel atas = new JPanel(new BorderLayout(10, 0));
        atas.setBackground(COLOR);
        atas.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel judul = new JLabel("Water & Poo Tracker");
        judul.setFont(new Font("Segoe UI", Font.BOLD, 24));
        judul.setForeground(Color.WHITE);

        JPanel navigasi = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navigasi.setOpaque(false);

        JButton btnKurang = buatTombolNavigasi("<", 35, 35);
        JButton btnTambah = buatTombolNavigasi(">", 35, 35);
        JButton btnHariIni = buatTombolNavigasi("Hari Ini", 100, 35);
        JButton btnPilih = buatTombolNavigasi("Pilih", 80, 35);

        labelTanggal = new JLabel();
        labelTanggal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelTanggal.setForeground(Color.WHITE);
        labelTanggal.setPreferredSize(new Dimension(120, 35));
        labelTanggal.setHorizontalAlignment(SwingConstants.CENTER);

        btnKurang.addActionListener(e -> {
            tanggalIni = tanggalIni.minusDays(1);
            update();
        });
        btnTambah.addActionListener(e -> {
            tanggalIni = tanggalIni.plusDays(1);
            update();
        });
        btnHariIni.addActionListener(e -> {
            tanggalIni = LocalDate.now();
            update();
        });
        btnPilih.addActionListener(e -> pilihTanggal());

        navigasi.add(btnKurang);
        navigasi.add(labelTanggal);
        navigasi.add(btnTambah);
        navigasi.add(btnHariIni);
        navigasi.add(btnPilih);

        atas.add(judul, BorderLayout.WEST);
        atas.add(navigasi, BorderLayout.CENTER);

        JPanel tengah = new JPanel(new GridLayout(1, 2, 20, 0));
        tengah.setBackground(COLOR_BG);
        tengah.add(PanelAir());
        tengah.add(PanelPoo());

        panel.add(atas, BorderLayout.NORTH);
        panel.add(tengah, BorderLayout.CENTER);
        return panel;
    }

    private JPanel PanelAir() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_WATER, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel info = new JPanel(new BorderLayout(10, 10));
        info.setBackground(Color.WHITE);

        JLabel titleAir = new JLabel("Konsumsi Air Hari Ini");
        titleAir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleAir.setForeground(COLOR_WATER);

        labelAir = new JLabel("0 ml / 2000 ml");
        labelAir.setFont(new Font("Segoe UI", Font.BOLD, 28));
        labelAir.setForeground(COLOR_WATER);
        labelAir.setHorizontalAlignment(SwingConstants.CENTER);

        progressAir = new JProgressBar(0, 100);
        progressAir.setStringPainted(true);
        progressAir.setFont(new Font("Segoe UI", Font.BOLD, 11));
        progressAir.setForeground(COLOR_WATER);
        progressAir.setBackground(new Color(230, 240, 250));
        progressAir.setPreferredSize(new Dimension(0, 25));

        info.add(titleAir, BorderLayout.NORTH);
        info.add(labelAir, BorderLayout.CENTER);
        info.add(progressAir, BorderLayout.SOUTH);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        tombol.setBackground(Color.WHITE);

        JButton btnGelas = buatTombolAksi("+ Gelas (250ml)", COLOR_WATER);
        JButton btnManual = buatTombolAksi("Manual Input", COLOR);
        JButton btnReset = buatTombolAksi("Reset", new Color(231, 76, 60));

        btnGelas.addActionListener(e -> {
            if (waterTracker.sudahMax(tanggalIni)) {
                pesan("⚠️ Sudah mencapai batas maksimal!");
                return;
            }
            int ml = dataManager.getSetting().getMlPerGelas();
            waterTracker.tambahData(ml, tanggalIni);
            update();
            if (waterTracker.perluAlert(tanggalIni)) {
                pesan("✓ Target harian tercapai!");
            }
        });

        btnManual.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(
                this, "Masukkan volume (ml):");
            if (input != null && !input.isEmpty()) {
                try {
                    int ml = Integer.parseInt(input);
                    if (ml > 0 && ml <= 2000) {
                        if (waterTracker.sudahMax(tanggalIni)) {
                            pesan("⚠️ Sudah mencapai batas maksimal!");
                            return;
                        }
                        waterTracker.tambahData(ml, tanggalIni);
                        update();
                        if (waterTracker.perluAlert(tanggalIni)) {
                            pesan("✓ Target harian tercapai!");
                        }
                    } else {
                        pesan("Masukkan angka 1-2000 ml");
                    }
                } catch (NumberFormatException ex) {
                    pesan("Input harus berupa angka!");
                }
            }
        });

        btnReset.addActionListener(e -> {
            int pilih = JOptionPane.showConfirmDialog(
                this, "Reset semua data air hari ini?");
            if (pilih == JOptionPane.YES_OPTION) {
                waterTracker.resetData(tanggalIni);
                update();
            }
        });

        tombol.add(btnGelas);
        tombol.add(btnManual);
        tombol.add(btnReset);

        areaAir = new JTextArea(8, 25);
        areaAir.setEditable(false);
        areaAir.setFont(new Font("Courier New", Font.PLAIN, 11));
        areaAir.setBackground(COLOR_LIGHT);
        areaAir.setForeground(COLOR_TEXT);
        areaAir.setLineWrap(true);
        areaAir.setWrapStyleWord(true);
        areaAir.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(areaAir);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240), 1));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 240), 1),
            "Riwayat", 0, 0, new Font("Segoe UI", Font.PLAIN, 11), COLOR_TEXT
        ));
        container.add(scroll);

        panel.add(info, BorderLayout.NORTH);
        panel.add(tombol, BorderLayout.CENTER);
        panel.add(container, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel PanelPoo() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_POO, 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel info = new JPanel(new BorderLayout(10, 10));
        info.setBackground(Color.WHITE);

        JLabel titlePoo = new JLabel("Aktivitas BAB");
        titlePoo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titlePoo.setForeground(COLOR_POO);

        labelPoo = new JLabel("0 kali");
        labelPoo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        labelPoo.setForeground(COLOR_POO);
        labelPoo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel infoNormal = new JLabel("Frekuensi Normal: 1-3x per hari");
        infoNormal.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        infoNormal.setForeground(new Color(127, 140, 141));
        infoNormal.setHorizontalAlignment(SwingConstants.CENTER);

        info.add(titlePoo, BorderLayout.NORTH);
        info.add(labelPoo, BorderLayout.CENTER);
        info.add(infoNormal, BorderLayout.SOUTH);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        tombol.setBackground(Color.WHITE);

        JButton btnCatat = buatTombolAksi("+ Catat BAB", COLOR_POO);
        JButton btnReset = buatTombolAksi("Reset", new Color(231, 76, 60));

        btnCatat.addActionListener(e -> {
            // Cek apakah sudah mencapai batas maksimal
            if (pooTracker.sudahMax(tanggalIni)) {
                pesan("⚠️ Sudah mencapai batas maksimal BAB hari ini!");
                return;
            }
            pooTracker.tambahData(1, tanggalIni);
            update();
        });

        btnReset.addActionListener(e -> {
            int pilih = JOptionPane.showConfirmDialog(
                this, "Reset semua data BAB hari ini?");
            if (pilih == JOptionPane.YES_OPTION) {
                pooTracker.resetData(tanggalIni);
                update();
            }
        });

        tombol.add(btnCatat);
        tombol.add(btnReset);

        areaPoo = new JTextArea(8, 25);
        areaPoo.setEditable(false);
        areaPoo.setFont(new Font("Courier New", Font.PLAIN, 11));
        areaPoo.setBackground(COLOR_LIGHT);
        areaPoo.setForeground(COLOR_TEXT);
        areaPoo.setLineWrap(true);
        areaPoo.setWrapStyleWord(true);
        areaPoo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scroll = new JScrollPane(areaPoo);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(250, 220, 200), 1));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(250, 220, 200), 1),
            "Riwayat", 0, 0, new Font("Segoe UI", Font.PLAIN, 11), COLOR_TEXT
        ));
        container.add(scroll);

        panel.add(info, BorderLayout.NORTH);
        panel.add(tombol, BorderLayout.CENTER);
        panel.add(container, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buatHistory() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filter.setBackground(Color.WHITE);
        filter.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        JLabel labelBulan = new JLabel("Bulan:");
        labelBulan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelBulan.setForeground(COLOR_TEXT);

        String[] namaBulan = {"Januari", "Februari", "Maret", "April", "Mei",
            "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        comboBulan = new JComboBox<>(namaBulan);
        comboBulan.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        comboBulan.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel labelTahun = new JLabel("Tahun:");
        labelTahun.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        labelTahun.setForeground(COLOR_TEXT);

        String[] tahun = new String[5];
        int thnSekarang = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            tahun[i] = String.valueOf(thnSekarang - 2 + i);
        }
        comboTahun = new JComboBox<>(tahun);
        comboTahun.setSelectedIndex(2);
        comboTahun.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnTampil = buatTombolAksi("Tampilkan Data", COLOR);

        filter.add(labelBulan);
        filter.add(comboBulan);
        filter.add(labelTahun);
        filter.add(comboTahun);
        filter.add(btnTampil);

        String[] kolom = {"Tanggal", "Air (ml)", "Target", "BAB", "Status"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabel = new JTable(model);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabel.setRowHeight(25);
        tabel.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabel.getTableHeader().setBackground(COLOR);
        tabel.getTableHeader().setForeground(Color.WHITE);
        tabel.setGridColor(new Color(189, 195, 199));
        tabel.setSelectionBackground(new Color(52, 152, 219));

        JScrollPane scroll = new JScrollPane(tabel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        btnTampil.addActionListener(e -> {
            int bulan = comboBulan.getSelectedIndex() + 1;
            int tahunPilih = Integer.parseInt(
                (String) comboTahun.getSelectedItem());
            YearMonth periode = YearMonth.of(tahunPilih, bulan);

            model.setRowCount(0);
            int target = dataManager.getSetting().getTargetHarian();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (RecordHari r : dataManager.getRiwayatBulan(periode)) {
                String status = r.getVolAir() >= target ? "✓" : "✗";
                model.addRow(new Object[]{
                    r.getTanggal().format(fmt),
                    r.getVolAir(),
                    target,
                    r.getJumlahPoo(),
                    status
                });
            }

            if (model.getRowCount() == 0) {
                pesan("Tidak ada data untuk periode ini");
            }
        });

        panel.add(filter, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel Settings() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(5, 2, 15, 20));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel labelGelas = new JLabel("ML per Gelas:");
        labelGelas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelGelas.setForeground(COLOR_TEXT);
        JTextField txtGelas = new JTextField(
            String.valueOf(dataManager.getSetting().getMlPerGelas()), 15);
        txtGelas.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gaya(txtGelas);

        JLabel labelTarget = new JLabel("Target Harian (ml):");
        labelTarget.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelTarget.setForeground(COLOR_TEXT);
        JTextField txtTarget = new JTextField(
            String.valueOf(dataManager.getSetting().getTargetHarian()), 15);
        txtTarget.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gaya(txtTarget);

        JLabel labelMax = new JLabel("Batas Maksimal Air (ml):");
        labelMax.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelMax.setForeground(COLOR_TEXT);
        JTextField txtMax = new JTextField(
            String.valueOf(dataManager.getSetting().getBatasMax()), 15);
        txtMax.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gaya(txtMax);

        JLabel labelMaxPoo = new JLabel("Batas Maksimal BAB (kali):");
        labelMaxPoo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        labelMaxPoo.setForeground(COLOR_TEXT);
        JTextField txtMaxPoo = new JTextField(
            String.valueOf(dataManager.getSetting().getBatasMaxPoo()), 15);
        txtMaxPoo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        gaya(txtMaxPoo);

        JButton btnSimpan = buatTombolAksi("Simpan Pengaturan", COLOR_ACCENT);

        form.add(labelGelas);
        form.add(txtGelas);
        form.add(labelTarget);
        form.add(txtTarget);
        form.add(labelMax);
        form.add(txtMax);
        form.add(labelMaxPoo);
        form.add(txtMaxPoo);
        form.add(new JLabel());
        form.add(btnSimpan);

        btnSimpan.addActionListener(e -> {
            try{
                int gelas = Integer.parseInt(txtGelas.getText());
                int target = Integer.parseInt(txtTarget.getText());
                int max = Integer.parseInt(txtMax.getText());
                int maxPoo = Integer.parseInt(txtMaxPoo.getText());

                if(gelas > 0 && target > 0 && max > target && maxPoo > 0 && maxPoo <= 10) {
                    dataManager.getSetting().setMlPerGelas(gelas);
                    dataManager.getSetting().setTargetHarian(target);
                    dataManager.getSetting().setBatasMax(max);
                    dataManager.getSetting().setBatasMaxPoo(maxPoo);
                    pesan("✓ Pengaturan berhasil disimpan!");
                    update();
                } else {
                    pesan("Nilai tidak valid! Pastikan batas max air > target dan batas BAB 1-10");
                }
            } catch (NumberFormatException  ex) {
                pesan("Input harus berupa angka!");
            }
        });

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        container.add(form);

        panel.add(container, BorderLayout.NORTH);
        return panel;
    }

    private void pilihTanggal() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JComboBox<Integer> comboTgl = new JComboBox<>();
        JComboBox<Integer> comboBln = new JComboBox<>();
        JComboBox<Integer> comboThn = new JComboBox<>();

        for (int i = 1; i <= 31; i++) comboTgl.addItem(i);
        for (int i = 1; i <= 12; i++) comboBln.addItem(i);

        int tahun = LocalDate.now().getYear();
        for (int i = tahun - 2; i <= tahun + 1; i++) {
            comboThn.addItem(i);
        }

        comboTgl.setSelectedItem(tanggalIni.getDayOfMonth());
        comboBln.setSelectedItem(tanggalIni.getMonthValue());
        comboThn.setSelectedItem(tanggalIni.getYear());

        panel.add(new JLabel("Tanggal:"));
        panel.add(comboTgl);
        panel.add(new JLabel("Bulan:"));
        panel.add(comboBln);
        panel.add(new JLabel("Tahun:"));
        panel.add(comboThn);

        int hasil = JOptionPane.showConfirmDialog(this, panel,
            "Pilih Tanggal", JOptionPane.OK_CANCEL_OPTION);

        if (hasil == JOptionPane.OK_OPTION) {
            try {
                tanggalIni = LocalDate.of(
                    (Integer) comboThn.getSelectedItem(),
                    (Integer) comboBln.getSelectedItem(),
                    (Integer) comboTgl.getSelectedItem()
                );
                update();
            } catch (Exception e) {
                pesan("Tanggal tidak valid!");
            }
        }
    }

    private void update() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
        labelTanggal.setText(tanggalIni.format(fmt));

        int total = waterTracker.getTotal(tanggalIni);
        int target = dataManager.getSetting().getTargetHarian();
        labelAir.setText(total + " / " + target + " ml");

        int persen = (total * 100) / target;
        if (persen > 100) persen = 100;
        progressAir.setValue(persen);

        int totalPoo = pooTracker.getTotal(tanggalIni);
        int maxPoo = dataManager.getSetting().getBatasMaxPoo();
        labelPoo.setText(totalPoo + " / " + maxPoo + " kali");

        RecordHari record = dataManager.getRecord(tanggalIni);
        areaAir.setText("");
        for (String s : record.getDetailAir()) {
            areaAir.append(s + "\n");
        }

        areaPoo.setText("");
        for (String s : record.getDetailPoo()) {
            areaPoo.append(s + "\n");
        }
    }

    private JButton buatTombolAksi(String text, Color warna) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(warna);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(terangi(warna));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(warna);
            }
        });
        return btn;
    }

    private JButton buatTombolNavigasi(String text, int width, int height) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(41, 128, 185));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, height));
        btn.setOpaque(true);
        return btn;
    }

    private void gaya(JTextField txt) {
        txt.setBackground(COLOR_LIGHT);
        txt.setForeground(COLOR_TEXT);
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private Color terangi(Color color) {
        int r = Math.min(color.getRed() + 30, 255);
        int g = Math.min(color.getGreen() + 30, 255);
        int b = Math.min(color.getBlue() + 30, 255);
        return new Color(r, g, b);
    }

    private void pesan(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Informasi",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AplikasiTracker app = new AplikasiTracker();
            app.setVisible(true);
            app.update();
        });
    }
}
package be.technifutur.tournament.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Scanner;

public class AppInfo {
    public static StringBuilder staticVersion ;
    public final int YEAR = Calendar.getInstance().get(Calendar.YEAR);
    public final int MONTH = Calendar.getInstance().get(Calendar.MONTH);
    public final int DAY = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    public int BUILD = 0;
    public String VERSION =String.format("%04d.%02d.%02d.%04d", YEAR, MONTH, DAY, BUILD);


    public AppInfo(){}
    public AppInfo(String ver) {
        VERSION = ver;
        StringBuilder sb = new StringBuilder();
        sb.append("APP VERSION*").append(ver).append("*");
        staticVersion = sb;
    }

    //versionning dynamique quotidienne
    public String getCurrentVersion() {
        Gson json = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        String today = java.time.LocalDate.now().toString(); // 2026-04-01
        today = today.replace("-", "."); // 2026.04.01

        File file = new File("./mydata/appVersion.json");
        //Lire ancienne version*******************************
        AppInfo oldInfo = null;
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                oldInfo = json.fromJson(reader, AppInfo.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int build = 1;
        //on fait les checks
        if (oldInfo != null && oldInfo.VERSION != null) {
            String[] parts = oldInfo.VERSION.split("\\.");

            String oldDate = parts[0] + "." + parts[1] + "." + parts[2];
            int oldBuild = Integer.parseInt(parts[3]);

            if (oldDate.equals(today)) {
                build = oldBuild + 1;
            }
        }
        String newVersion = String.format("%s.%04d", today, build);

        //save *********************************************
        AppInfo newData = new AppInfo(newVersion);
        newData.BUILD = build;
        newData.VERSION = newVersion;

        try (FileWriter writer = new FileWriter(file)){
            json.toJson(newData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newVersion;
    }
}

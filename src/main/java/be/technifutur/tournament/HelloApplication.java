package be.technifutur.tournament;

import be.technifutur.tournament.utils.AppInfo;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
public class HelloApplication extends Application {

    public HelloApplication(){
        AppInfo appInfo = new AppInfo();
        appInfo.getCurrentVersion();
        System.out.println(AppInfo.staticVersion);
    }
}
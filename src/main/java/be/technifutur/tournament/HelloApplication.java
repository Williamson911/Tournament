package be.technifutur.tournament;

import be.technifutur.tournament.utils.AppInfo;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api")
@WebListener
public class HelloApplication extends Application implements ServletContextListener {

    public HelloApplication() {}

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String realPath = sce.getServletContext().getRealPath("/");
        AppInfo appInfo = new AppInfo();
        appInfo.getCurrentVersion(realPath);
        System.out.println("\n$$$$$$$$$$$$$$$$$$$$$\n" + AppInfo.staticVersion + "\n$$$$$$$$$$$$$$$$$$$$$\n");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
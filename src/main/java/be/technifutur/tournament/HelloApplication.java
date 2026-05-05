package be.technifutur.tournament;

import be.technifutur.tournament.utils.AppInfo;
import be.technifutur.tournament.utils.Dsg;
import be.technifutur.tournament.utils.Tableau;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;



@ApplicationPath("/api")
@WebListener
public class HelloApplication extends Application implements ServletContextListener {

    public HelloApplication() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        String realPath = sce.getServletContext().getRealPath("/");
        AppInfo appInfo = new AppInfo();
        appInfo.getCurrentVersion(realPath);
        System.out.println(Tableau.displayInbox(Dsg.ye,AppInfo.staticVersion));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
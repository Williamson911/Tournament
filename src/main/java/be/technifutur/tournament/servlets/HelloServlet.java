package be.technifutur.tournament.servlets;

import be.technifutur.tournament.utils.EMFProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/hello/")
@ApplicationScoped
public class HelloServlet extends HttpServlet {

    private final EMFProvider emfProvider;

    @Inject
    public HelloServlet() {
        this.emfProvider = new EMFProvider();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String persistenceName = emfProvider.getPersistenceName();

        req.setAttribute("profile", persistenceName);

        req.getRequestDispatcher("/WEB-INF/pages/hello.jsp")
           .forward(req, resp);
    }
}

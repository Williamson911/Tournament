package be.technifutur.tournament.servlets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String apiUrl = req.getScheme() + "://"
                + req.getServerName() + ":"
                + req.getServerPort()
                + req.getContextPath()
                + "/api/openapi.json";

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        JsonObject root = JsonParser.parseReader(reader)
                                    .getAsJsonObject();
        reader.close();

        JsonObject paths = root.getAsJsonObject("paths");

        Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();

        Pattern parameterPattern = Pattern.compile("\\{.*?}");

        for (Map.Entry<String, JsonElement> entry : paths.entrySet()) {

            String rawPath = entry.getKey();

            String[] parts = rawPath.split("/");

            StringBuilder stablePath = new StringBuilder();

            for (String part : parts) {

                if (part == null || part.isBlank()) {
                    continue;
                }

                // stop at first parameter
                if (parameterPattern.matcher(part).matches()) {
                    break;
                }

                stablePath.append("/")
                          .append(part);
            }

            stablePath.append("/");

            String group = stablePath.toString();

            JsonObject methods = entry.getValue().getAsJsonObject();

            for (Map.Entry<String, JsonElement> m : methods.entrySet()) {

                JsonObject methodObj = m.getValue().getAsJsonObject();

                Map<String,String> ep = new HashMap<>();

                ep.put("method", m.getKey().toUpperCase());
                ep.put("path", rawPath);

                if (methodObj.has("summary")) {
                    ep.put("summary",
                           methodObj.get("summary").getAsString());
                } else {
                    ep.put("summary","ø");
                }

                grouped
                        .computeIfAbsent(group, k -> new ArrayList<>())
                        .add(ep);
            }
        }

        req.setAttribute("grouped", grouped);

        req.getRequestDispatcher("/WEB-INF/pages/home.jsp")
           .forward(req, resp);
    }
}

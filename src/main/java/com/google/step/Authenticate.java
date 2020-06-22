package com.google.step;


import java.io.IOException;
import java.net.URL;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/authenticate")
public class Authenticate extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final String CLIENT_ID = System.getenv("client_id");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String baseURL = request.getParameter("baseURL");
        // Remove any query parameters from URL if present
        if(baseURL.contains("/?")) {
            int position = baseURL.indexOf("/?");
            baseURL = baseURL.substring(0, position);
        }
        String redirectUri = baseURL + "/redirect";
        String req = String.format("https://accounts.google.com/o/oauth2/v2/auth?access_type=offline&client_id=%s&redirect_uri=%s&response_type=code&scope=https://www.googleapis.com/auth/drive&state=%s",
            CLIENT_ID, redirectUri, redirectUri); 
        response.setContentType("text/html");
        response.getWriter().println(req);
    }
}

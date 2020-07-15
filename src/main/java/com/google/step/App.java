package com.google.step;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.stream.Collectors;

/****************
    This servlet is the main page for the application
****************/

@WebServlet("/app")
public class App extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        // Check to see if the user is logged in
        if(session != null) {
            String accessToken = (String)session.getAttribute("accessToken");
            if(accessToken == null) {
                response.sendRedirect("/index.html");
            }
            else {
                BufferedReader reader = new BufferedReader(new FileReader("app.html"));
                String responseData = reader.lines().collect(Collectors.joining("\n"));
                response.getWriter().println(responseData);
            }
        }
        else {
            response.sendRedirect("/index.html");
        }
    }


}

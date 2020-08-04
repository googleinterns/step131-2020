package com.google.step;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet enables requests for additional locations to be recorded.
 */
@WebServlet("/add-location")
public class AddLocation extends HttpServlet {
    private final String PROJECT_ID = System.getenv("PROJECT_ID");

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        double latitude = Double.parseDouble(request.getParameter("latitude"));
        double longitude = Double.parseDouble(request.getParameter("longitude"));
        String cityName = request.getParameter("cityName");

        // TODO: New "TrackedLocation" entity created from form and stored.
    }
}

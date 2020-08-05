package com.google.step;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;

/**
 * This servlet handles the first step of the authentication process with OAuth. When the user
 * visits the home page they are immediately sent here to begin the authentication and redirection
 * process.
 */
@WebServlet("/authenticate")
public class Authenticate extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Authenticate.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String baseURL = request.getParameter("baseURL");
            String redirectUri = baseURL + "redirect";
            // Create the URI to begin the authentication process
            URIBuilder uriBuilder = new URIBuilder();
            uriBuilder.setScheme("https");
            uriBuilder.setHost("accounts.google.com");
            uriBuilder.setPath("/o/oauth2/v2/auth");
            uriBuilder.addParameter("access_type", "online");
            uriBuilder.addParameter("client_id", CommonUtils.CLIENT_ID);
            uriBuilder.addParameter("redirect_uri", redirectUri);
            uriBuilder.addParameter("response_type", "code");
            uriBuilder.addParameter("scope", "https://www.googleapis.com/auth/drive");
            uriBuilder.addParameter("state", redirectUri);
            String req = uriBuilder.build().toString();
            response.setContentType("text/html");
            response.getWriter().println(req);
        } catch (URISyntaxException e) {
            LOGGER.severe(e.getMessage());
        }
    }
}

package com.google.step;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.http.client.utils.URIBuilder;

/**
 * This servlet handles the second and third steps of the authentication process, specifically the
 * retrieval of the authorization code and the storage of the access token.
 */
@WebServlet("/redirect")
public class Redirect extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Redirect.class.getName());

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String redirectUri = request.getParameter("state");
        // The request was approved
        if (code != null) {
            try {
                // Use a URIBuilder object to serialize the request parameters
                URIBuilder builder = new URIBuilder();
                builder.setScheme("https"); // Filler scheme for builder
                builder.setHost("google.com"); // Filler host for builder
                builder.addParameter("client_id", CommonUtils.CLIENT_ID);
                builder.addParameter("client_secret", CommonUtils.CLIENT_SECRET);
                builder.addParameter("code", code);
                builder.addParameter("redirect_uri", redirectUri);
                builder.addParameter("grant_type", "authorization_code");
                String baseURL = builder.build().toString();
                byte[] queryParameters =
                        baseURL.substring(baseURL.indexOf("?") + 1)
                                .getBytes(StandardCharsets.UTF_8);
                String req = "https://oauth2.googleapis.com/token";
                URL url = new URL(req);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", Integer.toString(queryParameters.length));
                con.setDoInput(true);
                con.setDoOutput(true);
                try (DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
                    writer.write(queryParameters);
                } catch (IOException e) {
                    LOGGER.severe(e.getMessage());
                }
                InputStream is = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String responseData = reader.lines().collect(Collectors.joining(""));
                Gson gson = new Gson();
                TokenResponse tokenResponse = gson.fromJson(responseData, TokenResponse.class);
                // Save access token
                HttpSession session = request.getSession();
                session.setAttribute("accessToken", tokenResponse.getAccessToken());
                response.sendRedirect("/app.html");
            } catch (URISyntaxException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }
}

/** Class representing the access token required as part of the authorization process. * */
class TokenResponse {
    private String access_token;
    private int expires_in;
    private String scope;
    private String token_type;

    public String getAccessToken() {
        return access_token;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return token_type;
    }
}

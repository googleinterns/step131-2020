package com.google.step;

import java.io.IOException;
import java.net.URL;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import javax.servlet.http.HttpSession;
import org.apache.http.client.utils.URIBuilder;
import java.net.URISyntaxException;

/***
    This servlet handles the second and
    third steps of the authentication
    process, specifically the retrieval
    of the authorization code
    and the storage of the access token.
***/
@WebServlet("/redirect")
public class Redirect extends HttpServlet {
    // Both of these variables are unique identifiers for OAuth for the project.
    private final String CLIENT_ID = System.getenv("client_id"); 
    private final String CLIENT_SECRET = System.getenv("client_secret");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String redirectUri = request.getParameter("state");
        // The request was approved
        if(code != null) {
            try {
                String req = "https://oauth2.googleapis.com/token";
                URIBuilder builder = new URIBuilder();
                builder.setScheme("https");
                builder.setHost("google.com");
                builder.addParameter("client_id", CLIENT_ID);
                builder.addParameter("client_secret", CLIENT_SECRET);
                builder.addParameter("code", code);
                builder.addParameter("redirect_uri", redirectUri);
                builder.addParameter("grant_type", "authorization_code");
                String base = builder.build().toString();
                // Get escaped query parameters
                String data = base.substring(base.indexOf("?") + 1);
                URL url = new URL(req);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
                con.setDoInput(true);
                con.setDoOutput(true);
                // TODO: add logging for error handling
                try(DataOutputStream writer = new DataOutputStream(con.getOutputStream())) {
                    writer.write(data.getBytes(StandardCharsets.UTF_8));
                }
                catch(IOException e) {}
                InputStream is = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String responseData = reader.lines().collect(Collectors.joining(""));
                Gson gson = new Gson();
                TokenResponse tokenResponse = gson.fromJson(responseData, TokenResponse.class);
                // Save access token
                HttpSession session = request.getSession();
                session.setAttribute("accessToken", tokenResponse.getAccessToken());
                response.sendRedirect("/app");
            }
            catch(URISyntaxException e) {
                // TODO: add logging
            }
        }
    }
}

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
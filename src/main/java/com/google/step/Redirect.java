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

@WebServlet("/redirect")
public class Redirect extends HttpServlet {
    private final String CLIENT_ID = System.getenv("client_id");
    private final String CLIENT_SECRET = System.getenv("client_secret");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        String redirectUri = request.getParameter("state");
        // The request was approved
        if(code != null) {
            String req = "https://oauth2.googleapis.com/token";
            String data = String.format("client_id=%s&client_secret=%s&code=%s&redirect_uri=%s&grant_type=authorization_code", CLIENT_ID, CLIENT_SECRET, code, redirectUri);
            URL url = new URL(req);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
            con.setDoInput(true);
            con.setDoOutput(true);
            try(DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(data.getBytes(StandardCharsets.UTF_8));
            }
            InputStream is = con.getInputStream();
            StringBuilder responseData = new StringBuilder();
            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null)
            {
                responseData.append(line);
            }
            Gson gson = new Gson();
            TokenResponse tokenResponse = gson.fromJson(responseData.toString(), TokenResponse.class);
            // Save access token
            HttpSession session = request.getSession();
            session.setAttribute("accessToken", tokenResponse.getAccessToken());
            response.sendRedirect("/app.html");
        }
    }

  
}

class TokenResponse {
    private String access_token;
    private int expires_in;
    private String scope;
    private String token_type;
    
    public String getAccessToken() { return access_token; }

    public int getExpiresIn() { return expires_in; }

    public String getScope() { return scope; }

    public String getTokenType() { return token_type; }
}

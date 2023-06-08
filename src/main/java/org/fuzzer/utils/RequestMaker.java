package org.fuzzer.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class RequestMaker {
    private final URL url;

    public RequestMaker(URL url) {
        this.url = url;
    }

    private HttpURLConnection getNewConnection() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
        connection.setRequestProperty("Content-Type",
                "application/json");
//        connection.setRequestProperty("Accept",
//                "application/json");

        connection.setUseCaches(false);
        connection.setDoOutput(true);

        return connection;
    }

    public String executeGetReq(String jsonInputString) {
        HttpURLConnection connection = getNewConnection();
        try {
            try {
                connection.connect();

                OutputStream os = connection.getOutputStream();
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            //Send request
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            connection.disconnect();
        }
    }
}


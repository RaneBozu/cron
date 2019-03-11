package com.alexmail.cronServer;

import com.alexmail.cronDTO.Request;
import com.alexmail.cronDTO.RequestType;
import com.alexmail.cronDTO.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RequestHandler extends Thread {
    private static Logger LOGGER = LogManager.getLogger(RequestHandler.class.getSimpleName());
    private Socket socket;

    RequestHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
             Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost/History", "user", "user");
             Statement statement = conn.createStatement()) {

            Request request;
            Response response;
            request = (Request) is.readObject();
            LOGGER.info("Request received");
            if (request.getRequestType().equals(RequestType.TRANSLATE)) {
                response = new TranslationResponse(request).getResponse(statement);
                os.writeObject(response);
            }else {
                response = new HistoryResponse(request).getResponse(statement);
                os.writeObject(response);
            }
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            LOGGER.error(ex);
        }
        LOGGER.info("The response is sent");
    }
}
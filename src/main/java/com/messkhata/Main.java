package com.messkhata;

import com.messkhata.service.MessService;
import com.messkhata.ui.LoginFrame;
import com.messkhata.ui.theme.Theme;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * Starts the app. Data is kept in the "mess_data" folder next to the project.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            try {
                MessService service = new MessService("mess_data");
                new LoginFrame(service).setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not open the data folder:\n" + e.getMessage(),
                        "MessKhata", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}

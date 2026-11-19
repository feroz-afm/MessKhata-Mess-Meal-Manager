package com.messkhata.ui;

import com.messkhata.model.Manager;
import com.messkhata.service.MessException;
import com.messkhata.service.MessService;
import com.messkhata.ui.components.FlatButton;
import com.messkhata.ui.components.LogoMark;
import com.messkhata.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/**
 * First window: the manager signs in here.
 */
public class LoginFrame extends JFrame {

    private final MessService service;
    private final JTextField username = Theme.textField();
    private final JPasswordField password = Theme.passwordField();
    private final JLabel error = Theme.label(" ", Font.PLAIN, 12, Theme.CHILLI);

    public LoginFrame(MessService service) {
        this.service = service;
        setTitle("MessKhata - Sign in");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(new BrandPanel());
        root.add(buildForm());
        setContentPane(root);
        setSize(920, 560);
        setLocationRelativeTo(null);
    }

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.PAPER);
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(96, 64, 40, 64));

        JLabel hello = Theme.label("Welcome back", Font.BOLD, 28, Theme.TEXT);
        JLabel sub = Theme.label("Sign in to manage your mess", Font.PLAIN, 14, Theme.MUTED);

        FlatButton signIn = new FlatButton("Sign in", FlatButton.Style.DARK);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        signIn.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        username.addActionListener(e -> password.requestFocusInWindow());

        JLabel hint = Theme.label("First time here? Use  admin  /  1234", Font.PLAIN, 12, Theme.MUTED);

        for (Component c : new Component[]{hello, sub, username, password, signIn, hint, error}) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        username.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        form.add(hello);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(34));
        form.add(leftAligned(Theme.fieldLabel("Username")));
        form.add(Box.createVerticalStrut(6));
        form.add(username);
        form.add(Box.createVerticalStrut(16));
        form.add(leftAligned(Theme.fieldLabel("Password")));
        form.add(Box.createVerticalStrut(6));
        form.add(password);
        form.add(Box.createVerticalStrut(10));
        form.add(error);
        form.add(Box.createVerticalStrut(14));
        form.add(signIn);
        form.add(Box.createVerticalStrut(18));
        form.add(hint);
        outer.add(form, BorderLayout.CENTER);
        return outer;
    }

    private static JLabel leftAligned(JLabel l) {
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void doLogin() {
        try {
            Manager m = service.login(username.getText(), new String(password.getPassword()));
            new MainFrame(service, m).setVisible(true);
            dispose();
        } catch (MessException ex) {
            error.setText(ex.getMessage());
            password.setText("");
            password.requestFocusInWindow();
        }
    }

    /** Left half of the window: dark ink colour with the logo and a short pitch. */
    private static class BrandPanel extends JPanel {

        BrandPanel() {
            setBackground(Theme.INK);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(64, 56, 48, 48));

            LogoMark logo = new LogoMark(56);
            JLabel name = Theme.label("MessKhata", Font.BOLD, 34, Color.WHITE);
            JLabel tag = Theme.label("<html>Meals, bazar and balances of your mess,<br>counted fairly every month.</html>",
                    Font.PLAIN, 15, Theme.INK_TEXT);

            for (Component c : new Component[]{logo, name, tag}) {
                ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            add(logo);
            add(Box.createVerticalStrut(22));
            add(name);
            add(Box.createVerticalStrut(10));
            add(tag);
            add(Box.createVerticalStrut(38));
            add(point("Daily meal chart for every member"));
            add(Box.createVerticalStrut(12));
            add(point("Bazar and shared bills in one place"));
            add(Box.createVerticalStrut(12));
            add(point("Month-end settlement worked out for you"));
            add(Box.createVerticalGlue());
            JLabel foot = Theme.label("Computer & Programming project", Font.PLAIN, 12, new Color(0x8C98AD));
            foot.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(foot);
        }

        private JLabel point(String text) {
            JLabel l = Theme.label(text, Font.PLAIN, 14, Color.WHITE);
            l.setIcon(new javax.swing.Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.TURMERIC);
                    g2.fillOval(x, y + 3, 9, 9);
                    g2.dispose();
                }

                @Override
                public int getIconWidth() {
                    return 9;
                }

                @Override
                public int getIconHeight() {
                    return 15;
                }
            });
            l.setIconTextGap(14);
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            return l;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // two soft rings in the corner, like plates stacked on a table
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillOval(getWidth() - 190, getHeight() - 210, 300, 300);
            g2.setColor(new Color(227, 160, 58, 90));
            g2.setStroke(new java.awt.BasicStroke(18));
            g2.drawOval(getWidth() - 140, getHeight() - 160, 200, 200);
            g2.dispose();
        }
    }
}

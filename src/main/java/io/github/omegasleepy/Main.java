package io.github.omegasleepy;

import java.sql.SQLException;

public class Main {
    public static App app;

    static {
        try {
            app = new App();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static void main () {

    }
}

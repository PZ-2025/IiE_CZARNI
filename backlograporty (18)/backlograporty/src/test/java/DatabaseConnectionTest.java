git init
import com.example.silowniaprojekt.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Klasa testowa dla klasy DatabaseConnection.
 * Weryfikuje poprawność połączenia z bazą danych dla aplikacji siłowni.
 * Testy sprawdzają nawiązywanie i zamykanie połączenia z bazą.
 */
public class DatabaseConnectionTest {

    /**
     * Testuje poprawność nawiązywania połączenia z bazą danych.
     * 
     * Sprawdza czy:
     * 1. Połączenie z bazą danych jest nawiązywane poprawnie (nie jest null)
     * 2. Połączenie jest aktywne (nie jest zamknięte)
     * 3. Połączenie można prawidłowo zamknąć po zakończeniu pracy
     * 
     * Test pomaga weryfikować, czy konfiguracja bazy danych jest poprawna
     * i czy aplikacja może nawiązać połączenie.
     */
    @Test
    public void testGetConnection_validConnection() {
        // Próba nawiązania połączenia do bazy danych
        Connection connection = DatabaseConnection.getConnection();
        assertNotNull(connection, "Połączenie nie powinno być null.");

        try {
            // Sprawdzenie, czy połączenie jest aktywne
            assertFalse(connection.isClosed(), "Połączenie powinno być otwarte.");
            // Zamknięcie połączenia po zakończeniu testu
            connection.close();
        } catch (SQLException ex) {
            fail("Wyjątek podczas sprawdzania stanu połączenia: " + ex.getMessage());
        }
    }
}

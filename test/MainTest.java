import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

import static edu.cvtc.bigram.Main.getWordCount;
import static org.junit.jupiter.api.Assertions.*;

import edu.cvtc.bigram.*;

@SuppressWarnings({"SpellCheckingInspection"})
class MainTest {
  @Test
  void createConnection() {
    assertDoesNotThrow(
        () -> {
          Connection db = Main.createConnection();
          assertNotNull(db);
          assertFalse(db.isClosed());
          db.close();
          assertTrue(db.isClosed());
        }, "Failed to create and close connection."
    );
  }

  @Test
  void reset() {
    Main.reset();
    assertFalse(Files.exists(Path.of(Main.DATABASE_PATH)));
  }

  @Test
  void mainArgs() {
    assertAll(
        () -> {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          System.setOut(new PrintStream(out));
          Main.main(new String[]{"--version"});
          String output = out.toString();
          assertTrue(output.startsWith("Version "));
        },
        () -> {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          System.setOut(new PrintStream(out));
          Main.main(new String[]{"--help"});
          String output = out.toString();
          assertTrue(output.startsWith("Add bigrams"));
        },
        () -> assertDoesNotThrow(() -> {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          System.setErr(new PrintStream(out));
          Main.main(new String[]{"--reset"});
          String output = out.toString();
          assertTrue(output.startsWith("Expected"));
        }),
        () -> assertDoesNotThrow(() -> Main.main(new String[]{"./sample-texts/non-existant-file.txt"})),
        () -> assertDoesNotThrow(() -> Main.main(new String[]{"./sample-texts/empty.txt"}))
    );
  }

  // TODO: Create your test(s) below. /////////////////////////////////////////
  // NOTE: global variable db and @AfterEach cleanup function is required because if getId() fails it won't close the
  // connection to the database which causes issues for the reset() test. This ensures all tests aren't influenced
  // by each other
  private Connection db;
  @AfterEach
  void cleanup() {
    try {
      if (db != null && !db.isClosed()) {
        db.close();
      }
    } catch (SQLException e) {
      // Handle or log the exception if needed
      e.printStackTrace();
    }
  }

  @Test
  void getId() {
    Main.reset();
    db = Main.createConnection();

    assertDoesNotThrow(() -> {
      Main.getId(db, "Liberty's");
      int wordCount = Main.getWordCount(db);
      assertTrue(wordCount == 1);
    });
  }
}
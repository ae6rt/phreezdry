/**
 * @author petrovic May 25, 2010 5:13:14 PM
 */

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class LoggingConfigForTests {
    public LoggingConfigForTests() {
//        LogManager.getLogManager().getLogger("").setLevel(Level.WARNING);
        InputStream is = getClass().getResourceAsStream("logging-test.properties");
        try {
            LogManager.getLogManager().readConfiguration(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }
}

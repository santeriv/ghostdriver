package ghostdriver;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;


public class GoogleCheese {
    private static final int USE_GHOSTDRIVER = 0;
    private static final int USE_FIREFOX_DRIVER = 1;
    private static final int USE_CHROME_DRIVER = 2;
    private static final String PHANTOMJS_PATH = "/Users/detro/Workspaces/Qt/phantomjs/bin/phantomjs";   //< CHANGEME
    private static final String GHOSTDRIVER_PATH = "/Users/detro/Workspaces/Qt/ghostdriver/main.js";     //< CHANGEME

    public static void main( String[] args ) throws MalformedURLException, IOException, InterruptedException {
        int use = USE_GHOSTDRIVER;

        if (args.length == 1) {
            if (args[0].toLowerCase().equals("firefox")) {
                use = USE_FIREFOX_DRIVER;
            } else if (args[0].toLowerCase().equals("chrome")) {
                use = USE_CHROME_DRIVER;
            }
        }

        Date start = new Date();
        // Ask for a JavaScript-enabled browser
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setJavascriptEnabled(true);

        WebDriver driver = null;
        Capabilities actualCapabilities = null;
        Process phantomjs = null;

        switch (use) {
            case USE_FIREFOX_DRIVER: {
                System.out.println("*** USING FIREFOX DRIVER ***");
                // Get a handle to the driver. This will throw an exception if a matching driver cannot be located
                driver = new FirefoxDriver(capabilities);
                // Query the driver to find out more information
                actualCapabilities = ((FirefoxDriver) driver).getCapabilities();
                break;
            }
            case USE_CHROME_DRIVER: {
                System.out.println("*** USING CHROME DRIVER ***");
                System.err.println("Chrome Driver part of this test not implemented yet");
                System.exit(1);
                break;
            }
            case USE_GHOSTDRIVER: default: {
                System.out.println("*** USING GHOSTDRIVER ***");
                phantomjs = Runtime.getRuntime().exec(PHANTOMJS_PATH + ' ' + GHOSTDRIVER_PATH);

                BufferedReader phantomOutputReader = new BufferedReader (new InputStreamReader(phantomjs.getInputStream()));
                while (phantomOutputReader.readLine () == null) { } //< As soon as Ghostdriver outputs something, we can continue
                System.out.println("PhantomJS started in (ms): " + (new Date().getTime() - start.getTime()));
                phantomOutputReader.close();

                try {
                    // Get a handle to the driver. This will throw an exception if a matching driver cannot be located
                    driver = new RemoteWebDriver(new URL("http://localhost:8080"), capabilities);
                    // Query the driver to find out more information
                    actualCapabilities = ((RemoteWebDriver) driver).getCapabilities();
                } catch (UnreachableBrowserException e) {
                    phantomjs.destroy();
                }
                break;
            }
        }

        // And now use this to visit Google
        System.out.println("Loading 'http://www.google.com'...");
        driver.get("http://www.google.com");
        System.out.println("Loaded. Current URL is: '" + driver.getCurrentUrl() + "'");

        // Find the text input element by its name
        System.out.println("Finding an Element via [name='q']...");
        WebElement element = driver.findElement(By.name("q"));
        System.out.println("Found.");

        // Enter something to search for
        System.out.println("Sending keys 'Cheese!...'");
        element.sendKeys("Cheese!");
        System.out.println("Sent.");

        // Now submit the form. WebDriver will find the form for us from the element
        System.out.println("Submitting Element...");
        element.submit();
        System.out.println("Submitted.");

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        driver.close();
        if (phantomjs != null) {
            phantomjs.destroy();
        }
        
        System.out.println("Time elapsed (ms): " + (new Date().getTime() - start.getTime()));
    }
}
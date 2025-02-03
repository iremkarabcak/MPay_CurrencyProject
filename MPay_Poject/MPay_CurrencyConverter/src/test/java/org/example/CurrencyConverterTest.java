package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class CurrencyConverterTest {

    public static By getFromCurrency() {return By.xpath("//*[@id=\"baseCurrency_currency_autocomplete\"]");}

    public static By getToCurrency() {return By.xpath("//*[@id=\"quoteCurrency_currency_autocomplete\"]");}

    public static By getTargetCurrencyOption(){ return By.xpath("//*[@id=\"quoteCurrency_currency_autocomplete-option-0\"]");}

    public static By getDropdownBaseCurrencyResult(){ return  By.xpath("//*[@id=\"baseCurrency_currency_autocomplete-option-0\"]");}

    public static By getResult() { return By.xpath("(//input[@name='numberformat' and @type='text'])[2]");}

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        CurrencyAPIClient apiClient = new CurrencyAPIClient();
        List<String> countryCodes = List.of("GB", "CH");
        List<String> currencies = apiClient.getCurrenciesForCountries(countryCodes);

        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        try {
            driver.get("https://www.oanda.com/currency-converter/en/");

            for (String currency : currencies) {

                System.out.println("EUR -> " + currency + " Conversion is starting.");

                WebElement fromCurrency = driver.findElement(getFromCurrency());
                fromCurrency.clear();
                fromCurrency.click();
                fromCurrency.sendKeys("EUR");
                Thread.sleep(300);

                WebElement toCurrency = driver.findElement(getToCurrency());
                toCurrency.clear();
                toCurrency.click();
                toCurrency.sendKeys(currency);
                Thread.sleep(300);

                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                WebElement dropdownResult= driver.findElement(getTargetCurrencyOption());
                dropdownResult.click();
                Thread.sleep(300);

                WebElement result = driver.findElement(getResult());
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.visibilityOfElementLocated(getResult()));

                result.click();
                String rateText = result.getAttribute("value");
                double rate = Double.parseDouble(rateText);

                System.out.println("EUR -> " + currency + " currency: " + rate);

                if (rate > 1) {
                    System.out.println("Test completed successfully: " + currency + " currency rate is bigger than 1!");
                } else {
                    System.out.println("Test completed: " + currency + " currency rate is smaller than 1!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {

            driver.quit();

        }
    }

}
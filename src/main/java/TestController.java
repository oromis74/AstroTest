import org.apache.http.util.Asserts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import util.DriverController;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class TestController {


    private DriverController driverController;

    @BeforeEach
    public void loadBrowser(){
        this.driverController = new DriverController();
        this.driverController.dr().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        this.driverController.dr().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }



    public void getPage(String url){
        this.driverController.dr().get(url);
    }

    public void input(String field,String value){
        WebElement element = FindElementImpl(By.xpath(field));
        element.sendKeys(value);
    }

    public void click(String field){
        WebElement element = FindElementImpl(By.xpath(field));
        element.click();
    }

    public void assertField(String field, String value){
        WebElement element = FindElementImpl(By.xpath(field));
        System.out.println(element.getText());
        Asserts.check(element.getText().contains(value),"Поле не содержит ожидаемое значение");
    }

    public void contextClick(String field){
        WebElement element = this.driverController.dr().findElement(By.xpath(field));
        Actions actions = new Actions(this.driverController.dr());
        actions.moveToElement(element).contextClick().build().perform();
    }


    @AfterEach
    public void afterTest(){
        this.driverController.dr().quit();
    }



    private WebElement FindElementImpl(By xpath){
        List<WebElement> elements = this.driverController.dr().findElements(xpath);
        assert elements.size()>0;
        System.out.printf("Найдено %s элементов на странице по xpath %s%n",elements.size(),xpath.toString());
        Optional<WebElement> findElement = elements.stream().filter(this::checkElementVisible).findFirst();
        Asserts.check(findElement.isPresent(), "Web Element не найден");
        System.out.println(findElement.get().getTagName());
        return findElement.get();
    }


    private boolean checkElementVisible(WebElement element){
        try{
            Actions actions = new Actions(driverController.dr());
            if (element.isDisplayed() && element.isEnabled()){
                actions.moveToElement(element).build().perform();
                return true;
            }

        }
        catch (WebDriverException e){
            System.out.println(e.getMessage());
        }

        return false;
    }



}

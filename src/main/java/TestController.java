import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import org.apache.http.util.Asserts;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import util.DriverController;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.restassured.RestAssured.given;

public class TestController {


    private DriverController driverController;

    @BeforeEach
    private void loadBrowser(){
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

    public void select(String field,String listElement,String value){
        WebElement elementField = FindElementImpl(By.xpath(field));
        elementField.click();
        WebElement elementList = FindElementImpl(By.xpath(String.format(listElement,value)));
        elementList.click();
    }

    public void click(String field){
        WebElement element = FindElementImpl(By.xpath(field));
        element.click();
    }

    public void contextClick(String field){
        WebElement element = this.driverController.dr().findElement(By.xpath(field));
        Actions actions = new Actions(this.driverController.dr());
        actions.moveToElement(element).contextClick().build().perform();
    }

    public void assertField(String field, String value){
        WebElement element = FindElementImpl(By.xpath(field));
        System.out.println(element.getText());
        Asserts.check(element.getText().contains(value),"Поле не содержит ожидаемое значение");
    }

    /**
     * @param method
     * @param baseurl
     * @param auth
     * @param body
     * @param formParam
     * @param queryParam
     * @return
     */
    public JSONObject useAPI(String method, String baseurl, Map<String, String> auth, JSONObject body, Map<String, String> formParam, Map<String, String> queryParam, Integer waitCode){
        RequestSpecBuilder specBuilder = new RequestSpecBuilder();
        JSONObject returnObject = null;
        specBuilder.setBaseUri(baseurl);
        if(Objects.nonNull(body)){
            specBuilder.setBody(body.toString());
        }
        if(formParam!=null)
            formParam.forEach(specBuilder::addFormParam);
        if(queryParam!=null)
            queryParam.forEach(specBuilder::addQueryParam);
        String responseBody = null;
        Response response = null;
        if(method.contains("POST")){
            specBuilder.addHeader("Content-Type","application/json");
            response = given().log().all().spec(specBuilder.build()).post();
        }
        if(method.contains("GET")){
            response = given().log().all().spec(specBuilder.build()).get();
        }

        System.out.println("--------------------------------------");
        if(response!=null){
            if(waitCode!=null){
                response.then().statusCode(waitCode);
            }
            if(response.getBody()!=null)
                responseBody = response.getBody().prettyPrint();
        }
        else {
            System.out.println("Response is null");
        }
        System.out.println("--------------------------------------");

        try {
            returnObject = new JSONObject(responseBody);
        }
        catch (JSONException e){
            returnObject = new JSONObject("{ \"fix\": "+responseBody+"}");
        }
        finally {
            return returnObject;
        }
    }


    @AfterEach
    private void afterTest(){
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

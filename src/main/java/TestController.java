import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.util.Asserts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import util.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static io.restassured.RestAssured.given;

public class TestController implements MethodUI, MethodsRestAPI {

    private DriverController driverController;

    private ContextController contextController;
    private Properties props;
    @BeforeEach
    private void prepareTest(){
        try {
            props = new Properties();
            props.load(new FileInputStream("src/main/resources/config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Step("Перейти по URL: {url}")
    public void getPage(String url){
        this.driverController = new DriverController();
        this.driverController.dr().get(url);
        this.driverController.dr().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        this.driverController.dr().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
    }

    @Step("В поле {field} ввести значение {value}")
    public void enter(String field, String value){
        WebElement element = FindElementImpl(By.xpath(field));
        element.sendKeys(value);
    }

    @Step("В поле {field} выбрать значение {value}")
    public void select(String field,String listElement,String value){
        WebElement elementField = FindElementImpl(By.xpath(field));
        elementField.click();
        WebElement elementList = FindElementImpl(By.xpath(String.format(listElement,value)));
        elementList.click();
    }

    @Step("Нажать на поле {field}")
    public void click(String field){
        WebElement element = FindElementImpl(By.xpath(field));
        element.click();
    }

    public void contextClick(String field,String value){
        WebElement element = this.driverController.dr().findElement(By.xpath(field));
        Actions actions = new Actions(this.driverController.dr());
        actions.moveToElement(element).contextClick().build().perform();
    }

    @Step("Проверка значения {value} в поле {field}")
    public void assertField(String field, String value){
        WebElement element = FindElementImpl(By.xpath(field));
        System.out.println(element.getText());
        Asserts.check(element.getText().contains(value),"Поле не содержит ожидаемое значение");
    }


    @Step("Вызов API endpoint {baseurl}")
    public ResponseObjectAPI useAPI(Method method,
                             String baseurl,
                             Map<String, String> auth,
                             Map<String, String> formParam,
                             Map<String, String> queryParam,
                             JSONObject body,
                             ResponseSpecification validateResponse,
                             ExpectedReturnBody type){
        RequestSpecBuilder specBuilder = new RequestSpecBuilder();
        ResponseObjectAPI returnObject = new ResponseObjectAPI();
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
        RequestSpecification request = specBuilder.build();


        if (auth != null){
            if(auth.get("type").contains("Bearer")){
                request.auth()
                        .oauth2(auth.get("token"));
            }
            if(auth.get("type").contains("form")){
                request.auth()
                        .form(auth.get("login"),auth.get("pass"));
            }
            if(auth.get("type").contains("form")){
                request.auth()
                        .form(auth.get("login"),auth.get("pass"));
            }
        }


        if(method.equals(Method.POST)){
            specBuilder.addHeader("Content-Type","application/json");
            response = given().log().all().spec(request).post();
        }
        if(method.equals(Method.GET)){
            response = given().log().all().spec(request).get();
        }

        System.out.println("--------------------------------------");


        if(response!=null){
            response.then().spec(validateResponse);
            if(response.getBody()!=null){
                responseBody = response.getBody().prettyPrint();
            }
        }
        else {
            System.out.println("Response is null");
        }
        System.out.println("--------------------------------------");

        try {
            switch (type){
                case JSON -> returnObject.setObject(new JSONObject(responseBody));
                case TEXT -> returnObject.setRowData(responseBody);
                case BYTE -> returnObject.setBytes(responseBody.getBytes(StandardCharsets.UTF_8));

            }
        }
        catch (JSONException e){
            returnObject.setObjects(new JSONArray(responseBody));
        }
        return returnObject;
    }
    
    @AfterEach
    private void afterTest(){
        if(this.driverController!=null)
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

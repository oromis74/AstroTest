import io.qameta.allure.Step;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v111.network.Network;
import org.openqa.selenium.interactions.Actions;
import util.*;
import util.abstr.BaseElement;
import util.web.Template;
import util.web.templateEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static util.constant.UtilityConstants.*;

public abstract class TestController {

    private static Properties props;

    private DriverController driverController;
//    private ContextController contextController;
//    private templateEngine engine;
    private Map<String,String> requests;


    @BeforeAll
    private static void prepareTest(){
        try {
            props = new Properties();
            props.load(new FileInputStream("src/main/resources/config.properties"));
            System.out.println("Load properties config file....");
            props.entrySet().forEach(System.out::println);
            System.out.println("....load properties ended");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Step("Перейти по URL: {url}")
    public final void getPage(String url){
        if (Objects.isNull(this.driverController)){
            this.driverController = new DriverController();
            this.driverController.dr().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            this.driverController.dr().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        }
        this.driverController.dr().get(url);
        this.driverController.dr().manage().window().maximize();
    }

    @Step("В поле {field} ввести значение {value}")
    public final void enter(String field, String value){
        WebElement element = FindElementImpl(By.xpath(field));
        element.sendKeys(value);
    }

    public final void enter(String field, String value, BaseElement element) {
        String xpath = element.getSelector(field);
        WebElement webElement = FindElementByTemplateImpl(xpath);
        webElement.sendKeys(value);
    }

    public final void select(String field, String value) {

    }

    public final void select(String field, String value, BaseElement element) {
        WebElement elementField = FindElementByTemplateImpl(element.getSelector(field));
        elementField.click();
        //TODO
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement elementlist = FindElementByTemplateImpl(element.getListSelector(value));
        elementlist.click();

    }

    @Step("В поле {field} выбрать значение {value}")
    public final void select(String field,String listElement,String value){
        WebElement elementField = FindElementImpl(By.xpath(field));
        elementField.click();
        WebElement elementList = FindElementImpl(By.xpath(String.format(listElement,value)));
        elementList.click();
    }

    @Step("Нажать на поле {field}")
    public final void click(String field){
        WebElement webElement = FindElementImpl(By.xpath(field));
        webElement.click();
    }

    public final void click(String field, BaseElement element) {
        WebElement webElement = FindElementImpl(By.xpath(element.getSelector(field)));
        webElement.click();
    }


    public final void contextClick(String field,String value){
        WebElement element = FindElementImpl(By.xpath(field));
        Actions actions = new Actions(this.driverController.dr());
        actions.moveToElement(element).contextClick().build().perform();
        WebElement listElement = FindElementImpl(By.xpath(value));
        actions.moveToElement(listElement).click().build().perform();
    }

    @Step("Проверить значения {value} в поле {field}")
    public final void assertField(String field, String value){
        WebElement element = FindElementImpl(By.xpath(field));
        System.out.println(element.getText());
        Assertions.assertTrue(element.getText().contains(value),"Поле не содержит ожидаемое значение");
    }

    @Step("Получить все доступные значения по element.")
    public final List<WebElement> getWebElements(String name, BaseElement element){
     List<WebElement> elementList = FindAllElementImpl(By.xpath(element.getSelector(name)));
     return elementList;
    }

    //TODO только для работы с браузером Chrome;
    public final void initDevToolsLog(){
        requests = new HashMap<String, String>();
        ChromeDriver driver = (ChromeDriver) driverController.dr();
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.addListener(
                Network.requestWillBeSent(),
                entry -> {
                        requests.put(entry.getRequest().getUrl(), entry.getRequest().getHeaders().toJson().toString());
                }
        );
    }

    //TODO только для работы с браузером Chrome;
    public final void printDevtoolLog(){
        System.out.println("------------------------------------------------");
        for(Map.Entry<String,String> item : requests.entrySet()){
            System.out.println("NETWORK: "+item.getKey()+" | "+item.getValue());
        }
        System.out.println("------------------------------------------------");
    }


    @Step("Вызов API endpoint {baseurl}")
    public final ResponseObjectAPI useAPI(Method method,
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
        }


        if(method.equals(Method.POST)){
            specBuilder.addHeader("Content-Type","application/json");
            response = given().log().all().spec(request).post();
        }
        if(method.equals(Method.GET)){
            response = given().log().all().spec(request).get();
        }

        if(Objects.nonNull(type) && Objects.nonNull(response)){
            response.then().spec(validateResponse);
            if( response.getBody()!=null){
                returnObject = extractResponseBody(response, type);
            }
        }
        if(Objects.nonNull(type) && Objects.isNull(response)) {
            Assertions.fail("В ответе отсутвует тело");
        }

        return returnObject;
    }

    @Step("Список проверок для вызова API \n ожидаемый код ответа:{code}, типа данных ответа:{type}, шаблон для проверки API:{jsonSchema}")
    protected final ResponseSpecification configValidation(int code, long timeout, String jsonSchema, ContentType type){
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        if(Objects.nonNull(type))
            responseSpecBuilder.expectContentType(type);
        if(Objects.nonNull(code))
            responseSpecBuilder.expectStatusCode(code);
        if(Objects.nonNull(jsonSchema)){
            responseSpecBuilder.expectBody(JsonSchemaValidator.matchesJsonSchema(new File(getProp(JSON_SHEMA_ROOT_PATH)+jsonSchema)));
        }
        if(Objects.nonNull(timeout))
            responseSpecBuilder.expectResponseTime(Matchers.lessThan(timeout), TimeUnit.SECONDS);
        return responseSpecBuilder.build();
    }


    private ResponseObjectAPI extractResponseBody(Response response, ExpectedReturnBody expectedReturnBody){
        ResponseObjectAPI responseObjectAPI = new ResponseObjectAPI();
        try {
            switch (expectedReturnBody){
                case JSON -> responseObjectAPI.setObject(new JSONObject(response.getBody().prettyPrint()));
                case TEXT -> responseObjectAPI.setRowData(response.getBody().prettyPrint());
                case BYTE -> responseObjectAPI.setBytes(response.getBody().prettyPrint().getBytes(StandardCharsets.UTF_8));
            }
        }
        catch (JSONException e){
            responseObjectAPI.setObjects(new JSONArray(response.getBody().prettyPrint()));
        }
        return responseObjectAPI;
    }
    
    @AfterEach
    private void afterTest(){
        if(this.driverController!=null)
            this.driverController.dr().quit();
    }

    private WebElement FindElementImpl(By xpath){
        this.driverController.dr().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        List<WebElement> elements = this.driverController.dr().findElements(xpath);
        assert elements.size()>0;
        System.out.printf("Найдено %s элементов на странице по xpath %s%n",elements.size(),xpath.toString());
        Optional<WebElement> findElement = elements.stream().filter(t->checkElementVisible(t,true)).findFirst();
        Assertions.assertTrue(findElement.isPresent(), "Web Element не найден");
        System.out.println(findElement.get().getTagName());
        return findElement.get();
    }
    private List<WebElement> FindAllElementImpl(By xpath){
        this.driverController.dr().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        List<WebElement> elements = this.driverController.dr().findElements(xpath);
        System.out.printf("Найдено %s элементов на странице по xpath %s%n",elements.size(),xpath.toString());
        return elements.stream().filter(t->checkElementVisible(t,true)).toList();
    }

    private WebElement FindElementByTemplateImpl(String xpath){
        WebElement element = null;
        this.driverController.dr().manage().timeouts().implicitlyWait(Duration.ofMillis(150));
        for(int c = 1; c < 5; c++){
            List<WebElement> elements = this.driverController.dr().findElements(By.xpath(String.format(xpath,c)));
            assert elements.size()>0;
            System.out.printf("Найдено %s элементов на странице по xpath %s%n",elements.size(),xpath.toString());
            Optional<WebElement> findElement = elements.stream().filter(t->checkElementVisible(t,true)).findFirst();
            if(findElement.isPresent()){
                System.out.println(findElement.get().getTagName());
                element = findElement.get();
            }
        }
        Assertions.assertTrue(Objects.nonNull(element),"Не найден web element");
        return element;
    }

    private boolean checkElementVisible(WebElement element, boolean firstSearch){
        try{
            Actions actions = new Actions(driverController.dr());
            if (element.isDisplayed() && element.isEnabled()){
                actions.moveToElement(element).build().perform();
                return true;
            }
        }
        catch (WebDriverException e){
            System.out.println(e.getMessage());
            if(firstSearch){
                ((JavascriptExecutor) driverController.dr()).executeScript("arguments[0].scrollIntoView(true);", element);
                checkElementVisible(element, true);
            }
        }
        return false;
    }

    private String getProp(String key){
        return props.getProperty(key);
    }

}

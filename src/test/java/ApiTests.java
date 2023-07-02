import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.specification.ResponseSpecification;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.ExpectedReturnBody;
import util.ResponseObjectAPI;

import java.io.File;
import java.util.*;

import static util.ExpectedReturnBody.*;

public class ApiTests extends TestController{

    @Test
    @Feature("API")
    @Story("Проверка API получения авторизации")
    public void OneApiTest(){
        JSONObject body = new JSONObject();
        body.put("username","admin");
        body.put("password","password123");
        useAPI(Method.POST,"https://restful-booker.herokuapp.com/auth",
                null,null,null,body,configValidation(200,null,null), JSON);
    }

    @Test
    @Feature("API")
    @Story("Проверка API получения списка и получения по ID")
    public void TwoApiTest(){
        ResponseObjectAPI booksIds = useAPI(Method.GET,"https://restful-booker.herokuapp.com/booking",
                null,null,null,null,configValidation(200,null,null), JSON);

        assert booksIds.getObjects().length()>0;

        //проверить что все жлементы списка имеют ID
        for(int i=0;i<booksIds.getObjects().length();i++){
            assert Objects.nonNull(booksIds.getObjects().getJSONObject(i).get("bookingid"));
        }
        String bookId = String.valueOf(booksIds.getObjects()
                .getJSONObject(0).getInt("bookingid"));


        booksIds = useAPI(Method.GET,String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId ),
                null,null,null,
                null,
                configValidation(200,"src/test/resources/booking-template.json",ContentType.JSON),
                JSON);
        System.out.println(booksIds.getObject());

        //проверить что
        assert Objects.nonNull(booksIds.getObject().get("firstname"));
        assert Objects.nonNull(booksIds.getObject().get("lastname"));
        assert Objects.nonNull(booksIds.getObject().get("bookingdates"));

        //вызов REST API с несуществующим Id, проверка что код ответа будет 404
        booksIds = useAPI(Method.GET,"https://restful-booker.herokuapp.com/booking/00000",
                null,null,null,null,configValidation(404,null,null),
                TEXT);
        System.out.println(booksIds.getRowData());
        //вызов REST API с некорректным методом запроса, проверка что код ответа будет 405
        booksIds = useAPI(Method.PATCH,String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId),
                null,null,null,null,configValidation(405,null,null),
                TEXT);

        System.out.println(booksIds.getRowData());
    }


    @ParameterizedTest
    @ValueSource(strings = {"https://www.google.com/","https://www.ya.ru/"})
    @DisplayName("Тест проверки сайта")
    public void TestSite(String site){
        ResponseObjectAPI response = useAPI(Method.GET,site,null,null,null,null,configValidation(200,null,null), TEXT);
        System.out.println("HTML PAGE: " + response.getRowData());
    }

    @Step("Список проверок для вызова API \n ожидаемый код ответа:{code}, типа данных ответа:{type}, шаблон для проверки API:{jsonSchema}")
    public ResponseSpecification configValidation(int code, String jsonSchema, ContentType type){
        ResponseSpecBuilder responseSpecBuilder = new ResponseSpecBuilder();
        if(Objects.nonNull(type))
            responseSpecBuilder.expectContentType(type);
        if(Objects.nonNull(code))
            responseSpecBuilder.expectStatusCode(code);
        if(Objects.nonNull(jsonSchema))
            responseSpecBuilder.expectBody(JsonSchemaValidator.matchesJsonSchema(new File(jsonSchema)));
        return responseSpecBuilder.build();
    }



}


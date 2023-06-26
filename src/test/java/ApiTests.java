import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.specification.ResponseSpecification;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

public class ApiTests extends TestController{

    @Test
    @Feature("API")
    @Story("Проверка API получения авторизации")
    public void OneApiTest(){
        JSONObject body = new JSONObject();
        body.put("username","admin");
        body.put("password","password123");
        useAPI(Method.POST,"https://restful-booker.herokuapp.com/auth",
                null,null,null,body,configValidation(200,null,null));
    }

    @Test
    @Feature("API")
    @Story("Проверка API получения списка и получения по ID")
    public void TwoApiTest(){
        JSONObject booksIds = useAPI(Method.GET,"https://restful-booker.herokuapp.com/booking",
                null,null,null,null,configValidation(200,null,null));

        assert booksIds.getJSONArray("fix").length()>0;

        //проверить что все жлементы списка имеют ID
        for(int i=0;i<booksIds.getJSONArray("fix").length();i++){
            assert Objects.nonNull(booksIds.getJSONArray("fix").getJSONObject(i).get("bookingid"));
        }
        String bookId = String.valueOf(booksIds.getJSONArray("fix")
                .getJSONObject(0).getInt("bookingid"));

        booksIds = useAPI(Method.GET,String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId ),
                null,null,null,null,configValidation(200,"src/test/resources/booking-template.json",ContentType.JSON));
        System.out.println(booksIds);

        //проверить что
        assert Objects.nonNull(booksIds.get("firstname"));
        assert Objects.nonNull(booksIds.get("lastname"));
        assert Objects.nonNull(booksIds.get("bookingdates"));

        //вызов REST API с несуществующим Id, проверка что код ответа будет 404
        booksIds = useAPI(Method.GET,"https://restful-booker.herokuapp.com/booking/00000",
                null,null,null,null,configValidation(404,null,null));
        System.out.println(booksIds);
        //вызов REST API с некорректным методом запроса, проверка что код ответа будет 405
        booksIds = useAPI(Method.PATCH,String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId),
                null,null,null,null,configValidation(405,null,null));
        System.out.println(booksIds);
    }



    @Step("Проверка ответа API ожидаемый код ответа:{code}, типа данных ответа:{type}, шаблон для проверки API:{jsonSchema}")
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


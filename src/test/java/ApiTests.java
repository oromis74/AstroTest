import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import util.ResponseObjectAPI;

import java.util.Objects;

import static util.ExpectedReturnBody.JSON;
import static util.ExpectedReturnBody.TEXT;

public class ApiTests extends TestController{

    @Test
    @Feature("API")
    @Story("Проверка API получения авторизации")
    public void OneApiTest(){
        JSONObject body = new JSONObject();
        body.put("username","admin");
        body.put("password","password123");
        useAPI(Method.POST,"https://restful-booker.herokuapp.com/auth",
                null,null,null,body,configValidation(200,25,null,null), JSON);
    }

    @Test
    @Feature("API")
    @Story("Проверка API получения списка и получения по ID")
    public void TwoApiTest(){
        ResponseObjectAPI booksIds = useAPI(Method.GET,"https://restful-booker.herokuapp.com/booking",
                null,null,null,null,configValidation(200,10,null,null), JSON);

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
                configValidation(200,25,"restful-booker/booking-template.json",ContentType.JSON),
                JSON);
        System.out.println(booksIds.getObject());

        //проверить что
        assert Objects.nonNull(booksIds.getObject().get("firstname"));
        assert Objects.nonNull(booksIds.getObject().get("lastname"));
        assert Objects.nonNull(booksIds.getObject().get("bookingdates"));

        //вызов REST API с несуществующим Id, проверка что код ответа будет 404
        booksIds = useAPI(Method.GET,"https://restful-booker.herokuapp.com/booking/00000",
                null,null,null,null,configValidation(404,10,null,null),
                TEXT);
        System.out.println(booksIds.getRowData());
        //вызов REST API с некорректным методом запроса, проверка что код ответа будет 405
        booksIds = useAPI(Method.PATCH,String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId),
                null,null,null,null,configValidation(405,10,null,null),
                null);

        System.out.println(booksIds.getRowData());
    }


    @ParameterizedTest
    @ValueSource(strings = {"https://www.google.com/","https://www.ya.ru/"})
    @DisplayName("Тест проверки сайта")
    @Tag("run")
    public void TestSite(String site){
        ResponseObjectAPI response = useAPI(Method.GET,site,null,null,null,null,configValidation(200,10,null,null), TEXT);
        System.out.println("HTML PAGE: " + response.getRowData());
    }




}


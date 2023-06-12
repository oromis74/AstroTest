import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApiTests extends TestController{


    @Test
    public void OneApiTest(){
        JSONObject body = new JSONObject();
        body.put("username","admin");
        body.put("password","password123");
        useAPI("POST","https://restful-booker.herokuapp.com/auth",
                null,body,null,null,200);
    }

    @Test
    public void TwoApiTest(){
        Map<String,String> queryRequest = new HashMap<>();
        queryRequest.put("firstname","sally");
        queryRequest.put("lastname","brown");
        JSONObject booksIds = useAPI("GET","https://restful-booker.herokuapp.com/booking",
                null,null,null,null,200);

        assert booksIds.getJSONArray("fix").length()>0;

        //проверить что все жлементы списка имеют ID
        for(int i=0;i<booksIds.getJSONArray("fix").length();i++){
            assert Objects.nonNull(booksIds.getJSONArray("fix").getJSONObject(i).get("bookingid"));
        }
        String bookId = String.valueOf(booksIds.getJSONArray("fix")
                .getJSONObject(0).getInt("bookingid"));

        booksIds = useAPI("GET",String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId ),
                null,null,null,null,200);

        //проверить что
        assert Objects.nonNull(booksIds.get("firstname"));
        assert Objects.nonNull(booksIds.get("lastname"));
        assert Objects.nonNull(booksIds.get("bookingdates"));

        //вызов REST API с несуществующим Id, проверка что код ответа будет 404
        booksIds = useAPI("GET","https://restful-booker.herokuapp.com/booking/00000",
                null,null,null,null,404);
        System.out.println(booksIds);
        //вызов REST API с некорректным методом запроса, проверка что код ответа будет 405
        booksIds = useAPI("UPDATE",String.format("https://restful-booker.herokuapp.com/booking/%s",
                        bookId),
                null,null,null,null,405);
        System.out.println(booksIds);
    }


}

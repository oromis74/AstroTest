package util;

import io.restassured.http.Method;
import io.restassured.specification.ResponseSpecification;
import org.json.JSONObject;

import java.util.Map;

public interface MethodsRestAPI {

    ResponseObjectAPI useAPI(Method method,
                String url,
                Map<String, String> auth,
                Map<String, String> formParam,
                Map<String, String> queryParam,
                JSONObject body,
                ResponseSpecification responseSpecification,
                ExpectedReturnBody type
                      );


}

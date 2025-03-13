package components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Step;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import util.component.BaseComponent;

import java.util.Arrays;

@ToString
public class BaseField extends BaseComponent {


    @Step("Нажать на поле {field}")
    public void click(){
        Selenide.$x(getXpath()).should(Condition.exist, Condition.visible).click();
    }


    @Step("В поле {field} ввести значение {value}")
    public void enter(String value){
        Selenide.$x(getXpath()).should(Condition.exist, Condition.visible).setValue(value);
    }

    @Step("Проверить значения {value} в поле {field}")
    public final void assertField(String expected){
        String elementValue = Selenide.$x(getXpath()).should(Condition.exist, Condition.visible).getText();
        System.out.println(elementValue);
        Assertions.assertTrue(elementValue.contains(expected),"Поле не содержит ожидаемое значение");
    }

    @Step("В поле {field} ввести значение {value}")
    public void select(String value){
        Selenide.$x(getXpath()).should(Condition.exist, Condition.visible).click();
        Selenide.$x(getXpathOption(value)).should(Condition.exist, Condition.visible).click();
    }

    @Step("В поле {field} ввести значение {value}")
    public void select(String... values){
        Selenide.$x(getXpath()).should(Condition.exist, Condition.visible).click();
        Arrays.stream(values).forEach(item->{
            Selenide.$x(getXpathOption(item)).should(Condition.exist, Condition.visible).click();
        });
    }

}
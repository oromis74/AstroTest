import org.junit.jupiter.api.Test;

public class SeleniumTests extends TestController {


    @Test
    public void YandexTest(){
        getPage("https://ya.ru/");
        input("//input[@id='text']","Selenium 4");
        click("//button[text()='Найти']");
        assertField("//li[contains(@class,'serp-item')][1]//div[contains(@class,'Organic-Subtitle')]//b",
                "selenium.dev");

    }


    @Test
    public void GoogleTest(){
        getPage("https://www.google.com/");
        input("//textarea[@title='Поиск']","Selenium 4");
        click("//img[@alt='Google']");
        click("//input[@aria-label='Поиск в Google']");
        assertField("//h1[text()='Результаты поиска']/ancestor::div[1]/div/div[1]//cite",
                "selenium.dev");
    }

}

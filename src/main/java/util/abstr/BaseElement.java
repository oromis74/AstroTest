package util.abstr;

import org.openqa.selenium.By;


/**
 * Класс для описания web element
 *
 */

public final class BaseElement {

    private BaseElement parent;

    private String selector;

    private String listSelector;


    public String getSelector(String... args) {
        if(checkFormat()){
            return String.format(this.selector, args);
        }
        else {
            return String.format(this.selector);
        }
    }

    public String getListSelector(String... args) {
        if(checkFormat()){
            return String.format(this.listSelector, args);
        }
        else {
            return String.format(this.listSelector);
        }
    }

    public BaseElement getParent() {
        return parent;
    }

    public By getBy(String... args) {
        if(checkFormat()){
            return By.xpath(String.format(this.selector, (Object) args));
        }
        else {
            return By.xpath(String.format(this.selector));
        }
    }
    public By getByListSelector(String... args) {
        if(checkFormat()){
            return By.xpath(String.format(this.listSelector, (Object) args));
        }
        else {
            return By.xpath(String.format(this.listSelector));
        }
    }

    public BaseElement(String selector) {
        this.selector = selector;
    }

    public BaseElement(String selector, String listSelector) {
        this.selector = selector;
        this.listSelector = listSelector;
    }

    public BaseElement(BaseElement parent, String selector) {
        this.parent = parent;
        this.selector = selector;
    }

    public BaseElement(BaseElement parent, String selector, String listSelector) {
        this.parent = parent;
        this.selector = selector;
        this.listSelector = listSelector;
    }

    private boolean checkFormat(){
        return selector.contains("%s");
    }
}

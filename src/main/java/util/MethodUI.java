package util;

public interface MethodUI {

    void click(String field);
    void contextClick(String field,String value);
    void enter(String field, String value);
    void select(String field,String listElement,String value);


}

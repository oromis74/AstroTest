package util.component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class FactoryUI {



    public static  <T> T init(Class<T> x){
        try {
            T instance = x.getDeclaredConstructor().newInstance();
            ElementUI elementUI = null;
            if(x.isAnnotationPresent(ElementUI.class)){
                elementUI = x.getAnnotation(ElementUI.class);
                initField("xpath", compileXpath("", elementUI.xpath(), elementUI.id()), instance);
                initField("xpathOption", compileXpath("", elementUI.xpathOption(), elementUI.id()), instance);
                initField("number", elementUI.number(), instance);
            }

            for(Field field : x.getDeclaredFields()){
                if (field.isAnnotationPresent(ElementUI.class)){
                    initElement(field, instance,field.getAnnotation(ElementUI.class), elementUI);
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T init(Class<T> x, int number){
        try {
            T instance = x.getDeclaredConstructor().newInstance();
            ElementUI elementUI = null;
            if(x.isAnnotationPresent(ElementUI.class)){
                elementUI = x.getAnnotation(ElementUI.class);
                initField("xpath", compileXpath("", elementUI.xpath(), elementUI.id()), instance);
                initField("xpathOption", compileXpath("", elementUI.xpathOption(), elementUI.id()), instance);
                initField("number", number, instance);
            }

            for(Field field : x.getDeclaredFields()){
                if (field.isAnnotationPresent(ElementUI.class)){
                    initElement(field, instance,field.getAnnotation(ElementUI.class), elementUI);
                }
            }


        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    private static <T> void initElement(Field field, T instance, ElementUI elementUI, ElementUI parent) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        field.setAccessible(true);
        T fieldInstance = (T) field.getType().getDeclaredConstructor().newInstance();
        String parentXpath = (parent!=null) ?  parent.xpath().replaceAll("%id", parent.id()) : "";
        initField("xpath", compileXpath(parentXpath, elementUI.xpath(), elementUI.id()), fieldInstance);
        initField("xpathOption", compileXpath("", elementUI.xpathOption(), elementUI.id()), fieldInstance);
        initField("number", elementUI.number(), fieldInstance);
        field.set(instance, fieldInstance);
    }



    private static <T> void initField(String field, Object value, T instance){
        try {
            Field numberField = null;        numberField = BaseComponent.class.getDeclaredField(field);        numberField.setAccessible(true);        numberField.set(instance, value);    } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);    }
    }

    private static String compileXpath(String parent, String xpath, String id){
        return parent + xpath.replaceAll("%id", id);
    }

}

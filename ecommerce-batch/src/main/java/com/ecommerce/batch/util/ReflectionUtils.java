package com.ecommerce.batch.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Modifier.isStatic;

public class ReflectionUtils {

    //clazz는 클래스타입이 들어올거기 떄문에 저렇게 작성
    // Class<?> clazz = 클래스 정보 받기
    public static List<String> getFiledNames(Class<?> clazz) {
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields(); //모든 필드 가져오기
        for (Field field : fields) {
            //getModifiers 정적인지 아닌지 확인할수 있다
            // static이 아닐경우에만 필드 네임을 추가
            if (!isStatic(field.getModifiers())){
                fieldNames.add(field.getName());
            }

        }
        return fieldNames;
    }
}

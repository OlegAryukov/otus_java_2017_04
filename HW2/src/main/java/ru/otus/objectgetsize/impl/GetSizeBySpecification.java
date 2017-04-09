package ru.otus.objectgetsize.impl;

import ru.otus.objectgetsize.ObjectGetSize;
import ru.otus.objectgetsize.GettingSizeError;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by sergey on 09.04.17.
 */
public class GetSizeBySpecification implements ObjectGetSize {
//todo implement support of field named SIZE in user classes

    @Override
    public long getSize(Object object) throws GettingSizeError {
        try {
            return deepObjectGetSize(object, 0);
        } catch (IllegalAccessException e) {
            throw new GettingSizeError(e.getMessage(), e);
        }
    }

    private int getSizeSpec(Class type, Object object) throws GettingSizeError, IllegalAccessException {
        if ("java.lang.String".equals(type.getName())) {
            int fieldsCounter = type.getDeclaredFields().length;
            for (int idx = 0; idx < fieldsCounter; idx++) {
                Field field = type.getDeclaredFields()[idx];
                if (field.getName().equals("value")) {
                    field.setAccessible(true);
                    char[] chars = (char[]) field.get(object);
                    return (int) getSize(chars);
                }
            }
        } else {
            int fieldsCounter = type.getDeclaredFields().length;
            for (int idx = 0; idx < fieldsCounter; idx++) {
                Field field = type.getDeclaredFields()[idx];
                if (field.getName().equals("SIZE")) {
                    return field.getInt(type);
                }
            }
            if (fieldsCounter > 0 ) {
                int classSize = 0;
                for (int idx = 0; idx < fieldsCounter; idx++) {
                    Field field = type.getDeclaredFields()[idx];
                    if (!"this$0".equals(field.getName())) {
                        Class clazzParent = field.getType();
                        if (clazzParent != null) {
                            field.setAccessible(true);
                            Object objValue = field.get(object);

                            if (objValue.getClass().equals(type)) {
                                throw new GettingSizeError("infinity loop detected:" + type);
                            }
                            classSize +=getSizeSpec(objValue.getClass(), objValue);
                        }
                    }
                }
                return classSize;
            }
        }
        throw new GettingSizeError("unknown type:" + type);
    }

    private long deepObjectGetSize(Object object, long accum) throws GettingSizeError, IllegalAccessException {
        boolean isArray = object.getClass().isArray();

        if (isArray) {
          long accummArr = accum;
          for (int idx = 0; idx < Array.getLength(object); idx++) {
            accummArr += deepObjectGetSize(Array.get(object, idx), accum);
          }
          return accummArr;
        } else {
            return accum + getSizeSpec(object.getClass(), object);
        }
    }
}

package com.thomas.rxpreferences;

import java.lang.reflect.Type;

/**
 * Created by Wiki on 16/7/16.
 */
public interface PersistenceParser {
    Object deserialize(Type clazz, String text);

    String serialize(Object object);
}

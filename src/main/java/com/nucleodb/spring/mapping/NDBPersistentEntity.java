package com.nucleodb.spring.mapping;

import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

public class NDBPersistentEntity<T> extends BasicPersistentEntity<T, NDBPersistentProperty> {

    public NDBPersistentEntity(TypeInformation<T> information) {
        super(information);
    }
}
package com.nucleodb.spring.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.utils.Serializer;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NDBMappingContext extends AbstractMappingContext<NDBPersistentEntity<?>, NDBPersistentProperty> {
    public NDBMappingContext() {
    }

    public void register(Set<Class<?>> entityClasses) {
        Set<Class<?>> entityClassSet = new HashSet<>(entityClasses);
        entityClassSet.addAll(entityClasses.stream().filter(clazz-> DataEntry.class.isAssignableFrom(clazz)).map(de->{
            Type genericSuperclass = de.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;

                // Get the actual type arguments (in this case, it will be String due to type erasure)
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                for (Type type : actualTypeArguments) {
                    return (Class<?>)type;
                }
            }
            return null;
        }).filter(c->c!=null).collect(Collectors.toSet()));
        setInitialEntitySet(entityClassSet);
    }

    @Override
    public NDBPersistentEntity<?> getRequiredPersistentEntity(NDBPersistentProperty persistentProperty) throws MappingException {
        System.out.println(persistentProperty.getName());
        System.out.println(persistentProperty.getType().getName());
        System.out.println(persistentProperty.getActualType().getName());

        return super.getRequiredPersistentEntity(persistentProperty.getActualType());
    }

    @Override
    protected <T> NDBPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
        return new NDBPersistentEntity<>(typeInformation);
    }

    @Override
    protected NDBPersistentProperty createPersistentProperty(Property propertyDescriptor, NDBPersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {
        try {
            return new NDBPersistentProperty(propertyDescriptor, simpleTypeHolder, owner);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }


}
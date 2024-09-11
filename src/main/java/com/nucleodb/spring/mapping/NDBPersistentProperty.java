package com.nucleodb.spring.mapping;

import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.table.DataEntry;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class NDBPersistentProperty implements PersistentProperty<NDBPersistentProperty> {


    private final Property property;
    private final SimpleTypeHolder simpleTypeHolder;
    private final Field field;
    private final NDBPersistentEntity<?> owner;
    boolean isTransient;

    public NDBPersistentProperty(Property property, SimpleTypeHolder simpleTypeHolder, NDBPersistentEntity<?> owner) throws NoSuchFieldException {
        this.property = property;
        this.simpleTypeHolder = simpleTypeHolder;
        this.owner = owner;
        field = getFieldByNameIncludingParents(owner.getType(), property.getName());
    }


    public Field getFieldByNameIncludingParents(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            try {
                Field field = currentClass.getDeclaredField(fieldName);
                // Check if the field is transient
                isTransient = Modifier.isTransient(field.getModifiers());
                return field;
            } catch (NoSuchFieldException e) {
                // Field not found in this class, move to the parent class
                currentClass = currentClass.getSuperclass();
            }
        }

        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy.");
    }

    @Override
    public PersistentEntity<?, NDBPersistentProperty> getOwner() {
        return owner;
    }

    @Override
    public String getName() {
        return property.getName();
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }

    @Override
    public TypeInformation<?> getTypeInformation() {
        return TypeInformation.of(field.getType());
    }

    @Override
    public Iterable<? extends TypeInformation<?>> getPersistentEntityTypeInformation() {
        return List.of(TypeInformation.of(field.getType()));
    }

    @Override
    public Method getGetter() {
        return property.getGetter().get();
    }

    @Override
    public Method getSetter() {
        System.out.println("get setter for field: "+field.getName());
        return property.getSetter().get();
    }

    @Override
    public Method getWither() {
        return null;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public String getSpelExpression() {
        return "";
    }

    @Override
    public Association<NDBPersistentProperty> getAssociation() {
        return null;
    }

    @Override
    public boolean isEntity() {
        return !simpleTypeHolder.isSimpleType(getRawType());
    }

    @Override
    public boolean isIdProperty() {
        // Logic to determine if this property is the ID (e.g., check for field name "id")
        if(Connection.class.isAssignableFrom(owner.getType())){
            return "uuid".equals(getName());
        }else if(DataEntry.class.isAssignableFrom(owner.getType())){
            return "key".equals(getName());
        }
        return false;
    }

    @Override
    public boolean isVersionProperty() {
        if(DataEntry.class.isAssignableFrom(owner.getType())){
            switch (field.getName()){
                case "version": return true;
            }
        }
        return false;
    }

    @Override
    public boolean isCollectionLike() {
        return Iterable.class.isAssignableFrom(getRawType());
    }

    @Override
    public boolean isMap() {
        return java.util.Map.class.isAssignableFrom(getRawType());
    }

    @Override
    public boolean isArray() {
        return getRawType().isArray();
    }

    @Override
    public boolean isTransient() {
        return isTransient;
    }

    @Override
    public boolean isWritable() {
        return getSetter()!=null;
    }

    @Override
    public boolean isReadable() {
        return getGetter()!=null;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }


    @Override
    public boolean isAssociation() {
        return false;
    }

    @Override
    public Class<?> getComponentType() {
        return field.getType().getComponentType();
    }

    @Override
    public Class<?> getRawType() {
        return property.getType();
    }

    @Override
    public Class<?> getMapValueType() {
        return property.getType();
    }

    @Override
    public Class<?> getActualType() {
        return property.getType();
    }

    @Override
    public <A extends Annotation> A findAnnotation(Class<A> annotationType) {
        return field.getAnnotation(annotationType);
    }

    @Override
    public <A extends Annotation> A findPropertyOrOwnerAnnotation(Class<A> annotationType) {
        return owner.getType().getAnnotation(annotationType);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return field.isAnnotationPresent(annotationType);
    }

    @Override
    public boolean usePropertyAccess() {
        return false;
    }

    @Override
    public Class<?> getAssociationTargetType() {
        return null;
    }

    @Override
    public TypeInformation<?> getAssociationTargetTypeInformation() {
        return null;
    }

}
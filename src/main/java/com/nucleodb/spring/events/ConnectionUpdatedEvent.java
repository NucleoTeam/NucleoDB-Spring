package com.nucleodb.spring.events;

import com.nucleodb.library.database.tables.connection.Connection;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public class ConnectionUpdatedEvent<T extends Connection> implements ResolvableTypeProvider{
  private T connection;

  public ConnectionUpdatedEvent(T connection) {
    this.connection = connection;
  }

  public T getConnection() {
    return connection;
  }

  public void setConnection(T connection) {
    this.connection = connection;
  }

  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        getClass(),
        ResolvableType.forInstance(this.connection)
    );
  }
}
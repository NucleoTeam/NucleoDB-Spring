package com.nucleodb.spring.query.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.Type;

public class QueryOperation implements Serializable, Operation{
  String method;
  Operation next;

  transient Type returnType;

  public QueryOperation() {
  }

  public QueryOperation(String method, Operation next) {
    this.method = method;
    this.next = next;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public Operation getNext() {
    return next;
  }

  public void setNext(Operation next) {
    this.next = next;
  }

  @JsonIgnore
  public Type getReturnType() {
    return returnType;
  }

  public void setReturnType(Type returnType) {
    this.returnType = returnType;
  }
}

package com.nucleodb.spring.query.common;

public class LookupOperation implements Operation{
  OperatorProperty property;
  Operation next = null;

  public LookupOperation(OperatorProperty property, Operation next) {
    this.property = property;
    this.next = next;
  }

  public LookupOperation(OperatorProperty property) {
    this.property = property;
  }

  @Override
  public Operation getNext() {
    return next;
  }

  public OperatorProperty getProperty() {
    return property;
  }

  public void setProperty(OperatorProperty property) {
    this.property = property;
  }

  public void setNext(Operation next) {
    this.next = next;
  }
}

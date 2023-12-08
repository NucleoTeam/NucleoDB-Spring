package com.nucleodb.spring.query.common;

import java.io.Serializable;

public class ConditionOperation implements Serializable, Operation{
  String conditional;
  Operation next;

  public ConditionOperation(String conditional, Operation next) {
    this.conditional = conditional;
    this.next = next;
  }

  public String getConditional() {
    return conditional;
  }

  public void setConditional(String conditional) {
    this.conditional = conditional;
  }

  public Operation getNext() {
    return next;
  }

  public void setNext(Operation next) {
    this.next = next;
  }
}
package com.nucleodb.spring.events;

import com.nucleodb.library.database.tables.table.DataEntry;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public class DataEntryDeletedEvent<T extends DataEntry> implements ResolvableTypeProvider{
  private T dataEntry;

  public DataEntryDeletedEvent(T dataEntry) {
    this.dataEntry = dataEntry;
  }

  public T getDataEntry() {
    return dataEntry;
  }

  public void setDataEntry(T dataEntry) {
    this.dataEntry = dataEntry;
  }
  @Override
  public ResolvableType getResolvableType() {
    return ResolvableType.forClassWithGenerics(
        getClass(),
        ResolvableType.forInstance(this.dataEntry)
    );
  }
}
## Overview
NucleoDB Spring Repository Library makes it easier to use NucleoDB.

### Installation

##### Dependencies

* Kafka Cluster
  * /docker/kafka/docker-compose.yml

##### Import library
```groovy
repositories {
    mavenCentral()
    maven { url "https://nexus.synload.com/repository/maven-repo-releases/" }
}
dependencies {
    implementation 'com.nucleodb:library:1.13.18'
}
```

##### Initializing DB
```java
import com.nucleodb.library.NucleoDB;

class Application{
  public static void main(String[] args) {
    NucleoDB nucleoDB = new NucleoDB(
        NucleoDB.DBType.NO_LOCAL,
        "com.package.string"
    );
  }
}
```

```java

import java.io.Serializable;

@Table(tableName = "author", dataEntryClass = AuthorDE.class)
public class Author implements Serializable {
  @Index
  String name;
  public Author(String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

public class AuthorDE extends DataEntry<Author>{
  
}
```

##### Read data
```java
class Application {
  public static void main(String[] args) {
    Set<DataEntry> first = nucleoDB.getTable(Author.class).get("name", "test", new DataEntryProjection(){{
      setWritable(true);
    }});
  }
}
```

##### Write data

```java
class Application{
  public static void main(String[] args) {
    AuthorDE test = new AuthorDE(new Author("test"));
    nucleoDB.getTable(Author.class).saveSync(test);
  }
}
```

##### Delete data

```java
class Application{
  public static void main(String[] args) { 
    // read data 
    // var author = AuthorDE()
    nucleoDB.getTable(Author.class).deleteSync(author);
  }
}
```
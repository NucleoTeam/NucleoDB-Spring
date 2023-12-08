package com.nucleodb.spring.query;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class ParserConstants{
  static String propertyMatch = "([a-z_A-Z0-9]+)";
  static String propertyMatchOrBlank = "([a-z_A-Z0-9]+|)";
  public static Map<String, String[]> prefix = new TreeMap<>(){
    @Override
    public String[] put(String key, String[] value) {
      return super.put(createRegexString(key), value);
    }
    {
      put("find|By$", new String[]{"findBy", "returnObject", "operations"});
      put("read|By$", new String[]{"findBy", "returnObject", "operations"});
      put("query|By$", new String[]{"findBy", "returnObject", "operations"});
      put("get|By$", new String[]{"findBy", "returnObject", "operations"});
      put("stream|By$", new String[]{"streamBy", "returnObject", "operations"});
      put("search|By$", new String[]{"searchBy", "returnObject", "operations"});
      put("count|By$", new String[]{"countBy", "returnObject", "operations"});
      put("exists|By$", new String[]{"existsBy", "returnObject", "operations"});
      put("deleteBy$", new String[]{"deleteBy", "operations"});
      put("removeBy$", new String[]{"deleteBy", "operations"});
    }};
  public static Map<String, String[]> operator = new TreeMap<>(){
    @Override
    public String[] put(String key, String[] value) {
      return super.put(createRegexString(key), value);
    }
    {
      put("OrderBy$", new String[]{"ORDERBY", "property"});
      put("Or$", new String[]{"OR", "property"});
      put("And$", new String[]{"AND", "", "property"});
    }};

  public static String propertyNotExpression = "([a-z_A-Z0-9]+)Not([a-z_A-Z0-9]+)";
  public static Pattern propertyNotRegexPattern = Pattern.compile(propertyNotExpression);

  public static Map<String, String[]> conditional = new TreeMap<>(){
    @Override
    public String[] put(String key, String[] value) {
      return super.put(createRegexString(key), value);
    }
    {
      put("$GreaterThan", new String[]{">"}); // implemented
      put("$LessThan", new String[]{"<"}); // implemented
      put("$Between", new String[]{"<>"});
      put("$Before", new String[]{"["}); // implemented
      put("$After", new String[]{"]"}); // implemented
      put("$IsNull", new String[]{"=n"});
      put("$IsNotNull", new String[]{"!=n"});
      put("$Like", new String[]{"Like"});
      put("$NotLike", new String[]{"NotLike"});
      put("$StartingWith", new String[]{"s%"}); // implemented
      put("$EndingWith", new String[]{"%s"}); // implemented
      put("$Containing", new String[]{"contains"}); // implemented
      put("$In", new String[]{"In"});
      put("$True", new String[]{"true"});
      put("$False", new String[]{"false"});
      put("$IgnoreCase", new String[]{"ignoreCase"});
      put("$Empty", new String[]{"empty"});
    }};

  static String createRegexString(String template){
    return template.replace("|", propertyMatchOrBlank)
        .replace("$", propertyMatch);
  }
}

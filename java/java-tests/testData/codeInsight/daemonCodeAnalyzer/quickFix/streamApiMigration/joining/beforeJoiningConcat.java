// "Replace with collect" "true-preview"

import java.util.List;

public class Test {
  static String test(List<String> list) {
    StringBuilder sb = new StringBuilder();
    System.out.println("hello");
    for(String s : li<caret>st) {
      if(!s.isEmpty()) {
        sb.append(s.trim());
      }
    }
    String s = "Result: ";
    s += sb;
    System.out.println(s);
    return "[" + sb + "]";
  }
}
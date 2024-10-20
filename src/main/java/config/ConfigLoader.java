package config;

import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
  public static Properties loadProperties() {
    var properties = new Properties();
    try (var input =
        ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (input == null) {
        throw new RuntimeException("Couldn't find config file");
      }
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Unable to load config");
    }
    return properties;
  }
}

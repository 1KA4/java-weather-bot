import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class WeatherApp {
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_CYAN = "\u001B[36m";

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
      System.out.print("Wpisz miasto (np. Warszawa). Aby wyjść wpisz 0: ");
      String location = scanner.nextLine().trim();
      if (location.equals("0")) break;

      WeatherAgent agent = new WeatherAgent(location);
      String xmlWeather = agent.getXMLWeather();

      if (Objects.isNull(xmlWeather)) continue;
      displayCurrentWeather(xmlWeather);

      String forecastXml = agent.getXMLForecast();
      if (Objects.isNull(forecastXml)) continue;
      displayWeatherForecast(forecastXml, agent.getNumDaysForecast());
    }
  }

  private static void displayCurrentWeather(String xmlWeather) {
    WeatherData currentWeather = parseWeatherData(xmlWeather);

    if (!Objects.isNull(currentWeather)) {
      System.out.println(ANSI_GREEN + "Aktualna pogoda w " + currentWeather.getLocation() + ":" + ANSI_RESET);
      System.out.println("Temperatura: " + currentWeather.getTemperature() + " °C");
      System.out.println("Ciśnienie: " + currentWeather.getPressure() + " hPa");
      System.out.println("Prędkość wiatru: " + currentWeather.getWindSpeed() + " m/s");
      System.out.println("Wilgotność: " + currentWeather.getHumidity() + "%");
      System.out.println();
    } else {
      System.out.println("Nie można pobrać aktualnych danych pogodowych.");
    }
  }

  private static void displayWeatherForecast(String xmlForecast, int numDaysForecast) {
    WeatherData[] forecast = parseWeatherForecast(xmlForecast, numDaysForecast);

    if (!Objects.isNull(forecast)) {
      System.out.println(ANSI_GREEN + "Prognoza na najbliższe " + numDaysForecast + " dni:" + ANSI_RESET);
      for (int i = 0; i < Objects.requireNonNull(forecast).length; i++) {
        System.out.println(ANSI_CYAN + "Data: " + forecast[i].getDate() + ANSI_RESET);
        System.out.println("Minimalna temperatura: " + forecast[i].getMinTemperature() + " °C");
        System.out.println("Maksymalna temperatura: " + forecast[i].getMaxTemperature() + " °C");
        System.out.println();
      }
    } else {
      System.out.println("Nie można pobrać 5-dniowej prognozy pogody.");
    }
  }

  private static WeatherData parseWeatherData(String xml) {
    String location;
    double temperature;
    double pressure;
    double windSpeed;
    int humidity;

    try {
      // Parse weather data
      location = getXMLAttribute(getXMLValue(xml, "city"), "name");

      String temperatureValue = getXMLAttribute(getXMLValue(xml, "temperature"), "value");
      temperature = Double.parseDouble(temperatureValue);

      String pressureValue = getXMLAttribute(getXMLValue(xml, "pressure"), "value");
      pressure = Double.parseDouble(pressureValue);

      String windValue = getXMLValue(xml, "wind");
      String windSpeedValue = getXMLAttribute(getXMLValue(windValue, "speed"), "value");
      windSpeed = Double.parseDouble(windSpeedValue);

      String humidityValue = getXMLAttribute(getXMLValue(xml, "humidity"), "value");
      humidity = Integer.parseInt(humidityValue);

      return new WeatherData(location, temperature, pressure, windSpeed, humidity);
    } catch (Exception e) {
      System.out.println("Błąd podczas parsowania aktualnej pogody: " + e.getMessage());
      return null;
    }
  }

  private static WeatherData[] parseWeatherForecast(String xml, int numDaysForecast) {
    WeatherData[] forecast = new WeatherData[numDaysForecast];
    String currentDate = parseDate(new Date());
    int startIndex = 0;
    String nextDate;
    String date;

    int i = 0;
    double minTemperature = Double.NaN;
    double maxTemperature = Double.NaN;

    try {
      while(startIndex != -1) {
        String weatherXml = getXMLValue(xml.substring(startIndex), "time");
        startIndex = xml.indexOf("<time", startIndex + 4);

        date = parseDate(getXMLAttribute(weatherXml, "from"));
        if (Objects.equals(date, currentDate)) continue;

        if (startIndex != -1) {
          String nextTimeValue = getXMLValue(xml.substring(startIndex), "time");
          nextDate = parseDate(getXMLAttribute(nextTimeValue, "from"));
        } else {
          nextDate = null;
        }

        String temperatureTag = getXMLValue(weatherXml, "temperature");

        double minT = Double.parseDouble(getXMLAttribute(temperatureTag, "min"));
        minTemperature = !Double.isNaN(minTemperature) ? Math.min(minT, minTemperature) : minT;
        double maxT = Double.parseDouble(getXMLAttribute(temperatureTag, "max"));
        maxTemperature = !Double.isNaN(maxTemperature) ? Math.max(maxT, maxTemperature) : maxT;

        if (!Objects.equals(date, nextDate)) {
          forecast[i] = new WeatherData(date, minTemperature, maxTemperature);

          minTemperature = Double.NaN;
          maxTemperature = Double.NaN;
          i++;
        }
      }
      return forecast;
    } catch (Exception e) {
      System.out.println("Błąd podczas parsowania 5-dniowej prognozy: " + e.getMessage());
    }

    return null;
  }

  private static String parseDate(String dateString) {
    try {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
      Date parsedDate = dateFormat.parse(dateString);
      dateFormat = new SimpleDateFormat("yyyy-MM-dd");

      return dateFormat.format(parsedDate);
    } catch (Exception e) {
      System.out.println("Nie udało się przeparsować date! " + e.getMessage());
    }

    return null;
  }

  private static String parseDate(Date date) {
    try {
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
      return dateFormat.format(date);
    } catch (Exception e) {
      System.out.println("Nie udało się przeparsować date! " + e.getMessage());
    }

    return null;
  }

  private static String getXMLValue(String xml, String tagName) {
    int start = xml.indexOf("<" + tagName);
    int end = xml.indexOf("</" + tagName + ">");

    if (end == -1) {
      end = xml.indexOf("/>", start);
    } else {
      end += tagName.length() + 3;
    }
    return xml.substring(start, end);
  }

  private static String getXMLAttribute(String xml, String attributeName) {
    int start = xml.indexOf(attributeName + "=\"") + attributeName.length() + 2;
    int end = xml.indexOf("\"", start);
    return xml.substring(start, end);
  }
}
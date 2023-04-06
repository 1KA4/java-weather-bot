import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherAgent {
  String location;

  public WeatherAgent(String location) {
    this.location = location;
  }

  private static final String API_KEY = "79e7fde73f73e8e6d61f3b69ece55c0c";
  private static final String API_BASE_URL = "https://api.openweathermap.org/data/2.5/";
  private static final int NUM_DAYS_FORECAST = 5;
  private static final int API_CNT_FORECAST = NUM_DAYS_FORECAST * 8;

  public String getXMLWeather() {
    return makeRequest(buildUrl("weather"));
  }

  public String getXMLForecast() {
    return makeRequest(buildUrl("forecast"));
  }

  public int getNumDaysForecast() {
    return NUM_DAYS_FORECAST;
  }

  private String buildUrl(String mode) {
    return API_BASE_URL + mode + "?q=" + this.location + "&mode=xml&cnt=" + API_CNT_FORECAST + "&units=metric&appid=" + API_KEY;
  }

  private static String makeRequest(String stringUrl) {
    try {
      URL url = new URL(stringUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();

      int responseCode = connection.getResponseCode();

      if (responseCode == HttpURLConnection.HTTP_OK) {
        StringBuilder response = new StringBuilder();
        Scanner scanner = new Scanner(url.openStream());

        while (scanner.hasNext()) {
          response.append(scanner.nextLine());
        }
        scanner.close();

        return response.toString();
      } else {
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
          System.out.println("Proszę upewnić się że miejscowość została wprowadzana poprawnie!");
        } else {
          throw new RuntimeException("Coś poszło nie tak! Kod błędu " + responseCode);
        }
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    return null;
  }
}

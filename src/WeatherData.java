public class WeatherData {
    private String location;
    private String date;
    private double temperature;
    private double pressure;
    private double windSpeed;
    private double minTemperature;
    private double maxTemperature;
    private int humidity;

    public WeatherData(String location, double temperature, double pressure, double windSpeed, int humidity) {
        this.location = location;
        this.temperature = temperature;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.humidity = humidity;
    }
    public WeatherData(String date, double minTemperature, double maxTemperature) {
        this.date = date;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    public String getLocation() {
        return location;
    }

    public String getDate() {
        return date;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public int getHumidity() {
        return humidity;
    }
}

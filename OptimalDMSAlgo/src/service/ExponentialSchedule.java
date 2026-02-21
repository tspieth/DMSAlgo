package service;

public class ExponentialSchedule implements Schedule {
    private double temperature;
    private final double alpha;
    private final double minTemp;

    public ExponentialSchedule(double startTemp, double alpha, double minTemp) {
        this.temperature = startTemp;
        this.alpha = alpha;
        this.minTemp = minTemp;
    }

    public double getTemperature(int round) {
        if (temperature < minTemp)
            return 0;
        double current = temperature;
        temperature *= alpha;
        return current;
    }

    public void reset(double startTemp) {
        this.temperature = startTemp;
    }
}

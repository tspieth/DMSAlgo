import java.io.IOException;
import java.util.List;

import io.CSVReader;
import objects.Competition;
import objects.Swimmer;
import objects.SwimmingEvent;

public class App {
    public static void main(String[] args) throws Exception {

        Competition.setBaseTimesMale(getBaseTimes(true));
        Competition.setBaseTimesFemale(getBaseTimes(false));

        System.out.println(Competition.toStringBaseTimes(true));
        System.out.println(Competition.toStringBaseTimes(false));

    }

    // IMPORTANT: the base times in the CSV file must be in the same order as the
    // events in the SwimmingEvent enum
    // ALSO: the CSV file needs to contain every event
    public static double[] getBaseTimes(boolean isMale) {
        CSVReader baseTimesReader = new CSVReader("OptimalDMSAlgo/resources/base_times.csv");

        double[] baseTimes = new double[18];

        try {
            List<String[]> lines;

            lines = baseTimesReader.readAll();

            for (SwimmingEvent event : SwimmingEvent.values()) {
                int i = event.getIndex();
                baseTimes[i] = Double.parseDouble(
                        lines.get(i + 1)[isMale ? 3 : 5]);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null; // return null if there was an error reading the file
        }

        return baseTimes;
    }

    public static List<Swimmer> getSwimmersFromCSV(String filePath) {
        CSVReader swimmersReader = new CSVReader(filePath);
        List<Swimmer> swimmers = new java.util.ArrayList<>();

        try {
            swimmersReader.readLineByLine(values -> {
                String name = values[0] + " " + values[1]; // assuming the first two columns are first name and last name
                boolean isMale = values[1].equalsIgnoreCase}
}
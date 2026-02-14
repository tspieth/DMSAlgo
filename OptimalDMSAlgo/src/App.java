import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.CSVReader;
import objects.Competition;
import objects.Swimmer;
import objects.SwimmingClub;
import objects.SwimmingEvent;

public class App {
    public static void main(String[] args) throws Exception {

        Competition.setBaseTimesMale(getBaseTimes(true));
        Competition.setBaseTimesFemale(getBaseTimes(false));

        System.out.println(Competition.toStringBaseTimes(true));
        System.out.println(Competition.toStringBaseTimes(false));

        List<Swimmer> schwimmerListe = createSchwimmer("OptimalDMSAlgo/resources/testClub.csv");

        SwimmingClub club = new SwimmingClub(schwimmerListe);
        System.out.println(club.toString());

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

    public static List<Swimmer> createSchwimmer(String filePath) {
        CSVReader reader = new CSVReader(filePath);

        List<String[]> csvData;
        List<Swimmer> schwimmerListe = new ArrayList<>();
        try {
            csvData = reader.readAll();
            Swimmer aktuellerSchwimmer = null;

            // Header überspringen (erste Zeile)
            for (int i = 1; i < csvData.size(); i++) {

                String[] row = csvData.get(i);

                // Neue Swimmer-Zeile (Name nicht leer)
                if (row[0] != null && !row[0].isEmpty()) {

                    aktuellerSchwimmer = new Swimmer(
                            row[0] + " " + row[1], // Name
                            (row[2].equals("m")), // Geschlecht
                            Integer.parseInt(row[3]) // Ausdauer
                    );

                    schwimmerListe.add(aktuellerSchwimmer);
                }

                // Strecke + Zeit hinzufügen
                if (aktuellerSchwimmer != null && row[4] != null && !row[4].isEmpty()) {
                    aktuellerSchwimmer.setTimeForEvent(row[4], row[5]);
                    aktuellerSchwimmer.setPointsForEvent(SwimmingEvent.getByDisplayName(row[4]),
                            Competition.getTimeFromString(row[5]));
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // read all lines from the CSV file

        return schwimmerListe;
    }

}
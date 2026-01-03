package gamesavefinder;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class GameSaveFinder{

    static List<Path> foundSaves = new ArrayList<>();

    static void scanFolder(Path folder, String gameName) {

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            return;
        }

        try (Stream<Path> stream = Files.list(folder)) {

            List<Path> items = stream.toList();

            for (Path p : items) {

                if (!Files.isDirectory(p)) {
                    continue;
                }

                String folderName = p.getFileName().toString().toLowerCase();

                if (folderName.contains(gameName)) {

                    try (Stream<Path> inside = Files.list(p)) {

                        List<Path> insideItems = inside.toList();

                        for (Path f : insideItems) {

                            String name = f.getFileName().toString().toLowerCase();

                            if (name.equals("saves") || name.equals("save") || name.equals("savegame")
                                    || name.equals("profiles") || name.equals("players") || name.equals("userdata")) {

                                foundSaves.add(f);
                            }

                            if (Files.isRegularFile(f)) {

                                if (name.endsWith(".json") || name.endsWith(".dat") || name.endsWith(".bin")
                                        || name.endsWith(".db") || name.endsWith(".cfg")) {

                                    foundSaves.add(p);
                                }
                            }
                        }

                    } catch (Exception e) {
                    }
                }

                scanFolder(p, gameName);
            }

        } catch (Exception e) {
        }
    }

    public static void main(String[] args)  {

        try (Scanner input = new Scanner(System.in)) {

            while (true) {

                foundSaves.clear();

                System.out.println("Enter game name (0 to exit):");
                String gameName = input.nextLine().toLowerCase();

                if (gameName.equals("0")) {
                    System.out.println("Bye!");
                    break;
                }

                Path root = Paths.get(System.getProperty("user.home"));

                Path desktop = root.resolve("Desktop");
                Path documents = root.resolve("Documents");
                Path myGames = root.resolve("Documents").resolve("My Games");
                Path savedGames = root.resolve("Saved Games");
                Path roaming = root.resolve("AppData").resolve("Roaming");
                Path local = root.resolve("AppData").resolve("Local");
                Path localLow = root.resolve("AppData").resolve("LocalLow");
                Path steam = Paths.get("C:\\Program Files (x86)\\Steam\\userdata");


                List<Path> searchRoots = List.of(desktop, documents, myGames, savedGames, roaming, local, localLow, steam);

                for (Path rootFolder : searchRoots) {
                    scanFolder(rootFolder, gameName);
                }

                if (foundSaves.isEmpty()) {
                    System.out.println("No save folders were found.");
                    System.out.println("Press ENTER to try again, or type 0 to exit.");
                    String again = input.nextLine();
                    if (again.equals("0")) {
                        System.out.println("Bye!");
                        break;
                    } else {
                        continue;
                    }
                }

                boolean running = true;

                while (running) {

                    System.out.println();
                    System.out.println("Save folders found:");

                    for (int i = 0; i < foundSaves.size(); i++) {
                        System.out.println((i + 1) + ") " + foundSaves.get(i));
                    }

                    System.out.println();
                    System.out.println("Enter the path number to open (0 to exit):");
                    int choice = input.nextInt();
                    input.nextLine();

                    if (choice == 0) {
                        System.out.println("Bye!");
                        return;
                    }

                    if (choice > 0 && choice <= foundSaves.size()) {

                        Path selected = foundSaves.get(choice - 1);

                        System.out.println("Opening:");
                        System.out.println(selected);

                        try {
                            Desktop.getDesktop().open(selected.toFile());
                        } catch (Exception e) {
                            System.out.println("Could not open folder!");
                        }

                        return;

                    } else {
                        System.out.println("Invalid choice, try again!");
                    }
                }
            }
        }
    }
}

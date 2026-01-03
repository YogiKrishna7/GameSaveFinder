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

	static List<String> saveExtensions = List.of(
		    ".sav", ".sav1", ".sav2",
		    ".dat", ".bin", ".db", ".json",
		    ".ess", ".fos", ".age", ".b",
		    ".srm", ".state",
		    ".profile", ".player",
		    ".autosave", ".quick"
		);
	
	static List<String> saveFolderNames = List.of(
		    "saves", "save", "savegame",
		    "profiles", "players", "userdata",
		    "savedata", "dbe_production",
		    "user files"
		);

    static List<Path> foundSaves = new ArrayList<>();
    
    static String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    static void scanFolder(Path folder, String gameName, boolean insideGame) {

        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            return;
        }

        try (Stream<Path> stream = Files.list(folder)) {

            List<Path> items = stream.toList();

            for (Path p : items) {

                if (!Files.isDirectory(p)) {
                    continue;
                }

                String folderName = normalize(p.getFileName().toString());
                String normalizedGame = normalize(gameName);

                boolean nowInsideGame = insideGame || folderName.contains(normalizedGame);


                if (nowInsideGame) {

                    try (Stream<Path> inside = Files.list(p)) {

                        List<Path> insideItems = inside.toList();

                        for (Path f : insideItems) {

                            String name = f.getFileName().toString().toLowerCase();

                            for (String fName : saveFolderNames) {
                                if (name.equals(fName)) {
                                    if (!foundSaves.contains(f)) {
                                        foundSaves.add(f);
                                    }
                                }
                            }

                            if (Files.isRegularFile(f)) {
                                for (String ext : saveExtensions) {
                                    if (name.endsWith(ext)) {
                                        if (!foundSaves.contains(p)) {
                                            foundSaves.add(p);
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                    }
                }

                scanFolder(p, gameName, nowInsideGame);
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

                Path documents = root.resolve("Documents");
                List<Path> documentsSubFolders = new ArrayList<>();

                try (Stream<Path> docs = Files.list(documents)) {
                    for (Path p : docs.toList()) {
                        if (Files.isDirectory(p)) {
                            documentsSubFolders.add(p);
                        }
                    }
                } catch (Exception e) {
                }

                Path myGames = root.resolve("Documents").resolve("My Games");
                Path savedGames = root.resolve("Saved Games");
                Path roaming = root.resolve("AppData").resolve("Roaming");
                Path local = root.resolve("AppData").resolve("Local");
                Path localLow = root.resolve("AppData").resolve("LocalLow");
                Path steam = Paths.get("C:\\Program Files (x86)\\Steam\\userdata");


                List<Path> searchRoots = new ArrayList<>();

                searchRoots.add(documents);
                searchRoots.addAll(documentsSubFolders);
                searchRoots.add(myGames);
                searchRoots.add(savedGames);
                searchRoots.add(roaming);
                searchRoots.add(local);
                searchRoots.add(localLow);
                searchRoots.add(steam);

                for (Path rootFolder : searchRoots) {
                    scanFolder(rootFolder, gameName, false);
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

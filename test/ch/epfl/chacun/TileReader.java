package ch.epfl.chacun;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TileReader {

    private static final String CSV_FILE_PATH = "test/ch/epfl/chacun/tuiles.csv"; // Chemin vers le fichier CSV contenant les tuiles

    public static Tile readTileFromCSV(int tileId) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH));
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int id = Integer.parseInt(parts[0]);
                if (id == tileId) {
                    return parseTile(parts);
                }
            }
            return null; // Retourne null si aucune tuile correspondante n'est trouvée

        } catch (IOException e) {
            System.out.println("An error occurred.");
            throw new IllegalArgumentException("Error while reading the file");
        }

    }

    private static Tile parseTile(String[] data) {
        int id = Integer.parseInt(data[0]);
        String type = data[data.length - 1].trim().toUpperCase(); // Convertir en majuscules et supprimer les espaces

        Tile.Kind kind = switch (type) {
            case "NORMALE" -> Tile.Kind.NORMAL;
            case "MENHIR" -> Tile.Kind.MENHIR;
            case "DÉPART" -> Tile.Kind.START;
            default -> null;
        };

        TileSide n = parseTileSide(data, 1 , id);
        TileSide e = parseTileSide(data, 2, id);
        TileSide s = parseTileSide(data, 3, id);
        TileSide w = parseTileSide(data, 4, id);

        return new Tile(id, kind, n, e, s, w);
    }


    private static TileSide parseTileSide(String[] Data, int side, int tileId) {
        char type = Data[side].charAt(0);
        int localId = Character.getNumericValue(Data[side].charAt(1));
        return switch (type) {
            case 'M' -> new TileSide.Meadow(parseMeadow(Data[localId + 5], localId,tileId));
            case 'F' -> new TileSide.Forest(parseForest(Data[localId + 5], localId,tileId));
            case 'R' -> new TileSide.River(
                    parseMeadow(Data[localId + 5], localId, tileId),
                    parseRiver(Data[Character.getNumericValue(Data[side].charAt(2)) + 5], Data[13], Data[14], Character.getNumericValue(Data[side].charAt(2)) ,tileId),
                    parseMeadow(Data[Character.getNumericValue(Data[side].charAt(3)) + 5], Character.getNumericValue(Data[side].charAt(3)),tileId));
            default -> (null);
        };
    }

    private static Zone.Meadow parseMeadow(String data, int zoneId, int tileId) {
        int id = tileId * 10 + zoneId;
        List<Animal> animals = new ArrayList<>();
        Zone.SpecialPower specialPower = null;
        for (int i = 1; i < data.length(); i++) {
            char animalChar = data.charAt(i);
            switch (animalChar) {
                case 'a':
                    animals.add(new Animal(id * 10 + animals.size(), Animal.Kind.AUROCHS));
                    break;
                case 'd':
                    animals.add(new Animal(id * 10 + animals.size(), Animal.Kind.DEER));
                    break;
                case 'm':
                    animals.add(new Animal(id * 10 + animals.size(), Animal.Kind.MAMMOTH));
                    break;
                case 't':
                    animals.add(new Animal(id * 10 + animals.size(), Animal.Kind.TIGER));
                    break;
                case 'H':
                    specialPower = Zone.SpecialPower.HUNTING_TRAP;
                    break;
                case 'P':
                    specialPower = Zone.SpecialPower.PIT_TRAP;
                    break;
                case 'S':
                    specialPower = Zone.SpecialPower.SHAMAN;
                    break;
                case 'W':
                    specialPower = Zone.SpecialPower.WILD_FIRE;
                    break;
            }
        }
        return new Zone.Meadow(id, animals, specialPower);
    }

    private static Zone.Forest parseForest(String data, int zoneId, int tileId) {
        int id = tileId * 10 + zoneId;
        if (data.length() == 1)
            return new Zone.Forest(id, Zone.Forest.Kind.PLAIN);
        else {
            if (data.charAt(1) == 'm')
                return new Zone.Forest(id, Zone.Forest.Kind.WITH_MENHIR);
            else
                return new Zone.Forest(id, Zone.Forest.Kind.WITH_MUSHROOMS);
        }
    }

    private static Zone.River parseRiver(String zoneData, String lakeData1, String lakeData2, int zoneID, int tileId) {
        int id = tileId * 10 + zoneID;
        int fishCount = 0;
        int lakeFishCount = 0;
        Zone.SpecialPower lakeSpecialPower = null;
        Zone.Lake lake = null;
        int lakeConnected = 0;
        for (int i = 1; i < zoneData.length(); i++) {
            char c = zoneData.charAt(i);
            switch (c) {
                case 'f':
                    fishCount++;
                    break;
                case '1':
                    lakeConnected = 1;
                    break;
                case '2':
                    lakeConnected = 2;
                    break;
            }
        }
        if (lakeConnected == 1) {
            for (int i = 1; i < lakeData1.length(); i++) {
                char c = lakeData1.charAt(i);
                switch (c) {
                    case 'f':
                        lakeFishCount++;
                        break;
                    case 'R':
                        lakeSpecialPower = Zone.SpecialPower.RAFT;
                        break;
                    case 'B':
                        lakeSpecialPower = Zone.SpecialPower.LOGBOAT;
                        break;
                }
            }
            lake = new Zone.Lake(tileId * 10 + 8, lakeFishCount, lakeSpecialPower);
        }
        if (lakeConnected == 2) {
            for (int i = 1; i < lakeData2.length(); i++) {
                char c = lakeData2.charAt(i);
                switch (c) {
                    case 'f':
                        lakeFishCount++;
                        break;
                    case 'R':
                        lakeSpecialPower = Zone.SpecialPower.RAFT;
                        break;
                    case 'B':
                        lakeSpecialPower = Zone.SpecialPower.LOGBOAT;
                        break;
                }
            }
            lake = new Zone.Lake(tileId * 10 + 9, lakeFishCount, lakeSpecialPower);
        }
        return new Zone.River(id, fishCount, lake);
    }

    public static void main(String[] args) throws IOException {

            int tileId = 0; // Prenez l'ID de la tuile à partir des arguments de la ligne de commande
            Tile tile = readTileFromCSV(tileId);
            if (tile != null) {
                System.out.println("Tile found: " + tile);
            } else {
                System.out.println("Tile with ID " + tileId + " not found.");
            }
    }
}

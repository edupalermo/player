package org.palermo.totalbattle.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import org.palermo.totalbattle.util.bean.Army;
import org.palermo.totalbattle.util.bean.Cache;
import org.palermo.totalbattle.util.bean.State;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SheetUtil {

    private static final String[] PATHS = {
            "/home/palermo/workspace/tokens/google_credentials.json", "/home/eduardo/tokens/credentials.json"
    };

    private static final Sheets service = buildSheetsService();
    private static final String SPREAD_SHEET_ID = "1egdLR8A1-hXZDr0xssNb3-UZx8iwJHr9xqj1KaV6Ibo";

    public static final String CONF_MODE = "MODE";

    private static String playerName;
    private static State state;
    private static Army army;

    public static void main(String[] args) {
        System.out.println(getState("Palermo"));
        System.out.println(getArmy("Lovern"));
    }

    private static final Cache cache = new Cache();

    public static Optional<State> getState(String playerName) {
        State state = cache.get("state_" + playerName, State.class, () -> internalGetState(playerName).orElse(null));
        return Optional.ofNullable(state);
    }

    public static Optional<Army> getArmy(String playerName) {
        Army army = cache.get("army_" + playerName, Army.class, () -> internalGetArmy(playerName).orElse(null));
        return Optional.ofNullable(army);
    }

    public static <T> T getConfiguration(String name, Class<T> clazz) {
        Object object = cache.get("conf_" + name, Object.class, () -> internalGetConfiguration(name, clazz));
        return clazz.cast(object);
    }

    @SneakyThrows
    public static Optional<State> internalGetState(String playerName) {
        
        if (playerName.equals(SheetUtil.playerName) && SheetUtil.state != null) {
            return Optional.of(SheetUtil.state);
        }
        
        ValueRange valueRange = service.spreadsheets().values()
                .get(SPREAD_SHEET_ID, "State")
                .execute();

        java.util.List<java.util.List<Object>> rows = valueRange.getValues();

        int column = getPlayerColumn(rows.get(0), playerName);
        State.StateBuilder stateBuilder = State.builder();
        
        for (int i = 1; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            switch(row.get(0).toString()) {
                case "DEFAULT_LEADERSHIP":
                    stateBuilder.defaultLeadership(toInt(row.get(column)));
                    break;
                case "CITADEL":
                    stateBuilder.citadel(toInt(row.get(column)));
                    break;
                case "CRYPT":
                    stateBuilder.crypt(toInt(row.get(column)));
                    break;
                case "G_MELEE":
                    stateBuilder.gMeleeLevel(toInt(row.get(column)));
                    break;
                case "G_RANGED":
                    stateBuilder.gRangedLevel(toInt(row.get(column)));
                    break;
                case "G_MOUNTED":
                    stateBuilder.gMountedLevel(toInt(row.get(column)));
                    break;
                case "G_FLYING":
                    stateBuilder.gFlyingLevel(toInt(row.get(column)));
                    break;
                case "SPY":
                    stateBuilder.spyLevel(toInt(row.get(column)));
                    break;
                case "S_MELEE":
                    stateBuilder.sMeleeLevel(toInt(row.get(column)));
                    break;
                case "S_RANGED":
                    stateBuilder.sRangedLevel(toInt(row.get(column)));
                    break;
                case "S_MOUNTED":
                    stateBuilder.sMountedLevel(toInt(row.get(column)));
                    break;
                case "S_FLYING":
                    stateBuilder.sFlyingLevel(toInt(row.get(column)));
                    break;
                case "MONSTER":
                    stateBuilder.monsterLevel(toInt(row.get(column)));
                    break;
                case "EC":
                    stateBuilder.ecLevel(toInt(row.get(column)));
                    break;
                case "MERC":
                    stateBuilder.mercLevel(toInt(row.get(column)));
                    break;
                case "BUILDING":
                    if (column < row.size()) {
                        stateBuilder.building(row.get(column).toString());
                    }
                    break;
            }
            
        }
        return Optional.of(stateBuilder.build());
    }
    
    private static int toInt(Object object) {
        String asString = object.toString().replace(",", "");
        return Integer.parseInt(asString);
    }

    @SneakyThrows
    public static Optional<Army> internalGetArmy(String playerName) {
        if (playerName.equals(SheetUtil.playerName) && SheetUtil.army != null) {
            return Optional.of(SheetUtil.army);
        }

        ValueRange valueRange = service.spreadsheets().values()
                .get(SPREAD_SHEET_ID, "Army")
                .execute();

        java.util.List<java.util.List<Object>> rows = valueRange.getValues();

        int rowIndex = getPlayerRow(rows, playerName);
        
        if (rowIndex == -1) {
            return Optional.empty();
        }

        java.util.List<Object> row = rows.get(rowIndex);
        
        return Optional.of(Army.builder()
                .leadership(toInt(row.get(2)))
                .dominance(toInt(row.get(3)))
                .waves(toInt(row.get(4)))
                .build());
    }

    @SneakyThrows
    public static <T> T internalGetConfiguration(String name, Class<T> clazz) {
        ValueRange valueRange = service.spreadsheets().values()
                .get(SPREAD_SHEET_ID, "Configuration")
                .execute();

        java.util.List<java.util.List<Object>> rows = valueRange.getValues();

        int rowIndex = getConfigurationRow(rows, name);

        if (rowIndex == -1) {
            throw new RuntimeException("Configuration not found! " + name);
        }

        java.util.List<Object> row = rows.get(rowIndex);

        return convert(row.get(1).toString(), clazz);
    }

    private static <T> T convert(String value, Class<T> clazz) {
        if (clazz.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            T result = (T) Enum.valueOf((Class<? extends Enum>) clazz, value);
            return result;
        }

        if (clazz == String.class) {
            return clazz.cast(value);
        }

        if (clazz == Integer.class) {
            return clazz.cast(Integer.valueOf(value));
        }

        if (clazz == Long.class) {
            return clazz.cast(Long.valueOf(value));
        }

        if (clazz == Boolean.class) {
            return clazz.cast(Boolean.valueOf(value));
        }

        if (clazz == Double.class) {
            return clazz.cast(Double.valueOf(value));
        }

        throw new IllegalArgumentException(
                "Unsupported configuration type: " + clazz.getName()
        );
    }

    private static int getPlayerColumn(List<Object> row, String playerName) {
        for (int i = 1; i < row.size(); i++) {
            if (playerName.equals(row.get(i).toString())) {
                return i;
            }
        }
        return -1;
    }

    private static int getPlayerRow(java.util.List<java.util.List<Object>> rows, String playerName) {
        for (int i = 1; i < rows.size(); i++) {
            java.util.List<Object> row = rows.get(i);
            if (playerName.equals(row.get(0).toString()) && "ACTIVE".equals(row.get(1).toString())) {
                return i;
            }
        }
        return -1;
    }

    private static int getConfigurationRow(java.util.List<java.util.List<Object>> rows, String name) {
        for (int i = 1; i < rows.size(); i++) {
            java.util.List<Object> row = rows.get(i);
            if (name.equals(row.get(0).toString())) {
                return i;
            }
        }
        return -1;
    }

    public static String findExistingCredentialFile() {
        return Arrays.stream(PATHS)
                .filter(path -> Files.exists(Path.of(path)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Cannot find a google credential to access google sheet!"));
    }
    
    private static Sheets buildSheetsService() {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(findExistingCredentialFile()))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            return new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Troop Manager")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}

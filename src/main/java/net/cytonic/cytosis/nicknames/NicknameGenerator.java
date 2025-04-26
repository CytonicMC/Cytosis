package net.cytonic.cytosis.nicknames;

import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.Tuple;

import java.util.Map;
import java.util.Random;

public class NicknameGenerator {
    private static final String[] ADJECTIVES = {
            "Swift", "Dark", "Epic", "Sneaky", "Frosty", "Lucky", "Shadow", "Cursed", "Blazing"
    };

    private static final String[] NOUNS = {
            "Fox", "Wolf", "Miner", "Blade", "Ender", "Knight", "Slayer", "Dragon", "Storm"
    };

    private static final String[] SUFFIXES = {
            "", "_YT", "_MC", "_xX", "xX_", "_TV"
    };

    private static final Map<Character, String> LEET_MAP = Map.of(
            'a', "4", 'e', "3", 'i', "1", 'o', "0", 's', "5", 't', "7"
    );

    private static final Tuple<String, String>[] SKIN_DATA = SkinParser.parseSkinData();

    private static final Random RANDOM = new Random();

    public static String generateUsername() {
        String adj = pickRandom(ADJECTIVES);
        String noun = pickRandom(NOUNS);

        boolean useUnderscore = RANDOM.nextBoolean();
        boolean useNumber = RANDOM.nextBoolean();
        boolean useLeet = RANDOM.nextBoolean();
        boolean useWeirdCase = RANDOM.nextDouble() < 0.2;

        if (useLeet) {
            adj = applyLeetspeak(adj);
            noun = applyLeetspeak(noun);
        }
        if (useWeirdCase) {
            adj = applyRandomCasing(adj);
            noun = applyRandomCasing(noun);
        }

        String base = useUnderscore ? adj + "_" + noun : adj + noun;
        String number = useNumber ? String.valueOf(RANDOM.nextInt(10000)) : "";
        String suffix = pickRandom(SUFFIXES);

        // Try full combo
        String full = base + number + suffix;
        if (full.length() <= 16) return full;

        // Try without suffix
        full = base + number;
        if (full.length() <= 16) return full;

        // Try without number
        full = base + suffix;
        if (full.length() <= 16) return full;

        // Just base
        if (base.length() <= 16) return base;

        // Last resort: trim base to fit
        return base.substring(0, 16);
    }

    private static <T> T pickRandom(T[] arr) {
        return arr[RANDOM.nextInt(arr.length)];
    }

    private static String applyLeetspeak(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (LEET_MAP.containsKey(Character.toLowerCase(c)) && RANDOM.nextDouble() < 0.3) {
                sb.append(LEET_MAP.get(Character.toLowerCase(c)));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String applyRandomCasing(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(RANDOM.nextDouble() < 0.15 ? Character.toUpperCase(c) : Character.toLowerCase(c));
        }
        return sb.toString();
    }

    //todo: Procedural skin generation :)

    /**
     * Picks a random skin to use.
     *
     * @return A tuple containing the skin data. Signature, then value
     */
    public static Tuple<String, String> generateSkin() {
        return pickRandom(SKIN_DATA);
    }

    public static PlayerRank generateRank() {
        return PlayerRank.values()[RANDOM.nextInt(PlayerRank.values().length - 5) + 5]; // No staff ranks or elysian
    }

    public static NicknameManager.NicknameData generateNicknameData() {
        return new NicknameManager.NicknameData(generateUsername(), generateRank(), generateSkin());
    }
}

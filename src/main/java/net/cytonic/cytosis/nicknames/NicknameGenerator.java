package net.cytonic.cytosis.nicknames;

import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.data.objects.Tuple;

import java.util.Map;
import java.util.Random;

public class NicknameGenerator {
    private static final String[] ADJECTIVES = {
            "Swift", "Bold", "Bright", "Calm", "Gentle", "Quick", "Strong", "Clever", "Friendly", "Loyal", "Vivid", "Happy",
            "Radiant", "Charming", "Funky", "Noble", "Lucky", "Graceful", "Steady", "Serene", "Energetic", "Warm", "Quiet",
            "Caring", "Cunning", "Luminous", "Cheerful", "Shiny", "Sleek", "Dashing", "Timid", "Jolly", "Subtle", "Vibrant",
            "Eternal", "Mellow", "Mystic", "Shimmering", "Harmonious", "Blissful", "Adventurous", "Dynamic", "Blazing",
            "Effortless", "Proud", "Majestic", "Lively", "Stealthy", "Zesty", "Eager", "Serene", "Dynamic", "Smooth",
            "Mysterious", "Gleaming", "Radiating", "Playful", "Sturdy", "Vast", "Hasty", "Quiet", "Sly", "Brilliant",
            "Elegant", "Breezy", "Tidy", "Proud", "Vivid", "Intense", "Dreamy", "Mysterious", "Flexible", "Gentle", "Shy",
            "Hasty", "Effervescent", "Frosted", "Warm", "Jumpy", "Graceful", "Sharp", "Vivid", "Dazzling", "Jovial", "Jumpy",
            "Charming", "Eager", "Vigorous", "Bold", "Sharp", "Friendly", "Stormy", "Steady", "Proud", "Lustrous", "Harmonized",
            "Vivid", "Radiant", "Dynamic", "Frosted", "Serene", "Chilly", "Light", "Luminous", "Whimsical", "Smooth", "Majestic",
            "Hazy", "Determined", "Balanced", "Witty", "Spectral", "Regal", "Velvety", "Frosted", "Epic", "Vibrant", "Cool",
            "Thriving", "Surprising", "Soft", "Mystical", "Radiant", "Daring", "Energetic", "Hidden", "Jovial", "Breezy", "Jumpy",
            "Brave", "Chilly", "Glistening", "Luminous", "Dazzling", "Bright", "Gleaming", "Snappy", "Lively", "Mild", "Fierce",
            "Trendy", "Endless", "Silent", "Shiny", "Poised", "Magical", "Firey", "Glowing", "Cheerful", "Whispering", "Optimistic",
            "Funky", "Light", "Dreamlike", "Lustrous", "Electric", "Intense", "Confident", "Fresh", "Sleek", "Witty", "Tense"
    };

    private static final String[] NOUNS = {
            "Sky", "Cloud", "Wave", "Storm", "River", "Forest", "Mountain", "Breeze", "Sun", "Moon", "Star", "Ocean",
            "Wind", "Rain", "Leaf", "Rock", "Stone", "Shadow", "Dream", "Song", "Fire", "Light", "Flame", "Time", "Heart",
            "Soul", "Voice", "Journey", "Path", "Bridge", "Wave", "Storm", "Mountain", "Valley", "Horizon", "Moonlight",
            "Sunrise", "Echo", "Rhythm", "Idea", "Thought", "Energy", "Focus", "Quest", "Flow", "Pulse", "Vibe", "Mind",
            "Whisper", "Clarity", "Glance", "Leap", "Speed", "Moment", "Glide", "Step", "Leap", "Echo", "Mark", "Light",
            "Fire", "Chime", "Echo", "Path", "Bloom", "Tide", "Element", "Core", "Tone", "Shade", "Wave", "Jolt", "Force",
            "Signal", "Stream", "Flow", "Flicker", "Glimpse", "Flare", "Blaze", "Ray", "Spark", "Pulse", "Rush", "Flash",
            "Vibe", "Breeze", "Flicker", "Whirl", "Trail", "Blossom", "Stone", "Mind", "Peak", "Dream", "Whistle", "Radiance",
            "Phoenix", "Knight", "Knightmare", "Gladiator", "Champion", "Assassin", "Warrior", "Vanguard", "Tactician",
            "Scribe", "Scholar", "Seeker", "Vampire", "Zombie", "Ghost", "Witch", "Shaman", "Mage", "Magician", "King", "Queen",
            "Priest", "Pirate", "Bard", "Jester", "Guardian", "Sleuth", "Enchanter", "Valkyrie", "Dragon", "Tiger", "Lion",
            "Bear", "Eagle", "Shark", "Shaman", "Druid", "Vampire", "Fox", "Raven", "Hawk", "Falcon", "Wolf", "Reaper",
            "Ranger", "Outlaw", "Minotaur", "Titan", "Empress", "Duke", "Baron", "Warlord", "Rebel", "Crusader", "Giant",
            "Lumberjack", "Vagabond", "Thief", "Marauder", "Outlaw", "Bandit", "Rider", "Tamer", "Hunter", "Runner", "Grim",
            "Priestess", "Shifter", "Gladiator", "Mercenary", "Crusader", "Sage", "Fury", "Mender", "Champion", "Witch",
            "Seeker", "Sentinel", "Scribe", "Sleuth", "Viper", "Swordsman", "Sniper", "Tactician", "Sorcerer", "Witcher",
            "Necromancer", "Raider", "Mystic", "Shifter", "Alchemist", "Assailant", "Specter", "Thrasher", "Chronicler",
            "Outlaw", "Revenant", "Vigilante", "Sorceress", "Warden", "Nomad", "Enforcer", "Emissary", "Ravager", "Sentry",
            "Exile", "Barbarian", "Squire", "Brute", "Swordsman", "Marauder", "Bounty", "Chronicler", "Spy", "Witch", "Warden",
            "Phantom", "Musketeer", "Jester", "Vanguard", "Defender", "Ranger", "Outlaw", "Heretic", "Knave", "King", "Queen",
            "Empress", "Hero", "Mystic", "Warlord", "Witch", "Bandit", "Sleuth", "Pioneer", "Raider", "Vanguard", "Savior"
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
        String adj1 = pickRandom(ADJECTIVES);
        String adj2 = RANDOM.nextDouble() < 0.3 ? pickRandom(ADJECTIVES) : "";
        String noun = pickRandom(NOUNS);

        boolean useUnderscore = RANDOM.nextDouble() < 0.4;
        boolean useNumber = RANDOM.nextDouble() < 0.5;
        boolean useLeet = RANDOM.nextDouble() < 0.15;
        boolean useWeirdCase = RANDOM.nextDouble() < 0.1;
        boolean dropVowel = RANDOM.nextDouble() < 0.2;
        boolean doubleLetter = RANDOM.nextDouble() < 0.2;

        String base = adj1 + (adj2.isEmpty() ? "" : adj2) + noun;

        if (dropVowel) base = removeRandomVowel(base);
        if (doubleLetter) base = doubleRandomLetter(base);
        if (useLeet) base = applyLeetspeak(base);
        if (useWeirdCase) base = applyRandomCasing(base);

        if (useUnderscore && RANDOM.nextBoolean()) base = base.replaceFirst("(?<=[a-zA-Z])(?=[A-Z])", "_");

        String number = useNumber ? String.valueOf(RANDOM.nextInt(10000)) : "";
        String suffix = RANDOM.nextDouble() < 0.2 ? pickRandom(SUFFIXES) : "";

        String full = base + number + suffix;
        if (full.length() <= 16) return full;
        if ((full = base + number).length() <= 16) return full;
        if ((full = base + suffix).length() <= 16) return full;
        if (base.length() <= 16) return base;

        return base.substring(0, 16);
    }

    private static String removeRandomVowel(String str) {
        String vowels = "aeiouAEIOU";
        StringBuilder sb = new StringBuilder();
        boolean removed = false;
        for (char c : str.toCharArray()) {
            if (!removed && vowels.indexOf(c) != -1 && RANDOM.nextDouble() < 0.4) {
                removed = true;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static String doubleRandomLetter(String str) {
        if (str.isEmpty()) return str;
        int idx = RANDOM.nextInt(str.length());
        char c = str.charAt(idx);
        return str.substring(0, idx) + c + c + str.substring(idx);
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

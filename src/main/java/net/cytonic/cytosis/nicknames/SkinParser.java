package net.cytonic.cytosis.nicknames;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.cytonic.cytosis.data.objects.Tuple;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class SkinParser {

    @SuppressWarnings("unchecked")
    public static Tuple<String, String>[] parseSkinData() {
        InputStream inputStream = SkinParser.class.getResourceAsStream("skins.json");
        InputStreamReader reader = new InputStreamReader(inputStream);

        Gson gson = new Gson();
        List<Skin> skins = gson.fromJson(reader, new TypeToken<List<Skin>>() {
        }.getType());

        return skins.stream().map(skin -> Tuple.of(skin.signature, skin.value)).toArray(Tuple[]::new);
    }

    static class Skin {
        String signature;
        String value;
    }
}

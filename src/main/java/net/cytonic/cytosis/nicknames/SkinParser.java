package net.cytonic.cytosis.nicknames;

import com.google.gson.Gson;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.Tuple;
import net.cytonic.cytosis.files.FileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import com.google.gson.Gson;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.objects.Tuple;

public class SkinParser {

    @SuppressWarnings("unchecked")
    public static Tuple<String, String>[] parseSkinData() {
        File file = Cytosis.CONTEXT.getComponent(FileManager.class).extractResource("skins.json", Path.of("skins.json"));
        InputStreamReader reader = null;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        Gson gson = Cytosis.GSON;
        List<Skin> skins = gson.fromJson(reader, SkinFile.class).skins;

        return skins.stream().map(skin -> Tuple.of(skin.signature, skin.value)).toArray(Tuple[]::new);
    }

    static class SkinFile {

        List<Skin> skins;
    }

    static class Skin {

        String signature;
        String value;
    }
}
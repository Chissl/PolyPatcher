package club.sk1er.patcher.database;

import club.sk1er.patcher.hooks.FallbackResourceManagerHook;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AssetsDatabase {

    private File dir;

    public AssetsDatabase() {
        File minecraftHome = Launch.minecraftHome;
        if (minecraftHome == null) minecraftHome = new File(".");
        dir = new File(minecraftHome, "patcher");
        dir.mkdir();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                saveNegative(FallbackResourceManagerHook.negativeResourceCache);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }));
    }


    public List<String> getAllNegative() throws IOException {
        File file = new File(dir, "negative_cache.txt");
        if (file.exists())
            return FileUtils.readLines(file, Charset.defaultCharset());
        return new ArrayList<>();
    }

    public void saveNegative(Set<String> lines) throws IOException {
        FileUtils.writeLines(new File(dir, "negative_cache.txt"), lines);
    }

}
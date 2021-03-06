package shadon.technologies.app.craigslistdiffchecker.files;

import android.os.Environment;

import java.io.File;

/**
 * Created by Monday on 7/31/2016.
 */
public class Paths {
    public static final String baseFolder = Paths.build(Environment.getExternalStorageDirectory().getAbsolutePath(), "CraigslistChecker");
    public static final String dataFolderLocation = Paths.build(baseFolder, "data");
    public static final String saveSearchesPath = Paths.build(dataFolderLocation, "savedSearches.txt");
    public static final String cachedSearchesFileLocation = Paths.build(dataFolderLocation, "cachedURLs.txt");

    public static String build(String... pieces) {
        StringBuilder builder = new StringBuilder();
        for (String piece : pieces) {
            if (builder.length() > 0) {
                builder.append(File.separator);
            }
            builder.append(piece);
        }
        return builder.toString();
    }
}

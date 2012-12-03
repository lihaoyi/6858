package sandbox.lists;


import sandbox.rules.Rule;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WhiteList {
    /**
     * Checks whether the name of a class is allowed by seeing if it has a prefix
     * which exists in the private blacklist.
     */
    public static boolean allow(String s) {
        s = s.replace('/', '.');
        return !list.validate(s);
    }

    private static Rule list;

    static {
        Path p = Paths.get("resources/list.txt");
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            list = new Rule("-", lines);
        } catch (Exception e) {
        }
    }

}

package sandbox.rules;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Rule {
    private String prefix;
    private Set<Rule> children = new HashSet<>();

    public Rule(String prefix, List<String> src) {
        this.prefix = prefix;

        if (src.size() > 0) {
            int start = 0;
            for (int i = 1; i < src.size(); i++) {
                if (src.get(start).lastIndexOf(" ") < src.get(i).lastIndexOf(" ")) {
                    continue;
                } else {
                    children.add(new Rule(src.get(start).trim(), src.subList(start + 1, i)));
                    start = i;
                }
            }
            children.add(new Rule(src.get(start).trim(), src.subList(start + 1, src.size())));
        }
    }

    public boolean validate(String input) {
        if (input.startsWith(prefix.substring(1))) {

            String suffix = input.substring(prefix.length() - 1);
            for (Rule child : children) {
                if (child.validate(suffix)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return toString(0);
    }

    public String toString(int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + "    ";
        }
        s = s + prefix;
        for (Rule child : children) {
            s = s + "\n" + child.toString(n + 1);
        }
        return s;
    }
}

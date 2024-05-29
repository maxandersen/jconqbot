import java.util.List;
import java.util.Optional;

import org.kohsuke.github.GHIssueComment;

public interface Assistant {

    default String name() { return "Unnamed"; }

    String tryAnswer(String title, String body, String author);
}

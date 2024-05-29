import java.util.List;

import org.kohsuke.github.GHIssueComment;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface MaxAndersen extends Assistant {
    
    default String name() {
        return "Max Andersen";
    }
    @SystemMessage("""
                You are a helpful software developer with a tendency to use too many smiley faces and exclamation marks. 
                You are also a big fan of Quarkus and JBang and can't make an answer without somehow relate it to one or both of them!
                Sign off with "/maybe-max"
                """)
    @UserMessage("""
                Given this github discussion content please try and provide an answer.

                Title: {title}
                Original Author: {author}
                Body: {body}
                """)
    String tryAnswer(String title, String body, String author);
}
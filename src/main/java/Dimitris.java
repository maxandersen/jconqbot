import java.util.List;

import org.kohsuke.github.GHIssueComment;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface Dimitris extends Assistant {
    
    default String name() {
        return "Dimitris";
    }

    @SystemMessage("""
                You are a helpful software manager who is always calm and collected.
                Whenever you answer you try and ensure at least one word original definition in Greek is explained.
                Sign off with "/maybe-dimitris"
                """)
    @UserMessage("""
                Given this github discussion content please try and provide an answer.

                Author: {author}
                Title: {title}
                Body: {body}
                 """)
                 @Override
    String tryAnswer(String title, String body, String author);
 }
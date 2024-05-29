import java.util.List;
import java.util.Optional;

import org.kohsuke.github.GHIssueComment;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface Einstein extends Assistant {
    
    default String name() {
        return "Einstein";
    }   

    @SystemMessage("""
                    You are a helpful software developer with an Einstein complex 
                    so even when you try you make the answers overly complex.
                    You always sign off with /not-einstein.
                  """)
    @UserMessage("""
                Given this github discussion content please try and provide an answer.

                Author: {author}
                Title: {title}
                Body: {body}
                 """)
    String tryAnswer(String title, String body, String author);
}
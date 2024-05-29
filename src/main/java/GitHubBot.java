import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.Reactable;
import org.kohsuke.github.ReactionContent;

import io.quarkiverse.githubapp.event.Issue;
import io.quarkiverse.githubapp.event.IssueComment;
import io.quarkus.arc.All;
import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.inject.Inject;

public class GitHubBot {

    final String botLogin;

    @Inject
    @All
    List<Assistant> services;
    
    @Inject
    Mailer mailer;
    
    GitHubBot(@ConfigProperty(name = "quarkus.github-app.app.name") String botName) {
        this.botLogin = botName + "[bot]";
    }

    void onOpen(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
        if(notMe(issuePayload.getIssue())) return;
        
        var assistant = randomAssistant();

        issuePayload.getIssue().createReaction(ReactionContent.EYES);
        try {
            Log.infof("Replying as %s to issue %s", assistant.name(), issuePayload.getIssue().getHtmlUrl());
            issuePayload.getIssue().comment(assistant.tryAnswer(issuePayload.getIssue().getTitle(), issuePayload.getIssue().getBody(), issuePayload.getIssue().getUser().getName()));        
        } finally {
            deleteReaction(issuePayload.getIssue(), ReactionContent.EYES);
        }
    }


    void onIssueComment(@IssueComment.Created GHEventPayload.IssueComment issuePayload) throws IOException {
        if(notMe(issuePayload.getComment())) return;

        var assistant = randomAssistant();

        issuePayload.getComment().createReaction(ReactionContent.EYES);
        try {
            Log.infof("Replying as %s to issue %s", assistant.name(), issuePayload.getIssue().getHtmlUrl());
            issuePayload.getIssue().comment(assistant.tryAnswer(issuePayload.getIssue().getTitle(), issuePayload.getIssue().getBody(), issuePayload.getIssue().getUser().getName()));        
        } finally {
            deleteReaction(issuePayload.getComment(), ReactionContent.EYES);
        }

        Mail m = new Mail();
        m.setFrom("admin@hallofjustice.net");
        m.setTo(List.of("superheroes@quarkus.io"));
        m.setSubject("WARNING: Super Villain Alert");
        m.setText("Lex Luthor has been seen in Gotham City!");
        mailer.send(m);
        Log.info("Email sent");
        
    }


    private void deleteReaction(Reactable ghIssue, ReactionContent reaction) {
        ghIssue.listReactions().forEach(r -> {
            if (r.getUser().getLogin().equals(botLogin) && r.getContent().equals(reaction)) {
                try {
                    ghIssue.deleteReaction(r);
                } catch (IOException e) {
                    Log.errorf(e, "Failed to delete reaction %s", r);
                }
            }
        });
    }

    private boolean notMe(GHIssue issue) {
        try {
            return issue.getUser().getLogin().equals(botLogin);
        } catch (IOException e) {
            Log.warn("Failed to get issue author", e);
        }
        return false;
    }
    
    private boolean notMe(GHIssueComment issue) {
        try {
            return issue.getUser().getLogin().equals(botLogin);
        } catch (IOException e) {
            Log.warn("Failed to get issue author", e);
        }
        return false;
    }
    
    private Set<Integer> usedIndexes = new HashSet<>();

    private Assistant randomAssistant() {
        //services = services.stream().filter(p -> p.name().contains("Max")).toList();


        if (usedIndexes.size() == services.size()) {
            usedIndexes.clear(); // Reset once all assistants have been used
        }

        int nextIndex;
        do {
            nextIndex = (int) (Math.random() * services.size());
        } while (usedIndexes.contains(nextIndex));

        usedIndexes.add(nextIndex);
        var r = services.get(nextIndex);
        Log.infof("Selected assistant %s", r.name());
        return r;
    }
}

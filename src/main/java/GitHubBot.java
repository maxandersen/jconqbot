import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHRepositoryDiscussion;
import org.kohsuke.github.GitHub;

import io.quarkiverse.githubapp.event.Discussion;
import io.quarkiverse.githubapp.event.DiscussionComment;
import io.quarkiverse.githubapp.event.Issue;
import io.quarkus.logging.Log;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;

public class GitHubBot {
        
    final String botLogin;

    GitHubBot(@ConfigProperty(name = "quarkus.github-app.app.name") String botName) {
        this.botLogin = botName+"[bot]";
    }

    void onOpen(@Issue.Opened GHEventPayload.Issue issuePayload) throws IOException {
		issuePayload.getIssue().comment("Hello from my jconqbot App");
	}

    void onDiscussion(@Discussion.Created GHEventPayload.Discussion payload, DynamicGraphQLClient gitHubGraphQLClient) throws IOException {
        addComment(gitHubGraphQLClient, payload.getDiscussion(), "Hello from my jconqbot App");
    }

    void onDiscussionComment(@DiscussionComment.Created GHEventPayload.DiscussionComment payload, DynamicGraphQLClient gitHubGraphQLClient, GitHub gh) throws IOException {
        if(botLogin.equals(payload.getSender().getLogin())) {
            Log.info("Ignoring my own comment");
            return;
        }

        Log.info("Adding comment to " + payload.getSender().getLogin());

        addComment(gitHubGraphQLClient, payload.getDiscussion(), "Another Hello from my jconqbot App");
    }

    private void addComment(DynamicGraphQLClient gitHubGraphQLClient, 
                            GHRepositoryDiscussion discussion,
                            String comment) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("discussionId", discussion.getNodeId());
            variables.put("comment", comment);

            Response response = gitHubGraphQLClient.executeSync("""
                    mutation AddComment($discussionId: ID!, $comment: String!) {
                      addDiscussionComment(input: {
                        discussionId: $discussionId,
                        body: $comment }) {
                            clientMutationId
                      }
                    }""", variables);

            if (response.hasError()) {
                Log.info("Discussion #" + discussion.getNumber() + " - Unable to add comment: " + comment
                        + " - " + response.getErrors());
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.warn("Discussion #" + discussion.getNumber() + " - Unable to add comment: " + comment);
        }
    }
}

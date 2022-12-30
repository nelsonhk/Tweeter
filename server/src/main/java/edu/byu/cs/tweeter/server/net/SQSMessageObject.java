package edu.byu.cs.tweeter.server.net;

import java.util.List;

import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class SQSMessageObject {

    private Status post;
    private List<String> followers;

    public SQSMessageObject(Status post, List<String> followers) {
        this.post = post;
        this.followers = followers;
    }

    public Status getPost() {
        return post;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setPost(Status post) {
        this.post = post;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
}

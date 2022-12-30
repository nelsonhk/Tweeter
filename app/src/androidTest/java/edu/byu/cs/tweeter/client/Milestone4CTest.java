package edu.byu.cs.tweeter.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.client.presenter.MainPresenter;
import edu.byu.cs.tweeter.client.presenter.StoryPresenter;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.client.view.main.story.StoryFragment;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.request.LoginRequest;

public class Milestone4CTest {

    private ServerFacade serverFacade;
    private MainPresenter.MainView mainView;
    private MainPresenter mainPresenter;
    private String post;
    private MainPresenter.PostStatusObserver postStatusObserver;
    private CountDownLatch countDownLatch;

    @BeforeEach
    public void setup() {
        serverFacade = new ServerFacade();
        mainView = Mockito.mock(MainPresenter.MainView.class);
        mainPresenter = Mockito.spy(new MainPresenter(mainView));
        postStatusObserver = Mockito.spy(new MainPresenter.PostStatusObserver());
//        Mockito.when(mainPresenter.createPostStatusObserver()).thenReturn(postStatusObserver);
        Mockito.doReturn(postStatusObserver).when(mainPresenter).createPostStatusObserver();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                countDownLatch.countDown();
                return null;
            }
        }).when(mainView).displayInfoToast(Mockito.anyString());
        post = "test status @amy is the best " +
                "https://www.usatoday.com/picture-gallery/life/2020/03/18/cute-puppy-photos-to-make-you-smile/5068675002/";
        // Prepare the countdown latch
        resetCountDownLatch();
    }

    private void resetCountDownLatch() {
        countDownLatch = new CountDownLatch(1);
    }

    private void awaitCountDownLatch() throws InterruptedException {
        countDownLatch.await();
        resetCountDownLatch();
    }

    @Test
    public void postStatusSuccessfulTest() {
        try {
            AuthToken authToken = serverFacade.login(
                    new LoginRequest("@hannah", "password"), "/login").getAuthToken();

            mainPresenter.postStatus(post);
            awaitCountDownLatch();

//            Mockito.verify(postStatusObserver).postStatusSuccess();
            Mockito.verify(mainView).displayInfoToast(Mockito.anyString());
            // verify the methods called on the view as well
            List<Status> statuses = serverFacade.getStory(new GetStatusRequest(authToken,
                    "@hannah", 10, null), "/login").getStatuses();

            User user = new User("hannah", "nelson", "@hannah", "imageurl");
            List<String> urls = new ArrayList<>();
            urls.add("https://www.usatoday.com/picture-gallery/life/2020/03/18/cute-puppy-photos-to-make-you-smile/5068675002/");
            List<String> mentions = new ArrayList<>();
            mentions.add("@amy");
            Status status = new Status(post, user, null, urls, mentions);

            Assertions.assertTrue(statuses.contains(status));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

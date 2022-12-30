package edu.byu.cs.tweeter.client.model.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.util.FakeData;

public class GetStoryServiceTest {

    private User currentUser;
    private StatusService statusServiceSpy;
    private StoryObserver observer;
    private CountDownLatch countDownLatch;

    /**
     * Create a FollowService spy that uses a mock ServerFacade to return known responses to
     * requests.
     */
    @BeforeEach
    public void setup() {
        currentUser = new User("FirstName", "LastName", null);
//        AuthToken currentAuthToken = new AuthToken();

        statusServiceSpy = Mockito.spy(new StatusService());
        observer = new StoryObserver();

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

    /**
     * A {} implementation that can be used to get the values
     * eventually returned by an asynchronous call on the {@link FollowService}. Counts down
     * on the countDownLatch so tests can wait for the background thread to call a method on the
     * observer.
     */
    private class StoryObserver implements ServiceTemplate.PagedServiceObserver {

        private boolean success;
        private String message;
        private List<Status> statuses;
        private boolean hasMorePages;
        private Exception exception;

        @Override
        public void handleFailure(String message) {
            this.success = false;
            this.message = message;
            this.statuses = null;
            this.hasMorePages = false;
            this.exception = null;

            countDownLatch.countDown();
        }

        @Override
        public void getItemsSuccess(List items, boolean hasMorePages) {
            this.success = true;
            this.message = null;
            this.statuses = items;
            this.hasMorePages = hasMorePages;
            this.exception = null;

            countDownLatch.countDown();
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<Status> getStatuses() {
            return statuses;
        }

        public void setStatuses(List<Status> statuses) {
            this.statuses = statuses;
        }

        public boolean isHasMorePages() {
            return hasMorePages;
        }

        public void setHasMorePages(boolean hasMorePages) {
            this.hasMorePages = hasMorePages;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }
    }

    /**
     * Verify that for successful requests, the {}
     * asynchronous method eventually returns the same result as the {}.
     */
    @Test
    public void testGetStory_validRequest_correctResponse() throws InterruptedException {
//        followServiceSpy.getFollowees(currentAuthToken, currentUser, 3, null, observer);
        statusServiceSpy.getStory(currentUser, 10, null, observer);
        awaitCountDownLatch();

        List<Status> expectedStatuses = FakeData.getInstance().getFakeStatuses().subList(0, 3);
        Assertions.assertTrue(observer.isSuccess());
        Assertions.assertNull(observer.getMessage());
        Assertions.assertNotNull(expectedStatuses);
        Assertions.assertTrue(observer.isHasMorePages());
        Assertions.assertNull(observer.getException());
    }

    /**
     * Verify that for successful requests, the the {}
     * method loads the profile image of each user included in the result.
     */
    @Test
    public void testGetStory_validRequest_loadsProfileImages() throws InterruptedException {
//        followServiceSpy.getFollowees(currentAuthToken, currentUser, 3, null, observer);
        statusServiceSpy.getStory(currentUser, 10, null, observer);
        awaitCountDownLatch();

        List<Status> statuses = observer.getStatuses();
        Assertions.assertTrue(statuses.size() > 0);
    }

    /**
     * Verify that for unsuccessful requests, the the {}
     * method returns the same failure response as the server facade.
     */
    @Test
    public void testGetFollowees_invalidRequest_returnsNoFollowees() throws InterruptedException {
        statusServiceSpy.getStory(null, 0, null, observer);
        awaitCountDownLatch();

        Assertions.assertFalse(observer.isSuccess());
        Assertions.assertNull(observer.getMessage());
        Assertions.assertNull(observer.getStatuses());
        Assertions.assertFalse(observer.isHasMorePages());
        Assertions.assertNull(observer.getException());
    }
}


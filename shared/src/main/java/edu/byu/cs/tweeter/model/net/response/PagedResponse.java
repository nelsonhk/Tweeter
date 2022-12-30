package edu.byu.cs.tweeter.model.net.response;

/**
 * A response that can indicate whether there is more data available from the server.
 */
public class PagedResponse<T> extends Response {

//    private List<T> items;
    private final boolean hasMorePages;

    PagedResponse(boolean success, boolean hasMorePages) {
        super(success);
//        this.items = items;
        this.hasMorePages = hasMorePages;
    }

    PagedResponse(boolean success, String message, boolean hasMorePages) {
        super(success, message);
        this.hasMorePages = hasMorePages;
    }

    /**
     * An indicator of whether more data is available from the server. A value of true indicates
     * that the result was limited by a maximum value in the request and an additional request
     * would return additional data.
     *
     * @return true if more data is available; otherwise, false.
     */
    public boolean getHasMorePages() {
        return hasMorePages;
    }

    public boolean isHasMorePages() {
        return hasMorePages;
    }

//    public List<T> getItems() {
//        return items;
//    }
//
//    public void setItems(List<T> items) {
//        this.items = items;
//    }
}

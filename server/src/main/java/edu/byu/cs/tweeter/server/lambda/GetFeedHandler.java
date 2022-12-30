package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.response.GetStatusResponse;
import edu.byu.cs.tweeter.server.service.StatusService;

public class GetFeedHandler extends Handler implements RequestHandler<GetStatusRequest, GetStatusResponse> {

    @Override
    public GetStatusResponse handleRequest(GetStatusRequest getFeedRequest, Context context) {
        System.out.println("feed target alias: " + getFeedRequest.getTargetUserAlias());
        StatusService statusService = new StatusService(mDAOFactory);
        return statusService.getFeed(getFeedRequest);
    }
}

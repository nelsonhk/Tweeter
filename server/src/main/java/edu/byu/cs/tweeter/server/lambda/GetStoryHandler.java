package edu.byu.cs.tweeter.server.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import edu.byu.cs.tweeter.model.net.request.GetStatusRequest;
import edu.byu.cs.tweeter.model.net.response.GetStatusResponse;
import edu.byu.cs.tweeter.server.service.StatusService;

public class GetStoryHandler extends Handler implements RequestHandler<GetStatusRequest, GetStatusResponse> {
    @Override
    public GetStatusResponse handleRequest(GetStatusRequest getStoryRequest, Context context) {
//        System.out.println("story target alias: " + getStoryRequest.getTargetUserAlias());
        StatusService statusService = new StatusService(mDAOFactory);
        return statusService.getStory(getStoryRequest);
    }
}
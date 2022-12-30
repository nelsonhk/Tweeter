#!/bin/bash
arr=(
        "Tweeter-getFollowersCount"
	"Tweeter-logout"
	"Tweeter-register"
	"Tweeter-getFeed"
	"Tweeter-follow"
	"Tweeter-login"
	"Tweeter-getUser"
	"Tweeter-getFollowers"
	"Tweeter-unfollow"
	"Tweeter-postStatus"
	"Tweeter-isFollower"
	"Tweeter-getFollowingCount"
	"Tweeter-getStory"
	"Tweeter-getFollowing"
	"Tweeter-SQSLambda2"
	"Tweeter-SQSLambda3"
	"CreateDummyData"
    )
for FUNCTION_NAME in "${arr[@]}"
do
  aws lambda update-function-code --function-name $FUNCTION_NAME --zip-file fileb://./tweeter-samples-java/server/build/libs/server-all.jar  &
done

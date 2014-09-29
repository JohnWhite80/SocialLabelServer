package com.github.sociallabel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.APIConnectionException;
import cn.jpush.api.common.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;

@Service("jpushService")
public class JPushService {

	private final static Logger LOG = LoggerFactory.getLogger(JPushService.class);
	private final static String masterSecret = "0f3f5a0876a291c61934c9b9";
	private final static String appKey = "71ba3016e5034bba5ce786d5";

	private JPushClient jpushClient = new JPushClient(masterSecret, appKey, 3);

	@Async
	public void pushNotification(String message, String destUserId, String[] alias) {
		PushPayload payload = buildPushObject(message, destUserId, alias);

		try {
			PushResult result = jpushClient.sendPush(payload);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Got result - " + result);
			}
		} catch (APIConnectionException e) {
			LOG.error("Connection error. Should retry later. ", e);
		} catch (APIRequestException e) {
			LOG.error("Error response from JPush server. Should review and fix it. ", e);
			LOG.info("HTTP Status: " + e.getStatus());
			LOG.info("Error Code: " + e.getErrorCode());
			LOG.info("Error Message: " + e.getErrorMessage());
			LOG.info("Msg ID: " + e.getMsgId());
		}
	}

	private PushPayload buildPushObject(String message, String destUserId, String[] alias) {
		return PushPayload
				.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(
						Audience.newBuilder()
								.addAudienceTarget(AudienceTarget.alias(alias))
								.build())
				.setMessage(
						Message.newBuilder().setMsgContent(message)
								.addExtra("destUserId", destUserId).build()).build();
	}

}

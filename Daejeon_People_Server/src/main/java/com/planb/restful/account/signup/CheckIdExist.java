package com.planb.restful.account.signup;

import com.planb.support.routing.Route;
import com.planb.support.user.SignupManager;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

@Route(uri = "/signup/id/check", method = HttpMethod.POST)
public class CheckIdExist implements Handler<RoutingContext> {
	@Override
	public void handle(RoutingContext ctx) {
		String id = ctx.request().getFormAttribute("id");

		if (SignupManager.checkIdExists(id)) {
			ctx.response().setStatusCode(409).end();
			ctx.response().close();
		} else {
			ctx.response().setStatusCode(201).end();
			ctx.response().close();
		}
	}
}

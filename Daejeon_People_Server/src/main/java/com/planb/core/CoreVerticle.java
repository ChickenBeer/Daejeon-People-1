package com.planb.core;

import com.planb.core.handlers.CORSHandler;
import com.planb.core.handlers.LogHandler;
import com.planb.support.routing.Routing;
import com.planb.support.utilities.Config;
import com.planb.support.utilities.Log;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

public class CoreVerticle extends AbstractVerticle {
	public void start() throws Exception {
		Router router = Router.router(vertx);
		int serverPort = Config.getIntValue("serverPort");
		
		router.route().handler(BodyHandler.create().setUploadsDirectory("upload-files"));
		/**
		 * @brief
		 * public interface BodyHandler
		 * A handler which gathers the entire request body and sets it on the RoutingContext.
		 * 
		 * @see
		 * http://vertx.io/docs/apidocs/io/vertx/ext/web/handler/BodyHandler.html
		 */
		
		router.route().handler(CookieHandler.create());
		/**
		 * @brief
		 * public interface CookieHandler
		 * A handler which decodes cookies from the request,
		 * makes them available in the RoutingContext and writes them back in the response.
		 * 
		 * @see
		 * http://vertx.io/docs/apidocs/io/vertx/ext/web/handler/CookieHandler.html
		 */
		
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		/**
		 * @brief
		 * public interface SessionHandler
		 * A handler that maintains a Session for each browser session.
		 * 
		 * @see
		 * http://vertx.io/docs/apidocs/io/vertx/ext/web/handler/SessionHandler.html
		 */

		router.route().handler(CORSHandler.create());
		router.route().handler(LogHandler.create());
		
		Routing.route(router, "com.planb.restful");
		/**
		 * @brief
		 * Using reflection, routing 'Route' annotated classes
		 */
		
		router.route().handler(StaticHandler.create());
		/**
		 * @brief
		 * public class StaticHandler
		 * A handler for serving static resources from the file system or classpath.
		 * 
		 * @see
		 * http://vertx.io/docs/apidocs/io/vertx/rxjava/ext/web/handler/StaticHandler.html
		 */
		
//		new ParserThread().start();
		/**
		 * @brief
		 * Parse TourAPI to local database
		 */
		
		Log.I("Server Started");
		vertx.createHttpServer().requestHandler(router::accept).listen(serverPort);
		/**
		 * @brief
		 * public interface HttpServer
		 * .requestHandler() : Set the request handler for the server
		 * .listen() : Tell the server to start listening
		 * 
		 * @see
		 * http://vertx.io/docs/apidocs/io/vertx/core/http/HttpServer.html
		 */
	}
}

var log = Java.type("io.vertx.core.logging.LoggerFactory").getLogger('javascriptVerticle2');
var server;
var context = Vertx.currentContext();

exports.vertxStart = function() {
    log.info("javascriptVerticle2 is starting...");


    server = vertx.createHttpServer().requestHandler(function (request) {
        request.response()
            .putHeader("js2_worker", context.isWorkerContext() ? "true" : "false")
            .putHeader("js2_instances", context.getInstanceCount().toString())
            .putHeader("js2_config_foo", context.config().foo)
            .putHeader("js2_config_bar", context.config().bar)
            .end("response");
    });

    server.listen(8085, "localhost", function (res, res_err) {
        if (res_err == null) {
            log.info("Server established on http://localhost:8085")
            var testClass = Java.type("io.silverware.microservices.providers.vertx.XMLConfigTest");
            testClass.serversEstablished();
        } else {
            log.error("Failed to bind!");
        }
    });

}

exports.vertxStop = function() {
    log.info('javascriptVerticle2 stop called');
    server.close();
}
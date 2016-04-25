var log = Java.type("io.vertx.core.logging.LoggerFactory").getLogger('javascriptVerticle');
var server;

exports.vertxStart = function() {
    log.info("javascriptVerticle is starting...");

    server = vertx.createHttpServer().requestHandler(function (request) {
        request.response().end("response");
    });

    server.listen(8084, "localhost", function (res, res_err) {
        if (res_err == null) {
            log.info("Server established on http://localhost:8084")
            var testClass = Java.type("io.silverware.microservices.providers.vertx.XMLConfigTest");
            testClass.serversEstablished();
        } else {
            log.error("Failed to bind!");
        }
    });

}

exports.vertxStop = function() {
    log.info('javascriptVerticle stop called');
    server.close();
}
package io.vertx.nubes.handlers.impl;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import io.vertx.nubes.annotations.mixins.ContentType;
import io.vertx.nubes.handlers.AnnotationProcessor;
import static io.vertx.core.http.HttpHeaders.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ContentTypeProcessor implements AnnotationProcessor<ContentType> {

    public void init(RoutingContext context, ContentType contentType) {
        context.put("content-types", Arrays.asList(contentType.value()));
    }

    @Override
    public void preHandle(RoutingContext context) {
        String accept = context.request().getHeader(ACCEPT.toString());
        List<String> contentTypes = context.get("content-types");
        if (accept == null) {
            context.fail(406);
            return;
        }
        List<String> acceptableTypes = Utils.getSortedAcceptableMimeTypes(accept);
        Optional<String> bestType = acceptableTypes.stream().filter(type -> {
            return contentTypes.contains(type);
        }).findFirst();
        if (bestType.isPresent()) {
            context.put("best-content-type", bestType.get());
            context.next();
        } else {
            context.fail(406);
        }
    }

    @Override
    public void postHandle(RoutingContext context) {
        context.response().putHeader(CONTENT_TYPE, (String) context.get("best-content-type"));
        context.next();
    }

    @Override
    public Class<? extends ContentType> getAnnotationType() {
        return ContentType.class;
    }

}

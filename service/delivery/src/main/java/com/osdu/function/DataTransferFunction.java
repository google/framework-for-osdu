package com.osdu.function;

import com.osdu.model.osdu.manifest.ManifestObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Function;

@Component
public class DataTransferFunction implements Function<Message<ManifestObject>, Message<URL>> {

    private final static Logger log = LoggerFactory.getLogger(DataTransferFunction.class);

    @Override
    public Message<URL> apply(Message<ManifestObject> messageSource) {
        try {
            log.info("Received request: {}", messageSource);

            String urlToDownloadFilesFromDelfi = "http://some-url";

            return new GenericMessage<>(new URL(urlToDownloadFilesFromDelfi));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}

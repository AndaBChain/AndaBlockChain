package com.onets.core.wallet.families.ethereum;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;

import java.io.IOException;
import java.io.InputStream;

public abstract class Service implements Web3jService {
    protected final ObjectMapper objectMapper;

    public Service() {
        objectMapper = ObjectMapperFactory.getObjectMapper();
    }

    protected abstract InputStream performIO(String payload) throws IOException;

    @Override
    public <T extends Response> T send(
            Request request, Class<T> responseType) throws IOException {
        String payload = objectMapper.writeValueAsString(request);

        try (InputStream result = performIO(payload)) {
            if (result != null) {
                return objectMapper.readValue(result, responseType);
            } else {
                return null;
            }
        }
    }

}

package io.everytrade.server.plugin.impl.everytrade;

import org.knowm.xchange.service.BaseParamsDigest;
import si.mazi.rescu.RestInvocation;

import javax.crypto.Mac;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class WhaleBooksApiDigest extends BaseParamsDigest {
    private static final byte[] SPACE = " ".getBytes(StandardCharsets.UTF_8);

    public WhaleBooksApiDigest(String base64Key) {
        super(decodeBase64(Objects.requireNonNull(base64Key)), HMAC_SHA_512);
    }

    @Override
    public String digestParams(RestInvocation restInvocation) {
        final Mac sha512Hmac = getMac();

        sha512Hmac.update(restInvocation.getHttpMethod().getBytes(StandardCharsets.UTF_8));
        sha512Hmac.update(SPACE);
        sha512Hmac.update(restInvocation.getInvocationUrl().getBytes(StandardCharsets.UTF_8));
        if (restInvocation.getRequestBody() != null) {
            sha512Hmac.update(SPACE);
            sha512Hmac.update(restInvocation.getRequestBody().getBytes(StandardCharsets.UTF_8));
        }

        return Base64.getEncoder().encodeToString(sha512Hmac.doFinal());
    }
}

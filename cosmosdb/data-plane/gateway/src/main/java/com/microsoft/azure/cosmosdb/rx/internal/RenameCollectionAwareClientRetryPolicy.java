/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal;

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.DocumentCollection;
import com.microsoft.azure.cosmosdb.ISessionContainer;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.caches.RxClientCollectionCache;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Single;

import java.time.Duration;

public class RenameCollectionAwareClientRetryPolicy implements IDocumentClientRetryPolicy {

    private final static Logger logger = LoggerFactory.getLogger(RenameCollectionAwareClientRetryPolicy.class);

    private final IDocumentClientRetryPolicy retryPolicy;
    private final ISessionContainer sessionContainer;
    private final RxClientCollectionCache collectionCache;
    private RxDocumentServiceRequest request;
    private boolean hasTriggered = false;

    public RenameCollectionAwareClientRetryPolicy(ISessionContainer sessionContainer, RxClientCollectionCache collectionCache, IDocumentClientRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
        this.sessionContainer = sessionContainer;
        this.collectionCache = collectionCache;
        this.request = null;
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        this.request = request;
        this.retryPolicy.onBeforeSendRequest(request);
    }

    @Override
    public Single<ShouldRetryResult> shouldRetry(Exception e) {
        return this.retryPolicy.shouldRetry(e).flatMap(shouldRetryResult -> {
            if (!shouldRetryResult.shouldRetry && !this.hasTriggered) {
                DocumentClientException clientException = Utils.as(e, DocumentClientException.class);

                if (this.request == null) {
                    // someone didn't call OnBeforeSendRequest - nothing we can do
                    logger.error("onBeforeSendRequest is not invoked, encountered failure due to request being null", e);
                    return Single.just(ShouldRetryResult.error(e));
                }

                if (clientException != null && this.request.getIsNameBased() &&
                        Exceptions.isStatusCode(clientException, HttpConstants.StatusCodes.NOTFOUND) &&
                        Exceptions.isSubStatusCode(clientException, HttpConstants.SubStatusCodes.READ_SESSION_NOT_AVAILABLE)) {
                    // Clear the session token, because the collection name might be reused.
                    logger.warn("Clear the token for named base request {}", request.getResourceAddress());

                    this.sessionContainer.clearTokenByCollectionFullName(request.getResourceAddress());

                    this.hasTriggered = true;

                    String oldCollectionRid = request.requestContext.resolvedCollectionRid;

                    request.forceNameCacheRefresh = true;
                    request.requestContext.resolvedCollectionRid = null;

                    Single<DocumentCollection> collectionObs = this.collectionCache.resolveCollectionAsync(request);

                    return collectionObs.flatMap(collectionInfo -> {
                        if (collectionInfo == null) {
                            logger.warn("Can't recover from session unavailable exception because resolving collection name {} returned null", request.getResourceAddress());
                        } else if (!StringUtils.isEmpty(oldCollectionRid) && !StringUtils.isEmpty(collectionInfo.getResourceId())) {
                            return Single.just(ShouldRetryResult.retryAfter(Duration.ZERO));
                        }
                        return Single.just(shouldRetryResult);
                    }).onErrorResumeNext(throwable -> {
                        // When resolveCollectionAsync throws an exception ignore it because it's an attempt to recover an existing
                        // error. When the recovery fails we return ShouldRetryResult.noRetry and propagate the original exception to the client

                        logger.warn("Can't recover from session unavailable exception because resolving collection name {} failed with {}", request.getResourceAddress(), throwable.getMessage());
                        if (throwable instanceof Exception) {
                            return Single.just(ShouldRetryResult.error((Exception) throwable));
                        }
                        return Single.error(throwable);
                    });
                }
            }
            return Single.just(shouldRetryResult);
        });
    }
}

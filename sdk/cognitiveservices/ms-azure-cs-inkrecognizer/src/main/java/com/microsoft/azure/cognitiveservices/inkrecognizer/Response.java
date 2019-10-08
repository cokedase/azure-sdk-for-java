// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.cognitiveservices.inkrecognizer;

public class Response<T> {
    public final int status;
    public final String body;
    public final T root;

    public Response(int code, String content, T value) {
        status = code;
        body = content;
        root = value;
    }
}
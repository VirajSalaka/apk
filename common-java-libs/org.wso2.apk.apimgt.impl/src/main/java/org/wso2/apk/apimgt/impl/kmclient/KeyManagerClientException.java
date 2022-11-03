/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.apk.apimgt.impl.kmclient;

/**
 * This is the custom exception class for exceptions in the Feign Key Manager Client.
 */
public class KeyManagerClientException extends Exception {

    private int statusCode;
    private String reason;

    public KeyManagerClientException() {

        super();
    }

    public KeyManagerClientException(String message) {

        super(message);
    }

    public KeyManagerClientException(String message, Exception e) {

        super(message, e);
    }

    public KeyManagerClientException(int statusCode, String reason) {

        super("Received status code: " + statusCode + " Reason: " + reason);
        this.reason = reason;
        this.statusCode = statusCode;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public void setStatusCode(int statusCode) {

        this.statusCode = statusCode;
    }

    public String getReason() {

        return reason;
    }

    public void setReason(String reason) {

        this.reason = reason;
    }
}

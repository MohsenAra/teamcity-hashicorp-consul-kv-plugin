/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.teamcity.consul.kv.support;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.teamcity.consul.kv.UtilKt;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class ConsulKvTemplate {


    private final RestTemplate template;
    private final String endpoint;

    public ConsulKvTemplate(String endpoint,
                         @NotNull ClientHttpRequestFactory clientHttpRequestFactory
                         ) {
                            this.endpoint=endpoint;
        this.template = UtilKt.createRestTemplate(endpoint, clientHttpRequestFactory);
    }
    
    public String read(String path) {

        Assert.hasText(path, "Path must not be empty");
        String  res= doRead(endpoint+ path+"?raw=true", String.class);
        return res;
    }
    public String GetPath(String path) {

        return endpoint+ path;
    }
    private <T> T doRead(final String path, final Class<T> responseType) {
            return  template.getForObject(path,responseType);
    }
}


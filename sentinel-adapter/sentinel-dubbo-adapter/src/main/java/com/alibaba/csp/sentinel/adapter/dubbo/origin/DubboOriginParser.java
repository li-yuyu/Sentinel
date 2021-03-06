/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.dubbo.origin;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * Customized origin parse in Dubbo provider filter. {@link Context#getOrigin()}
 *
 * @author tiecheng
 */
public interface DubboOriginParser {

    /**
     * Handle the origin parse.
     *
     * @param invoker    Dubbo invoker
     * @param invocation Dubbo invocation
     * @return parse result
     */
    String parse(Invoker<?> invoker, Invocation invocation);

}

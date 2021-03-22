package com.springboot.dgumarket.interceptor;

import com.sun.istack.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class HandlerExceptionResolverComposite implements HandlerExceptionResolver, Ordered {

    @Nullable
    private List<HandlerExceptionResolver> resolvers;
    // AbstractHandlerExceptionResolver - private int order = 2147483647 = Ordered.LOWEST_PRECEDENCE;
    private int order = Ordered.LOWEST_PRECEDENCE;

    public void setExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        log.info("HandlerExceptionResolverComposite : {}",  "setExceptionResolvers()");
        this.resolvers = exceptionResolvers;
    }

    public List<HandlerExceptionResolver> getExceptionResolvers() {
        log.info("HandlerExceptionResolverComposite : {}",  "getExceptionResolvers()");
        return (this.resolvers != null ? Collections.unmodifiableList(this.resolvers) : Collections.emptyList());
    }

    @Override
    public int getOrder() {
        log.info("HandlerExceptionResolverComposite : {}",  "getOrder()");
        return this.order;
    }

    public void setOrder(int order) {
        log.info("HandlerExceptionResolverComposite : {}",  "setOrder()");
        log.info("order : {}", order);
        this.order = order;
    }

    @Override
    @Nullable
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        log.info("HandlerExceptionResolverComposite : {}",  "resolveException()");


        if (this.resolvers != null) {

            for (HandlerExceptionResolver handlerExceptionResolver : this.resolvers) {
                log.info("handlerExceptionResolver : {}",  handlerExceptionResolver);

                ModelAndView modelAndView = handlerExceptionResolver.resolveException(httpServletRequest, httpServletResponse, o, e);
                if (modelAndView != null) {
                    return modelAndView;
                }
            }
        }
        return null;
    }


}

package com.adaptris.jaxrscp.handler

import com.adaptris.jaxrscp.fixtures.ResourceFixture
import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.NotFoundException
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MultivaluedHashMap

/**
 * Created by irlap on 07/11/2016.
 */
@Unroll
class InvocationHandlerInterceptorTest extends Specification {

    def fixtureClass = ResourceFixture
    def fixtureObj = Mock(ResourceFixture)
    def webTarget = Mock(WebTarget)
    def headers = new MultivaluedHashMap<String, Object>()

    def "beforeCall is run for interceptors"() {
        given:
        def interceptor = Mock(InvocationHandlerInterceptor)
        def handler = spiedHandlerWithInterceptors([interceptor])

        when:
        def response = handler.invoke(fixtureObj, null, [null, null])

        then: "For proceed equal false, flow is stopped and proceed response is returned"
        1 * interceptor.beforeCall(handler, *_) >> {
            return new InterceptResponse(
                    proceed: false,
                    response: "Intercepted response"
            )
        }
        0 * handler.call(*_)
        response == "Intercepted response"

        when:
        response = handler.invoke(fixtureObj, null, [null, null])

        then: "For proceed equal true, flow is continued and call response is returned"
        1 * interceptor.beforeCall(handler, *_) >> new InterceptResponse(proceed: true, response: "Intercepted response")
        1 * handler.call(*_) >> "Handler Response"
        1 * interceptor.afterCall(handler, fixtureObj, *_) >> new InterceptResponse(proceed: true)
        response == "Handler Response"
    }

    def "afterCall is run for interceptors"() {
        given:
        def interceptor = Mock(InvocationHandlerInterceptor)
        def handler = spiedHandlerWithInterceptors([interceptor])

        when:
        def response = handler.invoke(fixtureObj, null, [null, null])

        then: "For proceed equal false, flow is stopped and proceed response is returned"
        1 * interceptor.beforeCall(handler, *_) >> new InterceptResponse(proceed: true, response: "Before Interceptor")
        1 * handler.call(*_) >> "Handler Response"
        1 * interceptor.afterCall(handler, fixtureObj, *_) >> new InterceptResponse(proceed: false, response: "After Interceptor")
        response == "After Interceptor"
    }

    def "onCallException is run for interceptors"() {
        given:
        def interceptor = Mock(InvocationHandlerInterceptor)
        def handler = spiedHandlerWithInterceptors([interceptor])
        def exception = new NotFoundException()

        when:
        def response = handler.invoke(fixtureObj, null, [null, null])

        then: "For proceed equal false, flow is stopped and proceed response is returned"
        1 * interceptor.beforeCall(handler, *_) >> new InterceptResponse(proceed: true, response: "Before Interceptor")
        1 * handler.call(*_) >> {throw exception}
        0 * interceptor.afterCall(handler, fixtureObj, *_) >> new InterceptResponse(proceed: false, response: "After Interceptor")
        1 * interceptor.onCallException(exception, *_) >> new InterceptResponse(proceed: false, response: "On exception call")
        response == "On exception call"

        when:
        response = handler.invoke(fixtureObj, null, [null, null])

        then: "For proceed equal true, flow is not stopped and exception is thrown"
        1 * interceptor.beforeCall(handler, *_) >> new InterceptResponse(proceed: true, response: "Before Interceptor")
        1 * handler.call(*_) >> {throw exception}
        0 * interceptor.afterCall(handler, fixtureObj, *_) >> new InterceptResponse(proceed: false, response: "After Interceptor")
        1 * interceptor.onCallException(exception, *_) >> new InterceptResponse(proceed: true, response: "On exception call")
        thrown(NotFoundException)
    }

    def spiedHandlerWithInterceptors(List<InvocationHandlerInterceptor> interceptors) {
        return Spy(ResourceHandler, constructorArgs: [fixtureClass, webTarget, headers, interceptors])
    }

}

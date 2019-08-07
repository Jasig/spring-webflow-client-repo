# Spring Webflow Client Repository [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.apereo/spring-webflow-client-repo/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.apereo/spring-webflow-client-repo) [![Mergify Status][mergify-status]][mergify]

[mergify]: https://mergify.io
[mergify-status]: https://img.shields.io/endpoint.svg?url=https://gh.mergify.io/badges/apereo/spring-webflow-client-repo&style=flat

This project provides a facility for storing flow execution state on the client
in Spring Webflow 2.x. Flow state is stored as an encoded byte stream in the
flow execution identifier provided to the client when rendering a view. It
effectively provides a replacement for
[`ClientContinuationFlowExecutionRepository`](http://static.springsource.org/spring-webflow/docs/1.0.5/api/org/springframework/webflow/execution/repository/continuation/ClientContinuationFlowExecutionRepository.html)
that was available for Spring Webflow 1.x, but with the following notable improvements:

* Support for conversation management (e.g. flow scope)
* Encryption of encoded flow state to prevent tampering by malicious clients

## Building [![Build Status](https://api.travis-ci.org/apereo/spring-webflow-client-repo.png)](http://travis-ci.org/apereo/spring-webflow-client-repo)

```bash
mvn clean install
```

## Integration
Add spring-webflow-client-repo to your application:

```xml
<dependency>
    <groupId>org.apereo</groupId>
    <artifactId>spring-webflow-client-repo</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Usage
`ClientFlowExecutionRepository` is the core component for proving for client
side flow state storage. The following configuration snippet demonstrates how
to wire up the component in a Webflow project:

```xml
<bean name="flowExecutor" class="org.springframework.webflow.executor.FlowExecutorImpl">
  <constructor-arg ref="flowRegistry" />
  <constructor-arg ref="flowExecutionFactory" />
  <constructor-arg ref="flowExecutionRepository" />
</bean>

<webflow:flow-registry id="flowRegistry">
  <webflow:flow-location path="test-flow.xml" id="test"/>
</webflow:flow-registry>

<bean name="flowExecutionFactory" class="org.springframework.webflow.engine.impl.FlowExecutionImplFactory"
      p:executionKeyFactory-ref="flowExecutionRepository"
      p:executionListenerLoader-ref="listenerLoader"/>

<bean id="flowExecutionRepository" class="org.apereo.spring.webflow.plugin.ClientFlowExecutionRepository">
  <constructor-arg ref="flowExecutionFactory" />
  <constructor-arg ref="flowRegistry" />
  <constructor-arg ref="transcoder" />
</bean>

<bean id="listenerLoader" class="org.springframework.webflow.execution.factory.StaticFlowExecutionListenerLoader">
  <constructor-arg>
    <list>
      <!--
      <ref bean="firstExecutionListener" />
      <ref bean="secondExecutionListener" />
      <ref bean="thirdExecutionListener" />
      -->
    </list>
  </constructor-arg>
</bean>

<bean id="transcoder" class="org.apereo.spring.webflow.plugin.EncryptedTranscoder"
      p:compression="true" />
```

## Requirements
Since the flow state is maintained in the flow execution identifier, the
`execution` parameter MUST be stored in the response rendered to the client as
either a request parameter or hidden form parameter. When rendering a form for
user input, a hidden parameter is convenient:

```html
<form:form modelAttribute="command" action="action.html">
  <input type="hidden" name="execution" value="${flowExecutionKey}" />

  <fieldset>
    ... 
    <div class="button">
      <input type="submit" id="save" name="_eventId_save" value="save"/>
      <input type="submit" name="_eventId_cancel" value="cancel"/>
    </div>
  </fieldset>
</form:form>
```

## Security
Since the server is providing data to the client for which the server is the
authority, the state MUST be validated when it is returned to the server after
a round trip. There are two obvious strategies:

 1. Symmetric encryption using a key known exclusively to the server.
 2. Attaching a digital signature to the state that is verfied on post.

This project provides an implementation using symmetric encryption since it is
simpler to implement and has the desirable property of hiding state details
from the client. The Transcoder component provides a straightforward extension
point to implement other encoding mechanisms.


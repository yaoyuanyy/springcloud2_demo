# spring-cloud-gateway

## GetStarted
```
1. run app

2. curl http://localhost:6001/api-a/add?a=1&b=2&accessToken=12

3. there is 3 on the browser
```


## Ribbon关键技术点

### 负载均衡如何在代码中实现

假设：一个url request,两个spring.application.name=service1的实例服务。 即一个url请求时，Ribbon是怎样实现从两个相同的service1实例中选择一个，进而发起请求的

#### 关键class
默认的IRule: ZoneAvoidanceRule。类型层级如下

```markdown
Object
-AbstractLoadBalancerRule
--ClientConfigEnableRoundRobinRule
----PredicateBasedRule
------ZoneAvoidanceRule
```


默认的ILoadBalancer: ZoneAwareLoadBalancer。类型层级如下
```
Object
-AbstractLoadBalancer
--BaseLoadBalancer
----DynamicServerListLoadBalancer
------ZoneAwareLoadBalancer
```
 
IRule与ILoadBalancer的关系是：
```markdown
AbstractLoadBalancerRule 持有ILoadBalancer属性
BaseLoadBalancer 持有IRule属性

``` 

#### 关键知识点

1. consumer优先调用相同zone的provider，当没有相同zone的provider时，也可以调用到这个provider。代码是如何实现的

实现逻辑见ZonePreferenceServerListFilter.getFilteredListOfServers()方法
```markdown
public List<Server> getFilteredListOfServers(List<Server> servers) {
    List<Server> output = super.getFilteredListOfServers(servers);
    if (this.zone != null && output.size() == servers.size()) {
        List<Server> local = new ArrayList<>();
        for (Server server : output) {
            if (this.zone.equalsIgnoreCase(server.getZone())) {
                local.add(server);
            }
        }
        if (!local.isEmpty()) {
            return local;
        }
    }
    return output;
}
``` 
对，就是这么一个方法搞定的。但前提是知道这个方法调用与被调用，以及返回值的流向。下面说下这个前提

 
#### 关键逻辑代码

##### 入口

LoadBalancerClientFilter.filter(ServerWebExchange exchange, GatewayFilterChain chain)方法

```markdown
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
    String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
    if (url == null || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
        return chain.filter(exchange);
    }
    //preserve the original url
    addOriginalRequestUrl(exchange, url); (1)

    log.trace("LoadBalancerClientFilter url before: " + url);

    // 关键点：根据请求request，选择(负载均衡)出下游的服务
    **final ServiceInstance instance = choose(exchange);** (2)

    if (instance == null) {
        String msg = "Unable to find instance for " + url.getHost();
        if(properties.isUse404()) {
            throw new FourOFourNotFoundException(msg);
        }
        throw new NotFoundException(msg);
    }

    URI uri = exchange.getRequest().getURI();

    // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
    // if the loadbalancer doesn't provide one.
    String overrideScheme = instance.isSecure() ? "https" : "http";
    if (schemePrefix != null) {
        overrideScheme = url.getScheme();
    }

    URI requestUrl = loadBalancer.reconstructURI(new DelegatingServiceInstance(instance, overrideScheme), uri); (3)

    log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
    exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
    return chain.filter(exchange);
}

```
这个方法有3个重点部分。其中(2)最为重要，此逻辑为根据request中的host(service1)获取真正请求的server。


RibbonLoadBalancerClient.choose()方法
```markdown
public ServiceInstance choose(String serviceId, Object hint) {
    Server server = getServer(getLoadBalancer(serviceId), hint);
    if (server == null) {
        return null;
    }
    return new RibbonServer(serviceId, server, isSecure(server, serviceId),
            serverIntrospector(serviceId).getMetadata(server));
}
```
从方法名字能看出，再根据参数和返回值，整个逻辑就出来了。这样的思想非常值得我们借鉴。
这个方法的作用是根据serviceId拿到最终的server。具体步骤为首先根据serviceId获取实际的负载均衡器(ILoadBalancer子类)，再通过这个负载均衡器获取到真正要请求的server服务。
getServer(getLoadBalancer(serviceId), hint)方法分为两步：
- 1.getLoadBalancer(serviceId)；
- 2.getServer(ILoadBalancer, hint)

先看第一步：getLoadBalancer(serviceId)，带着问题看代码：是怎么根据serviceId从哪里取到的LoadBalancer呢

RibbonLoadBalancerClient.getLoadBalancer()方法
```markdown
protected ILoadBalancer getLoadBalancer(String serviceId) {
    // clientFactory为SpringClientFactory
    return this.clientFactory.getLoadBalancer(serviceId);
}

public <C> C getInstance(String name, Class<C> type) {
    // name为ribbonLoadBalancer
    C instance = super.getInstance(name, type);
    if (instance != null) {
        return instance;
    }
}

NamedContextFactory.getInstance()
public <T> T getInstance(String name, Class<T> type) {
    AnnotationConfigApplicationContext context = getContext(name);
    if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
            type).length > 0) {
        return context.getBean(type);
    }
    return null;
}

NamedContextFactory.getContext()
protected AnnotationConfigApplicationContext getContext(String name) {
    if (!this.contexts.containsKey(name)) {
        synchronized (this.contexts) {
            if (!this.contexts.containsKey(name)) {
                this.contexts.put(name, createContext(name));
            }
        }
    }
    return this.contexts.get(name);
}

```
看过代码，答案就出来了：实际的ILoadBalancer实例从ioc容器，也就是从ApplicationContext.BeanFactory从获取。
这里的ILoadBalancer实例为ZoneAwareLoadBalancer对象


第二步：getServer(ILoadBalancer, hint)
在第一步中确定了LoadBalancer实例：ZoneAwareLoadBalancer后，接下来就是使用它获取要请求的server了，往下走

RibbonLoadBalancerClient.getServer()方法
```markdown
ZoneAwareLoadBalancer.getServer()
protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
    // Use 'default' on a null hint, or just pass it on?
    return loadBalancer.chooseServer(hint != null ? hint : "default");
}

BaseLoadBalancer.chooseServer()
public Server chooseServer(Object key) {
    if (rule == null) { // rule是ZoneAvoidanceRule对象
        return null;
    } else {
        try {
            return rule.choose(key);
        } catch (Exception e) {}
    }
}

PredicateBasedRule.choose()方法
public Server choose(Object key) {
    ILoadBalancer lb = getLoadBalancer();
    Optional<Server> server = getPredicate().chooseRoundRobinAfterFiltering(lb.getAllServers(), key);
    if (server.isPresent()) {
        return server.get();
    } else {
        return null;
    }       
}

```
从PredicateBasedRule.choose()看到，进一步的寻找server是用ZoneAvoidancePredicate和他的父类AbstractServerPredicate得到的。越来越接近真相了，往下看

```markdown
AbstractServerPredicate.chooseRoundRobinAfterFiltering()
public Optional<Server> chooseRoundRobinAfterFiltering(List<Server> servers, Object loadBalancerKey) {
     List<Server> eligible = getEligibleServers(servers, loadBalancerKey);
     if (eligible.size() == 0) {
         return Optional.absent();
     }
     return Optional.of(eligible.get(incrementAndGetModulo(eligible.size())));
 }

AbstractServerPredicate.getEligibleServers()
public List<Server> getEligibleServers(List<Server> servers, Object loadBalancerKey) {
    if (loadBalancerKey == null) {
        return ImmutableList.copyOf(Iterables.filter(servers, this.getServerOnlyPredicate()));            
    } else {
        List<Server> results = Lists.newArrayList();
        for (Server server: servers) {
            if (this.apply(new PredicateKey(loadBalancerKey, server))) {
                results.add(server);
            }
        }
        return results;            
    }
}

ZoneAvoidancePredicate.apply()
public boolean apply(@Nullable PredicateKey input) {
    if (!ENABLED.get()) {
        return true;
    }
    String serverZone = input.getServer().getZone();
    if (serverZone == null) {
        // there is no zone information from the server, we do not want to filter
        // out this server
        return true;
    }
    LoadBalancerStats lbStats = getLBStats();
    if (lbStats == null) {
        // no stats available, do not filter
        return true;
    }
    if (lbStats.getAvailableZones().size() <= 1) {
        // only one zone is available, do not filter
        return true;
    }
    Map<String, ZoneSnapshot> zoneSnapshot = ZoneAvoidanceRule.createSnapshot(lbStats);
    if (!zoneSnapshot.keySet().contains(serverZone)) {
        // The server zone is unknown to the load balancer, do not filter it out 
        return true;
    }
    logger.debug("Zone snapshots: {}", zoneSnapshot);
    Set<String> availableZones = ZoneAvoidanceRule.getAvailableZones(zoneSnapshot, triggeringLoad.get(), triggeringBlackoutPercentage.get());
    logger.debug("Available zones: {}", availableZones);
    if (availableZones != null) {
        return availableZones.contains(input.getServer().getZone());
    } else {
        return false;
    }
}  

```
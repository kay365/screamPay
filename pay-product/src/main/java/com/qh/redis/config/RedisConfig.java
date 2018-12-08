package com.qh.redis.config;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qh.pay.service.PayService;
import com.qh.redis.RedisConstants;
import com.qh.redis.properties.TaskProperties;
import com.qh.redis.service.RedisMsg;
import com.qh.redis.service.RedisUtil;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableCaching
@EnableConfigurationProperties(TaskProperties.class)
@Order(1)
public class RedisConfig extends CachingConfigurerSupport {

	@Autowired
	private TaskProperties taskProperties;

	@Autowired
	private RedisProperties redisProperties;

	@Bean
	public KeyGenerator keyGenerator() {
		return new KeyGenerator() {
			@Override
			public Object generate(Object target, Method method, Object... params) {
				StringBuilder sb = new StringBuilder();
				sb.append(target.getClass().getName());
				sb.append(method.getName());
				for (Object obj : params) {
					sb.append(obj.toString());
				}
				return sb.toString();
			}
		};
	}

	@SuppressWarnings("rawtypes")
	@Primary
	@Bean
	public CacheManager cacheManager(RedisTemplate redisTemplate) {
		RedisCacheManager rcm = new RedisCacheManager(redisTemplate);
		// 设置缓存过期时间
		// rcm.setDefaultExpiration(60);//秒
		return rcm;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(@Qualifier("jedisConnectionFactory")JedisConnectionFactory connectionFactory,
			Jackson2JsonRedisSerializer<Object> valueSerializer) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setValueSerializer(valueSerializer);
		// 使用StringRedisSerializer来序列化和反序列化redis的key值
		template.setKeySerializer(new StringRedisSerializer());
		template.afterPropertiesSet();
		RedisUtil.setRedisTemplate(template);
		RedisMsg.setRedisTemplate(template);
		return template;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(@Qualifier("jedisConnectionFactory")JedisConnectionFactory connectionFactory,
			Jackson2JsonRedisSerializer<Object> valueSerializer) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(connectionFactory);
		template.setValueSerializer(valueSerializer);
		// 使用StringRedisSerializer来序列化和反序列化redis的key值
		template.setKeySerializer(new StringRedisSerializer());
		template.afterPropertiesSet();
		return template;
	}
	
	@Bean("redisMessageListenerContainer")
	RedisMessageListenerContainer container(@Qualifier("jedisConnectionFactory")JedisConnectionFactory connectionFactory,
			@Qualifier("messageListener")MessageListener messageListener) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		List<PatternTopic> topics = new ArrayList<PatternTopic>();
		topics.add(new PatternTopic(RedisConstants.channel_order_notify));
		topics.add(new PatternTopic(RedisConstants.channel_order_data));
		topics.add(new PatternTopic(RedisConstants.channel_order_acp_nopass));
		topics.add(new PatternTopic(RedisConstants.channel_order_acp));
		topics.add(new PatternTopic(RedisConstants.channel_order_acp_notify));
		topics.add(new PatternTopic(RedisConstants.channel_order_acp_data));
		topics.add(new PatternTopic(RedisConstants.channel_charge_data));
		container.addMessageListener(messageListener, topics);
		return container;
	}

	@Bean("messageListener")
	MessageListener messageListener(CountDownLatch latch, PayService payService) {
		MessageListener messageListener = new MessageListenerRedis(latch, payService);
		return messageListener;
	}

	@Bean("jedisConnectionFactory")
	JedisConnectionFactory jedisConnectionFactory(){
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory(getSentinelConfig(),jedisPoolConfig());
		connectionFactory.setPassword(redisProperties.getPassword());
		connectionFactory.setDatabase(2);
		connectionFactory.setHostName(redisProperties.getHost());
		connectionFactory.setPort(redisProperties.getPort());
		return connectionFactory;
	}
	
	
	@Bean("jedisNotifyConnectionFactory")
	JedisConnectionFactory jedisNotifyConnectionFactory(){
		JedisConnectionFactory connectionFactory = new JedisConnectionFactory(getSentinelConfig(),jedisPoolConfig());
		connectionFactory.setPassword(redisProperties.getPassword());
		connectionFactory.setHostName(redisProperties.getHost());
		connectionFactory.setPort(redisProperties.getPort());
		return connectionFactory;
	}
	
	@Bean("messageNotifyRedis")
	MessageListener messageNotifyRedis(CountDownLatch latch, PayService payService) {
		MessageListener messageListAdap = new MessageNotifyRedis(latch, payService);
		return messageListAdap;
	}

	@Bean("redisMessageNotifyContainer")
	RedisMessageListenerContainer noitfyContainer(@Qualifier("jedisNotifyConnectionFactory")JedisConnectionFactory connectionFactory,
			@Qualifier("messageNotifyRedis")MessageListener messageNotifyRedis) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(messageNotifyRedis, new PatternTopic(RedisConstants.channel_keyevent_expired));
		return container;
	}
	
	
	@Bean("redisNotifyTemplate")
	RedisTemplate<String, Object> jedisSentinelConnectionFactory(@Qualifier("jedisNotifyConnectionFactory")JedisConnectionFactory connectionFactory,
		@Qualifier("messageNotifyRedis")MessageListener messageNotifyRedis,	Jackson2JsonRedisSerializer<Object> valueSerializer) {
		RedisTemplate<String, Object> redisNotifyTemplate = new RedisTemplate<>();
		redisNotifyTemplate.setConnectionFactory(connectionFactory);
		redisNotifyTemplate.setValueSerializer(valueSerializer);
		// 使用StringRedisSerializer来序列化和反序列化redis的key值
		redisNotifyTemplate.setKeySerializer(new StringRedisSerializer());
		redisNotifyTemplate.afterPropertiesSet();
		RedisUtil.setRedisNotifyTemplate(redisNotifyTemplate);
		return redisNotifyTemplate;
	}

	@Bean
	RedisSentinelConfiguration getSentinelConfig() {
		Sentinel sentinelProperties = redisProperties.getSentinel();
		if (sentinelProperties != null) {
			RedisSentinelConfiguration config = new RedisSentinelConfiguration();
			config.master(sentinelProperties.getMaster());
			config.setSentinels(this.createSentinels(sentinelProperties));
			return config;
		} else {
			return null;
		}
	}

	private List<RedisNode> createSentinels(Sentinel sentinel) {
        ArrayList<RedisNode> sentinels = new ArrayList<>();
        String nodes = sentinel.getNodes();
        String[] var4 = StringUtils.commaDelimitedListToStringArray(nodes);
        int var5 = var4.length;
        for(int var6 = 0; var6 < var5; ++var6) {
            String node = var4[var6];
            try {
                String[] ex = StringUtils.split(node, ":");
                sentinels.add(new RedisNode(ex[0], Integer.valueOf(ex[1]).intValue()));
            } catch (RuntimeException var9) {
                throw new IllegalStateException("Invalid redis sentinel property \'" + node + "\'", var9);
            }
        }
        return sentinels;
    }
	
	@Bean
	JedisPoolConfig jedisPoolConfig(){
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(redisProperties.getPool().getMaxActive());
		config.setMaxIdle(redisProperties.getPool().getMaxIdle());
		config.setMinIdle(redisProperties.getPool().getMinIdle());
		config.setMaxWaitMillis(redisProperties.getPool().getMaxWait());
		return config;
	}
	
	@Bean
	CountDownLatch latch() {
		return new CountDownLatch(1);
	}

	@Bean
	public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
		Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
				Object.class);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		jackson2JsonRedisSerializer.setObjectMapper(mapper);
		return jackson2JsonRedisSerializer;
	}
	@Bean // 配置线程池
	public Executor myTaskAsyncPool() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(taskProperties.getCorePoolSize());
		executor.setMaxPoolSize(taskProperties.getMaxPoolSize());
		executor.setQueueCapacity(taskProperties.getQueueCapacity());
		executor.setKeepAliveSeconds(taskProperties.getKeepAliveSeconds());
		executor.setThreadNamePrefix(taskProperties.getThreadNamePrefix());
		// rejection-policy：当pool已经达到max size的时候，如何处理新任务
		// CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}
}
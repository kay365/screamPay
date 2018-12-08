package com.qh.redis.config;

import java.nio.charset.Charset;

import org.springframework.data.redis.serializer.RedisSerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class FastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Class<T> clazz;

    static {
    	ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
    }
    public FastJson2JsonRedisSerializer() {
    }

    public FastJson2JsonRedisSerializer(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    public byte[] serialize(T t)  {
        if (t == null) {
            return new byte[0];
        }
        return JSON.toJSONString(t, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @SuppressWarnings("unchecked")
	public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }

        String str = new String(bytes, DEFAULT_CHARSET);
        if(clazz==null){
            return (T) JSON.parse(str);
        }
        return (T) JSON.parseObject(str, clazz);
    }

}
package com.qh.pay.api.utils;

import java.io.OutputStream;
import java.text.SimpleDateFormat;

import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonUtil {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	private static ObjectMapper objectMapper = null;

	static {
		objectMapper = new ObjectMapper();
		objectMapper.setDateFormat(new SimpleDateFormat(DateUtil.date_fmt_yyyyMMdd));
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
	}

	public static String stringify(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String stringify(Object object, String... properties) {
		try {
			return objectMapper.writer(new SimpleFilterProvider().addFilter(AnnotationUtils
					.getValue(AnnotationUtils.findAnnotation(object.getClass(), JsonFilter.class)).toString(),
					SimpleBeanPropertyFilter.filterOutAllExcept(properties))).writeValueAsString(object);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static void stringify(OutputStream out, Object object) {
		try {
			objectMapper.writeValue(out, object);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void stringify(OutputStream out, Object object, String... properties) {
		try {
			objectMapper.writer(new SimpleFilterProvider().addFilter(AnnotationUtils
					.getValue(AnnotationUtils.findAnnotation(object.getClass(), JsonFilter.class)).toString(),
					SimpleBeanPropertyFilter.filterOutAllExcept(properties))).writeValue(out, object);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static <T> T parse(String json, Class<T> clazz) {

		if (json == null || json.length() == 0) {
			return null;
		}

		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

}
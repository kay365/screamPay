package com.qh.paythird.tx.utils;

import com.qh.common.utils.R;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

    public static Map<String, Object> stringCastToObject(Map<String, String> stringMap) {
        if (null == stringMap) {
            return null;
        }
        Map<String, Object> objectMap = new HashMap<>();
        for (String key : stringMap.keySet()) {
            objectMap.put(key, stringMap.get(key));
        }
        return objectMap;
    }

    public static Map<String, String> objectCastToString(Map<String, Object> objectMap) {
        if (null == objectMap) {
            return null;
        }
        Map<String, String> stringMap = new HashMap<>();
        for (String key : objectMap.keySet()) {
            stringMap.put(key, objectMap.get(key).toString());
        }
        return stringMap;
    }

    public static R objectCastToR(Map<String, Object> objectMap) {
        if (null == objectMap) {
            return R.ok();
        }
        R r = R.ok();
        for (String key : objectMap.keySet()) {
            r.put(key, objectMap.get(key));
        }
        return r;
    }

    public static R stringCastToR(Map<String, String> stringMap) {
        return objectCastToR(stringCastToObject(stringMap));
    }


    /**
     * @param responseData
     * @return
     * @// TODO: 2018/4/10 将返回结果转换成map
     */
    public static Map<String, String> parseXmlToMap(String responseData) {
        Map<String, String> xmlMap = new HashMap<>();
        try {
            Document dom = DocumentHelper.parseText(responseData);
            Element root = dom.getRootElement();
            Document document = root.getDocument();
            List<Element> elements = root.elements();
            for (Element element : elements) {
                xmlMap.put(element.getName(), element.getText());
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return xmlMap;
    }

}

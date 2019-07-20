package com.xlaoy.starter.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 简单封装Jackson，实现JSON String<->Java Object的Mapper.
 * <p>
 * 封装不同的输出风格, 使用不同的builder函数创建实例.
 */
public class JsonUtil {

    private static Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    private ObjectMapper mapper;

    public JsonUtil(JsonInclude.Include inclusion) {
        mapper = new ObjectMapper();
        //时间处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Long.class, ToStringSerializer.instance);
        javaTimeModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(LocalDateTimeUtils.DTF));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(LocalDateTimeUtils.DTF));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(LocalDateTimeUtils.DF));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(LocalDateTimeUtils.DF));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(LocalDateTimeUtils.TF));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(LocalDateTimeUtils.TF));
        mapper.registerModule(javaTimeModule);
        //BaseEnum处理
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addSerializer(BaseEnum.class, new BaseEnumSerializer());
//        simpleModule.addDeserializer(BaseEnum.class, new BaseEnumDeserializer());
//        mapper.registerModule(simpleModule);
        //时间格式处理
        mapper.setDateFormat(new SimpleDateFormat(LocalDateTimeUtils.NORMAL_DATETIME));

        //设置输出时包含属性的风格
        mapper.setSerializationInclusion(inclusion);
        //设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        //禁止使用int代表Enum的order()來反序列化Enum,非常危險
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, Boolean.TRUE);
        //Enum处理
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, Boolean.TRUE);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, Boolean.TRUE);
        //缩进的特性,默认关闭
//        mapper.configure(SerializationFeature.INDENT_OUTPUT, Boolean.TRUE);

    }

    //用于生成@class
    private JsonUtil addDefaultTyping() {
        mapper.registerModule(new SimpleModule().addSerializer(new NullValueSerializer(null)));
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return this;
    }

    /**
     * 创建输出全部属性到Json字符串的Mapper.
     */
    public static JsonUtil buildNormalMapper() {
        return new JsonUtil(JsonInclude.Include.ALWAYS);
    }

    /**
     * 创建输出全部属性到Json字符串的Mapper.
     */
    public static JsonUtil buildNormalMapperWithDefaultTyping() {
        return new JsonUtil(JsonInclude.Include.ALWAYS).addDefaultTyping();
    }

    /**
     * 创建只输出非空属性到Json字符串的Mapper.
     */
    public static JsonUtil buildNonNullMapper() {
        return new JsonUtil(JsonInclude.Include.NON_NULL);
    }

    /**
     * 创建只输出初始值被改变的属性到Json字符串的Mapper.
     */
    public static JsonUtil buildNonDefaultMapper() {
        return new JsonUtil(JsonInclude.Include.NON_DEFAULT);
    }

    /**
     * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper.
     */
    public static JsonUtil buildNonEmptyMapper() {
        return new JsonUtil(JsonInclude.Include.NON_EMPTY);
    }

    /**
     * 如果对象为Null, 返回"null".
     * 如果集合为空集合, 返回"[]".
     */
    public String toJson(Object object) {

        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("", e);
            throw new BizException("json解析失败");
        }
    }

    /**
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * <p>
     * 如需读取集合如List/Map, 且不是List<String>这种简单类型时,先使用函數constructParametricType构造类型.
     *
     * @see #constructParametricType(Class, Class...)
     */
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            logger.warn("", e);
            throw new BizException("json解析失败");
        }
    }

    public static void main(String[] args) {
    }

    /**
     * 如果JSON字符串为Null或"null"字符串, 返回Null.
     * 如果JSON字符串为"[]", 返回空集合.
     * <p>
     * 如需读取集合如List/Map, 且不是List<String>这种简单类型时,先使用函數constructParametricType构造类型.
     *
     * @see #constructParametricType(Class, Class...)
     */
    @SuppressWarnings("unchecked")
    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return (T) mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            logger.warn("", e);
            throw new BizException("json解析失败");
        }
    }

    public <T> T fromMapToObject(Map map, Class<T> clazz) {
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        String jsonString = toJson(map);
        return this.fromJson(jsonString, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(String jsonString, Class<?> parametrized, Class<?>... parameterClasses) {
        return (T) this.fromJson(jsonString, constructParametricType(parametrized, parameterClasses));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> fromJsonToList(String jsonString, Class<T> classMeta) {
        return (List<T>) this.fromJson(jsonString, constructParametricType(List.class, classMeta));
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(JsonNode node, Class<?> parametrized, Class<?>... parameterClasses) {
        JavaType javaType = constructParametricType(parametrized, parameterClasses);
        try {
            return (T) mapper.treeToValue(node, parametrized);
        } catch (IOException e) {
            logger.warn("", e);
            throw new BizException("json解析失败");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T pathAtRoot(String json, String path, Class<?> parametrized, Class<?>... parameterClasses) {
        JsonNode rootNode = parseNode(json);
        JsonNode node = rootNode.path(path);
        return (T) fromJson(node, parametrized, parameterClasses);
    }

    @SuppressWarnings("unchecked")
    public <T> T pathAtRoot(String json, String path, Class<T> clazz) {
        JsonNode rootNode = parseNode(json);
        JsonNode node = rootNode.path(path);
        return (T) fromJson(node, clazz);
    }

    /**
     * 構造泛型的Type如List<MyBean>, 则调用constructParametricType(ArrayList.class,MyBean.class)
     * Map<String,MyBean>则调用(HashMap.class,String.class, MyBean.class)
     */
    public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        return mapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    /**
     * 當JSON裡只含有Bean的部分屬性時，更新一個已存在Bean，只覆蓋該部分的屬性.
     */
    @SuppressWarnings("unchecked")
    public <T> T update(T object, String jsonString) {
        try {
            return (T) mapper.readerForUpdating(object).readValue(jsonString);
        } catch (JsonProcessingException e) {
            logger.warn("update json string:" + jsonString + " to object:" + object + " error.", e);
        } catch (IOException e) {
            logger.warn("update json string:" + jsonString + " to object:" + object + " error.", e);
        }
        return null;
    }

    /**
     * 輸出JSONP格式數據.
     */
    public String toJsonP(String functionName, Object object) {
        return toJson(new JSONPObject(functionName, object));
    }

    /**
     * 設定是否使用Enum的toString函數來讀寫Enum,
     * 為False時時使用Enum的name()函數來讀寫Enum, 默認為False.
     * 注意本函數一定要在Mapper創建後, 所有的讀寫動作之前調用.
     */
    public void setEnumUseToString(boolean value) {
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, value);
        mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, value);
    }

    /**
     * 取出Mapper做进一步的设置或使用其他序列化API.
     */
    public ObjectMapper getMapper() {
        return mapper;
    }

    public JsonNode parseNode(String json) {
        try {
            return mapper.readValue(json, JsonNode.class);
        } catch (IOException e) {
            logger.warn("", e);
            throw new BizException("json解析失败");
        }
    }

    /**
     * 输出全部属性
     *
     * @param object
     * @return
     */
    public static String toNormalJson(Object object) {
        return new JsonUtil(JsonInclude.Include.ALWAYS).toJson(object);
    }

    /**
     * 输出非空属性
     *
     * @param object
     * @return
     */
    public static String toNonNullJson(Object object) {
        return new JsonUtil(JsonInclude.Include.NON_NULL).toJson(object);
    }

    /**
     * 输出初始值被改变部分的属性
     *
     * @param object
     * @return
     */
    public static String toNonDefaultJson(Object object) {
        return new JsonUtil(JsonInclude.Include.NON_DEFAULT).toJson(object);
    }

    /**
     * 输出非Null且非Empty(如List.isEmpty)的属性
     *
     * @param object
     * @return
     */
    public static String toNonEmptyJson(Object object) {
        return new JsonUtil(JsonInclude.Include.NON_EMPTY).toJson(object);
    }

    public void setDateFormat(String dateFormat) {
        mapper.setDateFormat(new SimpleDateFormat(dateFormat));
    }

    public static String toLogJson(Object object) {
        JsonUtil jsonMapper = new JsonUtil(JsonInclude.Include.NON_EMPTY);
        return jsonMapper.toJson(object);
    }

}



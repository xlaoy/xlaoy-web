package com.xlaoy.starter.controller;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yijun.zhang
 * @version 1.0
 * @date 2019/7/18 20:20
 */
@Service
public class DocService {

    private final static Logger logger = LoggerFactory.getLogger(DocService.class);

    private LinkedHashMap definitionsMap;

    private LinkedHashMap pathsMap;

    private String api;

    //标题
    private String title;

    //请求方式
    private String method;

    //方法说明
    private String summary = "";

    //请求参数
    private Map<String, List<ParmaDesc>> requestMap = new LinkedHashMap();

    //返回参数
    private Map<String, List<ParmaDesc>> responseMap = new LinkedHashMap();

    @Autowired
    private RestTemplate restTemplate;

    public void setApi(String api) {
        this.api = api;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @PostConstruct
    public void init() {
        String uri = "http://127.0.0.1:5000/v2/api-docs";
        String json = restTemplate.getForObject(URI.create(uri), String.class);
        Map<String, LinkedHashMap> map1 = JsonUtil.buildNormalMapper().fromJson(json, Map.class);
        pathsMap = map1.get("paths");
        definitionsMap = map1.get("definitions");
    }

    public void done() {
        LinkedHashMap<String, LinkedHashMap> apiMap = (LinkedHashMap)pathsMap.get(api);
        if(apiMap.size() != 1) {
            throw new BizException("api的请求方式过多");
        }
        List<ParmaDesc> parmaDescList = new ArrayList<>();
        apiMap.forEach((k, v) -> {
            this.setMethod(k);
            if(v.get("summary") != null) {
                this.setSummary(v.get("summary").toString());
            }

            //参数
            List<LinkedHashMap> parameters = (List<LinkedHashMap>)v.get("parameters");
            parameters.forEach(p -> {
                if(!"header".equals(p.get("in").toString())) {
                    ParmaDesc parmaDesc = JsonUtil.buildNormalMapper().fromMapToObject(p, ParmaDesc.class);
                    parmaDescList.add(parmaDesc);
                }
            });
            if(CollectionUtils.isEmpty(parmaDescList)) {
                requestMap.put("Request", parmaDescList);
            } else if(parmaDescList.size() == 1) {
                ParmaDesc desc = parmaDescList.get(0);
                if(!"body".equals(desc.getIn())) {
                    requestMap.put("Request", parmaDescList);
                }
            } else {
                requestMap.put("Request", parmaDescList);
            }
            this.handRequestDTO(parmaDescList);

            //返回值
            LinkedHashMap responses = (LinkedHashMap)v.get("responses");
            LinkedHashMap res_200 = (LinkedHashMap)responses.get("200");
            LinkedHashMap res_schema = (LinkedHashMap)res_200.get("schema");
            if(res_schema != null) {
                String res_$ref = res_schema.get("$ref").toString();
                String[] strs = res_$ref.split("/");
                String refence = strs[strs.length - 1];
                this.anlyResponse(refence);
            }
        });


        String md = getMDString();

        createMDFile(md);


        this.setApi("");
        this.setMethod("");
        this.setSummary("");
        this.setTitle("");
        this.requestMap = null;
        this.requestMap = null;
    }

    private void handRequestDTO(List<ParmaDesc> parmaDescList) {
        if(CollectionUtils.isEmpty(parmaDescList)) {
            return;
        }
        parmaDescList.forEach(parm -> {
            if("body".equals(parm.getIn())) {
                parm.setType(parm.getRefence());
                anlyRequest(parm.getRefence());
            }
        });
    }

    private void anlyRequest(String ref) {
        LinkedHashMap definition = (LinkedHashMap)definitionsMap.get(ref);
        LinkedHashMap properties = (LinkedHashMap)definition.get("properties");
        List<ParmaDesc> plist = new ArrayList<>();
        properties.forEach((key, value) -> {
            LinkedHashMap vmap = (LinkedHashMap)value;
            ParmaDesc desc = new ParmaDesc();
            desc.setName(key.toString());
            desc.setRequired(vmap.get("allowEmptyValue") == null ? false : !(Boolean)vmap.get("allowEmptyValue"));
            desc.setDescription(vmap.get("description") == null ? "" : vmap.get("description").toString());
            desc.setType(vmap.get("type") == null ? "" : vmap.get("type").toString());
            if(vmap.get("$ref") != null) {
                String $ref = vmap.get("$ref").toString();
                String[] strs = $ref.split("/");
                String refence = strs[strs.length - 1];
                desc.setIn("body");
                desc.setType(refence);
                desc.setRefence(refence);
            }
            if(vmap.get("items") != null) {
                LinkedHashMap items = (LinkedHashMap)vmap.get("items");
                String $ref = items.get("$ref").toString();
                String[] strs = $ref.split("/");
                String refence = strs[strs.length - 1];
                desc.setIn("body");
                desc.setType("List<" + refence + ">");
                desc.setRefence(refence);
            }
            plist.add(desc);
        });
        requestMap.put(ref, plist);
        plist.forEach(p -> {
            if("body".equals(p.getIn())) {
                anlyRequest(p.getRefence());
            }
        });
    }


    private void anlyResponse(String ref) {
        LinkedHashMap definition = (LinkedHashMap)definitionsMap.get(ref);
        LinkedHashMap<String, LinkedHashMap> res_properties = (LinkedHashMap)definition.get("properties");
        List<ParmaDesc> resList = new ArrayList<>();
        res_properties.forEach((resk, resv) -> {
            ParmaDesc desc = new ParmaDesc();
            desc.setName(resk);
            desc.setDescription(resv.get("description") == null ? "" : resv.get("description").toString());
            desc.setType(resv.get("type") == null ? "" : resv.get("type").toString());
            if(resv.get("items") != null) {
                LinkedHashMap items = (LinkedHashMap)resv.get("items");
                String $ref = items.get("$ref").toString();
                String[] strs = $ref.split("/");
                String refence = strs[strs.length - 1];
                desc.setIn("body");
                desc.setType("List<" + refence + ">");
                desc.setRefence(refence);
            }
            if(resv.get("$ref") != null) {
                String $ref = resv.get("$ref").toString();
                String[] strs = $ref.split("/");
                String refence = strs[strs.length - 1];
                desc.setIn("body");
                desc.setType(refence);
                desc.setRefence(refence);
            }
            resList.add(desc);
        });

        responseMap.put(ref, resList);
        resList.forEach(p -> {
            if("body".equals(p.getIn())) {
                anlyResponse(p.getRefence());
            }
        });
    }


    private String getMDString() {
        // 初始化模板引擎
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        velocityEngine.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine.init();

        // 获取模板文件
        Template template = velocityEngine.getTemplate("pages/doc.vm");

        // 设置变量，velocityContext是一个类似map的结构
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("api", api);
        velocityContext.put("title", title);
        velocityContext.put("method", method.toUpperCase());
        velocityContext.put("summary", summary);
        velocityContext.put("requestMap", requestMap);
        velocityContext.put("responseMap", responseMap);

        // 输出渲染后的结果
        StringWriter stringWriter = new StringWriter();
        template.merge(velocityContext, stringWriter);

        return stringWriter.toString();
    }


    private void createMDFile(String md) {
        try {
            FileUtils.writeStringToFile(new File("F:\\md\\" + title + ".md"), md, Charset.forName("UTF-8"), false);
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}

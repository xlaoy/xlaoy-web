package com.xlaoy.starter.controller;

/**
 * @author yijun.zhang
 * @version 1.0
 * @date 2019/7/18 21:24
 */
public class ParmaDesc {

    private String name;

    private String in;

    private String type;

    private boolean required;

    private String description;

    private Schema schema;

    private String refence;

    public class Schema {
        private String $ref;

        public String get$ref() {
            return $ref;
        }

        public void set$ref(String $ref) {
            this.$ref = $ref;
            String[] strs = this.$ref.split("/");
            refence = strs[strs.length - 1];
        }

        @Override
        public String toString() {
            return "Schema{" +
                    "$ref='" + $ref + '\'' +
                    '}';
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Schema getSchema() {
        return schema;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public String getRefence() {
        return refence;
    }

    public void setRefence(String refence) {
        this.refence = refence;
    }

    @Override
    public String toString() {
        return "ParmaDesc{" +
                "name='" + name + '\'' +
                ", in='" + in + '\'' +
                ", type='" + type + '\'' +
                ", required=" + required +
                ", description='" + description + '\'' +
                ", schema=" + schema +
                '}';
    }
}

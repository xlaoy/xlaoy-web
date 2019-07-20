package com.xlaoy.starter.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * {@link StdSerializer} adding class information required by default typing. This allows de-/serialization of
 * {@link NullValue}.
 *
 * @author Christoph Strobl
 * @since 1.8
 */
public class NullValueSerializer extends StdSerializer<NullValue> {

    private static final long serialVersionUID = 1999052150548658808L;
    private final String classIdentifier;

    /**
     * @param classIdentifier can be {@literal null} and will be defaulted to {@code @class}.
     */
    public NullValueSerializer(@Nullable String classIdentifier) {

        super(NullValue.class);
        this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
    }

    /*
     * (non-Javadoc)
     * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();
        jgen.writeStringField(classIdentifier, NullValue.class.getName());
        jgen.writeEndObject();
    }
}

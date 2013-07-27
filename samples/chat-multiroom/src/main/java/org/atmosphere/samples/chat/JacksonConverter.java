package org.atmosphere.samples.chat;

import java.io.IOException;

import org.atmosphere.config.managed.Converter;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class JacksonConverter<T> implements Converter<T> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public final Object convert(T object, Class<?> convertToType) {
    try {
      return this.convert(mapper, object, convertToType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract Object convert(ObjectMapper mapper, T object, Class<?> convertToType) throws IOException;
}

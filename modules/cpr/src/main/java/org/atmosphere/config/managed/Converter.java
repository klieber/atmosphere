package org.atmosphere.config.managed;

public interface Converter<T> {
  Object convert(T convertFrom, Class<?> convertToType);
}

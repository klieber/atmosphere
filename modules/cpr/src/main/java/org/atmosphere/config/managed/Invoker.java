/*
 * Copyright 2013 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.config.managed;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invoke a method based on {@link Encoder} and {@link Decoder}
 *
 * @author Jeanfrancois Arcand
 */
public class Invoker {

  private final static Logger logger = LoggerFactory.getLogger(Invoker.class);

  public static Object invokeMethod(Method method, Object objectToInvoke, Object decodedObject) {
    Object objectToEncode = null;
    boolean hasMatch = false;
    try {
      objectToEncode = method.invoke(objectToInvoke, new Object[]{decodedObject});
      hasMatch = true;
    } catch (IllegalAccessException e) {
      logger.trace("", e);
    } catch (InvocationTargetException e) {
      logger.trace("", e);
    } catch (java.lang.IllegalArgumentException e) {
      logger.trace("", e);
    } catch (Throwable e) {
      logger.error("", e);
    }

    if (!hasMatch) {
      logger.trace("No Method's Arguments {} matching {}", method.getName(), objectToInvoke);
    }
    return objectToEncode;
  }

  public static Object convert(List<? extends Converter<?>> converters, Object objectToEncode, Class<?> encodeToType) {
    Object encodedObject = matchConverter(objectToEncode, encodeToType, converters);
    if (encodedObject == null) {
      logger.trace("No Converter matching {}", objectToEncode);
    }
    return encodedObject;
  }

  public static Object all(
                           List<Converter<?>> encoders,
                           List<Converter<?>> decoders,
                           Object instanceType,
                           Object objectToInvoke,
                           Method method) {

    Object decodedObject = convert(decoders, instanceType, method.getParameterTypes()[0]);
    if (instanceType == null) {
      logger.trace("No Encoder matching {}", instanceType);
    }
    decodedObject = decodedObject == null ? instanceType : decodedObject;

    logger.trace("{} .on {}", method.getName(), decodedObject);
    Object objectToEncode = invokeMethod(method, objectToInvoke, decodedObject);

    Object encodedObject = null;
    if (objectToEncode != null) {
      encodedObject = convert(encoders, objectToEncode, method.getReturnType());
    }
    return encodedObject == null ? objectToEncode : encodedObject;
  }

  @SuppressWarnings("unchecked")
  public static Object matchConverter(Object instanceType, Class<?> encodeToType, List<? extends Converter<?>> converters) {
    if (instanceType == null) return null;

    Object encodedObject = converters.size() == 0 ? instanceType : null;
    for (@SuppressWarnings("rawtypes") Converter d : converters) {
      Class<?>[] typeArguments = TypeResolver.resolveArguments(d.getClass(), Converter.class);
      if (instanceType != null && typeArguments.length > 0 && typeArguments[0].isAssignableFrom(instanceType.getClass())) {
        logger.trace("{} is trying to convert {}", d, instanceType);
        encodedObject = d.convert(instanceType, encodeToType);
      }
    }
    return encodedObject;
  }

}

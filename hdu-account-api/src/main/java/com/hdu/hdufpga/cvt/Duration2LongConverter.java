package com.hdu.hdufpga.cvt;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Duration;

@Converter
public class Duration2LongConverter implements AttributeConverter<Duration,Long> {
  @Override
  public Long convertToDatabaseColumn(Duration d) {
    return d.toMillis();
  }
  @Override
  public Duration convertToEntityAttribute(Long l) {
    return Duration.ofMillis(l);
  }
}
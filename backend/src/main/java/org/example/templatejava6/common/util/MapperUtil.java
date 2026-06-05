package org.example.templatejava6.common.util;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;

public class MapperUtil {

    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
    }

    public static <S, D> D map(S source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }

    public static <S, T> void mapToExisting(S source, T target) {
        modelMapper.map(source, target);
    }
}

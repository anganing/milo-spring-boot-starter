package com.iboot.iot.milo.utils;

import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class VariantUtil {
    private VariantUtil() {}

    /**
     * Constructs a numeric type string value of a specified type that is written to OPC-UA
     *
     * @param typeId   BuiltinDataType typeId
     * @param strValue Numeric type string value
     * @return Variant
     * @see org.eclipse.milo.opcua.stack.core.BuiltinDataType
     */
    public static Variant buildVariant(int typeId, String strValue) {
        if (!Pattern.matches("-?(?:\\d+\\.\\d+|\\d+)", strValue)) {
            throw new IllegalArgumentException("The entered string value does not meet the number format requirements");
        }

        Map<Integer, BuiltinDataType> dataTypeMap = Arrays.stream(BuiltinDataType.values())
                .collect(Collectors.toMap(BuiltinDataType::getTypeId, dataType -> dataType));

        return Optional.ofNullable(dataTypeMap.get(typeId))
                .map(dataType -> {
                    Class<?> backingClass = dataType.getBackingClass();
                    try {
                        Method valueOf = backingClass.getDeclaredMethod("valueOf", String.class);
                        return new Variant(valueOf.invoke(null, strValue));
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new RuntimeException("BuiltinDataType does not have a corresponding typeId:" + typeId));
    }
}

package co.fluxis.store.mappers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class PreparedStatementMapper<T> {

    private final Class<T> clazz;

    public PreparedStatementMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Mapea TODOS los valores de la entidad al PreparedStatement
     * Los campos se mapean en el orden en que aparecen en la clase
     */
    public void mapToPreparedStatement(T entity, PreparedStatement pstmt) throws SQLException {
        List<Field> fields = getMappableFields();

        try {
            int paramIndex = 1;

            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                setParameter(pstmt, paramIndex++, value, field.getType());
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Error accediendo a los campos de la entidad", e);
        }
    }

    /**
     * Mapea valores excluyendo campos específicos
     */
    public void mapToPreparedStatement(T entity, PreparedStatement pstmt, String... excludeFields)
            throws SQLException {
        Set<String> excludeSet = new HashSet<>(Arrays.asList(excludeFields));
        List<Field> fields = getMappableFields();

        try {
            int paramIndex = 1;

            for (Field field : fields) {
                if (excludeSet.contains(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                Object value = field.get(entity);
                setParameter(pstmt, paramIndex++, value, field.getType());
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Error accediendo a los campos de la entidad", e);
        }
    }

    /**
     * Mapea solo campos específicos en el orden especificado
     */
    public void mapFields(T entity, PreparedStatement pstmt, String... fieldNames)
            throws SQLException {
        Map<String, Field> fieldMap = getFieldMap();

        try {
            int paramIndex = 1;

            for (String fieldName : fieldNames) {
                Field field = fieldMap.get(fieldName);
                if (field == null) {
                    throw new SQLException("Campo no encontrado: " + fieldName);
                }

                field.setAccessible(true);
                Object value = field.get(entity);
                setParameter(pstmt, paramIndex++, value, field.getType());
            }
        } catch (IllegalAccessException e) {
            throw new SQLException("Error accediendo a los campos de la entidad", e);
        }
    }

    /**
     * Obtiene TODOS los campos mapeables (no static, no transient)
     * Ignora completamente anotaciones JPA como @Id, @GeneratedValue
     */
    private List<Field> getMappableFields() {
        List<Field> mappableFields = new ArrayList<>();

        // Solo obtener campos de la clase actual (no heredados)
        Field[] declaredFields = clazz.getDeclaredFields();

        for (Field field : declaredFields) {
            // SOLO ignorar campos static y transient
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            // Agregar TODOS los demás campos (incluidos @Id, @GeneratedValue, etc.)
            mappableFields.add(field);
        }

        return mappableFields;
    }

    /**
     * Crea un mapa de nombre de campo -> Field
     */
    private Map<String, Field> getFieldMap() {
        Map<String, Field> fieldMap = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    !java.lang.reflect.Modifier.isTransient(field.getModifiers())) {
                fieldMap.put(field.getName(), field);
            }
        }

        return fieldMap;
    }

    /**
     * Setea el parámetro en el PreparedStatement según el tipo
     */
    private void setParameter(PreparedStatement pstmt, int paramIndex, Object value, Class<?> type)
            throws SQLException {

        if (value == null) {
            pstmt.setNull(paramIndex, getSQLType(type));
            return;
        }

        if (type == Integer.class || type == int.class) {
            pstmt.setInt(paramIndex, (Integer) value);
        } else if (type == Long.class || type == long.class) {
            pstmt.setLong(paramIndex, (Long) value);
        } else if (type == Double.class || type == double.class) {
            pstmt.setDouble(paramIndex, (Double) value);
        } else if (type == Float.class || type == float.class) {
            pstmt.setFloat(paramIndex, (Float) value);
        } else if (type == Boolean.class || type == boolean.class) {
            pstmt.setBoolean(paramIndex, (Boolean) value);
        } else if (type == Byte.class || type == byte.class) {
            pstmt.setByte(paramIndex, (Byte) value);
        } else if (type == Short.class || type == short.class) {
            pstmt.setShort(paramIndex, (Short) value);
        } else if (type == String.class) {
            pstmt.setString(paramIndex, (String) value);
        } else if (type == BigDecimal.class) {
            pstmt.setBigDecimal(paramIndex, (BigDecimal) value);
        } else if (type == LocalDate.class) {
            pstmt.setDate(paramIndex, java.sql.Date.valueOf((LocalDate) value));
        } else if (type == LocalDateTime.class) {
            pstmt.setTimestamp(paramIndex, Timestamp.valueOf((LocalDateTime) value));
        } else if (type == java.sql.Date.class) {
            pstmt.setDate(paramIndex, (java.sql.Date) value);
        } else if (type == java.sql.Time.class) {
            pstmt.setTime(paramIndex, (java.sql.Time) value);
        } else if (type == java.sql.Timestamp.class) {
            pstmt.setTimestamp(paramIndex, (java.sql.Timestamp) value);
        } else if (type.isEnum()) {
            pstmt.setString(paramIndex, ((Enum<?>) value).name());
        } else if (type == byte[].class) {
            pstmt.setBytes(paramIndex, (byte[]) value);
        } else {
            pstmt.setObject(paramIndex, value);
        }
    }

    /**
     * Obtiene el tipo SQL para setNull
     */
    private int getSQLType(Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return java.sql.Types.INTEGER;
        } else if (type == Long.class || type == long.class) {
            return java.sql.Types.BIGINT;
        } else if (type == Double.class || type == double.class) {
            return java.sql.Types.DOUBLE;
        } else if (type == Float.class || type == float.class) {
            return java.sql.Types.FLOAT;
        } else if (type == Boolean.class || type == boolean.class) {
            return java.sql.Types.BOOLEAN;
        } else if (type == String.class) {
            return java.sql.Types.VARCHAR;
        } else if (type == BigDecimal.class) {
            return java.sql.Types.DECIMAL;
        } else if (type == LocalDate.class || type == java.sql.Date.class) {
            return java.sql.Types.DATE;
        } else if (type == LocalDateTime.class || type == java.sql.Timestamp.class) {
            return java.sql.Types.TIMESTAMP;
        } else if (type.isEnum()) {
            return java.sql.Types.VARCHAR;
        } else {
            return java.sql.Types.OTHER;
        }
    }

    /**
     * Obtiene la lista de nombres de columnas (útil para generar SQL)
     */
    public List<String> getColumnNames() {
        List<String> columnNames = new ArrayList<>();
        List<Field> fields = getMappableFields();

        for (Field field : fields) {
            String columnName = camelToSnake(field.getName());
            columnNames.add(columnName);
        }

        return columnNames;
    }

    /**
     * Convierte camelCase a snake_case
     */
    private String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
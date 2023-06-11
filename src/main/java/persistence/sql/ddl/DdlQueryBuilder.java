package persistence.sql.ddl;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import persistence.sql.ddl.collection.IdGeneratedValueStrategyMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
public class DdlQueryBuilder {
    private static final String CREATE_TABLE = "create table %s (%s);";
    private static final String BLANK = " ";
    private static final String COMMA = ",";
    private final Map<String, String> idColumns = new LinkedHashMap<>();
    private final Map<String, String> columns = new LinkedHashMap<>();
    private final DefaultJavaToSqlColumnParser javaToSqlDialectMap;
    private final IdGeneratedValueStrategyMap idGeneratedValueStrategyMap;
    private Class<?> entity;

    public DdlQueryBuilder(DefaultJavaToSqlColumnParser javaToSqlDialectMap) {
        this.javaToSqlDialectMap = javaToSqlDialectMap;
        this.idGeneratedValueStrategyMap = new IdGeneratedValueStrategyMap();
    }

    public DdlQueryBuilder create(Class<?> entity) {
        this.entity = entity;
        setFields(entity);
        return this;
    }

    private void setFields(Class<?> entity) {
        final Field[] declaredFields = entity.getDeclaredFields();
        Arrays.stream(declaredFields)
                .forEach(this::addColumns);
    }

    private void addColumns(Field field) {
        if (field.isAnnotationPresent(Id.class)) {
            idColumns.put(getColumnName(field), getColumnTypeAndConstraint(field));
            return;
        }
        columns.put(getColumnName(field), getColumnTypeAndConstraint(field));
    }

    private String getColumnName(Field field) {
        if (!field.isAnnotationPresent(Column.class)) {
            return field.getName().toLowerCase();
        }
        final Column column = field.getAnnotation(Column.class);
        if (column.name().isBlank()) {
            return field.getName().toLowerCase();
        }
        return column.name();
    }

    private String getColumnTypeAndConstraint(Field field) {
        StringBuilder sb = new StringBuilder();
        if (field.isAnnotationPresent(Column.class)) {
            final Column column = field.getAnnotation(Column.class);
            sb.append(javaToSqlDialectMap.parse(field.getType(), column));
        }
        if (!field.isAnnotationPresent(Column.class)) {
            sb.append(javaToSqlDialectMap.parse(field.getType()));
        }

        if (field.isAnnotationPresent(GeneratedValue.class)) {
            final GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            sb.append(BLANK)
                    .append(idGeneratedValueStrategyMap.get(generatedValue.strategy()));
        }
        return sb.toString();
    }

    public String build() {
        if (entity == null) {
            throw new IllegalStateException("Entity is not set");
        }
        String content = addColumns(idColumns) +
                addColumns(columns) +
                addConstraint();
        return String.format(CREATE_TABLE, entity.getSimpleName().toLowerCase(), content);
    }

    private String addConstraint() {
        StringBuilder sb = new StringBuilder();
        if (!idColumns.isEmpty()) {
            sb.append(" constraint pk_")
                    .append(entity.getSimpleName().toLowerCase())
                    .append(" primary key (");
            final String join = String.join(", ", idColumns.keySet());
            sb.append(join);
            sb.append(")");
        }
        return sb.toString();
    }

    private String addColumns(Map<String, String> columns) {
        StringBuilder sb = new StringBuilder();
        columns.forEach((key, value) -> {
            sb.append(key);
            sb.append(BLANK);
            sb.append(value);
            sb.append(COMMA);
        });
        return sb.toString();
    }

    public Map<String, String> getIdColumns() {
        return idColumns;
    }

    public Map<String, String> getColumns() {
        return columns;
    }
}
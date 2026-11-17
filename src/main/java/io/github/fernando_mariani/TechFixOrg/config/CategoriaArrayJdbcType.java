package io.github.fernando_mariani.TechFixOrg.config;

import org.hibernate.dialect.type.PostgreSQLArrayJdbcType;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class CategoriaArrayJdbcType extends PostgreSQLArrayJdbcType {

    public CategoriaArrayJdbcType() {
        super(VarcharJdbcType.INSTANCE);
    }

    @Override
    protected String getElementTypeName(JavaType<?> javaType, SharedSessionContractImplementor session) {
        return "categoria";
    }
}

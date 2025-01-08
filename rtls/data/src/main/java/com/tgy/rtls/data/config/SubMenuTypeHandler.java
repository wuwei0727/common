package com.tgy.rtls.data.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgy.rtls.data.entity.user.MenuVO;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SubMenuTypeHandler extends BaseTypeHandler<List<MenuVO.SubMenuVO>> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<MenuVO.SubMenuVO> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, convertToJson(parameter));
    }

    @Override
    public List<MenuVO.SubMenuVO> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertFromJson(rs.getString(columnName));
    }

    @Override
    public List<MenuVO.SubMenuVO> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertFromJson(rs.getString(columnIndex));
    }

    @Override
    public List<MenuVO.SubMenuVO> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertFromJson(cs.getString(columnIndex));
    }

    private String convertToJson(List<MenuVO.SubMenuVO> subMenus) {
        try {
            return objectMapper.writeValueAsString(subMenus);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private List<MenuVO.SubMenuVO> convertFromJson(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, MenuVO.SubMenuVO.class));
        } catch (Exception e) {
            return null;
        }
    }
}

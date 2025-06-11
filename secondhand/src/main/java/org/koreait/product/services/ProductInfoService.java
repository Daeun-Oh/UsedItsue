package org.koreait.product.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.product.controllers.ProductSearch;
import org.koreait.global.search.ListData;
import org.koreait.global.search.Pagination;
import org.koreait.product.constants.ProductStatus;
import org.koreait.product.entities.Product;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
public class ProductInfoService {

    private final JdbcTemplate jdbcTemplate;

    // HttpServletRequest를 파라미터로 받는 방식으로 변경
    public ListData<Product> getList(ProductSearch search, HttpServletRequest request) {
        int page = Math.max(search.getPage(), 1);
        int limit = search.getLimit();
        limit = limit < 1 ? 20 : limit;
        int offset = (page - 1) * limit;

        List<String> addWhere = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String sopt = search.getSopt();
        String skey = search.getSkey();

        sopt = StringUtils.hasText(sopt) ? sopt : "ALL";
        if (StringUtils.hasText(skey)) {
            if (sopt.equals("NAME")) {
                addWhere.add("name LIKE ?");
            } else if (sopt.equals("CATEGORY")) {
                addWhere.add("category LIKE ?");
            } else {
                addWhere.add("(name LIKE ? OR category LIKE ?)");
                params.add("%" + skey + "%");  // 통합검색용 추가 파라미터
            }
            params.add("%" + skey + "%");
        }

        // 삭제되지 않은 상품만
        addWhere.add("deletedAt IS NULL");

        StringBuffer sb = new StringBuffer(2000);
        StringBuffer sb2 = new StringBuffer(2000);
        sb.append("SELECT * FROM PRODUCT");
        sb2.append("SELECT COUNT(*) FROM PRODUCT");

        if (!addWhere.isEmpty()) {
            String where = " WHERE " + String.join(" AND ", addWhere);
            sb.append(where);
            sb2.append(where);
        }

        sb.append(" ORDER BY createdAt DESC LIMIT ?, ?");

        // 전체 개수 조회
        int total = jdbcTemplate.queryForObject(sb2.toString(), int.class, params.toArray());

        // 페이징 파라미터 추가
        params.add(offset);
        params.add(limit);

        // 실제 데이터 조회
        List<Product> items = jdbcTemplate.query(sb.toString(), this::mapper, params.toArray());

        // 페이징 정보 생성
        Pagination pagination = new Pagination(page, total, 10, 20, request);

        return new ListData<>(items, pagination);
    }

    // RowMapper 구현
    private Product mapper(ResultSet rs, int rowNum) throws SQLException {
        Product product = new Product();
        product.setSeq(rs.getLong("seq"));
        product.setGid(rs.getString("gid"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setStatus(ProductStatus.valueOf(rs.getString("status")));
        product.setConsumerPrice(rs.getInt("consumerPrice"));
        product.setSalePrice(rs.getInt("salePrice"));
        product.setDescription(rs.getString("description"));

        // 날짜 필드 처리
        if (rs.getTimestamp("createdAt") != null) {
            product.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
        }
        if (rs.getTimestamp("modifiedAt") != null) {
            product.setModifiedAt(rs.getTimestamp("modifiedAt").toLocalDateTime());
        }
        if (rs.getTimestamp("deletedAt") != null) {
            product.setDeletedAt(rs.getTimestamp("deletedAt").toLocalDateTime());
        }

        return product;
    }
}
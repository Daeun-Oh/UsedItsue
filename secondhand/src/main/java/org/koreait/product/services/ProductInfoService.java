package org.koreait.product.services;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.koreait.global.search.ListData;
import org.koreait.global.search.Pagination;
import org.koreait.product.constants.ProductStatus;
import org.koreait.product.controllers.ProductSearch;
import org.koreait.product.entities.Product;
import org.koreait.product.repositories.ProductRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Lazy
@Service
@RequiredArgsConstructor
public class ProductInfoService {

    private final ProductRepository repository;
    private final JdbcTemplate jdbcTemplate;
    private final HttpServletRequest request;

    public List<Product> searchProducts(String name, String category) {
        boolean hasName = name != null && !name.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (!hasName && !hasCategory) {
            return repository.findAll();
        } else if (hasName && hasCategory) {
            return repository.findByNameAndCategory(name, category);
        } else if (hasName) {
            return repository.findByName(name);
        } else {
            return repository.findByCategory(category);
        }
    }

    /**
     * 회원 목록
     *
     * @param search
     * @return
     */
    public ListData<Product> getList(ProductSearch search, HttpServletRequest request) {
        int page = Math.max(search.getPage(), 1);   // 기본으로 page 1 지정
        int limit = search.getLimit();
        limit = limit < 1 ? 20 : limit;
        int offset = (page - 1) * limit;    // 레코드 시작 번호

        List<String> addWhere = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String sopt = search.getSopt();
        String skey = search.getSkey();

        List<ProductStatus> statusList = search.getStatusList();
        if (statusList != null && !statusList.isEmpty()) {
            String placeholders = statusList.stream().map(s -> "?").collect(Collectors.joining(", "));
            addWhere.add("status IN (" + placeholders + ")");
            params.addAll(statusList.stream().map(Enum::name).toList());
        }

        /**
         * 키워드 검색
         * sopt: 검색 옵션
         * - NAME: 상품명
         * - CATEGORY: 카테고리
         * - ALL: 통합검색 (NAME + CATEGORY)
         * - STATUS : 상품상태
         */
        sopt = StringUtils.hasText(sopt) ? sopt : "ALL";
        if (StringUtils.hasText(skey)) {  // 검색 키워드가 있는 경우

            // 상품명 검색
            if (sopt.equals("NAME")) {
                addWhere.add("name LIKE ?");
            }

            // 카테고리 검색
            else if (sopt.equalsIgnoreCase("CATEGORY")) {
                addWhere.add("category LIKE ?");
            }

            // 통합 검색
            else {
                addWhere.add("CONCAT(name, category) LIKE ?");  // CONCAT: 문자열 병합
            }

            params.add("%" + skey + "%");
        }

        // 삭제 처리된 레코드는 리스트에서 제외
        // addWhere.add("deletedAt IS NULL");

        StringBuffer sb = new StringBuffer(2000);
        StringBuffer sb2 = new StringBuffer(2000);
        sb.append("SELECT * FROM PRODUCT");
        sb2.append("SELECT COUNT(*) FROM PRODUCT");

        if (!addWhere.isEmpty()) {
            String where = " WHERE " + String.join(" AND ", addWhere);
            sb.append(where);
            sb2.append(where);
        }

        sb.append(" ORDER BY createdAt DESC");
        sb.append(" LIMIT ?, ?"); // 첫 번째 ?: offset, 두 번째 ?: limit

        // 검색 조건에 따른 전체 레코드 개수 조회
        int total = jdbcTemplate.queryForObject(sb2.toString(), int.class, params.toArray());

        // 페이징 파라미터 추가
        params.add(offset);  // 아래 쿼리의 첫 번째 물음표
        params.add(limit);   // 아래 쿼리의 두 번째 물음표

        // 실제 데이터 조회
        List<Product> items = jdbcTemplate.query(sb.toString(), this::mapper, params.toArray());

        // 페이징 정보 생성
        Pagination pagination = new Pagination(page, total, 10, 20, request);


        return new ListData<>(items, pagination);
    }

    // RowMapper 구현
    private Product mapper(ResultSet rs, int i) throws SQLException {
        Product item = new Product();
        item.setSeq(rs.getLong("seq"));
        item.setGid(rs.getString("gid"));
        item.setName(rs.getString("name"));
        item.setCategory(rs.getString("category"));
        item.setStatus(ProductStatus.valueOf(rs.getString("status")));
        item.setConsumerPrice(rs.getInt("consumerPrice"));
        item.setSalePrice(rs.getInt("salePrice"));
        item.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("createdAt");
        Timestamp modifiedAt = rs.getTimestamp("modifiedAt");
        Timestamp deletedAt = rs.getTimestamp("deletedAt");

        item.setImagePath(rs.getString("imagePath"));

        item.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        item.setModifiedAt(modifiedAt == null ? null : modifiedAt.toLocalDateTime());
        item.setDeletedAt(deletedAt == null ? null : deletedAt.toLocalDateTime());

        return item;
    }
}

package org.koreait.global.search;

import groovy.transform.ToString;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@ToString
public class Pagination {

    private int page;           // 현재 페이지 번호
    private int total;          // 총 레코드 개수
    private int range;          // 페이지 구간
    private int limit;          // 한 페이지 내 레코드 개수
    private int firstRangePage; // 현재 구간의 첫 번째 페이지 번호
    private int lastRangePage;  // 현재 구간의 마지막 페이지 번호
    private int prevRangePage;  // 이전 구간의 마지막 페이지 번호
    private int nextRangePage;  // 다음 구간의 첫 번째 페이지 번호
    private int lastPage;       // 마지막 구간의 마지막 페이지 번호
    private String baseUrl;     // 웹페이지 구성에 필요한 쿼리스트링


    public Pagination(int page, int total) {
        this(page, total, 0, 0);
    }

    public Pagination(int page, int total, int range, int limit) {
        this(page, total, range, limit, null);
    }

    /**
     *
     * @param page : 페이지 번호 (1, 2, 3, ...)
     * @param total : 총 레코드 개수
     * @param range : 페이지 구간 (10 -> 1~10, 11~20, ...)
     * @param limit : 한 페이지당 보여줄 레코드 개수
     * @param request : 주소의 쿼리스트링 유지를 위해 주소를 불러올 용도
     */
    public Pagination(int page, int total, int range, int limit, HttpServletRequest request) {

        /* 기본값 설정 S */
        page = Math.max(page, 1);           // 페이지 번호가 1보다 작으면 1로 설정
        range = range < 1 ? 10 : range;     //
        limit = limit < 0 ? 20 : limit;
        if (total < 1) return;              // 레코드(데이터)가 없으면 페이징할 필요가 없음
        /* 기본값 설정 E */


        // 총 페이지 개수 - 레코드 개수에 따라 마지막 페이지 구간은 range보다 적을 수 있기 때문에 필요
        int totalPages = (int)Math.ceil(total / (double)limit);


        /* 현재 페이지 구간 정보 S */

        // 페이지 구간 번호 (ex - page=18, range=10일 때는 1번째 페이지 (첫 페이지가 0))
        int rangeNum = (page - 1) / range;

        // 현재 구간에서의 첫 번째 페이지 번호 (ex - page=18, range=10일 때는 11)
        int firstRangePage = rangeNum * range + 1;

        // 현재 구간에서의 마지막 페이지 번호 (ex - page=18, range=10일 때는 20 / 마지막 페이지면, totalPages와 비교해서 작은 값)
        int lastRangePage = Math.min(firstRangePage + range - 1, totalPages);

        /* 현재 페이지 구간 정보 E */


        /* 이전 페이지 구간 정보 S */

        // 이전 구간의 마지막 페이지 번호 (첫 번째 구간에서는 이전 페이지가 존재하지 않음)
        int prevRangePage = rangeNum == 0 ? 0 : firstRangePage - 1;

        /* 이전 페이지 구간 정보 E */


        /* 다음 페이지 구간 정보 S */

        // 다음 구간의 첫 번째 페이지 번호 (마지막 구간에서는 다음 페이지가 존재하지 않음)
        int lastRangeNum = (totalPages - 1) / range;  // 마지막 구간의 페이지 번호
        int nextRangePage = rangeNum < lastRangeNum ? lastRangePage + 1 : 0;

        /* 이전 페이지 구간 정보 E */


        /* 쿼리 스트링 처리 S */

        // ?page=10&post_id=freetalk&t=100&page=11...
        String queryString = request == null ? "" : request.getQueryString();

        // & 기준으로 자르고, "page="가 포함된 걸 제외한 후, 다시 &으로 결합
        String baseUrl = "?";
        if (StringUtils.hasText(queryString)) {
            baseUrl += Arrays.stream(queryString.split("&"))
                    .filter(s -> !s.contains("page="))
                    .collect(Collectors.joining("&")) + "&";
        }

        /* 쿼리 스트링 처리 E */

        this.page =  page;
        this.total = total;
        this.range = range;
        this.limit = limit;
        this.firstRangePage = firstRangePage;
        this.lastRangePage = lastRangePage;
        this.prevRangePage = prevRangePage;
        this.nextRangePage = nextRangePage;
        this.lastPage = totalPages;
        this.baseUrl = baseUrl;
    }

    // ?page=번호

    /**
     * String 배열의 0번째 - 페이지 번호, 1번째 - ?page=번호와 같은 쿼리스트링
     * @return
     */
    public List<String[]> getPages() {
        return total < 1 ? List.of() :  // 레코드가 없으면 빈 배열 반환
                IntStream.rangeClosed(firstRangePage, lastRangePage)
                .mapToObj(i -> new String[] {"" + i, baseUrl + i})
                .toList();
    }
}

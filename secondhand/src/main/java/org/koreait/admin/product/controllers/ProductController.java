package org.koreait.admin.product.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.global.controllers.CommonController;
import org.koreait.global.search.ListData;
import org.koreait.product.constants.ProductAll;
import org.koreait.product.constants.ProductStatus;
import org.koreait.product.controllers.ProductSearch;
import org.koreait.product.controllers.RequestProduct;
import org.koreait.product.entities.Product;
import org.koreait.product.services.ProductInfoService;
import org.koreait.product.services.ProductUpdateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.koreait.product.services.ProductFileUploadService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/product")
public class ProductController extends CommonController {

    private final ProductUpdateService updateService;
    private final ProductInfoService infoService;
    private final HttpServletRequest request;
    private final ProductFileUploadService fileUploadService;

    /**
     * 관리자의 정형화된 틀
     * - mainCode (Override)
     */
    @Override
    @ModelAttribute("mainCode")
    public String mainCode() {
        return "product";
    }

    @ModelAttribute("addCss")
    public List<String> addCss() {
        return List.of("product/style");
    }

    @ModelAttribute("statusList")
    public ProductStatus[] statusList() {
        return ProductStatus.values();
    }

    @ModelAttribute("productAll")
    public ProductAll[] productAlls() {
        return ProductAll.values();
    }

    /**
     * 상품 목록
     */
    @GetMapping({"", "/list"})
    public String list(Model model, ProductSearch search) {
        commonProcess("list", model);

        ListData<Product> data = infoService.getList(search, request);
        model.addAttribute("items", data.getItems());
        model.addAttribute("statusList", ProductStatus.values());
        model.addAttribute("pagination", data.getPagination());

        return "admin/product/list";
    }


    /**
     * 상품 정보 수정 or 삭제 후 이동할 페이지
     */
    @RequestMapping({"", "list"})
    public String listPs(@RequestParam(name = "chk", required = false) List<Integer> chks, Model model) {

        updateService.processBatch(chks);

        // 처리 완료 후에는 부모 창의 목록을 새로고침
        model.addAttribute("script", "parent.location.reload();");
        return "common/_execute_script";
    }

    /**
     * 상품 등록
     */
    @GetMapping("/register")
    public String register(@ModelAttribute RequestProduct form, Model model) {
        commonProcess("register", model);
        form.setGid(UUID.randomUUID().toString()); // 절대 중복하지 않는 유니크 ID 생성
        form.setStatus(ProductStatus.READY);

        return "admin/product/register";
    }

    /**
     * 상품 정보 수정
     */
    @GetMapping("/update/{seq}")  // seq: 상품 등록 번호
    public String update(@PathVariable("seq") Long seq, Model model) {
        commonProcess("update", model);

        return "admin/product/update";
    }

    /**
     * 상품 등록, 수정 처리
     * PostMapping - 양식
     */
    @PostMapping("/save")
    public String saveProduct(@Valid RequestProduct form, Errors errors, Model model) {
        try {
            System.out.println("렛츠고");

            String mode = Objects.requireNonNullElse(form.getMode(), "add");
            commonProcess(mode.equals("edit") ? "register" : "update", model);

            if (errors.hasErrors()) {
                System.out.println("이브이");
                System.out.println("=== 오류 목록 ===");
                errors.getFieldErrors().forEach(error -> {
                    System.out.println("필드: " + error.getField() + ", 오류: " + error.getDefaultMessage());
                });
                return "admin/product/" + (mode.equals("edit") ? "update" : "register");
            }

            if (form.getImage() != null && !form.getImage().isEmpty()) {
                String imagePath = fileUploadService.uploadImage(form.getImage());
                if (imagePath != null && imagePath.length() > 0 && imagePath.charAt(0) == '/') {
                    imagePath = imagePath.substring(1);
                }
                System.out.println(imagePath);
                form.setImagePath(imagePath);
            }

            updateService.process(form);


            // 상품 등록 완료 후 상품 목록으로 이동
            return "redirect:/admin/product";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "admin/product/add";
        }

    }

    /**
     * 공통 처리 부분
     *
     * @param code: 현재 코드 (상품 등록, 상품 수정, 상품 목록)
     * @param model
     */
    private void commonProcess(String code, Model model) {
        code = StringUtils.hasText(code) ? code : "list";
        String pageTitle = "";

        List<String> addCommonScript = new ArrayList<>();
        List<String> addScript = new ArrayList<>();

        if (List.of("register", "update").contains(code)) { // 상품 등록 or 상품 수정
            addCommonScript.add("fileManager");
            addScript.add("product/form");  // 파일 업로드 후속 처리 또는 양식 처리 관련
            pageTitle = code.equals("update") ? "상품 정보 수정" : "상품 등록";
        } else if (code.equals("list")) {  // 상품 목록
            pageTitle = "상품 목록";
        }

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("subCode", code);
        model.addAttribute("addCommonScript", addCommonScript);
        model.addAttribute("addScript", addScript);
    }

    /**
     * 선택된 상품들의 상태를 일괄 변경합니다.
     *
     * @param request HTTP 요청 객체, 폼에서 전달된 데이터 포함
     * @param model   뷰로 전달할 데이터 모델
     * @return 상태 변경 후 실행할 스크립트 뷰 경로
     */
    @PatchMapping("/status")
    public String updateStatus(HttpServletRequest request, Model model) {
        String[] chkIndexes = request.getParameterValues("chk");

        if (chkIndexes == null || chkIndexes.length == 0) {
            model.addAttribute("script", "alert('선택된 상품이 없습니다.'); history.back();");
            return "common/_execute_script";
        }

        List<Long> ids = new ArrayList<>();
        List<String> statuses = new ArrayList<>();

        for (String idx : chkIndexes) {
            String seqStr = request.getParameter("seq_" + idx);
            String statusStr = request.getParameter("newStatus_" + idx);

            if (seqStr == null || statusStr == null) continue;

            try {
                Long id = Long.parseLong(seqStr);
                ids.add(id);
                statuses.add(statusStr);
            } catch (NumberFormatException ignore) {
            }
        }

        updateService.updateStatus(ids, statuses); // 리스트로 처리

        return "common/_execute_script";

    }
}

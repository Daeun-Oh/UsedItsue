package org.koreait.admin.product.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.koreait.admin.global.controllers.CommonController;
import org.koreait.global.search.ListData;
import org.koreait.member.constants.Authority;
import org.koreait.member.services.MemberInfoService;
import org.koreait.member.services.MemberUpdateService;
import org.koreait.product.constants.ProductStatus;
import org.koreait.product.entities.Product;
import org.koreait.product.services.ProductInfoService;
import org.koreait.product.services.ProductUpdateService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 상품 목록
     */
    @GetMapping({"", "/list"})
    public String list(Model model, ProductSearch search) {
        commonProcess("list", model);

        ListData<Product> data=infoService.getList(search);
        model.addAttribute("items", data.getItems());
        model.addAttribute("pagination", data.getPagination());

        return "admin/product/list";
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
        String mode = Objects.requireNonNullElse(form.getMode(), "add");
        commonProcess(mode.equals("edit") ? "register" : "update", model);

        if (errors.hasErrors()) {
            return "admin/product/" + (mode.equals("edit") ? "update" : "register");
        }

        updateService.process(form);

        // 상품 등록 완료 후 상품 목록으로 이동
        return "redirect:/admin/product";
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
}

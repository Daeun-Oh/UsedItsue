window.addEventListener("DOMContentLoaded", function() {
    /* 전체 토글 공통 기능 처리 S */
    const checkAll = document.getElementById("check-all");
    const targetName = checkAll.dataset.targetName;
    const childCheckboxes = document.getElementsByName(targetName);

    // 1. check-all 클릭 → 하위 체크박스 전체 체크/해제
    checkAll.addEventListener("change", function () {
        for (const chk of childCheckboxes) {
            chk.checked = this.checked;
        }
    });

    // 2. 하위 체크박스 중 하나라도 변경되면 → check-all 상태 갱신
    for (const chk of childCheckboxes) {
        chk.addEventListener("change", function () {
            const allChecked = Array.from(childCheckboxes).every(c => c.checked);
            checkAll.checked = allChecked;
        });
    }
    /* 전체 토글 공통 기능 처리 E */

    /* 공통 양식 처리 S */
    const processFormButtons = document.getElementsByClassName("process-form");
    for (const el of processFormButtons) {
        el.addEventListener("click", function() {
            // 버튼의 "class=..." 부분에 delete가 포함되어 있으면 DELETE, 아니면 PATCH
            const method = this.classList.contains("delete") ? "DELETE" : "PATCH";
            const {formName} = this.dataset;
            const formEl = document.forms[formName];
            if (!formEl) {
                        alert(`폼 "${formName}"을 찾을 수 없습니다.`);
                        return;
                    }
            const methodInput = formEl.querySelector('input[name="_method"]');
            if (!methodInput) {
                alert(`폼 "${formName}"에 _method 입력 필드가 없습니다.`);
                return;
            }

            methodInput.value = method;
            alert('정말 처리하겠습니까?', () => formEl.submit());
        });
    }

    /* 공통 양식 처리 E */

    /* 전체 선택 버튼 S */
    const allCheckbox = document.getElementById("status-ALL");
    const otherCheckboxes = document.querySelectorAll("input[name='statusList']:not(#status-ALL)");

    if (allCheckbox) {
        allCheckbox.addEventListener("change", function () {
            otherCheckboxes.forEach(cb => cb.checked = this.checked);
        });

        otherCheckboxes.forEach(cb => {
            cb.addEventListener("change", function () {
                const allChecked = Array.from(otherCheckboxes).every(c => c.checked);
                allCheckbox.checked = allChecked;
            });
        });
    }
    /* 전체 선택 버튼 E */

    /* 판매 상태 토글 S */
    // 상태 변경 후 iframe 응답 수신 시 체크박스 초기화
    const iframe = document.querySelector('iframe[name="ifrmProcess"]');
    if (iframe) {
        iframe.addEventListener('load', function () {
            // 상태 변경 후 iframe 로드 완료되면 체크박스 해제
            document.querySelectorAll('input[name="chk"]:checked').forEach(cb => {
                cb.checked = false;
            });
        });
    }
    /* 판매 상태 토글 E */

    /* text 입력 자동 사이징 S */
    document.addEventListener("input", function (e) {
        if (e.target.matches(".auto-grow")) {
            e.target.style.height = "auto";
            e.target.style.height = e.target.scrollHeight + "px";
        }
        if (e.target.matches(".auto-stretch")) {
            e.target.style.width = "auto";
            e.target.style.width = e.target.scrollWidth + "px";
        }
    });
    /* text 입력 자동 사이징 E */
  
  
    // 상품 상태 변경 시 체크박스 자동 선택
    document.querySelectorAll('.status-select').forEach(select => {
        select.addEventListener('change', function () {
            // id가 'newStatus_숫자' 형태라고 가정
            const nameAttr = this.getAttribute('name'); // newStatus_3
            const index = nameAttr?.split('_')[1]; // "3"
            const checkbox = document.querySelector(`#chk-${index}`);
            if (checkbox) checkbox.checked = true;
        });
    });

});
window.addEventListener("DOMContentLoaded", function() {
    /* 전체 토글 공통 기능 처리 S */
    // 해야 할 일: 모두 체크상태일 때, check-all도 체크 처리 / check-all이 체크 상태일 때, 목록 중 하나 해제 시 check-all도 체크 해제
    const chkAlls = document.getElementsByClassName("check-all");
    for (const el of chkAlls) {
        el.addEventListener("click", function() {
            const {targetName} = this.dataset;
            console.log(targetName);
            const chks = document.getElementsByName(targetName);
            for (const chk of chks) {
                chk.checked = this.checked;
            }
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
        const otherCheckboxes = document.querySelectorAll("input[name='status']:not(#status-ALL)");

        if (allCheckbox) {
            // 전체 선택 클릭 시 → 하위 전체 선택/해제
            allCheckbox.addEventListener("change", function () {
                otherCheckboxes.forEach(cb => cb.checked = this.checked);
            });

            // 하위 체크박스들 중 하나가 바뀌면 전체 상태도 조정
            otherCheckboxes.forEach(cb => {
                cb.addEventListener("change", function () {
                    const allChecked = Array.from(otherCheckboxes).every(c => c.checked);
                    allCheckbox.checked = allChecked;
                });
            });
        }
    /* 전체 선택 버튼 E */

    /* 전체 선택 버튼 S */
    const allCheckbox = document.getElementById("status-ALL");
    const otherCheckboxes = document.querySelectorAll("input[name='status']:not(#status-ALL)");

    if (allCheckbox) {
        // 전체 선택 클릭 시 → 하위 전체 선택/해제
        allCheckbox.addEventListener("change", function () {
            otherCheckboxes.forEach(cb => cb.checked = this.checked);
        });

        // 하위 체크박스들 중 하나가 바뀌면 전체 상태도 조정
        otherCheckboxes.forEach(cb => {
            cb.addEventListener("change", function () {
                const allChecked = Array.from(otherCheckboxes).every(c => c.checked);
                allCheckbox.checked = allChecked;
            });
        });
    }
    /* 전체 선택 버튼 E */

/* text 입력 자동 사이징 S */

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

});
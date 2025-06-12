//window.addEventListener("DOMContentLoaded", function() {
//    const toggleButton = document.getElementById("unlockMode");
//    let editMode = false;
//
//    toggleButton.addEventListener("click", function () {
//        editMode = !editMode;
//
//        const rows = document.querySelectorAll(".table-rows tbody tr");
//        rows.forEach(row => {
//            // 상품명, 분류, 가격, 설명 필드들을 가져옴
//            const nameCell = row.querySelector("td:nth-child(2)");
//            const categoryCell = row.querySelector("td:nth-child(3)");
//            const consumerPriceCell = row.querySelector("td:nth-child(5)");
//            const salePriceCell = row.querySelector("td:nth-child(6)");
//            const descriptionCell = row.querySelector("td:nth-child(7)");
//
//            const fields = [
//                { cell: nameCell, name: "name" },
//                { cell: categoryCell, name: "category" },
//                { cell: consumerPriceCell, name: "consumerPrice" },
//                { cell: salePriceCell, name: "salePrice" },
//                { cell: descriptionCell, name: "description" }
//            ];
//
//            fields.forEach(field => {
//                const cell = field.cell;
//                const name = field.name;
//                const value = cell.innerText.trim();
//
//                if (unlockMode) {
//                    // 기존 텍스트를 input으로 바꿈
//                    cell.innerHTML = `<input type="text" name="${name}" value="${value}" class="unlockMode">`;
//                } else {
//                    const input = cell.querySelector("input");
//                    if (input) {
//                        cell.textContent = input.value;
//                    }
//                }
//            });
//        });
//
//        toggleButton.textContent = unlockMode ? "수정 모드 끄기" : "수정 모드 켜기";
//    });
//});
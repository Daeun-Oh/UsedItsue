window.addEventListener("DOMContentLoaded",function(){
    function showImageModal(src, alt) {
        const modal = document.getElementById('imageModal');
        const modalImg = document.getElementById('modalImage');
        const modalCaption = document.getElementById('modalCaption');

        modal.style.display = 'block';
        modalImg.src = src;
        modalCaption.textContent = alt;

        // ESC 키로 모달 닫기
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                closeImageModal();
            }
        });
    }

    // 이미지 모달 닫기
    function closeImageModal() {
        const modal = document.getElementById('imageModal');
        modal.style.display = 'none';

        // 이벤트 리스너 제거
        document.removeEventListener('keydown', arguments.callee);
    }
});
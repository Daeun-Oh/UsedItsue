window.addEventListener("DOMContentLoaded", function() {

    /* 트렌드 통계 데이터 처리 S */
    const el = document.getElementById("chart-data");
    //if(!el) return;

    let data = JSON.parse(el.dataset.json);
    console.log("불러온 데이터:", data);
    const labels = Object.keys(data);
    const values = Object.values(data);
    /* 트렌드 통계 데이터 처리 E */

    const ctx = document.getElementById('dailyChart');

     new Chart(ctx, {
       type: 'pie',
       data: {
         labels,
         datasets: [{
           label: '핫 트렌드',
           data: values,
           borderWidth: 1
         }]
       },
       options: {
         scales: {
           y: {
             beginAtZero: true
           }
         }
       }
     });
});
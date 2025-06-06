window.addEventListener("DOMContentLoaded", function() {
    /* 트렌드 통계 데이터 처리 S */
    let data = document.getElementById("chart-data").innerHTML;
    data = JSON.parse(data);
    const labels = Object.keys(data);
    const values = Object.values(data);
    /* 트렌드 통계 데이터 처리 E */

    const ctx = document.getElementById('myChart');
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
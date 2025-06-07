// DOM 로딩 완료 시 실행

  
  /* 트렌드 통계 데이터 처리 S */
    let rawData = document.getElementById("chart-data").innerHTML;
    rawData = JSON.parse(rawData);

    console.log("불러온 데이터:", rawData);

    // 1. 날짜 추출 (x축)
    const dates = Object.keys(rawData).sort();

    // 2. 모든 키워드를 중복 없이 수집
    const allKeywordsSet = new Set();
    dates.forEach(date => {
        const keywordsObj = JSON.parse(rawData[date].keywords);
        Object.keys(keywordsObj).forEach(k => allKeywordsSet.add(k));
    });
    const allKeywords = Array.from(allKeywordsSet);

    // 3. 키워드별 날짜에 따른 빈도 배열 만들기 (없으면 0)
    const datasets = allKeywords.map((keyword, i) => {
        const data = dates.map(date => {
            const keywordsObj = JSON.parse(rawData[date].keywords);
            return keywordsObj[keyword] || 0;
        });

        // 임의의 색깔 생성 (Hue 색상 사용)
        const color = `hsl(${(i * 360 / allKeywords.length)}, 70%, 50%)`;

        return {
            label: keyword,
            data: data,
            borderColor: color,
            backgroundColor: color,
            fill: false,
            tension: 0,
            pointRadius: 3,
            borderWidth: 2
        };
    });

    /* 트렌드 통계 데이터 처리 E */

    // Chart.js 그래프 생성
    const ctx = document.getElementById('weeklyChart').getContext('2d');
    const weeklyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: dates,
            datasets: datasets
        },
        options: {
            responsive: true,
            interaction: {
                mode: 'index',
                axis: 'x',
                intersect: false,
            },
            plugins: {
                legend: {
                    position: 'right',
                    maxHeight: 300,
                    labels: {
                        boxWidth: 12,
                        padding: 6,
                        font: {
                            size: 12,
                        }
                    }
                },
                title: {
                    display: true,
                    text: '주간 키워드 빈도수'
                },
                tooltip: {
                    enabled: true,
                    mode: 'nearest',
                    intersect: false,
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: '날짜'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: '빈도수'
                    },
                    beginAtZero: true,
                    ticks: {
                        stepSize: 5
                    }
                }
            }
        }
    });
});

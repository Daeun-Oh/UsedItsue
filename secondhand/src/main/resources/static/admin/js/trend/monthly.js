// DOM 로딩 완료 시 실행
window.addEventListener("DOMContentLoaded", function () {
  /** ------------------------------
   * [1] 오늘의 트렌드 파이 차트 그리기
   * - 대상: <div id="chart-data" data-json="{...}">
   * - 데이터: 키워드별 비율 (JSON)
   -------------------------------- */
  /*
  const pieDataRaw = document.getElementById("chart-data");
  if (pieDataRaw) {
    try {
      const raw = pieDataRaw.dataset.json;  // 문자열 JSON 추출
      const data = JSON.parse(raw);         // 파싱
      const labels = Object.keys(data);     // 키워드명 배열
      const values = Object.values(data);   // 각 키워드 수치

      const ctx = document.getElementById("myChart");
      new Chart(ctx, {
        type: "pie",
        data: {
          labels,
          datasets: [{
            label: "오늘의 트렌드",
            data: values,
            borderWidth: 1
          }]
        },
        options: {
          plugins: {
            legend: {
              position: "bottom"   // 범례 하단 표시
            }
          }
        }
      });
    } catch (err) {
      console.error("파이차트 로딩 오류:", err);
    }
  }

  /** ------------------------------
   * [2] 공통 꺾은선 그래프 함수
   * - 기간별 키워드 누적 추이 시각화
   * - 대상: 일간, 월간 데이터
   * @param {string} containerId - 차트를 그릴 canvas ID
   * @param {string} rawJson - JSON 문자열 (EtcTrend 리스트)
   -------------------------------- */
  /*
  function drawLineChart(containerId, rawJson) {
    try {
      const dataList = JSON.parse(rawJson); // [{ createdAt, keywords: "{...}" }, ...]
      const dateLabels = dataList.map(item => item.createdAt.substring(0, 10));  // 날짜만 추출

      // 모든 키워드를 날짜별로 집계할 객체 생성
      const allKeywords = {};
      dataList.forEach((item, idx) => {
        const keywords = JSON.parse(item.keywords);  // 키워드 JSON → 객체
        Object.entries(keywords).forEach(([word, count]) => {
          if (!allKeywords[word]) allKeywords[word] = Array(dataList.length).fill(0);  // 초기화
          allKeywords[word][idx] = count;  // 해당 날짜에 카운트 저장
        });
      });

      // 상위 5개 키워드만 추출
      const topWords = Object.entries(allKeywords)
        .map(([word, counts]) => [word, counts.reduce((a, b) => a + b, 0)])  // 전체 합산
        .sort((a, b) => b[1] - a[1]) // 내림차순 정렬
        .slice(0, 5)                 // 상위 5개
        .map(([word]) => word);      // 키워드명만 추출

      // Chart.js datasets 구성
      const datasets = topWords.map(word => ({
        label: word,
        data: allKeywords[word],
        fill: false,
        borderWidth: 2
      }));

      // 차트 렌더링
      const ctx = document.getElementById(containerId);
      new Chart(ctx, {
        type: "line",
        data: {
          labels: dateLabels,
          datasets
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              position: "bottom"
            },
            title: {
              display: true,
              text: ctx.dataset.title || ""   // <canvas data-title="..."> 값 사용
            }
          }
        }
      });
    } catch (err) {
      console.error(`꺾은선 그래프(${containerId}) 오류:`, err);
    }
  }

  /** ------------------------------
   * [3] 일주일 트렌드 꺾은선 차트
   * - 대상: <div id="weekly-chart-data" data-json="...">
   -------------------------------- */
  /*
  const weeklyRaw = document.getElementById("weekly-chart-data");
  if (weeklyRaw) {
    drawLineChart("weeklyChart", weeklyRaw.dataset.json);
  }

  /** ------------------------------
   * [4] 한달 트렌드 꺾은선 차트
   * - 대상: <div id="monthly-chart-data" data-json="...">
   -------------------------------- */
  /*
  const monthlyRaw = document.getElementById("monthly-chart-data");
  if (monthlyRaw) {
    drawLineChart("monthlyChart", monthlyRaw.dataset.json);
  }
  
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

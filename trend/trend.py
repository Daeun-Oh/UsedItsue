# 모듈 임포트
import os
import sys
import requests
import json
from time import strftime
from konlpy.tag import Okt
from collections import Counter
from wordcloud import WordCloud
from bs4 import BeautifulSoup as bs

# sys.argv에 값이 넘어오지 않은 경우
if len(sys.argv) < 2:
    sys.exit(1)  # 0: 정상 종료, 1: 비정상 종료

# 워드 클라우드 이미지가 저장될 경로 체크 (없으면 생성)
path = sys.argv[1] if sys.argv[1] else "c:/tmp"  # 리눅스에 없긴 함

if os.path.isdir(path) == False:
    os.mkdir(path)

# 원격 컨텐츠 로드 (입력 받는 걸로 수정해야 함) **
url = sys.argv[2] if len(sys.argv) > 3 and sys.argv[2] else "https://news.naver.com/"  # sys.argv[2] - 형식에 맞지 않으면 예외 처리
html = requests.get(url).text
soup = bs(html, 'html.parser')
body = soup.select_one("body")
text = body.get_text().strip().replace("\n", " ")
stopwords = ['본문', '바로가기', 'NAVER', '검색', '이슈', '닫기', '구독']  # 불용어

# 명사, 동사, 형용사의 단어로 형태소를 분리
okt = Okt()
words = []
for word, pos in okt.pos(text):
    if word not in stopwords and pos in ['Noun', 'Verb', 'Adjective']:  # 명사, 동사, 형용사만 추출
        words.append(word)

# 가장 많이 등장하는 키워드 상위 50개 추출
stat = Counter(words).most_common(50)

# 워드 클라우드 이미지 생성
image_file = strftime('%Y%m%d%H') + "_news.jpg"
wc = WordCloud(font_path='C:/Users/User/Desktop/UsedItsue/trend/fonts/NanumGothic-ExtraBold.ttf', 
                background_color='white', 
                max_font_size=200,
                width=700, height=450)
cloud = wc.generate_from_frequencies(dict(stat))
cloud.to_file(f"{path}/{image_file}")

# JSON 문자열로 출력
data = {"image": image_file, "keywords": dict(stat)}

print(json.dumps(data, ensure_ascii=False))
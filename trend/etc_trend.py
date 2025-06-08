# 모듈 임포트 
import os
import sys
import json
import requests
from konlpy.tag import Okt
from collections import Counter
from bs4 import BeautifulSoup as bs
from wordcloud import WordCloud
from time import strftime

# sys.argv에 값이 넘어오지 않은 경우
# args: [0]=파일명, [1]=저장경로, [2]=크롤링할 url
if len(sys.argv) < 3:
    sys.exit(1)

# 워드 클라우드 이미지가 저장될 경로 체크 및 생성
path = sys.argv[1] if sys.argv[1] else "c:/tmp"

if not os.path.isdir(path):
    os.mkdir(path)

# 원격 컨텐츠 로드 
url = sys.argv[2]   #"https://news.naver.com/"
html = requests.get(url).text
soup = bs(html, 'html.parser')
body = soup.select_one("body")
text = body.get_text().strip().replace("\n", " ")
stopwords = ['본문', '바로가기', 'NAVER', '검색', '이슈', '닫기', '구독'] # 불용어


# 명사, 형용사, 동사의 단어로 형태소 분리
okt = Okt()
words = []
for word, pos in okt.pos(text):
    if word not in stopwords and pos in ['Noun', 'Verb', 'Adjective']:
        words.append(word)

# 가장 많이 등장하는 키워드를 상위 50개 추출 
stat = Counter(words).most_common(50)

# 워드 클라우드 이미지 생성
image_file = strftime("%Y%m%d%H") + "_etc.jpg"
wc = WordCloud(font_path='C:/Users/User/Desktop/UsedItsue/trend/fonts/NanumGothic-ExtraBold.ttf', 
               background_color='white', 
               max_font_size=100, 
               width=500, height=300)
cloud = wc.generate_from_frequencies(dict(stat))
cloud.to_file(f"{path}/{image_file}")

# JSON 문자열로 출력
data = {"image": image_file, "keywords": dict(stat)}

print(json.dumps(data, ensure_ascii=False))
import sys
import json
import os
import uuid
from wordcloud import WordCloud

def main():
    if len(sys.argv) != 3:
        print("Usage: python generate_image.py <output_dir> <keywords_json_path>")
        sys.exit(1)

    output_dir = sys.argv[1]
    json_path = sys.argv[2]

    # JSON 파일 읽기
    with open(json_path, 'r', encoding='utf-8') as f:
        keywords = json.load(f)

    # 워드클라우드 설정
    wc = WordCloud(
        font_path="C:/Users/User/Desktop/UsedItsue/trend/fonts/NanumGothic-ExtraBold.ttf", 
        width=400,
        height=400,
        background_color='white'
    )

    wc.generate_from_frequencies(keywords)

    # 파일명 생성
    filename = f"{uuid.uuid4().hex}_merged.jpg"
    output_path = os.path.join(output_dir, filename)

    # 이미지 저장
    wc.to_file(output_path)

    # 자바에서 결과로 받을 수 있도록 파일명만 출력
    print(filename)

if __name__ == "__main__":
    main()

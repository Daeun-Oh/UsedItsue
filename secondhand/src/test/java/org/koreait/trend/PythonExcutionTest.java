package org.koreait.trend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;

@SpringBootTest
public class PythonExcutionTest {

    @Test
    void test1() throws Exception {
        ProcessBuilder builder = new ProcessBuilder("C:/trend/.venv/Scripts/activate.bat"); // 가상환경 활성화
        Process process = builder.start(); // 명령어 실행
        int statusCode = process.waitFor();
        if (statusCode != 0) return; // 정상 실행이 아닌 경우 다음 실행 X

        builder = new ProcessBuilder("C:/trend/.venv/Scripts/python.exe", "C:/trend/trend.py", "C:/uploads/trend");
        process = builder.start();
        statusCode = process.waitFor();
        if (statusCode == 0) {
            process.inputReader().lines().forEach(System.out::println);
            return;
        }
        BufferedReader br = process.errorReader();
        br.lines().forEach(System.out::println);
    }
}
